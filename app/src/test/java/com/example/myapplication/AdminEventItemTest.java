package com.example.myapplication;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AdminEventItemTest {

    @Test
    public void constructorAndGetters_workCorrectly() {
        AdminEventItem item = new AdminEventItem(
                "event123",
                "Tech Workshop",
                "Gotham",
                "Karen Doe",
                "https://example.com/poster.jpg"
        );

        assertEquals("event123", item.getEventId());
        assertEquals("Tech Workshop", item.getTitle());
        assertEquals("Gotham", item.getLocation());
        assertEquals("Karen Doe", item.getOrganizerName());
        assertEquals("https://example.com/poster.jpg", item.getPosterURL());
    }
}