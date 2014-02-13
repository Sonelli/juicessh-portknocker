package com.sonelli.portknocker.models;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sonelli.juicessh.pluginlibrary.PluginClient;
import com.sonelli.juicessh.pluginlibrary.PluginContract;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.UUID;

public class KnockSequence {

    public static interface OnConnectListener {
        void onMessage(String message);
        void onFailure(String reason);
        void onComplete();
    }

    public static final String TAG = "KnockSequence";
    public static final String PREFS_NAME = "sequences";
    public static final int SOCKET_TIMEOUT_MS = 1000;

    private ArrayList<KnockItem> items = new ArrayList<KnockItem>();
    private UUID connection;

    public void save(Context context){
        if(context == null || connection == null)
            return;

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        synchronized (this){
            if(items.size() > 0){
                editor.putString(connection.toString(), new Gson().toJson(this));
            } else {
                editor.remove(connection.toString());
            }
        }

        editor.commit();

    }

    public static KnockSequence load(Context context, UUID id){
        if(context == null || id == null)
            return new KnockSequence();

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return new Gson().fromJson(prefs.getString(id.toString(), "{}"), KnockSequence.class);
    }

    public void connect(final Activity activity, final OnConnectListener listener){

        if(activity == null || connection == null)
            return;

        final Handler handler = new Handler();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                final UUID copyConnection;
                final ArrayList<KnockItem> copyItems;

                synchronized (this){
                    copyConnection = UUID.fromString(connection.toString());
                    copyItems = new ArrayList<KnockItem>(items);
                }

                // Look up the connection address
                String host = getAddressForConnection(activity, connection.toString());
                if(host == null){
                    if(listener != null){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onFailure("Connection has an invalid address");
                            }
                        });
                    }
                    return;
                }

                for(final KnockItem item: copyItems){
                    switch(item.getType()){
                        case KnockItem.TYPE_PAUSE:
                            try {
                                if(listener != null){
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            listener.onMessage("Pausing for " + item.getValue() + "ms");
                                        }
                                    });
                                }
                                Thread.sleep((long) item.getValue());
                            } catch (InterruptedException e){}
                            break;

                        case KnockItem.TYPE_UDP_PACKET:
                            try {

                                if(listener != null){
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            listener.onMessage("Sending UDP packet to port " + item.getValue());
                                        }
                                    });
                                }

                                DatagramSocket socket = new DatagramSocket();
                                final InetAddress address = InetAddress.getByName(host);
                                DatagramPacket packet = new DatagramPacket(new byte[]{1}, 1, address, item.getValue());
                                socket.send(packet);
                                socket.close();

                            } catch (final IOException e){
                                if(listener != null){
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            listener.onFailure("Could not send UDP packet: " + e.getMessage());
                                        }
                                    });
                                }
                                return;
                            }

                            break;

                        case KnockItem.TYPE_TCP_PACKET:

                            if(listener != null){
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        listener.onMessage("Sending TCP packet to port " + item.getValue());
                                    }
                                });
                            }

                            try {

                                final InetAddress address = InetAddress.getByName(host);
                                Socket socket = new Socket();
                                socket.connect(new InetSocketAddress(address, item.getValue()), SOCKET_TIMEOUT_MS);
                                socket.close();

                            } catch (IOException e){
                                // We expect this to fail to connect
                                // TODO: SYN-and-forget would be better than waiting for a timeout
                            }

                            break;
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(listener != null){
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onComplete();

                                    // Start the connection in the foreground
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse("ssh://" + connection.toString()));
                                    activity.startActivity(intent);

                                }
                            });
                        }
                    }
                });

            }
        });

        thread.setName("connection-" + connection);
        thread.start();

    }

    private String getAddressForConnection(Activity activity, String id) {

        if(connection == null)
            return null;

        Cursor cursor = activity.getContentResolver().query(
                PluginContract.Connections.CONTENT_URI,
                PluginContract.Connections.PROJECTION,
                PluginContract.Connections.COLUMN_ID + " = ?",
                new String[]{connection.toString()},
                PluginContract.Connections.SORT_ORDER_DEFAULT
        );

        if(cursor != null){
            while(cursor.moveToNext()){
                int column = cursor.getColumnIndex(PluginContract.Connections.COLUMN_ADDRESS);
                if(column > -1){
                    return cursor.getString(column);
                }
            }
        }

        return null;

    }

    public void setConnection(UUID connection) {
        synchronized (this){
            this.connection = connection;
        }
    }

    public void add(int index, KnockItem item){
        synchronized (this){
            items.add(index, item);
        }
    }

    public KnockItem get(int index){
        synchronized (this){
            return items.get(index);
        }
    }

    public void remove(int index){
        synchronized (this){
            items.remove(index);
        }
    }

    public int size(){
        synchronized (this){
            return items.size();
        }
    }

}
