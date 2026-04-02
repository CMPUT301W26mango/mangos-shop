package com.example.myapplication;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.google.firebase.Timestamp;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * The following test file was written with the guidance of Gemini AI
 * Prompt: "Guide me with writing tests for Event" April 2, 2026
 */


public class EventsTest {

    private Event event;

    @Before
    public void setUp() {
        // This runs before every single test to give us a fresh Event object
        event = new Event();
    }

    @Test
    public void testEmptyConstructor() {
        Event emptyEvent = new Event();
        assertNull("ID should be null on empty initialization", emptyEvent.getId());
    }

    @Test
    public void testParameterizedConstructor() {
        Timestamp start = new Timestamp(new Date());
        Timestamp end = new Timestamp(new Date(System.currentTimeMillis() + 10000));
        List<String> coOrgs = Arrays.asList("org1", "org2");
        List<String> invited = Arrays.asList("user1", "user2");

        Event fullEvent = new Event("123", "Test Title", "Desc", "Loc",
                start, end, "url", "qr", 100, "2026-04-10",
                "Organizer", "Music", 50, "device123", true, coOrgs, invited);

        assertEquals("123", fullEvent.getId());
        assertEquals("Test Title", fullEvent.getTitle());
        assertEquals("Loc", fullEvent.getLocation());
        assertEquals(100, fullEvent.getCapacity());
        assertTrue(fullEvent.getPrivateEvent());
        assertEquals(2, fullEvent.getCoOrganizers().size());
    }

    @Test
    public void testSetAndGetTitle() {
        event.setTitle("Mango Festival");
        assertEquals("Mango Festival", event.getTitle());
    }

    @Test
    public void testSetAndGetCapacity() {
        event.setCapacity(500);
        assertEquals(500, event.getCapacity());
    }

    @Test
    public void testListsSettersAndGetters() {
        List<String> invited = Arrays.asList("Sayuj", "Sami", "Aditya");
        event.setInvitedUsers(invited);

        assertNotNull(event.getInvitedUsers());
        assertEquals(3, event.getInvitedUsers().size());
        assertTrue(event.getInvitedUsers().contains("Sami"));
    }

    @Test
    public void testIsRegistrationOpen_WhenOpen() {
        // Set start time to 1 hour ago
        Timestamp pastStart = new Timestamp(new Date(System.currentTimeMillis() - 3600000));
        // Set end time to 1 hour in the future
        Timestamp futureEnd = new Timestamp(new Date(System.currentTimeMillis() + 3600000));

        event.setRegStart(pastStart);
        event.setRegEnd(futureEnd);

        assertTrue("Registration should be open", event.isRegistrationOpen());
    }

    @Test
    public void testIsRegistrationOpen_WhenNotStartedYet() {
        // Set start time to 1 hour in the future
        Timestamp futureStart = new Timestamp(new Date(System.currentTimeMillis() + 3600000));
        Timestamp futureEnd = new Timestamp(new Date(System.currentTimeMillis() + 7200000));

        event.setRegStart(futureStart);
        event.setRegEnd(futureEnd);

        assertFalse("Registration should be closed (hasn't started)", event.isRegistrationOpen());
    }

    @Test
    public void testIsRegistrationOpen_WhenAlreadyClosed() {
        // Set both times in the past
        Timestamp pastStart = new Timestamp(new Date(System.currentTimeMillis() - 7200000));
        Timestamp pastEnd = new Timestamp(new Date(System.currentTimeMillis() - 3600000));

        event.setRegStart(pastStart);
        event.setRegEnd(pastEnd);

        assertFalse("Registration should be closed (already ended)", event.isRegistrationOpen());
    }

    @Test
    public void testIsRegistrationOpen_WithNullDates() {
        event.setRegStart(null);
        event.setRegEnd(null);
        assertFalse("Registration should default to closed if dates are missing", event.isRegistrationOpen());
    }
}