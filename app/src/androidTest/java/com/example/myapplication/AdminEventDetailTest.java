package com.example.myapplication;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

/**
 * Intent tests for AdminEventDetailActivity.
 * Verifies event details display, delete actions, and dialog interactions.
 */
@RunWith(AndroidJUnit4.class)
public class AdminEventDetailTest {

    /**
     * Launch activity with required intent extras.
     */
    @Rule
    public ActivityTestRule<AdminEventDetailActivity> rule =
            new ActivityTestRule<AdminEventDetailActivity>(AdminEventDetailActivity.class, true, false) {
                @Override
                protected Intent getActivityIntent() {
                    Intent intent = new Intent(ApplicationProvider.getApplicationContext(), AdminEventDetailActivity.class);

                    // Provide required data so activity doesn't crash
                    intent.putExtra("eventId", "testEvent123");
                    intent.putExtra("title", "Test Event");
                    intent.putExtra("location", "Edmonton");
                    intent.putExtra("organizer", "Admin");
                    intent.putExtra("posterURL", ""); // empty to avoid Firebase Storage issues

                    return intent;
                }
            };

    /**
     * Test that event details are displayed correctly.
     */
    @Test
    public void testEventDetailsDisplayed() {
        rule.launchActivity(null);

        onView(withId(R.id.textEventTitle))
                .check(matches(isDisplayed()));

        onView(withId(R.id.textEventLocation))
                .check(matches(isDisplayed()));

        onView(withId(R.id.textEventOrganizer))
                .check(matches(isDisplayed()));
    }

    /**
     * Test clicking delete event button and confirming dialog appears.
     */
    @Test
    public void testDeleteEventDialogAppears() {
        rule.launchActivity(null);

        onView(withId(R.id.btn_delete_event))
                .perform(click());

        onView(withText("Delete Event"))
                .check(matches(isDisplayed()));
    }

    /**
     * Test canceling delete event dialog.
     */
    @Test
    public void testCancelDeleteEvent() {
        rule.launchActivity(null);

        onView(withId(R.id.btn_delete_event))
                .perform(click());

        onView(withText("Cancel"))
                .perform(click());
    }

    /**
     * Test remove image button behavior when no image exists.
     */
    @Test
    public void testRemoveImageNoImage() {
        rule.launchActivity(null);

        onView(withId(R.id.buttonRemoveImage))
                .perform(click());
    }
}