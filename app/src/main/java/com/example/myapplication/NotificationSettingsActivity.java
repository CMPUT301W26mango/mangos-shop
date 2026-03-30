package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.FirebaseFirestore;

public class NotificationSettingsActivity extends AppCompatActivity {

    private SwitchMaterial toggleNotifications;
    private FirebaseFirestore db;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

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