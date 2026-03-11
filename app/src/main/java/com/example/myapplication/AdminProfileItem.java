package com.example.myapplication;

public class AdminProfileItem {
    private String userId;
    private String name;
    private String email;
    private String role;

    public AdminProfileItem() {
    }

    public AdminProfileItem(String userId, String name, String email, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}