package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * US 02.02.01 - WaitingListAdapter
 *
 * Displays entrant names in a RecyclerView. Display only — no action buttons.
 * (Replace/Cancel buttons belong to US 02.05.03 and US 02.06.04)
 */
public class WaitingListAdapter extends RecyclerView.Adapter<WaitingListAdapter.ViewHolder> {

    private final List<String> entrantNames;

    public WaitingListAdapter(List<String> entrantNames) {
        this.entrantNames = entrantNames;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_waitlist_entrant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvEntrantName.setText(entrantNames.get(position));
    }

    @Override
    public int getItemCount() {
        return entrantNames.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEntrantName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEntrantName = itemView.findViewById(R.id.tvEntrantName);
        }
    }
}