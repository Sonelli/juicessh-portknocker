package com.sonelli.portknocker.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.sonelli.portknocker.R;
import com.sonelli.portknocker.adapters.ConnectionSpinnerAdapter;
import com.sonelli.portknocker.adapters.KnockSequenceListAdapter;
import com.sonelli.portknocker.loaders.ConnectionListLoader;
import com.sonelli.portknocker.models.KnockSequence;

public class KnockSequenceListFragment extends Fragment {

    private KnockSequence sequence;

    private ListView knockItemList;
    private KnockSequenceListAdapter knockItemListAdapter;

    private Spinner connectionList;
    private ConnectionListLoader connectionListLoader;
    private ConnectionSpinnerAdapter connectionListAdapter;

    public KnockSequenceListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_main, container, false);

        this.sequence = KnockSequence.load(getActivity());

        this.connectionList = (Spinner) layout.findViewById(R.id.connection_spinner);
        this.connectionListAdapter = new ConnectionSpinnerAdapter(getActivity());
        connectionList.setAdapter(connectionListAdapter);

        this.knockItemList = (ListView) layout.findViewById(R.id.knock_item_list);
        this.knockItemListAdapter = new KnockSequenceListAdapter(getActivity(), sequence);
        knockItemList.setAdapter(knockItemListAdapter);

        connectionList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sequence.setConnection(connectionListAdapter.getConnectionId(position));
                sequence.save(getActivity());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                sequence.setConnection(null);
                sequence.save(getActivity());
            }
        });

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
                    int position = connectionListAdapter.getIndexOfConnection(sequence.getConnectionString());
                    if(position > -1){
                        connectionList.setSelection(position);
                    }
                }
            });

            getActivity().getSupportLoaderManager().initLoader(0, null, connectionListLoader);
        } else {
            getActivity().getSupportLoaderManager().restartLoader(0, null, connectionListLoader);
        }

    }
}
