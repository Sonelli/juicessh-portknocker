package com.sonelli.portknocker.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.sonelli.portknocker.R;
import com.sonelli.portknocker.adapters.ConnectionSpinnerAdapter;
import com.sonelli.portknocker.adapters.KnockSequenceListAdapter;
import com.sonelli.portknocker.loaders.ConnectionListLoader;
import com.sonelli.portknocker.models.KnockSequence;
import com.sonelli.portknocker.models.LastUsedConnection;

import java.util.UUID;

public class KnockSequenceListFragment extends Fragment {

    private KnockSequence sequence;

    private ListView knockItemList;
    private KnockSequenceListAdapter knockItemListAdapter;

    private Spinner connectionList;
    private ConnectionListLoader connectionListLoader;
    private ConnectionSpinnerAdapter connectionListAdapter;

    private Button connectButton;

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

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Use a Loader to load the connection list into the adapter from the JuiceSSH content provider
        // This keeps DB activity async and off the UI thread to prevent the plugin lagging
        if(this.connectionListLoader == null){
            this.connectionListLoader = new ConnectionListLoader(getActivity(), connectionListAdapter);
            connectionListLoader.setOnLoadedListener(new ConnectionListLoader.OnLoadedListener() {
                @Override
                public void onLoaded() {

                    UUID last = LastUsedConnection.get(getActivity());

                    if(last != null){

                        int position = connectionListAdapter.getIndexOfConnection(last.toString());

                        if(position > -1){
                            connectionList.setSelection(position);
                            sequence = KnockSequence.load(getActivity(), connectionListAdapter.getConnectionId(position));
                        } else {
                            sequence = new KnockSequence();
                        }

                    } else {
                        sequence = new KnockSequence();
                    }

                    knockItemListAdapter = new KnockSequenceListAdapter(getActivity(), sequence);
                    knockItemList.setAdapter(knockItemListAdapter);

                    connectionList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            sequence = KnockSequence.load(getActivity(), connectionListAdapter.getConnectionId(position));
                            sequence.setConnection(connectionListAdapter.getConnectionId(position));
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
                                            if(activity == null)
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
