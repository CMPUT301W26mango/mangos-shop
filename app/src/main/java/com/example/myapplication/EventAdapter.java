package com.example.myapplication;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.FragmentManager;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * This Class was written with the help of the Android Developer documentation, <a href="https://developer.android.com/develop/ui/views/layout/recyclerview#java">...</a>
 * This Class was written with the help of Professor SluitIer from YouTube, <a href="https://www.youtube.com/watch?v=4-hK6qZv56U&list=PLhPyEFL5u-i1jAc79cJ2j8pDZFEyvpoH_&index=5">...</a>
 * It allows for a scrollable list of events
 * */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    // The events
    List<Event> eventList;
    FragmentManager fragmentManager;

    public EventAdapter(List<Event> eventList, FragmentManager fragmentManager){
        this.eventList = eventList;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int ViewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);

        return new EventViewHolder(view);
    }

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
        } else {
            holder.imageViewPoster.setImageDrawable(null);
            holder.imageViewPoster.setBackgroundColor(0xFFE8E8E8);
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("eventId", event.getQrValue());
                EventDetailsFragment fragment = new EventDetailsFragment();
                fragment.setArguments(bundle);
                fragment.show(fragmentManager, "eventDetails");
            }
        });

    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder{
        ImageView imageViewPoster;
        TextView eventName;
        TextView location;
        TextView deadline;

        TextView organizer;
        public EventViewHolder(@NonNull View itemView){
            super(itemView);
            imageViewPoster = itemView.findViewById(R.id.imageViewPoster);
            eventName = itemView.findViewById(R.id.textViewEventName);
            location = itemView.findViewById(R.id.textViewLocation);
            deadline = itemView.findViewById(R.id.textViewDeadline);
            organizer = itemView.findViewById(R.id.textViewOrganizer);
        }
    }


}