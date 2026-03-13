package com.example.myapplication;

import org.junit.Test;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


import com.google.firebase.Timestamp;

import java.util.Date;

public class UnitTestEventRegistration {
    @Test
    public void checkIfRegistered() {
        Event event = new Event();

        Timestamp start = new Timestamp(new Date(System.currentTimeMillis() - 10000));
        Timestamp end = new Timestamp(new Date(System.currentTimeMillis() + 10000));

        event.setRegStart(start);
        event.setRegEnd(end);

        assertTrue(event.isRegistrationOpen());
    }

    @Test
    public void checkIfRegistrationClosed(){
        Event event = new Event();

        Timestamp start = new Timestamp(new Date(System.currentTimeMillis() - 100000));
        Timestamp end = new Timestamp(new Date(System.currentTimeMillis() - 10000));

        event.setRegStart(start);
        event.setRegEnd(end);

        assertFalse(event.isRegistrationOpen());
    }
}

