package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventHistoryAdapter extends RecyclerView.Adapter<EventHistoryAdapter.ViewHolder> {

    private List<EventHistory> historyList;

    public EventHistoryAdapter(List<EventHistory> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EventHistory item = historyList.get(position);
        holder.eventName.setText(item.getEventName());

        // send the user's waiting list status into the capacity text view
        holder.eventStatus.setText("Status: " + item.getStatus());
        holder.eventLocation.setText("Registration Record");
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView eventName;
        TextView eventStatus;
        TextView eventLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.card_event_name);
            // repurposing the capacity textview to show the selected/waiting status
            eventStatus = itemView.findViewById(R.id.card_event_capacity);
            eventLocation = itemView.findViewById(R.id.card_event_location);
        }
    }
}