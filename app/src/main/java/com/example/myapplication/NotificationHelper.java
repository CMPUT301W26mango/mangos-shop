package com.example.myapplication;

import android.util.Log;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class NotificationHelper {

    private static final String TAG = "NotificationHelper";

    /**
     * Sends a notification to a specific user's notification subcollection.
     * @param targetUserId The deviceId of the recipient
     * @param eventId The ID of the event related to the notification
     * @param eventName The name of the event
     * @param notiName The title of the notification (e.g., "Invitation!")
     * @param description The body text (e.g., "You have been invited to ")
     */
    public static void sendNotification(String targetUserId, String eventId, String eventName,
                                        String notiName, String description) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> notification = new HashMap<>();
        notification.put("eventId", eventId);
        notification.put("eventName", eventName);
        notification.put("notification_name", notiName);
        notification.put("description", description);
        notification.put("timestamp", Timestamp.now());
        notification.put("read", false);

        db.collection("users")
                .document(targetUserId)
                .collection("notifications")
                .add(notification)
                .addOnSuccessListener(documentReference ->
                        Log.d(TAG, "Notification sent to user: " + targetUserId))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error sending notification", e));
    }
}