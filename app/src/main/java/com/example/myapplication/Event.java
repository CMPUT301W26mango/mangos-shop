package com.example.myapplication;

import com.google.firebase.Timestamp;

public class Event {
    private String id;
    private String title;
    private String description;
    private String location;

    private Timestamp regStart;
    private Timestamp regEnd;

    private String posterURL;
    private String qrValue;
    private int capacity;

    private String dateEvent;

    private String organizerName;
    private String eventType;
    private int spotsToFill;

    // Empty constructor for Firebase
    public Event(){}

    public Event(String id, String title, String descirption, String location,
                 Timestamp regStart, Timestamp regEnd, String posterURL,
                 String qrValue, int capacity, String dateEvent,
                 String organizerName, String eventType, int spotsToFill) {

        this.id = id;
        this.title = title;
        this.description = descirption;
        this.location = location;
        this.regStart = regStart;
        this.regEnd = regEnd;
        this.posterURL = posterURL;
        this.qrValue = qrValue;
        this.capacity = capacity;
        this.dateEvent = dateEvent;
        this.organizerName = organizerName;
        this.eventType = eventType;
        this.spotsToFill = spotsToFill;
    }

    // Getters and Setters


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String descirption) {
        this.description = descirption;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Timestamp getRegStart() {
        return regStart;
    }

    public void setRegStart(Timestamp regStart) {
        this.regStart = regStart;
    }

    public Timestamp getRegEnd() {
        return regEnd;
    }

    public void setRegEnd(Timestamp regEnd) {
        this.regEnd = regEnd;
    }

    public String getPosterURL() {
        return posterURL;
    }

    public void setPosterURL(String posterURL) {
        this.posterURL = posterURL;
    }

    public String getQrValue() {
        return qrValue;
    }

    public void setQrValue(String qrValue) {
        this.qrValue = qrValue;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getDateEvent() {
        return dateEvent;
    }

    public void setDateEvent(String dateEvent) {
        this.dateEvent = dateEvent;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public int getSpotsToFill() {
        return spotsToFill;
    }

    public void setSpotsToFill(int spotsToFill) {
        this.spotsToFill = spotsToFill;
    }

    // Logic from your class
    public boolean isRegistrationOpen() {



        if (regStart == null || regEnd == null) {
            return false;
        }

        Timestamp currentTime = Timestamp.now();

        return currentTime.compareTo(regStart) > 0 &&
                currentTime.compareTo(regEnd) <= 0;
    }
}
