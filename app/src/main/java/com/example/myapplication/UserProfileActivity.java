package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfileActivity extends AppCompatActivity {

    private TextView tvProfileName;
    private TextView tvProfileRole;
    private FirebaseFirestore db;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // init views
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileRole = findViewById(R.id.tvProfileRole);


        // setup firebase and grab the device id
        db = FirebaseFirestore.getInstance();
        Profiles profiles = new Profiles();
        deviceId = profiles.getDeviceId(this);

        // populate the profile screen
        loadUserData();

        // menu click listeners
        findViewById(R.id.rowEditProfile).setOnClickListener(v -> {
            startActivity(new Intent(UserProfileActivity.this, EntrantAccount.class));
        });

        findViewById(R.id.rowNotifications).setOnClickListener(v -> {
            startActivity(new Intent(UserProfileActivity.this, NotificationSettingsActivity.class));
        });

        findViewById(R.id.rowDeleteAccount).setOnClickListener(v -> {
            // routing to EntrantAccount since the delete logic is already built there
            startActivity(new Intent(UserProfileActivity.this, EntrantAccount.class));
        });
    }

    private void loadUserData() {
        db.collection("users").document(deviceId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                // update name if it exists, otherwise default to Guest
                if (doc.contains("name")) {
                    tvProfileName.setText(doc.getString("name"));
                } else {
                    tvProfileName.setText("Guest");
                }

                // update role
                if (doc.contains("role")) {
                    tvProfileRole.setText(doc.getString("role"));
                }
            }
        }).addOnFailureListener(e -> Log.e("UserProfile", "Failed to load user data", e));
    }
}