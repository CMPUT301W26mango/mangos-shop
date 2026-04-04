/**
 * Activity that displays detailed information for a selected user profile.
 *
 * Role in application:
 * - Admin inspection screen for user information.
 *
 * Outstanding issues:
 * - Profile detail currently supports viewing only and does not include delete actions as it's not
 * part of the halfway checkpoint.
 */

package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.provider.Settings;

import com.google.firebase.firestore.FirebaseFirestore;


public class AdminProfileDetailActivity extends AppCompatActivity {

    private TextView textProfileName;
    private TextView textProfileEmail;
    private TextView textProfileRole;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile_detail);

        textProfileName = findViewById(R.id.textProfileName);
        textProfileEmail = findViewById(R.id.textProfileEmail);
        textProfileRole = findViewById(R.id.textProfileRole);

        Button deleteButton = findViewById(R.id.btn_delete_profile);
        Button removeOrganizerButton = findViewById(R.id.btn_remove_organizer);

        final String userId = getIntent().getStringExtra("userId");
        String currentDeviceId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Missing user ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        removeOrganizerButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Remove Organizer")
                    .setMessage("Remove organizer privileges for this user?")
                    .setPositiveButton("Yes", (dialog, which) -> {

                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        db.collection("users")
                                .document(userId)
                                .update("role", "Entrant")
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Organizer removed", Toast.LENGTH_SHORT).show();
                                    textProfileRole.setText("Role: Entrant");
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error updating role", Toast.LENGTH_SHORT).show();
                                });

                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Profile")
                    .setMessage("Are you sure you want to delete this user?")
                    .setPositiveButton("Yes", (dialog, which) -> {

                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        db.collection("users")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(document -> {

                                    String deviceId = document.getString("deviceId");

                                    db.collection("users")
                                            .document(userId)
                                            .delete()
                                            .addOnSuccessListener(aVoid -> {

                                                Toast.makeText(this, "Profile deleted", Toast.LENGTH_SHORT).show();

                                                if (deviceId != null && deviceId.equals(currentDeviceId)) {

                                                    Intent intent = new Intent(AdminProfileDetailActivity.this, MainActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);

                                                } else {
                                                    finish();
                                                }

                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(this, "Error deleting profile", Toast.LENGTH_SHORT).show();
                                            });

                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error deleting profile", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });


        db = FirebaseFirestore.getInstance();
        loadProfileDetails(userId);
    }

    private void loadProfileDetails(String userId) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    String name = document.getString("name");
                    String email = document.getString("email");
                    String role = document.getString("role");
                    Button removeOrganizerButton = findViewById(R.id.btn_remove_organizer);

                    if (role != null && role.equals("Organizer")) {
                        removeOrganizerButton.setVisibility(View.VISIBLE);
                    } else {
                        removeOrganizerButton.setVisibility(View.GONE);
                    }

                    if (name == null || name.isEmpty()) {
                        name = "Unnamed User";
                    }
                    if (email == null || email.isEmpty()) {
                        email = "No email";
                    }
                    if (role == null || role.isEmpty()) {
                        role = "Unknown role";
                    }

                    textProfileName.setText(name);
                    textProfileEmail.setText("Email: " + email);
                    textProfileRole.setText("Role: " + role);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
                );
    }
}