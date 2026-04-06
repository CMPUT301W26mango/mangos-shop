package com.example.myapplication;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.intent.Intents;
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
 * Instrumented UI tests for OrganizerDashboardActivity.
 *
 * Uses real Firebase Firestore — no mocks. Each test seeds the data it needs
 * before running, and tears it down afterward so tests are hermetic.
 *
 * IdlingResource is used instead of Thread.sleep() so Espresso waits for
 * Firestore callbacks naturally before asserting on the UI.
 *
*
 *  * The following test file was written with the guidance of Gemini AI
 *  * Prompt: "Guide me with writing tests for Event creation page" April 2, 2026
 *  * Extended with validation, intent, and additional field coverage.
 *
 */
@RunWith(AndroidJUnit4.class)
public class OrganizerDashboardActivityTest {

    // -----------------------------------------------------------------------
    // Test fixture IDs — use a clearly fake device ID so we don't collide
    // with real organizer data in Firestore
    // -----------------------------------------------------------------------
    private static final String TEST_DEVICE_ID   = "test_organizer_device_001";
    private static final String TEST_EVENT_TITLE = "Test Broadcast Event";
    private static final String TEST_RECIPIENT_ID = "test_recipient_device_001";

    private FirebaseFirestore db;
    private String seededEventId;                // filled in by seedEvent()
    private final List<String> cleanupPaths = new ArrayList<>(); // tracks docs to delete

    // Simple latch-based IdlingResource used when we need Firestore to settle
    private FirestoreLatchIdlingResource idlingResource;

    // -----------------------------------------------------------------------
    // Setup / Teardown
    // -----------------------------------------------------------------------

    @Before
    public void setUp() throws InterruptedException {
        Intents.init();
        db = FirebaseFirestore.getInstance();

        // Seed a user doc for the fake organizer (needed by Profiles.getDeviceId
        // indirectly — some paths read the "name" field)
        seedUser(TEST_DEVICE_ID, "Test Organizer");

        // Seed a recipient user so executeBlast() can write to their notifications
        seedUser(TEST_RECIPIENT_ID, "Test Recipient");

        // Seed one event owned by TEST_DEVICE_ID
        seededEventId = seedEvent(TEST_DEVICE_ID, TEST_EVENT_TITLE);

        // Seed the recipient onto the event's waiting list with status "waiting"
        seedWaitlistEntry(seededEventId, TEST_RECIPIENT_ID, "waiting");
    }

    @After
    public void tearDown() throws InterruptedException {
        Intents.release();

        if (idlingResource != null) {
            IdlingRegistry.getInstance().unregister(idlingResource);
        }

        // Delete everything we wrote so the DB stays clean
        CountDownLatch latch = new CountDownLatch(cleanupPaths.size());
        for (String path : cleanupPaths) {
            String[] parts = path.split("/");
            if (parts.length == 2) {
                db.collection(parts[0]).document(parts[1])
                        .delete()
                        .addOnCompleteListener(t -> latch.countDown());
            } else if (parts.length == 4) {
                db.collection(parts[0]).document(parts[1])
                        .collection(parts[2]).document(parts[3])
                        .delete()
                        .addOnCompleteListener(t -> latch.countDown());
            } else {
                latch.countDown(); // skip malformed — don't block forever
            }
        }
        latch.await(10, TimeUnit.SECONDS);
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /**
     * Verifies that the dashboard launches and shows its core UI components.
     * Does not depend on Firestore state — purely layout presence.
     */
    @Test
    public void testDashboardElementsPresence() {
        ActivityScenario.launch(OrganizerDashboardActivity.class);

        onView(withId(R.id.events_recycler_view)).check(matches(isDisplayed()));
        onView(withId(R.id.add_event)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_send_notifications)).check(matches(isDisplayed()));
    }

    /**
     * Clicking the FAB should navigate to EventCreateActivity.
     */
    @Test
    public void testNavigateToCreateEvent() {
        ActivityScenario.launch(OrganizerDashboardActivity.class);

        onView(withId(R.id.add_event)).perform(click());
        intended(hasComponent(EventCreateActivity.class.getName()));
    }

