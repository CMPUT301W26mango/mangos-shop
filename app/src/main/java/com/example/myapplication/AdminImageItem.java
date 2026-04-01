package com.example.myapplication;

public class AdminImageItem {
    private String eventId;
    private String imageUrl;

    public AdminImageItem(String eventId, String imageUrl) {
        this.eventId = eventId;
        this.imageUrl = imageUrl;
    }

    public String getEventId() { return eventId; }
    public String getImageUrl() { return imageUrl; }
}