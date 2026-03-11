package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentReference;
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
        // check the server again this way everythign saves correctly

        db.collection("users").document(deviceId).get(Source.SERVER).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                String role = task.getResult().getString("role");
                if ("Admin".equals(role)) listener.onSuccess(task.getResult().toObject(Admin.class));
                else if ("Organizer".equals(role)) listener.onSuccess(task.getResult().toObject(Organizer.class));
                else listener.onSuccess(task.getResult().toObject(Entrant.class));
            } else {
                // back if profile gone
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