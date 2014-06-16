package com.sonelli.portknocker.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.sonelli.portknocker.R;
import com.sonelli.portknocker.activities.MainActivity;
import com.sonelli.portknocker.adapters.ConnectionSpinnerAdapter;
import com.sonelli.portknocker.adapters.KnockSequenceListAdapter;
import com.sonelli.portknocker.loaders.ConnectionListLoader;
import com.sonelli.portknocker.models.KnockSequence;
import com.sonelli.portknocker.models.LastUsedConnection;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class KnockSequenceListFragment extends Fragment {

    private KnockSequence sequence;

    private ListView knockItemList;
    private KnockSequenceListAdapter knockItemListAdapter;

    private Spinner connectionList;
    private ConnectionListLoader connectionListLoader;
    private ConnectionSpinnerAdapter connectionListAdapter;

    private Button connectButton;
    private Button shortcutButton;

    public KnockSequenceListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_main, container, false);

        this.connectionList = (Spinner) layout.findViewById(R.id.connection_spinner);
        this.connectionListAdapter = new ConnectionSpinnerAdapter(getActivity());
        connectionList.setAdapter(connectionListAdapter);

        this.knockItemList = (ListView) layout.findViewById(R.id.knock_item_list);
        this.connectButton = (Button) layout.findViewById(R.id.connect_button);
        this.shortcutButton = (Button) layout.findViewById(R.id.shortcut_button);

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Use a Loader to load the connection list into the adapter from the JuiceSSH content provider
        // This keeps DB activity async and off the UI thread to prevent the plugin lagging
        if (this.connectionListLoader == null) {

            this.connectionListLoader = new ConnectionListLoader(getActivity(), connectionListAdapter);
            connectionListLoader.setOnLoadedListener(new ConnectionListLoader.OnLoadedListener() {
                @Override
                public void onLoaded() {

                    UUID last = null;
                    final AtomicBoolean startImmedately = new AtomicBoolean(false);

                    if (getArguments() != null && getArguments().getString("id") != null) {
                        // If we've been passed a specific ID to use, then this is a request
                        // from a homescreen shortcut - initate the connection immediately
                        try {
                            last = UUID.fromString(getArguments().getString("id"));
                            startImmedately.set(true);
                        } catch (NullPointerException e) {
                            Toast.makeText(getActivity(), getString(R.string.invalid_connection), Toast.LENGTH_SHORT).show();
                        } catch (IllegalArgumentException e) {
                            Toast.makeText(getActivity(), getString(R.string.invalid_connection), Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        // If we haven't been passed a specific ID, then just load
                        // the last connection the user was looking at
                        last = LastUsedConnection.get(getActivity());
                    }

                    if (last != null) {
                        int position = connectionListAdapter.getIndexOfConnection(last.toString());
                        if(position > -1) {
                            // Load up the last connection the user had open
                            connectionList.setSelection(position);
                            sequence = KnockSequence.load(getActivity(), connectionListAdapter.getConnectionId(position));
                        } else {
                            // Tried to load a non-existant connection
                            sequence = new KnockSequence();
                            connectionList.setSelection(0);
                        }
                    } else {
                        // We don't know the last connection - set the selection to the first item
                        sequence = new KnockSequence();
                        connectionList.setSelection(0);
                    }

                    knockItemListAdapter = new KnockSequenceListAdapter(getActivity(), sequence);
                    knockItemList.setAdapter(knockItemListAdapter);

                    connectionList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                            sequence = KnockSequence.load(getActivity(), connectionListAdapter.getConnectionId(position));
                            sequence.setConnection(connectionListAdapter.getConnectionId(position));
                            sequence.setConnectionName(connectionListAdapter.getConnectionName(position));
                            knockItemListAdapter.updateSequence(sequence);
                            LastUsedConnection.set(getActivity(), connectionListAdapter.getConnectionId(position));
                            connectButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    connectButton.setEnabled(false);
                                    sequence.connect(getActivity(), new KnockSequence.OnConnectListener() {

                                        @Override
                                        public void onMessage(String message) {
                                            connectButton.setText(message);
                                        }

                                        @Override
                                        public void onFailure(String reason) {

                                            Activity activity = getActivity();
                                            if (activity == null)
                                                return;

                                            Toast.makeText(activity, reason, Toast.LENGTH_SHORT).show();
                                            connectButton.setEnabled(true);
                                            connectButton.setText(R.string.connect);

                                        }

                                        @Override
                                        public void onComplete() {
                                            connectButton.setEnabled(true);
                                            connectButton.setText(R.string.connect);
                                        }
                                    });
                                }
                            });

                            // If we've been passed a connection to start immediately, then do so.
                            if(startImmedately.get()){
                                getArguments().putString("id", null);
                                startImmedately.set(false);
                                connectButton.performClick();
                            }

                            shortcutButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    // The launch intent
                                    Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
                                    intent.setAction(Intent.ACTION_MAIN);
                                    intent.putExtra("id", sequence.getConnectionString());

                                    // The homescreen shortcut intent
                                    Intent shortcutIntent = new Intent();
                                    Intent.ShortcutIconResource icon = Intent.ShortcutIconResource.fromContext(getActivity().getApplicationContext(), R.drawable.ic_launcher);
                                    shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
                                    shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, sequence.getConnectionName());
                                    shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
                                    shortcutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                                    getActivity().getApplicationContext().sendBroadcast(shortcutIntent);
                                    Toast.makeText(getActivity(), getString(R.string.homescreen_shortcut_created), Toast.LENGTH_SHORT).show();

                                }
                            });

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }

                    });

                }
            });
            getActivity().getSupportLoaderManager().initLoader(0, null, connectionListLoader);
        } else {
            getActivity().getSupportLoaderManager().restartLoader(0, null, connectionListLoader);
        }

    }
}
