package com.example.myapplication;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyEventsActivity extends AppCompatActivity {

    private RecyclerView historyRecyclerView;
    private EventHistoryAdapter adapter;
    private List<EventHistory> displayList;
    private FirebaseFirestore db;
    private String deviceId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        historyRecyclerView = findViewById(R.id.history_list_view);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // added semicolon

        // initialize the list and attach the adapter
        displayList = new ArrayList<>();
        adapter = new EventHistoryAdapter(displayList);
        historyRecyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        fetchMyEventHistory();
    }

    private void fetchMyEventHistory() {
        db.collection("events").get().addOnSuccessListener(eventSnapshots -> {
            displayList.clear();
            for (QueryDocumentSnapshot eventDoc : eventSnapshots) {
                String eventId = eventDoc.getId();
                String eventTitle = eventDoc.getString("title");

                eventDoc.getReference().collection("waitingList").document(deviceId)
                        .get().addOnSuccessListener(userEntry -> {
                            if (userEntry.exists()) {
                                String status = userEntry.getString("status");
                                if (status == null) status = "waiting";

                                EventHistory historyItem = new EventHistory(eventId, eventTitle, status.toUpperCase());
                                displayList.add(historyItem);
                                adapter.notifyDataSetChanged();
                            }
                        }).addOnFailureListener(e -> Log.e("MyEvents", "Error fetching user status", e));
            }
        }).addOnFailureListener(e -> Log.e("MyEvents", "Error fetching events", e));
    }
}