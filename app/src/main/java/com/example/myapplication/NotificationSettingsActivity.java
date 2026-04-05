package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Activity responsible for managing the user's global notification preferences.
 * This class provides a toggle interface that instantly syncs the user's opt-in/opt-out
 * status with their profile document in Firestore.
 */
public class NotificationSettingsActivity extends BaseActivity {

    private SwitchMaterial toggleNotifications;
    private FirebaseFirestore db;
    private String deviceId;

    /**
     * Initializes the activity, sets the content view, and configures the UI components.
     * Retrieves the device ID and sets up a listener to push toggle state changes
     * directly to the database.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     * being shut down, this contains the most recent data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        android.widget.ImageButton backButton = findViewById(R.id.btn_back);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        db = FirebaseFirestore.getInstance();
        Profiles profiles = new Profiles();
        deviceId = profiles.getDeviceId(this);

        toggleNotifications = findViewById(R.id.toggleNotifications);

        // Load their current preference from Firebase
        loadCurrentSetting();

        // Listen for clicks to update Firebase instantly
        toggleNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateSettingInDatabase(isChecked);
        });
    }

    /**
     * Retrieves the user's current notification preference from Firestore.
     * Updates the visual state of the SwitchMaterial to reflect the stored data.
     */
    private void loadCurrentSetting() {
        db.collection("users").document(deviceId).get().addOnSuccessListener(doc -> {
            if (doc.exists() && doc.contains("notificationsEnabled")) {
                Boolean isEnabled = doc.getBoolean("notificationsEnabled");
                if (isEnabled != null) {
                    toggleNotifications.setChecked(isEnabled);
                }
            }
        }).addOnFailureListener(e -> Log.e("NotifSettings", "Failed to load setting", e));
    }

    /**
     * Pushes the new notification preference to the user's Firestore document.
     * Displays a success toast upon completion, or safely reverts the visual toggle
     * state if the database transaction fails.
     *
     * @param isEnabled The new boolean state of the notification setting.
     */
    private void updateSettingInDatabase(boolean isEnabled) {
        db.collection("users").document(deviceId)
                .update("notificationsEnabled", isEnabled)
                .addOnSuccessListener(aVoid -> {
                    String status = isEnabled ? "Enabled" : "Disabled";
                    Toast.makeText(this, "Notifications " + status, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update setting", Toast.LENGTH_SHORT).show();
                    // Revert the toggle visually if the database fails
                    toggleNotifications.setChecked(!isEnabled);
                });
    }
}