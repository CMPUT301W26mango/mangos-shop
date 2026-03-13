package com.example.myapplication;

import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * US 02.02.03 - Enable or disable geolocation requirement for an event.
 *
 * Utility class - not a separate screen. The geolocation toggle lives
 * on the Event Settings screen (built by another teammate). This class
 * provides methods they can call to wire up the toggle.
 *
 */
public class GeolocationHelper {

    private static final String TAG = "GeolocationHelper";

    /**
     * Loads the current geolocation setting from Firestore and sets the switch.
     * If the field doesn't exist yet, defaults to false (OFF).
     *
     * Criteria #4: Setting is stored with the event in Firebase.
     *
     * @param activity the Activity context
     * @param eventId  the event document ID
     * @param geoSwitch the Switch widget to update
     */
    public static void loadSetting(AppCompatActivity activity, String eventId, Switch geoSwitch) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Disable switch until value is loaded
        geoSwitch.setEnabled(false);

        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Log.e(TAG, "Event not found: " + eventId);
                        return;
                    }

                    Boolean geoRequired = documentSnapshot.getBoolean("geolocationRequired");
                    boolean isEnabled = (geoRequired != null) ? geoRequired : false;

                    geoSwitch.setChecked(isEnabled);
                    geoSwitch.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load geolocation setting", e);
                    Toast.makeText(activity, "Error loading geolocation setting.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Sets up the switch to write to Firestore whenever it is toggled.
     * If the write fails, the switch reverts to its previous state.
     *
     * Criteria #1: Toggle for geolocation requirement on event form.
     * Criteria #4: Setting is stored with the event in Firebase.
     *
     * @param activity the Activity context
     * @param eventId  the event document ID
     * @param geoSwitch the Switch widget to listen to
     */
    public static void setupToggle(AppCompatActivity activity, String eventId, Switch geoSwitch) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        geoSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            db.collection("events").document(eventId)
                    .update("geolocationRequired", isChecked)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(activity,
                                "Geolocation " + (isChecked ? "enabled" : "disabled") + ".",
                                Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to update geolocation setting", e);
                        Toast.makeText(activity, "Error saving setting.", Toast.LENGTH_SHORT).show();
                        // Revert the switch since save failed
                        geoSwitch.setChecked(!isChecked);
                    });
        });
    }
}
