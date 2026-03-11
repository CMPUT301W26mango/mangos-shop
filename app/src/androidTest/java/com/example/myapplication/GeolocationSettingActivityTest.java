package com.example.myapplication;

import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;

/**
 * US 02.02.03 - Tests for GeolocationSettingActivity
 *
 * Tests that the geolocation toggle:
 * - Exists and is visible (criteria #1)
 * - Can be toggled on and off (criteria #1)
 * - Updates status text when toggled (criteria #1)
 * - Defaults to off/disabled (criteria #3)
 * - Handles missing eventId gracefully
 * - Ownership check works (criteria #4 — Firestore dependent)
 */
@RunWith(AndroidJUnit4.class)
public class GeolocationSettingActivityTest {

    private Context context = ApplicationProvider.getApplicationContext();

    // =====================================================
    // INTENT HANDLING — Activity requires an eventId
    // =====================================================

    /**
     * Test that the Activity finishes immediately when launched
     * without an eventId in the Intent.
     */
    @Test
    public void testFinishesWhenNoEventIdProvided() {
        Intent intent = new Intent(context, GeolocationSettingActivity.class);

        ActivityScenario<GeolocationSettingActivity> scenario = ActivityScenario.launch(intent);

        assertEquals("Activity should be destroyed when no eventId is provided",
                Lifecycle.State.DESTROYED, scenario.getState());
    }

    /**
     * Test that the Activity does NOT immediately finish when
     * a valid eventId is provided.
     */
    @Test
    public void testDoesNotImmediatelyFinishWithEventId() {
        Intent intent = new Intent(context, GeolocationSettingActivity.class);
        intent.putExtra("eventId", "test_event_123");
        intent.putExtra("testMode", true);

        ActivityScenario<GeolocationSettingActivity> scenario = ActivityScenario.launch(intent);

        assertNotEquals("Activity should not be destroyed when eventId is provided",
                Lifecycle.State.DESTROYED, scenario.getState());
    }

    // =====================================================
    // CRITERIA #1 — Toggle for geolocation on event form
    // =====================================================

    /**
     * Test that the title is displayed.
     */
    @Test
    public void testTitleIsDisplayed() {
        Intent intent = new Intent(context, GeolocationSettingActivity.class);
        intent.putExtra("eventId", "test_event_123");
        intent.putExtra("testMode", true);

        ActivityScenario.launch(intent);

        onView(withId(R.id.tvGeoTitle))
                .check(matches(isDisplayed()));
    }

    /**
     * Test that the title says "Geolocation Setting".
     */
    @Test
    public void testTitleTextIsCorrect() {
        Intent intent = new Intent(context, GeolocationSettingActivity.class);
        intent.putExtra("eventId", "test_event_123");
        intent.putExtra("testMode", true);

        ActivityScenario.launch(intent);

        onView(withId(R.id.tvGeoTitle))
                .check(matches(withText("Geolocation Setting")));
    }

    /**
     * Test that the geolocation switch is displayed.
     */
    @Test
    public void testSwitchIsDisplayed() {
        Intent intent = new Intent(context, GeolocationSettingActivity.class);
        intent.putExtra("eventId", "test_event_123");
        intent.putExtra("testMode", true);

        ActivityScenario.launch(intent);

        onView(withId(R.id.switchGeolocation))
                .check(matches(isDisplayed()));
    }

    /**
     * Test that the switch is enabled (interactable) in test mode.
     */
    @Test
    public void testSwitchIsEnabled() {
        Intent intent = new Intent(context, GeolocationSettingActivity.class);
        intent.putExtra("eventId", "test_event_123");
        intent.putExtra("testMode", true);

        ActivityScenario.launch(intent);

        onView(withId(R.id.switchGeolocation))
                .check(matches(isEnabled()));
    }

    /**
     * Test that the status text is displayed.
     */
    @Test
    public void testStatusTextIsDisplayed() {
        Intent intent = new Intent(context, GeolocationSettingActivity.class);
        intent.putExtra("eventId", "test_event_123");
        intent.putExtra("testMode", true);

        ActivityScenario.launch(intent);

        onView(withId(R.id.tvGeoStatus))
                .check(matches(isDisplayed()));
    }

    // =====================================================
    // CRITERIA #3 — When disabled, no location data collected
    //               (default state should be OFF)
    // =====================================================

    /**
     * Test that the switch defaults to OFF (unchecked).
     */
    @Test
    public void testSwitchDefaultsToOff() {
        Intent intent = new Intent(context, GeolocationSettingActivity.class);
        intent.putExtra("eventId", "test_event_123");
        intent.putExtra("testMode", true);

        ActivityScenario.launch(intent);

        onView(withId(R.id.switchGeolocation))
                .check(matches(isNotChecked()));
    }

