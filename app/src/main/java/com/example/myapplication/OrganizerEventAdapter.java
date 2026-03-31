package com.example.myapplication;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Adapter class for the RecycleView used in the Organizer Dashboard.
 * This class handles the mapping of a list of Event objects into the
 * visual representations (cards) displayed on the screen.
 * @author Sayuj
 */
public class OrganizerEventAdapter extends RecyclerView.Adapter<OrganizerEventAdapter.EventViewHolder> {

    private List<Event> eventList;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    private OnEventClickListener clickListener;

    /**
     * Constructs a new OrganizerEventAdapter.
     * @param eventList The initial list of events to be displayed in the dashboard.
     * @param clickListener The listener to handle event card click events.
     */
    public OrganizerEventAdapter(List<Event> eventList, OnEventClickListener clickListener) {
        this.eventList = eventList;
        this.clickListener = clickListener;
    }

    /**
     * Called when RecyclerView needs a new EventViewHolder to represent an item.
     * This inflates the individual event card layout from XML.
     * @param parent   The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new EventViewHolder that holds the View for each event card.
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_card, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * @param holder   The ViewHolder which should be updated.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        // put event data into card, move from object to ui
        holder.titleText.setText(event.getTitle());
        holder.locationText.setText("Location: " + event.getLocation());
        holder.capacityText.setText("Capacity: " + event.getCapacity());

        if (event.getPosterURL() != null && !event.getPosterURL().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(event.getPosterURL())
                    .centerCrop()
                    .into(holder.imagePoster);
        } else {
            holder.imagePoster.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Open the comments if the organizer presses on it
        holder.viewCommentsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), CommentActivity.class);
            intent.putExtra("eventId", event.getId());
            intent.putExtra("organizerId", event.getDeviceId());
            v.getContext().startActivity(intent);
        });

        holder.itemView.setOnClickListener(v -> {
            clickListener.onEventClick(event);
        });
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * @return The size of the current event list, or 0 if the list is null.
     */
    @Override
    public int getItemCount() {
        return eventList != null ? eventList.size() : 0;
    }


    /**
     * ViewHolder class that caches references to the components of an event card.
     * This improves performance by avoiding repeated calls to findViewById
     * by caching potentially expensive findViewById results.
     */
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, locationText, capacityText;

        ImageView imagePoster;
        ImageButton viewCommentsBtn;
        /**
         * Initializes the components found in the event card layout.
         * @param itemView The view of the individual event card.
         */
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.card_event_name);
            locationText = itemView.findViewById(R.id.card_event_location);
            capacityText = itemView.findViewById(R.id.card_event_capacity);
            imagePoster = itemView.findViewById(R.id.card_event_image);
            viewCommentsBtn = itemView.findViewById(R.id.viewCommentsBtnForOrganizers);
        }
    }
}