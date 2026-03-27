package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Activity that displays all of the events created by the organizer
 * From this page organizers can click to go to the event creation page
 * Organizers can click on individual events to see more details
 * @author Sayuj
 */

public class OrganizerDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_dashboard);

        recyclerView = findViewById(R.id.events_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton createEvent = findViewById(R.id.add_event);
        createEvent.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerDashboardActivity.this, EventCreateActivity.class);
            startActivity(intent);
        });

        LinearLayout myEvents = findViewById(R.id.my_events);
        myEvents.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerDashboardActivity.this, OrganizerDashboardActivity.class);
            startActivity(intent);
        });

        LinearLayout myProfile = findViewById(R.id.nav_profile);
        myProfile.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerDashboardActivity.this, EntrantAccount.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data every time the screen is visible
        refreshEventList();
    }

    private void refreshEventList() {
        Profiles profilesHelper = new Profiles();
        String myId = profilesHelper.getDeviceId(this);

        EventStore eventStore = new EventStore();

        // Only call this once!
        eventStore.getEventsByOrganizer(myId, events -> {
            // Pass the click listener (the interface we discussed)
            OrganizerEventAdapter adapter = new OrganizerEventAdapter(events, event -> {
                Intent intent = new Intent(this, EventDetailActivity.class);
                intent.putExtra("EVENT_ID", event.getId());
                startActivity(intent);
            });
            recyclerView.setAdapter(adapter);
        });
    }
}