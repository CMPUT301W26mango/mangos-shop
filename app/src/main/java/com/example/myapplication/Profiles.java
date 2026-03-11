package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;
import com.google.firebase.firestore.WriteBatch;

public class Profiles {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // US 01.07.01 - Identified by device
    @SuppressLint("HardwareIds")
    public String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public void fetchUserRole(String deviceId, OnSuccessListener<UserProfiles> listener) {
        db.collection("users").document(deviceId).get(Source.SERVER).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                DocumentSnapshot doc = task.getResult();
                //admin greanted
                Boolean isAdmin = doc.getBoolean("isAdmin");
                if (Boolean.TRUE.equals(isAdmin)) {

                    // so now whent the admin is requested and accepted role changes from Entrant to Admin
                    if (!"Admin".equals(doc.getString("role"))){
                        db.collection("users").document(deviceId).update("role", "Admin");
                    }
                    //now we are doing it so
                    listener.onSuccess(doc.toObject(Admin.class));
                    return;
                }

                //none admin roles
                String role = doc.getString("role");
                if ("Organizer".equals(role)) {
                    listener.onSuccess(doc.toObject(Organizer.class));
                } else {
                    listener.onSuccess(doc.toObject(Entrant.class));
                }
            } else {
                listener.onSuccess(null);
            }
        });
    }

    public void deleteProfile(String deviceId, OnCompleteListener<Void> onComplete) {
        WriteBatch batch = db.batch();
        DocumentReference userRef = db.collection("users").document(deviceId);
        batch.delete(userRef);

        // when deleted the profile also delte the entries into any waiting lists
        db.collectionGroup("waitingList").whereEqualTo("deviceId", deviceId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            batch.delete(doc.getReference());
                        }
                    } else {
                        //debug, but should be good,
                        System.out.println("Warning: Could not clear waiting lists. Missing Index?");
                    }

                    // update and commit
                    batch.commit().addOnCompleteListener(onComplete);
                });
    }


    // US 01.05.04 - Waitlist count
    public void getWaitingListCount(String eventId, OnSuccessListener<Long> onSuccess) {
        AggregateQuery countQuery = db.collection("events_waitlist.xml").document(eventId).collection("waitingList").count();
        countQuery.get(AggregateSource.SERVER).addOnSuccessListener(snapshot -> {
            onSuccess.onSuccess(snapshot.getCount());
        });

        //should be done all userstories but check again
    }
}