package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Adapter for the RecyclerView in UserSearchActivity that displays user search results.
 * * This adapter manages the visualization of UserProfiles and handles the logic for
 * inviting users to private events or promoting them to co-organizers. It dynamically
 * adjusts the available actions based on the event's privacy settings
 * and the current user's permissions.
 *  @author Sayuj
 */

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserViewHolder> {

    private List<UserProfiles> userList;
    private Context context;
    private String eventId;

    private String eventName;
    private boolean isPrivate;

    private boolean isCoOrg;

    /**
     * Constructs a new UserSearchAdapter.
     *
     * @param context   the activity context used for inflating layouts and showing dialogs
     * @param userList  the initial list of users to display
     * @param eventId   the unique Firestore ID of the event being managed
     * @param eventName the name of the event for notification purposes
     * @param isPrivate true if the event is private, false otherwise
     * @param isCoOrg   true if the current user is a co-organizer, false if they are the owner
     */
    public UserSearchAdapter(Context context, List<UserProfiles> userList, String eventId, String eventName, boolean isPrivate, boolean isCoOrg) {
        this.context = context;
        this.userList = userList;
        this.eventId = eventId;
        this.eventName = eventName;
        this.isPrivate = isPrivate;
        this.isCoOrg = isCoOrg;
    }

    /**
     * Updates the data set and refreshes the RecyclerView.
     *
     * @param newList the new list of UserProfiles to be displayed
     */
    public void updateList(List<UserProfiles> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }


    /**
     * Called when RecyclerView needs a new UserViewHolder to represent an item.
     *
     * @param parent   the ViewGroup into which the new View will be added
     * @param viewType the view type of the new View
     * @return a new UserViewHolder that holds the View for a user item
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_search, parent, false);
        return new UserViewHolder(view);
    }

    /**
     * Binds user data to the views inside the ViewHolder and sets up the click interaction.
     * * Logic inside the click listener determines if a user can be invited to a private
     * event or made a co-organizer based on the flags provided in the constructor.
     *
     * @param holder   the ViewHolder which should be updated
     * @param position the position of the item within the adapter's data set
     */
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

    /**
     * @return the total number of items in the user list
     */
    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * Sets a new event name to be used for notification messages.
     *
     * @param eventName the new title of the event
     */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    /**
     * ViewHolder class for user search items.
     */
    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_user_name);
        }
    }
}