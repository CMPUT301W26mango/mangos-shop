package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity responsible for displaying the user's personal event history.
 * This class filters events from Firestore to show only those where the user
 * is a co-organizer, an invited guest, or an active entrant on a waiting list.
 */
public class MyEventsActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> myEventList;
    private FirebaseFirestore db;
    private TextView textViewEmpty;
    private List<Event> fullEventList;


    /**
     * Initializes the activity, sets the content view, and configures the UI components.
     * Overrides the default page title and hides standard event list controls to
     * focus on the history view.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     * being shut down, this contains the most recent data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.whole_event_list);

        setupBottomNavigation("Entrant");

        TextView pageTitle = findViewById(R.id.top_page_title);
        pageTitle.setText("Event History");

        db = FirebaseFirestore.getInstance();
        myEventList = new ArrayList<>();
        fullEventList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerViewEvents);
        textViewEmpty = findViewById(R.id.textViewEmpty);

        SearchView searchBar = findViewById(R.id.eventsSearch);

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterEvents(newText.trim());
                return true;
            }
        });
        // Hide utility buttons not required for the history view
        findViewById(R.id.btnFilter).setVisibility(View.GONE);
        findViewById(R.id.scanQRButton).setVisibility(View.GONE);
        findViewById(R.id.lotteryinfoButton).setVisibility(View.GONE);

        // ensure borders
        adapter = new EventAdapter(myEventList, getSupportFragmentManager(), true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        setupBottomNavigation();
        loadMyEvents();
    }

    /**
     * Retrieves events from Firestore associated with the current device ID.
     * Iterates through the global events collection and checks the waiting list to see user participation(selected/waiting/notselected/accepted).
     * Updates the UI to show an empty state message if no events are found.
     */
    private void loadMyEvents() {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        db.collection("events").get().addOnSuccessListener(queryDocumentSnapshots -> {
            myEventList.clear();
            List<Event> pendingEvents = new ArrayList<>();

            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Event event = doc.toObject(Event.class);
                event.setId(doc.getId());

                boolean isCoOrg = event.getCoOrganizers() != null && event.getCoOrganizers().contains(deviceId);
                boolean isInvited = event.getInvitedUsers() != null && event.getInvitedUsers().contains(deviceId);

                if (isCoOrg || isInvited) {
                    myEventList.add(event);
                } else {
                    pendingEvents.add(event);
                }
            }

            // If there's nothing to check, just update the screen
            if (pendingEvents.isEmpty()) {
                updateUIAndBackup();
                return;
            }

            int[] checkedCount = {0};
            for (Event e : pendingEvents) {
                db.collection("events").document(e.getId()).collection("waitingList").document(deviceId).get()
                        .addOnCompleteListener(task -> {
                            checkedCount[0]++;

                            if (task.isSuccessful() && task.getResult().exists()) {
                                myEventList.add(e);
                            }

                            if (checkedCount[0] == pendingEvents.size()) {
                                updateUIAndBackup();
                            }
                        });
            }
        }).addOnFailureListener(e -> Log.e("MyEventsActivity", "Error loading events", e));
    }

    /**
     * Configures the click listeners for the custom bottom navigation bar.
     * Handles transitions between Event List, Notifications, and User Profile activities.
     */
    private void setupBottomNavigation() {
        LinearLayout navEvents = findViewById(R.id.nav_events);
        LinearLayout navNotifications = findViewById(R.id.nav_notifications);
        LinearLayout navProfile = findViewById(R.id.nav_profile_entrant);

        navEvents.setOnClickListener(v -> {
            startActivity(new Intent(this, EventListActivity.class));
            finish();
        });

        navNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
            finish();
        });

        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, UserProfileActivity.class));
            finish();
        });
    }

    /**
     * Filters the event list based on the user's search query.
     *
     * @param query The search
     */
    private void filterEvents(String query) {
        List<Event> filteredList = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase();

        for (Event event : fullEventList) {
            String title = event.getTitle() != null ? event.getTitle().toLowerCase() : "";
            String location = event.getLocation() != null ? event.getLocation().toLowerCase() : "";

            if (title.contains(lowerCaseQuery) || location.contains(lowerCaseQuery)) {
                filteredList.add(event);
            }
        }

        adapter.updateList(filteredList);

        if (textViewEmpty != null) {
            textViewEmpty.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Refreshes the adapter and syncs the backup list for searching.
     */
    private void updateUIAndBackup() {
        adapter.notifyDataSetChanged();
        fullEventList.clear();
        fullEventList.addAll(myEventList);
        if (textViewEmpty != null) {
            textViewEmpty.setVisibility(myEventList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }
}