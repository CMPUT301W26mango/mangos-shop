package com.example.myapplication;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;

import android.app.Activity;
import android.app.Instrumentation;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * The following test file was written with the guidance of Claude AI
 * Prompt: "Guide me with writing UI tests for EventCreateActivity" March 27, 2026
 */

@RunWith(AndroidJUnit4.class)
public class EventDetailActivityTest {

    private static final String TEST_EVENT_ID = "event_abc_123";

    // Helper to launch EventDetailActivity with a given EVENT_ID extra
    private ActivityScenario<EventDetailActivity> launchWithEventId(String eventId) {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                EventDetailActivity.class
        );
        intent.putExtra("EVENT_ID", eventId);
        return ActivityScenario.launch(intent);
    }

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    // -------------------------------------------------------------------------
    // Settings / Edit intent tests
    // Uses intending() to intercept the launch so EventCreateActivity never
    // starts — avoids the Firestore initialisation crash in that activity.
    // -------------------------------------------------------------------------

    /**
     * Clicking the settings cog should fire an intent to EventCreateActivity
     * with MODE=EDIT and the correct EVENT_ID.
     */
    @Test
    public void settingsCog_launchesEditActivity_withCorrectExtras() {
        // Intercept and stub out the launch before EventCreateActivity starts
        intending(hasComponent(EventCreateActivity.class.getName()))
                .respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));

        try (ActivityScenario<EventDetailActivity> ignored = launchWithEventId(TEST_EVENT_ID)) {

            onView(withId(R.id.btn_settings_cog)).perform(click());

            intended(allOf(
                    hasComponent(EventCreateActivity.class.getName()),
                    hasExtra("MODE", "EDIT"),
                    hasExtra("EVENT_ID", TEST_EVENT_ID)
            ));
        }
    }

    /**
     * The EVENT_ID forwarded must match exactly what was received —
     * verifies the ID isn't hardcoded or mutated.
     */
    @Test
    public void settingsCog_forwardsEventId_notHardcoded() {
        String differentId = "event_xyz_999";

        intending(hasComponent(EventCreateActivity.class.getName()))
                .respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));

        try (ActivityScenario<EventDetailActivity> ignored = launchWithEventId(differentId)) {

            onView(withId(R.id.btn_settings_cog)).perform(click());

            intended(hasExtra("EVENT_ID", differentId));
        }
    }

    // -------------------------------------------------------------------------
    // QR / Share dialog tests
    // -------------------------------------------------------------------------

    /**
     * Clicking the share button should display the QR dialog with the correct title.
     */
    @Test
    public void shareButton_opensQrDialog() {
        try (ActivityScenario<EventDetailActivity> ignored = launchWithEventId(TEST_EVENT_ID)) {

            onView(withId(R.id.btn_share_qr)).perform(click());

            onView(withText("Event QR Code"))
                    .inRoot(isDialog())
                    .check(matches(isDisplayed()));
        }
    }

    /**
     * The QR dialog's Close button should dismiss the dialog without crashing.
     */
    @Test
    public void qrDialog_closeButton_dismissesDialog() {
        try (ActivityScenario<EventDetailActivity> ignored = launchWithEventId(TEST_EVENT_ID)) {

            onView(withId(R.id.btn_share_qr)).perform(click());

            onView(withText("Close"))
                    .inRoot(isDialog())
                    .check(matches(isDisplayed()))
                    .perform(click());

            // Dialog should be gone — the share button on the activity is visible again
            onView(withId(R.id.btn_share_qr)).check(matches(isDisplayed()));
        }
    }

    /**
     * Launching with a null EVENT_ID should not crash the activity (defensive check
     * for callers that forget to pass the extra).
     */
    @Test
    public void nullEventId_doesNotCrashOnLaunch() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                EventDetailActivity.class
        );
        // Intentionally omit EVENT_ID extra

        try (ActivityScenario<EventDetailActivity> scenario = ActivityScenario.launch(intent)) {
            // Activity should reach RESUMED state without throwing
            scenario.onActivity(activity -> {
                // Verify the activity is alive
                assert !activity.isFinishing();
            });
        }
    }

    /**
     * Clicking the share button when EVENT_ID is null should still show
     * the QR dialog (error image path in QrHelper) rather than crashing.
     */
    @Test
    public void nullEventId_shareButton_showsDialogWithoutCrash() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                EventDetailActivity.class
        );

        try (ActivityScenario<EventDetailActivity> ignored = ActivityScenario.launch(intent)) {

            onView(withId(R.id.btn_share_qr)).perform(click());

            // Dialog should still appear even if QrHelper falls back to error drawable
            onView(withText("Event QR Code"))
                    .inRoot(isDialog())
                    .check(matches(isDisplayed()));
        }
    }
}