package com.example.myapplication;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class AdminBrowseProfilesActivityTest {

    @Test
    public void browseProfilesScreen_displaysImportantViews() {
        try (ActivityScenario<AdminBrowseProfilesActivity> scenario =
                     ActivityScenario.launch(AdminBrowseProfilesActivity.class)) {

            onView(withId(R.id.searchViewProfiles)).check(matches(isDisplayed()));
            onView(withId(R.id.recyclerViewAdminProfiles)).check(matches(isDisplayed()));
        }
    }
}