package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class SelectedUsersActivity extends AppCompatActivity {

    private static final String TAG = "SelectedUsersActivity";

    private FirebaseFirestore db;
    private String eventId;

    private LinearLayout containerAccepted;
    private LinearLayout containerDeclined;
    private LinearLayout containerPending;
    private LinearLayout containerCancelled;

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

        String eventName = getIntent().getStringExtra("eventName"); // Grab the name passed from Event Details

        Button btnMessageSelected = findViewById(R.id.btnMessageSelected);
        btnMessageSelected.setOnClickListener(v -> {
            AnnouncementHelper.showAnnouncementDialog(this, eventId, eventName, "selected", "Selected");
        });

        Button btnMessageCancelled = findViewById(R.id.btnMessageCancelled);
        btnMessageCancelled.setOnClickListener(v -> {
            AnnouncementHelper.showAnnouncementDialog(this, eventId, eventName, "cancelled", "Cancelled");
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
                        String userId = doc.getString("userId");
                        String status = doc.getString("status");
                        if (status == null) status = "waiting";

                        switch (status) {
                            case "accepted":
                                addUserRow(containerAccepted, userId, "accepted", doc.getId());
                                break;
                            case "rejected":
                                addUserRow(containerDeclined, userId, "rejected", doc.getId());
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

                    // Color name based on status
                    switch (status) {
                        case "accepted":
                            tvName.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            btnAction.setVisibility(View.GONE);
                            break;
                        case "rejected":
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
                    // to userId if user doc not found
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
}