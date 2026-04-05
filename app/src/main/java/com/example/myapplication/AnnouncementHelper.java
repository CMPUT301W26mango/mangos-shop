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

/**
 * Utility class responsible for broadcasting mass announcements to specific groups
 * of entrants for a given event (e.g., the entire waiting list or solely selected entrants).
 */
public class AnnouncementHelper {

    private static final String TAG = "AnnouncementHelper";

    /**
     *
     * Shows a popup dialog letting the Organizer type a custom message, then
     * pushes that message as a Notification to everyone in the specified status group.
     *
     * @param context           The activity context required to build and display the dialog.
     * @param eventId           The Firebase ID of the event associated with the announcement.
     * @param eventName         The name of the event for notification context.
     * @param targetDisplayName A descriptive name for the targeted group (e.g., "Waiting List") for the dialog title.
     * @param targetStatus      The list of entrant statuses to target (e.g., "Waiting", "Selected").
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
     * Queries the Firebase 'waitingList' sub-collection for entrants matching the specified status filters.
     * Iterates through the results and generates a newly formatted notification document
     * within each respective user's personal notification collection.
     *
     * @param context       The activity context used for displaying success or failure Toasts.
     * @param eventId       The Firebase ID of the event generating the announcement.
     * @param eventName     The name of the event.
     * @param statusFilters The specific statuses to query (e.g., ["Waiting", "Selected"]).
     * @param messageBody   The content of the announcement message.
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
                        notifData.put("notiName", "Organizer Announcement");
                        notifData.put("eventName", eventName != null ? eventName : "Event");
                        notifData.put("description", messageBody);
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