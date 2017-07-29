package vn.ttplinh.noteapp.activities;

import android.content.Intent;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import vn.ttplinh.noteapp.R;
import vn.ttplinh.noteapp.utils.PreferenceUtils;
import vn.ttplinh.noteapp.utils.Utils;

/**
 * Created by linhtang on 7/28/17.
 */

public class RegisterActivity extends BaseActivity {

    private EditText mEmailEdt;
    private EditText mPasswordEdt;
    private ProgressBar mProgressBar;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        init();
    }

    private void init() {
        mEmailEdt = (EditText) findViewById(R.id.email_edt);
        mEmailEdt.setOnKeyListener(keyListener);
        mPasswordEdt = (EditText) findViewById(R.id.password_edt);
        mPasswordEdt.setOnKeyListener(keyListener);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        findViewById(R.id.sign_up_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSignUpClick();
            }
        });

    }

    private View.OnKeyListener keyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                handleSignUpClick();

                return true;
            }
            return false;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mProgressBar.setVisibility(View.GONE);
    }

    private void handleSignUpClick() {

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

            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    mProgressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        String uid = task.getResult().getUser().getUid();
                        Log.d("RegisterActivity", "Uid: " + uid);
                        NoteApp.getInstance().getPreferenceUtils().putString(PreferenceUtils.USER_ID, uid);
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();

                    } else {
                        String message = task.getException().getMessage();
                        if(TextUtils.isEmpty(message)) {
                            message = getString(R.string.authentication_failed);
                        }
                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();

                    }
                }
            });


        } else {
            Toast.makeText(this, getString(R.string.no_network_connection), Toast.LENGTH_SHORT).show();
        }
    }

}
