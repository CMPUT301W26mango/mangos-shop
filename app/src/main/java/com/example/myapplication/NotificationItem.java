package com.example.myapplication;


import com.google.firebase.Timestamp;

/**
 * Model class representing a notification for an entrant.
 * Covers private event invitations and co-organizer invitations.
 */
public class NotificationItem {

    private String id;
    private String eventId;

    private String eventName;

    private String notiName;
    private String description;

    private Timestamp notiTime;
    private boolean read;

    public NotificationItem(String id, String eventId, String eventName,
                            String notiName, String description, Timestamp notiTime, boolean read) {
        this.id = id;
        this.eventId = eventId;
        this.eventName = eventName;
        this.notiName = notiName;
        this.description = description;
        this.notiTime = notiTime;
        this.read = read;
    }

    public String getId() { return id; }

    public String getEventId() { return eventId; }

    public String getEventName() {return eventName; }
    public String getNotiName() { return notiName; }
    public String getDescription() { return description; }

    public Timestamp getNotiTime() {return notiTime; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}