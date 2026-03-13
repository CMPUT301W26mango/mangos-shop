package com.example.myapplication;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
* Asked Gemini AI to assist in helping write the tests.
* The prompt used: 
* Can you please help me in ideas for what should be tested for this file <the file of code for this page> and generate some ideal test cases please.
*/

/**
 * Tests that the Profiles class:
 * Retrieves the correct unique Device ID.
 * Handles database calls without crashing.
 */
@RunWith(AndroidJUnit4.class)
public class ProfilesTest {
    private Profiles profiles;
    private Context context;

    /**
     * Sets up the application context and makes a new Profiles class before each test runs. (empty)
     */
    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        profiles = new Profiles();
    }

    /**
     * Tests that the getDeviceId method successfully works and returns a value (not empty or null)
     */
    @Test
    public void testGetDeviceIdReturnsValidString() {
        String deviceId = profiles.getDeviceId(context);

        Assert.assertNotNull("Device ID should not be null", deviceId);
        Assert.assertFalse("Device ID should not be empty", deviceId.isEmpty());
    }

    /**
     * Tests that the fetchUserRole method can be called with a test ID without throwing an error.
     */
    @Test
    public void testFetchUserRoleHandlesDummyIdSafely() {
        // Making sure that calling this doesn't crash the app
        try {
            profiles.fetchUserRole("testID", result -> {
                // If it successfully completes, it should return null for a fake ID
                Assert.assertNull(result);
            });
        } catch (Exception e) {
            Assert.fail("Calling fetchUserRole threw an unexpected exception: " + e.getMessage());
        }
    }
}
