package com.example.myapplication;
/**
 * Model class representing a user profile item shown in the admin browse profiles screen.
 * Stores basic user information needed for display and navigation.
 */
public class AdminProfileItem {
    private String userId;
    private String name;
    private String email;
    private String role;
    /**
     * Empty constructor required for Firebase or default object creation.
     */
    public AdminProfileItem() {
    }
    /**
     * Creates an AdminProfileItem with the provided user information.
     *
     * @param userId unique Firestore document ID for the user
     * @param name user's name
     * @param email user's email
     * @param role user's role in the application
     */
    public AdminProfileItem(String userId, String name, String email, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }
    /**
     * Returns the Firestore document ID of the user.
     *
     * @return user ID
     */
    public String getUserId() {
        return userId;
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
     * Returns the user's email.
     *
     * @return user email
     */
    public String getEmail() {
        return email;
    }
    /**
     * Returns the user's role.
     *
     * @return user role
     */
    public String getRole() {
        return role;
    }
}