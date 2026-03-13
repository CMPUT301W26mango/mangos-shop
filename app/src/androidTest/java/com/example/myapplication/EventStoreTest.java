package com.example.myapplication;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Integration tests for EventStore that write real data to Firestore and clean up after.
 * These tests require a real device or emulator with internet access.
 * Place in: app/src/androidTest/java/com/example/myapplication/
 * The following test file was written with the guidance of Claude AI
 * Prompt: "Guide me with writing tests for EventStore" March 13, 2026
 */
@RunWith(AndroidJUnit4.class)
public class EventStoreTest {

    private EventStore eventStore;
    private FirebaseFirestore db;
    private String testEventId;

    @Before
    public void setUp() {
        // Initialize Firebase
        FirebaseApp.initializeApp(
                InstrumentationRegistry.getInstrumentation().getTargetContext()
        );
        eventStore = new EventStore();
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Cleans up the test event from Firestore after each test.
     * This ensures no leftover data remains after tests run.
     */
    @After
    public void tearDown() throws InterruptedException {
        if (testEventId != null) {
            CountDownLatch latch = new CountDownLatch(1);
            db.collection("events")
                    .whereEqualTo("id", testEventId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (var doc : queryDocumentSnapshots.getDocuments()) {
                            doc.getReference().delete();
                        }
                        latch.countDown();
                    })
                    .addOnFailureListener(e -> latch.countDown());
            latch.await(5, TimeUnit.SECONDS);
        }
    }

    /**
     * Test that addEvent saves the event to Firestore and it can be retrieved.
     */
    @Test
    public void testAddEvent_savesToFirestore() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        // Create test event
        Event event = new Event();
        event.setTitle("Test Soccer Tryouts");
        event.setLocation("Old Trafford");
        event.setDescription("Integration test event");
        event.setCapacity(50);
        event.setOrganizerName("Test Name");
        event.setEventType("Sports");
        event.setGeolocationRequired(false);

        // Add to Firestore
        eventStore.addEvent(event);

        // Wait briefly for Firestore write
        Thread.sleep(3000);

        // Now query Firestore to verify it was saved
        db.collection("events")
                .whereEqualTo("title", "Test Soccer Tryouts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    assertFalse("Event should exist in Firestore",
                            queryDocumentSnapshots.isEmpty());

                    var doc = queryDocumentSnapshots.getDocuments().get(0);
                    testEventId = doc.getString("id"); // save for cleanup

                    assertEquals("Old Trafford", doc.getString("location"));
                    assertEquals("Test Name", doc.getString("organizerName"));
                    assertEquals("Sports", doc.getString("eventType"));
                    assertEquals(50L, (long) doc.getLong("capacity"));
                    assertFalse(Boolean.TRUE.equals(doc.getBoolean("geolocationRequired")));

                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    fail("Firestore query failed: " + e.getMessage());
                    latch.countDown();
                });

        latch.await(10, TimeUnit.SECONDS);
    }

    /**
     * Test that addEvent saves geolocationRequired as true when set.
     */
    @Test
    public void testAddEvent_geolocationRequired_true() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Event event = new Event();
        event.setTitle("Test Geo Event");
        event.setLocation("Old Trafford");
        event.setGeolocationRequired(true);

        eventStore.addEvent(event);
        Thread.sleep(3000);

        db.collection("events")
                .whereEqualTo("title", "Test Geo Event")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    assertFalse("Event should exist", queryDocumentSnapshots.isEmpty());
                    var doc = queryDocumentSnapshots.getDocuments().get(0);
                    testEventId = doc.getString("id");
                    assertTrue("Geolocation should be true",
                            Boolean.TRUE.equals(doc.getBoolean("geolocationRequired")));
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    fail("Firestore query failed: " + e.getMessage());
                    latch.countDown();
                });

        latch.await(10, TimeUnit.SECONDS);
    }

    /**
     * Test that delEventById removes the event from Firestore.
     */
    @Test
    public void testDelEventById_removesFromFirestore() throws InterruptedException {
        CountDownLatch addLatch = new CountDownLatch(1);

        // First add an event
        Event event = new Event();
        event.setTitle("Test Delete Event");
        event.setLocation("Old Trafford");

        eventStore.addEvent(event);
        Thread.sleep(3000);

        // Get the event ID
        db.collection("events")
                .whereEqualTo("title", "Test Delete Event")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        testEventId = queryDocumentSnapshots.getDocuments()
                                .get(0).getString("id");
                    }
                    addLatch.countDown();
                })
                .addOnFailureListener(e -> addLatch.countDown());

        addLatch.await(10, TimeUnit.SECONDS);
        assertNotNull("Event should have been created", testEventId);

        // Now delete it
        eventStore.delEventById(testEventId);
        Thread.sleep(3000);

        // Verify it's gone
        CountDownLatch deleteLatch = new CountDownLatch(1);
        db.collection("events")
                .whereEqualTo("id", testEventId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    assertTrue("Event should be deleted",
                            queryDocumentSnapshots.isEmpty());
                    testEventId = null; // already deleted, skip tearDown cleanup
                    deleteLatch.countDown();
                })
                .addOnFailureListener(e -> {
                    fail("Firestore query failed: " + e.getMessage());
                    deleteLatch.countDown();
                });

        deleteLatch.await(10, TimeUnit.SECONDS);
    }

    /**
     * Test that getEventById retrieves the correct event from Firestore.
     */
    @Test
    public void testGetEventById_returnsCorrectEvent() throws InterruptedException {
        CountDownLatch addLatch = new CountDownLatch(1);

        // Add event first
        Event event = new Event();
        event.setTitle("Test Get Event");
        event.setLocation("Old Trafford");
        event.setDescription("Get test");
        event.setCapacity(30);
        event.setOrganizerName("Test Name");

        eventStore.addEvent(event);
        Thread.sleep(3000);

        // Get the event ID
        db.collection("events")
                .whereEqualTo("title", "Test Get Event")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        testEventId = queryDocumentSnapshots.getDocuments()
                                .get(0).getString("id");
                    }
                    addLatch.countDown();
                })
                .addOnFailureListener(e -> addLatch.countDown());

        addLatch.await(10, TimeUnit.SECONDS);
        assertNotNull("Event should have been created", testEventId);

        // Now retrieve it using getEventById
        CountDownLatch getLatch = new CountDownLatch(1);
        eventStore.getEventById(testEventId, loadedEvent -> {
            assertNotNull("Loaded event should not be null", loadedEvent);
            assertEquals("Test Get Event", loadedEvent.getTitle());
            assertEquals("Old Trafford", loadedEvent.getLocation());
            assertEquals("Get test", loadedEvent.getDescription());
            assertEquals(30, loadedEvent.getCapacity());
            assertEquals("Test Name", loadedEvent.getOrganizerName());
            getLatch.countDown();
        });

        getLatch.await(10, TimeUnit.SECONDS);
    }
}