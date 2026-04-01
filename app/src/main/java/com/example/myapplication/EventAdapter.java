package com.example.myapplication;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.FragmentManager;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * This Class was written with the help of the Android Developer documentation, <a href="https://developer.android.com/develop/ui/views/layout/recyclerview#java">...</a>
 * This Class was written with the help of Professor SluitIer from YouTube, <a href="https://www.youtube.com/watch?v=4-hK6qZv56U&list=PLhPyEFL5u-i1jAc79cJ2j8pDZFEyvpoH_&index=5">...</a>
 * This is a RecyclerView adapter that allows for a scrollable list of events
 * This loads the event information that will be showed on the event card
 * Also Listens for a click of the event and opens the event details
 * */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    // The events
    List<Event> eventList;
    FragmentManager fragmentManager;
    private FirebaseFirestore db;

    /**
    * This is the Constructor for the Adapter, it initializes the event list and the fragment manager
    * @param
     * eventList:
     *   The list of events
     * @param fragmentManager
     *          The fragment mangager for the eventDetails
    * */

    public EventAdapter(List<Event> eventList, FragmentManager fragmentManager){


        this.eventList = eventList;
        this.fragmentManager = fragmentManager;
        this.db = FirebaseFirestore.getInstance();
    }


    /**
     * This function makes the view for each item in the list
     * @param
     *  parent
     *      Container that holds these events
     * @param
     *   ViewType
     *      Type of view that is being shown
     *
     * @return
     *  An instance of the holder for an EventsView
     *
     * */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int ViewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);

        return new EventViewHolder(view);
    }

    /**
     * This function fills a view with the correct data to show, it also detects the click on an event
     * and opens the event details
     * @param
     *  holder
     *      Holder that contains the element for the UI
     * @param
     *   position
     *      Position of the event
     *
     * */
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull EventAdapter.EventViewHolder holder, int position) {
        Event event = eventList.get(position);


        holder.eventName.setText(event.getTitle());
        holder.location.setText(event.getLocation());

        if (event.getRegEnd() != null){
            SimpleDateFormat closeDate = new SimpleDateFormat("MMM dd", Locale.getDefault());
            String closeDateString = closeDate.format(event.getRegEnd().toDate());
            holder.deadline.setText("Registration Closes: " + closeDateString);
        }

        holder.organizer.setText("Hosted by: " + event.getOrganizerName());


        if (event.getPosterURL() != null && !event.getPosterURL().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(event.getPosterURL())
                    .centerCrop()
                    .into(holder.imageViewPoster);
        }

        holder.eventStatus.setVisibility(View.GONE);

        String deviceId = Settings.Secure.getString(
                holder.itemView.getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        String firestoreDocId = event.getId();
        if (firestoreDocId != null && !firestoreDocId.isEmpty()) {
            db.collection("events")
                    .document(firestoreDocId)
                    .collection("waitingList")
                    .document(deviceId)
                    .addSnapshotListener((doc, e) -> {

                        if (e != null || doc == null) return;


                        com.google.android.material.card.MaterialCardView card =
                                (com.google.android.material.card.MaterialCardView) holder.eventCardRoot;

                        if (doc != null && doc.exists()) {
                            String status = doc.getString("status");
                            if (status == null) status = "waiting";

                            holder.eventStatus.setVisibility(View.VISIBLE);



                            card.setStrokeWidth(8);

                            if (status.equals("selected")) {
                                holder.eventStatus.setText("Status: Selected");
                                holder.eventStatus.setTextColor(Color.parseColor("#FFBF00"));
                                card.setStrokeColor(Color.parseColor("#FFBF00"));
                            } else if (status.equals("accepted")) {
                                holder.eventStatus.setText("Status: Accepted");
                                holder.eventStatus.setTextColor(Color.parseColor("#008000"));
                                card.setStrokeColor(Color.parseColor("#008000"));
                            } else if (status.equals("rejected")) {
                                holder.eventStatus.setText("Status: Rejected");
                                holder.eventStatus.setTextColor(Color.parseColor("#FF0000"));
                                card.setStrokeColor(Color.parseColor("#FF0000"));
                            } else if (status.equals("waiting")) {
                                holder.eventStatus.setText("Status: Waiting");
                                holder.eventStatus.setTextColor(Color.parseColor("#000000"));
                                card.setStrokeColor(Color.parseColor("#000000")); // Black Border
                            }

                        } else {
                            // user not in waiting list
                            // Check if they are in the invitedUsers array of the main event
                            List<String> invitedUsers = event.getInvitedUsers();

                            if (invitedUsers != null && invitedUsers.contains(deviceId)) {
                                // show invited status
                                holder.eventStatus.setVisibility(View.VISIBLE);
                                holder.eventStatus.setText("Status: Invited");
                                holder.eventStatus.setTextColor(Color.parseColor("#800080")); // Purple

                                card.setStrokeWidth(8);
                                card.setStrokeColor(Color.parseColor("#800080")); // Purple Border
                            } else {
                                // regular event not join
                                holder.eventStatus.setVisibility(View.GONE);
                                card.setStrokeWidth(0);
                            }
                        }
                    });
        }


        // Open the comments when the image is clicked
        holder.viewCommentsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), CommentActivity.class);
            // Pass the actual document ID so the Activity knows which event's comments to fetch
            intent.putExtra("eventId", event.getId());
            intent.putExtra("organizerId", event.getDeviceId());
            v.getContext().startActivity(intent);
        });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("eventId", event.getId());
                EventDetailsFragment fragment = new EventDetailsFragment();
                fragment.setArguments(bundle);
                fragment.show(fragmentManager, "eventDetails");
            }
        });

    }

    /**
     * Returns how many events right now
     * @return eventList
     *      Size of the test
     * */
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * Creates the object for the view that the user sees with all the UI elements
     * */

    static class EventViewHolder extends RecyclerView.ViewHolder{
        ImageView imageViewPoster;
        TextView eventName;
        TextView location;
        TextView deadline;

        View eventCardRoot;

        TextView organizer;
        TextView eventStatus;
        ImageButton viewCommentsBtn;

        /**
         * Initializes all the UI elements in this object
         * @param itemView
         *  View of all the UI elements thaat the user should see
         * */
        public EventViewHolder(@NonNull View itemView){
            super(itemView);
            imageViewPoster = itemView.findViewById(R.id.imageViewPoster);
            eventName = itemView.findViewById(R.id.textViewEventName);
            location = itemView.findViewById(R.id.textViewLocation);
            deadline = itemView.findViewById(R.id.textViewDeadline);
            organizer = itemView.findViewById(R.id.textViewOrganizer);
            eventStatus = itemView.findViewById(R.id.eventStatus);
            eventCardRoot = itemView.findViewById(R.id.eventCardRoot);
            viewCommentsBtn = itemView.findViewById(R.id.viewCommentsBtn);

        }
    }

}