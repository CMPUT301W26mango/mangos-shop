package com.example.myapplication;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for AdminImageItem model class.
 * Verifies constructor and getter behavior.
 */
public class AdminImageItemTest {

    /**
     * Test that the constructor correctly assigns values
     * and getters return the expected data.
     */
    @Test
    public void testConstructorAndGetters() {
        String eventId = "event123";
        String imageUrl = "http://image.com/event.jpg";

        AdminImageItem item = new AdminImageItem(eventId, imageUrl);

        assertEquals(eventId, item.getEventId());
        assertEquals(imageUrl, item.getImageUrl());
    }

    /**
     * Test behavior with empty string values.
     */
    @Test
    public void testEmptyValues() {
        AdminImageItem item = new AdminImageItem("", "");

        assertEquals("", item.getEventId());
        assertEquals("", item.getImageUrl());
    }

    /**
     * Test behavior with null values.
     */
    @Test
    public void testNullValues() {
        AdminImageItem item = new AdminImageItem(null, null);

        assertNull(item.getEventId());
        assertNull(item.getImageUrl());
    }
}