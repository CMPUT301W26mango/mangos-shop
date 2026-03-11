package com.example.myapplication;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * US 02.02.03 — Enable or disable geolocation requirement for an event.
 *
 * Allows the organizer to toggle a boolean field (geolocationRequired)
 * on their event document in Firestore. That's all this does.
 *
 * The entrant-side code (US 01.01.01) reads this boolean when someone
 * tries to join the waiting list and handles location capture if true.
 *
 * The map view (US 02.02.02) reads the stored coordinates to display
 * entrant locations to the organizer.
 *
 * HOW TO LAUNCH (for teammates during merge):
 *     Intent intent = new Intent(context, GeolocationSettingActivity.class);
 *     intent.putExtra("eventId", "your_event_id_here");
 *     startActivity(intent);
 *
 * ASSUMED FIRESTORE STRUCTURE:
 *     events/{eventId}
 *         - organizerDeviceId: String
 *         - geolocationRequired: Boolean
 */
public class GeolocationSettingActivity extends AppCompatActivity {

    private static final String TAG = "GeolocationSetting";

    private FirebaseFirestore db;

    private Switch switchGeolocation;
    private TextView tvGeoStatus;

    private String eventId;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geolocation_setting);

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
        switchGeolocation = findViewById(R.id.switchGeolocation);
        tvGeoStatus = findViewById(R.id.tvGeoStatus);

        // --- Disable switch until we load current value ---
        switchGeolocation.setEnabled(false);

        // --- Check test mode ---
        boolean testMode = getIntent().getBooleanExtra("testMode", false);

        if (testMode) {
            // Test mode — skip Firestore, but set up UI so toggle tests work
            switchGeolocation.setEnabled(true);
            updateStatusText(false);
            switchGeolocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateStatusText(isChecked);
            });
            return;
        }

        // --- Verify ownership, then load current setting ---
        verifyOwnershipAndLoad();
    }

    /**
     * Checks that the current device owns this event before allowing changes.
     * Then loads the current geolocationRequired value from Firestore.
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

                    // Ownership confirmed — load current geolocation setting
                    Boolean geoRequired = documentSnapshot.getBoolean("geolocationRequired");
                    boolean isEnabled = (geoRequired != null) ? geoRequired : false;

                    switchGeolocation.setChecked(isEnabled);
                    switchGeolocation.setEnabled(true);
                    updateStatusText(isEnabled);
                    setupToggleListener();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load event", e);
                    Toast.makeText(this, "Error loading event.", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    /**
     * Sets up the switch toggle listener.
     * When the organizer flips the switch, it immediately writes
     * the new value to Firestore.
     */
    private void setupToggleListener() {
        switchGeolocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateStatusText(isChecked);
            saveGeolocationSetting(isChecked);
        });
    }

    /**
     * Updates the status text below the switch to reflect the current state.
     */
    private void updateStatusText(boolean isEnabled) {
        if (isEnabled) {
            tvGeoStatus.setText("Geolocation is currently ENABLED for this event.\nEntrants must share their location when joining.");
        } else {
            tvGeoStatus.setText("Geolocation is currently DISABLED for this event.\nNo location data will be collected.");
        }
    }

    /**
     * Writes the geolocationRequired boolean to the event document in Firestore.
     * This is the core of US 02.02.03 — criteria #4.
     */
    private void saveGeolocationSetting(boolean isEnabled) {
        db.collection("events").document(eventId)
                .update("geolocationRequired", isEnabled)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Geolocation " + (isEnabled ? "enabled" : "disabled") + ".", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update geolocation setting", e);
                    Toast.makeText(this, "Error saving setting.", Toast.LENGTH_SHORT).show();
                    // Revert the switch since save failed
                    switchGeolocation.setChecked(!isEnabled);
                    updateStatusText(!isEnabled);
                });
    }
}