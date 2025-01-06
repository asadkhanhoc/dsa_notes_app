package com.asadullah.brainwave;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.asadullah.brainwave.model.*;
import com.asadullah.brainwave.recyclerview.NotesAdapter;
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private NotesLinkedList notesList;
    private EditText titleInput, contentInput, searchInput;
    private TextView textWebUrl;
    private LinearLayout layoutWebUrl;
    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private Dialog dialogAddUrl;
    private String selectedUrl = "";

    private long selectedDateTime = 0;

    private static final int PICK_IMAGE_REQUEST = 1;
    private String selectedImagePath = "";
    private TextView noNotesTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initializeViews();
        setupRecyclerView();
        setupUrlDialog();
        setupSearchListener();

        createNotificationChannel();
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();

        new DatePickerDialog(this, (view, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);

            new TimePickerDialog(this, (timeView, hour, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                selectedDateTime = calendar.getTimeInMillis();

                Toast.makeText(this, "Reminder set for: " +
                                new SimpleDateFormat("dd/MM/yyyy HH:mm").format(calendar.getTime()),
                        Toast.LENGTH_SHORT).show();

            }, calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE), true)
                    .show();

        }, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    @SuppressLint("ScheduleExactAlarm")
    private void scheduleNotification(Note note) {
        Intent intent = new Intent(this, ReminderBroadcast.class);
        intent.putExtra("title", note.title);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                (int) System.currentTimeMillis(), intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, note.reminderTime, pendingIntent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "notifyNote", "Note Reminders",
                    NotificationManager.IMPORTANCE_HIGH);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            selectedImagePath = imageUri.toString();

            // Show preview if desired
            ImageView previewImage = findViewById(R.id.previewImage);
            if (previewImage != null) {
                previewImage.setVisibility(View.VISIBLE);
                Glide.with(this).load(imageUri).into(previewImage);
            }
        }
    }

    private void initializeViews() {
        notesList = new NotesLinkedList();

        noNotesTextView = findViewById(R.id.noNoteFound);

        titleInput = findViewById(R.id.titleInput);
        contentInput = findViewById(R.id.contentInput);
        searchInput = findViewById(R.id.searchInput);
        textWebUrl = findViewById(R.id.textWebUrl);
        layoutWebUrl = findViewById(R.id.layoutWebUrl);

        findViewById(R.id.addButton).setOnClickListener(v -> addNote());
        findViewById(R.id.addUrlButton).setOnClickListener(v -> showAddUrlDialog());

        findViewById(R.id.addImageButton).setOnClickListener(v -> openImagePicker());

        findViewById(R.id.setReminderButton).setOnClickListener(v -> showDateTimePicker());
    }


    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotesAdapter(new Note[0]);
        recyclerView.setAdapter(adapter);
    }


    private void setupUrlDialog() {
        dialogAddUrl = new Dialog(this);
        dialogAddUrl.setContentView(R.layout.dialog_add_url);
        dialogAddUrl.getWindow().setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
        );

        EditText inputUrl = dialogAddUrl.findViewById(R.id.inputUrl);
        TextView textAdd = dialogAddUrl.findViewById(R.id.textAdd);


        textAdd.setOnClickListener(v -> {
            String url = inputUrl.getText().toString().trim();

            if (url.isEmpty()) {
                Toast.makeText(MainActivity.this, "Enter URL", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.WEB_URL.matcher(url).matches()) {
                Toast.makeText(MainActivity.this, "Enter Valid URL", Toast.LENGTH_SHORT).show();
            } else {
                if (layoutWebUrl != null) {
                    layoutWebUrl.setVisibility(View.VISIBLE);
                }
                if (textWebUrl != null) {
                    textWebUrl.setText(url);
                }
                selectedUrl = url;
                dialogAddUrl.dismiss();
                inputUrl.setText("");
            }
        });
    }

    private void setupSearchListener() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                searchNotes(s.toString());
            }
        });
    }

    private void showAddUrlDialog() {
        if (dialogAddUrl != null) {
            EditText inputUrl = dialogAddUrl.findViewById(R.id.inputUrl);
            inputUrl.setText("");
            dialogAddUrl.show();
        }
    }

    private void searchNotes(String query) {
        if (query.isEmpty()) {
            // agr search empty he to notes show krdo or no note found ka msg hide krdo
            updateRecyclerView();
            noNotesTextView.setVisibility(View.GONE);
            return;
        }

        Note[] allNotes = notesList.toArray();
        ArrayList<Note> matchingNotes = new ArrayList<>();

        // Search through notes and add matching ones to the list
        for (Note note : allNotes) {
            if (note.title.toLowerCase().contains(query.toLowerCase()) ||
                    note.content.toLowerCase().contains(query.toLowerCase())) {
                matchingNotes.add(note);
            }
        }

        if (matchingNotes.isEmpty()) {
            // note agr na mily
            noNotesTextView.setVisibility(View.VISIBLE);
        } else {
            // note agr mil jaye
            noNotesTextView.setVisibility(View.GONE);
        }

        // Convert ArrayList to array and update the RecyclerView
        Note[] filteredNotes = matchingNotes.toArray(new Note[0]);
        adapter.updateNotes(filteredNotes);
    }

    private void addNote() {
        String title = titleInput.getText().toString().trim();
        String content = contentInput.getText().toString().trim();

        Note note = new Note(title, content, selectedUrl,selectedImagePath, selectedDateTime);

        if (!title.isEmpty() && !content.isEmpty()) {
            notesList.insertNote(title, content, selectedUrl, selectedImagePath, selectedDateTime);

            if (selectedDateTime > 0) {
                scheduleNotification(note);
            }

            updateRecyclerView();
            clearInputs();
        } else {
            Toast.makeText(this, "Title and content are required", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateRecyclerView() {
        adapter.updateNotes(notesList.toArray());
    }

    public void deleteNote(String title) {
        if (notesList.deleteNote(title)) {
            updateRecyclerView();
        }
    }

    private void clearInputs() {
        titleInput.setText("");
        contentInput.setText("");
        if (textWebUrl != null) {
            textWebUrl.setText("");
        }
        if (layoutWebUrl != null) {
            layoutWebUrl.setVisibility(View.GONE);
        }
        selectedUrl = "";

        selectedImagePath = "";

        // Clear image preview if exists
        ImageView previewImage = findViewById(R.id.previewImage);
        if (previewImage != null) {
            previewImage.setVisibility(View.GONE);
            previewImage.setImageDrawable(null);
        }
    }

    private void showNoteDialog(Note note) {
        new AlertDialog.Builder(this)
                .setTitle(note.title)
                .setMessage(note.content)
                .setPositiveButton("OK", null)
                .show();
    }
}