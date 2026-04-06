package com.example.myapplication;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * RecyclerView adapter for displaying notifications to the entrant.
 *
 * Handles the following notification types:
 *   - Invitation to join the waiting list of a private event
 *   - Invitation to be a co-organizer for an event
 *
 * Each notification card shows the notification name, a relative timestamp,
 * a description with a clickable "here" link that opens the EventDetailsFragment,
 * an unread indicator dot, and a delete button to remove the notification.
 *
 * Clicking "here" in the description marks the notification as read in Firestore
 * and opens the corresponding event details popup.
 *
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationItem> notificationList;
    private FragmentManager fragmentManager;
    private FirebaseFirestore db;
    private String deviceId;

    /**
     * Constructs a new NotificationAdapter.
     *
     * @param notificationList The list of NotificationItem objects to display.
     * @param fragmentManager  The FragmentManager used to show EventDetailsFragment.
     * @param db               The FirebaseFirestore instance for read/write operations.
     * @param deviceId         The current device's unique identifier used to scope
     *                         Firestore operations to the correct user.
     */
    public NotificationAdapter(List<NotificationItem> notificationList,
                               FragmentManager fragmentManager,
                               FirebaseFirestore db,
                               String deviceId) {
        this.notificationList = notificationList;
        this.fragmentManager = fragmentManager;
        this.db = db;
        this.deviceId = deviceId;
    }


    /**
     * Called when RecyclerView needs a new NotificationViewHolder.
     * Inflates the item_notification layout for each notification card.
     *
     * @param parent   The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new NotificationViewHolder holding the inflated notification card view.
     */
    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    /**
     * Binds notification data to the ViewHolder at the given position.
     *
     * Sets the notification name, relative timestamp, and description text.
     * The description includes a clickable "here" span that opens EventDetailsFragment
     * for the associated event and marks the notification as read in Firestore.
     *
     * Shows or hides the unread indicator dot based on the notification's read status.
     * Sets up the delete button to remove the notification from Firestore.
     *
     * The clickable "here" span implementation was written with the guidance of Claude AI.
     * Prompt: "How do I make a certain part of a textview lead to another fragment when clicked"
     * Date: 2026-04-02
     *
     * @param holder   The ViewHolder to update with data at the given position.
     * @param position The position of the item in the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        // get notification item
        NotificationItem item = notificationList.get(position);

        // get notification name
        holder.notificationName.setText(item.getNotiName());

        // show unread dot if notification not seen by user, else its invisible
        if (!item.isRead()) {
            holder.unreadDot.setVisibility(View.VISIBLE);
        } else {
            holder.unreadDot.setVisibility(View.GONE);
        }

        // get time and make it relative to user seeing notification
        if (item.getNotiTime() != null) {
            long timeInMillis = item.getNotiTime().toDate().getTime();
            long now = System.currentTimeMillis();

            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                    timeInMillis,
                    now,
                    DateUtils.MINUTE_IN_MILLIS);

            holder.notificationTime.setText(relativeTime);
        } else {
            holder.notificationTime.setText("");
        }

        // create string to store message
        String description = item.getDescription() + " for " + item.getEventName() + "\n Click here to view more details.";

        SpannableString ss = new SpannableString(description);

        // The following code to implement Click "here" is from Claude, "How do I make a certain part of a textview lead to another fragment when clicked", 2026-04-02
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Initialize  fragment
                EventDetailsFragment fragment = new EventDetailsFragment();

                // make notification read after user clicks details
                item.setRead(true);
                holder.unreadDot.setVisibility(View.GONE);

                if (item.getId() != null && !item.getId().equals(item.getEventId())) {
                    db.collection("users")
                            .document(deviceId)
                            .collection("notifications")
                            .document(item.getId())
                            .update("read", true)
                            .addOnFailureListener(e -> Log.e("Firestore", "Failed to update read status", e));
                }

                // Pass eventId
                Bundle args = new Bundle();
                args.putString("eventId", item.getEventId());
                fragment.setArguments(args);

                // Show event details fragment
                fragment.show(fragmentManager, "EventDetailsFragment");
            }

            @Override
            public void updateDrawState(@NonNull android.text.TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);


                int linkColor = holder.itemView.getContext().getColor(R.color.blue);;
                ds.setColor(linkColor);
            }
        };

        // layout index of where to click
        int startIndex = description.indexOf("here");
        int endIndex = startIndex + 4;

        if (startIndex != -1) {
            ss.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        holder.notificationDesc.setText(ss);
        holder.notificationDesc.setMovementMethod(LinkMovementMethod.getInstance());

        holder.clearSingleBtn.setOnClickListener(v -> {
            String notificationId = item.getId();

            db.collection("users")
                    .document(deviceId)
                    .collection("notifications")
                    .document(notificationId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {

                        Toast.makeText(holder.itemView.getContext(), "Notification removed", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("NotificationAdapter", "Error deleting document", e);
                    });
        });

    }


    /**
     * Returns the total number of notifications in the list.
     *
     * @return The size of the notification list, or 0 if the list is null.
     */
    @Override
    public int getItemCount() {
        return notificationList != null ? notificationList.size() : 0;
    }

    /**
     * ViewHolder class that caches references to the UI components of a notification card.
     * Improves RecyclerView performance by avoiding repeated findViewById calls.
     */
    public static class NotificationViewHolder extends RecyclerView.ViewHolder {

        TextView notificationName;
        TextView notificationDesc;

        TextView notificationTime;

        View unreadDot;

        ImageView clearSingleBtn;

        /**
         * Initializes the ViewHolder and binds all UI component references
         * from the notification card layout.
         *
         * @param itemView The inflated view for a single notification card.
         */
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationName = itemView.findViewById(R.id.notification_name);
            notificationDesc = itemView.findViewById(R.id.notification_desc);
            notificationTime = itemView.findViewById(R.id.notification_timestamp);
            unreadDot = itemView.findViewById(R.id.unread_indicator_dot);
            clearSingleBtn = itemView.findViewById(R.id.delete_noti);

        }
    }
}