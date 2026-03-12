package com.example.myapplication;

/**
 * Organizer user
 * Inherits from profile, so it has the users info
 * Role is set to Organizer
 */
public class Organizer extends UserProfiles {
    /**
     * Auto assign Organizer if database says they are
     * need for data mapping
     */
    public Organizer() {
        super();
        setRole("Organizer");
    }

    /**
     * Organizer is made
     * Used to make the Organizer account
     *
     * @param name Organzier name
     * @param email Organizer Email
     * @param phone Organizer number (optional)
     */
    public Organizer(String name, String email, String phone) {
        super(name, email, phone, "Organizer");
    }
}