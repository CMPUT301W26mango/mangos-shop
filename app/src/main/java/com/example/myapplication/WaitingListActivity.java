package com.example.myapplication;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * US 02.02.01 — View the list of entrants who joined the waiting list.
 *
 * Displays a real-time list of all entrants on the waiting list for a
 * specific event. Only the organizer who owns the event (matched by
 * device ID) can view it.
 *
 * HOW TO LAUNCH (for teammates during merge):
 *     Intent intent = new Intent(context, WaitingListActivity.class);
 *     intent.putExtra("eventId", "your_event_id_here");
 *     startActivity(intent);
 *
 * ASSUMED FIRESTORE STRUCTURE:
 *     events/{eventId}
 *         - organizerDeviceId: String
 *
 *     events/{eventId}/waitingList/{entrantId}
 *         - name: String
 */
public class WaitingListActivity extends AppCompatActivity {

    private static final String TAG = "WaitingListActivity";

    private FirebaseFirestore db;
    private ListenerRegistration waitingListListener;

    private TextView tvTitle;
    private TextView tvEntrantCount;
    private RecyclerView rvEntrants;

    private List<String> entrantNames;
    private WaitingListAdapter adapter;

    private String eventId;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_list);

        // --- Get event ID from intent ---
        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null) {
            Toast.makeText(this, "Error: No event ID provided.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // --- Get this device's ID (used for ownership check) ---
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // --- Initialize Firestore ---
        db = FirebaseFirestore.getInstance();

        // --- Bind UI ---
        tvTitle = findViewById(R.id.tvWaitingListTitle);
        tvEntrantCount = findViewById(R.id.tvEntrantCount);
        rvEntrants = findViewById(R.id.rvEntrants);

        // --- Setup RecyclerView ---
        entrantNames = new ArrayList<>();
        adapter = new WaitingListAdapter(entrantNames);
        rvEntrants.setLayoutManager(new LinearLayoutManager(this));
        rvEntrants.setAdapter(adapter);

        // --- Verify ownership, then load waiting list ---
        // Test mode: skip Firestore check so UI tests can verify layout
        // without needing real Firestore data. In production, this extra
        // is never set, so ownership is always verified.
        boolean testMode = getIntent().getBooleanExtra("testMode", false);
        boolean demoMode = getIntent().getBooleanExtra("demoMode", false);
        if (demoMode) {
            // Demo mode — load dummy data for manual visual testing
            // Remove this before merging to main branch
            entrantNames.add("Oakley");
            entrantNames.add("Santan Dave");
            entrantNames.add("Julia");
            entrantNames.add("Chris");
            tvEntrantCount.setText("Total Entrants: " + entrantNames.size());
            adapter.notifyDataSetChanged();
            return;
        }
        if (testMode) {
            // Skip ownership check — used only for automated UI tests
            return;
        }
        verifyOwnershipAndLoad();
    }

    /**
     * Checks that the current device owns this event before showing data.
     * This satisfies acceptance criteria #5: organizers can only see
     * waiting lists for their own events.
     */
    private void verifyOwnershipAndLoad() {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    String ownerDeviceId = documentSnapshot.getString("organizerDeviceId");

                    if (ownerDeviceId == null || !ownerDeviceId.equals(deviceId)) {
                        Toast.makeText(this, "Access denied: you are not the organizer of this event.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // Ownership confirmed — start listening to waiting list
                    attachWaitingListListener();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to verify event ownership", e);
                    Toast.makeText(this, "Error loading event.", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    /**
     * Attaches a real-time snapshot listener to the waitingList subcollection.
     * This satisfies acceptance criteria #4: list updates when entrants
     * join or leave.
     */
    private void attachWaitingListListener() {
        waitingListListener = db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .addSnapshotListener((QuerySnapshot snapshots, com.google.firebase.firestore.FirebaseFirestoreException error) -> {

                    if (error != null) {
                        Log.e(TAG, "Error listening to waiting list", error);
                        return;
                    }

                    if (snapshots == null) {
                        return;
                    }

                    // Clear and rebuild the list from the snapshot
                    entrantNames.clear();

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String name = doc.getString("name");
                        if (name != null) {
                            entrantNames.add(name);
                        }
                    }

                    // Update count display (acceptance criteria #3)
                    tvEntrantCount.setText("Total Entrants: " + entrantNames.size());

                    // Refresh the list
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up the listener to avoid memory leaks
        if (waitingListListener != null) {
            waitingListListener.remove();
        }
    }
}