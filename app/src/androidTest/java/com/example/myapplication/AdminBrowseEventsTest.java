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
 * Intent tests for AdminBrowseEventsActivity.
 * Verifies browsing, navigation, and interaction with event list.
 */
@RunWith(AndroidJUnit4.class)
public class AdminBrowseEventsTest {

    @Rule
    public ActivityTestRule<AdminBrowseEventsActivity> rule =
            new ActivityTestRule<>(AdminBrowseEventsActivity.class);

    /**
     * Helper method to pause execution to allow async Firebase data to load.
     */
    private void waitFor(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test that the RecyclerView displaying events is visible.
     */
    @Test
    public void testRecyclerViewDisplayed() {
        onView(withId(R.id.recyclerViewAdminEvents))
                .check(matches(isDisplayed()));
    }

    /**
     * Test clicking on an event item in the RecyclerView.
     * This should trigger the event detail dialog.
     */
    @Test
    public void testClickEventItem() {

        waitFor(2000);

        onView(withId(R.id.recyclerViewAdminEvents))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
    }

    /**
     * Test clicking the comments button inside a RecyclerView item.
     * This should launch the CommentActivity.
     */
    @Test
    public void testClickCommentsButton() {

        waitFor(2000);

        onView(withId(R.id.recyclerViewAdminEvents))
                .perform(RecyclerViewActions.actionOnItemAtPosition(
                        0,
                        clickChildViewWithId(R.id.btn_view_comments)
                ));
    }

    /**
     * Test navigation to Admin Logs screen using bottom navigation.
     */
    @Test
    public void testNavigateToLogs() {
        onView(withId(R.id.nav_admin_logs))
                .perform(click());
    }

    /**
     * Helper method to click a child view inside a RecyclerView item.
     */
    public static ViewAction clickChildViewWithId(int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Click on child view with id " + id;
            }

            @Override
            public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                if (v != null) {
                    v.performClick();
                }
            }
        };
    }
}