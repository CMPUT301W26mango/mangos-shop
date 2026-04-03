package com.example.myapplication;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasType;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * The following test file was written with the guidance of Gemini AI
 * Prompt: "Guide me with writing tests for Event creation page" April 2, 2026
 */

/**
 * Enhanced UI Tests for EventCreateActivity.
 * Handles both Creation and Edit modes with proper scroll support.
 */
@RunWith(AndroidJUnit4.class)
public class EventCreateActivityUITest {


    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Rule
    public ActivityScenarioRule<EventCreateActivity> activityRule =
            new ActivityScenarioRule<>(EventCreateActivity.class);

    /**
     * Verifies that the activity displays the correct title and
     * essential fields when launched normally (Create Mode).
     */
    @Test
    public void testUIElementsPresence() {
        // FIXED: Matching the actual text "Create Your Event" found in your logs
        onView(withId(R.id.create_event_title))
                .check(matches(withText("Create Your Event")));

        onView(withId(R.id.event_name_input)).check(matches(isDisplayed()));
        onView(withId(R.id.location_input)).check(matches(isDisplayed()));

        // Use scrollTo for elements near the bottom of the form
        onView(withId(R.id.create_event_button))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    /**
     * Tests that typing into input fields correctly updates the UI.
     */
    @Test
    public void testFormInput() {
        onView(withId(R.id.event_name_input))
                .perform(typeText("Soccer Finals"), closeSoftKeyboard());
        onView(withId(R.id.event_name_input))
                .check(matches(withText("Soccer Finals")));

        onView(withId(R.id.capacity_input))
                .perform(scrollTo(), typeText("50"), closeSoftKeyboard());
        onView(withId(R.id.capacity_input))
                .check(matches(withText("50")));
    }

    /**
     * Verifies toggle logic for Geolocation and Private switches.
     */
    @Test
    public void testSwitches() {
        // Test Geolocation Toggle
        onView(withId(R.id.switchGeolocation)).perform(scrollTo());
        onView(withId(R.id.switchGeolocation)).check(matches(isNotChecked()));
        onView(withId(R.id.switchGeolocation)).perform(click());
        onView(withId(R.id.switchGeolocation)).check(matches(isChecked()));

        // Test Private Event Toggle
        onView(withId(R.id.switch_private_event)).perform(scrollTo());
        onView(withId(R.id.switch_private_event)).check(matches(isNotChecked()));
        onView(withId(R.id.switch_private_event)).perform(click());
        onView(withId(R.id.switch_private_event)).check(matches(isChecked()));
    }

    /**
     * Tests "EDIT" mode to ensure the UI updates title and button text
     * based on Intent extras.
     */
    @Test
    public void testEditModeUI() {
        // Manually launch with "EDIT" mode intent
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, EventCreateActivity.class);
        intent.putExtra("MODE", "EDIT");
        intent.putExtra("EVENT_ID", "mock_id_123");

        try (ActivityScenario<EventCreateActivity> scenario = ActivityScenario.launch(intent)) {
            // Title should now be "Edit Your Event"
            onView(withId(R.id.create_event_title))
                    .check(matches(withText("Edit Your Event")));

            // Button should now be "Update"
            onView(withId(R.id.create_event_button))
                    .perform(scrollTo())
                    .check(matches(withText("Update")));
        }
    }

    /**
     * Upload button should be there with proper text
     */
    @Test
    public void testUploadButtonInitialState() {
        onView(withId(R.id.upload_poster_button))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
                .check(matches(withText("Upload Poster Image")));
    }


    /**
     * Poster preview image should be hidden by default before any image is selected
     */
    @Test
    public void testPosterPreview_hiddenByDefault() {
        onView(withId(R.id.poster_image_preview))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /**
     * Upload poster button should be clickable
     */
    @Test
    public void testUploadPosterButton_isClickable() {
        onView(withId(R.id.upload_poster_button))
                .perform(scrollTo())
                .check(matches(isClickable()));
    }

    /**
     * Create button should be visible before any upload is started
     */
    @Test
    public void testCreateButton_visibleBeforeUpload() {
        onView(withId(R.id.create_event_button))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }
}