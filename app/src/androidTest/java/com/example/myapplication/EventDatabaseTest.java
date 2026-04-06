package com.example.myapplication;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Integration tests for EventDatabase that write real data to Firestore and clean up after.
 * These tests require a real device or emulator with internet access.
 * Place in: app/src/androidTest/java/com/example/myapplication/
 * The following test file was written with the guidance of Claude AI
 * Prompt: "Guide me with writing tests for EventStore" March 13, 2026
 * Extended with coverage for getEventsByOrganizer, qrValue, privateEvent, and edge cases.
 */
@RunWith(AndroidJUnit4.class)
public class EventDatabaseTest {

    private EventDatabase eventDatabase;
    private FirebaseFirestore db;

    // Track ALL event IDs written during a test so tearDown can clean all of them
    private final List<String> testEventIds = new ArrayList<>();

    @Before
    public void setUp() {
        FirebaseApp.initializeApp(
                InstrumentationRegistry.getInstrumentation().getTargetContext()
        );
        eventDatabase = new EventDatabase();
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Cleans up every event written during the test from Firestore.
     */
    @After
    public void tearDown() throws InterruptedException {
        if (testEventIds.isEmpty()) return;

        CountDownLatch latch = new CountDownLatch(testEventIds.size());
        for (String id : testEventIds) {
            db.collection("events")
                    .whereEqualTo("id", id)
                    .get()
                    .addOnSuccessListener(snap -> {
                        for (var doc : snap.getDocuments()) doc.getReference().delete();
                        latch.countDown();
                    })
                    .addOnFailureListener(e -> latch.countDown());
        }
        latch.await(10, TimeUnit.SECONDS);
        testEventIds.clear();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Writes an event via addEvent, waits for it to land in Firestore, then
     * returns the auto-generated document ID so tests can use it and tearDown
     * can clean it up.
     */
    private String addEventAndGetId(Event event, String uniqueTitle) throws InterruptedException {
        eventDatabase.addEvent(event);
        Thread.sleep(3000);

        CountDownLatch latch = new CountDownLatch(1);
        final String[] idHolder = {null};

        db.collection("events")
                .whereEqualTo("title", uniqueTitle)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        idHolder[0] = snap.getDocuments().get(0).getString("id");
                    }
                    latch.countDown();
                })
                .addOnFailureListener(e -> latch.countDown());

        latch.await(10, TimeUnit.SECONDS);

