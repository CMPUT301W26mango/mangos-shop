package com.example.myapplication;

public class EventHistory {
    private String eventId;
    private String eventName;
    private String status;

    public EventHistory(String eventId, String eventName, String status) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.status = status;
    }

    public String getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public String getStatus() { return status; }
}