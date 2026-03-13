package com.example.myapplication;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

/**
 * Tests that the UserProfiles class:
 * Initializes default values correctly using the empty constructor.
 * Assigns values properly.
 * Updates and retrieves data from the getters and setters.
 */
public class UserProfilesTest {

    private UserProfiles testUser;

    /**
     * Sets up a new UserProfile before each test runs.
     */
    @Before
    public void setUp() {
        testUser = new UserProfiles("Testing Mango", "test@user.com", "1234567890", "Entrant");
    }

    /**
     * Tests that the parameter constructor correctly gives the passed strings,
     * and sets the admin security boolean flags to false by default.
     */
    @Test
    public void testParameterizedConstructorSetsCorrectValues() {
        Assert.assertEquals("Testing Mango", testUser.getName());
        Assert.assertEquals("test@user.com", testUser.getEmail());
        Assert.assertEquals("1234567890", testUser.getPhone());
        Assert.assertEquals("Entrant", testUser.getRole());

        // Security checks
        Assert.assertFalse(testUser.getIsAdmin());
        Assert.assertFalse(testUser.getAdminRequested());
    }

    /**
     * Tests that the getters and setters accurately update the user's basic information.
     */
    @Test
    public void testGettersAndSettersUpdateInformation() {
        testUser.setName("First Last");
        testUser.setEmail("first@last.com");
        testUser.setPhone("0987654321");
        testUser.setRole("Organizer");

        Assert.assertEquals("First Last", testUser.getName());
        Assert.assertEquals("first@last.com", testUser.getEmail());
        Assert.assertEquals("0987654321", testUser.getPhone());
        Assert.assertEquals("Organizer", testUser.getRole());
    }

    /**
     * Tests the specific admin boolean logic to ensure the security flags
     * can be safely toggled and retrieved.
     */
    @Test
    public void testAdminSecurityFlagsCanBeToggled() {
        // Test admin request flag
        testUser.setAdminRequested(true);
        Assert.assertTrue(testUser.getAdminRequested());
        Assert.assertFalse(testUser.getIsAdmin()); // Ensure it doesn't give auto admin

        // Test actual admin grant flag
        testUser.setIsAdmin(true);
        Assert.assertTrue(testUser.getIsAdmin());
    }

    /**
     * Tests that the empty constructor creates a valid, non-null object
     * so Firestore doesn't crash
     */
    @Test
    public void testEmptyConstructorCreatesValidObject() {
        UserProfiles emptyUser = new UserProfiles();

        // The object itself shouldn't be null
        Assert.assertNotNull(emptyUser);

        // Uninitialized booleans in Java default to false
        Assert.assertFalse(emptyUser.getIsAdmin());
        Assert.assertFalse(emptyUser.getAdminRequested());
    }
}