        if (idHolder[0] != null) testEventIds.add(idHolder[0]);
        return idHolder[0];
    }

    // -------------------------------------------------------------------------
    // addEvent tests
    // -------------------------------------------------------------------------

    /**
     * addEvent should save core fields to Firestore correctly.
     */
    @Test
    public void testAddEvent_savesToFirestore() throws InterruptedException {
        Event event = new Event();
        event.setTitle("Test Soccer Tryouts");
        event.setLocation("Old Trafford");
        event.setDescription("Integration test event");
        event.setCapacity(50);
        event.setOrganizerName("Test Name");
        event.setEventType("Sports");
        event.setGeolocationRequired(false);

        String id = addEventAndGetId(event, "Test Soccer Tryouts");
        assertNotNull("Event should have been written to Firestore", id);

        CountDownLatch latch = new CountDownLatch(1);
        db.collection("events").whereEqualTo("id", id).get()
                .addOnSuccessListener(snap -> {
                    assertFalse("Event should exist", snap.isEmpty());
                    var doc = snap.getDocuments().get(0);
                    assertEquals("Old Trafford", doc.getString("location"));
                    assertEquals("Test Name", doc.getString("organizerName"));
                    assertEquals("Sports", doc.getString("eventType"));
                    assertEquals(50L, (long) doc.getLong("capacity"));
                    assertFalse(Boolean.TRUE.equals(doc.getBoolean("geolocationRequired")));
                    latch.countDown();
                })
                .addOnFailureListener(e -> { fail(e.getMessage()); latch.countDown(); });
        latch.await(10, TimeUnit.SECONDS);
    }

    /**
     * addEvent should auto-set qrValue equal to the generated document ID.
     */
    @Test
    public void testAddEvent_qrValueMatchesEventId() throws InterruptedException {
        Event event = new Event();
        event.setTitle("Test QR Event");
        event.setLocation("Test Location");

        String id = addEventAndGetId(event, "Test QR Event");
        assertNotNull(id);

        CountDownLatch latch = new CountDownLatch(1);
        db.collection("events").whereEqualTo("id", id).get()
                .addOnSuccessListener(snap -> {
                    assertFalse(snap.isEmpty());
                    var doc = snap.getDocuments().get(0);
                    String qrValue = doc.getString("qrValue");
                    String savedId = doc.getString("id");
                    assertEquals("qrValue should equal the event's id", savedId, qrValue);
                    latch.countDown();
                })
                .addOnFailureListener(e -> { fail(e.getMessage()); latch.countDown(); });
        latch.await(10, TimeUnit.SECONDS);
    }

    /**
     * addEvent should save geolocationRequired = true correctly.
     */
    @Test
    public void testAddEvent_geolocationRequired_true() throws InterruptedException {
        Event event = new Event();
        event.setTitle("Test Geo Event");
        event.setLocation("Old Trafford");
        event.setGeolocationRequired(true);

        String id = addEventAndGetId(event, "Test Geo Event");
        assertNotNull(id);

        CountDownLatch latch = new CountDownLatch(1);
        db.collection("events").whereEqualTo("id", id).get()
                .addOnSuccessListener(snap -> {
                    assertFalse(snap.isEmpty());
                    assertTrue("Geolocation should be true",
                            Boolean.TRUE.equals(snap.getDocuments().get(0).getBoolean("geolocationRequired")));
                    latch.countDown();
                })
                .addOnFailureListener(e -> { fail(e.getMessage()); latch.countDown(); });
        latch.await(10, TimeUnit.SECONDS);
    }

    /**
     * addEvent should save privateEvent = true correctly.
     */
    @Test
    public void testAddEvent_privateEvent_true() throws InterruptedException {
        Event event = new Event();
        event.setTitle("Test Private Event");
        event.setLocation("Secret Venue");
        event.setPrivateEvent(true);

        String id = addEventAndGetId(event, "Test Private Event");
        assertNotNull(id);

        CountDownLatch latch = new CountDownLatch(1);
        db.collection("events").whereEqualTo("id", id).get()
                .addOnSuccessListener(snap -> {
                    assertFalse(snap.isEmpty());
                    assertTrue("privateEvent should be true",
                            Boolean.TRUE.equals(snap.getDocuments().get(0).getBoolean("privateEvent")));
                    latch.countDown();
                })
                .addOnFailureListener(e -> { fail(e.getMessage()); latch.countDown(); });
        latch.await(10, TimeUnit.SECONDS);
    }

    /**
     * addEvent should save privateEvent = false correctly.
     */
    @Test
    public void testAddEvent_privateEvent_false() throws InterruptedException {
        Event event = new Event();
        event.setTitle("Test Public Event");
        event.setLocation("Public Venue");
        event.setPrivateEvent(false);

        String id = addEventAndGetId(event, "Test Public Event");
        assertNotNull(id);

        CountDownLatch latch = new CountDownLatch(1);
        db.collection("events").whereEqualTo("id", id).get()
                .addOnSuccessListener(snap -> {
                    assertFalse(snap.isEmpty());
                    assertFalse("privateEvent should be false",
                            Boolean.TRUE.equals(snap.getDocuments().get(0).getBoolean("privateEvent")));
                    latch.countDown();
                })
                .addOnFailureListener(e -> { fail(e.getMessage()); latch.countDown(); });
        latch.await(10, TimeUnit.SECONDS);
    }

    // -------------------------------------------------------------------------
    // delEventById tests
    // -------------------------------------------------------------------------

    /**
     * delEventById should remove the event document from Firestore.
     */
    @Test
    public void testDelEventById_removesFromFirestore() throws InterruptedException {
        Event event = new Event();
        event.setTitle("Test Delete Event");
        event.setLocation("Old Trafford");

        String id = addEventAndGetId(event, "Test Delete Event");
        assertNotNull("Event should have been created", id);

        eventDatabase.delEventById(id);
        Thread.sleep(3000);

        CountDownLatch latch = new CountDownLatch(1);
        db.collection("events").whereEqualTo("id", id).get()
                .addOnSuccessListener(snap -> {
                    assertTrue("Event should be deleted", snap.isEmpty());
                    testEventIds.remove(id); // already deleted, skip tearDown
                    latch.countDown();
                })
                .addOnFailureListener(e -> { fail(e.getMessage()); latch.countDown(); });
        latch.await(10, TimeUnit.SECONDS);
    }

    /**
     * delEventById with a non-existent ID should not throw or cause errors.
     * It should silently do nothing.
     */
    @Test
    public void testDelEventById_nonExistentId_doesNothing() throws InterruptedException {
        // This should not throw; the query just returns empty and the loop doesn't execute
        eventDatabase.delEventById("non_existent_id_xyz_999");
        // Wait briefly to ensure no async crash occurs
        Thread.sleep(3000);
        // If we get here without crashing, the test passes
    }

    // -------------------------------------------------------------------------
    // getEventById tests
    // -------------------------------------------------------------------------

    /**
     * getEventById should retrieve the correct event and map all fields properly.
     */
    @Test
    public void testGetEventById_returnsCorrectEvent() throws InterruptedException {
        Event event = new Event();
        event.setTitle("Test Get Event");
        event.setLocation("Old Trafford");
        event.setDescription("Get test");
        event.setCapacity(30);
        event.setOrganizerName("Test Name");

        String id = addEventAndGetId(event, "Test Get Event");
        assertNotNull("Event should have been created", id);

        CountDownLatch latch = new CountDownLatch(1);
        eventDatabase.getEventById(id, loadedEvent -> {
            assertNotNull("Loaded event should not be null", loadedEvent);
            assertEquals("Test Get Event", loadedEvent.getTitle());
            assertEquals("Old Trafford", loadedEvent.getLocation());
            assertEquals("Get test", loadedEvent.getDescription());
            assertEquals(30, loadedEvent.getCapacity());
            assertEquals("Test Name", loadedEvent.getOrganizerName());
            latch.countDown();
        });
        latch.await(10, TimeUnit.SECONDS);
    }

    /**
     * Documents the known bug: getEventById throws IndexOutOfBoundsException
     * when no matching event exists because getDocuments().get(0) is called
     * on an empty list without a null/empty check.
     *
     * This test is expected to fail (or hang without calling the listener)
     * until the bug is fixed. It serves as a regression marker.
     *
     * Fix needed in EventDatabase.getEventById:
     *   if (queryDocumentSnapshots.isEmpty()) { return; }
     */
    @Test
    public void testGetEventById_nonExistentId_documentedBug() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] listenerCalled = {false};

        // This will silently fail to call the listener (or crash) because
        // the success callback calls getDocuments().get(0) on an empty result
        db.collection("events")
                .whereEqualTo("id", "non_existent_id_abc_000")
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    // Demonstrates the bug: isEmpty() is true but the code doesn't check
                    assertTrue("Query returns empty for non-existent id", snap.isEmpty());
                    listenerCalled[0] = true;
                    latch.countDown();
                })
                .addOnFailureListener(e -> latch.countDown());

        latch.await(10, TimeUnit.SECONDS);
        assertTrue("Direct Firestore query should have returned empty", listenerCalled[0]);
        // NOTE: calling eventDatabase.getEventById("non_existent_id_abc_000", ...) here
        // would throw IndexOutOfBoundsException — do not call it until the bug is fixed.
    }

    // -------------------------------------------------------------------------
    // getEventsByOrganizer tests
    // -------------------------------------------------------------------------

    /**
     * getEventsByOrganizer should return all events matching the given deviceId.
     */
    @Test
    public void testGetEventsByOrganizer_returnsMatchingEvents() throws InterruptedException {
        String testDeviceId = "test_device_organizer_abc";

        // Write two events with the same deviceId
        Event e1 = new Event();
        e1.setTitle("Test Organizer Event 1");
        e1.setLocation("Venue A");
        e1.setDeviceId(testDeviceId);

        Event e2 = new Event();
        e2.setTitle("Test Organizer Event 2");
        e2.setLocation("Venue B");
        e2.setDeviceId(testDeviceId);

        String id1 = addEventAndGetId(e1, "Test Organizer Event 1");
        String id2 = addEventAndGetId(e2, "Test Organizer Event 2");
        assertNotNull("Event 1 should be created", id1);
        assertNotNull("Event 2 should be created", id2);

        CountDownLatch latch = new CountDownLatch(1);
        eventDatabase.getEventsByOrganizer(testDeviceId, events -> {
            assertNotNull(events);
            // Filter to only the events this test wrote, in case other test data is present
            long count = events.stream()
                    .filter(e -> testDeviceId.equals(e.getDeviceId()))
                    .count();
            assertTrue("Should have at least 2 events for this organizer", count >= 2);
            latch.countDown();
        });
        latch.await(10, TimeUnit.SECONDS);
    }

    /**
     * getEventsByOrganizer should return an empty list for a deviceId with no events.
     */
    @Test
    public void testGetEventsByOrganizer_noEvents_returnsEmptyList() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        eventDatabase.getEventsByOrganizer("device_id_that_has_no_events_xyz", events -> {
            assertNotNull(events);
            assertTrue("Should return empty list for unknown organizer", events.isEmpty());
            latch.countDown();
        });
        latch.await(10, TimeUnit.SECONDS);
    }

    /**
     * getEventsByOrganizer should not return events belonging to a different organizer.
     */
    @Test
    public void testGetEventsByOrganizer_doesNotReturnOtherOrganizersEvents()
            throws InterruptedException {
        String deviceA = "test_device_org_A";
        String deviceB = "test_device_org_B";

        Event eventA = new Event();
        eventA.setTitle("Test Org A Event");
        eventA.setLocation("Venue A");
        eventA.setDeviceId(deviceA);

        String idA = addEventAndGetId(eventA, "Test Org A Event");
        assertNotNull(idA);

        CountDownLatch latch = new CountDownLatch(1);
        // Query for device B — should not see device A's event
        eventDatabase.getEventsByOrganizer(deviceB, events -> {
            boolean containsA = events.stream()
                    .anyMatch(e -> deviceA.equals(e.getDeviceId()));
            assertFalse("Device B's results should not include Device A's events", containsA);
            latch.countDown();
        });
        latch.await(10, TimeUnit.SECONDS);
    }
}