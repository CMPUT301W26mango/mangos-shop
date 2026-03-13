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

        // Ensure the user document exists first so the update doesn't fail
        Profiles profiles = new Profiles();
        String deviceId = profiles.getDeviceId(context);

        FirebaseFirestore.getInstance().collection("users").document(deviceId)
                .set(new java.util.HashMap<String, Object>() {{
                    put("isAdmin", false);
                    put("role", "Entrant");
                }}, com.google.firebase.firestore.SetOptions.merge());

        // Launch the activity
        Intent intent = new Intent(context, EntrantAccount.class);
        try (ActivityScenario<EntrantAccount> scenario = ActivityScenario.launch(intent)) {

            // Verify start on Entrant screen
            onView(withId(R.id.main)).check(matches(isDisplayed()));

            // Trigger the Admin change
            FirebaseFirestore.getInstance().collection("users").document(deviceId)
                    .update("isAdmin", true, "role", "Admin");

            // 5 seconds to fire the intent
            long startTime = System.currentTimeMillis();
            boolean success = false;
            while (System.currentTimeMillis() - startTime < 5000) {
                try {
                    intended(hasComponent(AdminBrowseEventsActivity.class.getName()));
                    success = true;
                    break;
                } catch (AssertionError e) {
                    // give more time
                    Thread.sleep(500);
                }
            }

            if (!success) {
                // standard error
                intended(hasComponent(AdminBrowseEventsActivity.class.getName()));
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}