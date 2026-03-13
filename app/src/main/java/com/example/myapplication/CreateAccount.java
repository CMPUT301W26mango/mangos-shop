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

/**
 * Activity responsible for handling new user registration.
 * Allows the user to input their name, email, phone, and select a role
 * (Entrant, Organizer, or Admin). It then saves this data to Firestore.
 */

public class CreateAccount extends AppCompatActivity {
    private EditText userName, userEmail, userPhone;
    private String selectedRole = "";
    private Profiles profiles;
    private String deviceId;
    private Button entrantButton, organizerButton, adminButton;

    /**
     * Initalizes activity
     * sets up all button on-click listeners (roles and login)
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */

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

        //quick quality of life work (for coloru toggle)
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

    /**
     * Updates the visual state of the role buttons.
     * The selected role button is darkened to indicate it is active,
     * while the others are reset to the default color.
     */
    private void updateRoleUI() {
        String defaultMango = "#E5B35C";  //like mango shop
        String darkerPressedMango = "#B8862D";

        // if one is selected make others the normal (default)
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

    /**
     * Checks the user's input
     * Creates the UserProfiles object based on the what role they choose
     * Saves it to Firestore
     */
    private void saveAndRedirect() {
        String name = userName.getText().toString().trim();
        String email = userEmail.getText().toString().trim();
        String phone = userPhone.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Email required", Toast.LENGTH_SHORT).show();
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

    /**
     * Redirects the user to their appropriate dashboard based on their profile type.
     * If the user is an Entrant, sends to event lists (almost like home page).
     *
     * @param user User profile
     */
    private void redirectUser(UserProfiles user) {
        Intent intent;
        if (user instanceof Admin) {
            intent = new Intent(this, AdminBrowseEventsActivity.class);
        } else if (user instanceof Organizer) {
            intent = new Intent(this, EventCreateActivity.class);
        } else {
            // The default fallback ensures 'intent' is ALWAYS initialized
            intent = new Intent(this, EventListActivity.class);
        }
        startActivity(intent);
        finish();
    }
}