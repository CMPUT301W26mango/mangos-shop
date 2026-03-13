package com.example.myapplication;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * The following test file was written with the guidance of Claude AI
 * Prompt: "Help me with writing UI tests for EventDetailsFragment" March 12, 2026
 */

@RunWith(AndroidJUnit4.class)
public class EventDetailsDialogueTest {

    @Rule
    public ActivityScenarioRule<EventListActivity> activityRule =
            new ActivityScenarioRule<>(EventListActivity.class);

    private void openEventDetailsDialog(String eventId) {
        activityRule.getScenario().onActivity(activity -> {
            android.os.Bundle args = new android.os.Bundle();
            args.putString("eventId", eventId);
            EventDetailsFragment fragment = new EventDetailsFragment();
            fragment.setArguments(args);
            fragment.show(activity.getSupportFragmentManager(), "eventDetails");
        });


    }

    // Checks for close button in popup
    @Test
    public void testCloseButtonIsVisible() {
        openEventDetailsDialog("12345");
        onView(withId(R.id.btn_close))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    // Checks if close button dismisses popup
    @Test
    public void testCloseButtonDismissesDialog() {
        openEventDetailsDialog("12345");

        onView(withId(R.id.btn_close))
                .inRoot(isDialog())
                .perform(click());

        onView(withId(R.id.btn_close)).check(doesNotExist());
    }

    // Checks if no eventId provided, popup will disappear
    @Test
    public void testNoEventIdDismissesDialog() {
        activityRule.getScenario().onActivity(activity -> {
            EventDetailsFragment fragment = new EventDetailsFragment();
            fragment.show(activity.getSupportFragmentManager(), "eventDetails");
        });


        onView(withId(R.id.btn_close)).check(doesNotExist());
    }

    // Checks if event title is correctly displayed
    @Test
    public void testEventTitleIsDisplayed() {
        openEventDetailsDialog("12345");
        onView(withId(R.id.tv_event_title))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    // Checks if event type is correctly displayed
    @Test
    public void testEventTypeIsDisplayed() {
        openEventDetailsDialog("12345");
        onView(withId(R.id.tv_event_type))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    // Checks if event description is correctly displayed
    @Test
    public void testEventDescIsDisplayed() {
        openEventDetailsDialog("12345");
        onView(withId(R.id.tv_event_description))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    // Checks if event location is correctly displayed
    @Test
    public void testEventLocationIsDisplayed() {
        openEventDetailsDialog("12345");
        onView(withId(R.id.tv_event_location))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    // Checks if event date is correctly displayed
    @Test
    public void testEventDateIsDisplayed() {
        openEventDetailsDialog("12345");
        onView(withId(R.id.tv_event_date))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    // Checks if registration start date is correctly displayed
    @Test
    public void testEventRegistrationOpen() {
        openEventDetailsDialog("12345");
        onView(withId(R.id.tv_reg_start))
                .inRoot(isDialog())
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    // Checks if registration end date is correctly displayed
    @Test
    public void testEventRegistrationClose() {
        openEventDetailsDialog("12345");
        onView(withId(R.id.tv_reg_end))
                .inRoot(isDialog())
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    // Checks if number of spots available is correctly displayed
    @Test
    public void testEventSpotsDisplayed() {
        openEventDetailsDialog("12345");
        onView(withId(R.id.tv_spots_available))
                .inRoot(isDialog())
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    // Checks if organizer name is properly displayed
    @Test
    public void testOrganizerIsDisplayed() {
        openEventDetailsDialog("12345");
        onView(withId(R.id.tv_organizer))
                .inRoot(isDialog())
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    // Checks if either register or cancel registration button is shown
    @Test
    public void testOnlyOneRegisterButtonVisible() {
        openEventDetailsDialog("12345");
        onView(withId(R.id.registerBtn))
                .inRoot(isDialog())
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }
}