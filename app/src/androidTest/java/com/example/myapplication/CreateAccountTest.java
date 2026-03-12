package com.example.myapplication;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * Tests for the CreateAccount:
 *
 * Launches successfully and displays all required UI elements.
 * Properly validates an incorrect email format.
 */
@RunWith(AndroidJUnit4.class)
public class CreateAccountTest {

    /**
     * Tests that the Activity launches
     * Buttons and typing input bars are there.
     */
    @Test
    public void testActivityLaunchesAndDisplaysUI() {
        try (ActivityScenario<CreateAccount> scenario = ActivityScenario.launch(CreateAccount.class)) {
            // Verify text fields are displayed
            onView(withId(R.id.userName)).check(matches(isDisplayed()));
            onView(withId(R.id.userEmail)).check(matches(isDisplayed()));

            // Verify buttons are displayed
            onView(withId(R.id.btnRoleEntrant)).check(matches(isDisplayed()));
            onView(withId(R.id.saveButton)).check(matches(isDisplayed()));
        }
    }

    /**
     * Tests the email validation logic.
     * If a user types an invalid email and clicks log in (red error).
     */
    @Test
    public void testInvalidEmailDisplaysError() {
        try (ActivityScenario<CreateAccount> scenario = ActivityScenario.launch(CreateAccount.class)) {
            // Type a name
            onView(withId(R.id.userName)).perform(typeText("Test User"), closeSoftKeyboard());

            // Type an invalid email
            onView(withId(R.id.userEmail)).perform(typeText("bad-email-format"), closeSoftKeyboard());

            // Select a role
            onView(withId(R.id.btnRoleEntrant)).perform(click());

            // Click log in (saves the profile) if info valid
            onView(withId(R.id.saveButton)).perform(click());

            // Check that the email field shows the exact error message
            onView(withId(R.id.userEmail)).check(matches(hasErrorText("Please enter a valid email address")));
        }
    }
}