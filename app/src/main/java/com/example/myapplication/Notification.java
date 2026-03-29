package com.example.myapplication;

import com.google.firebase.Timestamp;

/**
 * Notifications sent to User.
 * Used by organizers to announce lottery results (if the user has been selected or not), and to notify general updates.
 */
public class Notification {
    private String message;
    private String eventId;
    private Timestamp timestamp;

    // firestore needs an empty constructor to map the data
    public Notification() {}

    /**
     * Makes Notifications (The specific messages, and according to the specific events)
     * Time Stamp of the notification is also now a new one (current time)
     *
     * @param message This is the message of the notification (what the user sees)
     * @param eventId This is the Event ID, (for the event the notification is from)
     */
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