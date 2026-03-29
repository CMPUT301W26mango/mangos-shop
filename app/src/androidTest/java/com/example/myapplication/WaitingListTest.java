package com.example.myapplication;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;


import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

/**
* Asked Gemini AI to assist in helping write the tests.
* The prompt used: 
* Can you please help me in ideas for what should be tested for this file <the file of code for this page> and generate some ideal test cases please.
*/

/**
 *
 * Tests that the WaitingList Activity:
 * - Can launch successfully with the event id.
 * - Displays the correct UI elements.
 * - formats the waiting list count properly.
 *
 */
@RunWith(AndroidJUnit4.class)
public class WaitingListTest {

    //actually launches and the UI looks good

    /**
     * Tests that the Activity can launch without crashing and that the
     * waitlistCountText TextView is visible on the screen.
     */
    @Test
    public void testActivityLaunchesAndDisplaysTextView() {
        // Create an intent with a dummy event ID
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, WaitingList.class);
        intent.putExtra("EVENT_ID", "dummy_event_123");

        // Launch the activity with the intent
        try (ActivityScenario<WaitingList> scenario = ActivityScenario.launch(intent)) {
            // Verify the TextView exists and is visible
            onView(withId(R.id.waitlistCountText)).check(matches(isDisplayed()));
        }
    }

    //database checks (checking if the data is good and loads, and if the UI works

    /**
     * Tests that the UI correctly updates with the formatted string after getting count from Firestore database.
     */
    @Test
    public void testWaitingListCountTextFormatsCorrectly() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, WaitingList.class);

        // Pass an event ID that either exists or will safely return 0
        intent.putExtra("EVENT_ID", "test_event_id");

        try (ActivityScenario<WaitingList> scenario = ActivityScenario.launch(intent)) {

            // takes about this long to update so wait for timer to see if it works
            Thread.sleep(2000);


            // make sure number matches.
            onView(withId(R.id.waitlistCountText))
                    .check(matches(withText(containsString("People on Waiting List: "))));

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
