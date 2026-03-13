package com.example.myapplication;

import android.app.Dialog;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Displays event details as a popup dialog
 * Receives eventId via Bundle from QR scan result or event being clicked from EventListFragment
 * Uses respective eventId to fetch data from Firebase and display in a popup
 * @author Ali
 * @author Aditya
 */
public class EventDetailsFragment extends DialogFragment {

    private final String logTag = "Fragment for event Details";
    private FirebaseFirestore db;

    private String firestoreDocId;
    private String deviceId;

    public EventDetailsFragment() {

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_details, container, false);

        db = FirebaseFirestore.getInstance();


        // Device Id
        deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        ImageButton btnClose = view.findViewById(R.id.btn_close);
        btnClose.setOnClickListener(v -> dismiss());

        Bundle args = getArguments();
        if (args != null && args.containsKey("eventId")) {
            String eventId = args.getString("eventId");
            loadEventFromFirestore(eventId, view);
        } else {
            Toast.makeText(getContext(), "No event found", Toast.LENGTH_SHORT).show();
            dismiss();
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    /**
     * Loads event data from Firestore using qrValue and populates the popup UI
     * Matches eventId encoded in QR Code with corresponding event document in db
     * Extracts event details from db including title, location, capacity, image poster, etc.
     * regStart/regEnd handled as timestamp and event date handled as string
     * Shows different buttons depending on user registration status
     * @author Ali
     * @author Aditya
     * @param eventId Id used to fetch correct event from db
     * @param view  UI layout of screen to display details
     */
    private void loadEventFromFirestore(String eventId, View view) {
        TextView tvTitle = view.findViewById(R.id.tv_event_title);
        TextView tvLocation = view.findViewById(R.id.tv_event_location);
        TextView tvSpots = view.findViewById(R.id.tv_spots_available);
        TextView tvOrganizer = view.findViewById(R.id.tv_organizer);
        TextView tvEventType = view.findViewById(R.id.tv_event_type);
        TextView tvEventDate = view.findViewById(R.id.tv_event_date);
        TextView eventFull = view.findViewById(R.id.registerLimitReached);
        TextView tvRegStart = view.findViewById(R.id.tv_reg_start);
        TextView tvRegEnd = view.findViewById(R.id.tv_reg_end);
        TextView tvDescription = view.findViewById(R.id.tv_event_description);
        ImageView ivPoster = view.findViewById(R.id.iv_event_poster);

        Button registerBtn = view.findViewById(R.id.registerBtn);
        Button btnCancel = view.findViewById(R.id.cancelRegisterBtn);
        TextView textViewAlreadyRegistered = view.findViewById(R.id.alreadyRegisteredTextView);



        db.collection("events")
                .whereEqualTo("qrValue", eventId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded() || getContext() == null) return;
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot document =
                                (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);

                        // Firestore Document Id
                        firestoreDocId = document.getId();

                        // Title
                        String title = document.getString("title");
                        tvTitle.setText(title != null ? title : "No Title");

                        // Event Type
                        String eventType = document.getString("eventType");
                        tvEventType.setText(eventType != null ? eventType : "GENERAL");

                        // Description
                        String description = document.getString("description");
                        tvDescription.setText(description != null ? description : "No description available");

                        // Location
                        String location = document.getString("location");
                        tvLocation.setText(location != null ? location : "No Location");

                        // Event Date (String)
                        String eventDate = document.getString("dateEvent");
                        tvEventDate.setText(eventDate != null ? eventDate : "TBD");

                        // Registration dates
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

                        Object regStartObj = document.get("regStart");
                        if (regStartObj instanceof Timestamp) {
                            tvRegStart.setText(sdf.format(((Timestamp) regStartObj).toDate()));
                        } else if (regStartObj instanceof String) {
                            tvRegStart.setText((String) regStartObj);
                        } else {
                            tvRegStart.setText("TBD");
                        }

                        Object regEndObj = document.get("regEnd");
                        if (regEndObj instanceof Timestamp) {
                            tvRegEnd.setText(sdf.format(((Timestamp) regEndObj).toDate()));
                        } else if (regEndObj instanceof String) {
                            tvRegEnd.setText((String) regEndObj);
                        } else {
                            tvRegEnd.setText("TBD");
                        }

                        // Capacity/spots
                        if (document.getLong("capacity") != null) {
                            Long maxSpots = document.getLong("maxWaitingListSize");
                            if (maxSpots == null || maxSpots == -1) {
                                tvSpots.setText("Unlimited");
                            } else {
                                tvSpots.setText(String.valueOf(maxSpots.intValue()));
                            }
                        } else {
                            tvSpots.setText("N/A");
                        }

                        // Organizer
                        String organizerName = document.getString("organizerName");
                        tvOrganizer.setText(organizerName != null ? organizerName : "Unknown");

                        // Poster
                        String posterUrl = document.getString("posterURL");
                        if (posterUrl != null && !posterUrl.isEmpty()) {
                            ivPoster.setVisibility(View.VISIBLE);
                            Glide.with(requireContext())
                                    .load(posterUrl)
                                    .into(ivPoster);
                        }

                        // Determine what button to show
                        isRegistered(registerBtn, btnCancel, textViewAlreadyRegistered);

                        // Create a listener for the two buttons
                        registerBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                joinWaitingList(registerBtn, btnCancel, textViewAlreadyRegistered, eventFull);
                            }
                        });

                        btnCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                leaveWaitingList(registerBtn, btnCancel, textViewAlreadyRegistered);
                            }
                        });

                    } else {
                        if (!isAdded() || getContext() == null) return;
                        Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || getContext() == null) return;
                    Toast.makeText(getContext(), "Failed to load event: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    dismiss();
                });
    }


    // Register Button / Cancel Register button
    private void isRegistered(Button registerBtn, Button cancelBtn, TextView textViewAlreadyRegistered){
        db.collection("events")
                .document(firestoreDocId)
                .collection("waitingList")
                .document(deviceId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!isAdded() || getContext() == null) return;
                    if (doc.exists()) {
                        // If already reigsterd show cancel button
                        registerBtn.setVisibility(View.GONE);
                        cancelBtn.setVisibility(View.VISIBLE);
                        textViewAlreadyRegistered.setVisibility(View.VISIBLE);
                    } else {
                        // If not registerd then show register button
                        registerBtn.setVisibility(View.VISIBLE);
                        cancelBtn.setVisibility(View.GONE);
                        textViewAlreadyRegistered.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(logTag, "Something went wrong when checking registration status", e);
                });
    }

    private void joinWaitingList(Button registerBtn, Button cancelBtn, TextView textViewAlreadyRegistered, TextView eventFull){
        Map<String, Object> entrantInfo = new HashMap<>();
        entrantInfo.put("userId", deviceId);




        db.collection("events").document(firestoreDocId).collection("waitingList").get().addOnSuccessListener(waitingListCount -> {
            if (!isAdded() || getContext() == null) return;
            int currentWaitingListSize = waitingListCount.size();

            db.collection("events").document(firestoreDocId).get().addOnSuccessListener(eventListInfo -> {
                if (!isAdded() || getContext() == null) return;
                Long maxWaitingListSize = eventListInfo.getLong("maxWaitingListSize");
                if (maxWaitingListSize == null || maxWaitingListSize == -1 || currentWaitingListSize < maxWaitingListSize){

                    db.collection("events")
                            .document(firestoreDocId)
                            .collection("waitingList")
                            .document(deviceId).set(entrantInfo)
                            .addOnSuccessListener( waitingList -> {
                                if (!isAdded() || getContext() == null) return;
                                Log.d(logTag, "Successfully joined waiting list");
                                Toast.makeText(getContext(),
                                        "You've joined the waiting list!",
                                        Toast.LENGTH_SHORT).show();

                                // Swap buttons
                                registerBtn.setVisibility(View.GONE);
                                cancelBtn.setVisibility(View.VISIBLE);
                                textViewAlreadyRegistered.setVisibility(View.VISIBLE);
                    }).addOnFailureListener(e -> {
                            if (!isAdded() || getContext() == null) return;
                            Log.d(logTag, "Something went wrong when registering");
                                Toast.makeText(getContext(), "Something went wrong when registering", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    eventFull.setVisibility(View.VISIBLE);
                }
            });



        });




    }

    private void leaveWaitingList(Button registerBtn, Button cancelBtn, TextView textViewAlreadyRegistered){
        db.collection("events")
                .document(firestoreDocId)
                .collection("waitingList")
                .document(deviceId)
                .delete()
                .addOnSuccessListener(waitingList -> {
                    if (!isAdded() || getContext() == null) return;
                    Log.d(logTag, "Successfully left waiting list");


                    Toast.makeText(getContext(),
                            "You've left the waiting list.",
                            Toast.LENGTH_SHORT).show();

                    // Swap buttons back
                    registerBtn.setVisibility(View.VISIBLE);
                    cancelBtn.setVisibility(View.GONE);
                    textViewAlreadyRegistered.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || getContext() == null) return;
                    Log.e(logTag, "Something went wrong when cancelling", e);
                    Toast.makeText(getContext(),
                            "Something went wrong when cancelling",
                            Toast.LENGTH_SHORT).show();
                });
    }


}