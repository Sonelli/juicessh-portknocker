package com.sonelli.portknocker.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sonelli.juicessh.pluginlibrary.PluginContract;
import com.sonelli.portknocker.R;

import java.util.UUID;

/**
 * Loads JuiceSSH connections from a cursor and provides an adapter
 * that can be used in a ListView or Spinner. You can optionally pass
 * a {@link com.sonelli.juicessh.pluginlibrary.PluginContract.Connections.TYPE}
 * to the constructor to filter down the list to a specific connection type.
 */
public class ConnectionSpinnerAdapter extends CursorAdapter {

    public static final String TAG = "ConnectionAdapter";

    private LayoutInflater inflater;

    /**
     * Loads JuiceSSH connections ready for a ListView/Spinner
     * @param context
     * @param type
     */
    public ConnectionSpinnerAdapter(Context context){
        super(context, null, false);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Returns the UUID connection ID for the item at a given position, or null if not available
     * @param position
     * @return The UUID connection ID
     */
    public UUID getConnectionId(int position) {

        UUID id = null;

        if(getCursor() != null){
            getCursor().moveToPosition(position);
            int idIndex = getCursor().getColumnIndex(PluginContract.Connections.COLUMN_ID);
            if(idIndex > -1){
                id = UUID.fromString(getCursor().getString(idIndex));
            }
        }

        return id;

    }

    public int getIndexOfConnection(String id){

        Cursor cursor = getCursor();
        if(cursor != null){
            while(cursor.moveToNext()){
                int column = cursor.getColumnIndex(PluginContract.Connections.COLUMN_ID);
                if(column > -1){
                    Log.e(TAG, "Checking if " + id + " equals " + cursor.getString(column));
                    if(id.equals(cursor.getString(column))){
                        return cursor.getPosition();
                    }
                }
            }
        }

        return -1;

    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return inflater.inflate(R.layout.spinner_list_item, null, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        int nameColumn = cursor.getColumnIndex(PluginContract.Connections.COLUMN_NAME);

        if(nameColumn > -1){

            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            String name = cursor.getString(nameColumn);
            textView.setText(name);

        }

    }

}
