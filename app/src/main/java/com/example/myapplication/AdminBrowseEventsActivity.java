/**
 * Activity that allows an administrator to browse all events,
 * search for events by title, and navigate to the admin event detail screen.
 *
 * Role in application:
 * - Admin control screen for viewing events.
 *
 * Outstanding issues:
 * - UI currently depends on Firestore field names remaining consistent.
 */

package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.LinearLayout;
import android.app.AlertDialog;


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
    private LinearLayout buttonBrowseProfiles;
    private LinearLayout buttonBrowseImages;
    private LinearLayout buttonLogs;

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
        buttonBrowseProfiles = findViewById(R.id.buttonBrowseProfiles);
        buttonBrowseImages = findViewById(R.id.buttonBrowseImages);

        buttonBrowseProfiles.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminBrowseProfilesActivity.class);
            startActivity(intent);
        });
        buttonBrowseImages.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminBrowseImagesActivity.class);
            startActivity(intent);
        });
        buttonLogs = findViewById(R.id.buttonLogs);

        buttonLogs.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminLogsActivity.class));
        });

        db = FirebaseFirestore.getInstance();

        recyclerViewAdminEvents.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminEventAdapter(filteredEvents, eventItem -> {

            View dialogView = getLayoutInflater().inflate(R.layout.dialog_admin_event_detail, null);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create();

            TextView tvTitle = dialogView.findViewById(R.id.tv_event_title);
            TextView tvLocation = dialogView.findViewById(R.id.tv_event_location);
            TextView tvOrganizer = dialogView.findViewById(R.id.tv_organizer);

            Button btnDeleteEvent = dialogView.findViewById(R.id.btn_delete_event);
            Button btnDeleteImage = dialogView.findViewById(R.id.btn_delete_image);

            ImageButton btnClose = dialogView.findViewById(R.id.btn_close);

            tvTitle.setText(eventItem.getTitle());
            tvLocation.setText(eventItem.getLocation());
            tvOrganizer.setText(eventItem.getOrganizerName());

            btnClose.setOnClickListener(v -> dialog.dismiss());

            btnDeleteEvent.setOnClickListener(v -> {
                FirebaseFirestore.getInstance()
                        .collection("events")
                        .document(eventItem.getEventId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadEvents();
                        });
            });

            btnDeleteImage.setOnClickListener(v -> {
                FirebaseFirestore.getInstance()
                        .collection("events")
                        .document(eventItem.getEventId())
                        .update("posterURL", "")
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadEvents();
                        });
            });
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.show();
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

                        String organizerId = document.getString("organizerId");

                        AdminEventItem eventItem = new AdminEventItem(
                                eventId, title, location, organizerName, posterURL, organizerId
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