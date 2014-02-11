package com.sonelli.portknocker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.Spinner;

import com.sonelli.portknocker.R;
import com.sonelli.portknocker.models.KnockItem;
import com.sonelli.portknocker.models.KnockSequence;

import java.lang.ref.WeakReference;

public class KnockSequenceListAdapter extends BaseAdapter implements ListAdapter {

    private WeakReference<Context> context;
    private final KnockSequence sequence = new KnockSequence();
    private LayoutInflater inflater;

    public KnockSequenceListAdapter(Context context) {
        this.context = new WeakReference<Context>(context);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        refresh();
    }

    public void refresh(){

        // TODO: Load KnockSequence from shared prefs
        sequence.clear();
        sequence.add(KnockItem.tcp(1));
        sequence.add(KnockItem.pause(2));
        sequence.add(KnockItem.udp(3));
        sequence.add(KnockItem.pause(4));
        sequence.add(KnockItem.tcp(5));

    }

    @Override
    public int getCount() {
        return sequence.size() + 1;
    }

    @Override
    public KnockItem getItem(int position) {
        return sequence.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = inflater.inflate(R.layout.knock_item, parent, false);
        }

        Spinner type = (Spinner) convertView.findViewById(R.id.type_spinner);
        EditText value = (EditText) convertView.findViewById(R.id.value);
        ImageButton removeButton = (ImageButton) convertView.findViewById(R.id.remove_button);
        type.setAdapter(new KnockItemTypeAdapter(context.get()));

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sequence.remove(position);
                notifyDataSetChanged();
            }
        });

        if(position > sequence.size() -1){
            // This is the empty one, which allows users to add a new value
            removeButton.setEnabled(false);
            type.setSelection(KnockItem.TYPE_PAUSE);
            value.setHint(R.string.duration);
            value.setText(null);
        } else {
            // This is an actual item
            removeButton.setEnabled(true);

            KnockItem item = getItem(position);

            switch(item.getType()){

                case KnockItem.TYPE_PAUSE:

                    type.setSelection(item.getType());
                    value.setHint(R.string.duration);

                    if(item.getMilliseconds() > -1){
                        value.setText(String.valueOf(item.getMilliseconds()));
                    }

                    break;

                case KnockItem.TYPE_TCP_PACKET:
                case KnockItem.TYPE_UDP_PACKET:

                    type.setSelection(item.getType());
                    value.setHint(R.string.port);

                    if(item.getPort() > -1){
                        value.setText(String.valueOf(item.getPort()));
                    }

                    break;

            }


        }

        return convertView;

    }
}
