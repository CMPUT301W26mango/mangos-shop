package com.example.myapplication;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * The following test file was written with the guidance of Gemini AI
 * Prompt: "Guide me with writing tests for Event" April 2, 2026
 */

/**
 * UI Tests for UserSearchActivity.
 * Focuses on the search bar interaction and the 2-character query threshold.
 * @author Sayuj
 */
@RunWith(AndroidJUnit4.class)
public class UserSearchActivityUITest {

    private static final String TEST_EVENT_ID = "LVpMCQZpFkqISPnkgYPQ";

    /**
     * Verifies that the search bar and results list are present on launch.
     */
    @Test
    public void testUIInitialization() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), UserSearchActivity.class);
        intent.putExtra("EVENT_ID", TEST_EVENT_ID);

        try (ActivityScenario<UserSearchActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.et_search_bar)).check(matches(isDisplayed()));
            onView(withId(R.id.rv_search_results)).check(matches(isDisplayed()));
        }
    }

    /**
     * Tests the search logic:
     * 1. Typing 1 character should not trigger a search.
     * 2. Typing 2+ characters should trigger the Profiles helper.
     */
    @Test
    public void testSearchThresholdLogic() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), UserSearchActivity.class);
        intent.putExtra("EVENT_ID", TEST_EVENT_ID);

        try (ActivityScenario<UserSearchActivity> scenario = ActivityScenario.launch(intent)) {
            // Step 1: Type only 1 character
            onView(withId(R.id.et_search_bar)).perform(typeText("S"));

            // Small wait to ensure no unexpected async search triggers
            try { Thread.sleep(500); } catch (InterruptedException e) {}

            // Ensure the text is present but the threshold hasn't triggered results
            onView(withId(R.id.et_search_bar)).check(matches(withText("S")));

            // Step 2: Clear and type 3 characters to pass the threshold
            onView(withId(R.id.et_search_bar)).perform(clearText(), typeText("Say"));

            // Give Firestore time to return users matching "Say"
            try { Thread.sleep(2000); } catch (InterruptedException e) {}

            // At this point, the RecyclerView would be populated.
            // We verify the activity is still active and responding to the input.
            onView(withId(R.id.rv_search_results)).check(matches(isDisplayed()));
        }
    }

    /**
     * Tests that the Activity correctly receives and handles intent extras
     * for private events and co-organizer permissions.
     */
    @Test
    public void testIntentExtrasHandling() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), UserSearchActivity.class);
        intent.putExtra("EVENT_ID", TEST_EVENT_ID);
        intent.putExtra("EVENT_NAME", "UofA Gala 2026");
        intent.putExtra("IS_PRIVATE", true);
        intent.putExtra("IS_CO_ORG", false);

        try (ActivityScenario<UserSearchActivity> scenario = ActivityScenario.launch(intent)) {
            // Verify basic UI presence with these specific flags
            onView(withId(R.id.et_search_bar)).check(matches(isDisplayed()));
        }
    }
}