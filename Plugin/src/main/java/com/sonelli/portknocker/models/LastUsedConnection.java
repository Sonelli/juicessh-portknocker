package com.sonelli.portknocker.models;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public class LastUsedConnection {

    private static final String PREFERENCE_TYPE = "connections";
    private static final String LAST_USED_PREFERENCE = "last-used";

    public static UUID get(Context context){
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCE_TYPE, Context.MODE_PRIVATE);
        try {
            return UUID.fromString(prefs.getString(LAST_USED_PREFERENCE, null));
        } catch (NullPointerException e){
            return null;
        } catch (IllegalArgumentException e){
            return null;
        }
    }

    public static void set(Context context, UUID id){
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCE_TYPE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(LAST_USED_PREFERENCE, id.toString());
        editor.commit();
    }

}
