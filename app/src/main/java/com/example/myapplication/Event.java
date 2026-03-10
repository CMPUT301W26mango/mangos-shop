package com.example.myapplication;

// testing things right now
// test 2

// test 3

public class Event {
    // class that holds all the data for Events
    private int eventId;
    private String title;
    private String descirption;
    // location is String for now, but maybe it should be coords or something? Change as needed
    private String location;
    private int regStart;
    private int regEnd;
    private String posterURL;
    private int qrValue;

    public Event() {
    }

    public Event(int eventId, String title, String descirption, String location, int regStart, int regEnd, String posterURL, int qrValue) {
        this.eventId = eventId;
        this.title = title;
        this.descirption = descirption;
        this.location = location;
        this.regStart = regStart;
        this.regEnd = regEnd;
        this.posterURL = posterURL;
        this.qrValue = qrValue;
    }

    public int getEventId() {
        return eventId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescirption() {
        return descirption;
    }

    public String getLocation() {
        return location;
    }

    public int getRegStart() {
        return regStart;
    }

    public int getRegEnd() {
        return regEnd;
    }

    public String getPosterURL() {
        return posterURL;
    }

    public int getQrValue() {
        return qrValue;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescirption(String descirption) {
        this.descirption = descirption;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setRegStart(int regStart) {
        this.regStart = regStart;
    }

    public void setPosterURL(String posterURL) {
        this.posterURL = posterURL;
    }

    public void setRegEnd(int regEnd) {
        this.regEnd = regEnd;
    }

    public void setQrValue(int qrValue) {
        this.qrValue = qrValue;
    }
}
