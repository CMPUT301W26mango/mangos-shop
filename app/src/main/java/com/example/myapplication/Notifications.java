package com.example.myapplication;

import com.google.firebase.Timestamp;

public class Notifications {
    private String message;
    private String eventId;
    private Timestamp timestamp;
    private String eventName;
    private String status;

    // firestore needs an empty constructor to map the data
    public Notifications() {
    }

    public Notifications(String message, String eventId, String eventName, String status) {
        this.message = message;
        this.eventId = eventId;
        this.timestamp = Timestamp.now();
        this.eventName = eventName;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotificationName() {
        return eventName != null ? eventName : "Event Update";
    }
}