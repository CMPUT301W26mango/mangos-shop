package com.example.myapplication;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class EventStore {
    private final FirebaseFirestore db;

    public interface OnEventLoadedListener {
        void onEventLoaded(Event event);
        void onError(String message);
    }

    public EventStore() {
        db = FirebaseFirestore.getInstance();
    }

    public void addEvent(Event event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("id", event.getEventId());
        eventData.put("title", event.getTitle());
        eventData.put("description", event.getDescirption());
        eventData.put("location", event.getLocation());
        eventData.put("regStart", event.getRegStart());
        eventData.put("regEnd", event.getRegEnd());
        eventData.put("posterURL", event.getPosterURL());
        eventData.put("qrValue", event.getQrValue());
        eventData.put("capacity", event.getQrValue());

        db.collection("events")
                .document()
                .set(eventData);
    }

    public void delEventById(int eventId) {
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

    public void getEventById(int eventId, OnEventLoadedListener listener) {
        db.collection("events")
                .whereEqualTo("id", eventId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    QueryDocumentSnapshot document =
                            (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);

                    Event event = new Event();
                    event.setEventId(document.getLong("id").intValue());
                    event.setTitle(document.getString("title"));
                    event.setDescirption(document.getString("description"));
                    event.setLocation(document.getString("location"));
                    event.setRegStart(document.getLong("regStart").intValue());
                    event.setRegEnd(document.getLong("regEnd").intValue());
                    event.setPosterURL(document.getString("posterURL"));
                    event.setQrValue(document.getLong("qrValue").intValue());
                    event.setQrValue(document.getLong("capacity").intValue());


                    listener.onEventLoaded(event);
                });
    }
}