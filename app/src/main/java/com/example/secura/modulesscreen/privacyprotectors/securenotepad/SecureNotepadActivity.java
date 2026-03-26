package com.example.secura.modulesscreen.privacyprotectors.securenotepad;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secura.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SecureNotepadActivity extends AppCompatActivity implements NoteAdapter.OnNoteInteractionListener {

    private static final String PREFS_NAME = "SecureNotepadPrefs";
    private static final String KEY_NOTES = "notepad_notes";

    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private List<Note> notes;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secure_notepad);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Secure Notepad");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();

        recyclerView = findViewById(R.id.recyclerViewNotes);
        FloatingActionButton fabAddNote = findViewById(R.id.fabAddNote);

        notes = loadNotes();
        noteAdapter = new NoteAdapter(this, notes, this); // Pass 'this' as listener
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(noteAdapter);

        fabAddNote.setOnClickListener(v -> showAddNoteDialog());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    /**
     * Loads notes from SharedPreferences.
     * @return A list of Note objects.
     */
    private List<Note> loadNotes() {
        String json = sharedPreferences.getString(KEY_NOTES, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<Note>>() {}.getType();
        return gson.fromJson(json, type);
    }

    /**
     * Saves the current list of notes to SharedPreferences.
     */
    private void saveNotes() {
        String json = gson.toJson(notes);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_NOTES, json);
        editor.apply();
    }

    /**
     * Displays a custom dialog to add a new note.
     */
    private void showAddNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_AlertDialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_note, null);
        builder.setView(dialogView);

        final EditText etNoteTitle = dialogView.findViewById(R.id.etNoteTitle);
        final EditText etNoteContent = dialogView.findViewById(R.id.etNoteContent);
        Button btnSaveNote = dialogView.findViewById(R.id.btnSaveNote);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();

        btnSaveNote.setOnClickListener(v -> {
            String title = etNoteTitle.getText().toString().trim();
            String content = etNoteContent.getText().toString().trim();

            if (title.isEmpty() || content.isEmpty()) {
                showToast("Title and content cannot be empty.");
            } else {
                Note newNote = new Note(System.currentTimeMillis(), title, content);
                notes.add(0, newNote); // Add to the beginning of the list
                saveNotes();
                noteAdapter.notifyItemInserted(0); // Notify adapter that an item was inserted at position 0
                recyclerView.scrollToPosition(0); // Scroll to the new note
                dialog.dismiss();
                showToast("Note added!");
            }
        });
        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    /**
     * Displays a custom dialog to edit an existing note.
     * @param note The note to be edited.
     */
    private void showEditNoteDialog(final Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_AlertDialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_note, null); // Reusing add_note layout for simplicity
        builder.setView(dialogView);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        dialogTitle.setText("Edit Note");

        final EditText etNoteTitle = dialogView.findViewById(R.id.etNoteTitle);
        final EditText etNoteContent = dialogView.findViewById(R.id.etNoteContent);
        Button btnSaveNote = dialogView.findViewById(R.id.btnSaveNote);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        etNoteTitle.setText(note.getTitle());
        etNoteContent.setText(note.getContent());

        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();

        btnSaveNote.setOnClickListener(v -> {
            String newTitle = etNoteTitle.getText().toString().trim();
            String newContent = etNoteContent.getText().toString().trim();

            if (newTitle.isEmpty() || newContent.isEmpty()) {
                showToast("Title and content cannot be empty.");
            } else {
                note.setTitle(newTitle);
                note.setContent(newContent);
                saveNotes();
                noteAdapter.notifyDataSetChanged(); // Notify adapter that data has changed
                dialog.dismiss();
                showToast("Note updated!");
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    /**
     * Interface callback for when a note is clicked.
     * @param position The position of the clicked note.
     */
    @Override
    public void onNoteClick(int position) {
        // When a note is clicked, allow editing it
        showEditNoteDialog(notes.get(position));
    }

    /**
     * Interface callback for when a note's delete button is clicked.
     * @param position The position of the note to be deleted.
     */
    @Override
    public void onNoteDeleteClick(int position) {
        if (position >= 0 && position < notes.size()) {
            notes.remove(position);
            saveNotes();
            noteAdapter.notifyItemRemoved(position);
            showToast("Note deleted!");
        }
    }

    /**
     * Displays a short Toast message.
     * @param message The message to display.
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}