package com.sonelli.portknocker.models;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.UUID;

public class KnockSequence {

    // Maybe we want to store more than just a single sequence in the future
    public static final String PREFS_NAME = "sequences";

    private ArrayList<KnockItem> items = new ArrayList<KnockItem>();
    private String connection;

    public void save(Context context){
        if(context == null)
            return;

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(connection, new Gson().toJson(this));
        editor.commit();

    }

    public static KnockSequence load(Context context, UUID id){
        if(context == null || id == null)
            return new KnockSequence();

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return new Gson().fromJson(prefs.getString(id.toString(), "{}"), KnockSequence.class);
    }

    public UUID getConnection() {
        try {
            return UUID.fromString(connection);
        } catch (IllegalArgumentException e){
            return null;
        }
    }

    public void setConnection(UUID connection) {
        this.connection = connection.toString();
    }

    public String getConnectionString(){
        return this.connection;
    }

    public void add(KnockItem item){
        items.add(item);
    }

    public void add(int index, KnockItem item){
        items.add(index, item);
    }

    public void clear(){
        items.clear();
    }

    public KnockItem get(int index){
        return items.get(index);
    }

    public void remove(int index){
        items.remove(index);
    }

    public int size(){
        return items.size();
    }


}
