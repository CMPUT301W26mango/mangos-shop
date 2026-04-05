package com.example.myapplication;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * The following test file was written with the guidance of Claude AI
 * Prompt: "Write UI tests for US 01.05.02 and US 01.05.03" April 2, 2026
 */
@RunWith(AndroidJUnit4.class)
public class InvitationResponseUITest {

    private static final String EVENT_ID = "LVpMCQZpFkqISPnkgYPQ";

    @Rule
    public GrantPermissionRule permissionRule =
            GrantPermissionRule.grant(
                    android.Manifest.permission.POST_NOTIFICATIONS,
                    android.Manifest.permission.CAMERA);

    @Rule
    public ActivityScenarioRule<EventListActivity> activityRule =
            new ActivityScenarioRule<>(EventListActivity.class);

    /**
     * Opens EventDetailsFragment with the selected event and waits for Firestore to load
     */
    private void openEventDetails() {
        activityRule.getScenario().onActivity(activity -> {
            android.os.Bundle bundle = new android.os.Bundle();
            bundle.putString("eventId", EVENT_ID);
            EventDetailsFragment fragment = new EventDetailsFragment();
            fragment.setArguments(bundle);
            fragment.show(activity.getSupportFragmentManager(), "eventDetails");
        });

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * When status is "selected", the congratulations message should be visible
     */
    @Test
    public void testSelectedStatus_showsSelectedMessage() {
        openEventDetails();
        onView(withId(R.id.tv_selected_message))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /**
     * When status is "selected", accept button should be visible
     */
    @Test
    public void testSelectedStatus_showsAcceptButton() {
        openEventDetails();
        onView(withId(R.id.acceptBtn))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /**
     * When status is "selected", decline button should be visible
     */
    @Test
    public void testSelectedStatus_showsDeclineButton() {
        openEventDetails();
        onView(withId(R.id.declineBtn))
                .inRoot(isDialog())
                .perform(androidx.test.espresso.action.ViewActions.scrollTo())
                .check(matches(isDisplayed()));
    }

    /**
     * When status is "selected", register button should be hidden
     */
    @Test
    public void testSelectedStatus_hidesRegisterButton() {
        openEventDetails();
        onView(withId(R.id.registerBtn))
                .inRoot(isDialog())
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /**
     * When status is "selected", cancel button should be hidden
     */
    @Test
    public void testSelectedStatus_hidesCancelButton() {
        openEventDetails();
        onView(withId(R.id.cancelRegisterBtn))
                .inRoot(isDialog())
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /**
     * When status is "selected", accepted message should be hidden
     */
    @Test
    public void testSelectedStatus_hidesAcceptedMessage() {
        openEventDetails();
        onView(withId(R.id.tv_accepted_message))
                .inRoot(isDialog())
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /**
     * When status is "selected", declined message should be hidden
     */
    @Test
    public void testSelectedStatus_hidesDeclinedMessage() {
        openEventDetails();
        onView(withId(R.id.tv_declined_message))
                .inRoot(isDialog())
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /**
     * When status is "selected", already registered text should be hidden
     */
    @Test
    public void testSelectedStatus_hidesAlreadyRegisteredText() {
        openEventDetails();
        onView(withId(R.id.alreadyRegisteredTextView))
                .inRoot(isDialog())
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }
}