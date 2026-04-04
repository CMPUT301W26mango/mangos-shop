package com.example.myapplication;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Asked Gemini AI to assist in helping write the tests.
 * The prompt used:
 * Can you please help me in ideas for what should be tested for this file <the file of code for this page> and generate some ideal test cases please.
 */

/**
 * Tests that the MyEventsActivity:
 * Properly displays the history UI to the entrant (US 01.02.03).
 */
@RunWith(AndroidJUnit4.class)
public class EventsHistoryUITest {

    @Rule
    public ActivityScenarioRule<MyEventsActivity> activityRule =
            new ActivityScenarioRule<>(MyEventsActivity.class);

    /**
     * US 01.02.03: Verifies that the screen correctly labels itself as Event History
     * and provides the list view for past registrations.
     */
    @Test
    public void testHistoryScreenElementsAreDisplayed() {
        // Check that our Java title override worked
        Espresso.onView(ViewMatchers.withId(R.id.top_page_title))
                .check(ViewAssertions.matches(ViewMatchers.withText("Event History")));

        // Check that the list is on the screen ready to show historical data
        Espresso.onView(ViewMatchers.withId(R.id.recyclerViewEvents))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}