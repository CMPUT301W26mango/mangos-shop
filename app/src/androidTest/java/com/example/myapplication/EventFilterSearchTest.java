package com.example.myapplication;

import android.view.View;
import android.widget.DatePicker;
import android.widget.SearchView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

/**
 * This Test class Tests the filters
 * It opens the filters, enters the filters for event Type/Capacity/date
 * It closes the dialog and opens the event details for the remaining events to see if they match the filter
 * It also tests the search functionality
 * This Test class was also written with the help of Google "Gemini" LLM
 * Prompt: Given these following scenarios recommend proper test cases to test them
 *  - Scenario 1: Event Date Filters shows the events that fall in that particular date
 *  - Scenario 2: Events can also be filtered by Event Type
 *  - Scenario 3: Events can also be filtered by Event Capacity
 *  - Events can also be searched for by keyword, these keywords can exist in the description or of event name
 *  - All of the above can be worked alongside each other
 * */

@RunWith(AndroidJUnit4.class)
public class EventFilterSearchTest {
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.POST_NOTIFICATIONS,
            android.Manifest.permission.ACCESS_FINE_LOCATION
    );

    @Rule
    public ActivityScenarioRule<EventListActivity> activityRule =
            new ActivityScenarioRule<>(EventListActivity.class);

    @Test
    public void testFilterByCategoryOnly() {
        waitForNetwork(3000);

        // Open Filters and select Sports
        onView(withId(R.id.btnFilter)).perform(click());
        onView(withId(R.id.chipSports)).perform(click());
        onView(withId(R.id.applyFiltersBtn)).perform(click());

        waitForNetwork(1000);

        // Verify baseball championship (Sports) is visible
        onView(withId(R.id.recyclerViewEvents))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        waitForNetwork(1500);

        onView(withId(R.id.tv_event_title)).check(matches(withText("baseball championship")));
        onView(withId(R.id.tv_event_type)).check(matches(withText("Sports")));

        onView(withId(R.id.btn_close)).perform(click());
    }

    @Test
    public void testFilterByCapacityOnly() {
        waitForNetwork(3000);

        // Open Filters and enter Capacity bounds to target Event 2 (73-80)
        onView(withId(R.id.btnFilter)).perform(click());
        onView(withId(R.id.filterMinSpots)).perform(typeText("70"), closeSoftKeyboard());
        onView(withId(R.id.filterMaxSpots)).perform(typeText("85"), closeSoftKeyboard());
        onView(withId(R.id.applyFiltersBtn)).perform(click());

        waitForNetwork(1000);

        // Verify Real Madrid Vs Barcelona is present
        onView(withId(R.id.recyclerViewEvents))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        waitForNetwork(1500);

        onView(withId(R.id.tv_event_title)).check(matches(withText("Real Madrid Vs Barcelona")));

        onView(withId(R.id.btn_close)).perform(click());
    }

    @Test
    public void testFilterByDateOnly() {
        waitForNetwork(3000);

        onView(withId(R.id.btnFilter)).perform(click());

        onView(withId(R.id.filterMinEventDate)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName())))
                .perform(PickerActions.setDate(2026, 4, 16));
        onView(withId(android.R.id.button1)).perform(click()); // Clicks 'OK' on the dialog


        onView(withId(R.id.filterMaxEventDate)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName())))
                .perform(PickerActions.setDate(2026, 4, 18));
        onView(withId(android.R.id.button1)).perform(click());

        onView(withId(R.id.applyFiltersBtn)).perform(click());
        waitForNetwork(1000);

        onView(withId(R.id.recyclerViewEvents))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        waitForNetwork(1500);

        onView(withId(R.id.tv_event_title)).check(matches(withText("Real Madrid Vs Barcelona")));

        onView(withId(R.id.btn_close)).perform(click());
    }

    @Test
    public void testSearchOnly() {
        waitForNetwork(3000);


        onView(withId(R.id.eventsSearch)).perform(typeSearchViewText("Ronaldo"));
        waitForNetwork(1000);


        onView(withId(R.id.recyclerViewEvents))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        waitForNetwork(1500);

        onView(withId(R.id.tv_event_title)).check(matches(withText("Portugal Vs Argentia")));
        onView(withId(R.id.tv_event_description)).check(matches(withText(containsString("Ronaldo"))));

        onView(withId(R.id.btn_close)).perform(click());
    }

    @Test
    public void testCombinedSearchAndFilters() {
        waitForNetwork(3000);


        onView(withId(R.id.btnFilter)).perform(click());
        onView(withId(R.id.chipSports)).perform(click());
        onView(withId(R.id.filterMinSpots)).perform(typeText("100"), closeSoftKeyboard());
        onView(withId(R.id.applyFiltersBtn)).perform(click());


        onView(withId(R.id.eventsSearch)).perform(typeSearchViewText("Messi"));
        waitForNetwork(1000);


        onView(withId(R.id.recyclerViewEvents))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        waitForNetwork(1500);

        onView(withId(R.id.tv_event_title)).check(matches(withText("Portugal Vs Argentia")));

        onView(withId(R.id.btn_close)).perform(click());
    }

    private void waitForNetwork(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static ViewAction typeSearchViewText(final String text) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return allOf(isDisplayed(), isAssignableFrom(SearchView.class));
            }

            @Override
            public String getDescription() {
                return "Set query on SearchView";
            }

            @Override
            public void perform(UiController uiController, View view) {
                SearchView searchView = (SearchView) view;
                searchView.setQuery(text, true);
            }
        };
    }
}