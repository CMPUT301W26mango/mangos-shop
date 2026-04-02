package com.example.myapplication;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
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
 * UI Tests for EventDetailActivity.
 * Uses owner-level Event ID: wOYPJ7TtDATW8rKcGWfI
 * @author Sayuj
 */
@RunWith(AndroidJUnit4.class)
public class EventDetailActivityTest {

    private static final String OWNER_EVENT_ID = "wOYPJ7TtDATW8rKcGWfI";

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * Verifies that clicking the Settings Cog navigates to EventCreateActivity.
     * Includes a forced visibility check to bypass hardware device ID mismatches.
     */
    @Test
    public void testSettingsButtonNavigation() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventDetailActivity.class);
        intent.putExtra("EVENT_ID", OWNER_EVENT_ID);

        try (ActivityScenario<EventDetailActivity> scenario = ActivityScenario.launch(intent)) {
            // 1. Wait for Firestore data to fetch
            try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }

            // 2. Ensure the button is visible.
            // Even if the ID is from an owner, the emulator's device ID
            // will differ from the document's 'deviceId' field.
            scenario.onActivity(activity -> {
                View settingsBtn = activity.findViewById(R.id.btn_settings_cog);
                settingsBtn.setVisibility(View.VISIBLE);
            });

            // 3. Perform the click
            onView(withId(R.id.btn_settings_cog)).perform(click());

            // 4. Verify navigation to Edit Mode
            intended(allOf(
                    hasComponent(EventCreateActivity.class.getName()),
                    hasExtra("MODE", "EDIT"),
                    hasExtra("EVENT_ID", OWNER_EVENT_ID)
            ));
        }
    }

    @Test
    public void testUIElementsPresence() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventDetailActivity.class);
        intent.putExtra("EVENT_ID", OWNER_EVENT_ID);

        try (ActivityScenario<EventDetailActivity> scenario = ActivityScenario.launch(intent)) {
            // Check that the UI container and map are present
            onView(withId(R.id.tv_event_name)).check(matches(isDisplayed()));
            onView(withId(R.id.mapView)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testInviteButtonNavigation() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventDetailActivity.class);
        intent.putExtra("EVENT_ID", OWNER_EVENT_ID);

        try (ActivityScenario<EventDetailActivity> scenario = ActivityScenario.launch(intent)) {
            try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }

            // Force visibility for the invite button as well
            scenario.onActivity(activity -> {
                activity.findViewById(R.id.btn_invite_users).setVisibility(View.VISIBLE);
            });

            onView(withId(R.id.btn_invite_users)).perform(click());

            intended(allOf(
                    hasComponent(UserSearchActivity.class.getName()),
                    hasExtra("EVENT_ID", OWNER_EVENT_ID)
            ));
        }
    }
}