package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration; // testing for now if this works, for admin change

/**
 * Dashboard activity for users with the "Entrant" role.
 * Handles viewing and updating profile information, loading specific fragments
 * (like the event list), and deleting the user account.
 */

public class EntrantAccount extends AppCompatActivity {
    private EditText userName, userEmail, userPhone;
    private Profiles profiles;
    private String deviceId;
    private FirebaseFirestore db;
    private ListenerRegistration roleChecker;

    /**
     *
     * Initializes the activity
     * Sets up Firebase references
     * Checks incoming intents to see if a specific fragment (like the Event List) should be loaded immediately.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_account);

        profiles = new Profiles();
        deviceId = profiles.getDeviceId(this);
        db = FirebaseFirestore.getInstance();

        roleCheckerListener();
    }


    //https://gotodba.com/2015/03/09/listener-registration-explained/
    //https://firebase.google.com/docs/firestore/query-data/listen
    /**
     * Real time updater with Firestore
     * If the admin boolean goes to true in the database, this will auto make them admin(send them to the page, without having to reload the app)
     */
    private void roleCheckerListener() {
        //https://gotodba.com/2015/03/09/listener-registration-explained/
        //https://firebase.google.com/docs/firestore/query-data/listen
        roleChecker = db.collection("users").document(deviceId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        return;
                    }

                    Boolean isAdmin = snapshot.getBoolean("isAdmin");
                    String currentRole = snapshot.getString("role");

                    if (Boolean.TRUE.equals(isAdmin) || "Admin".equals(currentRole)) {
                        if (!"Admin".equals(currentRole)) {
                            db.collection("users").document(deviceId).update("role", "Admin");
                        }

                        Toast.makeText(EntrantAccount.this, "Admin Access Granted!", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(EntrantAccount.this, AdminBrowseEventsActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    /**
     * An auto storage cleaner/memory cleaner
     * For any memory leaks(just in case).
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (roleChecker != null) {
            roleChecker.remove(); // according to google this is a good practice
        }
    }

    /**
     * gets the entrant's current profile data from Firestore using their deviceID.
     */
    private void loadEntrantData() {
        db.collection("users").document(deviceId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Entrant entrant = doc.toObject(Entrant.class);
                if (entrant != null) {
                    userName.setText(entrant.getName() != null ? entrant.getName() : "");
                    userEmail.setText(entrant.getEmail() != null ? entrant.getEmail() : "");
                    userPhone.setText(entrant.getPhone() != null ? entrant.getPhone() : "");
                }
            }
        });
    }


    /**
     * US 01.02.02 - Update the profile
     *
     * Validates the input
     * Saves the updated Entrant info back to Firestore.
     */
    private void updateProfile() {
        String name = userName.getText().toString().trim();
        String email = userEmail.getText().toString().trim();
        String phone = userPhone.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Name and Email required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            userEmail.setError("Please enter a valid email address");
            userEmail.requestFocus(); // Pops the keyboard back open on this field
            return;
        }

        Entrant updatedEntrant = new Entrant(name, email, phone);
        db.collection("users").document(deviceId).set(updatedEntrant).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Displays a warning dialog to the user before permanently deleting their profile.
     * If confirmed, it removes their data from Firestore and redirects them to the Create Account screen
     */
    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("This will remove your data and waiting list entries. Continue?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    profiles.deleteProfile(deviceId, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Profile Deleted", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, MainActivity.class);
                            //this should be working/
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}