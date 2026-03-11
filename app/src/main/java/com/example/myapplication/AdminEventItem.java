package com.example.myapplication;

public class AdminEventItem {
    private String eventId;
    private String title;
    private String location;
    private String organizerName;
    private String posterURL;

    public AdminEventItem() {
    }

    public AdminEventItem(String eventId, String title, String location, String organizerName, String posterURL) {
        this.eventId = eventId;
        this.title = title;
        this.location = location;
        this.organizerName = organizerName;
        this.posterURL = posterURL;
    }

    public String getEventId() {
        return eventId;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public String getPosterURL() {
        return posterURL;
    }
}