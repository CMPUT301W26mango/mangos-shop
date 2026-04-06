package com.example.myapplication;
/**
 * Model class representing an event item shown in the admin browse events screen.
 * Stores basic event information needed for display and navigation.
 */
public class AdminEventItem {
    private String eventId;
    private String title;
    private String location;
    private String organizerName;
    private String posterURL;
    private String organizerId;
    private String deadline;
    private com.google.firebase.Timestamp regEnd;

    /**
     * Empty constructor required for Firebase or default object creation.
     */
    public AdminEventItem() {
    }
    /**
     * Creates an AdminEventItem with the provided event information.
     *
     * @param eventId unique Firestore document ID for the event
     * @param title event title
     * @param location event location
     * @param organizerName organizer name
     * @param posterURL URL of the event poster image
     * @param organizerId unique ID of the organizer
     * @param regEnd timestamp representing the registration deadline
     */
    public AdminEventItem(String eventId, String title, String location,
                          String organizerName, String posterURL,
                          String organizerId,
                          com.google.firebase.Timestamp regEnd) {
        this.eventId = eventId;
        this.title = title;
        this.location = location;
        this.organizerName = organizerName;
        this.posterURL = posterURL;
        this.organizerId = organizerId;
        this.regEnd = regEnd;
    }
    /**
     * Returns the Firestore document ID of the event.
     *
     * @return event ID
     */
    public String getEventId() {
        return eventId;
    }
    /**
     * Returns the event title.
     *
     * @return event title
     */
    public String getTitle() {
        return title;
    }
    /**
     * Returns the event location.
     *
     * @return event location
     */
    public String getLocation() {
        return location;
    }
    /**
     * Returns the organizer name.
     *
     * @return organizer name
     */
    public String getOrganizerName() {
        return organizerName;
    }
    /**
     * Returns the poster image URL.
     *
     * @return poster URL
     */
    public String getPosterURL() {
        return posterURL;
    }
    /**
     * Returns the organizer Id.
     *
     * @return unique organizer Id
     */
    public String getOrganizerId() {
        return organizerId;
    }
    /**
     * Gets the event deadline as a string.
     *
     * @return deadline string
     */
    public String getDeadline() {
        return deadline;
    }
    /**
     * Gets the registration end timestamp.
     *
     * @return Firestore Timestamp representing registration deadline
     */
    public com.google.firebase.Timestamp getRegEnd() {
        return regEnd;
    }
}