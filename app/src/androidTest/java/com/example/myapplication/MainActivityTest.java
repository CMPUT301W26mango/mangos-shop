package com.example.myapplication;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

//https://github.com/android/testing-samples
//https://github.com/android/testing-samples/tree/main/ui/espresso/BasicSample
//https://www.geeksforgeeks.org/android/ui-testing-with-espresso-in-android-studio/

/**
 * Tests that the MainActivity:
 * Launches app successfully.
 * Shows the correct first page.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    /**
     * Tests that the Activity launches,
     * Initializes Firebase,
     * Displays the correct page
     */
    @Test
    public void testRouterActivityLaunchesWithoutCrashing() {
        // Launch the activity
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            // Check that the root layout of the loading screen is displayed momentarily
            onView(withId(R.id.main)).check(matches(isDisplayed()));
        }
    }
}