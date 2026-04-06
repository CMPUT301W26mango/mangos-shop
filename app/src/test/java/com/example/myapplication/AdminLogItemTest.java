package com.example.myapplication;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for AdminLogItem model class.
 * Verifies constructor and getter behavior.
 */
public class AdminLogItemTest {

    /**
     * Test that the constructor correctly assigns values
     * and getters return the expected data.
     */
    @Test
    public void testConstructorAndGetters() {
        String message = "Event updated";
        String sender = "Organizer123";
        String time = "2026-04-06 14:30";

        AdminLogItem item = new AdminLogItem(message, sender, time);

        assertEquals(message, item.getMessage());
        assertEquals(sender, item.getSender());
        assertEquals(time, item.getTime());
    }

    /**
     * Test behavior with empty string values.
     */
    @Test
    public void testEmptyValues() {
        AdminLogItem item = new AdminLogItem("", "", "");

        assertEquals("", item.getMessage());
        assertEquals("", item.getSender());
        assertEquals("", item.getTime());
    }

    /**
     * Test behavior with null values.
     */
    @Test
    public void testNullValues() {
        AdminLogItem item = new AdminLogItem(null, null, null);

        assertNull(item.getMessage());
        assertNull(item.getSender());
        assertNull(item.getTime());
    }
}