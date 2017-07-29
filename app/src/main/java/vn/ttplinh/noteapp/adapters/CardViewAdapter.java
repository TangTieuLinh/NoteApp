package vn.ttplinh.noteapp.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import vn.ttplinh.noteapp.R;
import vn.ttplinh.noteapp.beans.NoteModel;

/**
 * Created by linhtang on 7/27/17.
 */

public class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.ViewHolder> {

    private List<NoteModel> notes;
    private CardViewListener listener;

    public interface CardViewListener {
        void onItemClick(View v, int position);

        void onItemLongClick(View v, int position);
    }

    public CardViewAdapter() {

    }

    public CardViewAdapter(List<NoteModel> notes, CardViewListener listener) {
        this.notes = notes;
        this.listener = listener;
    }

    public void setListener(CardViewListener listener) {
        this.listener = listener;
    }

    public void setNotes(List<NoteModel> notes) {
        this.notes = notes;
    }

    @Override
    public CardViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_note, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CardViewAdapter.ViewHolder holder, int position) {
        holder.position = position;
        NoteModel note = notes.get(position);

        holder.mTitleTv.setText(note.getTitle());
        holder.mContentTv.setText(note.getContent());
    }

    @Override
    public int getItemCount() {
        return notes == null ? 0 : notes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView mTitleTv, mContentTv;
        int position;

        public ViewHolder(View v) {
            super(v);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            mTitleTv = (TextView) v.findViewById(R.id.title_tv);
            mContentTv = (TextView) v.findViewById(R.id.content_tv);
        }


        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClick(v, position);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (listener != null) {
                listener.onItemLongClick(v, position);
            }
            return true;
        }
    }
}
