package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Button;
import android.graphics.Color;
import android.content.res.ColorStateList;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class CreateAccount extends AppCompatActivity {
    private EditText userName, userEmail, userPhone;
    private String selectedRole = "";
    private Profiles profiles;
    private String deviceId;
    private Button entrantButton, organizerButton, adminButton;

    //moved it from main to here
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account);

        profiles = new Profiles();
        deviceId = profiles.getDeviceId(this);

        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        userPhone = findViewById(R.id.userPhone);

        entrantButton = findViewById(R.id.btnRoleEntrant);
        organizerButton = findViewById(R.id.btnRoleOrganizer);
        adminButton = findViewById(R.id.btnRoleAdmin);

        //quick quality of life work
        entrantButton.setOnClickListener(v -> {
            selectedRole = "Entrant";
            updateRoleUI();
        });

        organizerButton.setOnClickListener(v -> {
            selectedRole = "Organizer";
            updateRoleUI();
        });

        adminButton.setOnClickListener(v -> {
            selectedRole = "Admin";
            updateRoleUI();
        });

        findViewById(R.id.saveButton).setOnClickListener(v -> saveAndRedirect());
    }

    private void updateRoleUI() {
        String defaultMango = "#E5B35C";
        String darkerPressedMango = "#B8862D";

        // if one is selected make others the normal
        entrantButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(defaultMango)));
        organizerButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(defaultMango)));
        adminButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(defaultMango)));

        // Darken only the selected button
        if ("Entrant".equals(selectedRole)) {
            entrantButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(darkerPressedMango)));
        } else if ("Organizer".equals(selectedRole)) {
            organizerButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(darkerPressedMango)));
        } else if ("Admin".equals(selectedRole)) {
            adminButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(darkerPressedMango)));
        }
    }

    private void saveAndRedirect() {
        String name = userName.getText().toString().trim();
        String email = userEmail.getText().toString().trim();
        String phone = userPhone.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Name and Email required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            userEmail.setError("Please enter a valid email address");
            userEmail.requestFocus();
            return;
        }

        if (selectedRole.isEmpty()) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }

        UserProfiles user;
        if ("Organizer".equals(selectedRole)) {
            user = new Organizer(name, email, phone);
        } else if ("Admin".equals(selectedRole)) {
            user = new Entrant(name, email, phone);
            user.setAdminRequested(true);
            Toast.makeText(this, "Admin requested. Defaulting to Entrant pending approval.", Toast.LENGTH_LONG).show();
        } else {
            user = new Entrant(name, email, phone);
        }

        FirebaseFirestore.getInstance().collection("users").document(deviceId).set(user)
                .addOnSuccessListener(aVoid -> redirectUser(user));
    }

    private void redirectUser(UserProfiles user) {
        if (user instanceof Admin) {
            Intent intent = new Intent(this, AdminAccount.class);
            startActivity(intent);
        } else if (user instanceof Organizer) {
            Intent intent = new Intent(this, OrganizerAccount.class);
            startActivity(intent);
        } else {

            Intent intent = new Intent(this, EntrantAccount.class);
            intent.putExtra("loadFragment", "eventList");
            startActivity(intent);
        }
        finish();
    }
}