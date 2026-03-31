package com.example.myapplication;

public class ImageItem {
    private String url;
    private String eventId;

    public ImageItem(String url, String eventId) {
        this.url = url;
        this.eventId = eventId;
    }

    public String getUrl() {
        return url;
    }

    public String getEventId() {
        return eventId;
    }
}