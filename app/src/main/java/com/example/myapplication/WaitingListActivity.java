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

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * US 02.02.01 - View the list of entrants who joined the waiting list.
 * US 02.05.02 - Lazy-check safety net: triggers lottery draw if deadline passed and draw not yet done.
 * US 02.06.05 - Export entrants as CSV.
 *
 * Displays a real-time list of all entrant names on the waiting list for a
 * specific event. Only the organizer who owns the event (matched by device ID)
 * can view this screen. The lottery draw itself runs automatically in the
 * background via LotteryDrawWorker (PeriodicWorkRequest). This screen only
 * acts as a safety net via the lazy check in verifyOwnershipAndLoad().
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

        tvTitle.setText("Waiting List: " + eventName);

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

        // --- Verify ownership, then lazy-check draw, then load waiting list ---
        boolean testMode = getIntent().getBooleanExtra("testMode", false);
        if (testMode) {
            return;
        }
        verifyOwnershipAndLoad();
    }

    /**
     * Handles the result from the CSV file picker.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CSV_EXPORT_REQUEST && resultCode == RESULT_OK && data != null) {
            CsvExportHelper.writeCsvToUri(this, data.getData(), entrants);
        }
    }

    /**
     * Verifies that the current device owns this event, then:
     *   1. Runs the lazy draw check (US 02.05.02 safety net).
     *   2. Attaches the real-time waiting list listener.
     */
    private void verifyOwnershipAndLoad() {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // Ownership check — field "deviceId" matches EventStore.addEvent()
                    String ownerDeviceId = documentSnapshot.getString("deviceId");
                    if (ownerDeviceId == null || !ownerDeviceId.equals(deviceId)) {
                        Toast.makeText(this, "Access denied: you are not the organizer of this event.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // US 02.05.02 lazy check — safety net if WorkManager missed this event.
                    // "regEnd" field name matches EventStore.addEvent() and EventDetailsFragment.
                    // "drawCompleted" is set by LotteryDrawHelper.performDraw().
                    runLazyDrawCheck(documentSnapshot);

                    attachWaitingListListener();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to verify event ownership", e);
                    Toast.makeText(this, "Error loading event.", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    /**
     * Lazy draw check (US 02.05.02 safety net).
     *
     * If the registration deadline has passed AND the draw has not been marked
     * complete, trigger the draw immediately. This covers gaps between the
     * periodic WorkManager runs (e.g., device was offline, app was reinstalled).
     *
     * @param eventDoc already-loaded event document snapshot
     */
    private void runLazyDrawCheck(DocumentSnapshot eventDoc) {
        Boolean drawCompleted = eventDoc.getBoolean("drawCompleted");
        if (Boolean.TRUE.equals(drawCompleted)) {
            return; // draw already done — nothing to do
        }

        Timestamp regEnd = eventDoc.getTimestamp("regEnd");
        if (regEnd == null) {
            return; // no deadline set — skip
        }

        if (regEnd.compareTo(Timestamp.now()) < 0) {
            // Deadline has passed and draw hasn't run yet — trigger it now
            Log.d(TAG, "Lazy check: triggering draw for event " + eventId);
            LotteryDrawHelper.performDraw(eventId, new LotteryDrawHelper.OnDrawCompleteListener() {
                @Override
                public void onSuccess(int selectedCount) {
                    Log.d(TAG, "Lazy draw complete: " + selectedCount + " entrants selected.");
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Lazy draw failed for event: " + eventId, e);
                }
            });
        }
    }

    /**
     * Attaches a real-time snapshot listener to the waitingList subcollection.
     * Reads name, email, phone, enrolmentDate for display and CSV export.
     * Status is NOT read or displayed here — the lottery screen handles that.
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