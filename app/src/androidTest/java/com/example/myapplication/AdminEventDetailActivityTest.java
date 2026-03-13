package com.example.myapplication;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class AdminEventDetailActivityTest {

    @Test
    public void eventDetailScreen_displaysRequiredViews() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                AdminEventDetailActivity.class
        );

        intent.putExtra("eventId", "rWdDQCowQGTIhMWInBJS");

        try (ActivityScenario<AdminEventDetailActivity> scenario =
                     ActivityScenario.launch(intent)) {

            onView(withId(R.id.textEventTitle)).check(matches(isDisplayed()));
            onView(withId(R.id.textEventLocation)).check(matches(isDisplayed()));
            onView(withId(R.id.textEventOrganizer)).check(matches(isDisplayed()));
            onView(withId(R.id.buttonRemoveImage)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void clickingRemoveImage_showsConfirmationDialog() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                AdminEventDetailActivity.class
        );

        // Use a real event ID from your Firebase if possible
        intent.putExtra("eventId", "rWdDQCowQGTIhMWInBJS");

        try (ActivityScenario<AdminEventDetailActivity> scenario =
                     ActivityScenario.launch(intent)) {

            onView(withId(R.id.buttonRemoveImage)).perform(click());

            onView(withText("Remove Image")).check(matches(isDisplayed()));
            onView(withText("Are you sure you want to remove this event image?"))
                    .check(matches(isDisplayed()));
        }
    }
}