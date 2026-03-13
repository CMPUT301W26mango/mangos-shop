package com.example.myapplication;

import com.google.firebase.Timestamp;

/**
 * Model class representing an event in the application.
 *
 * This class is used as a data model throughout the app and maps directly
 * to documents in the Firestore "events" collection. It follows a simple
 * pattern with getters and setters for all fields.
 * @author Sayuj
 */

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
    private int maxWaitingListSize;
    private boolean geolocationRequired;

    /**
     * Empty constructor required by Firebase Firestore for
     * automatic deserialization of documents into Event objects.
     */
    public Event(){}


    /**
     * Parameterized constructor for creating a fully populated Event object.
     *
     * @param id              the unique Firestore document ID
     * @param title           the name of the event
     * @param descirption     a description of the event
     * @param location        the physical location of the event
     * @param regStart        the registration start timestamp
     * @param regEnd          the registration end timestamp
     * @param posterURL       URL of the event poster image
     * @param qrValue         the QR code value used to identify this event
     * @param capacity        maximum number of attendees
     * @param dateEvent       the date the event takes place (as a String)
     * @param organizerName   the name of the event organizer
     * @param eventType       the type/category of the event
     * @param maxWaitingListSize maximum number of entrants allowed on the waiting list
     */
    public Event(String id, String title, String descirption, String location,
                 Timestamp regStart, Timestamp regEnd, String posterURL,
                 String qrValue, int capacity, String dateEvent,
                 String organizerName, String eventType, int maxWaitingListSize) {

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
        this.maxWaitingListSize = maxWaitingListSize;
    }

    // Getters and Setters

    /**
     * @return the unique Firestore document ID for this event
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the unique Firestore document ID to set
     */
    public void setId(String id) {
        this.id = id;
    }


    /**
     * @return the title/name of the event
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title/name of the event to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the description of the event
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param descirption the description of the event to set
     */
    public void setDescription(String descirption) {
        this.description = descirption;
    }

    /**
     * @return the physical location of the event
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location the physical location of the event to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return the registration start timestamp
     */
    public Timestamp getRegStart() {
        return regStart;
    }

    /**
     * @param regStart the registration start timestamp to set
     */
    public void setRegStart(Timestamp regStart) {
        this.regStart = regStart;
    }

    /**
     * @return the registration end timestamp
     */
    public Timestamp getRegEnd() {
        return regEnd;
    }

    /**
     * @param regEnd the registration end timestamp to set
     */
    public void setRegEnd(Timestamp regEnd) {
        this.regEnd = regEnd;
    }

    /**
     * @return the URL of the event poster image
     */
    public String getPosterURL() {
        return posterURL;
    }

    /**
     * @param posterURL the URL of the event poster image to set
     */
    public void setPosterURL(String posterURL) {
        this.posterURL = posterURL;
    }

    /**
     * @return the QR code value used to identify and navigate to this event
     */
    public String getQrValue() {
        return qrValue;
    }

    /**
     * @param qrValue the QR code value to set
     */
    public void setQrValue(String qrValue) {
        this.qrValue = qrValue;
    }

    /**
     * @return the maximum number of attendees for this event
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * @param capacity the maximum number of attendees to set
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * @return the date the event takes place as a formatted String
     */
    public String getDateEvent() {
        return dateEvent;
    }

    /**
     * @param dateEvent the event date string to set
     */
    public void setDateEvent(String dateEvent) {
        this.dateEvent = dateEvent;
    }

    /**
     * @return the name of the event organizer
     */
    public String getOrganizerName() {
        return organizerName;
    }

    /**
     * @param organizerName the name of the event organizer to set
     */
    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    /**
     * @return the type/category of the event (e.g. "Sports", "Music")
     */
    public String getEventType() {
        return eventType;
    }


    /**
     * @param eventType the type/category of the event to set
     */
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
     * @return the maximum number of entrants allowed on the waiting list
     */
    public int getMaxWaitingListSize() {
        return maxWaitingListSize;
    }

    /**
     * @param spotsToFill the maximum waiting list size to set
     */
    public void setMaxWaitingListSize(int spotsToFill) {
        this.maxWaitingListSize = spotsToFill;
    }

    /**
     * @return true if geolocation is required for this event, false otherwise
     */
    public boolean getGeolocationRequired() {
        return geolocationRequired;
    }

    /**
     * @param geolocationRequired true to require geolocation, false to disable it
     */
    public void setGeolocationRequired(boolean geolocationRequired) {
        this.geolocationRequired = geolocationRequired;
    }

    /**
     * Checks whether registration for this event is currently open.
     * Registration is considered open if the current time is after regStart
     * and before or equal to regEnd.
     *
     * @return true if registration is currently open, false otherwise
     */
    public boolean isRegistrationOpen() {



        if (regStart == null || regEnd == null) {
            return false;
        }

        Timestamp currentTime = Timestamp.now();

        return currentTime.compareTo(regStart) > 0 &&
                currentTime.compareTo(regEnd) <= 0;
    }
}
