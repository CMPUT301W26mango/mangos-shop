package com.example.myapplication;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AdminProfileItemTest {

    @Test
    public void constructorAndGetters_workCorrectly() {
        AdminProfileItem item = new AdminProfileItem(
                "user123",
                "John Smith",
                "john@email.com",
                "Entrant"
        );

        assertEquals("user123", item.getUserId());
        assertEquals("John Smith", item.getName());
        assertEquals("john@email.com", item.getEmail());
        assertEquals("Entrant", item.getRole());
    }
}