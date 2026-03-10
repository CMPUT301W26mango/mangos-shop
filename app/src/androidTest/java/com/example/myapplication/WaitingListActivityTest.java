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
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;

/**
 * US 02.02.01 - Tests for WaitingListActivity
 *
 * Tests the Activity's behavior for:
 * - Intent handling (missing eventId should close the activity)
 * - UI elements are present on launch
 * - Initial state is correct (count starts at 0, list is empty)
 * - Title displays correctly
 *
 * NOTE ON FIRESTORE TESTS:
 * Tests that involve real Firestore data (ownership check, real-time updates)
 * require either the Firebase Emulator or real Firestore test data.
 * Those tests are marked with comments explaining what to set up.
 * The adapter tests (WaitingListAdapterTest) already cover the data
 * binding and update logic independently of Firestore.
 */
@RunWith(AndroidJUnit4.class)
public class WaitingListActivityTest {

    private Context context = ApplicationProvider.getApplicationContext();

    // =====================================================
    // INTENT HANDLING — Activity requires an eventId
    // =====================================================

    /**
     * Test that the Activity finishes immediately when launched
     * without an eventId in the Intent.
     *
     * This verifies the safety check in onCreate.
     * A teammate who forgets to pass eventId will see the toast
     * and the screen will close — not crash.
     */
    @Test
    public void testFinishesWhenNoEventIdProvided() {
        Intent intent = new Intent(context, WaitingListActivity.class);
        // Deliberately NOT putting "eventId" in the intent

        ActivityScenario<WaitingListActivity> scenario = ActivityScenario.launch(intent);

        // The activity calls finish() in onCreate, so it should be DESTROYED
        assertEquals("Activity should be destroyed when no eventId is provided",
                Lifecycle.State.DESTROYED, scenario.getState());
    }

    /**
     * Test that the Activity does NOT immediately finish when
     * a valid eventId is provided in the Intent.
     * (It may finish later if ownership check fails, but it should
     * not finish in onCreate itself.)
     */
    @Test
    public void testDoesNotImmediatelyFinishWithEventId() {
        Intent intent = new Intent(context, WaitingListActivity.class);
        intent.putExtra("eventId", "test_event_123");
        intent.putExtra("testMode", true);

        ActivityScenario<WaitingListActivity> scenario = ActivityScenario.launch(intent);

        // It should be alive — not DESTROYED means it passed the onCreate check
        assertNotEquals("Activity should not be destroyed when eventId is provided",
                Lifecycle.State.DESTROYED, scenario.getState());
    }

    // =====================================================
    // UI ELEMENTS — All required views exist on screen
    // =====================================================

    /**
     * Test that the title TextView is displayed.
     * Criteria #1: organizers can navigate to this screen.
     */
    @Test
    public void testTitleIsDisplayed() {
        Intent intent = new Intent(context, WaitingListActivity.class);
        intent.putExtra("eventId", "test_event_123");
        intent.putExtra("testMode", true);

        ActivityScenario.launch(intent);

        onView(withId(R.id.tvWaitingListTitle))
                .check(matches(isDisplayed()));
    }

    /**
     * Test that the title says "Waiting List".
     */
    @Test
    public void testTitleTextIsCorrect() {
        Intent intent = new Intent(context, WaitingListActivity.class);
        intent.putExtra("eventId", "test_event_123");
        intent.putExtra("testMode", true);

        ActivityScenario.launch(intent);

        onView(withId(R.id.tvWaitingListTitle))
                .check(matches(withText("Waiting List")));
    }

    /**
     * Test that the entrant count TextView is displayed.
     * Criteria #3: list count matches total entrant count.
     */
    @Test
    public void testEntrantCountIsDisplayed() {
        Intent intent = new Intent(context, WaitingListActivity.class);
        intent.putExtra("eventId", "test_event_123");
        intent.putExtra("testMode", true);

        ActivityScenario.launch(intent);

        onView(withId(R.id.tvEntrantCount))
                .check(matches(isDisplayed()));
    }

    /**
     * Test that the entrant count starts at "Total Entrants: 0".
     * Before Firestore data arrives, the count should be 0.
     */
    @Test
    public void testInitialCountIsZero() {
        Intent intent = new Intent(context, WaitingListActivity.class);
        intent.putExtra("eventId", "test_event_123");
        intent.putExtra("testMode", true);

        ActivityScenario.launch(intent);

        onView(withId(R.id.tvEntrantCount))
                .check(matches(withText("Total Entrants: 0")));
    }

    /**
     * Test that the RecyclerView is displayed.
     * Criteria #2: list shows entrant names (the container must exist).
     */
    @Test
    public void testRecyclerViewIsDisplayed() {
        Intent intent = new Intent(context, WaitingListActivity.class);
        intent.putExtra("eventId", "test_event_123");
        intent.putExtra("testMode", true);

        ActivityScenario.launch(intent);

        onView(withId(R.id.rvEntrants))
                .check(matches(isDisplayed()));
    }

    // =====================================================
    // OWNERSHIP CHECK — Criteria #5
    // Organizers can only see waiting lists for their own events.
    //
    // These tests require Firestore test data. Instructions below.
    // =====================================================

    /**
     * Test that the Activity closes when the event's organizerDeviceId
     * does NOT match the current device.
     *
     * SETUP REQUIRED: Before running this test, create a Firestore document:
     *     Collection: "events"
     *     Document ID: "event_owned_by_other"
     *     Fields:
     *         organizerDeviceId: "some_other_device_id"
     */
    @Test
    public void testDeniesAccessForNonOwner() {
        Intent intent = new Intent(context, WaitingListActivity.class);
        intent.putExtra("eventId", "event_owned_by_other");

        ActivityScenario<WaitingListActivity> scenario = ActivityScenario.launch(intent);

        // Wait for Firestore response (ownership check is async)
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Activity should have called finish() and be destroyed
        assertEquals("Activity should be destroyed when device is not the event owner",
                Lifecycle.State.DESTROYED, scenario.getState());
    }

    /**
     * Test that the Activity stays open when the event's organizerDeviceId
     * MATCHES the current device.
     *
     * SETUP REQUIRED: Before running this test, you need to:
     * 1. Find your test device/emulator's ANDROID_ID
     *    (adb shell settings get secure android_id)
     * 2. Create a Firestore document:
     *     Collection: "events"
     *     Document ID: "event_owned_by_me"
     *     Fields:
     *         organizerDeviceId: "<your_device_android_id>"
     */
    @Test
    public void testAllowsAccessForOwner() {
        Intent intent = new Intent(context, WaitingListActivity.class);
        intent.putExtra("eventId", "event_owned_by_me");

        ActivityScenario<WaitingListActivity> scenario = ActivityScenario.launch(intent);

        // Wait for Firestore response
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Activity should still be alive
        assertNotEquals("Activity should remain alive when device is the event owner",
                Lifecycle.State.DESTROYED, scenario.getState());
    }

    /**
     * Test that the Activity handles a non-existent event gracefully.
     *
     * If someone passes an eventId that doesn't exist in Firestore,
     * the Activity should show an error and close — not crash.
     */
    @Test
    public void testHandlesNonExistentEvent() {
        Intent intent = new Intent(context, WaitingListActivity.class);
        intent.putExtra("eventId", "this_event_does_not_exist_999");

        ActivityScenario<WaitingListActivity> scenario = ActivityScenario.launch(intent);

        // Wait for Firestore to respond with "not found"
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Activity should have called finish() and be destroyed
        assertEquals("Activity should be destroyed when event does not exist",
                Lifecycle.State.DESTROYED, scenario.getState());
    }
}