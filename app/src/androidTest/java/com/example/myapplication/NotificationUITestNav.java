package com.example.myapplication;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import android.Manifest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Asked Gemini AI to assist in helping write the tests.
 * The prompt used:
 * Can you please help me in ideas for what should be tested for this file <the file of code for this page> and generate some ideal test cases please.
 */

/**
 * Tests the In-App Notifications UI:
 * Verifies that the user can navigate to the Notifications tab.
 * Ensures the screen successfully loads the required UI elements.
 */
@RunWith(AndroidJUnit4.class)
public class NotificationUITestNav {

    // Auto Click "Yes" on allow notis
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS);

    // Launch Main Activity
    @Rule
    public ActivityScenarioRule<EventListActivity> activityRule =
            new ActivityScenarioRule<>(EventListActivity.class);


    /**
     * Tests that clicking the Notifications button in the bottom navigation bar
     * successfully switches the screen/fragment to display the notifications UI.
     */
    @Test
    public void testNavigatingToNotificationsTab() {
        // 1. Physically click the Notifications icon on the bottom nav bar
        Espresso.onView(ViewMatchers.withId(R.id.nav_notifications))
                .perform(ViewActions.click());

        // 2. Verify that the screen actually changed.
        Espresso.onView(ViewMatchers.withId(R.id.recyclerViewNotifications))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}