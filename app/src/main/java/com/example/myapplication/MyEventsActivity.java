package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyEventsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> myEventList;
    private FirebaseFirestore db;
    private TextView textViewEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.whole_event_list);

        db = FirebaseFirestore.getInstance();
        myEventList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerViewEvents);
        textViewEmpty = findViewById(R.id.textViewEmpty);

        // Don't think we need this?
        findViewById(R.id.eventsSearch).setVisibility(View.GONE);
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
                adapter.notifyDataSetChanged();
                if (textViewEmpty != null) {
                    textViewEmpty.setVisibility(myEventList.isEmpty() ? View.VISIBLE : View.GONE);
                }
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
                                adapter.notifyDataSetChanged();
                                if (textViewEmpty != null) {
                                    textViewEmpty.setVisibility(myEventList.isEmpty() ? View.VISIBLE : View.GONE);
                                }
                            }
                        });
            }
        }).addOnFailureListener(e -> Log.e("MyEventsActivity", "Error loading events", e));
    }

    private void setupBottomNavigation() {
        LinearLayout navEvents = findViewById(R.id.nav_events);
        LinearLayout navMyEvents = findViewById(R.id.nav_my_events);
        LinearLayout navNotifications = findViewById(R.id.nav_notifications);
        LinearLayout navProfile = findViewById(R.id.nav_profile);

        navEvents.setOnClickListener(v -> {
            startActivity(new Intent(this, EventListActivity.class));
            finish();
        });


        navMyEvents.setOnClickListener(v -> {});

        navNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
            finish();
        });

        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, UserProfileActivity.class));
            finish();
        });
    }
}