package vn.ttplinh.noteapp.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.activeandroid.ActiveAndroid;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.jcodecraeer.xrecyclerview.progressindicator.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import vn.ttplinh.noteapp.R;
import vn.ttplinh.noteapp.adapters.CardViewAdapter;
import vn.ttplinh.noteapp.beans.NoteModel;
import vn.ttplinh.noteapp.databases.ActiveAndroidQueryUtils;
import vn.ttplinh.noteapp.utils.Constants;
import vn.ttplinh.noteapp.utils.PreferenceUtils;
import vn.ttplinh.noteapp.utils.Utils;

public class MainActivity extends BaseActivity {

    private XRecyclerView mNoteRecyclerView;
    private CardViewAdapter adapter;
    private List<NoteModel> notes;

    private DatabaseReference firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData(false);
    }

    private void init() {
        findViewById(R.id.floating_btn).setOnClickListener(onClick);
        findViewById(R.id.sign_out_img).setOnClickListener(onClick);

        mNoteRecyclerView = (XRecyclerView) findViewById(R.id.note_recycler);
        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mNoteRecyclerView.setLayoutManager(gridLayoutManager);
        mNoteRecyclerView.setLoadingMoreProgressStyle(AVLoadingIndicatorView.LineScaleParty);
        mNoteRecyclerView.setPullRefreshEnabled(false);
        mNoteRecyclerView.setLoadingMoreEnabled(true);
        mNoteRecyclerView.setLoadingListener(loadingListener);
        notes = new ArrayList<>();
        adapter = new CardViewAdapter(notes, listener);
        mNoteRecyclerView.setAdapter(adapter);

    }

    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.sign_out_img) {
                handleLogout();
            } else if (v.getId() == R.id.floating_btn) {
                Intent intent = new Intent(MainActivity.this, CreateNoteActivity.class);
                startActivity(intent);
            }
        }
    };

    private void handleLogout() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.title_dialog_logout)).
                setMessage(getString(R.string.content_dialog_logout))
                .setPositiveButton(getString(R.string.logout), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        logout();
                    }
                }).
                setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();


    }

    private void logout() {
        NoteApp.getInstance().getPreferenceUtils().clear();
        ActiveAndroidQueryUtils.deleteNoteTable();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private XRecyclerView.LoadingListener loadingListener = new XRecyclerView.LoadingListener() {
        @Override
        public void onRefresh() {

        }

        @Override
        public void onLoadMore() {
            loadData(true);
        }
    };

    private CardViewAdapter.CardViewListener listener = new CardViewAdapter.CardViewListener() {
        @Override
        public void onItemClick(View v, int position) {
            Intent intent = new Intent(MainActivity.this, CreateNoteActivity.class);
            intent.putExtra(Constants.NOTE_ID, notes.get(position).getId());
            startActivity(intent);
        }

        @Override
        public void onItemLongClick(View v, int position) {
            deleteNote(position);
        }
    };

    private void deleteNote(final int position) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.title_dialog_delete)).
                setMessage(getString(R.string.content_dialog_delete)).
                setIcon(android.R.drawable.ic_menu_delete)
                .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        handleDeleteNote(position);
                    }
                }).
                setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    private void handleDeleteNote(int position) {
        final NoteModel note = notes.get(position);

        if (Utils.isInternetConnected(this)) {
            String uid = NoteApp.getInstance().getPreferenceUtils().getStringValue(PreferenceUtils.USER_ID, "");
            firebaseDatabase.child(Constants.USER_NODE).child(uid).child(Constants.NOTE_NODE).child(note.getNoteId() + "").setValue(null).addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        NoteApp.getInstance().getPreferenceUtils().putLong(PreferenceUtils.LAST_UPDATE_TIME, new Date().getTime());
                        ActiveAndroidQueryUtils.deleteNote(note);
                    }
                }
            });
        } else {
            note.setStatus(1);
            note.setLastUpdateDate(new Date().getTime());
            ActiveAndroidQueryUtils.createOrUpdateNote(note);

        }

        notes.remove(position);
        adapter.notifyDataSetChanged();
    }

    private void loadData(boolean isLoading) {
        new LoadDataAsyncTask().execute(isLoading);
    }


    class LoadDataAsyncTask extends AsyncTask<Boolean, Void, List<NoteModel>> {

        private boolean isLoading;

        @Override
        protected List<NoteModel> doInBackground(Boolean... booleans) {
            if (booleans != null && booleans.length > 0) {
                isLoading = booleans[0];
            }
            if (isLoading) {
                return ActiveAndroidQueryUtils.getNotes(Constants.LIMIT_LOAD_DATA + notes.size(), notes.size());
            } else {
                int limit = notes.size() > Constants.LIMIT_LOAD_DATA ? notes.size() : Constants.LIMIT_LOAD_DATA;
                return ActiveAndroidQueryUtils.getNotes(limit, 0);
            }

        }

        @Override
        protected void onPostExecute(List<NoteModel> noteModels) {
            if (!isLoading) {
                notes.clear();
            }

            if (noteModels.size() < Constants.LIMIT_LOAD_DATA) {
                mNoteRecyclerView.setLoadingMoreEnabled(false);
            } else {
                mNoteRecyclerView.setLoadingMoreEnabled(true);
            }
            notes.addAll(noteModels);
            adapter.notifyDataSetChanged();
            mNoteRecyclerView.loadMoreComplete();

        }
    }

}
