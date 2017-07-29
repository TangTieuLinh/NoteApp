package vn.ttplinh.noteapp.activities;

import android.app.Application;

import com.activeandroid.ActiveAndroid;

import vn.ttplinh.noteapp.utils.PreferenceUtils;

/**
 * Created by linhtang on 7/27/17.
 */

public class NoteApp extends Application {

    private static NoteApp noteApp;
    private PreferenceUtils preferenceUtils;

    @Override
    public void onCreate() {
        super.onCreate();
        noteApp = this;
        preferenceUtils = new PreferenceUtils(this);
        ActiveAndroid.initialize(this);

    }

    public synchronized static NoteApp getInstance () {
        if(noteApp == null) {
            noteApp = new NoteApp();
        }
        return noteApp;
    }

    public synchronized PreferenceUtils getPreferenceUtils () {
        if(preferenceUtils == null) {
            preferenceUtils = new PreferenceUtils(getInstance());
        }
        return preferenceUtils;
    }
}
