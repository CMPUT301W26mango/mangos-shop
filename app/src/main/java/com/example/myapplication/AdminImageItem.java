package com.example.myapplication;
/**
 * Model class representing an event image item shown in the admin browse images screen.
 * Stores information about event images for display and management.
 */
public class AdminImageItem {
    private String eventId;
    private String imageUrl;

    /**
     * Creates an AdminImageItem with the provided image information.
     *
     * @param eventId unique Firestore document ID for the event
     * @param imageUrl URL of the event image
     */
    public AdminImageItem(String eventId, String imageUrl) {
        this.eventId = eventId;
        this.imageUrl = imageUrl;
    }
    /**
     * Returns the event ID associated with the image.
     *
     * @return event ID
     */
    public String getEventId() { return eventId; }
    /**
     * Returns the image URL.
     *
     * @return image URL
     */
    public String getImageUrl() { return imageUrl; }
}