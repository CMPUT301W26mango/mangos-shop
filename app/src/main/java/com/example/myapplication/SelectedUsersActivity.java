package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity that displays and manages the list of users selected during an event lottery draw.
 *
 * This screen organizes entrants into categorized sections based on their current status:
 * Accepted, Declined/Rejected, Pending, and Cancelled.
 * It provides administrative actions for organizers, such as revoking invitations
 * or drawing new entrants from the waiting list to fill spots left by declined users.
 * It also does sending bulk announcements to specific groups of entrants.
 * @author Sayuj
 */

public class SelectedUsersActivity extends AppCompatActivity {

    private static final String TAG = "SelectedUsersActivity";
    private static final int CSV_EXPORT_REQUEST = 1001;

    private FirebaseFirestore db;
    private String eventId;
    private String eventName;

    private LinearLayout containerAccepted;
    private LinearLayout containerDeclined;
    private LinearLayout containerPending;
    private LinearLayout containerCancelled;

    private Button btnSendNotifications;
    private Button btnExportCsv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_users);

        eventId = getIntent().getStringExtra("eventId");
        db = FirebaseFirestore.getInstance();

        // Back button
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        containerAccepted = findViewById(R.id.container_accepted);
        containerDeclined = findViewById(R.id.container_declined);
        containerPending = findViewById(R.id.container_pending);
        containerCancelled = findViewById(R.id.container_cancelled);

        loadUsers();

        eventName = getIntent().getStringExtra("eventName"); // Grab the name passed from Event Details

        Button btnMessageSelected = findViewById(R.id.btnMessageSelected);
        btnMessageSelected.setOnClickListener(v -> {
            AnnouncementHelper.showAnnouncementDialog(this, eventId, eventName, "Selected Entrants", java.util.Arrays.asList("selected", "accepted"));
        });

        Button btnMessageCancelled = findViewById(R.id.btnMessageCancelled);
        btnMessageCancelled.setOnClickListener(v -> {
            AnnouncementHelper.showAnnouncementDialog(this, eventId, eventName, "Unsuccessful/Cancelled", java.util.Arrays.asList("cancelled", "declined", "not_selected"));
        });

        btnSendNotifications = findViewById(R.id.btnSendNotifications);
        btnSendNotifications.setOnClickListener(v -> sendNotifications());

        btnExportCsv = findViewById(R.id.btnExportCsv);
        btnExportCsv.setOnClickListener(v -> {
            String safeEventName = (eventName != null && !eventName.isEmpty()) ? eventName : "Event";
            Intent intent = CsvExportHelper.createExportIntent(safeEventName);
            startActivityForResult(intent, CSV_EXPORT_REQUEST);
        });
    }

    private void loadUsers() {
        db.collection("events").document(eventId)
                .collection("waitingList")
                .get()
                .addOnSuccessListener(snapshots -> {
                    containerAccepted.removeAllViews();
                    containerDeclined.removeAllViews();
                    containerPending.removeAllViews();
                    containerCancelled.removeAllViews();

                    for (QueryDocumentSnapshot doc : snapshots) {
                        String userId = doc.getId();
                        String status = doc.getString("status");
                        if (status == null) status = "waiting";

                        switch (status) {
                            case "accepted":
                                addUserRow(containerAccepted, userId, "accepted", doc.getId());
                                break;
                            case "not_selected":
                            case "declined":
                                addUserRow(containerDeclined, userId, "declined", doc.getId());
                                break;
                            case "selected":
                                addUserRow(containerPending, userId, "selected", doc.getId());
                                break;
                            case "cancelled":
                            case "canceled":
                                addUserRow(containerCancelled, userId, "cancelled", doc.getId());
                                break;
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load waiting list", e));
    }

    /**
     * Inflates a user row and adds it to the given section container.
     * Shows Replace button for declined users, Cancel button for pending users.
     */
    private void addUserRow(LinearLayout container, String userId, String status, String docId) {
        // Fetch user name from users collection
        db.collection("users").document(userId).get()
                .addOnSuccessListener(userDoc -> {
                    String name = userDoc.getString("name");
                    if (name == null || name.isEmpty()) name = userId;

                    View row = LayoutInflater.from(this)
                            .inflate(R.layout.item_selected_user, container, false);

                    TextView tvName = row.findViewById(R.id.user_name);
                    android.widget.Button btnAction = row.findViewById(R.id.btn_action);

                    tvName.setText(name);

                    ImageView avatar = row.findViewById(R.id.avatar);
                    String profileUrl = userDoc.getString("profileImageUrl");
                    if (profileUrl != null && !profileUrl.isEmpty()) {
                        com.bumptech.glide.Glide.with(this)
                                .load(profileUrl)
                                .circleCrop()
                                .placeholder(android.R.drawable.sym_def_app_icon)
                                .into(avatar);
                    }

                    // Color name based on status
                    switch (status) {
                        case "accepted":
                            tvName.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            btnAction.setVisibility(View.GONE);
                            break;
                        case "declined":
                            tvName.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                            btnAction.setVisibility(View.VISIBLE);
                            btnAction.setText("Replace");
                            btnAction.setBackgroundTintList(
                                    android.content.res.ColorStateList.valueOf(
                                            android.graphics.Color.parseColor("#7B61FF")));
                            btnAction.setOnClickListener(v -> replaceUser(docId, container, row));
                            break;
                        case "selected":
                            tvName.setTextColor(getResources().getColor(android.R.color.black));
                            btnAction.setVisibility(View.VISIBLE);
                            btnAction.setText("Cancel");
                            btnAction.setBackgroundTintList(
                                    android.content.res.ColorStateList.valueOf(
                                            android.graphics.Color.parseColor("#FF3B30")));
                            btnAction.setOnClickListener(v -> cancelUser(docId, container, row));
                            break;
                        case "cancelled":
                            tvName.setTextColor(getResources().getColor(android.R.color.darker_gray));
                            btnAction.setVisibility(View.VISIBLE);
                            btnAction.setText("Replace");
                            btnAction.setBackgroundTintList(
                                    android.content.res.ColorStateList.valueOf(
                                            android.graphics.Color.parseColor("#7B61FF")));
                            btnAction.setOnClickListener(v -> replaceUser(docId, container, row));
                            break;
                    }

                    container.addView(row);
                })
                .addOnFailureListener(e -> {
                    View row = LayoutInflater.from(this)
                            .inflate(R.layout.item_selected_user, container, false);
                    TextView tvName = row.findViewById(R.id.user_name);
                    tvName.setText(userId);
                    container.addView(row);
                });
    }

    /**
     * Cancel a pending (selected) user's invitation.
     */
    private void cancelUser(String docId, LinearLayout container, View row) {
        db.collection("events").document(eventId)
                .collection("waitingList").document(docId)
                .update("status", "cancelled")
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Invitation revoked", Toast.LENGTH_SHORT).show();
                    loadUsers();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to cancel", Toast.LENGTH_SHORT).show());
    }

    /**
     * Reads all waiting list entries and writes in-app notifications for entrants
     * whose status is "selected" or "rejected" and who have not yet been notified
     * (notified field is not true). Sets notified = true on each waitingList document
     * after notifying, so pressing the button again only picks up new unnotified entrants
     * (e.g. replacement draws).
     */
    private void sendNotifications() {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(eventDoc -> {
                    String displayTitle = eventDoc.getString("title");
                    if (displayTitle == null) displayTitle = (eventName != null) ? eventName : "an event";
                    final String finalTitle = displayTitle;

                    db.collection("events").document(eventId)
                            .collection("waitingList")
                            .get()
                            .addOnSuccessListener(snapshots -> {
                                int[] count = {0};

                                for (QueryDocumentSnapshot doc : snapshots) {
                                    String status = doc.getString("status");
                                    Boolean notified = doc.getBoolean("notified");

                                    // Only notify selected/rejected entrants who haven't been notified yet
                                    if (!"selected".equals(status) && !"rejected".equals(status)) continue;
                                    if (Boolean.TRUE.equals(notified)) continue;

                                    String userId = doc.getId();
                                    String entrantName = doc.getString("name");
                                    String displayName = (entrantName != null && !entrantName.isEmpty())
                                            ? entrantName : "Entrant";

                                    Map<String, Object> notifData = new HashMap<>();
                                    notifData.put("eventId", eventId);
                                    notifData.put("eventName", finalTitle);
                                    notifData.put("read", false);
                                    notifData.put("timestamp", Timestamp.now());

                                    if ("selected".equals(status)) {
                                        notifData.put("notiName", "Lottery Winner!");
                                        notifData.put("description", "Congratulations " + displayName
                                                + "! You have been selected to join " + finalTitle
                                                + ". Please go to the event page to accept or reject your entry.");
                                    } else {
                                        notifData.put("notiName", "Lottery Result");
                                        notifData.put("description", "Unfortunately " + displayName
                                                + ", you were not selected for " + finalTitle + " this time.");
                                    }

                                    // Write notification
                                    db.collection("users").document(userId)
                                            .collection("notifications")
                                            .add(notifData)
                                            .addOnFailureListener(e ->
                                                    Log.e(TAG, "Failed to write notification for: " + userId, e));

                                    // Mark this entrant as notified so re-pressing the button skips them
                                    db.collection("events").document(eventId)
                                            .collection("waitingList").document(userId)
                                            .update("notified", true)
                                            .addOnFailureListener(e ->
                                                    Log.e(TAG, "Failed to set notified for: " + userId, e));

                                    count[0]++;
                                }

                                if (count[0] == 0) {
                                    Toast.makeText(this, "No new entrants to notify", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Notifications sent to " + count[0] + " entrant(s)!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to read waiting list for notifications", e);
                                Toast.makeText(this, "Failed to send notifications.", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to read event document for notifications", e);
                    Toast.makeText(this, "Failed to send notifications.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Remove a declined user from the waiting list, then draw a replacement.
     * The removal always happens. If no replacement is available, that's okay.
     */
    private void replaceUser(String docId, LinearLayout container, View row) {
        db.collection("events").document(eventId)
                .collection("waitingList").document(docId)
                .delete()
                .addOnSuccessListener(v -> {
                    LotteryDrawHelper.drawReplacement(eventId, new LotteryDrawHelper.OnDrawCompleteListener() {
                        @Override
                        public void onSuccess(int count) {
                            if (count > 0) {
                                Toast.makeText(SelectedUsersActivity.this, "Replacement entrant selected!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SelectedUsersActivity.this, "No entrants available for replacement.", Toast.LENGTH_SHORT).show();
                            }
                            loadUsers(); // refresh the list
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Replacement draw failed", e);
                            Toast.makeText(SelectedUsersActivity.this, "Replacement failed.", Toast.LENGTH_SHORT).show();
                            loadUsers(); // still refresh to show the removal
                        }
                    });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(SelectedUsersActivity.this, "Failed to remove user.", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CSV_EXPORT_REQUEST && resultCode == RESULT_OK && data != null) {
            // Fetch all entrants on the waiting list, including their statuses, and export
            db.collection("events").document(eventId).collection("waitingList").get()
                    .addOnSuccessListener(snapshots -> {
                        List<EnrolledEntrant> allEntrants = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            String name = doc.getString("name");
                            if (name == null || name.isEmpty()) name = doc.getId(); // Fallback
                            String email = doc.getString("email");
                            String phone = doc.getString("phone");
                            String enrolmentDate = doc.getString("enrolmentDate");
                            String status = doc.getString("status");

                            if (status == null) status = "waiting";

                            EnrolledEntrant entrant = new EnrolledEntrant(name, email, phone, enrolmentDate);
                            entrant.setStatus(status);
                            allEntrants.add(entrant);
                        }
                        CsvExportHelper.writeCsvToUriWithStatus(this, data.getData(), allEntrants);
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to retrieve users for CSV export.", Toast.LENGTH_SHORT).show());
        }
    }
}