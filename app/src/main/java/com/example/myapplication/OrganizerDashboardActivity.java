package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.widget.Button;
import android.app.AlertDialog;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Button btnNotify = findViewById(R.id.btn_send_notifications);
        btnNotify.setOnClickListener(v -> fetchEventsAndShowBroadcastDialog());

        LinearLayout myEvents = findViewById(R.id.my_events);
        myEvents.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerDashboardActivity.this, OrganizerDashboardActivity.class);
            startActivity(intent);
        });

        LinearLayout myProfile = findViewById(R.id.nav_profile);
        myProfile.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerDashboardActivity.this, UserProfileActivity.class);
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

        EventDatabase eventDatabase = new EventDatabase();


        eventDatabase.getEventsByOrganizer(myId, events -> {
            OrganizerEventAdapter adapter = new OrganizerEventAdapter(events, event -> {
                Intent intent = new Intent(this, EventDetailActivity.class);
                intent.putExtra("EVENT_ID", event.getId());
                startActivity(intent);
            });
            recyclerView.setAdapter(adapter);
        });
    }

    // grab my events first so the dropdown actually has stuff in it
    private void fetchEventsAndShowBroadcastDialog() {
        String myDeviceId = new Profiles().getDeviceId(this);
        EventDatabase eventDatabase = new EventDatabase();

        eventDatabase.getEventsByOrganizer(myDeviceId, events -> {
            if (events == null || events.isEmpty()) {
                Toast.makeText(this, "Need to create an event first", Toast.LENGTH_SHORT).show();
                return;
            }
            buildBroadcastDialog(events);
        });
    }

    // setting up the alert dialog for the broadcast
    private void buildBroadcastDialog(List<Event> myEvents) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Global Broadcast");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // event selection dropdown
        TextView lblEvent = new TextView(this);
        lblEvent.setText("Select Event:");
        layout.addView(lblEvent);

        Spinner eventSpinner = new Spinner(this);
        List<String> eventNames = new ArrayList<>();
        for (Event e : myEvents) {
            eventNames.add(e.getTitle());
        }
        ArrayAdapter<String> eventAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, eventNames);
        eventSpinner.setAdapter(eventAdapter);
        layout.addView(eventSpinner);

        // target group dropdown
        TextView lblGroup = new TextView(this);
        lblGroup.setText("\nSelect Target Group:");
        layout.addView(lblGroup);

        Spinner groupSpinner = new Spinner(this);
        String[] groupOptions = {"Waitlist Only", "Selected Entrants", "Cancelled/Rejected Entrants"};
        ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, groupOptions);
        groupSpinner.setAdapter(groupAdapter);
        layout.addView(groupSpinner);

        // message input
        EditText messageInput = new EditText(this);
        messageInput.setHint("\nType your message here...");
        layout.addView(messageInput);

        builder.setView(layout);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String message = messageInput.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            Event selectedEvent = myEvents.get(eventSpinner.getSelectedItemPosition());
            int groupIndex = groupSpinner.getSelectedItemPosition();

            String targetStatus = "waiting";
            if (groupIndex == 1) targetStatus = "selected";
            if (groupIndex == 2) targetStatus = "rejected";

            executeBlast(selectedEvent.getId(), targetStatus, message, selectedEvent.getTitle());        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // pushing the message directly to the entrants' notification sub-collections
    private void executeBlast(String eventId, String targetStatus, String message, String eventName) {
        FirebaseFirestore.getInstance().collection("events").document(eventId).collection("waitingList")
                .whereEqualTo("status", targetStatus)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    if (querySnapshots.isEmpty()) {
                        Toast.makeText(this, "No entrants found in that list", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int sentCount = 0;
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        String entrantDeviceId = doc.getId();

                        // push message to their inbox
                        Map<String, Object> notifData = new HashMap<>();
                        notifData.put("eventId", eventId); // Important for the "Click here" link
                        notifData.put("eventName", eventName);
                        notifData.put("notiName", "Organizer Broadcast");
                        notifData.put("description", message); // This maps to what the entrant sees as the message
                        notifData.put("read", false);
                        notifData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
                        String senderId = new Profiles().getDeviceId(this);
                        notifData.put("senderId", senderId);


                        db.collection("users")
                                .document(entrantDeviceId)
                                .collection("notifications")
                                .add(notifData);

                        sentCount++;
                    }

                    String senderId = new Profiles().getDeviceId(this);

                    db.collection("users")
                            .get()
                            .addOnSuccessListener(users -> {

                                String senderName = "Unknown";

                                for (QueryDocumentSnapshot user : users) {
                                    if (user.getString("name") != null) {
                                        senderName = user.getString("name");
                                        break;
                                    }
                                }

                                Map<String, Object> logData = new HashMap<>();
                                logData.put("message", message);
                                logData.put("eventName", eventName);
                                logData.put("senderId", senderId);
                                logData.put("senderName", senderName); // ✅ ADD THIS
                                logData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

                                db.collection("notifications").add(logData);
                            });
                    Toast.makeText(this, "Notification sent to " + sentCount + " entrants", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to reach entrants", Toast.LENGTH_SHORT).show());
    }
}