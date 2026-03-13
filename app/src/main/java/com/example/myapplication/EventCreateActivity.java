package com.example.myapplication;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;

import java.util.Calendar;
import java.util.Date;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;


public class EventCreateActivity extends AppCompatActivity {
    private EditText eventNameInput;
    private EditText locationInput;
    private EditText startDateInput;
    private EditText endDateInput;
    private EditText eventDescriptionInput;

    private EditText posterURLInput;
    private Button uploadPosterButton;
    private Button createEventButton;

    private EditText eventDateInput;

    private EventStore eventStore;

    private EditText capacityInput;


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
        posterURLInput = findViewById(R.id.posterurl_input);
//        uploadPosterButton = findViewById(R.id.upload_poster_button);
        createEventButton = findViewById(R.id.create_event_button);
        eventDateInput = findViewById(R.id.event_date_input);
        capacityInput = findViewById(R.id.capacity_input);


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




        eventStore = new EventStore();
        createEventButton.setOnClickListener(v -> {
            String eventName = eventNameInput.getText().toString().trim();
            String location = locationInput.getText().toString().trim();
            String startDate = startDateInput.getText().toString().trim();
            String endDate = endDateInput.getText().toString().trim();
            String description = eventDescriptionInput.getText().toString().trim();
            String eventDate = eventDateInput.getText().toString().trim();
            String capacityText = capacityInput.getText().toString().trim();
            String posterImageURL = posterURLInput.getText().toString().trim();

            Event event = new Event();
            event.setTitle(eventName);
            event.setLocation(location);
            event.setDescription(description);

            if (!posterImageURL.isEmpty()) {

                event.setPosterURL(posterImageURL);
            }

            if (!eventDate.isEmpty()) {
                event.setDateEvent(eventDate);
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

            eventStore.addEvent(event);
            eventDateInput.setText("");
            eventNameInput.setText("");
            locationInput.setText("");
            startDateInput.setText("");
            endDateInput.setText("");
            eventDescriptionInput.setText("");
            capacityInput.setText("");
            posterURLInput.setText("");
        });
    }
};
