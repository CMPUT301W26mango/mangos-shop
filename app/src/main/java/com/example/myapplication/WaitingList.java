package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class WaitingList extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.events_waitlist);

        TextView waitlistCountText = findViewById(R.id.waitlistCountText);
        String eventId = getIntent().getStringExtra("EVENT_ID");

        Profiles profiles = new Profiles();

        // US 01.05.04 - Display waiting list count
        profiles.getWaitingListCount(eventId, count -> {
            waitlistCountText.setText("People on Waiting List: " + count);
        });
    }
}