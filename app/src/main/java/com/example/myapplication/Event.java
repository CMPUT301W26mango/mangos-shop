package com.example.myapplication;

// testing things right now
// test 2

// test 3

public class Event {
    // class that holds all the data for Events
    private String eventId;
    private String title;
    private String descirption;
    // location is String for now, but maybe it should be coords or something? Change as needed
    private String location;
    private String regStart;
    private String regEnd;
    private String posterURL;
    private String qrValue;

    private int capacity;

    private String dateEvent;



    public Event() {
    }

    public Event(String eventId, String title, String descirption, String location, String regStart, String regEnd, String posterURL, String qrValue, int capacity, String dateEvent) {
        this.eventId = eventId;
        this.title = title;
        this.descirption = descirption;
        this.location = location;
        this.regStart = regStart;
        this.regEnd = regEnd;
        this.posterURL = posterURL;
        this.qrValue = qrValue;
        this.capacity = capacity;
        this.dateEvent = dateEvent;
    }

    public String getDateEvent() {
        return dateEvent;
    }

    public void setDateEvent(String dateEvent) {
        this.dateEvent = dateEvent;
    }

    public String getEventId() {
        return eventId;
    }

    public int getCapacity() {
        return capacity;
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

    public String getRegStart() {
        return regStart;
    }

    public String getRegEnd() {
        return regEnd;
    }

    public String getPosterURL() {
        return posterURL;
    }

    public String getQrValue() {
        return qrValue;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
    public void setDescirption(String descirption) {
        this.descirption = descirption;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setRegStart(String regStart) {
        this.regStart = regStart;
    }

    public void setPosterURL(String posterURL) {
        this.posterURL = posterURL;
    }

    public void setRegEnd(String regEnd) {
        this.regEnd = regEnd;
    }

    public void setQrValue(String qrValue) {
        this.qrValue = qrValue;
    }
}

