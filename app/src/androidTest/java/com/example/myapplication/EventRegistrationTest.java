package com.example.myapplication;

import android.content.Intent;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(AndroidJUnit4.class)
public class EventRegistrationTest {

    @Rule
    public ActivityScenarioRule<EntrantAccount> activityRule =
            new ActivityScenarioRule<>(
                    new Intent(ApplicationProvider.getApplicationContext(), EventListActivity.class)
            );


    /**
     * This function reads what type of button is currently being displayed and the press it
     * and checks if the opposite appears
     * */
    @Test
    public void eventRegistrationButtonToggles() throws InterruptedException {
        Thread.sleep(3000);
        onView(withText("Tech Workshop")).perform(click());
        Thread.sleep(2000);

        boolean registerVisible = isDialogButtonVisible(R.id.registerBtn);
        boolean cancelVisible = isDialogButtonVisible(R.id.cancelRegisterBtn);

        if (registerVisible) {
            onView(withId(R.id.registerBtn)).perform(click());
            Thread.sleep(1500);

            onView(withId(R.id.cancelRegisterBtn))
                    .check(matches(isDisplayed()));
        } else if (cancelVisible) {
            onView(withId(R.id.cancelRegisterBtn)).perform(click());
            Thread.sleep(1500);

            onView(withId(R.id.registerBtn))
                    .check(matches(isDisplayed()));
        } else {
            throw new AssertionError("Neither register nor cancel register button was visible");
        }
    }


    /**
     * This checks which button is visible right now
     * @param buttonId
     *  This is the button to check if its visible
     * @return
     *  This returns true or false if the button is visible
     * */
    private boolean isDialogButtonVisible(int buttonId) {
        AtomicBoolean visible = new AtomicBoolean(false);

        activityRule.getScenario().onActivity(activity -> {
            Fragment fragment =
                    activity.getSupportFragmentManager().findFragmentByTag("eventDetails");

            if (fragment instanceof EventDetailsFragment) {
                EventDetailsFragment eventDetailsFragment = (EventDetailsFragment) fragment;
                if (eventDetailsFragment.getDialog() != null) {
                    View dialogView = eventDetailsFragment.getDialog().findViewById(buttonId);
                    visible.set(dialogView != null && dialogView.getVisibility() == View.VISIBLE);
                }
            }
        });

        return visible.get();
    }
}
