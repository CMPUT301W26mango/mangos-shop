//package com.example.myapplication;
//
//import org.junit.Assert;
//import org.junit.Test;
//
///**
// * I am testing all 3 roles (Admin, Entrant, and Organizer) at the same time.
// * Tests that the subclasses:
// * - Assign the specific roles correctly.
// * - Pass data properly up to the parent UserProfiles class.
// */
//public class AllRolesTest {
//
//    /**
//     * Tests that the Admin subclass correctly assigns the "Admin" role
//     */
//    @Test
//    public void testAdminRoleInitialization() {
//        Admin emptyAdmin = new Admin();
//        Assert.assertEquals("Admin", emptyAdmin.getRole());
//
//        // Testing name initialization
//        Admin paramAdmin = new Admin("AdminJohn", "john@admin.com", "111");
//        Assert.assertEquals("Admin", paramAdmin.getRole());
//        Assert.assertEquals("AdminJohn", paramAdmin.getName());
//    }
//
//    /**
//     * Tests that the Organizer subclass correctly assigns the "Organizer" role
//     */
//    @Test
//    public void testOrganizerRoleInitialization() {
//        Organizer emptyOrganizer = new Organizer();
//        Assert.assertEquals("Organizer", emptyOrganizer.getRole());
//
//        // Testing email initialization
//        Organizer paramOrganizer = new Organizer("OrganzierJohn", "OrgJohn@org.com", "222");
//        Assert.assertEquals("Organizer", paramOrganizer.getRole());
//        Assert.assertEquals("OrgJohn@org.com", paramOrganizer.getEmail());
//    }
//
//    /**
//     * Tests that the Entrant subclass correctly assigns the "Entrant" role
//     */
//    @Test
//    public void testEntrantRoleInitialization() {
//        Entrant emptyEntrant = new Entrant();
//        Assert.assertEquals("Entrant", emptyEntrant.getRole());
//
//        // Testing phone initialization
//        Entrant paramEntrant = new Entrant("EntrantJohn", "EntrantJohn@entrant.com", "333);
//        Assert.assertEquals("Entrant", paramEntrant.getRole());
//        Assert.assertEquals("333", paramEntrant.getPhone());
//    }
//}