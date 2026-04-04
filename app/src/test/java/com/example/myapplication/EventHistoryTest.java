package com.example.myapplication;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

/**
 * Asked Gemini AI to assist in helping write the tests.
 * The prompt used:
 * Can you please help me in ideas for what should be tested for this file <the file of code for this page> and generate some ideal test cases please.
 */

/**
 * Tests that the Event History data model:
 * Properly stores and retrieves historical event data (US 01.02.03).
 */
public class EventHistoryTest {

    private EventHistory testHistory1;
    private EventHistory testHistory2;
    private EventHistory testHistory3;

    @Before
    public void setUp() {
        testHistory1 = new EventHistory("evt_001", "Edmonton Coding Bootcamp", "Waiting");
        testHistory2 = new EventHistory("evt_002", "Soccer", "Selected");
        testHistory3 = new EventHistory("evt_003", "Basketball", "Cancelled");

    }

    /**
     * US 01.02.03: Tests that the history object correctly records if the user
     * was selected, waiting, or cancelled.
     */
    @Test
    public void testEventHistoryStoresCorrectStatus() {
        // Checking if my waiting list status saves properly
        Assert.assertEquals("evt_001", testHistory1.getEventId());
        Assert.assertEquals("Edmonton Coding Bootcamp", testHistory1.getEventName());
        Assert.assertEquals("Waiting", testHistory1.getStatus());

        // Checking if my selected status saves properly
        Assert.assertEquals("evt_002", testHistory2.getEventId());
        Assert.assertEquals("Soccer", testHistory2.getEventName());
        Assert.assertEquals("Selected", testHistory2.getStatus());

        // Checking if my cancelled status saves properly
        Assert.assertEquals("evt_003", testHistory3.getEventId());
        Assert.assertEquals("Basketball", testHistory3.getEventName());
        Assert.assertEquals("Cancelled", testHistory3.getStatus());
    }
}