package com.example.myapplication;

/**
 * Admin user
 * Inherits from profile, so it has the users info
 * Role is set to Admin
 */
public class Admin extends UserProfiles {
    /**
     * Auto assign Admin if database says they are
     * need for data mapping
     */
    public Admin() {
        super();
        setRole("Admin");
    }

    /**
     * Admin is made
     * Used to make the admin account
     *
     * @param name Admins name
     * @param email Admins Email
     * @param phone Admins number (optional)
     */
    public Admin(String name, String email, String phone) {
        super(name, email, phone, "Admin");
    }
}