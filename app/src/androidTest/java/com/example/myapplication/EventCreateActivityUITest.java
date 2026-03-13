package com.example.myapplication;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class EventCreateActivityUITest {

    @Rule
    public ActivityScenarioRule<EventCreateActivity> activityRule =
            new ActivityScenarioRule<>(EventCreateActivity.class);


    @Test
    public void testActivityLaunches() {
        onView(withId(R.id.event_name_input)).check(matches(isDisplayed()));
    }

    @Test
    public void testCreateButtonIsDisplayed() {
        onView(withId(R.id.create_event_button)).perform(scrollTo());
        onView(withId(R.id.create_event_button)).check(matches(isDisplayed()));
    }


    @Test
    public void testEventNameFieldIsDisplayed() {
        onView(withId(R.id.event_name_input)).check(matches(isDisplayed()));
    }

    @Test
    public void testLocationFieldIsDisplayed() {
        onView(withId(R.id.location_input)).check(matches(isDisplayed()));
    }

    @Test
    public void testDescriptionFieldIsDisplayed() {
        onView(withId(R.id.event_description_input)).check(matches(isDisplayed()));
    }

    @Test
    public void testCapacityFieldIsDisplayed() {
        onView(withId(R.id.capacity_input)).perform(scrollTo());
        onView(withId(R.id.capacity_input)).check(matches(isDisplayed()));
    }

    @Test
    public void testPosterURLFieldIsDisplayed() {
        onView(withId(R.id.posterurl_input)).perform(scrollTo());
        onView(withId(R.id.posterurl_input)).check(matches(isDisplayed()));
    }

    @Test
    public void testEventTypeFieldIsDisplayed() {
        onView(withId(R.id.event_type)).perform(scrollTo());
        onView(withId(R.id.event_type)).check(matches(isDisplayed()));
    }

    @Test
    public void testOrganizerNameFieldIsDisplayed() {
        onView(withId(R.id.organizer_name)).perform(scrollTo());
        onView(withId(R.id.organizer_name)).check(matches(isDisplayed()));
    }


    @Test
    public void testTypeEventName() {
        onView(withId(R.id.event_name_input))
                .perform(typeText("Soccer Tryouts"), closeSoftKeyboard());
        onView(withId(R.id.event_name_input))
                .check(matches(withText("Soccer Tryouts")));
    }

    @Test
    public void testTypeLocation() {
        onView(withId(R.id.location_input))
                .perform(typeText("Old Trafford"), closeSoftKeyboard());
        onView(withId(R.id.location_input))
                .check(matches(withText("Old Trafford")));
    }

    @Test
    public void testTypeDescription() {
        onView(withId(R.id.event_description_input))
                .perform(typeText("A fun event for everyone"), closeSoftKeyboard());
        onView(withId(R.id.event_description_input))
                .check(matches(withText("A fun event for everyone")));
    }

    @Test
    public void testTypeCapacity() {
        onView(withId(R.id.capacity_input))
                .perform(scrollTo(), typeText("50"), closeSoftKeyboard());
        onView(withId(R.id.capacity_input))
                .check(matches(withText("50")));
    }

    @Test
    public void testTypePosterURL() {
        onView(withId(R.id.posterurl_input))
                .perform(scrollTo(), typeText("https://example.com/poster.jpg"), closeSoftKeyboard());
        onView(withId(R.id.posterurl_input))
                .check(matches(withText("https://example.com/poster.jpg")));
    }

    @Test
    public void testTypeEventType() {
        onView(withId(R.id.event_type))
                .perform(scrollTo(), typeText("Sports"), closeSoftKeyboard());
        onView(withId(R.id.event_type))
                .check(matches(withText("Sports")));
    }

    @Test
    public void testTypeOrganizerName() {
        onView(withId(R.id.organizer_name))
                .perform(scrollTo(), typeText("Test Name"), closeSoftKeyboard());
        onView(withId(R.id.organizer_name))
                .check(matches(withText("Test Name")));
    }


    @Test
    public void testCreateButtonClearsEventName() {
        onView(withId(R.id.event_name_input))
                .perform(typeText("Soccer Tryouts"), closeSoftKeyboard());
        onView(withId(R.id.create_event_button)).perform(scrollTo(), click());
        onView(withId(R.id.event_name_input)).check(matches(withText("")));
    }

    @Test
    public void testCreateButtonClearsLocation() {
        onView(withId(R.id.location_input))
                .perform(typeText("Old Trafford"), closeSoftKeyboard());
        onView(withId(R.id.create_event_button)).perform(scrollTo(), click());
        onView(withId(R.id.location_input)).check(matches(withText("")));
    }

    @Test
    public void testCreateButtonClearsCapacity() {
        onView(withId(R.id.capacity_input))
                .perform(scrollTo(), typeText("100"), closeSoftKeyboard());
        onView(withId(R.id.create_event_button)).perform(scrollTo(), click());
        onView(withId(R.id.capacity_input)).check(matches(withText("")));
    }

    @Test
    public void testCreateButtonClearsPosterURL() {
        onView(withId(R.id.posterurl_input))
                .perform(scrollTo(), typeText("https://example.com/poster.jpg"), closeSoftKeyboard());
        onView(withId(R.id.create_event_button)).perform(scrollTo(), click());
        onView(withId(R.id.posterurl_input)).check(matches(withText("")));
    }

    @Test
    public void testCreateButtonClearsEventType() {
        onView(withId(R.id.event_type))
                .perform(scrollTo(), typeText("Sports"), closeSoftKeyboard());
        onView(withId(R.id.create_event_button)).perform(scrollTo(), click());
        onView(withId(R.id.event_type)).check(matches(withText("")));
    }

    @Test
    public void testCreateButtonClearsOrganizerName() {
        onView(withId(R.id.organizer_name))
                .perform(scrollTo(), typeText("Test Name"), closeSoftKeyboard());
        onView(withId(R.id.create_event_button)).perform(scrollTo(), click());
        onView(withId(R.id.organizer_name)).check(matches(withText("")));
    }

    @Test
    public void testCreateFullEvent_clearsAllFields() {
        onView(withId(R.id.event_name_input))
                .perform(typeText("Soccer Tryouts"), closeSoftKeyboard());
        onView(withId(R.id.location_input))
                .perform(typeText("Old Trafford"), closeSoftKeyboard());
        onView(withId(R.id.event_description_input))
                .perform(typeText("A fun event for everyone"), closeSoftKeyboard());
        onView(withId(R.id.capacity_input))
                .perform(scrollTo(), typeText("50"), closeSoftKeyboard());
        onView(withId(R.id.posterurl_input))
                .perform(scrollTo(), typeText("https://example.com/poster.jpg"), closeSoftKeyboard());
        onView(withId(R.id.event_type))
                .perform(scrollTo(), typeText("Sports"), closeSoftKeyboard());
        onView(withId(R.id.organizer_name))
                .perform(scrollTo(), typeText("Test Name"), closeSoftKeyboard());
        onView(withId(R.id.create_event_button)).perform(scrollTo(), click());

        onView(withId(R.id.event_name_input)).check(matches(withText("")));
        onView(withId(R.id.location_input)).check(matches(withText("")));
        onView(withId(R.id.event_description_input)).check(matches(withText("")));
        onView(withId(R.id.capacity_input)).check(matches(withText("")));
        onView(withId(R.id.posterurl_input)).check(matches(withText("")));
    }
}