package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/***
 * This Test File was written with the help of the OpenAI's ChatGPT LLM
 * Prompt: Given the criteias of: Events being displayed on open for entrant,
 * firebase reads work, and registers work generate the UI tests
 *
 */

@RunWith(AndroidJUnit4.class)
public class EventRegistrationTest {

    private ActivityScenario<EventListActivity> scenario;
    private String deviceId;
    private final String seededEventDocId = "ui_test_event_registration";
    private final String seededEventTitle = "UI Test Event Registration";
    private final String seededQrValue = "ui-test-qr-registration";

    @Before
    public void setUp() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();

        Profiles profiles = new Profiles();
        deviceId = profiles.getDeviceId(context);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> user = new HashMap<>();
        user.put("name", "Test Entrant");
        user.put("email", "test@example.com");
        user.put("phone", "7800000000");
        user.put("role", "Entrant");
        user.put("isAdmin", false);

        db.collection("users").document(deviceId).set(user);

        Map<String, Object> event = new HashMap<>();
        event.put("title", seededEventTitle);
        event.put("location", "Test Location");
        event.put("description", "Test Description");
        event.put("organizerName", "Test Organizer");
        event.put("eventType", "GENERAL");
        event.put("qrValue", seededQrValue);
        event.put("regStart", Timestamp.now());
        event.put("regEnd", new Timestamp(new Date(System.currentTimeMillis() + 86400000L)));
        event.put("maxWaitingListSize", 50L);
        event.put("capacity", 50L);

        db.collection("events").document(seededEventDocId).set(event);

        Thread.sleep(2000);

        scenario = ActivityScenario.launch(
                new Intent(context, EventListActivity.class)
        );

        Thread.sleep(3000);
    }

    @After
    public void tearDown() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (scenario != null) {
            scenario.close();
        }

        db.collection("events").document(seededEventDocId).delete();
        db.collection("users").document(deviceId).delete();

        Thread.sleep(1500);
    }

    @Test
    public void eventRegistrationButtonToggles() throws InterruptedException {
        onView(withText(seededEventTitle)).perform(click());
        Thread.sleep(2000);

        boolean registerVisible = isDialogButtonVisible(R.id.registerBtn);
        boolean cancelVisible = isDialogButtonVisible(R.id.cancelRegisterBtn);

        if (registerVisible) {
            onView(withId(R.id.registerBtn)).perform(click());
            Thread.sleep(1500);

            onView(withId(R.id.cancelRegisterBtn))
                    .check(matches(isDisplayed()));
        } else if (cancelVisible) {
            onView(withId(R.id.cancelRegisterBtn)).perform(click());
            Thread.sleep(1500);

            onView(withId(R.id.registerBtn))
                    .check(matches(isDisplayed()));
        } else {
            throw new AssertionError("Neither register nor cancel register button was visible");
        }
    }

    private boolean isDialogButtonVisible(int buttonId) {
        AtomicBoolean visible = new AtomicBoolean(false);

        scenario.onActivity(activity -> {
            Fragment fragment =
                    activity.getSupportFragmentManager().findFragmentByTag("eventDetails");

            if (fragment instanceof EventDetailsFragment) {
                EventDetailsFragment eventDetailsFragment = (EventDetailsFragment) fragment;
                if (eventDetailsFragment.getDialog() != null) {
                    View dialogView = eventDetailsFragment.getDialog().findViewById(buttonId);
                    visible.set(dialogView != null && dialogView.getVisibility() == View.VISIBLE);
                }
            }
        });

        return visible.get();
    }
}
