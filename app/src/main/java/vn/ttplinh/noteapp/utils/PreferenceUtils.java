package vn.ttplinh.noteapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by linhtang on 7/28/17.
 */

public class PreferenceUtils {

    public static final String USER_ID = "user_id";
    public static final String LAST_UPDATE_TIME = "last_update_time";

    private SharedPreferences sharedPreferences;

    public PreferenceUtils(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void putString(String key, String value) {
        sharedPreferences.edit().putString(key, value).commit();
    }

    public String getStringValue(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public void putLong(String key, long value) {
        sharedPreferences.edit().putLong(key, value).commit();
    }

    public long getLong(String key, long defaultLong) {
        return sharedPreferences.getLong(key, defaultLong);
    }

    public void clear () {
        putString(USER_ID, "");
        putLong(LAST_UPDATE_TIME, 0);
    }

}
