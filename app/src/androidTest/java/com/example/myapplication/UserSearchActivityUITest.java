package com.example.myapplication;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Intent;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * The following test file was written with the guidance of Gemini AI
 * Prompt: "Guide me with writing tests for Event creation page" April 2, 2026
 * Extended with validation, intent, and additional field coverage.
 */

@RunWith(AndroidJUnit4.class)
public class UserSearchActivityUITest {

    private static final String TEST_EVENT_ID = "test_usersearch_event_001";
    private static final String TEST_EVENT_NAME = "UofA Gala 2026";
    private static final String SEARCHABLE_USER_ID = "test_searchable_user_001";
    private static final String SEARCHABLE_USER_NAME = "Sayuj TestUser";

    private FirebaseFirestore db;
    private final List<String> cleanupPaths = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        db = FirebaseFirestore.getInstance();
        seedEvent(TEST_EVENT_ID, TEST_EVENT_NAME, true);
        seedUser(SEARCHABLE_USER_ID, SEARCHABLE_USER_NAME);
    }

    @After
    public void tearDown() throws Exception {
        CountDownLatch latch = new CountDownLatch(cleanupPaths.size());

        for (String path : cleanupPaths) {
            String[] p = path.split("/");
            db.collection(p[0]).document(p[1])
                    .delete()
                    .addOnCompleteListener(task -> latch.countDown());
        }

        latch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testUIInitialization() {
        try (ActivityScenario<UserSearchActivity> scenario =
                     ActivityScenario.launch(buildIntent(TEST_EVENT_ID, TEST_EVENT_NAME, true, false))) {

            onView(withId(R.id.et_search_bar)).check(matches(isDisplayed()));
            onView(withId(R.id.rv_search_results)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testSingleCharDoesNotTriggerSearch() {
        try (ActivityScenario<UserSearchActivity> scenario =
                     ActivityScenario.launch(buildIntent(TEST_EVENT_ID, TEST_EVENT_NAME, true, false))) {

            onView(withId(R.id.et_search_bar))
                    .perform(typeText("S"), closeSoftKeyboard());

            waitForRecyclerCount(scenario, 0, 3000);
            assertRecyclerCount(scenario, 0);
        }
    }

    @Test
    public void testThreeCharSearchReturnsSeededUser() {
        try (ActivityScenario<UserSearchActivity> scenario =
                     ActivityScenario.launch(buildIntent(TEST_EVENT_ID, TEST_EVENT_NAME, true, false))) {

            onView(withId(R.id.et_search_bar))
                    .perform(typeText("Say"), closeSoftKeyboard());

            waitForRecyclerMinCount(scenario, 1, 6000);
            onView(withText(SEARCHABLE_USER_NAME)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testPublicEventShowsMakeCoOrgOption() throws Exception {
        seedEvent("test_public_event_001", "Public Gala", false);

        try (ActivityScenario<UserSearchActivity> scenario =
                     ActivityScenario.launch(buildIntent("test_public_event_001", "Public Gala", false, false))) {

            onView(withId(R.id.et_search_bar))
                    .perform(typeText("Say"), closeSoftKeyboard());

            waitForRecyclerMinCount(scenario, 1, 6000);
            onView(withText(SEARCHABLE_USER_NAME)).perform(click());
            onView(withText("Make Co-Organizer")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testPrivateEventShowsBothOptions() {
        try (ActivityScenario<UserSearchActivity> scenario =
                     ActivityScenario.launch(buildIntent(TEST_EVENT_ID, TEST_EVENT_NAME, true, false))) {

            onView(withId(R.id.et_search_bar))
                    .perform(typeText("Say"), closeSoftKeyboard());

            waitForRecyclerMinCount(scenario, 1, 6000);
            onView(withText(SEARCHABLE_USER_NAME)).perform(click());

            onView(withText("Send Invite")).check(matches(isDisplayed()));
            onView(withText("Make Co-Organizer")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testSendInviteWritesToFirestore() throws Exception {
        try (ActivityScenario<UserSearchActivity> scenario =
                     ActivityScenario.launch(buildIntent(TEST_EVENT_ID, TEST_EVENT_NAME, true, false))) {

            onView(withId(R.id.et_search_bar))
                    .perform(typeText("Say"), closeSoftKeyboard());

            waitForRecyclerMinCount(scenario, 1, 6000);
            onView(withText(SEARCHABLE_USER_NAME)).perform(click());
            onView(withText("Send Invite")).perform(click());
        }

        waitForFirestoreInvite(TEST_EVENT_ID, SEARCHABLE_USER_ID, 8000);
    }

    @Test
    public void testClearSearchResetsResults() {
        try (ActivityScenario<UserSearchActivity> scenario =
                     ActivityScenario.launch(buildIntent(TEST_EVENT_ID, TEST_EVENT_NAME, true, false))) {

            onView(withId(R.id.et_search_bar))
                    .perform(typeText("Say"), closeSoftKeyboard());

            waitForRecyclerMinCount(scenario, 1, 6000);
            onView(withId(R.id.et_search_bar)).perform(clearText());

            waitForRecyclerCount(scenario, 0, 4000);
            assertRecyclerCount(scenario, 0);
        }
    }

    @Test
    public void testMissingEventNameDefaultsGracefully() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), UserSearchActivity.class);
        intent.putExtra("EVENT_ID", TEST_EVENT_ID);

        try (ActivityScenario<UserSearchActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.et_search_bar)).check(matches(isDisplayed()));
        }
    }

    // ---------------- Helpers ----------------

    private Intent buildIntent(String eventId, String eventName, boolean isPrivate, boolean isCoOrg) {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), UserSearchActivity.class);
        intent.putExtra("EVENT_ID", eventId);
        intent.putExtra("EVENT_NAME", eventName);
        intent.putExtra("IS_PRIVATE", isPrivate);
        intent.putExtra("IS_CO_ORG", isCoOrg);
        return intent;
    }

    private void seedEvent(String docId, String title, boolean isPrivate) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("isPrivate", isPrivate);
        data.put("capacity", 20);
        data.put("location", "Test Location");
        data.put("invitedUsers", new ArrayList<String>());
        data.put("coOrganizers", new ArrayList<String>());

        db.collection("events").document(docId)
                .set(data)
                .addOnCompleteListener(task -> latch.countDown());

        latch.await(5, TimeUnit.SECONDS);
        cleanupPaths.add("events/" + docId);
    }

    private void seedUser(String deviceId, String name) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);

        db.collection("users").document(deviceId)
                .set(data)
                .addOnCompleteListener(task -> latch.countDown());

        latch.await(5, TimeUnit.SECONDS);
        cleanupPaths.add("users/" + deviceId);
    }

    private void waitForRecyclerMinCount(ActivityScenario<UserSearchActivity> scenario,
                                         int minCount,
                                         long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < deadline) {
            final int[] count = {0};
            scenario.onActivity(activity -> {
                RecyclerView rv = activity.findViewById(R.id.rv_search_results);
                if (rv.getAdapter() != null) {
                    count[0] = rv.getAdapter().getItemCount();
                }
            });

            if (count[0] >= minCount) {
                return;
            }

            sleep(150);
        }

        throw new AssertionError("RecyclerView never reached min count: " + minCount);
    }

    private void waitForRecyclerCount(ActivityScenario<UserSearchActivity> scenario,
                                      int expectedCount,
                                      long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < deadline) {
            final int[] count = {0};
            scenario.onActivity(activity -> {
                RecyclerView rv = activity.findViewById(R.id.rv_search_results);
                if (rv.getAdapter() != null) {
                    count[0] = rv.getAdapter().getItemCount();
                }
            });

            if (count[0] == expectedCount) {
                return;
            }

            sleep(150);
        }

        throw new AssertionError("RecyclerView count never became: " + expectedCount);
    }

    private void assertRecyclerCount(ActivityScenario<UserSearchActivity> scenario, int expected) {
        final int[] count = {0};
        scenario.onActivity(activity -> {
            RecyclerView rv = activity.findViewById(R.id.rv_search_results);
            if (rv.getAdapter() != null) {
                count[0] = rv.getAdapter().getItemCount();
            }
        });

        assertEquals(expected, count[0]);
    }

    private void waitForFirestoreInvite(String eventId, String userId, long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < deadline) {
            CountDownLatch latch = new CountDownLatch(1);
            final boolean[] found = {false};

            db.collection("events").document(eventId).get()
                    .addOnSuccessListener(doc -> {
                        List<?> invited = (List<?>) doc.get("invitedUsers");
                        found[0] = invited != null && invited.contains(userId);
                        latch.countDown();
                    })
                    .addOnFailureListener(e -> latch.countDown());

            latch.await(2, TimeUnit.SECONDS);

            if (found[0]) {
                assertTrue(found[0]);
                return;
            }

            sleep(250);
        }

        throw new AssertionError("Firestore invite was never written.");
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }
}
