package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminBrowseEventsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewAdminEvents;
    private SearchView searchViewEvents;
    private TextView textViewEmptyEvents;

    private FirebaseFirestore db;
    private AdminEventAdapter adapter;

    private final List<AdminEventItem> allEvents = new ArrayList<>();
    private final List<AdminEventItem> filteredEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_browse_events);

        recyclerViewAdminEvents = findViewById(R.id.recyclerViewAdminEvents);
        searchViewEvents = findViewById(R.id.searchViewEvents);
        textViewEmptyEvents = findViewById(R.id.textViewEmptyEvents);

        db = FirebaseFirestore.getInstance();

        recyclerViewAdminEvents.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminEventAdapter(filteredEvents, eventItem -> {
            Intent intent = new Intent(this, AdminEventDetailActivity.class);
            intent.putExtra("eventId", eventItem.getEventId());
            intent.putExtra("title", eventItem.getTitle());
            intent.putExtra("location", eventItem.getLocation());
            intent.putExtra("organizer", eventItem.getOrganizerName());
            intent.putExtra("posterURL", eventItem.getPosterURL());
            startActivity(intent);
        });

        recyclerViewAdminEvents.setAdapter(adapter);

        loadEvents();
        setupSearch();
    }

    private void loadEvents() {
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    allEvents.clear();
                    filteredEvents.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                        String eventId = document.getId();
                        String title = document.getString("title");
                        String location = document.getString("location");
                        String organizerName = document.getString("organizerName");
                        String posterURL = document.getString("posterURL");

                        if (title == null) {
                            title = "Untitled Event";
                        }

                        AdminEventItem eventItem = new AdminEventItem(
                                eventId,
                                title,
                                location,
                                organizerName,
                                posterURL
                        );

                        allEvents.add(eventItem);
                    }

                    filteredEvents.addAll(allEvents);
                    adapter.notifyDataSetChanged();
                    updateEmptyState();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupSearch() {

        searchViewEvents.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                filterEvents(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterEvents(newText);
                return true;
            }
        });
    }

    private void filterEvents(String query) {

        filteredEvents.clear();

        if (query == null || query.trim().isEmpty()) {

            filteredEvents.addAll(allEvents);

        } else {

            String lowerQuery = query.toLowerCase();

            for (AdminEventItem event : allEvents) {

                if (event.getTitle() != null &&
                        event.getTitle().toLowerCase().contains(lowerQuery)) {

                    filteredEvents.add(event);
                }
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {

        if (filteredEvents.isEmpty()) {

            textViewEmptyEvents.setVisibility(View.VISIBLE);
            recyclerViewAdminEvents.setVisibility(View.GONE);

        } else {

            textViewEmptyEvents.setVisibility(View.GONE);
            recyclerViewAdminEvents.setVisibility(View.VISIBLE);
        }
    }
}