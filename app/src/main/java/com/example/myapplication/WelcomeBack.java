package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * A welcome back screen for all users who have previously created an account
 * Displays the Users Name, and a message telling them Welcome Back
 * Has a tap anywhere on screen feature which when done, redirects the user to their role based page
 * (eg, Entrants to Events page)
 */
public class WelcomeBack extends AppCompatActivity {
    /**
     *
     * Initializes the activity, retrieves the user's name and where they will be sent
     * Sets up the welcome text and the screen tap listener.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_back);

        TextView welcomeText = findViewById(R.id.welcomeText);

        String userName = getIntent().getStringExtra("USER_NAME");
        String nextActivityClassName = getIntent().getStringExtra("NEXT_ACTIVITY");

        //no need ot check since Name can't be empty (already checked elsewhere)
        welcomeText.setText("Welcome Back,\n" + userName + "!");

        findViewById(R.id.rootLayout).setOnClickListener(v -> {
            Intent intent;
            try {
                Class<?> nextClass = Class.forName(nextActivityClassName);

                if (nextClass == EntrantAccount.class) {
                    intent = new Intent(WelcomeBack.this, EventListActivity.class);
                } else {
                    intent = new Intent(WelcomeBack.this, nextClass);
                }
            } catch (Exception e) {
                intent = new Intent(WelcomeBack.this, EventListActivity.class);
            }

            startActivity(intent);
            finish();
        });
    }
}