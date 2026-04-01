package com.example.myapplication;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;

import java.util.Calendar;
import java.util.Date;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import android.content.Intent;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Activity that provides the event creation form for organizers.
 *
 * This screen allows an organizer to fill in all event details including
 * name, location, registration dates, event date, description, capacity,
 * poster URL, event type, organizer name, and whether geolocation is required.
 * On clicking Create, the event is saved to Firestore via EventStore.
 *
 * Date pickers and time pickers are used for all date fields, with validation
 * to ensure reg end is after reg start, and event date is after reg end.
 * @author Sayuj
 */

public class EventCreateActivity extends AppCompatActivity {

    StorageReference storageReference;
    Uri image;
    private EditText eventNameInput;
    private EditText locationInput;
    private EditText startDateInput;
    private EditText endDateInput;
    private EditText eventDescriptionInput;

    //private EditText posterURLInput;

    private Button uploadPosterButton;
    private Button createEventButton;

    private String posterDownloadUrl = null;
    private ImageView posterPreview;

    private EditText eventDateInput;

    private EventStore eventStore;

    private EditText capacityInput;

    private EditText waitingListInput;


    private EditText eventTypeDropdown;

    private Switch geoSwitch;

    private Switch privSwitch;
    private ImageButton profileButton;


    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    image = result.getData().getData();
                    createEventButton.setVisibility(View.GONE);
                    uploadPosterButton.setText("Uploading...");
                    uploadPosterButton.setEnabled(false);

                    // Show preview immediately
                    posterPreview.setVisibility(View.VISIBLE);
                    Glide.with(this).load(image).into(posterPreview);

