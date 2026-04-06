package com.example.myapplication;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for AdminProfileItem model class.
 * Verifies that constructor and getters return correct values.
 */
public class AdminProfileItemTest {

    /**
     * Test that the parameterized constructor correctly assigns values
     * and getters return the expected data.
     */
    @Test
    public void testConstructorAndGetters() {
        String userId = "user123";
        String name = "John Doe";
        String email = "john@example.com";
        String role = "entrant";
        String profileImageUrl = "http://image.com/profile.jpg";

        AdminProfileItem item = new AdminProfileItem(
                userId, name, email, role, profileImageUrl
        );

        assertEquals(userId, item.getUserId());
        assertEquals(name, item.getName());
        assertEquals(email, item.getEmail());
        assertEquals(role, item.getRole());
        assertEquals(profileImageUrl, item.getProfileImageUrl());
    }

    /**
     * Test that the empty constructor creates an object with null fields.
     */
    @Test
    public void testEmptyConstructor() {
        AdminProfileItem item = new AdminProfileItem();

        assertNull(item.getUserId());
        assertNull(item.getName());
        assertNull(item.getEmail());
        assertNull(item.getRole());
        assertNull(item.getProfileImageUrl());
    }

    /**
     * Test behavior with realistic edge case values.
     */
    @Test
    public void testEdgeCases() {
        AdminProfileItem item = new AdminProfileItem(
                "", "", "", "", ""
        );

        assertEquals("", item.getUserId());
        assertEquals("", item.getName());
        assertEquals("", item.getEmail());
        assertEquals("", item.getRole());
        assertEquals("", item.getProfileImageUrl());
    }
}