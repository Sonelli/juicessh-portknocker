package com.sonelli.portknocker.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.sonelli.portknocker.R;
import com.sonelli.portknocker.adapters.KnockSequenceListAdapter;

public class KnockSequenceListFragment extends Fragment {

    private ListView knockItemList;
    private KnockSequenceListAdapter knockItemListAdapter;

    public KnockSequenceListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_main, container, false);

        this.knockItemList = (ListView) layout.findViewById(R.id.knock_item_list);
        this.knockItemListAdapter = new KnockSequenceListAdapter(getActivity());
        knockItemList.setAdapter(knockItemListAdapter);

        return layout;
    }
}
