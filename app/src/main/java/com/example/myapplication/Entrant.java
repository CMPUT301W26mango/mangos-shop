package com.example.myapplication;

public class Entrant extends UserProfiles {
    public Entrant() {
        super();
        setRole("Entrant");
    }
    public Entrant(String name, String email, String phone) {
        super(name, email, phone, "Entrant");
    }
}