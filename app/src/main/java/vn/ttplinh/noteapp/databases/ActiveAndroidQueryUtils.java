package vn.ttplinh.noteapp.databases;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.util.List;

import vn.ttplinh.noteapp.activities.NoteApp;
import vn.ttplinh.noteapp.beans.NoteModel;
import vn.ttplinh.noteapp.utils.Constants;
import vn.ttplinh.noteapp.utils.PreferenceUtils;

/**
 * Created by linhtang on 7/28/17.
 */

public class ActiveAndroidQueryUtils {

    public static List<NoteModel> getNotes(int limit, int offset) {
        return new Select().from(NoteModel.class).where(NoteColumnName.COLUMN_STATUS + " == 0").limit(limit).orderBy(NoteColumnName.COLUMN_NOTE_ID + " DESC").offset(offset).execute();
    }

    public static void createOrUpdateNote(NoteModel note) {
        note.save();
    }

    public static void deleteNote(NoteModel note) {
        note.delete();
    }

    public static long getMaxNoteId() {
        NoteModel note = new Select().from(NoteModel.class).limit(1).orderBy(NoteColumnName.COLUMN_NOTE_ID + " DESC").executeSingle();
        if (note == null) {
            return 0;
        }
        return note.getNoteId();

    }

    public static void deleteNoteTable () {
        // Delete all rows from table
        ActiveAndroid.execSQL("DELETE FROM " + NoteColumnName.NOTE_TABLE);
        // Reset id
        ActiveAndroid.execSQL("DELETE FROM sqlite_sequence WHERE name='" + NoteColumnName.NOTE_TABLE + "';");
    }

    public static List<NoteModel> getAllDateToUpdateFirebase () {
        long time = NoteApp.getInstance().getPreferenceUtils().getLong(PreferenceUtils.LAST_UPDATE_TIME, 0);
        if(time == 0) {
            return null;
        }

        return new Select().from(NoteModel.class).where(NoteColumnName.COLUMN_LAST_UPDATE_DATE + " > " + time).execute();

    }

    public static void deleteNoteAfterUpdateToFirebase() {
        new Delete().from(NoteModel.class).where(NoteColumnName.COLUMN_STATUS + " == 1").execute();
    }
}
