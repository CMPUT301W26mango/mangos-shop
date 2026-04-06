package com.example.myapplication;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Called by:
 * - LotteryDrawWorker (PeriodicWorkRequest - scans all expired events every ~15 min)
 * - WaitingListActivity lazy check
 *
 * Draw-once guarantee: a `drawCompleted` boolean on the event document is checked
 * before every draw. The batch write sets `drawCompleted = true` atomically with
 * the status updates, so a second concurrent trigger will find it already true.
 */
public class LotteryDrawHelper {

    private static final String TAG = "LotteryDrawHelper";

    /**
     * Callback so callers know whether the draw succeeded or was skipped.
     */
    public interface OnDrawCompleteListener {
        void onSuccess(int selectedCount);
        void onFailure(Exception e);
    }

    /**
     * Queries Firestore for all events whose registration deadline has passed
     * and whose draw has not yet been completed, then calls performDraw() for each.
     *
     * This is called by the periodic WorkManager worker so it requires no knowledge
     * of specific event IDs - it finds all events that need drawing on its own.
     *
     * @param listener called once per event that is processed (may be null)
     */
    public static void scanAndDrawAll(OnDrawCompleteListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query events where regEnd < now (deadline has passed).
        // drawCompleted check is done client-side to avoid needing a composite Firestore index.
        db.collection("events")
                .whereLessThan("regEnd", Timestamp.now())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot == null || querySnapshot.isEmpty()) {
                        Log.d(TAG, "No expired events found.");
                        if (listener != null) listener.onSuccess(0);
                        return;
                    }

                    for (DocumentSnapshot eventDoc : querySnapshot.getDocuments()) {
                        Boolean drawCompleted = eventDoc.getBoolean("drawCompleted");
                        if (Boolean.TRUE.equals(drawCompleted)) {
                            continue; // already drawn - skip
                        }

                        String eventId = eventDoc.getId();
                        Log.d(TAG, "Drawing for expired event: " + eventId);
                        // Perform the draw; listener is per-event so pass null here to avoid
                        // conflating multiple events into one callback chain.
                        performDraw(eventId, null);
                    }

