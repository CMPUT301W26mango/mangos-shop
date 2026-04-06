package com.example.myapplication;
/**
 * Model class representing a notification log item shown in the admin logs screen.
 * Stores basic information about notifications sent by organizers.
 */
public class AdminLogItem {
    private String message;
    private String sender;
    private String time;

    /**
     * Creates an AdminLogItem with the provided log information.
     *
     * @param message notification message content
     * @param sender name of the user who sent the notification
     * @param time timestamp of when the notification was sent
     */
    public AdminLogItem(String message, String sender, String time) {
        this.message = message;
        this.sender = sender;
        this.time = time;
    }
    /**
     * Returns the notification message.
     *
     * @return message content
     */

    public String getMessage() { return message; }
    /**
     * Returns the sender of the notification.
     *
     * @return sender name
     */
    public String getSender() { return sender; }
    /**
     * Returns the time the notification was sent.
     *
     * @return timestamp string
     */
    public String getTime() { return time; }
}