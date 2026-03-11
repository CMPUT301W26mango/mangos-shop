package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainActivity extends AppCompatActivity {
    private EditText userName, userEmail, userPhone;
    private String selectedRole = "";
    private Profiles profiles;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        profiles = new Profiles();
        deviceId = profiles.getDeviceId(this);

        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        userPhone = findViewById(R.id.userPhone);

        profiles.fetchUserRole(deviceId, user -> {
            if (user != null) redirectUser(user);
        });

        findViewById(R.id.btnRoleOrganizer).setOnClickListener(v -> selectedRole = "Organizer");
        findViewById(R.id.btnRoleEntrant).setOnClickListener(v -> selectedRole = "Entrant");
        findViewById(R.id.saveButton).setOnClickListener(v -> saveAndRedirect());

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new EventListFragment())
                    .commit();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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

        UserProfiles user;
        if ("Organizer".equals(selectedRole)) user = new Organizer(name, email, phone);
        else if ("Admin".equals(selectedRole)) user = new Admin(name, email, phone);
        else user = new Entrant(name, email, phone);

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