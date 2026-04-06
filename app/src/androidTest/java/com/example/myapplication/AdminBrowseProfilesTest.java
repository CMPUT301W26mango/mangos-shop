package com.example.myapplication;

import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

/**
 * Intent tests for AdminBrowseProfilesActivity.
 * Verifies browsing, navigation, and interaction with profile list.
 */
@RunWith(AndroidJUnit4.class)
public class AdminBrowseProfilesTest {

    @Rule
    public ActivityTestRule<AdminBrowseProfilesActivity> rule =
            new ActivityTestRule<>(AdminBrowseProfilesActivity.class);

    /**
     * Helper method to wait for Firebase data to load.
     */
    private void waitFor(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test that RecyclerView displaying profiles is visible.
     */
    @Test
    public void testRecyclerViewDisplayed() {
        onView(withId(R.id.recyclerViewAdminProfiles))
                .check(matches(isDisplayed()));
    }

    /**
     * Test clicking on a profile item.
     * This should open the profile detail dialog.
     */
    @Test
    public void testClickProfileItem() {

        waitFor(2000);

        onView(withId(R.id.recyclerViewAdminProfiles))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
    }

    /**
     * Test navigation to Admin Events screen.
     */
    @Test
    public void testNavigateToEvents() {
        onView(withId(R.id.nav_admin_events))
                .perform(click());
    }

    /**
     * Test navigation to Admin Logs screen.
     */
    @Test
    public void testNavigateToLogs() {
        onView(withId(R.id.nav_admin_logs))
                .perform(click());
    }

    /**
     * Test navigation to Admin Images screen.
     */
    @Test
    public void testNavigateToImages() {
        onView(withId(R.id.nav_admin_images))
                .perform(click());
    }
}