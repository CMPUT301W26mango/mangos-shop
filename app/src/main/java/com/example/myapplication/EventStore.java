package com.example.myapplication;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class EventStore {
    private final FirebaseFirestore db;

    public interface OnEventLoadedListener {
        void onEventLoaded(Event event);
    }

    public EventStore() {
        db = FirebaseFirestore.getInstance();
    }

    public void addEvent(Event event) {
        Map<String, Object> eventData = new HashMap<>();
        DocumentReference docRef = db.collection("events").document();
        String eventId = docRef.getId();

        eventData.put("id", eventId);
        eventData.put("title", event.getTitle());
        eventData.put("description", event.getDescirption());
        eventData.put("location", event.getLocation());
        eventData.put("regStart", event.getRegStart());
        eventData.put("regEnd", event.getRegEnd());
        eventData.put("dateEvent", event.getDateEvent());
        eventData.put("posterURL", event.getPosterURL());
        eventData.put("qrValue", eventId);
        eventData.put("capacity", event.getCapacity());
        eventData.put("spotsToFill", event.getSpotsToFill());
        eventData.put("eventType", event.getEventType());
        eventData.put("organizerName", event.getOrganizerName());
        docRef.set(eventData);
    }

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
                    event.setDescirption(document.getString("description"));
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
