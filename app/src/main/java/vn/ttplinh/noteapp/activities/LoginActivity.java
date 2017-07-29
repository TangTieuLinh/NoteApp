package vn.ttplinh.noteapp.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import vn.ttplinh.noteapp.R;
import vn.ttplinh.noteapp.beans.NoteModel;
import vn.ttplinh.noteapp.databases.ActiveAndroidQueryUtils;
import vn.ttplinh.noteapp.utils.Constants;
import vn.ttplinh.noteapp.utils.PreferenceUtils;
import vn.ttplinh.noteapp.utils.Utils;

/**
 * Created by linhtang on 7/28/17.
 */

public class LoginActivity extends BaseActivity {

    private EditText mEmailEdt;
    private EditText mPasswordEdt;
    private ProgressBar mProgressBar;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!TextUtils.isEmpty(NoteApp.getInstance().getPreferenceUtils().getStringValue(PreferenceUtils.USER_ID, ""))) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProgressBar.setVisibility(View.GONE);
    }

    private void init() {
        mEmailEdt = (EditText) findViewById(R.id.email_edt);
        mEmailEdt.setOnKeyListener(keyListener);
        mPasswordEdt = (EditText) findViewById(R.id.password_edt);
        mPasswordEdt.setOnKeyListener(keyListener);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        findViewById(R.id.login_btn).setOnClickListener(onClick);
        findViewById(R.id.sign_up_btn).setOnClickListener(onClick);

    }

    private View.OnKeyListener keyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                handleLoginClick();

                return true;
            }
            return false;
        }
    };

    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mProgressBar.getVisibility() == View.VISIBLE) {
                return;
            }

            if (v.getId() == R.id.login_btn) {
                handleLoginClick();

            } else if (v.getId() == R.id.sign_up_btn) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }

        }
    };

    private void handleLoginClick() {

        String email = mEmailEdt.getText().toString().trim();
        String password = mPasswordEdt.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_email), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_password), Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(getApplicationContext(), getString(R.string.invalid_password), Toast.LENGTH_SHORT).show();
            return;
        }


        if (Utils.isInternetConnected(this)) {
            mProgressBar.setVisibility(View.VISIBLE);

            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {
                        String uid = task.getResult().getUser().getUid();
                        Log.d("LoginActivity", "Uid: " + uid);
                        NoteApp.getInstance().getPreferenceUtils().putString(PreferenceUtils.USER_ID, uid);
                        loadDataFormFirebase(uid);

                    } else {
                        mProgressBar.setVisibility(View.GONE);
                        String message = task.getException().getMessage();
                        if(TextUtils.isEmpty(message)) {
                            message = getString(R.string.authentication_failed);
                        }
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } else {
            Toast.makeText(this, getString(R.string.no_network_connection), Toast.LENGTH_SHORT).show();
        }

    }


    private void loadDataFormFirebase(String uid) {
        DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseDatabase.child(Constants.USER_NODE).child(uid).child(Constants.NOTE_NODE).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.hasChildren()) {
                    NoteModel note;
                    List<NoteModel> list = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        note = snapshot.getValue(NoteModel.class);
                        if (note != null) {
                            list.add(note);
                        }
                    }
                    if (list != null && list.size() > 0) {

                        new UpdateDatabaseTask().execute(list);
                    } else {
                        startMainActivity();
                    }

                } else {
                    startMainActivity();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                startMainActivity();
            }
        });


    }

    private void startMainActivity() {
        NoteApp.getInstance().getPreferenceUtils().putLong(PreferenceUtils.LAST_UPDATE_TIME, new Date().getTime());
        mProgressBar.setVisibility(View.GONE);
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    class UpdateDatabaseTask extends AsyncTask<List<NoteModel>, Void, Void> {

        @Override
        protected Void doInBackground(List<NoteModel>[] lists) {

            ActiveAndroid.beginTransaction();
            try {
                for (NoteModel note : lists[0]) {
                    note.setLastUpdateDate(new Date().getTime());
                    ActiveAndroidQueryUtils.createOrUpdateNote(note);
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            startMainActivity();

        }
    }


}
