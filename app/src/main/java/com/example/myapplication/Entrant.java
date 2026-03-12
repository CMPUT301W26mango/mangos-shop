package com.example.myapplication;

/**
 * Entrant user
 * Inherits from profile, so it has the users info
 * Role is set to Entrant
 */
public class Entrant extends UserProfiles {
    /**
     * Auto assign Entrant if database says they are
     * need for data mapping
     */
    public Entrant() {
        super();
        setRole("Entrant");
    }

    /**
     * Entrant is made
     * Used to make the Entrant account
     *
     * @param name Entrant name
     * @param email Entrant Email
     * @param phone Entrant number (optional)
     */
    public Entrant(String name, String email, String phone) {
        super(name, email, phone, "Entrant");
    }
}