package com.example.myapplication;

/**
 * User information and roles description
 * This is what takes in the user info for the profiles to be created.
 * Also the info for admin requests
 */

public class UserProfiles {
    private String name;
    private String email;
    private String phone;
    private String role; // "Entrant" or "Organizer"
    private boolean isAdmin = false;
    private boolean adminRequested = false;

    // needed by Firestore (for data mapping)
    public UserProfiles() {}

    /**
     * Used for creating a new account for the user
     * Users who want to be admin are not admin by default but rather entrants
     * They will have to ask for admin, and then someone with access to the database will give them perms
     *
     * @param name Name of user (full name)
     * @param email Email of user (correct format, needs @ and "."
     * @param phone Phone number of user (optional)
     * @param role What are they (Entrant, Organizer, or Admin)
     */
    public UserProfiles(String name, String email, String phone, String role) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.isAdmin = false;
        this.adminRequested = false;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    //TA idea
    public boolean getIsAdmin() {
        return isAdmin;
    }
    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public boolean getAdminRequested() {
        return adminRequested;
    }
    public void setAdminRequested(boolean adminRequested) {
        this.adminRequested = adminRequested;
    }
}
