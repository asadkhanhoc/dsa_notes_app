package com.asadullah.brainwave.recyclerview;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.asadullah.brainwave.MainActivity;
import com.asadullah.brainwave.R;
import com.asadullah.brainwave.model.Note;
import com.bumptech.glide.Glide;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
    private Note[] notes;

    public NotesAdapter(Note[] notes) {
        this.notes = notes;
    }

    public void updateNotes(Note[] newNotes) {
        this.notes = newNotes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_item, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes[position];
        holder.titleText.setText(note.title);
        holder.contentText.setText(note.content);

        // image k liye
        if (note.hasImage()) {
            holder.noteImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(Uri.parse(note.imagePath))
                    .into(holder.noteImage);
        } else {
            holder.noteImage.setVisibility(View.GONE);
        }

        // url k liye
        if (note.hasUrl()) {
            holder.urlText.setVisibility(View.VISIBLE);
            holder.urlText.setText(note.getFormattedUrl());
        } else {
            holder.urlText.setVisibility(View.GONE);
        }

        holder.urlText.setOnClickListener(v -> {
            if (!note.hasUrl()) return;

            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(note.getFormattedUrl()));
                v.getContext().startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(v.getContext(), "Invalid URL: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (v.getContext() instanceof MainActivity) {
                ((MainActivity) v.getContext()).deleteNote(note.title);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.length;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, contentText, urlText;
        ImageButton deleteButton;
        ImageView noteImage;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.noteTitle);
            contentText = itemView.findViewById(R.id.noteContent);
            urlText = itemView.findViewById(R.id.textWebUrl);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            noteImage = itemView.findViewById(R.id.noteImage);
        }
    }
}