package com.example.myapplication;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Data access class responsible for all Firestore operations related to events.
 *
 * This class abstracts all Firebase Firestore read/write/delete operations for the "events" collection
 * so that Activities and Fragments do not interact with Firestore directly.
 * @author Sayuj
 */

public class EventStore {
    private final FirebaseFirestore db;

    /**
     * Callback interface for when an event has been loaded from Firestore.
     */
    public interface OnEventLoadedListener {
        /**
         * Called when the event has been successfully loaded.
         *
         * @param event the loaded Event object
         */
        void onEventLoaded(Event event);
    }

    /**
     * Constructs a new EventStore and initializes the Firestore instance.
     */
    public EventStore() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Saves a new event to the Firestore "events" collection.
     * Automatically generates a unique document ID which is also used as the qrValue.
     *
     * @param event the Event object to save
     */
    public void addEvent(Event event) {
        Map<String, Object> eventData = new HashMap<>();
        DocumentReference docRef = db.collection("events").document();
        String eventId = docRef.getId();

        eventData.put("id", eventId);
        eventData.put("title", event.getTitle());
        eventData.put("description", event.getDescription());
        eventData.put("location", event.getLocation());
        eventData.put("regStart", event.getRegStart());
        eventData.put("regEnd", event.getRegEnd());
        eventData.put("dateEvent", event.getDateEvent());
        eventData.put("posterURL", event.getPosterURL());
        eventData.put("qrValue", eventId);
        eventData.put("capacity", event.getCapacity());
        eventData.put("maxWaitingListSize", event.getMaxWaitingListSize());
        eventData.put("eventType", event.getEventType());
        eventData.put("organizerName", event.getOrganizerName());
        eventData.put("geolocationRequired", event.getGeolocationRequired());
        docRef.set(eventData);
    }

    /**
     * Deletes an event from Firestore by its event ID field.
     * Queries for documents where "id" matches the given eventId and deletes them.
     *
     * @param eventId the event ID to search for and delete
     */
    public void delEventById(String eventId) {
        db.collection("events")
                .whereEqualTo("id", eventId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        db.collection("events")
                                .document(document.getId())
                                .delete();
                    }
                });
    }

    /**
     * Retrieves a single event from Firestore by its event ID field.
     * Calls the listener with the loaded Event object on success.
     *
     * Outstanding issue: will throw IndexOutOfBoundsException if no
     * matching event is found in Firestore.
     *
     * @param eventId  the event ID to search for
     * @param listener callback to receive the loaded Event object
     */
    public void getEventById(String eventId, OnEventLoadedListener listener) {
        db.collection("events")
                .whereEqualTo("id", eventId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    QueryDocumentSnapshot document =
                            (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);

                    Event event = new Event();
                    event.setId(document.getString("id"));
                    event.setTitle(document.getString("title"));
                    event.setDescription(document.getString("description"));
                    event.setLocation(document.getString("location"));
                    event.setRegStart(document.getTimestamp("regStart"));
                    event.setRegEnd(document.getTimestamp("regEnd"));
                    event.setDateEvent(document.getString("dateEvent"));
                    event.setPosterURL(document.getString("posterURL"));
                    event.setQrValue(document.getString("qrValue"));
                    event.setCapacity(document.getLong("capacity").intValue());
                    event.setOrganizerName(document.getString("organizerName"));
                    event.setEventType(document.getString("eventType"));



                    listener.onEventLoaded(event);
                });
    }
}
