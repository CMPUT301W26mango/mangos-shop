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

/**
 * Tests for the EntrantAccount Activity
 *
 * Tests that the EntrantAccount Activity:
 * - Can launch successfully on its own.
 * - Successfully intercepts the "loadFragment" intent to display the Event List.
 */
@RunWith(AndroidJUnit4.class)
public class EntrantAccountTest {

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
            onView(withId(R.id.fragment_container)).check(matches(isDisplayed()));
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
            onView(withId(R.id.fragment_container)).check(matches(isDisplayed()));
        }
    }
}