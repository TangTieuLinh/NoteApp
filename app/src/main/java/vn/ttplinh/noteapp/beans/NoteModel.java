package vn.ttplinh.noteapp.beans;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import vn.ttplinh.noteapp.databases.NoteColumnName;

/**
 * Created by linhtang on 7/27/17.
 */

@Table(name = NoteColumnName.NOTE_TABLE)
public class NoteModel extends Model implements Serializable {

    @Column(name = NoteColumnName.COLUMN_NOTE_ID, unique = true, index = true)
    @SerializedName("id")
    private long noteId;
    @Column(name = NoteColumnName.COLUMN_TITLE)
    @SerializedName("title")
    private String title;
    @Column(name = NoteColumnName.COLUMN_CONTENT)
    @SerializedName("content")
    private String content;
    @Column(name = NoteColumnName.COLUMN_STATUS)
    private int status = 0;
    @Column(name = NoteColumnName.COLUMN_CREATE_DATE)
    @SerializedName("create_date")
    private long createDate;
    @Column(name = NoteColumnName.COLUMN_LAST_UPDATE_DATE)
    @SerializedName("last_update")
    private long lastUpdateDate;


    public NoteModel() {
        super();
    }

    public long getNoteId() {
        return noteId;
    }

    public void setNoteId(long noteId) {
        this.noteId = noteId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public long getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(long lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }
}
