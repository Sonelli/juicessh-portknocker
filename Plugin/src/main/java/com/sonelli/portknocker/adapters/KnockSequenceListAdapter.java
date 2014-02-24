package com.sonelli.portknocker.adapters;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.doomonafireball.betterpickers.numberpicker.NumberPickerBuilder;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.sonelli.portknocker.R;
import com.sonelli.portknocker.models.KnockItem;
import com.sonelli.portknocker.models.KnockSequence;

import java.lang.ref.WeakReference;

public class KnockSequenceListAdapter extends BaseAdapter implements ListAdapter {

    private WeakReference<FragmentActivity> activity;
    private KnockSequence sequence;
    private LayoutInflater inflater;

    public KnockSequenceListAdapter(FragmentActivity activity, KnockSequence sequence) {
        this.activity = new WeakReference<FragmentActivity>(activity);
        this.sequence = sequence;
        this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void save() {
        sequence.save(activity.get());
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return sequence.size() + 1;
    }

    @Override
    public KnockItem getItem(int position) {
        if (sequence.size() < (position + 1)) {
            KnockItem item = new KnockItem();
            sequence.add(position, item);
            return item;
        } else {
            return sequence.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final FragmentActivity context = activity.get();
        if (context == null)
            return null;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.knock_item, parent, false);
        }

        final Spinner type = (Spinner) convertView.findViewById(R.id.type_spinner);
        final TextView value = (TextView) convertView.findViewById(R.id.value);
        final ImageButton editButton = (ImageButton) convertView.findViewById(R.id.edit_button);
        final ImageButton removeButton = (ImageButton) convertView.findViewById(R.id.remove_button);

        type.setAdapter(new KnockItemTypeAdapter(context));

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String dialogLabel = "";
                switch ((int) type.getSelectedItemId()) {

                    case KnockItem.TYPE_PAUSE:
                        dialogLabel = context.getString(R.string.ms).toLowerCase();
                        break;

                    case KnockItem.TYPE_TCP_PACKET:
                    case KnockItem.TYPE_UDP_PACKET:
                        dialogLabel = context.getString(R.string.port).toLowerCase();
                        break;

                }

                new NumberPickerBuilder()
                        .setFragmentManager(context.getSupportFragmentManager())
                        .setPlusMinusVisibility(View.INVISIBLE)
                        .setDecimalVisibility(View.INVISIBLE)
                        .setReference(position)
                        .setMaxNumber(65535)
                        .addNumberPickerDialogHandler(new NumberPickerDialogFragment.NumberPickerDialogHandler() {
                            @Override
                            public void onDialogNumberSet(int reference, int number, double decimal, boolean negative, double fullNumber) {
                                KnockItem item = getItem(reference);
                                item.setValue(number);
                                item.setType((int) type.getSelectedItemId());
                                save();
                            }
                        })
                        .setLabelText(dialogLabel)
                        .setStyleResId(R.style.BetterPickersDialogFragment_Light)
                        .show();
            }
        });

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sequence.remove(position);
                save();
            }
        });

        if (position > sequence.size() - 1) {

            // This is the empty one, which allows users to add a new value
            removeButton.setEnabled(false);
            type.setSelection(KnockItem.TYPE_TCP_PACKET);
            value.setText(null);

        } else {

            // This is an actual item
            removeButton.setEnabled(true);
            KnockItem item = getItem(position);
            type.setSelection(item.getType());
            if (item.getValue() > -1) {
                value.setText(String.valueOf(item.getValue()));
            }

        }

        return convertView;

    }

    public void updateSequence(KnockSequence sequence) {
        this.sequence = sequence;
        notifyDataSetChanged();
    }
}
