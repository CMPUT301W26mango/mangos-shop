package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class AnnouncementHelper {

    private static final String TAG = "AnnouncementHelper";

    /**
     *
     * Shows a popup dialog letting the Organizer type a custom message, then
     * pushes that message as a Notification to everyone in the specified status group.
     *
     * @param context
     * @param eventId
     * @param eventName
     * @param targetStatus
     * @param targetDisplayName
     */
    public static void showAnnouncementDialog(Context context, String eventId, String eventName, String targetDisplayName, java.util.List<String> targetStatus) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Message " + targetDisplayName + " Entrants");

        final EditText input = new EditText(context);
        input.setHint("Type your announcement here...");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        layout.addView(input);
        builder.setView(layout);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String message = input.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMassNotification(context, eventId, eventName, targetStatus, message);
            } else {
                Toast.makeText(context, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     *
     * @param context
     * @param eventId
     * @param eventName
     * @param statusFilters
     * @param messageBody
     */
    private static void sendMassNotification(Context context, String eventId, String eventName, java.util.List<String> statusFilters, String messageBody) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events").document(eventId).collection("waitingList")
                .whereIn("status", statusFilters)
                .get()
                .addOnSuccessListener((QuerySnapshot snapshots) -> {
                    if (snapshots.isEmpty()) {
                        Toast.makeText(context, "No users found in this list.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int count = 0;
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String userId = doc.getId(); // Device ID of the entrant

                        Map<String, Object> notifData = new HashMap<>();
                        notifData.put("recipientDeviceId", userId);
                        notifData.put("message", "Organizer Announcement: " + messageBody);
                        notifData.put("eventId", eventId);
                        notifData.put("timestamp", Timestamp.now());
                        notifData.put("read", false);

                        db.collection("users").document(userId).collection("notifications").add(notifData);
                        count++;
                    }
                    Toast.makeText(context, "Sent to " + count + " users.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch entrants for announcement", e);
                    Toast.makeText(context, "Failed to send announcement", Toast.LENGTH_SHORT).show();
                });
    }
}