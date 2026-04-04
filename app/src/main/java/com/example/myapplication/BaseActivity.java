package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

/**
 * A foundational Activity class intended to be extended by other activities in the application.
 * It provides global functionalities, such as real-time monitoring of user role changes
 * in Firebase Firestore, ensuring consistent security and navigation state across the app.
 */
public class BaseActivity extends AppCompatActivity {

    protected FirebaseFirestore db;
    private ListenerRegistration globalRoleListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        // Start listening the second ANY screen opens
        startGlobalRoleListener();
    }

    /**
     * Initializes a global SnapshotListener to monitor the user's Firestore profile document.
     * If the 'isAdmin' flag or 'role' field updates to Admin, the application seamlessly
     * redirects the user to the Admin Dashboard, regardless of their current active screen.
     */
    private void startGlobalRoleListener() {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        globalRoleListener = db.collection("users").document(deviceId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        return;
                    }

                    Boolean isAdmin = snapshot.getBoolean("isAdmin");
                    String currentRole = snapshot.getString("role");

                    if (Boolean.TRUE.equals(isAdmin) || "Admin".equals(currentRole)) {

                        // Prevent infinite loops if they are ALREADY on the Admin screen
                        if (this.getClass().getSimpleName().equals("AdminBrowseEventsActivity")) {
                            return;
                        }

                        if (!"Admin".equals(currentRole)) {
                            db.collection("users").document(deviceId).update("role", "Admin");
                        }

                        Toast.makeText(this, "Admin Access Granted!", Toast.LENGTH_LONG).show();

                        // Kick them to the Admin Dashboard
                        Intent intent = new Intent(this, AdminBrowseEventsActivity.class);
                        // Clear the backstack so they can't hit "Back" to escape admin mode
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    /**
     * Safely removes the Firestore SnapshotListener when the Activity is destroyed
     * to prevent memory leaks and background application crashes.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up the listener when the screen closes so the app doesn't crash
        if (globalRoleListener != null) {
            globalRoleListener.remove();
        }
    }
}