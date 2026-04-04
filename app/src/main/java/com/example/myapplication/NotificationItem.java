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

    /**
     * Creates a new NotificationItem.
     *
     * @param id The unique identifier for this notification document.
     * @param eventId The Firebase ID of the event this is tied to.
     * @param eventName The display name of the event.
     * @param notiName The main title or heading of the notification.
     * @param description The body text explaining the notification.
     * @param notiTime The exact timestamp when this was generated.
     * @param read Keeps track of whether I have opened/read this yet.
     */
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

    /** @return The unique ID of the notification. */
    public String getId() { return id; }

    /** @return The Firebase document ID of the associated event. */
    public String getEventId() { return eventId; }

    /** @return The display name of the event. */
    public String getEventName() {return eventName; }

    /** @return The title/header of the notification. */
    public String getNotiName() { return notiName; }

    /** @return The full text description of the notification. */
    public String getDescription() { return description; }

    /** @return The Firestore timestamp of when it was sent. */
    public Timestamp getNotiTime() {return notiTime; }

    /** @return True if the notification has been read, false otherwise. */
    public boolean isRead() { return read; }

    /** @param read Updates the read status of this notification. */
    public void setRead(boolean read) { this.read = read; }
}