                    // Upload to Firebase Storage
                    StorageReference ref = storageReference.child("events/" + UUID.randomUUID() + "/poster");
                    ref.putFile(image)
                            .addOnSuccessListener(taskSnapshot ->
                                    ref.getDownloadUrl().addOnSuccessListener(uri -> {
                                        posterDownloadUrl = uri.toString();
                                        uploadPosterButton.setText("Poster Uploaded ✓");
                                        createEventButton.setVisibility((View.VISIBLE));
                                        uploadPosterButton.setEnabled(true);
                                        Toast.makeText(this, "Poster uploaded!", Toast.LENGTH_SHORT).show();
                                    })
                            )
                            .addOnFailureListener(e -> {
                                uploadPosterButton.setText("Upload Poster Image");
                                uploadPosterButton.setEnabled(true);
                                posterPreview.setVisibility(View.GONE);
                                Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
                }
            });


    private void fetchEventDataForEditing(String eventId) {
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();

        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event event = documentSnapshot.toObject(Event.class);
                    if (event != null) {
                        // pre fill
                        eventNameInput.setText(event.getTitle());
                        locationInput.setText(event.getLocation());
                        eventDescriptionInput.setText(event.getDescription());
                        capacityInput.setText(String.valueOf(event.getCapacity()));
                        waitingListInput.setText(String.valueOf(event.getMaxWaitingListSize()));
                        eventDateInput.setText(event.getDateEvent());

                        // geo logic
                        geoSwitch.setChecked(event.getGeolocationRequired());

                        // priv logic
                        privSwitch.setChecked(event.getPrivateEvent());

                        // time logic
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        if (event.getRegStart() != null) {
                            startDateInput.setText(formatter.format(event.getRegStart().toDate()));
                        }
                        if (event.getRegEnd() != null) {
                            endDateInput.setText(formatter.format(event.getRegEnd().toDate()));
                        }

                        // Poster Preview
                        if (event.getPosterURL() != null && !event.getPosterURL().isEmpty()) {
                            posterDownloadUrl = event.getPosterURL(); // Store the URL for saving later
                            posterPreview.setVisibility(View.VISIBLE);
                            Glide.with(this)
                                    .load(posterDownloadUrl)
                                    .into(posterPreview);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading event data", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_event);

        eventNameInput = findViewById(R.id.event_name_input);
        locationInput = findViewById(R.id.location_input);
        startDateInput = findViewById(R.id.start_date_input);
        endDateInput = findViewById(R.id.end_date_input);
        eventDescriptionInput = findViewById(R.id.event_description_input);
        uploadPosterButton = findViewById(R.id.upload_poster_button);
        posterPreview = findViewById(R.id.poster_image_preview);
        createEventButton = findViewById(R.id.create_event_button);
        eventDateInput = findViewById(R.id.event_date_input);
        capacityInput = findViewById(R.id.capacity_input);
        waitingListInput = findViewById(R.id.max_waitingList_size);
        eventTypeDropdown = findViewById(R.id.event_type_dropdown);
        geoSwitch = findViewById(R.id.switchGeolocation);
        privSwitch = findViewById(R.id.switch_private_event);


        String mode = getIntent().getStringExtra("MODE");
        String eventId = getIntent().getStringExtra("EVENT_ID");

        if ("EDIT".equals(mode) && eventId != null) {
            // change ui text
            TextView titleHeader = findViewById(R.id.create_event_title);
            titleHeader.setText("Edit Your Event");

            createEventButton.setText("Update");

            // pull data
            fetchEventDataForEditing(eventId);
        }

        // using time picker and date picker (event date logic)
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


        // using time picker and date picker (reg start logic)
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


        // using time picker and date picker, also validation that end > start (reg end logic)
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

        eventTypeDropdown = findViewById(R.id.event_type_dropdown);
        String[] eventTypes = getResources().getStringArray(R.array.event_types);

        eventTypeDropdown.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Select Event Type")
                    .setItems(eventTypes, (dialog, which) -> {
                        eventTypeDropdown.setText(eventTypes[which]);
                    })
                    .show();
        });


        FirebaseApp.initializeApp(EventCreateActivity.this);
        storageReference = FirebaseStorage.getInstance().getReference();

        uploadPosterButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            activityResultLauncher.launch(intent);
        });

        eventStore = new EventStore();
        createEventButton.setOnClickListener(v -> {
            String eventName = eventNameInput.getText().toString().trim();
            String location = locationInput.getText().toString().trim();
            String startDate = startDateInput.getText().toString().trim();
            String endDate = endDateInput.getText().toString().trim();
            String description = eventDescriptionInput.getText().toString().trim();
            String eventDate = eventDateInput.getText().toString().trim();
            String capacityText = capacityInput.getText().toString().trim();
            String waitingListText = waitingListInput.getText().toString().trim();
            String eventTypeInput = eventTypeDropdown.getText().toString().trim();

            if (eventName.isEmpty()) {
                eventNameInput.setError("Event name is required");
                eventNameInput.requestFocus();
                return;
            }
            if (location.isEmpty()) {
                locationInput.setError("Location is required");
                locationInput.requestFocus();
                return;
            }
            if (startDate.isEmpty()) {
                startDateInput.setError("Registration start date is required");
                startDateInput.requestFocus();
                return;
            }
            if (endDate.isEmpty()) {
                endDateInput.setError("Registration end date is required");
                endDateInput.requestFocus();
                return;
            }
            if (eventDate.isEmpty()) {
                eventDateInput.setError("Event date is required");
                eventDateInput.requestFocus();
                return;
            }
            if (description.isEmpty()) {
                eventDescriptionInput.setError("Description is required");
                eventDescriptionInput.requestFocus();
                return;
            }
            if (capacityText.isEmpty()) {
                capacityInput.setError("Capacity is required");
                capacityInput.requestFocus();
                return;
            }
            if (eventTypeInput.isEmpty()) {
                eventTypeDropdown.setError("Event type is required");
                eventTypeDropdown.requestFocus();
                return;
            }

            Profiles profilesHelper = new Profiles();
            String myId = profilesHelper.getDeviceId(this);

            profilesHelper.getProfileName(myId, userName -> {
                Event event = new Event();
                event.setTitle(eventName);
                event.setOrganizerName(userName);
                event.setLocation(location);
                event.setDescription(description);
                event.setDeviceId(myId);
                if (posterDownloadUrl != null && !posterDownloadUrl.isEmpty()) {
                    event.setPosterURL(posterDownloadUrl);
                }

                if (!eventDate.isEmpty()) {
                    event.setDateEvent(eventDate);
                }

                if (!eventTypeInput.isEmpty()) {
                    event.setEventType(eventTypeInput);
                }

                if (!startDate.isEmpty()) {
                    try {
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        Date date = formatter.parse(startDate);
                        event.setRegStart(new Timestamp(date));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                if (!endDate.isEmpty()) {
                    try {
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        Date date = formatter.parse(endDate);
                        event.setRegEnd(new Timestamp(date));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                if (!capacityText.isEmpty()) {
                    event.setCapacity(Integer.parseInt(capacityText));
                }


                if (!waitingListText.isEmpty()) {
                    event.setMaxWaitingListSize(Integer.parseInt(waitingListText));
                }

                boolean geoRequired = geoSwitch.isChecked();
                event.setGeolocationRequired(geoRequired);


                event.setPrivateEvent(privSwitch.isChecked());



                if (eventId != null) event.setId(eventId);

                if ("EDIT".equals(mode) && eventId != null) {
                    createEventButton.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    // overwrite existing data
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("events")
                            .document(eventId)
                            .set(event)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Event updated successfully!", Toast.LENGTH_SHORT).show();
                                posterPreview.setVisibility(View.GONE);
                                uploadPosterButton.setText("Upload Poster Image");
                                posterDownloadUrl = null;
                                image = null;
                                finish(); // Go back to the Detail page
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Update failed.", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    // no edit mode
                    eventStore.addEvent(event);
                    finish();
                }
            });
        });
    }
}