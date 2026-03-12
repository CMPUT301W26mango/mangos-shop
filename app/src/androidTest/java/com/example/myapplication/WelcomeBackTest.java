package com.example.myapplication;

import android.content.Context;
import android.content.Intent;

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

/**
 * Tests that the WelcomeBack Activity:
 * Formats and displays the user's name correctly.
 */

@RunWith(AndroidJUnit4.class)
public class WelcomeBackTest {

    /**
     * Tests that the app successfully displays the personalized greeting when a name is passed.
     */
    @Test
    public void testWelcomeTextDisplaysName() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, WelcomeBack.class);

        // Pass a name
        intent.putExtra("USER_NAME", "Test");

        try (ActivityScenario<WelcomeBack> scenario = ActivityScenario.launch(intent)) {
            // Verify the text view is visible and text perfectly matches the expected output.
            onView(withId(R.id.welcomeText)).check(matches(isDisplayed()));
            onView(withId(R.id.welcomeText)).check(matches(withText("Welcome Back,\nTest!")));
        }
    }
}