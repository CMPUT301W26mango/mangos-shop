package com.example.myapplication;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
/**
 * The following test file was written with the guidance of Gemini AI
 * Prompt: "Guide me with writing tests for Event" April 2, 2026
 */

/**
 * UI Tests for the Organizer Dashboard.
 * Covers navigation, event list visibility, and the broadcast notification system.
 */
@RunWith(AndroidJUnit4.class)
public class OrganizerDashboardActivityTest {

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * Verifies that the dashboard launches and displays its primary UI components.
     */
    @Test
    public void testDashboardElementsPresence() {
        ActivityScenario.launch(OrganizerDashboardActivity.class);

        onView(withId(R.id.events_recycler_view)).check(matches(isDisplayed()));
        onView(withId(R.id.add_event)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_send_notifications)).check(matches(isDisplayed()));
    }

    /**
     * Tests navigation to the Event Creation screen via the Floating Action Button.
     */
    @Test
    public void testNavigateToCreateEvent() {
        ActivityScenario.launch(OrganizerDashboardActivity.class);

        onView(withId(R.id.add_event)).perform(click());
        intended(hasComponent(EventCreateActivity.class.getName()));
    }

    /**
     * Tests navigation to the User Profile screen.
     */
    @Test
    public void testNavigateToProfile() {
        ActivityScenario.launch(OrganizerDashboardActivity.class);

        onView(withId(R.id.nav_profile)).perform(click());
        intended(hasComponent(UserProfileActivity.class.getName()));
    }

    /**
     * Verifies that the Broadcast Dialog opens and contains the necessary fields.
     */
    @Test
    public void testBroadcastDialogFlow() {
        ActivityScenario.launch(OrganizerDashboardActivity.class);

        // Click the send notifications button
        onView(withId(R.id.btn_send_notifications)).perform(click());

        // Wait a moment for Firestore to fetch events and show the dialog
        try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }

        // Verify Dialog Title
        onView(withText("Global Broadcast")).check(matches(isDisplayed()));

        // Verify we can see the selection labels
        onView(withText("Select Event:")).check(matches(isDisplayed()));
        onView(withText("\nSelect Target Group:")).check(matches(isDisplayed()));

        // Test typing a message in the EditText
        onView(withHint("\nType your message here..."))
                .perform(typeText("Test broadcast message"), closeSoftKeyboard());

        // Click Cancel to close the dialog
        onView(withText("Cancel")).perform(click());
    }
}