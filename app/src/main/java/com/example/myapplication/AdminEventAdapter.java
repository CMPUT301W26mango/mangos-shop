/**
 * RecyclerView adapter for displaying event items on the admin browse events screen.
 *
 * Role in application:
 * - Presentation-layer adapter for admin event browsing.
 */

package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.AdminEventViewHolder> {

    public interface OnEventClickListener {
        void onEventClick(AdminEventItem eventItem);
    }

    private List<AdminEventItem> eventList;
    private OnEventClickListener listener;

    public AdminEventAdapter(List<AdminEventItem> eventList, OnEventClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_event, parent, false);
        return new AdminEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminEventViewHolder holder, int position) {
        AdminEventItem eventItem = eventList.get(position);
        holder.textViewEventTitle.setText(eventItem.getTitle());

        String location = eventItem.getLocation();
        if (location == null || location.isEmpty()) {
            location = "No location";
        }
        holder.textViewEventLocation.setText("Location: " + location);

        String organizer = eventItem.getOrganizerName();
        if (organizer == null || organizer.isEmpty()) {
            organizer = "Unknown organizer";
        }
        holder.textViewEventOrganizer.setText("Organizer: " + organizer);

        holder.itemView.setOnClickListener(v -> listener.onEventClick(eventItem));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class AdminEventViewHolder extends RecyclerView.ViewHolder {
        TextView textViewEventTitle, textViewEventLocation, textViewEventOrganizer;

        public AdminEventViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewEventTitle = itemView.findViewById(R.id.textViewEventTitle);
            textViewEventLocation = itemView.findViewById(R.id.textViewEventLocation);
            textViewEventOrganizer = itemView.findViewById(R.id.textViewEventOrganizer);
        }
    }
}