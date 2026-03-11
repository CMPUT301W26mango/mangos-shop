package com.example.myapplication;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.app.DatePickerDialog;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.Date;
import androidx.appcompat.app.AlertDialog;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.UUID;

public class EventCreateActivity extends AppCompatActivity {
    private EditText eventNameInput;
    private EditText locationInput;
    private EditText startDateInput;
    private EditText endDateInput;
    private EditText eventDescriptionInput;
    private Button uploadPosterButton;
    private Button createEventButton;
    private EditText eventDateInput;
    private ImageView posterPreview;

    private EventStore eventStore;

    // Stores the Firebase Storage download URL after upload
    private String posterDownloadUrl = "";

    // Firebase Storage reference
    private FirebaseStorage storage;
    private StorageReference storageRef;

    // Image picker launcher
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_event);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.create_event_scroll), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        eventNameInput = findViewById(R.id.event_name_input);
        locationInput = findViewById(R.id.location_input);
        startDateInput = findViewById(R.id.start_date_input);
        endDateInput = findViewById(R.id.end_date_input);
        eventDescriptionInput = findViewById(R.id.event_description_input);
        uploadPosterButton = findViewById(R.id.upload_poster_button);
        createEventButton = findViewById(R.id.create_event_button);
        eventDateInput = findViewById(R.id.event_date_input);

        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Register image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            handleImageSelected(imageUri);
                        }
                    }
                }
        );

        // Upload poster button click
        uploadPosterButton.setOnClickListener(v -> openImagePicker());

        // Event date picker
        eventDateInput.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        TimePickerDialog timeDialog = new TimePickerDialog(
                                this,
                                (timeView, hourOfDay, minute) -> {
                                    String result = String.format(Locale.getDefault(),
                                            "%04d-%02d-%02d %02d:%02d",
                                            year, month + 1, dayOfMonth, hourOfDay, minute);

                                    String endText = endDateInput.getText().toString().trim();

                                    if (!endText.isEmpty()) {
                                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                        try {
                                            Date regEnd = formatter.parse(endText);
                                            Date eventDate = formatter.parse(result);
                                            if (!eventDate.after(regEnd)) {
                                                new AlertDialog.Builder(this)
                                                        .setTitle("Invalid Event Time")
                                                        .setMessage("Event date and time must be after the registration end.")
                                                        .setPositiveButton("OK", null)
                                                        .show();
                                                return;
                                            }
                                        } catch (ParseException e) {
                                            new AlertDialog.Builder(this)
                                                    .setTitle("Invalid Date")
                                                    .setMessage("Please select the date again.")
                                                    .setPositiveButton("OK", null)
                                                    .show();
                                            return;
                                        }
                                    }
                                    eventDateInput.setText(result);
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                true
                        );
                        timeDialog.show();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });

        // Reg start date picker
        startDateInput.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        TimePickerDialog timeDialog = new TimePickerDialog(
                                this,
                                (timeView, hourOfDay, minute) -> {
                                    String result = String.format(Locale.getDefault(),
                                            "%04d-%02d-%02d %02d:%02d",
                                            year, month + 1, dayOfMonth, hourOfDay, minute);
                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                    try {
                                        Date selectedStart = formatter.parse(result);
                                        Date now = new Date();
                                        if (selectedStart.before(now)) {
                                            new AlertDialog.Builder(this)
                                                    .setTitle("Invalid Start Time")
                                                    .setMessage("Registration start must be today or later.")
                                                    .setPositiveButton("OK", null)
                                                    .show();
                                            return;
                                        }
                                    } catch (ParseException e) {
                                        new AlertDialog.Builder(this)
                                                .setTitle("Invalid Date")
                                                .setMessage("Please select the date again.")
                                                .setPositiveButton("OK", null)
                                                .show();
                                        return;
                                    }
                                    startDateInput.setText(result);
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                true
                        );
                        timeDialog.show();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });

        // Reg end date picker
        endDateInput.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        TimePickerDialog timeDialog = new TimePickerDialog(
                                this,
                                (timeView, hourOfDay, minute) -> {
                                    String result = String.format(Locale.getDefault(),
                                            "%04d-%02d-%02d %02d:%02d",
                                            year, month + 1, dayOfMonth, hourOfDay, minute);
                                    String startText = startDateInput.getText().toString().trim();
                                    if (!startText.isEmpty()) {
                                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                        try {
                                            Date start = formatter.parse(startText);
                                            Date end = formatter.parse(result);
                                            if (!end.after(start)) {
                                                new AlertDialog.Builder(this)
                                                        .setTitle("Invalid End Time")
                                                        .setMessage("End date and time must be after the start date and time.")
                                                        .setPositiveButton("OK", null)
                                                        .show();
                                                return;
                                            }
                                        } catch (ParseException e) {
                                            new AlertDialog.Builder(this)
                                                    .setTitle("Invalid Date")
                                                    .setMessage("Please select the date again.")
                                                    .setPositiveButton("OK", null)
                                                    .show();
                                            return;
                                        }
                                    }
                                    endDateInput.setText(result);
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                true
                        );
                        timeDialog.show();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });

        // Create event button
        eventStore = new EventStore();
        createEventButton.setOnClickListener(v -> {
            String eventName = eventNameInput.getText().toString().trim();
            String location = locationInput.getText().toString().trim();
            String startDate = startDateInput.getText().toString().trim();
            String endDate = endDateInput.getText().toString().trim();
            String description = eventDescriptionInput.getText().toString().trim();

            Event event = new Event();
            event.setTitle(eventName);
            event.setLocation(location);
            event.setDescirption(description);
            event.setPosterURL(posterDownloadUrl);

            if (!startDate.isEmpty()) event.setRegStart(startDate);
            if (!endDate.isEmpty()) event.setRegEnd(endDate);

            eventStore.addEvent(event);

            // Clear form
            eventNameInput.setText("");
            locationInput.setText("");
            startDateInput.setText("");
            endDateInput.setText("");
            eventDescriptionInput.setText("");
            posterDownloadUrl = "";
            uploadPosterButton.setText("Upload Poster Image");

            Toast.makeText(this, "Event created!", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Opens Android image picker filtered to JPG and PNG only.
     * US 02.04.01 - Launch image picker for poster upload.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/png"});
        imagePickerLauncher.launch(intent);
    }

    /**
     * Validates and uploads the selected image to Firebase Storage.
     * US 02.04.01 - Validate file size <= 5MB, upload to Storage, save URL to Firestore.
     */
    private void handleImageSelected(Uri imageUri) {
        try {
            // Validate file size - must be <= 5MB
            long fileSize = getContentResolver()
                    .openAssetFileDescriptor(imageUri, "r")
                    .getLength();

            long maxSize = 5 * 1024 * 1024; // 5MB in bytes

            if (fileSize > maxSize) {
                Toast.makeText(this,
                        "Image too large. Please select an image under 5MB.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            // Show uploading feedback
            uploadPosterButton.setText("Uploading...");
            uploadPosterButton.setEnabled(false);

            // Generate unique path in Firebase Storage
            String eventId = UUID.randomUUID().toString();
            StorageReference posterRef = storageRef.child("events/" + eventId + "/poster");

            // Upload to Firebase Storage
            posterRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get download URL and save it
                        posterRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            posterDownloadUrl = uri.toString();
                            uploadPosterButton.setText("Poster Uploaded ✓");
                            uploadPosterButton.setEnabled(true);
                            Toast.makeText(this,
                                    "Poster uploaded successfully!",
                                    Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        uploadPosterButton.setText("Upload Poster Image");
                        uploadPosterButton.setEnabled(true);
                        Toast.makeText(this,
                                "Upload failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });

        } catch (Exception e) {
            Toast.makeText(this,
                    "Could not read file. Please try again.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}