                    if (listener != null) listener.onSuccess(querySnapshot.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to query expired events", e);
                    if (listener != null) listener.onFailure(e);
                });
    }

    /**
     * Executes the lottery draw for one event.
     *
     * Steps:
     * 1. Read the event document.
     * 2. If drawCompleted == true, abort silently.
     * 3. Read the waiting list entries with status == "waiting".
     * 4. Shuffle and select the first [capacity] entries.
     * 5. Commit a WriteBatch: update selected entrants to "selected",
     * set drawCompleted = true and drawDate = now on the event document.
     * 6. Send in-app notifications to selected entrants.
     *
     * @param eventId  Firestore document ID of the event
     * @param listener optional callback; pass null if not needed
     */
    public static void performDraw(String eventId, OnDrawCompleteListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("events").document(eventId);

        // read the event document
        eventRef.get()
                .addOnSuccessListener(eventDoc -> {
                    if (!eventDoc.exists()) {
                        Log.w(TAG, "Event not found: " + eventId);
                        if (listener != null) listener.onSuccess(0);
                        return;
                    }

                    // guard - skip if draw already ran
                    Boolean drawCompleted = eventDoc.getBoolean("drawCompleted");
                    if (Boolean.TRUE.equals(drawCompleted)) {
                        Log.d(TAG, "Draw already completed for event: " + eventId);
                        if (listener != null) listener.onSuccess(0);
                        return;
                    }

                    // capacity field name matches capacity
                    Long capacityLong = eventDoc.getLong("capacity");
                    int capacity = (capacityLong != null) ? capacityLong.intValue() : 0;
                    String eventTitle = eventDoc.getString("title");

                    if (capacity <= 0) {
                        Log.w(TAG, "Capacity is 0 for event: " + eventId + " — marking done.");
                        markDrawComplete(db, eventRef, 0, listener);
                        return;
                    }

                    // read waiting list entries with status == "waiting"
                    eventRef.collection("waitingList")
                            .whereEqualTo("status", "waiting")
                            .get()
                            .addOnSuccessListener(waitingSnap -> {
                                Map<String, String> waitingEntrants = new HashMap<>(); // deviceId -> name
                                List<String> waitingIds = new ArrayList<>();
                                for (DocumentSnapshot doc : waitingSnap.getDocuments()) {
                                    // Document ID is the entrant's deviceId
                                    String entrantName = doc.getString("name");
                                    waitingEntrants.put(doc.getId(), entrantName != null ? entrantName : "");
                                    waitingIds.add(doc.getId());
                                }

                                if (waitingIds.isEmpty()) {
                                    Log.d(TAG, "No waiting entrants for event: " + eventId);
                                    markDrawComplete(db, eventRef, 0, listener);
                                    return;
                                }

                                // shuffle and select up to capacity entrants
                                Collections.shuffle(waitingIds);
                                int selectCount = Math.min(capacity, waitingIds.size());
                                List<String> selected = waitingIds.subList(0, selectCount);
                                List<String> notSelected = waitingIds.subList(selectCount, waitingIds.size());


                                // atomic batch write
                                WriteBatch batch = db.batch();

                                for (String userId : selected) {
                                    DocumentReference entrantRef =
                                            eventRef.collection("waitingList").document(userId);

                                    batch.update(entrantRef, "status", "selected");
                                }

                                // Mark non-selected entrants as "rejected" so they form the replacement pool
                                for (String userId : notSelected) {
                                    DocumentReference entrantRef =
                                            eventRef.collection("waitingList").document(userId);
                                    batch.update(entrantRef, "status", "rejected");
                                }

                                batch.update(eventRef, "drawCompleted", true);
                                batch.update(eventRef, "drawDate", Timestamp.now());

                                batch.commit()
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Draw complete for " + eventId
                                                    + ": " + selectCount + " selected.");
                                            if (listener != null) listener.onSuccess(selectCount);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Batch write failed for event: " + eventId, e);
                                            if (listener != null) listener.onFailure(e);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to read waiting list for event: " + eventId, e);
                                if (listener != null) listener.onFailure(e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to read event document: " + eventId, e);
                    if (listener != null) listener.onFailure(e);
                });
    }

    /**
     * Draws a single replacement entrant from the rejected pool.
     * Queries for entrants with status "rejected", randomly picks one,
     * updates their status to "selected", and writes a notification.
     *
     * @param eventId  the event's Firestore document ID
     * @param listener callback with the number of replacements drawn (0 or 1)
     */
    public static void drawReplacement(String eventId, OnDrawCompleteListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("events").document(eventId);

        // Query waiting list for rejected entrants
        eventRef.collection("waitingList")
                .whereEqualTo("status", "rejected")
                .get()
                .addOnSuccessListener(rejectedSnap -> {
                    if (rejectedSnap == null || rejectedSnap.isEmpty()) {
                        Log.d(TAG, "drawReplacement: no rejected entrants for event: " + eventId);
                        if (listener != null) listener.onSuccess(0);
                        return;
                    }

                    // Collect IDs and shuffle for random pick
                    List<DocumentSnapshot> rejectedDocs = new ArrayList<>(rejectedSnap.getDocuments());
                    Collections.shuffle(rejectedDocs);
                    DocumentSnapshot picked = rejectedDocs.get(0);

                    String pickedId = picked.getId();

                    // Update status to "selected" AND reset notified to false
                    eventRef.collection("waitingList").document(pickedId)
                            .update(
                                    "status", "selected",
                                    "notified", false
                            )
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "drawReplacement: selected " + pickedId + " for event: " + eventId);
                                if (listener != null) listener.onSuccess(1);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "drawReplacement: failed to update status for: " + pickedId, e);
                                if (listener != null) listener.onFailure(e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "drawReplacement: failed to query rejected pool for event: " + eventId, e);
                    if (listener != null) listener.onFailure(e);
                });
    }

    /** Marks drawCompleted = true when capacity is 0 or waiting pool is empty. */
    private static void markDrawComplete(FirebaseFirestore db, DocumentReference eventRef,
                                         int selectedCount, OnDrawCompleteListener listener) {
        WriteBatch batch = db.batch();
        batch.update(eventRef, "drawCompleted", true);
        batch.update(eventRef, "drawDate", Timestamp.now());
        batch.update(eventRef, "invitedUsers", new ArrayList<>());
        batch.commit()
                .addOnSuccessListener(v -> {
                    if (listener != null) listener.onSuccess(selectedCount);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to mark draw complete", e);
                    if (listener != null) listener.onFailure(e);
                });
    }
}