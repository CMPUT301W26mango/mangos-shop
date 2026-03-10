package com.example.myapplication;

public class Organizer extends UserProfiles {
    public Organizer() {
        super();
        setRole("Organizer");
    }
    public Organizer(String name, String email, String phone) {
        super(name, email, phone, "Organizer");
    }
}