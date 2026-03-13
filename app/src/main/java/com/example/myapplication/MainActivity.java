package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

/**
 * Starting point and redirecter for all activity for the app.
 * Checks the deviceID and sees if user is new or already exists.
 * Sends user to Welcome Back screen or the Create Account screen (depending on deviceID).
 */

public class MainActivity extends AppCompatActivity {
    private Profiles profiles;
    private String deviceId;

    /**
     * Initializes the application, connects to Firebase, and fetches the user's role.
     * If the user exists, they are redirected to WelcomeBack.
     * If they do not exist, they are sent to CreateAccount to register.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */

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

                String nextActivity = EventListActivity.class.getName();
                if (user instanceof Admin) nextActivity = AdminBrowseEventsActivity.class.getName();
                else if (user instanceof Organizer) nextActivity = EventCreateActivity.class.getName();
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