package com.example.myapplication;

import com.google.firebase.Timestamp;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for AdminEventItem model class.
 * Verifies constructor and getter behavior.
 */
public class AdminEventItemTest {

    /**
     * Test that the parameterized constructor correctly assigns values
     * and getters return the expected data.
     */
    @Test
    public void testConstructorAndGetters() {
        String eventId = "event123";
        String title = "Test Event";
        String location = "Edmonton";
        String organizerName = "Alice";
        String posterURL = "http://image.com/poster.jpg";
        String organizerId = "org123";
        Timestamp timestamp = new Timestamp(1000, 0);

        AdminEventItem item = new AdminEventItem(
                eventId,
                title,
                location,
                organizerName,
                posterURL,
                organizerId,
                timestamp
        );

        assertEquals(eventId, item.getEventId());
        assertEquals(title, item.getTitle());
        assertEquals(location, item.getLocation());
        assertEquals(organizerName, item.getOrganizerName());
        assertEquals(posterURL, item.getPosterURL());
        assertEquals(organizerId, item.getOrganizerId());
        assertEquals(timestamp, item.getRegEnd());

        assertNull(item.getDeadline());
    }

    /**
     * Test that the empty constructor initializes fields to null.
     */
    @Test
    public void testEmptyConstructor() {
        AdminEventItem item = new AdminEventItem();

        assertNull(item.getEventId());
        assertNull(item.getTitle());
        assertNull(item.getLocation());
        assertNull(item.getOrganizerName());
        assertNull(item.getPosterURL());
        assertNull(item.getOrganizerId());
        assertNull(item.getRegEnd());
        assertNull(item.getDeadline());
    }

    /**
     * Test behavior with empty string values and a valid timestamp.
     */
    @Test
    public void testEdgeCases() {
        Timestamp timestamp = new Timestamp(0, 0);

        AdminEventItem item = new AdminEventItem(
                "", "", "", "", "", "", timestamp
        );

        assertEquals("", item.getEventId());
        assertEquals("", item.getTitle());
        assertEquals("", item.getLocation());
        assertEquals("", item.getOrganizerName());
        assertEquals("", item.getPosterURL());
        assertEquals("", item.getOrganizerId());
        assertEquals(timestamp, item.getRegEnd());
    }
}