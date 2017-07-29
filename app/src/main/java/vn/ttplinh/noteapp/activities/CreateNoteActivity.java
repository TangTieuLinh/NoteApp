package vn.ttplinh.noteapp.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

import vn.ttplinh.noteapp.R;
import vn.ttplinh.noteapp.beans.NoteModel;
import vn.ttplinh.noteapp.databases.ActiveAndroidQueryUtils;
import vn.ttplinh.noteapp.utils.Constants;
import vn.ttplinh.noteapp.utils.PreferenceUtils;
import vn.ttplinh.noteapp.utils.Utils;

/**
 * Created by linhtang on 7/27/17.
 */

public class CreateNoteActivity extends BaseActivity {
    NoteModel note;
    private EditText mTitleTv;
    private EditText mContentTv;
    private TextView mCreateDateTv;
    private ProgressBar mProgressBar;

    private DatabaseReference firebaseDatabase;
    private boolean isSaved;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        firebaseDatabase = FirebaseDatabase.getInstance().getReference();

        init();
        showData();

    }

    private void init() {
        mTitleTv = (EditText) findViewById(R.id.title_edt);
        mContentTv = (EditText) findViewById(R.id.content_edt);
        mCreateDateTv = (TextView) findViewById(R.id.create_date_tv);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);

        findViewById(R.id.back_img).setOnClickListener(onClick);
        findViewById(R.id.save_tv).setOnClickListener(onClick);


    }

    private void showData() {
        Intent intent = getIntent();
        Long id = intent.getLongExtra(Constants.NOTE_ID, -1);

        if (id != -1) {
            note = NoteModel.load(NoteModel.class, id);
            mCreateDateTv.setVisibility(View.VISIBLE);
            mCreateDateTv.setText(Utils.convertTime(note.getCreateDate()));
            mTitleTv.setText(note.getTitle());
            mContentTv.setText(note.getContent());
            mContentTv.requestFocus();

        } else {
            note = new NoteModel();
        }
    }

    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.back_img:
                    onBackPressed();
                    break;
                case R.id.save_tv:
                    saveData();
                    break;
            }
        }
    };

    @Override
    public void onBackPressed() {
        if (!isSaved && (!TextUtils.isEmpty(mTitleTv.getText()) || !TextUtils.isEmpty(mContentTv.getText()))) {
            confirmBackPress();
        } else {
            super.onBackPressed();
        }
    }

    private void confirmBackPress() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.title_dialog_back_press)).
                setMessage(getString(R.string.content_dialog_back_press))
                .setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        saveData();
                    }
                }).
                setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        isSaved = true;
                        onBackPressed();
                    }
                }).show();


    }

    private void saveData() {
        note.setTitle(mTitleTv.getText().toString());
        note.setContent(mContentTv.getText().toString());

        if (TextUtils.isEmpty(note.getTitle()) && TextUtils.isEmpty(note.getContent())) {
            Toast.makeText(this, getString(R.string.title_and_content_is_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);
        if (note.getId() == null) {
            note.setCreateDate(new Date().getTime());
            long maxId = ActiveAndroidQueryUtils.getMaxNoteId() + 1;
            note.setNoteId(maxId);
        }

        note.setLastUpdateDate(new Date().getTime());
        ActiveAndroidQueryUtils.createOrUpdateNote(note);
        isSaved = true;

        if (Utils.isInternetConnected(this)) {
            saveDateToFirebase();
        } else {
            onBackPressed();
        }

    }

    private void saveDateToFirebase() {
        String uid = NoteApp.getInstance().getPreferenceUtils().getStringValue(PreferenceUtils.USER_ID, "");
        firebaseDatabase.child(Constants.USER_NODE).child(uid).child(Constants.NOTE_NODE).child(note.getNoteId() + "").setValue(note).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    NoteApp.getInstance().getPreferenceUtils().putLong(PreferenceUtils.LAST_UPDATE_TIME, new Date().getTime());
                    onBackPressed();
                }
            }
        });
    }


}
