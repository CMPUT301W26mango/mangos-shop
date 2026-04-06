package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 *
 * Displays one entrant name per row. Holds full EnrolledEntrant objects
 * (name, email, phone, enrolment date) so the same list can be passed
 * directly to CsvExportHelper without a second Firestore read.
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

    /** Binds the entrant's name to the row. */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EnrolledEntrant entrant = entrants.get(position);
        holder.tvEntrantName.setText(entrants.get(position).getName());

        if (entrant.getDeviceId() != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(entrant.getDeviceId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        String profileUrl = doc.getString("profileImageUrl");
                        if (profileUrl != null && !profileUrl.isEmpty()) {
                            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                                    .load(profileUrl)
                                    .circleCrop()
                                    .placeholder(android.R.drawable.sym_def_app_icon)
                                    .into(holder.ivProfileIcon);
                        }
                    });
        }
    }

    /** Returns the number of entrants currently on the waiting list. */
    @Override
    public int getItemCount() {
        return entrants.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEntrantName;
        android.widget.ImageView ivProfileIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEntrantName = itemView.findViewById(R.id.tvEntrantName);
            ivProfileIcon = itemView.findViewById(R.id.ivProfileIcon);
        }
    }
}