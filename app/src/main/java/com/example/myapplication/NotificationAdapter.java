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
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Adapter for the notifications RecyclerView.
 * Handles two notification types:
 * - invitation to waiting list of private event
 * - invitation to co-organize event
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationItem> notificationList;
    private FragmentManager fragmentManager;
    private FirebaseFirestore db;
    private String deviceId;

    public NotificationAdapter(List<NotificationItem> notificationList,
                               FragmentManager fragmentManager,
                               FirebaseFirestore db,
                               String deviceId) {
        this.notificationList = notificationList;
        this.fragmentManager = fragmentManager;
        this.db = db;
        this.deviceId = deviceId;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem item = notificationList.get(position);


        holder.notificationName.setText(item.getNotiName());

        if (!item.isRead()) {
            holder.unreadDot.setVisibility(View.VISIBLE);
        } else {
            holder.unreadDot.setVisibility(View.GONE);
        }

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


        String description = item.getDescription() + item.getEventName() + "\n Click here to view more details.";

        SpannableString ss = new SpannableString(description);

        // Define the ClickableSpan
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Initialize  fragment
                EventDetailsFragment fragment = new EventDetailsFragment();

                item.setRead(true);
                holder.unreadDot.setVisibility(View.GONE);

                db.collection("users")
                        .document(deviceId)
                        .collection("notifications")
                        .document(item.getId())
                        .update("read", true)
                        .addOnSuccessListener(aVoid -> {
                            // success (optional)
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firestore", "Failed to update read status", e);
                        });


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


        int startIndex = description.indexOf("here");
        int endIndex = startIndex + 4;

        if (startIndex != -1) {
            ss.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        holder.notificationDesc.setText(ss);
        holder.notificationDesc.setMovementMethod(LinkMovementMethod.getInstance());

    }


    @Override
    public int getItemCount() {
        return notificationList != null ? notificationList.size() : 0;
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {

        TextView notificationName;
        TextView notificationDesc;

        TextView notificationTime;

        View unreadDot;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationName = itemView.findViewById(R.id.notification_name);
            notificationDesc = itemView.findViewById(R.id.notification_desc);
            notificationTime = itemView.findViewById(R.id.notification_timestamp);
            unreadDot = itemView.findViewById(R.id.unread_indicator_dot);

        }
    }
}