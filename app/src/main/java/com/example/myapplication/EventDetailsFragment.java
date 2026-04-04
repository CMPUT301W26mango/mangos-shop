package com.example.myapplication;

import android.app.Dialog;
import android.content.Intent;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.graphics.Bitmap;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

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
    private ListenerRegistration statusListener;
    private ListenerRegistration waitlistCountListener; // Listen for the waitlist changes

    private boolean geolocationRequired = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
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
        TextView selectedMsg = view.findViewById(R.id.tv_selected_message);
        TextView acceptedMsg = view.findViewById(R.id.tv_accepted_message);
        TextView declinedMsg = view.findViewById(R.id.tv_declined_message);
        TextView invitedMsg = view.findViewById(R.id.tv_invited_message);
        TextView rejectedMsg = view.findViewById(R.id.tv_rejected_message);
        TextView lotteryRedrawMsg = view.findViewById(R.id.tv_lottery_redraw);
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
        Button acceptBtn = view.findViewById(R.id.acceptBtn);
        Button declineBtn = view.findViewById(R.id.declineBtn);
        Button btnCancel = view.findViewById(R.id.cancelRegisterBtn);
        Button acceptInvBtn = view.findViewById(R.id.acceptInvBtn);
        Button declineInvBtn = view.findViewById(R.id.declineInvBtn);
        TextView textViewAlreadyRegistered = view.findViewById(R.id.alreadyRegisteredTextView);
        ImageView shareBtn = view.findViewById(R.id.btn_share_qr);
        shareBtn.setVisibility(View.VISIBLE);
        shareBtn.setOnClickListener(v -> showQRCodePopup(eventId));



        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(document -> {
                    if (!isAdded() || getContext() == null) return;
                    if (document.exists()) {
                        firestoreDocId = document.getId();

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
                        Long maxSpots = document.getLong("maxWaitingListSize");

                        if (maxSpots == null || maxSpots == -1) {
                            tvSpots.setText("Unlimited");
                        } else {
                            waitlistCountListener = db.collection("events").document(firestoreDocId).collection("waitingList")
                                    .addSnapshotListener((snapshots, firestoreException) -> {
                                        // Error handler
                                        if (firestoreException != null || snapshots == null || !isAdded() || getContext() == null) {
                                            return;
                                        }

                                        int currentWaitListSize = snapshots.size();
                                        int remainingSpots = (int)(maxSpots - currentWaitListSize);
                                        tvSpots.setText(String.valueOf(Math.max(0, remainingSpots)));

                                    });
                        }

                        // Organizer
                        String organizerName = document.getString("organizerName");
                        tvOrganizer.setText(organizerName != null ? organizerName : "Unknown");

                        // Geolocation flag for location capture on join
                        geolocationRequired = Boolean.TRUE.equals(document.getBoolean("geolocationRequired"));
                        requestLocationPermissionIfNeeded();

                        // Poster
                        String posterUrl = document.getString("posterURL");
                        if (posterUrl != null && !posterUrl.isEmpty()) {
                            ivPoster.setVisibility(View.VISIBLE);
                            Glide.with(requireContext())
                                    .load(posterUrl)
                                    .into(ivPoster);
                        }



                        TextView tvCoOrgMessage = view.findViewById(R.id.tv_co_organizer_message);
                        Button btnOrgView = view.findViewById(R.id.btn_go_to_organizer_view);

                        List<String> coOrganizers = (List<String>) document.get("coOrganizers");
                        boolean isCoOrg = coOrganizers != null && coOrganizers.contains(deviceId);


                        // Check if user is in invitedUsers array
                        List<String> invitedUsers = (List<String>) document.get("invitedUsers");
                        boolean isInvited = invitedUsers != null && invitedUsers.contains(deviceId);

                        if (isCoOrg) {
                            registerBtn.setVisibility(View.GONE);
                            btnCancel.setVisibility(View.GONE);
                            acceptBtn.setVisibility(View.GONE);
                            declineBtn.setVisibility(View.GONE);
                            textViewAlreadyRegistered.setVisibility(View.GONE);
                            tvCoOrgMessage.setVisibility(View.VISIBLE);
                            btnOrgView.setVisibility(View.VISIBLE);
                            btnOrgView.setOnClickListener(v -> {
                                Intent intent = new Intent(requireContext(), EventDetailActivity.class);
                                intent.putExtra("EVENT_ID", firestoreDocId);
                                startActivity(intent);
                                dismiss();
                            });
                        }

                        else {
                            checkStatusAndShowUI(registerBtn, btnCancel, acceptBtn, declineBtn,
                                    textViewAlreadyRegistered, selectedMsg,
                                    acceptedMsg, declinedMsg, rejectedMsg, lotteryRedrawMsg, eventFull, isInvited, invitedMsg, acceptInvBtn,declineInvBtn);

                            // Set up the click listeners once
                            registerBtn.setOnClickListener(v -> {
                                if (geolocationRequired && ContextCompat.checkSelfPermission(requireContext(),
                                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    Toast.makeText(getContext(), "This event requires location permission. Enable it in Settings.", Toast.LENGTH_LONG).show();
                                } else {
                                    joinWaitingList(registerBtn, btnCancel, textViewAlreadyRegistered, eventFull, invitedMsg, acceptInvBtn, declineInvBtn);
                                }
                            });

                            acceptInvBtn.setOnClickListener(v ->
                                    joinWaitingList(registerBtn, btnCancel, textViewAlreadyRegistered, eventFull, invitedMsg, acceptInvBtn, declineInvBtn));

                            declineInvBtn.setOnClickListener(v ->
                                    declinePrivInvitation(acceptInvBtn, declineInvBtn, invitedMsg));

                            acceptBtn.setOnClickListener(v ->
                                    acceptSelection(acceptBtn, declineBtn, selectedMsg, acceptedMsg, declinedMsg));
                            
                            declineBtn.setOnClickListener((v ->
                                    declineSelection(acceptBtn, declineBtn, selectedMsg, acceptedMsg, declinedMsg)
                                    ));

                            btnCancel.setOnClickListener(v ->
                                    leaveWaitingList(registerBtn, btnCancel, acceptBtn, declineBtn, textViewAlreadyRegistered, selectedMsg, acceptedMsg, declinedMsg));
                        }

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




    /**
     * This Method manages the checking of whether the user has already joined the waiting list, it shows the correct view
     * depending on if the user is signed up or not.
     * Note instead of using this function, the work has been transferred over to "checkStatusAndShowUI"
     * This function is here in case its needed in the future.
     * @param registerBtn
     *  The instance of the register button, needs to be shown or hidden
     * @param cancelBtn
     *  The instance of the cancel button, needs to be shown or hidden
     * @param textViewAlreadyRegistered
     *  This is a text view to show that they are already registered and can cancel
     * */
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

    /**
     * Adds the current device to the event's waiting list subcollection in Firestore.
     *
     * First checks whether the waiting list has capacity before adding. If the event
     * has geolocation required and permission is granted, saves the current location
     * to the waiting list document. Also enriches the waiting list document with the
     * entrant's profile data (name, email, phone) for organizer visibility.
     *
     * If coming from a private event invite flow, hides the invite UI after joining.
     * Removes the device from the invitedUsers array after successfully joining.
     *
     * @param registerBtn              The register button to hide on success.
     * @param cancelBtn                The cancel/leave button to show on success.
     * @param textViewAlreadyRegistered TextView to show on success.
     * @param eventFull                TextView to show if the waiting list is full.
     * @param invitedMsg               The invite message TextView, null if not from invite flow.
     * @param acceptInvBtn             The accept invite button, null if not from invite flow.
     * @param declineInvBtn            The decline invite button, null if not from invite flow.
     */
    private void joinWaitingList(Button registerBtn, Button cancelBtn, TextView textViewAlreadyRegistered, TextView eventFull, TextView invitedMsg, Button acceptInvBtn, Button declineInvBtn   ){
        Map<String, Object> entrantInfo = new HashMap<>();
        entrantInfo.put("userId", deviceId);
        entrantInfo.put("status", "waiting");




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

                                // Hide invite UI
                                if (invitedMsg != null) invitedMsg.setVisibility(View.GONE);
                                if (acceptInvBtn != null) acceptInvBtn.setVisibility(View.GONE);
                                if (declineInvBtn != null) declineInvBtn.setVisibility(View.GONE);


                                // Swap buttons
                                registerBtn.setVisibility(View.GONE);
                                cancelBtn.setVisibility(View.VISIBLE);
                                cancelBtn.setText("Leave waiting list");
                                eventFull.setVisibility(View.GONE);
                                textViewAlreadyRegistered.setVisibility(View.VISIBLE);

                                // Save location if geolocation is required (fire-and-forget after join)
                                boolean locPermGranted = ContextCompat.checkSelfPermission(getContext(),
                                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                                if (geolocationRequired && locPermGranted) {
                                    LocationHelper.getCurrentLocation(getContext(), (lat, lng) -> {
                                        if (lat != null && lng != null) {
                                            Map<String, Object> locationData = new HashMap<>();
                                            locationData.put("latitude", lat);
                                            locationData.put("longitude", lng);
                                            db.collection("events").document(firestoreDocId)
                                                    .collection("waitingList").document(deviceId)
                                                    .update(locationData)
                                                    .addOnFailureListener(locErr ->
                                                            Log.e(logTag, "Failed to save location", locErr));
                                        }
                                    });
                                }

                                // Enrich waiting list document with profile data (fire-and-forget)
                                db.collection("users").document(deviceId).get()
                                        .addOnSuccessListener(profileDoc -> {
                                            if (profileDoc.exists()) {
                                                Map<String, Object> profileData = new HashMap<>();
                                                String name = profileDoc.getString("name");
                                                String email = profileDoc.getString("email");
                                                String phone = profileDoc.getString("phone");
                                                if (name != null) profileData.put("name", name);
                                                if (email != null) profileData.put("email", email);
                                                if (phone != null) profileData.put("phone", phone);
                                                if (!profileData.isEmpty()) {
                                                    db.collection("events").document(firestoreDocId)
                                                            .collection("waitingList").document(deviceId)
                                                            .update(profileData)
                                                            .addOnFailureListener(err ->
                                                                    Log.e(logTag, "Failed to enrich waiting list with profile data", err));
                                                }
                                            }
                                        })
                                        .addOnFailureListener(err ->
                                                Log.e(logTag, "Failed to read profile for waiting list enrichment", err));
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

    /**
     * Requests ACCESS_FINE_LOCATION permission upfront if the event requires geolocation.
     * Called immediately after event data loads so the permission dialog appears before
     * the entrant ever taps Register.
     */
    private void requestLocationPermissionIfNeeded() {
        if (!geolocationRequired) return;
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) return;
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
    }

    /**
     * Removes the current device from the event's waiting list and also removes them
     * from the invitedUsers array, then dismisses the dialog.
     *
     * Removing from invitedUsers ensures the event is no longer visible to the
     * entrant if it was a private event they were invited to.
     *
     * @param registerBtn              The register button to show on success.
     * @param cancelBtn                The cancel button to hide on success.
     * @param acceptBtn                The accept button to hide on success.
     * @param declineBtn               The decline button to hide on success.
     * @param textViewAlreadyRegistered TextView to hide on success.
     * @param tvSelectedMessage        The selected status message to hide on success.
     * @param tvAcceptedMessage        The accepted status message to hide on success.
     * @param tvDeclinedMessage        The declined status message to hide on success.
     */
    private void leaveWaitingList(Button registerBtn, Button cancelBtn, Button acceptBtn, Button declineBtn, TextView textViewAlreadyRegistered, TextView tvSelectedMessage, TextView tvAcceptedMessage, TextView tvDeclinedMessage) {
        //  Remove the user from the waiting list sub-collection
        db.collection("events")
                .document(firestoreDocId)
                .collection("waitingList")
                .document(deviceId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    //  Remove the user from the invitedUsers array so they lose access/visibility
                    db.collection("events")
                            .document(firestoreDocId)
                            .update("invitedUsers", com.google.firebase.firestore.FieldValue.arrayRemove(deviceId))
                            .addOnSuccessListener(updateVoid -> {
                                if (!isAdded() || getContext() == null) return;

                                Log.d(logTag, "Successfully left waiting list and removed from invitedUsers");
                                Toast.makeText(getContext(), "You've left the event.", Toast.LENGTH_SHORT).show();


                                dismiss();
                            })
                            .addOnFailureListener(e -> {

                                if (!isAdded() || getContext() == null) return;
                                dismiss();
                            });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || getContext() == null) return;
                    Log.e(logTag, "Something went wrong when leaving", e);
                    Toast.makeText(getContext(), "Something went wrong when leaving", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Sets up a real-time Firestore snapshot listener on the current device's waiting list
     * document and updates the UI based on the entrant's current status.
     *
     * Resets all UI elements to hidden on each snapshot update before showing
     * the appropriate elements. Handles the following statuses:
     *
     *   "selected" — shows congratulations message with accept and decline buttons
     *   "accepted" — shows enrolled message with cancel registration button
     *   "declined" — shows declined message only
     *   "rejected" — shows rejected message with redraw info and leave button
     *   "waiting" — shows already registered text with leave button
     *
     *
     * If the device is not in the waiting list and is invited, shows the private event
     * invite UI. Otherwise shows the register button for regular users.
     *
     * @param registerBtn          The register button for new entrants.
     * @param cancelBtn            The cancel/leave button for registered entrants.
     * @param acceptBtn            The accept lottery invitation button.
     * @param declineBtn           The decline lottery invitation button.
     * @param tvAlreadyRegistered  TextView shown when status is "waiting".
     * @param tvSelectedMessage    TextView shown when status is "selected".
     * @param tvAcceptedMessage    TextView shown when status is "accepted".
     * @param tvDeclinedMessage    TextView shown when status is "declined".
     * @param tvRejectedMessage    TextView shown when status is "rejected".
     * @param tvRedrawMessage      TextView shown alongside rejected status about re-draw.
     * @param eventFull            TextView shown when waiting list is at capacity.
     * @param isInvited            Whether the device is in the event's invitedUsers array.
     * @param invitedMsg           TextView shown when device is invited to private event.
     * @param acceptInvBtn         Button to accept a private event invitation.
     * @param declineInvBtn        Button to decline a private event invitation.
     */
    private void checkStatusAndShowUI(Button registerBtn, Button cancelBtn,
                                      Button acceptBtn,
                                      Button declineBtn,
                                      TextView tvAlreadyRegistered,
                                      TextView tvSelectedMessage,
                                      TextView tvAcceptedMessage,
                                      TextView tvDeclinedMessage,
                                      TextView tvRejectedMessage,
                                      TextView tvRedrawMessage,
                                      TextView eventFull,
                                      boolean isInvited,
                                      TextView invitedMsg,
                                      Button acceptInvBtn,
                                      Button declineInvBtn) {

        statusListener = db.collection("events")
                .document(firestoreDocId)
                .collection("waitingList")
                .document(deviceId)
                .addSnapshotListener((doc, e) -> {

                    if (e != null) {
                        Log.e(logTag, "Listener failed", e);
                        return;
                    }

                    if (!isAdded() || getContext() == null) return;

                    // Make sure everything isnt visible at first
                    registerBtn.setVisibility(View.GONE);
                    cancelBtn.setVisibility(View.GONE);
                    acceptBtn.setVisibility(View.GONE);
                    declineBtn.setVisibility(View.GONE);
                    tvAlreadyRegistered.setVisibility(View.GONE);
                    tvSelectedMessage.setVisibility(View.GONE);
                    tvAcceptedMessage.setVisibility(View.GONE);
                    tvDeclinedMessage.setVisibility(View.GONE);
                    tvRejectedMessage.setVisibility(View.GONE);
                    tvRedrawMessage.setVisibility(View.GONE);
                    invitedMsg.setVisibility(View.GONE);
                    acceptInvBtn.setVisibility(View.GONE);
                    declineInvBtn.setVisibility(View.GONE);

                    //  Determine State
                    if (doc != null && doc.exists()) {
                        // USER IS IN WAITING LIST
                        String status = doc.getString("status");
                        if (status == null) status = "waiting";

                        if (status.equals("selected")) {
                            tvSelectedMessage.setVisibility(View.VISIBLE);
                            acceptBtn.setVisibility(View.VISIBLE);
                            declineBtn.setVisibility(View.VISIBLE);

                        } else if (status.equals("accepted")) {
                            tvAcceptedMessage.setVisibility(View.VISIBLE);
                            cancelBtn.setVisibility(View.VISIBLE);
                            cancelBtn.setText("Cancel Registration");

                        } else if (status.equals("declined")) {
                            tvDeclinedMessage.setVisibility(View.VISIBLE);

                        } else if (status.equals("rejected")) {
                            tvRejectedMessage.setVisibility(View.VISIBLE);
                            tvRedrawMessage.setVisibility(View.VISIBLE);
                            cancelBtn.setVisibility(View.VISIBLE);
                            cancelBtn.setText("Leave waiting list");

                        } else if (status.equals("waiting")) {
                            cancelBtn.setVisibility(View.VISIBLE);
                            cancelBtn.setText("Leave waiting list");
                            tvAlreadyRegistered.setVisibility(View.VISIBLE);
                        }
                  
                    } else {
                        // user not in waiting list
                        if (isInvited) {
                            // User is invited but hasn't joined yet
                            invitedMsg.setVisibility(View.VISIBLE);
                            acceptInvBtn.setVisibility(View.VISIBLE);
                            declineInvBtn.setVisibility(View.VISIBLE);
                        } else {
                            // Regular user, show register button
                            registerBtn.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

        /**
         * Removes active Firestore listeners when the fragment view is destroyed
         * to prevent memory leaks and callbacks on a detached fragment.
         */
        @Override
        public void onDestroyView() {
            super.onDestroyView();
            if (statusListener != null) {
                statusListener.remove();
            }
            if (waitlistCountListener != null) {
                waitlistCountListener.remove();
            }
    }


    /**
     * Updates the waiting list status to "accepted" when the entrant accepts a lottery
     * selection invitation. Hides the accept and decline buttons and shows the
     * accepted message.
     *
     * @param acceptBtn          The accept button to hide on success.
     * @param declineBtn         The decline button to hide on success.
     * @param tvSelectedMessage  The selected message to hide on success.
     * @param tvAcceptedMessage  The accepted message to show on success.
     * @param tvDeclinedMessage  The declined message to hide on success.
     */
    private void acceptSelection(Button acceptBtn, Button declineBtn,  TextView tvSelectedMessage, TextView tvAcceptedMessage, TextView tvDeclinedMessage) {
        db.collection("events").document(firestoreDocId)
                .collection("waitingList").document(deviceId)
                .update("status", "accepted")
                .addOnSuccessListener(v -> {
                    if (!isAdded() || getContext() == null) return;
                    Toast.makeText(getContext(), "You've accepted the invitation!",
                            Toast.LENGTH_SHORT).show();
                    acceptBtn.setVisibility(View.GONE);
                    declineBtn.setVisibility(View.GONE);
                    tvSelectedMessage.setVisibility(View.GONE);
                    tvDeclinedMessage.setVisibility(View.GONE);
                    tvAcceptedMessage.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || getContext() == null) return;
                    Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                });
    }


    /**
     * Updates the waiting list status to "declined" when the entrant declines a lottery
     * selection invitation. Hides the accept and decline buttons and shows the
     * declined message.
     *
     * @param acceptBtn          The accept button to hide on success.
     * @param declineBtn         The decline button to hide on success.
     * @param tvSelectedMessage  The selected message to hide on success.
     * @param tvAcceptedMessage  The accepted message to hide on success.
     * @param tvDeclinedMessage  The declined message to show on success.
     */
    private void declineSelection(Button acceptBtn, Button declineBtn,  TextView tvSelectedMessage, TextView tvAcceptedMessage, TextView tvDeclinedMessage) {
        db.collection("events").document(firestoreDocId)
                .collection("waitingList").document(deviceId)
                .update("status", "declined")
                .addOnSuccessListener(v -> {
                    if (!isAdded() || getContext() == null) return;
                    Toast.makeText(getContext(), "You've declined the invitation!",
                            Toast.LENGTH_SHORT).show();
                    acceptBtn.setVisibility(View.GONE);
                    declineBtn.setVisibility(View.GONE);
                    tvSelectedMessage.setVisibility(View.GONE);
                    tvAcceptedMessage.setVisibility(View.GONE);
                    tvDeclinedMessage.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || getContext() == null) return;
                    Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Displays a popup dialog containing the QR code for the given event ID.
     * Generates the QR code bitmap using QrHelper and shows it in an AlertDialog.
     * Shows an error icon if QR code generation fails.
     *
     * @param eventId The event ID to encode in the QR code.
     */
    private void showQRCodePopup(String eventId) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Event QR Code");

        android.widget.ImageView qrView = new android.widget.ImageView(requireContext());
        qrView.setPadding(40, 40, 40, 40);

        try {
            Bitmap bitmap = QrHelper.generateQrCode(eventId);
            qrView.setImageBitmap(bitmap);
        } catch (Exception e) {
            qrView.setImageResource(android.R.drawable.stat_notify_error);
        }

        builder.setView(qrView);
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }


    /**
     * Handles declining a private event waiting list invitation.
     * Removes the device from the invitedUsers array in Firestore,
     * hides the invite UI, and shows a confirmation dialog before dismissing.
     *
     * @param acceptInvBtn  The accept invite button to hide on success.
     * @param declineInvBtn The decline invite button to hide on success.
     * @param invitedMsg    The invite message TextView to hide on success.
     */
    private void declinePrivInvitation(Button acceptInvBtn, Button declineInvBtn, TextView invitedMsg) {
        db.collection("events").document(firestoreDocId)
                .update("invitedUsers", com.google.firebase.firestore.FieldValue.arrayRemove(deviceId))
                .addOnSuccessListener(v -> {
                    if (!isAdded() || getContext() == null) return;

                    // Hide invite UI
                    // Hide invite UI
                    acceptInvBtn.setVisibility(View.GONE);
                    declineInvBtn.setVisibility(View.GONE);
                    invitedMsg.setVisibility(View.GONE);

                    // Show declined popup
                    new android.app.AlertDialog.Builder(requireContext())
                            .setTitle("Invitation Declined")
                            .setMessage("You have declined the invite.")
                            .setPositiveButton("OK", (dialog, which) -> dismiss())
                            .show();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || getContext() == null) return;
                    Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                });
    }
}