    /**
     * Test that the default status text indicates geolocation is disabled.
     */
    @Test
    public void testDefaultStatusTextShowsDisabled() {
        Intent intent = new Intent(context, GeolocationSettingActivity.class);
        intent.putExtra("eventId", "test_event_123");
        intent.putExtra("testMode", true);

        ActivityScenario.launch(intent);

        onView(withId(R.id.tvGeoStatus))
                .check(matches(withText(
                        "Geolocation is currently DISABLED for this event.\nNo location data will be collected."
                )));
    }

    // =====================================================
    // CRITERIA #1 — Toggle can be switched on and off
    // =====================================================

    /**
     * Test that tapping the switch turns it ON.
     */
    @Test
    public void testCanToggleSwitchOn() {
        Intent intent = new Intent(context, GeolocationSettingActivity.class);
        intent.putExtra("eventId", "test_event_123");
        intent.putExtra("testMode", true);

        ActivityScenario.launch(intent);

        // Switch starts OFF — tap to turn ON
        onView(withId(R.id.switchGeolocation))
                .perform(click());

        onView(withId(R.id.switchGeolocation))
                .check(matches(isChecked()));
    }

    /**
     * Test that tapping the switch twice returns it to OFF.
     */
    @Test
    public void testCanToggleSwitchBackOff() {
        Intent intent = new Intent(context, GeolocationSettingActivity.class);
        intent.putExtra("eventId", "test_event_123");
        intent.putExtra("testMode", true);

        ActivityScenario.launch(intent);

        // Tap ON then OFF
        onView(withId(R.id.switchGeolocation))
                .perform(click())
                .perform(click());

        onView(withId(R.id.switchGeolocation))
                .check(matches(isNotChecked()));
    }

    /**
     * Test that status text updates to ENABLED when switch is turned ON.
     */
    @Test
    public void testStatusTextUpdatesWhenEnabled() {
        Intent intent = new Intent(context, GeolocationSettingActivity.class);
        intent.putExtra("eventId", "test_event_123");
        intent.putExtra("testMode", true);

        ActivityScenario.launch(intent);

        // Turn switch ON
        onView(withId(R.id.switchGeolocation))
                .perform(click());

        onView(withId(R.id.tvGeoStatus))
                .check(matches(withText(
                        "Geolocation is currently ENABLED for this event.\nEntrants must share their location when joining."
                )));
    }

    /**
     * Test that status text updates back to DISABLED when switch is turned OFF.
     */
    @Test
    public void testStatusTextUpdatesWhenDisabled() {
        Intent intent = new Intent(context, GeolocationSettingActivity.class);
        intent.putExtra("eventId", "test_event_123");
        intent.putExtra("testMode", true);

        ActivityScenario.launch(intent);

        // Turn ON then back OFF
        onView(withId(R.id.switchGeolocation))
                .perform(click())
                .perform(click());

        onView(withId(R.id.tvGeoStatus))
                .check(matches(withText(
                        "Geolocation is currently DISABLED for this event.\nNo location data will be collected."
                )));
    }

    // =====================================================
    // OWNERSHIP CHECK — Same pattern as WaitingListActivity
    // These tests require Firestore test data.
    // =====================================================

    /**
     * Test that the Activity closes when the event's organizerDeviceId
     * does NOT match the current device.
     *
     * SETUP REQUIRED: Firestore document:
     *     Collection: "events"
     *     Document ID: "event_owned_by_other"
     *     Fields: organizerDeviceId: "some_other_device_id"
     */
    @Test
    public void testDeniesAccessForNonOwner() {
        Intent intent = new Intent(context, GeolocationSettingActivity.class);
        intent.putExtra("eventId", "event_owned_by_other");

        ActivityScenario<GeolocationSettingActivity> scenario = ActivityScenario.launch(intent);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals("Activity should be destroyed when device is not the event owner",
                Lifecycle.State.DESTROYED, scenario.getState());
    }

    /**
     * Test that the Activity stays open when the event's organizerDeviceId
     * MATCHES the current device.
     *
     * SETUP REQUIRED:
     * 1. Find emulator ANDROID_ID: adb shell settings get secure android_id
     * 2. Firestore document:
     *     Collection: "events"
     *     Document ID: "event_owned_by_me"
     *     Fields: organizerDeviceId: "<your_android_id>"
     *             geolocationRequired: false
     */
    @Test
    public void testAllowsAccessForOwner() {
        Intent intent = new Intent(context, GeolocationSettingActivity.class);
        intent.putExtra("eventId", "event_owned_by_me");

        ActivityScenario<GeolocationSettingActivity> scenario = ActivityScenario.launch(intent);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertNotEquals("Activity should remain alive when device is the event owner",
                Lifecycle.State.DESTROYED, scenario.getState());
    }

    /**
     * Test that the Activity handles a non-existent event gracefully.
     */
    @Test
    public void testHandlesNonExistentEvent() {
        Intent intent = new Intent(context, GeolocationSettingActivity.class);
        intent.putExtra("eventId", "this_event_does_not_exist_999");

        ActivityScenario<GeolocationSettingActivity> scenario = ActivityScenario.launch(intent);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals("Activity should be destroyed when event does not exist",
                Lifecycle.State.DESTROYED, scenario.getState());
    }
}