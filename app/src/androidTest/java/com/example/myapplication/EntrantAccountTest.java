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

import androidx.test.espresso.intent.Intents;
import org.junit.After;
import org.junit.Before;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import com.google.firebase.firestore.FirebaseFirestore;

//import com.google.android.gms.tasks.Task;
//import com.google.android.gms.tasks.Tasks;
//
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.TimeoutException;

/**
 * Tests for the EntrantAccount Activity
 *
 * Tests that the EntrantAccount Activity:
 * Can launch successfully on its own.
 * Successfully intercepts the "loadFragment" intent to display the Event List.
 * Successfully navigates to AdminBrowseEventsActivity when role changes.
 */
@RunWith(AndroidJUnit4.class)
public class EntrantAccountTest {

    /**
     * Track before test runs
     */
    @Before
    public void setUp() {
        Intents.init();

        // 1. Get the device ID and Firestore instance
        Context context = ApplicationProvider.getApplicationContext();
        Profiles profiles = new Profiles();
        String deviceId = profiles.getDeviceId(context);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 2. Force the database back to a normal Entrant BEFORE the test starts
        db.collection("users").document(deviceId).update("isAdmin", false, "role", "Entrant");

        // 3. Pause for 1 second to give the Firebase cloud time to sync the change
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop tracking after test also stops.
     */
    @After
    public void endOfTest() {
        Intents.release();
    }

    /**
     * Tests that the Activity can launch without crashing under normal circumstances.
     * Checks if the main layout container is visible.
     */
    @Test
    public void testActivityLaunchesNormally() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, EntrantAccount.class);

        // Launch the activity
        try (ActivityScenario<EntrantAccount> scenario = ActivityScenario.launch(intent)) {
            // Check that the fragment container (which holds the layout) is displayed
            onView(withId(R.id.main)).check(matches(isDisplayed()));
        }
    }

    /**
     * Tests that when the "loadFragment" intent is passed with "eventList",
     * the activity correctly processes it.
     */
    @Test
    public void testActivityLoadsFragmentFromIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, EntrantAccount.class);

        intent.putExtra("loadFragment", "eventList");

        try (ActivityScenario<EntrantAccount> scenario = ActivityScenario.launch(intent)) {
            // Verify the fragment container is visible, meaning the transaction didn't crash
            onView(withId(R.id.main)).check(matches(isDisplayed()));
        }
    }

    /**
     * Tests that the snapshot listener successfully fires an intent to open the
     * AdminBrowseEventsActivity when triggered.
     */
    @Test
    public void testIfAdminAutoNavigate() {
        Context context = ApplicationProvider.getApplicationContext();

        // Ensure the user document exists first by "merging" a default state
        Profiles profiles = new Profiles();
        String deviceId = profiles.getDeviceId(context);

        // Use set with merge to create the doc if it's missing, rather than failing on update
        FirebaseFirestore.getInstance().collection("users").document(deviceId)
                .set(new java.util.HashMap<String, Object>() {{
                    put("isAdmin", false);
                    put("role", "Entrant");
                }}, com.google.firebase.firestore.SetOptions.merge());

        // Wait a moment for the initial creation to sync before launching the activity
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(context, EntrantAccount.class);
        try (ActivityScenario<EntrantAccount> scenario = ActivityScenario.launch(intent)) {

            // Verify start on Entrant screen
            onView(withId(R.id.main)).check(matches(isDisplayed()));

            // Flip the database switch to Admin
            FirebaseFirestore.getInstance().collection("users").document(deviceId)
                    .update("isAdmin", true, "role", "Admin");

            // Wait for Firestore listener to trigger (using a loop to avoid freezing Espresso permanently)
            long startTime = System.currentTimeMillis();
            boolean intentFired = false;

            while (System.currentTimeMillis() - startTime < 5000) { // 5-second timeout
                try {
                    intended(hasComponent(AdminBrowseEventsActivity.class.getName()));
                    intentFired = true;
                    break; // It passed! Break the loop early.
                } catch (AssertionError e) {
                    // Intent hasn't fired yet, wait 500ms and check again
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }

            // If the loop finished and it never fired, do one final intended() call to trigger the standard failure message
            if (!intentFired) {
                intended(hasComponent(AdminBrowseEventsActivity.class.getName()));
            }

        }
    }
}