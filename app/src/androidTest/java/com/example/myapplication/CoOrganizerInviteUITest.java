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

/**
 * The following test file was written with the guidance of Claude AI
 * Prompt: "Write UI tests for US 01.09.01" April 2, 2026
 */
@RunWith(AndroidJUnit4.class)
public class CoOrganizerInviteUITest {

    private static final String CO_ORG_EVENT_ID = "1KbPgApIl85F0FX1im31";

    @Rule
    public GrantPermissionRule permissionRule =
            GrantPermissionRule.grant(
                    android.Manifest.permission.POST_NOTIFICATIONS,
                    android.Manifest.permission.CAMERA);

    @Rule
    public ActivityScenarioRule<EventListActivity> activityRule =
            new ActivityScenarioRule<>(EventListActivity.class);

    /**
     * Opens EventDetailsFragment with the co-organizer event and waits for Firestore to load
     */
    private void openEventDetails() {
        activityRule.getScenario().onActivity(activity -> {
            android.os.Bundle bundle = new android.os.Bundle();
            bundle.putString("eventId", CO_ORG_EVENT_ID);
            EventDetailsFragment fragment = new EventDetailsFragment();
            fragment.setArguments(bundle);
            fragment.show(activity.getSupportFragmentManager(), "eventDetails");
        });

        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * When device is in coOrganizers, co-organizer message should be visible
     * Covers US 01.09.01 — entrant sees they have been invited as co-organizer
     */
    @Test
    public void testCoOrgStatus_showsCoOrgMessage() {
        openEventDetails();
        onView(withId(R.id.tv_co_organizer_message))
                .inRoot(isDialog())
                .perform(ViewActions.scrollTo())
                .check(matches(isDisplayed()));
    }

    /**
     * When device is in coOrganizers, organizer view button should be visible
     * Covers US 01.09.01 — co-organizer can navigate to organizer view
     */
    @Test
    public void testCoOrgStatus_showsOrganizerViewButton() {
        openEventDetails();
        onView(withId(R.id.btn_go_to_organizer_view))
                .inRoot(isDialog())
                .perform(ViewActions.scrollTo())
                .check(matches(isDisplayed()));
    }

    /**
     * When device is in coOrganizers, register button should be hidden
     * Co-organizer should not be able to register as entrant
     */
    @Test
    public void testCoOrgStatus_hidesRegisterButton() {
        openEventDetails();
        onView(withId(R.id.registerBtn))
                .inRoot(isDialog())
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /**
     * When device is in coOrganizers, cancel button should be hidden
     */
    @Test
    public void testCoOrgStatus_hidesCancelButton() {
        openEventDetails();
        onView(withId(R.id.cancelRegisterBtn))
                .inRoot(isDialog())
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /**
     * When device is in coOrganizers, accept lottery button should be hidden
     */
    @Test
    public void testCoOrgStatus_hidesAcceptButton() {
        openEventDetails();
        onView(withId(R.id.acceptBtn))
                .inRoot(isDialog())
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /**
     * When device is in coOrganizers, decline lottery button should be hidden
     */
    @Test
    public void testCoOrgStatus_hidesDeclineButton() {
        openEventDetails();
        onView(withId(R.id.declineBtn))
                .inRoot(isDialog())
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /**
     * When device is in coOrganizers, accept invite button should be hidden
     */
    @Test
    public void testCoOrgStatus_hidesAcceptInviteButton() {
        openEventDetails();
        onView(withId(R.id.acceptInvBtn))
                .inRoot(isDialog())
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /**
     * When device is in coOrganizers, decline invite button should be hidden
     */
    @Test
    public void testCoOrgStatus_hidesDeclineInviteButton() {
        openEventDetails();
        onView(withId(R.id.declineInvBtn))
                .inRoot(isDialog())
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /**
     * When device is in coOrganizers, already registered text should be hidden
     */
    @Test
    public void testCoOrgStatus_hidesAlreadyRegisteredText() {
        openEventDetails();
        onView(withId(R.id.alreadyRegisteredTextView))
                .inRoot(isDialog())
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /**
     * When device is in coOrganizers, invited message should be hidden
     */
    @Test
    public void testCoOrgStatus_hidesInvitedMessage() {
        openEventDetails();
        onView(withId(R.id.tv_invited_message))
                .inRoot(isDialog())
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }
}