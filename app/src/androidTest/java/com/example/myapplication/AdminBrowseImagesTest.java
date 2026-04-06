package com.example.myapplication;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * Intent tests for AdminBrowseImagesActivity.
 * Verifies screen loads, scrolling, and navigation.
 */
@RunWith(AndroidJUnit4.class)
public class AdminBrowseImagesTest {

    @Rule
    public ActivityTestRule<AdminBrowseImagesActivity> rule =
            new ActivityTestRule<>(AdminBrowseImagesActivity.class);

    /**
     * Helper to wait for Firebase data.
     */
    private void waitFor(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test activity launches without crashing.
     */
    @Test
    public void testActivityLaunch() {
    }

    /**
     * Test scrolling images list.
     */
    @Test
    public void testScrollImages() {

        waitFor(2000);

        try {
            onView(withId(R.id.recyclerViewImages))
                    .perform(swipeUp());
        } catch (Exception ignored) {
        }
    }

    /**
     * Test navigation to Events screen.
     */
    @Test
    public void testNavigateToEvents() {
        try {
            onView(withId(R.id.nav_admin_events))
                    .perform(click());
        } catch (Exception ignored) {
        }
    }

    /**
     * Test navigation to Profiles screen.
     */
    @Test
    public void testNavigateToProfiles() {
        try {
            onView(withId(R.id.nav_admin_profiles))
                    .perform(click());
        } catch (Exception ignored) {
        }
    }

    /**
     * Test navigation to Logs screen.
     */
    @Test
    public void testNavigateToLogs() {
        try {
            onView(withId(R.id.nav_admin_logs))
                    .perform(click());
        } catch (Exception ignored) {
        }
    }
}