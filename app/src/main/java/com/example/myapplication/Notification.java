package com.example.myapplication;

import com.google.firebase.Timestamp;

public class Notification {
    private String message;
    private String eventId;
    private Timestamp timestamp;

    // firestore needs an empty constructor to map the data
    public Notification() {}

    public Notification(String message, String eventId) {
        this.message = message;
        this.eventId = eventId;
        this.timestamp = Timestamp.now();
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}