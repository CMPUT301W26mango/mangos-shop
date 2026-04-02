package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Intent;


/**
 * This is an activity that shows all events to entrants
 * It queries the firebase databse for load events
 * This enables it so that the users can:
 * - View event details by selecting an event
 * - Scan a QR code to open the corresponding event
 * - View lottery guidelines
 * - Navigate to their profile editing screen
 * The activity also filters events based on their registration start
 * and end times so that only currently active events are displayed.
 */
public class EventListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;
    private List<Event> allActiveEvents;
    private List<Event> displayedEvents;
    private FirebaseFirestore db;

    private ImageButton lotteryinfoButton;
    private ImageButton scanQRButton;
    private ImageButton closeInfoButton;
    private ImageButton btnFilter;
    private ImageButton profileButton; // go to edit profile

    private SearchView eventsSearch;

    private com.google.firebase.firestore.ListenerRegistration statusListener;

    private com.google.firebase.firestore.ListenerRegistration eventChangesListener;

    private ActivityResultLauncher<ScanOptions> scannerLauncher;

    // Set the state for the filters
    private List<String> activeCategories = new ArrayList<>();
    private Integer activeMinSpots = null;
    private Integer activeMaxSpots = null;
    private String activeMinDate = null;
    private String activeMaxDate = null;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.whole_event_list);

        scannerLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                String scannedValue = result.getContents();

                Bundle bundle = new Bundle();
                bundle.putString("eventId", scannedValue);

                EventDetailsFragment eventDetailsFragment = new EventDetailsFragment();
                eventDetailsFragment.setArguments(bundle);


                eventDetailsFragment.show(getSupportFragmentManager(), "eventDetails");
            } else {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView = findViewById(R.id.recyclerViewEvents);
        lotteryinfoButton = findViewById(R.id.lotteryinfoButton);
        scanQRButton = findViewById(R.id.scanQRButton);
        btnFilter = findViewById(R.id.btnFilter);
//        profileButton = findViewById(R.id.btn_to_edit_profile);

        eventList = new ArrayList<>();
        allActiveEvents = new ArrayList<>();
        displayedEvents = new ArrayList<>();


        adapter = new EventAdapter(displayedEvents, getSupportFragmentManager());

        db = FirebaseFirestore.getInstance();
        loadEvents();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);



        lotteryinfoButton.setOnClickListener(v -> {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.lottery_guidelines_dialog);

            closeInfoButton = dialog.findViewById(R.id.closeButton);
            closeInfoButton.setOnClickListener(closeView -> dialog.dismiss());

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
            dialog.show();
        });

        scanQRButton.setOnClickListener(v -> launchQRScanner());
        btnFilter.setOnClickListener((v -> showFilterDialog()));

        LinearLayout myProfile = findViewById(R.id.nav_profile);
        myProfile.setOnClickListener(v -> {
            Intent intent = new Intent(EventListActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });

        LinearLayout myNotifications = findViewById(R.id.nav_notifications);
        myNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(EventListActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });

        LinearLayout myHistory = findViewById(R.id.nav_history);
        myHistory.setOnClickListener(v -> {
            Intent intent = new Intent(EventListActivity.this, MyEventsActivity.class);
            startActivity(intent);
        });

        // ask for notification permission the second they open this screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        listenForStatusChanges();
        listenForEventChanges();
        listenForNewNotifications();


        // Implement the search
        eventsSearch = findViewById(R.id.eventsSearch);
        eventsSearch.clearFocus();

        eventsSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) { // Changes the screen as user is typing
                applyFilters();
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                applyFilters();
                eventsSearch.clearFocus();// Hides the keyboard
                return true;
            }
        });

    }


    private void loadEvents() {
        Timestamp now = Timestamp.now();
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        db.collection("events")
                .whereLessThanOrEqualTo("regStart", now)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allActiveEvents.clear();
                    eventList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        event.setId(doc.getId());

                        boolean isPublic = !Boolean.TRUE.equals(event.getPrivateEvent());
                        boolean isCoOrg = event.getCoOrganizers() != null
                                && event.getCoOrganizers().contains(deviceId);
                        boolean isInvited = event.getInvitedUsers() != null
                                && event.getInvitedUsers().contains(deviceId);
                        boolean isActive = event.getRegEnd() != null
                                && event.getRegEnd().compareTo(now) >= 0;

                        if (isActive && (isPublic || isCoOrg || isInvited)) {
                            allActiveEvents.add(event);
                        }
                    }
                    applyFilters();
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Log.e("EventListActivity", "Error loading events", e));
    }

    private void launchQRScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Press back to cancel");
        options.setBeepEnabled(false);
        options.setOrientationLocked(false);
        options.setBarcodeImageEnabled(false);
        scannerLauncher.launch(options);
    }


    private void listenForStatusChanges() {
        String deviceId = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ANDROID_ID);

        statusListener = db.collectionGroup("waitingList")
                .whereEqualTo("userId", deviceId)
                .addSnapshotListener((snapshots, e) -> {

                    if (e != null) {
                        Log.e("FirestoreListener", "Error", e);
                        return;
                    }

                    Log.d("FirestoreListener", "Status changed!");


                    loadEvents();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (statusListener != null) {
            statusListener.remove();
        }
        if (eventChangesListener != null) {
            eventChangesListener.remove();
        }
    }

    private void listenForNewNotifications() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String deviceId = new Profiles().getDeviceId(this);

        // setup channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("EVENT_ALERTS", "Event Alerts", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        long appStartTime = System.currentTimeMillis();
        db.collection("users").document(deviceId).collection("notifications")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        // only trigger on brand new messages, ignore old ones
                        if (dc.getType() == DocumentChange.Type.ADDED) {

                            Timestamp notifTime = dc.getDocument().getTimestamp("timestamp");
                            if (notifTime == null || notifTime.toDate().getTime() < appStartTime) {
                                continue; // skips old notifications
                            }

                            // check if they actually want notifications
                            db.collection("users").document(deviceId).get().addOnSuccessListener(doc -> {
                                Boolean wantsNotis = doc.getBoolean("notificationsEnabled");

                                // default to true if null just in case
                                if (wantsNotis == null || wantsNotis) {
                                    String messageText = dc.getDocument().getString("description");
                                    String titleText = dc.getDocument().getString("eventName");
                                    showSystemNotification(titleText, messageText);
                                }
                            });
                        }
                    }
                });
    }

    private void showSystemNotification(String title, String messageText) {
        // Needed for the newer andriod apparantly
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "EVENT_ALERTS")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("New Event Update!")
                .setContentText(messageText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void listenForEventChanges() {
        eventChangesListener = db.collection("events")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("EventListActivity", "Event listener error", e);
                        return;
                    }
                    loadEvents();
                });
    }

    private void showFilterDialog(){
        // Create the dialog
        Dialog filterDialog = new Dialog(this);
        filterDialog.setContentView(R.layout.fragment_filter);

        if (filterDialog.getWindow() != null) {
            filterDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            // Make it full width with some margins
            filterDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        ImageButton btnClose = filterDialog.findViewById(R.id.btn_close);
        btnClose.setOnClickListener(v -> filterDialog.dismiss());

        // Set up everything to do with the filter (button listerners and such)
        setupFilterDialogue(filterDialog);
        filterDialog.show();


    }

    private void setupFilterDialogue(Dialog dialog){

        TextInputEditText minDateInput = dialog.findViewById(R.id.filterMinEventDate);
        TextInputEditText maxDateInput = dialog.findViewById(R.id.filterMaxEventDate);
        TextInputEditText minSpotsInput = dialog.findViewById(R.id.filterMinSpots);
        TextInputEditText maxSpotsInput = dialog.findViewById(R.id.filterMaxSpots);
        ChipGroup chipGroup = dialog.findViewById(R.id.chipGroupEventType);
        Button filterApplyButton = dialog.findViewById(R.id.applyFiltersBtn);

        // Restore the state of everything
        if (activeMinSpots != null) {
            minSpotsInput.setText(String.valueOf(activeMinSpots));
        }
        if (activeMaxSpots != null) {
            maxSpotsInput.setText(String.valueOf(activeMaxSpots));
        }

        // Restore Date Limits
        if (activeMinDate != null) {
            minDateInput.setText(activeMinDate);
        }
        if (activeMaxDate != null) {
            maxDateInput.setText(activeMaxDate);
        }

        // Restore Selected Categories (Chips)
        if (activeCategories != null && !activeCategories.isEmpty()) {
            // Loop through all the chips in the group
            for (int i = 0; i < chipGroup.getChildCount(); i++) {

                android.view.View child = chipGroup.getChildAt(i);
                if (child instanceof com.google.android.material.chip.Chip) {
                    Chip chip = (com.google.android.material.chip.Chip) child;

                    // Set it to be selected
                    if (activeCategories.contains(chip.getText().toString())) {
                        chip.setChecked(true);
                    }
                }
            }
        }



        minDateInput.setOnClickListener(v -> showDatePicker(minDateInput));
        maxDateInput.setOnClickListener(v -> showDatePicker(maxDateInput));

        filterApplyButton.setOnClickListener(v->{
            gatherAndApplyFilters(dialog);
            dialog.dismiss();
        });


    }

    private void showDatePicker(TextInputEditText targetDateInput){
        // Get the caldendar
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this, (view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            targetDateInput.setText(selectedDate);
        },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();

    }

    private void gatherAndApplyFilters(Dialog dialog){
        // Get the selected category
        ChipGroup chipGroup = dialog.findViewById(R.id.chipGroupEventType);
        List<String> selectedCategories = new ArrayList<>();

        List<Integer> checkedChipIds = chipGroup.getCheckedChipIds();

        for (int id : checkedChipIds) {
            com.google.android.material.chip.Chip chip = dialog.findViewById(id);
            if (chip != null) {
                selectedCategories.add(chip.getText().toString());
            }
        }

        // Get Capacity Limits
        TextInputEditText minSpotsInput = dialog.findViewById(R.id.filterMinSpots);
        TextInputEditText maxSpotsInput = dialog.findViewById(R.id.filterMaxSpots);

        // Also take care if the field is empty
        Integer minSpots = minSpotsInput.getText().toString().isEmpty() ? null : Integer.parseInt(minSpotsInput.getText().toString());
        Integer maxSpots = maxSpotsInput.getText().toString().isEmpty() ? null : Integer.parseInt(maxSpotsInput.getText().toString());

        //Get Date Limits
        TextInputEditText minDateInput = dialog.findViewById(R.id.filterMinEventDate);
        TextInputEditText maxDateInput = dialog.findViewById(R.id.filterMaxEventDate);

        String minDate = minDateInput.getText().toString().isEmpty() ? null : minDateInput.getText().toString();
        String maxDate = maxDateInput.getText().toString().isEmpty() ? null : maxDateInput.getText().toString();

        // Save them to the Activity's state variables instead of passing them
        activeCategories = selectedCategories;
        activeMinSpots = minSpots;
        activeMaxSpots = maxSpots;
        activeMinDate = minDate;
        activeMaxDate = maxDate;

        // Pass to filtering logic
        applyFilters();
    }

    private void applyFilters(){
        displayedEvents.clear();

        String searchQuery;

        if (eventsSearch != null && eventsSearch.getQuery() != null) {
            searchQuery = eventsSearch.getQuery().toString().toLowerCase().trim();
        } else {
            searchQuery = "";
        }

        // parse the time difference for event time filter
        SimpleDateFormat localFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        localFormat.setTimeZone(TimeZone.getDefault());

        SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date minDateObject = null;
        Date maxDateObject = null;

        try {
            if (activeMinDate != null) {
                minDateObject = localFormat.parse(activeMinDate); // This Parses as 00:00:00 local time by itself so we can just set it
            }
            if (activeMaxDate != null) {
                // To include the entire day (since day ends at 11:59:59 we need to manually set it
                // Since default is 0:00:00
                Date parsedMax = localFormat.parse(activeMaxDate);
                Calendar c = Calendar.getInstance();
                c.setTime(parsedMax);
                c.set(Calendar.HOUR_OF_DAY, 23);
                c.set(Calendar.MINUTE, 59);
                c.set(Calendar.SECOND, 59);
                maxDateObject = c.getTime();
            }
        } catch (Exception e) {
            Log.e("Filter", "Error parsing filter dates", e);
        }



        for (Event event : allActiveEvents) {
            boolean matches = true;

            if (!searchQuery.isEmpty()) {
                String title = event.getTitle() != null ? event.getTitle().toLowerCase() : "";
                String desc = event.getDescription() != null ? event.getDescription().toLowerCase() : "";

                // If the query isn't in the title and isn't in the description, it's not a match
                if (!title.contains(searchQuery) && !desc.contains(searchQuery)) {
                    matches = false;
                }
            }


            // Category Filter
            if (matches && activeCategories != null && !activeCategories.isEmpty()) {
                boolean hasMatchingCategory = false;

                if (event.getEventType() != null) {
                    // Check if the event's type matches any of the selected chips
                    for (String cat : activeCategories) {
                        if (event.getEventType().equalsIgnoreCase(cat)) {   // See if any of them are matching
                            hasMatchingCategory = true;
                            break;
                        }
                    }
                }

                // If the event didn't match any selected category, it fails the filter
                if (!hasMatchingCategory) {
                    matches = false;
                }
            }

            // Capacity Filter
            if (matches && activeMinSpots != null && event.getMaxWaitingListSize() < activeMinSpots) {
                matches = false;
            }

            if (matches && activeMaxSpots != null && event.getMaxWaitingListSize() > activeMaxSpots) {
                matches = false;
            }

            // Date Filter
            if (matches && event.getDateEvent() != null && !event.getDateEvent().isEmpty()) {
                try {
                    // Parse the event's string as a UTC date
                    Date eventDateUTC = utcFormat.parse(event.getDateEvent());

                    // Compare the exact moments in time
                    if (minDateObject != null && eventDateUTC.before(minDateObject)) matches = false;
                    if (maxDateObject != null && eventDateUTC.after(maxDateObject)) matches = false;

                } catch (Exception e) {
                    Log.e("Filter", "Error parsing event date string", e);
                    matches = false;
                }
            } else if (matches && (activeMinDate != null || activeMaxDate != null)) {
                matches = false;
            }

            if (matches) {
                displayedEvents.add(event);
            }
        }

        adapter.notifyDataSetChanged();
    }

}

