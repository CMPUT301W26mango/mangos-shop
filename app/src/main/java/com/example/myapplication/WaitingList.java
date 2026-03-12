package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Responsible for:
 * displaying the current number of entrants on the waiting list for a specific event.
 */

public class WaitingList extends AppCompatActivity {
    /**
     * Fetches the waiting list count from the database using the event ID
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
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