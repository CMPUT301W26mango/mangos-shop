package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
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
    private static ListenerRegistration globalRoleListener;
    protected static boolean isSelfDeleting = false;
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

        if (globalRoleListener != null) {
            return;
        }

        globalRoleListener = db.collection("users").document(deviceId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        android.util.Log.e("BaseActivity", "Listen failed.", error);
                        return;
                    }

                    if (snapshot == null || !snapshot.exists()) {

                        String currentActivity = this.getClass().getSimpleName();
                        if (currentActivity.equals("MainActivity") || currentActivity.equals("CreateAccount")) {
                            return;
                        }

                        if (!isSelfDeleting) {
                            Toast.makeText(this, "Your account has been removed by an administrator.", Toast.LENGTH_LONG).show();
                        }

                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        return; // Stop here because the user document is gone
                    }

                    Boolean isAdmin = snapshot.getBoolean("isAdmin");
                    String currentRole = snapshot.getString("role");

                    SharedPreferences prefs = getSharedPreferences("ROLE_PREF", MODE_PRIVATE);
                    String overrideRole = prefs.getString("currentRole", null);

                    if (overrideRole != null) {
                        return;
                    }

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

    protected void setupBottomNavigation(String role) {
        android.view.View navEntrant = findViewById(R.id.bottom_nav_entrant_container);
        android.view.View navOrganizer = findViewById(R.id.bottom_nav_organizer_container);

        // If this screen doesn't have the include tags, just exit
        if (navEntrant == null || navOrganizer == null) return;

        if ("Organizer".equalsIgnoreCase(role)) {
            // Show my Organizer bar, hide my Entrant bar
            navOrganizer.setVisibility(android.view.View.VISIBLE);
            navEntrant.setVisibility(android.view.View.GONE);

            // Wire up my custom Organizer buttons
            findViewById(R.id.my_events).setOnClickListener(v -> startActivity(new android.content.Intent(this, OrganizerDashboardActivity.class)));
            findViewById(R.id.nav_profile_organizer).setOnClickListener(v -> startActivity(new android.content.Intent(this, UserProfileActivity.class)));

        } else {
            // Show my Entrant bar, hide my Organizer bar
            navEntrant.setVisibility(android.view.View.VISIBLE);
            navOrganizer.setVisibility(android.view.View.GONE);

            // Wire up my custom Entrant buttons
            findViewById(R.id.nav_history).setOnClickListener(v -> startActivity(new android.content.Intent(this, MyEventsActivity.class)));
            findViewById(R.id.nav_events).setOnClickListener(v -> startActivity(new android.content.Intent(this, EventListActivity.class)));
            findViewById(R.id.nav_notifications).setOnClickListener(v -> startActivity(new android.content.Intent(this, NotificationsActivity.class)));
            findViewById(R.id.nav_profile_entrant).setOnClickListener(v -> startActivity(new android.content.Intent(this, UserProfileActivity.class)));
        }
    }

    // Global sweep to delete user and all hosted events
    protected void deleteAccountAndEvents() {
        isSelfDeleting = true;

        if (globalRoleListener != null) {
            globalRoleListener.remove();
            globalRoleListener = null;
        }

        String deviceId = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        // Find all events where this user is the host
        db.collection("events").whereEqualTo("deviceId", deviceId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (com.google.firebase.firestore.QueryDocumentSnapshot eventDoc : queryDocumentSnapshots) {
                        String eventId = eventDoc.getId();
                        String eventName = eventDoc.getString("title");

                        // Notify waitlisted people before deleting
                        db.collection("events").document(eventId).collection("waitingList").get()
                                .addOnSuccessListener(waitlistSnaps -> {
                                    for (com.google.firebase.firestore.QueryDocumentSnapshot entrantDoc : waitlistSnaps) {
                                        String entrantId = entrantDoc.getId();

                                        java.util.Map<String, Object> notif = new java.util.HashMap<>();
                                        notif.put("eventName", eventName);
                                        notif.put("description", "The organizer deleted their account. Event cancelled.");
                                        notif.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

                                        db.collection("users").document(entrantId).collection("notifications").add(notif);
                                        entrantDoc.getReference().delete();
                                    }
                                    eventDoc.getReference().delete();
                                });
                    }

                    // Delete the actual user profile last
                    db.collection("users").document(deviceId).delete()
                            .addOnSuccessListener(aVoid -> {
                                android.widget.Toast.makeText(this, "Your Account Has Been Deleted", android.widget.Toast.LENGTH_SHORT).show();
                                android.content.Intent intent = new android.content.Intent(this, MainActivity.class);
                                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            });
                });
    }

}