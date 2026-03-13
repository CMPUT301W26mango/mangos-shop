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
 * US 02.02.01 + US 02.06.05 - Tests for WaitingListActivity
 *
 * Tests the Activity's behavior for:
 * - Intent handling (missing eventId should close the activity)
 * - UI elements are present on launch
 * - Initial state is correct (count starts at 0, list is empty)
 * - Title displays correctly
 * - CSV export button is present
 */
@RunWith(AndroidJUnit4.class)
public class WaitingListActivityTest {

    private Context context = ApplicationProvider.getApplicationContext();

    // =====================================================
    // INTENT HANDLING
    // =====================================================

    @Test
    public void testFinishesWhenNoEventIdProvided() {
        Intent intent = new Intent(context, WaitingListActivity.class);

        ActivityScenario<WaitingListActivity> scenario = ActivityScenario.launch(intent);

        assertEquals("Activity should be destroyed when no eventId is provided",
                Lifecycle.State.DESTROYED, scenario.getState());
    }

    @Test
    public void testDoesNotImmediatelyFinishWithEventId() {
        Intent intent = new Intent(context, WaitingListActivity.class);
        intent.putExtra("eventId", "test_event_123");
        intent.putExtra("testMode", true);

        ActivityScenario<WaitingListActivity> scenario = ActivityScenario.launch(intent);

        assertNotEquals("Activity should not be destroyed when eventId is provided",
                Lifecycle.State.DESTROYED, scenario.getState());
    }

    // =====================================================
    // UI ELEMENTS — US 02.02.01
    // =====================================================

    @Test
    public void testTitleIsDisplayed() {
        Intent intent = new Intent(context, WaitingListActivity.class);
        intent.putExtra("eventId", "test_event_123");
        intent.putExtra("testMode", true);

        ActivityScenario.launch(intent);

        onView(withId(R.id.tvWaitingListTitle))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testTitleTextIsCorrect() {
        Intent intent = new Intent(context, WaitingListActivity.class);
        intent.putExtra("eventId", "test_event_123");
        intent.putExtra("testMode", true);

        ActivityScenario.launch(intent);

        onView(withId(R.id.tvWaitingListTitle))
                .check(matches(withText("Registered Entrants")));
    }

    @Test
    public void testEntrantCountIsDisplayed() {
        Intent intent = new Intent(context, WaitingListActivity.class);
        intent.putExtra("eventId", "test_event_123");
        intent.putExtra("testMode", true);

        ActivityScenario.launch(intent);

        onView(withId(R.id.tvEntrantCount))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testInitialCountIsZero() {
        Intent intent = new Intent(context, WaitingListActivity.class);
        intent.putExtra("eventId", "test_event_123");
        intent.putExtra("testMode", true);

        ActivityScenario.launch(intent);

        onView(withId(R.id.tvEntrantCount))
                .check(matches(withText("Total Entrants: 0")));
    }

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
    // CSV EXPORT BUTTON — US 02.06.05
    // =====================================================

    /**
     * Test that the CSV export button is displayed.
     * Criteria #1: "Export users as CSV" button on the enrolled entrants screen.
     */
    @Test
    public void testExportCsvButtonIsDisplayed() {
        Intent intent = new Intent(context, WaitingListActivity.class);
        intent.putExtra("eventId", "test_event_123");
        intent.putExtra("testMode", true);

        ActivityScenario.launch(intent);

        onView(withId(R.id.btnExportCsv))
                .check(matches(isDisplayed()));
    }

    /**
     * Test that the CSV export button has the correct text.
     */
    @Test
    public void testExportCsvButtonText() {
        Intent intent = new Intent(context, WaitingListActivity.class);
        intent.putExtra("eventId", "test_event_123");
        intent.putExtra("testMode", true);

        ActivityScenario.launch(intent);

        onView(withId(R.id.btnExportCsv))
                .check(matches(withText("Export users as CSV")));
    }

    @Test
    public void testHandlesNonExistentEvent() {
        Intent intent = new Intent(context, WaitingListActivity.class);
        intent.putExtra("eventId", "this_event_does_not_exist_999");

        ActivityScenario<WaitingListActivity> scenario = ActivityScenario.launch(intent);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals("Activity should be destroyed when event does not exist",
                Lifecycle.State.DESTROYED, scenario.getState());
    }
}
