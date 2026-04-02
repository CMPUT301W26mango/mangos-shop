package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

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

import java.util.List;

/**
 * Helper class that manages user profile data and Firestore database interactions.
 * Handles device identification, fetching user roles, deleting profiles,
 * and aggregating waitlist counts.
 */
public class Profiles {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Identifies who the user is by the deviceID
     *
     * @param context app
     * @return id of the device
     */
    @SuppressLint("HardwareIds")
    public String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * Fetechs user role from database using deviceID then returns the correct profile role
     * If given Admin roles, then the role is changed from Entrant to Admin
     *
     * @param deviceId id of device (hardware)
     * @param listener when fetching complete
     */
    public void fetchUserRole(String deviceId, OnSuccessListener<UserProfiles> listener) {
        db.collection("users").document(deviceId).get(Source.SERVER).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                DocumentSnapshot doc = task.getResult();

                //admin granted
                Boolean isAdmin = doc.getBoolean("isAdmin");
                if (Boolean.TRUE.equals(isAdmin)) {

                    // so now when the admin is requested and accepted role changes from Entrant to Admin
                    if (!"Admin".equals(doc.getString("role"))) {
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

    /**
     * Deletes users profile from the collection in the database
     * Removes them from any event waiting list they joined before deleting account
     *
     * @param deviceId   device id (hardware)
     * @param onComplete when delete is completed
     */
    public void deleteProfile(String deviceId, OnCompleteListener<Void> onComplete) {
        // First, grab all events
        db.collection("events").get().addOnSuccessListener(eventsSnap -> {
            WriteBatch batch = db.batch();

            // Delete the user's main profile document
            DocumentReference userRef = db.collection("users").document(deviceId);
            batch.delete(userRef);

            // Loop through every event and delete this user's specific document from the waiting list
            for (DocumentSnapshot eventDoc : eventsSnap.getDocuments()) {
                DocumentReference waitlistEntry = eventDoc.getReference()
                        .collection("waitingList")
                        .document(deviceId);
                batch.delete(waitlistEntry); // If they aren't in this list, Firebase safely ignores this
            }

            // Commit the delete
            batch.commit().addOnCompleteListener(onComplete);

        }).addOnFailureListener(e -> {
            Log.e("Profiles", "Failed to fetch events for deletion", e);
            db.collection("users").document(deviceId).delete().addOnCompleteListener(onComplete);
        });
    }


    /**
     * Waitlist Count
     * Uses query to go through the database to see how many entrants are in the waiting list for the event selected
     *
     * @param eventId   id of the event (like device id but for events)
     * @param onSuccess gives the final count of the users
     */
    public void getWaitingListCount(String eventId, OnSuccessListener<Long> onSuccess) {
        AggregateQuery countQuery = db.collection("events_waitlist.xml").document(eventId).collection("waitingList").count();
        countQuery.get(AggregateSource.SERVER).addOnSuccessListener(snapshot -> {
            onSuccess.onSuccess(snapshot.getCount());
        });
    }

    /**
     * Fetches the user's name from Firestore based on their device ID.
     *
     * @param deviceId The device ID of the user.
     * @param listener Callback to return the name string.
     */
    public void getProfileName(String deviceId, OnSuccessListener<String> listener) {
        db.collection("users").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        listener.onSuccess(name != null ? name : "Unknown Organizer");
                    } else {
                        listener.onSuccess("New Organizer");
                    }
                })
                .addOnFailureListener(e -> listener.onSuccess("Organizer"));
    }
    public void searchUsers(String searchText, OnSuccessListener<List<UserProfiles>> listener) {
        // smart search
        String searchField = "name"; // Default to name

        if (searchText.contains("@")) {
            searchField = "email";
        } else if (searchText.matches(".*\\d.*")) {
            // If it contains any numbers, assume it's a phone number
            searchField = "phone";
        }

        // serach on chose field
        db.collection("users")
                .orderBy(searchField)
                .startAt(searchText)
                .endAt(searchText + "\uf8ff") // prefix matching
                .limit(20)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<UserProfiles> results = snapshots.toObjects(UserProfiles.class);
                    listener.onSuccess(results);
                })
                .addOnFailureListener(e -> {
                    Log.e("Search", "Error searching users", e);
                });
    }
}