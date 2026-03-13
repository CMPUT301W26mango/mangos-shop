package com.example.myapplication;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class AdminBrowseEventsActivityTest {

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void browseEventsScreen_displaysImportantViews() {
        try (ActivityScenario<AdminBrowseEventsActivity> scenario =
                     ActivityScenario.launch(AdminBrowseEventsActivity.class)) {

            onView(withId(R.id.searchViewEvents)).check(matches(isDisplayed()));
            onView(withId(R.id.recyclerViewAdminEvents)).check(matches(isDisplayed()));
            onView(withId(R.id.buttonBrowseProfiles)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void clickingProfilesButton_opensBrowseProfilesScreen() {
        try (ActivityScenario<AdminBrowseEventsActivity> scenario =
                     ActivityScenario.launch(AdminBrowseEventsActivity.class)) {

            onView(withId(R.id.buttonBrowseProfiles)).perform(click());

            intended(hasComponent(AdminBrowseProfilesActivity.class.getName()));
        }
    }
}