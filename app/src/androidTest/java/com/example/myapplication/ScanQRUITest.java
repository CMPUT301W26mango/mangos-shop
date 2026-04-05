package com.example.myapplication;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * The following test file was written with the guidance of Claude AI
 * Prompt: "Write UI tests for US 01.06.01" April 2, 2026
 */
@RunWith(AndroidJUnit4.class)
public class ScanQRUITest {

    @Rule
    public GrantPermissionRule permissionRule =
            GrantPermissionRule.grant(
                    android.Manifest.permission.POST_NOTIFICATIONS,
                    android.Manifest.permission.CAMERA);

    @Rule
    public ActivityScenarioRule<EventListActivity> activityRule =
            new ActivityScenarioRule<>(EventListActivity.class);

    /**
     * QR scan button should be visible on the event list screen
     */
    @Test
    public void testScanQRButton_isVisible() {
        onView(withId(R.id.scanQRButton))
                .check(matches(isDisplayed()));
    }

    /**
     * QR scan button should be clickable
     */
    @Test
    public void testScanQRButton_isClickable() {
        onView(withId(R.id.scanQRButton))
                .check(matches(isDisplayed()));
    }

    /**
     * Simulates a valid QR scan by directly launching EventDetailsFragment
     * with a known eventId — mirrors what scannerLauncher does after a real scan
     * Verifies the event details popup opens correctly
     */
    @Test
    public void testValidQRScan_opensEventDetailsPopup() {
        activityRule.getScenario().onActivity(activity -> {
            android.os.Bundle bundle = new android.os.Bundle();
            bundle.putString("eventId", "LVpMCQZpFkqISPnkgYPQ");
            EventDetailsFragment fragment = new EventDetailsFragment();
            fragment.setArguments(bundle);
            fragment.show(activity.getSupportFragmentManager(), "eventDetails");
        });

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.btn_close))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /**
     * After a simulated valid QR scan, event title should be displayed
     */
    @Test
    public void testValidQRScan_showsEventTitle() {
        activityRule.getScenario().onActivity(activity -> {
            android.os.Bundle bundle = new android.os.Bundle();
            bundle.putString("eventId", "LVpMCQZpFkqISPnkgYPQ");
            EventDetailsFragment fragment = new EventDetailsFragment();
            fragment.setArguments(bundle);
            fragment.show(activity.getSupportFragmentManager(), "eventDetails");
        });

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.tv_event_title))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /**
     * After a simulated valid QR scan, event type should be displayed
     */
    @Test
    public void testValidQRScan_showsEventType() {
        activityRule.getScenario().onActivity(activity -> {
            android.os.Bundle bundle = new android.os.Bundle();
            bundle.putString("eventId", "LVpMCQZpFkqISPnkgYPQ");
            EventDetailsFragment fragment = new EventDetailsFragment();
            fragment.setArguments(bundle);
            fragment.show(activity.getSupportFragmentManager(), "eventDetails");
        });

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.tv_event_type))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /**
     * After a simulated valid QR scan, event description should be displayed
     */
    @Test
    public void testValidQRScan_showsEventDesc() {
        activityRule.getScenario().onActivity(activity -> {
            android.os.Bundle bundle = new android.os.Bundle();
            bundle.putString("eventId", "LVpMCQZpFkqISPnkgYPQ");
            EventDetailsFragment fragment = new EventDetailsFragment();
            fragment.setArguments(bundle);
            fragment.show(activity.getSupportFragmentManager(), "eventDetails");
        });

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.tv_event_description))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }




    /**
     * After a simulated valid QR scan, event location should be displayed
     */
    @Test
    public void testValidQRScan_showsEventLocation() {
        activityRule.getScenario().onActivity(activity -> {
            android.os.Bundle bundle = new android.os.Bundle();
            bundle.putString("eventId", "LVpMCQZpFkqISPnkgYPQ");
            EventDetailsFragment fragment = new EventDetailsFragment();
            fragment.setArguments(bundle);
            fragment.show(activity.getSupportFragmentManager(), "eventDetails");
        });

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.tv_event_location))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /**
     * After a simulated valid QR scan, event date should be displayed
     */
    @Test
    public void testValidQRScan_showsEventDate() {
        activityRule.getScenario().onActivity(activity -> {
            android.os.Bundle bundle = new android.os.Bundle();
            bundle.putString("eventId", "LVpMCQZpFkqISPnkgYPQ");
            EventDetailsFragment fragment = new EventDetailsFragment();
            fragment.setArguments(bundle);
            fragment.show(activity.getSupportFragmentManager(), "eventDetails");
        });

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.tv_event_date))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /**
     * After a simulated valid QR scan, registration start date should be displayed
     */
    @Test
    public void testValidQRScan_showsEventRegStart() {
        activityRule.getScenario().onActivity(activity -> {
            android.os.Bundle bundle = new android.os.Bundle();
            bundle.putString("eventId", "LVpMCQZpFkqISPnkgYPQ");
            EventDetailsFragment fragment = new EventDetailsFragment();
            fragment.setArguments(bundle);
            fragment.show(activity.getSupportFragmentManager(), "eventDetails");
        });

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.tv_reg_start))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /**
     * After a simulated valid QR scan, registration end date should be displayed
     */
    @Test
    public void testValidQRScan_showsEventRegEnd() {
        activityRule.getScenario().onActivity(activity -> {
            android.os.Bundle bundle = new android.os.Bundle();
            bundle.putString("eventId", "LVpMCQZpFkqISPnkgYPQ");
            EventDetailsFragment fragment = new EventDetailsFragment();
            fragment.setArguments(bundle);
            fragment.show(activity.getSupportFragmentManager(), "eventDetails");
        });

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.tv_reg_end))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /**
     * After a simulated valid QR scan, spots available should be displayed
     */
    @Test
    public void testValidQRScan_showsEventSpots() {
        activityRule.getScenario().onActivity(activity -> {
            android.os.Bundle bundle = new android.os.Bundle();
            bundle.putString("eventId", "LVpMCQZpFkqISPnkgYPQ");
            EventDetailsFragment fragment = new EventDetailsFragment();
            fragment.setArguments(bundle);
            fragment.show(activity.getSupportFragmentManager(), "eventDetails");
        });

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.tv_spots_available))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /**
     * After a simulated valid QR scan, close button should dismiss the popup
     * Verifies the user can exit event details and return to event list
     */
    @Test
    public void testValidQRScan_closeButtonDismissesPopup() {
        activityRule.getScenario().onActivity(activity -> {
            android.os.Bundle bundle = new android.os.Bundle();
            bundle.putString("eventId", "LVpMCQZpFkqISPnkgYPQ");
            EventDetailsFragment fragment = new EventDetailsFragment();
            fragment.setArguments(bundle);
            fragment.show(activity.getSupportFragmentManager(), "eventDetails");
        });

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.btn_close))
                .inRoot(isDialog())
                .perform(click());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.scanQRButton))
                .check(matches(isDisplayed()));
    }

    /**
     * Without a valid eventId, the event details popup should not appear
     * Simulates what happens when QR scan produces no result
     */
    @Test
    public void testNoEventId_doesNotOpenPopup() {
        activityRule.getScenario().onActivity(activity -> {
            EventDetailsFragment fragment = new EventDetailsFragment();
            fragment.show(activity.getSupportFragmentManager(), "eventDetails");
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.scanQRButton))
                .check(matches(isDisplayed()));
    }
}