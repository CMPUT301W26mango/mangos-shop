package com.example.myapplication;

import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

/**
 * This Test class Tests the comments
 * It presses the comment icon in an event and ensures the comment opens
 * It types in the comment box and see it gets posted
 * It tests that the users name exists in the box
 * This Test class was also written with the help of Google "Gemini" LLM
 * Prompt: Given these following scenarios recommend proper test cases to test them
 *  - click the comment button on the event, it should open the comments
 *  - Open click the comment button on the event, and we should be able to type something in the comment bar
 *  - The comment should be posted and that comment should appear in comments
 *  - The username of the person who made that comment should be shown
 * */
@RunWith(AndroidJUnit4.class)
public class CommentTest {

    @Rule
    public ActivityScenarioRule<EventListActivity> activityRule =
            new ActivityScenarioRule<>(EventListActivity.class);

    @Test
    public void testOpenCommentSection() {
        waitForNetwork(3000);

        // Click the comment button specifically on the first event card
        onView(withId(R.id.recyclerViewEvents))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.viewCommentsBtn)));

        waitForNetwork(1000);

        // Verify we successfully navigated to the CommentActivity by checking if the input bar exists
        onView(withId(R.id.commentInput)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonSendComment)).check(matches(isDisplayed()));
    }

    @Test
    public void testTypeAndPostComment() {
        waitForNetwork(3000);

        // Open the comments for the first event
        onView(withId(R.id.recyclerViewEvents))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.viewCommentsBtn)));

        waitForNetwork(1000);

        // Create a unique comment string so we don't accidentally verify an old test's comment
        String uniqueCommentText = "Automated Test Comment: " + UUID.randomUUID().toString().substring(0, 8);

        // Type something in the comment bar
        onView(withId(R.id.commentInput))
                .perform(typeText(uniqueCommentText), closeSoftKeyboard());

        // Click Send
        onView(withId(R.id.buttonSendComment)).perform(click());

        // Wait for Firestore to process the write and sync it back to the RecyclerView
        waitForNetwork(2000);


        onView(withId(R.id.recyclerViewComments))
                .perform(RecyclerViewActions.scrollTo(hasDescendant(withText(uniqueCommentText))));


        onView(withText(uniqueCommentText)).check(matches(isDisplayed()));
    }

    @Test
    public void testCommentDisplaysUsername() {
        waitForNetwork(3000);


        onView(withId(R.id.recyclerViewEvents))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.viewCommentsBtn)));

        waitForNetwork(1000);

        String uniqueCommentText = "Username Test: " + UUID.randomUUID().toString().substring(0, 8);


        onView(withId(R.id.commentInput)).perform(typeText(uniqueCommentText), closeSoftKeyboard());
        onView(withId(R.id.buttonSendComment)).perform(click());

        waitForNetwork(2000);

        onView(withId(R.id.recyclerViewComments))
                .perform(RecyclerViewActions.scrollTo(hasDescendant(withText(uniqueCommentText))));


        Matcher<View> specificCommentCard = allOf(
                withParent(withId(R.id.recyclerViewComments)),
                hasDescendant(withText(uniqueCommentText))
        );

        onView(allOf(
                withId(R.id.commentingUser),
                isDisplayed(),
                isDescendantOfA(specificCommentCard)
        )).check(matches(not(withText(""))));
    }

    private void waitForNetwork(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Custom ViewAction to click a specific button INSIDE a RecyclerView row.
     * Standard RecyclerViewActions.actionOnItemAtPosition clicks the entire row,
     * which would accidentally open EventDetailsFragment instead of CommentActivity.
     */
    public static ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return null;
            }

            @Override
            public String getDescription() {
                return "Click on a child view with a specified ID.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                v.performClick();
            }
        };
    }
}