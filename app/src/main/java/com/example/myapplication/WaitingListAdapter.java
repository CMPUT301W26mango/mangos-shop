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
 * Uses EnrolledEntrant objects so the full data (name, email, phone, date)
 * is available for CSV export (US 02.06.05), but only name is shown in the UI.
 */
public class WaitingListAdapter extends RecyclerView.Adapter<WaitingListAdapter.ViewHolder> {

    private final List<EnrolledEntrant> entrants;

    public WaitingListAdapter(List<EnrolledEntrant> entrants) {
        this.entrants = entrants;
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
        holder.tvEntrantName.setText(entrants.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return entrants.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEntrantName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEntrantName = itemView.findViewById(R.id.tvEntrantName);
        }
    }
}