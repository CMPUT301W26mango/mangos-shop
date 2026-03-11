package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeBack extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_back);

        TextView welcomeText = findViewById(R.id.welcomeText);

        String userName = getIntent().getStringExtra("USER_NAME");
        String nextActivityClassName = getIntent().getStringExtra("NEXT_ACTIVITY");

        if (userName != null && !userName.isEmpty()) {
            welcomeText.setText("Welcome Back,\n" + userName + "!");
        } else {
            welcomeText.setText("Welcome Back!");
        }

        findViewById(R.id.rootLayout).setOnClickListener(v -> {
            Intent intent;
            try {
                Class<?> nextClass = Class.forName(nextActivityClassName);
                intent = new Intent(WelcomeBack.this, nextClass);
            } catch (Exception e) {
                intent = new Intent(WelcomeBack.this, EntrantAccount.class);
            }
            startActivity(intent);
            finish();
        });
    }
}