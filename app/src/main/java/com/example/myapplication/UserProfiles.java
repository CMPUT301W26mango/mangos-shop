package com.example.myapplication;

import com.google.firebase.firestore.DocumentId;

/**
 * Model class representing a user profile in the application.
 * Stores user information and role details for account management and admin requests.
 */

public class UserProfiles {
    private String name;
    private String email;
    private String phone;
    private String role; // "Entrant" or "Organizer"
    private boolean isAdmin = false;
    private boolean adminRequested = false;


    @DocumentId
    private String deviceId;

    /**
     * Empty constructor required for Firebase or default object creation.
     */
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
    public UserProfiles(String name, String email, String phone, String role, String deviceId) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.isAdmin = false;
        this.adminRequested = false;
        this.deviceId = deviceId;
    }
    /**
     * Returns the device ID.
     *
     * @return device ID
     */
    public String getDeviceId() {
        return deviceId;
    }
    /**
     * Sets the device ID.
     *
     * @param deviceId device ID
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    /**
     * Returns the user's name.
     *
     * @return user name
     */
    public String getName() {
        return name;
    }
    /**
     * Sets the user's name.
     *
     * @param name user name
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * Returns the user's email.
     *
     * @return user email
     */
    public String getEmail() {
        return email;
    }
    /**
     * Sets the user's email.
     *
     * @param email user email
     */
    public void setEmail(String email) {
        this.email = email;
    }
    /**
     * Returns the user's phone number.
     *
     * @return phone number
     */
    public String getPhone() {
        return phone;
    }
    /**
     * Sets the user's phone number.
     *
     * @param phone phone number
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }
    /**
     * Returns the user's role.
     *
     * @return user role
     */
    public String getRole() {
        return role;
    }
    /**
     * Sets the user's role.
     *
     * @param role user role
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Returns whether the user is an admin.
     *
     * @return true if admin, false otherwise
     */
    public boolean getIsAdmin() {
        return isAdmin;
    }
    /**
     * Sets whether the user is an admin.
     *
     * @param isAdmin admin status
     */
    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
    /**
     * Returns whether the user has requested admin privileges.
     *
     * @return true if admin requested, false otherwise
     */
    public boolean getAdminRequested() {
        return adminRequested;
    }
    /**
     * Sets whether the user has requested admin privileges.
     *
     * @param adminRequested admin request status
     */
    public void setAdminRequested(boolean adminRequested) {
        this.adminRequested = adminRequested;
    }
}
