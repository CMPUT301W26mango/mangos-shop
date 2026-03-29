package com.example.myapplication;

import org.junit.Assert;
import org.junit.Test;

/**
* Asked Gemini AI to assist in helping write the tests.
* The prompt used: 
* Can you please help me in ideas for what should be tested for this file <the file of code for this page> and generate some ideal test cases please.
*/

/**
 * I AM TESTING ALL 3 ROLES (ADMINS, ENTRANTS, and ORGANIZERS) at the same time as their codes do essenitialy the same thing.
 * Tests that the subclasses:
 * Assigns the specific roles.
 * Passes data properly up to the parent UserProfiles class.
 * ALSO TESTING 1 per Admin, Organizer, Entrant, so it is efficient.
 */
public class AllRolesTest {

    /**
     * Tests that the Admin subclass correctly assigns the "Admin" role
     */
    @Test
    public void testAdminRoleInitialization() {
        Admin emptyAdmin = new Admin();
        Assert.assertEquals("Admin", emptyAdmin.getRole());

        //testing the name aspect
        Admin paramAdmin = new Admin("AdminJohn", "john@admin.com", "111");
        Assert.assertEquals("Admin", paramAdmin.getRole());
        Assert.assertEquals("AdminJohn", paramAdmin.getName());
    }

    /**
     * Tests that the Organizer subclass correctly assigns the "Organizer" role
     */
    @Test
    public void testOrganizerRoleInitialization() {
        Organizer emptyOrganizer = new Organizer();
        Assert.assertEquals("Organizer", emptyOrganizer.getRole());

        //testing the email aspect
        Organizer paramOrganizer = new Organizer("OrganzierJohn", "OrgJohn@org.com", "222");
        Assert.assertEquals("Organizer", paramOrganizer.getRole());
        Assert.assertEquals("OrgJohn@org.com", paramOrganizer.getEmail());
    }

    /**
     * Tests that the Entrant subclass correctly assigns the "Entrant" role
     */
    @Test
    public void testEntrantRoleInitialization() {
        Entrant emptyEntrant = new Entrant();
        Assert.assertEquals("Entrant", emptyEntrant.getRole());

        //testing the phone number aspect
        Entrant paramEntrant = new Entrant("EntrantJohn", "EntrantJohn@entrant.com", "333");
        Assert.assertEquals("Entrant", paramEntrant.getRole());
        Assert.assertEquals("333", paramEntrant.getPhone());
    }
}
