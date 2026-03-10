package com.example.myapplication;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * EventDetailsFragment - Displays event details as a popup dialog.
 * Handles US 01.05.05 (lottery info display) and US 01.06.01 (navigate from QR scan).
 * Receives eventId via Bundle from QR scan result in EventListFragment.
 */
public class EventDetailsFragment extends DialogFragment {

    private FirebaseFirestore db;

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
     * Loads event data from Firestore using qrValue and populates the popup UI.
     * Handles regStart/regEnd as Timestamp or String, dateEvent as String.
     */
    private void loadEventFromFirestore(String eventId, View view) {
        TextView tvTitle = view.findViewById(R.id.tv_event_title);
        TextView tvLocation = view.findViewById(R.id.tv_event_location);
        TextView tvSpots = view.findViewById(R.id.tv_spots_available);
        TextView tvOrganizer = view.findViewById(R.id.tv_organizer);
        TextView tvEventType = view.findViewById(R.id.tv_event_type);
        TextView tvEventDate = view.findViewById(R.id.tv_event_date);
        TextView tvRegStart = view.findViewById(R.id.tv_reg_start);
        TextView tvRegEnd = view.findViewById(R.id.tv_reg_end);
        TextView tvDescription = view.findViewById(R.id.tv_event_description);
        ImageView ivPoster = view.findViewById(R.id.iv_event_poster);

        int parsedId;
        try {
            parsedId = Integer.parseInt(eventId);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid QR code", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        db.collection("events")
                .whereEqualTo("qrValue", parsedId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot document =
                                (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);

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

                        // Registration dates - handle Timestamp or String
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
                            tvSpots.setText(String.valueOf(Objects.requireNonNull(document.getLong("spotsToFill")).intValue()));
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

                    } else {
                        Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load event: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    dismiss();
                });
    }
}