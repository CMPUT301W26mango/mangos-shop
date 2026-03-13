package com.example.myapplication;


import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import android.content.Intent;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;


/**
 * This was written with the help of the espresso testing documentation <a href="https://developer.android.com/training/testing/espresso/lists#java">...</a>
 * */


@RunWith(AndroidJUnit4.class)
public class EventListTest {

    /**
     * Check if the recycler view opens when the app opens, this indicates that the event list is showna
     */
    @Rule
    public ActivityScenarioRule<EntrantAccount> activityRule =
            new ActivityScenarioRule<>(
                    new Intent(ApplicationProvider.getApplicationContext(), EventListActivity.class)
            );




    /**
     * This tests wether the recycle view with events are on the screen
     *
     * */
    @Test
    public void eventListRecyclerViewIsDisplayed() {
        onView(withId(R.id.recyclerViewEvents))
                .check(matches(isDisplayed()));
    }


    /**
     * This tests wether the event details screen pops up
     * Since the events take a second to load we manaully wait before we check
     * */
    @Test
    public void clickingEventOpensDetails() throws InterruptedException {
        Thread.sleep(3000); // wait 3 seconds for events to load

        onView(withId(R.id.recyclerViewEvents))
                .perform(click());

        Thread.sleep(1500); // wait for popup to appear

        onView(allOf(withText("Location"), isDisplayed()))
                .check(matches(isDisplayed()));

        onView(allOf(withText("Event Type"), isDisplayed()))
                .check(matches(isDisplayed()));
    }




}