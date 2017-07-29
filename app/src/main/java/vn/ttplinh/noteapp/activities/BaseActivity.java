package vn.ttplinh.noteapp.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Date;
import java.util.List;

import vn.ttplinh.noteapp.beans.NoteModel;
import vn.ttplinh.noteapp.databases.ActiveAndroidQueryUtils;
import vn.ttplinh.noteapp.services.eventbus.EventBusMessage;
import vn.ttplinh.noteapp.services.eventbus.EventBusModel;
import vn.ttplinh.noteapp.utils.Constants;
import vn.ttplinh.noteapp.utils.PreferenceUtils;

/**
 * Created by linhtang on 7/27/17.
 */

public class BaseActivity extends Activity {

    private int index;
    private List<NoteModel> listNotes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

    }

    @Override
    protected void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }

    @Subscribe
    public void onEvent(EventBusModel event) {
        if (event.getMessage() == EventBusMessage.NETWORK_CONNECTED) {
            syncDataToFirebase();
        }
    }

    private void syncDataToFirebase() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                listNotes = ActiveAndroidQueryUtils.getAllDateToUpdateFirebase();
                if (listNotes == null || listNotes.size() == 0) {
                    return;
                }
                syncToFirebase();
            }
        }).start();

    }

    private void syncToFirebase() {

        if (index >= listNotes.size()) {
            NoteApp.getInstance().getPreferenceUtils().putLong(PreferenceUtils.LAST_UPDATE_TIME, new Date().getTime());
            return;
        }

        DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        String uid = NoteApp.getInstance().getPreferenceUtils().getStringValue(PreferenceUtils.USER_ID, "");

        final NoteModel note = listNotes.get(index);
        if (note.getStatus() == 1) {
            firebaseDatabase.child(Constants.USER_NODE).child(uid).child(Constants.NOTE_NODE).child(note.getNoteId() + "").setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        index += 1;
                        ActiveAndroidQueryUtils.deleteNote(note);
                    }
                    syncToFirebase();

                }
            });
        } else {
            firebaseDatabase.child(Constants.USER_NODE).child(uid).child(Constants.NOTE_NODE).child(note.getNoteId() + "").setValue(note).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        index += 1;
                    }
                    syncToFirebase();
                }
            });
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // hidden keyboard when out touch
        View view = getCurrentFocus();
        if (view != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) && view instanceof EditText && !view.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            view.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + view.getLeft() - scrcoords[0];
            float y = ev.getRawY() + view.getTop() - scrcoords[1];
            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom())
                ((InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow((this.getWindow().getDecorView().getApplicationWindowToken()), 0);
        }
        return super.dispatchTouchEvent(ev);
    }


}
