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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class AdminProfileDetailActivity extends AppCompatActivity {

    private TextView textProfileName;
    private TextView textProfileEmail;
    private TextView textProfileRole;

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile_detail);

        textProfileName = findViewById(R.id.textProfileName);
        textProfileEmail = findViewById(R.id.textProfileEmail);
        textProfileRole = findViewById(R.id.textProfileRole);

        db = FirebaseFirestore.getInstance();
        userId = getIntent().getStringExtra("userId");

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Missing user ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProfileDetails();
    }

    private void loadProfileDetails() {
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