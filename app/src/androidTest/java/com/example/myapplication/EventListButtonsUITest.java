package com.example.myapplication;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * The following test file was written with the guidance of Claude AI
 * Prompt: "Help me with writing UI tests for the buttons on EventListActivity" March 12, 2026
 */

@RunWith(AndroidJUnit4.class)
public class EventListButtonsUITest {

    @Rule
    public ActivityScenarioRule<EventListActivity> activityRule =
            new ActivityScenarioRule<>(EventListActivity.class);

    // Checks if lottery info button is displayed on event lists page
    @Test
    public void testLotteryInfoButtonIsVisible() {
        onView(withId(R.id.lotteryinfoButton))
                .check(matches(isDisplayed()));
    }

    // Checks if clicking on lottery info button results in popup with close button
    @Test
    public void testLotteryInfoButtonClickOpensDialog() {
        onView(withId(R.id.lotteryinfoButton))
                .perform(click());

        onView(withId(R.id.closeButton))
                .check(matches(isDisplayed()));
    }

    // Opens lottery info dialog and closes it using close button
    @Test
    public void testLotteryInfoDialogCloseButtonDismissesDialog() {
        onView(withId(R.id.lotteryinfoButton))
                .perform(click());

        onView(withId(R.id.closeButton))
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withId(R.id.closeButton))
                .check(doesNotExist());
    }

    // Checks if QR scanner button appears on EventList page
    @Test
    public void testScanQRButtonIsVisible() {
        onView(withId(R.id.scanQRButton))
                .check(matches(isDisplayed()));
    }

    // Checks if QR scanner button can be clicked without crashing
    @Test
    public void testScanQRButtonIsClickable() {
        onView(withId(R.id.scanQRButton))
                .perform(click());
    }
}