    /**
     * Clicking the profile nav item should navigate to UserProfileActivity.
     */
    @Test
    public void testNavigateToProfile() {
        ActivityScenario.launch(OrganizerDashboardActivity.class);

        onView(withId(R.id.nav_profile_organizer)).perform(click());
        intended(hasComponent(UserProfileActivity.class.getName()));
    }

    // -----------------------------------------------------------------------
    // Helpers — seed and cleanup
    // -----------------------------------------------------------------------

    /**
     * Seeds a minimal user document and registers it for cleanup.
     */
    private void seedUser(String deviceId, String name) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("deviceId", deviceId);

        db.collection("users").document(deviceId)
                .set(data)
                .addOnCompleteListener(t -> latch.countDown());

        latch.await(5, TimeUnit.SECONDS);
        cleanupPaths.add("users/" + deviceId);
    }

    /**
     * Seeds a minimal event document owned by the given organizer.
     * Returns the generated Firestore document ID.
     */
    private String seedEvent(String organizerDeviceId, String title) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final String[] docId = {null};

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("organizerId", organizerDeviceId);
        data.put("location", "Test Location");
        data.put("capacity", 10);

        db.collection("events")
                .add(data)
                .addOnSuccessListener(ref -> {
                    docId[0] = ref.getId();
                    latch.countDown();
                })
                .addOnFailureListener(e -> latch.countDown());

        latch.await(5, TimeUnit.SECONDS);
        assertNotNull("Event seeding failed", docId[0]);
        cleanupPaths.add("events/" + docId[0]);
        return docId[0];
    }

    /**
     * Seeds a waiting-list entry for a given event with the specified status.
     */
    private void seedWaitlistEntry(String eventId, String userId, String status) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Map<String, Object> data = new HashMap<>();
        data.put("status", status);

        db.collection("events").document(eventId)
                .collection("waitingList").document(userId)
                .set(data)
                .addOnCompleteListener(t -> latch.countDown());

        latch.await(5, TimeUnit.SECONDS);
        cleanupPaths.add("events/" + eventId + "/waitingList/" + userId);
    }

    /**
     * Deletes notification docs written during testBroadcastSendWritesToFirestore.
     * Called after assertions so we don't pollute the DB.
     */
    private void cleanupNotificationsForRecipient(String userId, String eventId) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        db.collection("users").document(userId)
                .collection("notifications")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(snap -> {
                    CountDownLatch inner = new CountDownLatch(snap.size());
                    if (snap.isEmpty()) { latch.countDown(); return; }
                    for (var doc : snap) {
                        doc.getReference().delete().addOnCompleteListener(t -> {
                            inner.countDown();
                            if (inner.getCount() == 0) latch.countDown();
                        });
                    }
                })
                .addOnFailureListener(e -> latch.countDown());
        latch.await(5, TimeUnit.SECONDS);
    }

    // -----------------------------------------------------------------------
    // Simple latch-based IdlingResource
    // -----------------------------------------------------------------------

    /**
     * A lightweight IdlingResource that lets callers signal "work is in progress"
     * via start(), and then becomes idle after a short fixed wait.
     *
     * This is simpler than wrapping every Firestore call because the latency
     * window is small and bounded (dialog appears within ~1 s on a real device).
     * For production test suites, consider a proper CountingIdlingResource tied
     * to your Firestore wrapper.
     */
    static class FirestoreLatchIdlingResource implements IdlingResource {
        private final String name;
        private volatile boolean isIdle = true;
        private ResourceCallback callback;

        FirestoreLatchIdlingResource(String name) {
            this.name = name;
        }

        void start() {
            isIdle = false;
            // Transition to idle after 1.5 s — enough for a local/emulator Firestore round-trip
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                isIdle = true;
                if (callback != null) callback.onTransitionToIdle();
            }, 1500);
        }

        /** Blocks the calling thread until the resource reports idle. */
        void waitForIdle() {
            long deadline = System.currentTimeMillis() + 3000;
            while (!isIdle && System.currentTimeMillis() < deadline) {
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            }
        }

        @Override public String getName() { return name; }
        @Override public boolean isIdleNow() { return isIdle; }
        @Override public void registerIdleTransitionCallback(ResourceCallback c) { this.callback = c; }
    }
}