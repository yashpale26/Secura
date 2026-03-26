package com.example.secura.modulesscreen.privacyprotectors.securenotepad;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secura.R;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private Context context;
    private List<Note> notes;
    private OnNoteInteractionListener listener;

    public interface OnNoteInteractionListener {
        void onNoteClick(int position);
        void onNoteDeleteClick(int position);
    }

    public NoteAdapter(Context context, List<Note> notes, OnNoteInteractionListener listener) {
        this.context = context;
        this.notes = notes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.tvTitle.setText(note.getTitle());
        holder.tvContentPreview.setText(note.getContent());

        // Format timestamp for display
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        holder.tvTimestamp.setText(sdf.format(note.getTimestamp()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNoteClick(holder.getAdapterPosition());
            }
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNoteDeleteClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvContentPreview;
        TextView tvTimestamp;
        ImageButton btnDelete;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNoteTitle);
            tvContentPreview = itemView.findViewById(R.id.tvNoteContentPreview);
            tvTimestamp = itemView.findViewById(R.id.tvNoteTimestamp);
            btnDelete = itemView.findViewById(R.id.btnDeleteNote);
        }
    }
}