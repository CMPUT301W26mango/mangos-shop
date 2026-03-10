package com.example.myapplication;

public class Admin extends UserProfiles {
    public Admin() {
        super();
        setRole("Admin");
    }
    public Admin(String name, String email, String phone) {
        super(name, email, phone, "Admin");
    }
}