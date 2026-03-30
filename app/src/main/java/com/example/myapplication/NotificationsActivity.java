package com.example.myapplication;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that displays all notifications for the current entrant
 */
public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationItem> notificationList;
    private FirebaseFirestore db;
    private String deviceId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        deviceId = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ANDROID_ID);

        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerViewNotifications);
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList, getSupportFragmentManager(), db, deviceId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadNotifications();
    }

    /**
     * Loads notifications from Firestore for the current device
     * ordered by most recent first
     */
    private void loadNotifications() {
        db.collection("users")
                .document(deviceId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    notificationList.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        NotificationItem item = new NotificationItem(
                                doc.getId(),
                                doc.getString("eventId"),
                                doc.getString("eventName"),
                                doc.getString("notification_name"),
                                doc.getString("description"),
                                doc.getTimestamp("timestamp"),
                                Boolean.TRUE.equals(doc.getBoolean("read"))
                        );
                        notificationList.add(item);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}