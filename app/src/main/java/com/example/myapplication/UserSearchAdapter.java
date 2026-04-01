package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserViewHolder> {

    private List<UserProfiles> userList;
    private Context context;
    private String eventId;

    private String eventName;
    private boolean isPrivate;

    private boolean isCoOrg;

    public UserSearchAdapter(Context context, List<UserProfiles> userList, String eventId, String eventName, boolean isPrivate, boolean isCoOrg) {
        this.context = context;
        this.userList = userList;
        this.eventId = eventId;
        this.eventName = eventName;
        this.isPrivate = isPrivate;
        this.isCoOrg = isCoOrg;
    }

    public void updateList(List<UserProfiles> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_search, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserProfiles user = userList.get(position);

        holder.tvName.setText(user.getName());

        // Handle the Invite Click
        holder.itemView.setOnClickListener(v -> {
            String[] options;
            if (isPrivate && !isCoOrg) {
                options = new String[]{"Send Invite", "Make Co-Organizer"};
            } else if (isPrivate && isCoOrg) {
                options = new String[]{"Send Invite"}; // co-org can invite but not promote
            } else {
                options = new String[]{"Make Co-Organizer"}; // public event, owner only
            }

            new android.app.AlertDialog.Builder(context)
                    .setTitle(user.getName())
                    .setItems(options, (dialog, which) -> {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        boolean isInvitingToPrivate = isPrivate && which == 0;

                        if (isInvitingToPrivate) {
                            // send invitation to private event
                            db.collection("events").document(eventId)
                                    .update("invitedUsers", com.google.firebase.firestore.FieldValue.arrayUnion(user.getDeviceId()))
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, "Invite sent to " + user.getName(), Toast.LENGTH_SHORT).show();

                                        // Trigger Notification
                                        NotificationHelper.sendNotification(
                                                user.getDeviceId(),
                                                eventId,
                                                eventName,
                                                "Private Invitation",
                                                "You are invited to join the private waiting list for "
                                        );
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to send invite", Toast.LENGTH_SHORT).show());

                        } else {
                            // make user co-organizer
                            db.collection("events").document(eventId)
                                    .update("coOrganizers", com.google.firebase.firestore.FieldValue.arrayUnion(user.getDeviceId()))
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, user.getName() + " is now a co-organizer", Toast.LENGTH_SHORT).show();

                                        // Trigger Notification
                                        NotificationHelper.sendNotification(
                                                user.getDeviceId(),
                                                eventId,
                                                eventName,
                                                "Co-Organizer Invite",
                                                "You have been added as a co-organizer for "
                                        );
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to assign co-organizer", Toast.LENGTH_SHORT).show());
                        }
                    })
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_user_name);
        }
    }
}