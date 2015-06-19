package com.intrepid.wonseokshin.intrepidcheckin;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManagerCustom {
    public final static String PREFERENCE_NAME = "com.intrepid.wonseokshin.intrepidcheckin";

    public static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean getBoolean(Context context, String value, boolean defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_NAME, Activity.MODE_PRIVATE);
        return settings.getBoolean(value, defaultValue);
    }


    public static void putString(Context context, String key, String value) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getString(Context context, String value, String defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_NAME, Activity.MODE_PRIVATE);
        return settings.getString(value, defaultValue);
    }
}
