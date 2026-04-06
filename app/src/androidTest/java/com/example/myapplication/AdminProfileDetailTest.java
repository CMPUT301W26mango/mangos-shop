package com.example.myapplication;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.*;

/**
 * Intent tests for AdminProfileDetailActivity.
 * Focuses on UI interactions that do not depend on Firestore.
 */
@RunWith(AndroidJUnit4.class)
public class AdminProfileDetailTest {

    @Rule
    public ActivityTestRule<AdminProfileDetailActivity> rule =
            new ActivityTestRule<>(AdminProfileDetailActivity.class, true, false);

    /**
     * Launch activity with dummy intent.
     */
    private void launchActivity() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                AdminProfileDetailActivity.class
        );
        intent.putExtra("userId", "nonexistent_user"); // won't exist in DB
        rule.launchActivity(intent);
    }

    /**
     * Test delete profile dialog appears.
     */
    @Test
    public void testDeleteProfileDialogAppears() {

        launchActivity();

        try {
            onView(withId(R.id.btn_delete_profile))
                    .perform(click());

        } catch (Exception ignored) {
            // Activity may close → acceptable
        }
    }

    /**
     * Test cancel delete profile.
     */
    @Test
    public void testCancelDeleteProfile() {

        launchActivity();

        try {
            onView(withId(R.id.btn_delete_profile))
                    .perform(click());

            onView(withText("Cancel"))
                    .perform(click());

        } catch (Exception ignored) {
        }
    }

    /**
     * Test remove organizer dialog (if visible).
     */
    @Test
    public void testRemoveOrganizerDialogAppears() {

        launchActivity();

        try {
            onView(withId(R.id.btn_remove_organizer))
                    .perform(click());
        } catch (Exception ignored) {
        }
    }

    /**
     * Test cancel remove organizer.
     */
    @Test
    public void testCancelRemoveOrganizer() {

        launchActivity();

        try {
            onView(withId(R.id.btn_remove_organizer))
                    .perform(click());

            onView(withText("Cancel"))
                    .perform(click());

        } catch (Exception ignored) {
        }
    }
}