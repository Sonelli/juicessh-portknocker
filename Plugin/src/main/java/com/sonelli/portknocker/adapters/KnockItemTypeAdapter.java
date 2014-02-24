package com.sonelli.portknocker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.sonelli.portknocker.R;
import com.sonelli.portknocker.models.KnockItem;

import java.util.ArrayList;

public class KnockItemTypeAdapter extends BaseAdapter implements SpinnerAdapter {

    private final ArrayList<String> types = new ArrayList<String>();
    private LayoutInflater inflater;

    public KnockItemTypeAdapter(Context context) {

        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        types.add(KnockItem.TYPE_TCP_PACKET, context.getString(R.string.tcp));
        types.add(KnockItem.TYPE_UDP_PACKET, context.getString(R.string.udp));
        types.add(KnockItem.TYPE_PAUSE, context.getString(R.string.pause));

    }

    @Override
    public int getCount() {
        return types.size();
    }

    @Override
    public String getItem(int position) {
        return types.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.knock_item_type, parent, false);
        }

        if (convertView != null && convertView instanceof TextView) {
            ((TextView) convertView).setText(getItem(position));
        }

        return convertView;

    }
}
