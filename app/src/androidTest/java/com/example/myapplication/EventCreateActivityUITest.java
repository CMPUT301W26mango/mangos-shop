package com.example.myapplication;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasType;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * The following test file was written with the guidance of Gemini AI
 * Prompt: "Guide me with writing tests for Event creation page" April 2, 2026
 * Extended with validation, intent, and additional field coverage.
 */
@RunWith(AndroidJUnit4.class)
public class EventCreateActivityUITest {

    @Rule
    public ActivityScenarioRule<EventCreateActivity> activityRule =
            new ActivityScenarioRule<>(EventCreateActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    // -------------------------------------------------------------------------
    // UI Presence & Initial State
    // -------------------------------------------------------------------------

    /**
     * Verifies essential fields and the correct title are shown in Create mode.
     */
    @Test
    public void testUIElementsPresence() {
        onView(withId(R.id.create_event_title))
                .check(matches(withText("Create Your Event")));

        onView(withId(R.id.event_name_input)).check(matches(isDisplayed()));
        onView(withId(R.id.location_input)).check(matches(isDisplayed()));

        onView(withId(R.id.create_event_button))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    /**
     * Upload button should show correct initial text.
     */
    @Test
    public void testUploadButtonInitialState() {
        onView(withId(R.id.upload_poster_button))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
                .check(matches(withText("Upload Poster Image")));
    }

    /**
     * Poster preview should be hidden before any image is selected.
     */
    @Test
    public void testPosterPreview_hiddenByDefault() {
        onView(withId(R.id.poster_image_preview))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /**
     * Upload poster button should be clickable.
     */
    @Test
    public void testUploadPosterButton_isClickable() {
        onView(withId(R.id.upload_poster_button))
                .perform(scrollTo())
                .check(matches(isClickable()));
    }

    /**
     * Create button should be visible before any upload starts.
     */
    @Test
    public void testCreateButton_visibleBeforeUpload() {
        onView(withId(R.id.create_event_button))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    /**
     * Both switches should be off by default.
     */
    @Test
    public void testSwitches_defaultOff() {
        onView(withId(R.id.switchGeolocation))
                .perform(scrollTo())
                .check(matches(isNotChecked()));

        onView(withId(R.id.switch_private_event))
                .perform(scrollTo())
                .check(matches(isNotChecked()));
    }

    // -------------------------------------------------------------------------
    // Form Input
    // -------------------------------------------------------------------------

    /**
     * Typing into text fields should update their content correctly.
     */
    @Test
    public void testFormInput_eventNameAndCapacity() {
        onView(withId(R.id.event_name_input))
                .perform(typeText("Soccer Finals"), closeSoftKeyboard());
        onView(withId(R.id.event_name_input))
                .check(matches(withText("Soccer Finals")));

        onView(withId(R.id.capacity_input))
                .perform(scrollTo(), typeText("50"), closeSoftKeyboard());
        onView(withId(R.id.capacity_input))
                .check(matches(withText("50")));
    }

    /**
     * Description field should accept and retain typed input.
     */
    @Test
    public void testFormInput_description() {
        onView(withId(R.id.event_description_input))
                .perform(scrollTo(), typeText("This is a test description"), closeSoftKeyboard());
        onView(withId(R.id.event_description_input))
                .check(matches(withText("This is a test description")));
    }

    /**
     * Waiting list input should accept numeric input.
     */
    @Test
    public void testFormInput_waitingList() {
        onView(withId(R.id.max_waitingList_size))
                .perform(scrollTo(), typeText("20"), closeSoftKeyboard());
        onView(withId(R.id.max_waitingList_size))
                .check(matches(withText("20")));
    }

    // -------------------------------------------------------------------------
    // Switch Toggles
    // -------------------------------------------------------------------------

    /**
     * Tapping the geolocation switch should turn it on.
     */
    @Test
    public void testGeolocationSwitch_toggleOn() {
        onView(withId(R.id.switchGeolocation))
                .perform(scrollTo(), click());
        onView(withId(R.id.switchGeolocation))
                .check(matches(isChecked()));
    }

    /**
     * Tapping the geolocation switch twice should return it to off.
     */
    @Test
    public void testGeolocationSwitch_toggleOffAgain() {
        onView(withId(R.id.switchGeolocation))
                .perform(scrollTo(), click(), click());
        onView(withId(R.id.switchGeolocation))
                .check(matches(isNotChecked()));
    }

    /**
     * Tapping the private event switch should turn it on.
     */
    @Test
    public void testPrivateSwitch_toggleOn() {
        onView(withId(R.id.switch_private_event))
                .perform(scrollTo(), click());
        onView(withId(R.id.switch_private_event))
                .check(matches(isChecked()));
    }

    // -------------------------------------------------------------------------
    // Validation — empty field errors
    // -------------------------------------------------------------------------

    /**
     * Tapping Create with no event name should show an error on that field.
     */
    @Test
    public void testValidation_emptyEventName_showsError() {
        onView(withId(R.id.create_event_button))
                .perform(scrollTo(), click());
        onView(withId(R.id.event_name_input))
                .check(matches(hasErrorText("Event name is required")));
    }

    /**
     * Filling in event name but leaving location blank should show location error.
     */
    @Test
    public void testValidation_emptyLocation_showsError() {
        onView(withId(R.id.event_name_input))
                .perform(typeText("Test Event"), closeSoftKeyboard());

        onView(withId(R.id.create_event_button))
                .perform(scrollTo(), click());

        onView(withId(R.id.location_input))
                .check(matches(hasErrorText("Location is required")));
    }



    // -------------------------------------------------------------------------
    // Intent — image picker
    // -------------------------------------------------------------------------

    /**
     * Tapping the upload button should fire an ACTION_GET_CONTENT intent for images.
     */
    @Test
    public void testUploadButton_firesImagePickerIntent() {
        // Stub out the image picker so it returns immediately without opening a real picker
        intending(allOf(hasAction(Intent.ACTION_GET_CONTENT), hasType("image/*")))
                .respondWith(new Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null));

        onView(withId(R.id.upload_poster_button))
                .perform(scrollTo(), click());

        intended(allOf(hasAction(Intent.ACTION_GET_CONTENT), hasType("image/*")));
    }

    // -------------------------------------------------------------------------
    // Edit Mode
    // -------------------------------------------------------------------------

    /**
     * Launching in EDIT mode should update the title and button text in the UI.
     * Note: Firebase prefill of field values is tested via integration tests
     * against a real Firestore instance, not here.
     */
    @Test
    public void testEditModeUI_titleAndButtonText() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, EventCreateActivity.class);
        intent.putExtra("MODE", "EDIT");
        intent.putExtra("EVENT_ID", "mock_id_123");

        try (ActivityScenario<EventCreateActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.create_event_title))
                    .check(matches(withText("Edit Your Event")));

            onView(withId(R.id.create_event_button))
                    .perform(scrollTo())
                    .check(matches(withText("Update")));
        }
    }
}