package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {
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

        // Auto-login
        profiles.fetchUserRole(deviceId, user -> {
            if (user != null) {
                // User exists go tr0 Welcome Back screen
                Intent intent = new Intent(MainActivity.this, WelcomeBack.class);
                intent.putExtra("USER_NAME", user.getName());

                String nextActivity = EntrantAccount.class.getName();
                if (user instanceof Admin) nextActivity = AdminAccount.class.getName();
                else if (user instanceof Organizer) nextActivity = OrganizerAccount.class.getName();

                intent.putExtra("NEXT_ACTIVITY", nextActivity);
                startActivity(intent);
            } else {
                // User dne go to Create Account screen
                Intent intent = new Intent(MainActivity.this, CreateAccount.class);
                startActivity(intent);
            }
            finish(); // Close MainActivity
        });
    }
}