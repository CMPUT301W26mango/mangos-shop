package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class CreateAccount extends AppCompatActivity {
    private EditText userName, userEmail, userPhone;
    private String selectedRole = "";
    private Profiles profiles;
    private String deviceId;

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

        findViewById(R.id.btnRoleAdmin).setOnClickListener(v -> selectedRole = "Admin");
        findViewById(R.id.btnRoleOrganizer).setOnClickListener(v -> selectedRole = "Organizer");
        findViewById(R.id.btnRoleEntrant).setOnClickListener(v -> selectedRole = "Entrant");
        findViewById(R.id.saveButton).setOnClickListener(v -> saveAndRedirect());
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
        Intent intent;
        if (user instanceof Admin) intent = new Intent(this, AdminAccount.class);
        else if (user instanceof Organizer) intent = new Intent(this, OrganizerAccount.class);
        else intent = new Intent(this, EntrantAccount.class);

        startActivity(intent);
        finish();
    }
}