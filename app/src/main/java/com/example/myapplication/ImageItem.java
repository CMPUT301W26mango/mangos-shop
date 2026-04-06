package com.example.myapplication;
/**
 * Model class representing an image item used in the application.
 * Stores basic image information for display and navigation.
 */
public class ImageItem {
    private String url;
    private String eventId;

    /**
     * Creates an ImageItem with the provided image information.
     *
     * @param url URL of the image
     * @param eventId unique Firestore document ID for the associated event
     */
    public ImageItem(String url, String eventId) {
        this.url = url;
        this.eventId = eventId;
    }
    /**
     * Returns the image URL.
     *
     * @return image URL
     */
    public String getUrl() {
        return url;
    }
    /**
     * Returns the associated event ID.
     *
     * @return event ID
     */
    public String getEventId() {
        return eventId;
    }
}