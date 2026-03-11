package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class EntrantAccount extends AppCompatActivity {
    private EditText userName, userEmail, userPhone;
    private Profiles profiles;
    private String deviceId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_account);

        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        userPhone = findViewById(R.id.userPhone);

        profiles = new Profiles();
        deviceId = profiles.getDeviceId(this);
        db = FirebaseFirestore.getInstance();

        loadEntrantData();

        findViewById(R.id.updateButton).setOnClickListener(v -> updateProfile());
        findViewById(R.id.deleteButton).setOnClickListener(v -> showDeleteConfirmation());

        // event checker
        findViewById(R.id.viewEventButton).setOnClickListener(v -> {
            Intent intent = new Intent(this, WaitingList.class);
            intent.putExtra("EVENT_ID", "sample_event_123");
            startActivity(intent);
        });
    }

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

    // US 01.02.02 - Update the progile
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

    // delte (wont work when testing try again)
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