package com.example.myapplication;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * The following test file was written with the guidance of Claude AI
 * Prompt: "Write UI tests for notification user stories" April 2, 2026
 */
@RunWith(AndroidJUnit4.class)
public class NotificationUITest {

    @Rule
    public GrantPermissionRule permissionRule =
            GrantPermissionRule.grant(
                    android.Manifest.permission.POST_NOTIFICATIONS);

    /**
     * NotificationsActivity should launch successfully
     */
    @Test
    public void testNotificationsActivity_launches() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                NotificationsActivity.class);
        try (ActivityScenario<NotificationsActivity> scenario =
                     ActivityScenario.launch(intent)) {
            onView(withId(R.id.recyclerViewNotifications))
                    .check(matches(isDisplayed()));
        }
    }

    /**
     * Notifications screen should show the RecyclerView
     */
    @Test
    public void testNotificationsScreen_showsRecyclerView() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                NotificationsActivity.class);
        try (ActivityScenario<NotificationsActivity> scenario =
                     ActivityScenario.launch(intent)) {
            onView(withId(R.id.recyclerViewNotifications))
                    .check(matches(isDisplayed()));
        }
    }

    /**
     * Navigating to notifications from EventListActivity should work
     * Tests the nav_notifications bottom nav button
     */
    @Test
    public void testNavNotifications_navigatesToNotificationsScreen() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                EventListActivity.class);
        try (ActivityScenario<EventListActivity> scenario =
                     ActivityScenario.launch(intent)) {

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onView(withId(R.id.nav_notifications)).perform(click());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onView(withId(R.id.recyclerViewNotifications))
                    .check(matches(isDisplayed()));
        }
    }

    /**
     * Notifications screen should be accessible from EventListActivity
     */
    @Test
    public void testNotificationsScreen_accessibleFromEventList() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                EventListActivity.class);
        try (ActivityScenario<EventListActivity> scenario =
                     ActivityScenario.launch(intent)) {

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onView(withId(R.id.nav_notifications)).perform(click());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onView(withId(R.id.recyclerViewNotifications))
                    .check(matches(isDisplayed()));
        }
    }
}