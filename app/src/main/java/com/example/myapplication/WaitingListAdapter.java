package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * US 02.02.01 - Adapter for the waiting list RecyclerView.
 *
 * Displays one entrant name per row. Holds full EnrolledEntrant objects
 * (name, email, phone, enrolment date) so the same list can be passed
 * directly to CsvExportHelper without a second Firestore read.
 *
 * Only the name field is shown in the UI - the other fields are
 * invisible to the organizer here but available for CSV export.
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

    /** Binds the entrant's name to the row. Only name is shown in the UI. */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvEntrantName.setText(entrants.get(position).getName());
    }

    /** Returns the number of entrants currently on the waiting list. */
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
