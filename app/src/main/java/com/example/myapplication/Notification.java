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

    /**
     * Gets the specific messages for the notifications
     * @return Returns the notifications (text)
     */
    public String getMessage() { return message; }

    /**
     * Sets the content of the messages
     * @param message This is the message
     */
    public void setMessage(String message) { this.message = message; }

    /**
     * Gets the Event ID of the event that the notification is from
     * @return The Event ID
     */
    public String getEventId() { return eventId; }

    /**
     * Sets the ID of the event associated with this notification.
     * @param eventId The event ID string.
     */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /**
     * Gets the exact time this notification was made.
     * @return A Firebase Timestamp (the creation time).
     */
    public Timestamp getTimestamp() { return timestamp; }

    /**
     * Sets the time this notification was made.
     * @param timestamp A Firebase Timestamp object.
     */
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}