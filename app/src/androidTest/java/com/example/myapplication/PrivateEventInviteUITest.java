package com.example.myapplication;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/***
 * The following test file was written with the guidance of Claude AI
 * Prompt: "Write UI tests for US 01.05.06 and US 01.05.07" April 2, 2026
 */
@RunWith(AndroidJUnit4.class)
public class PrivateEventInviteUITest {

    private static final String EVENT_ID ="p1GMyGAI6gzOipW9ofoN";

    @Rule
    public GrantPermissionRule permissionRule =
            GrantPermissionRule.grant(
                    android.Manifest.permission.POST_NOTIFICATIONS,
                    android.Manifest.permission.CAMERA);

    @Rule
    public ActivityScenarioRule<EventListActivity> activityRule =
            new ActivityScenarioRule<>(EventListActivity.class);

    /**
     * Opens EventDetailsFragment with the invited event and waits for Firestore to load
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
     * When device is in invitedUsers, the invited message should be visible
     * Covers US 01.05.06 — entrant sees they have been invited
     */
    @Test
    public void testInvitedStatus_showsInvitedMessage() {
        openEventDetails();
        onView(withId(R.id.tv_invited_message))
                .inRoot(isDialog())
                .perform(ViewActions.scrollTo())
                .check(matches(isDisplayed()));
    }

    /**
     * When device is in invitedUsers, accept invite button should be visible
     * Covers US 01.05.07 — entrant can accept the invitation
     */
    @Test
    public void testInvitedStatus_showsAcceptInviteButton() {
        openEventDetails();
        onView(withId(R.id.acceptInvBtn))
                .inRoot(isDialog())
                .perform(ViewActions.scrollTo())
                .check(matches(isDisplayed()));
    }

    /**
     * When device is in invitedUsers, decline invite button should be visible
     * Covers US 01.05.07 — entrant can decline the invitation
     */
    @Test
    public void testInvitedStatus_showsDeclineInviteButton() {
        openEventDetails();
        onView(withId(R.id.declineInvBtn))
                .inRoot(isDialog())
                .perform(ViewActions.scrollTo())
                .check(matches(isDisplayed()));
    }

    /**
     * When device is in invitedUsers, register button should be hidden
     */
    @Test
    public void testInvitedStatus_hidesRegisterButton() {
        openEventDetails();
        onView(withId(R.id.registerBtn))
                .inRoot(isDialog())
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /**
     * When device is in invitedUsers, cancel button should be hidden
     */
    @Test
    public void testInvitedStatus_hidesCancelButton() {
        openEventDetails();
        onView(withId(R.id.cancelRegisterBtn))
                .inRoot(isDialog())
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /**
     * When device is in invitedUsers, accept lottery button should be hidden
     * This is different from acceptInvBtn — acceptBtn is for lottery selection
     */
    @Test
    public void testInvitedStatus_hidesLotteryAcceptButton() {
        openEventDetails();
        onView(withId(R.id.acceptBtn))
                .inRoot(isDialog())
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /**
     * When device is in invitedUsers, decline lottery button should be hidden
     * This is different from declineInvBtn — declineBtn is for lottery selection
     */
    @Test
    public void testInvitedStatus_hidesLotteryDeclineButton() {
        openEventDetails();
        onView(withId(R.id.declineBtn))
                .inRoot(isDialog())
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /**
     * When device is in invitedUsers, selected message should be hidden
     */
    @Test
    public void testInvitedStatus_hidesSelectedMessage() {
        openEventDetails();
        onView(withId(R.id.tv_selected_message))
                .inRoot(isDialog())
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /**
     * When device is in invitedUsers, already registered text should be hidden
     */
    @Test
    public void testInvitedStatus_hidesAlreadyRegisteredText() {
        openEventDetails();
        onView(withId(R.id.alreadyRegisteredTextView))
                .inRoot(isDialog())
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }
}