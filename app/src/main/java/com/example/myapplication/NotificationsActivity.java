package com.example.myapplication;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity responsible for managing and displaying the user's notifications.
 * This class fetches real-time updates from Firestore, maps them to NotificationItem models,
 * and provides functionality to clear individual or all notifications.
 */
public class NotificationsActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationItem> notificationList;
    private FirebaseFirestore db;
    private String deviceId;

    /**
     * Initializes the activity, sets the content view, and configures the UI components.
     * Establishes the connection to the RecyclerView and attaches the custom NotificationAdapter.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     * being shut down, this contains the most recent data.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // find device ID
        deviceId = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ANDROID_ID);

        // initialize instance
        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerViewNotifications);
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList, getSupportFragmentManager(), db, deviceId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // load all notifications
        loadNotifications();

        // clear button
        Button clearAllBtn = findViewById(R.id.clear_all_btn);


        clearAllBtn.setOnClickListener(v -> clearAllNotifications());
    }

    /**
     * Retrieves the user's notifications from the Firestore database in real-time.
     * Attaches a snapshot listener to the specific user's notification sub-collection,
     * ordering the results so the most recent notifications appear at the top.
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
                                doc.getString("notiName"),
                                doc.getString("description"),
                                doc.getTimestamp("timestamp"),
                                Boolean.TRUE.equals(doc.getBoolean("read"))
                        );
                        notificationList.add(item);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    /**
     * Deletes all notifications currently associated with the user's device ID.
     * Iterates through the current list of notifications and issues a delete command
     * to Firestore for each respective document.
     */
    private void clearAllNotifications() {
        if (notificationList.isEmpty()) return;

        // loop through subcollection to delete all notifications
        for (NotificationItem item : notificationList) {
            db.collection("users")
                    .document(deviceId)
                    .collection("notifications")
                    .document(item.getId())
                    .delete()
                    .addOnFailureListener(e -> Log.e("ClearAll", "Failed to delete: " + item.getId()));
        }

        Toast.makeText(this, "All notifications cleared", Toast.LENGTH_SHORT).show();
    }

}