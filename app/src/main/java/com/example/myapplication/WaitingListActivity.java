package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
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
 * US 02.06.05 — Export entrants as CSV.
 *
 * Displays a real-time list of all entrants on the waiting list for a
 * specific event. Only the organizer who owns the event (matched by
 * device ID) can view it. Includes a CSV export button.
 *
 * HOW TO LAUNCH (for teammates during merge):
 *     Intent intent = new Intent(context, WaitingListActivity.class);
 *     intent.putExtra("eventId", "your_event_id_here");
 *     intent.putExtra("eventName", "Swimming Lessons");
 *     startActivity(intent);
 *
 * ASSUMED FIRESTORE STRUCTURE:
 *     events/{eventId}
 *         - organizerDeviceId: String
 *
 *     events/{eventId}/waitingList/{entrantId}
 *         - name: String
 *         - email: String
 *         - phone: String (optional, may be null)
 *         - enrolmentDate: String
 */
public class WaitingListActivity extends AppCompatActivity {

    private static final String TAG = "WaitingListActivity";
    private static final int CSV_EXPORT_REQUEST = 1001;

    private FirebaseFirestore db;
    private ListenerRegistration waitingListListener;

    private TextView tvTitle;
    private TextView tvEntrantCount;
    private RecyclerView rvEntrants;
    private Button btnExportCsv;

    private List<EnrolledEntrant> entrants;
    private WaitingListAdapter adapter;

    private String eventId;
    private String eventName;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_list);

        // --- Get event ID and name from intent ---
        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null) {
            Toast.makeText(this, "Error: No event ID provided.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        eventName = getIntent().getStringExtra("eventName");
        if (eventName == null) {
            eventName = "Event";
        }

        // --- Get this device's ID (used for ownership check) ---
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // --- Initialize Firestore ---
        db = FirebaseFirestore.getInstance();

        // --- Bind UI ---
        tvTitle = findViewById(R.id.tvWaitingListTitle);
        tvEntrantCount = findViewById(R.id.tvEntrantCount);
        rvEntrants = findViewById(R.id.rvEntrants);
        btnExportCsv = findViewById(R.id.btnExportCsv);

        // --- Setup RecyclerView ---
        entrants = new ArrayList<>();
        adapter = new WaitingListAdapter(entrants);
        rvEntrants.setLayoutManager(new LinearLayoutManager(this));
        rvEntrants.setAdapter(adapter);

        // --- Setup CSV export button (US 02.06.05) ---
        btnExportCsv.setOnClickListener(v -> {
            Intent intent = CsvExportHelper.createExportIntent(eventName);
            startActivityForResult(intent, CSV_EXPORT_REQUEST);
        });

        // --- Verify ownership, then load waiting list ---
        boolean testMode = getIntent().getBooleanExtra("testMode", false);
        if (testMode) {
            return;
        }
        verifyOwnershipAndLoad();
    }

    /**
     * Handles the result from the CSV file picker.
     * When the user picks a save location, writes the CSV there.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CSV_EXPORT_REQUEST && resultCode == RESULT_OK && data != null) {
            CsvExportHelper.writeCsvToUri(this, data.getData(), entrants);
        }
    }

    /**
     * Checks that the current device owns this event before showing data.
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
     * Reads all four fields: name, email, phone, enrolmentDate.
     * UI shows name only; all fields are used for CSV export.
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

                    entrants.clear();

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String name = doc.getString("name");
                        if (name != null) {
                            String email = doc.getString("email");
                            String phone = doc.getString("phone");
                            String enrolmentDate = doc.getString("enrolmentDate");
                            entrants.add(new EnrolledEntrant(name, email, phone, enrolmentDate));
                        }
                    }

                    tvEntrantCount.setText("Total Entrants: " + entrants.size());
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (waitingListListener != null) {
            waitingListListener.remove();
        }
    }
}