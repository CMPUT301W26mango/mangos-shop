package com.example.myapplication;
import android.text.format.DateUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a RecyclerView adapter that allows for a scrollable list of comments
 * This loads the comment information that will be showed for each comment card
 * */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder>{
    private List<Comment> commentList;
    private String organizerId;

    // Set the interface up for a pop up to delete comment
    public interface OnCommentLongClickListener {
        void onCommentLongClick(Comment comment);
    }

    // To open the replies
    public interface OnCommentClickListener {
        void onCommentClick(Comment comment);
    }

    // To react to a comment
    public interface OnCommentReactListener {
        void onCommentReact(Comment comment, String emoji);
    }

    private OnCommentLongClickListener longClickListener;
    private OnCommentClickListener clickListener;
    private OnCommentReactListener reactListener;

    private int openReactionIndex = -1;




    /**
     * This is the constructor for the adapter, it sets the comment information, the organizer, and a listener for a delete feature
     * @param commentList
     *  This is an array of comments, where each comment is an object of the comment class
     * @param longClickListener
     *  This is a listener to a long click for a comment, it allows for the feature of deleting comments for admins and organizers
     * @param organizerId
     *  This is the id of the organizer that organized the event in which the comments are for
     * */
    public CommentAdapter(List<Comment> commentList, String organizerId, OnCommentLongClickListener longClickListener, OnCommentClickListener clickListener, OnCommentReactListener reactListener){
        this.commentList = commentList;
        this.organizerId = organizerId;
        this.longClickListener = longClickListener;
        this.clickListener = clickListener;
        this.reactListener = reactListener;
    }


    /**
     * Creates and returns a view holder for the comments
     * Called when the recycler view needs to make a new view (comment)
     * @param parent
     *  The view group that the new view will be attached to
     * @param viewType
     *  Type of view
     * @return CommentViewHolder
     *  The new comment view holder that has the inflated view
     * */
    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create the view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_card, parent, false);
        return new CommentViewHolder(view);
    }

    /**
     * Binds a comment object to the CommentViewHolder
     * It also sets the time that the comment was sent, whether its an organizer and calls the function to handle a long press (used for deleting)
     * @param holder
     *  The view holder in which to attach the comment
     * @param position
     *  Position of the item in the comment list
     * */
    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.userName.setText(comment.getUserName());
        holder.commentText.setText(comment.getCommentText());

        // Check if the comment writer is an organizer or not
        if (comment.getDeviceId() != null && comment.getDeviceId().equals(organizerId)) {
            holder.organizerTag.setVisibility(View.VISIBLE);
        } else {
            holder.organizerTag.setVisibility(View.GONE);
        }

        // Write the timestamp in readable language
        if (comment.getTimestamp() != null){
            long millisecondTime = comment.getTimestamp().toDate().getTime();
            CharSequence readableTime = DateUtils.getRelativeTimeSpanString(
                    millisecondTime, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
            holder.timestampText.setText(readableTime);
        } else {
            holder.timestampText.setText("Just Now");
        }

        // Handle the Reply UI
        if (comment.getReplyCount() > 0) {
            holder.replyIndicator.setVisibility(View.VISIBLE);
            holder.replyIndicator.setText(comment.getReplyCount() + " replies");
        } else {
            holder.replyIndicator.setVisibility(View.GONE);
        }

// Show the reactions tally
        Map<String, String> reactions = comment.getReactions();
        if (reactions != null && !reactions.isEmpty()) {
            Map<String, Integer> tallyMap = new HashMap<>();
            for (String emoji : reactions.values()) {
                tallyMap.put(emoji, tallyMap.getOrDefault(emoji, 0) + 1);
            }
            StringBuilder tallyString = new StringBuilder();
            for (Map.Entry<String, Integer> entry : tallyMap.entrySet()) {
                tallyString.append(entry.getKey()).append(" ").append(entry.getValue()).append("   ");
            }
            holder.reactionTallyText.setText(tallyString.toString().trim());
            holder.reactionTallyCard.setVisibility(View.VISIBLE);
        } else {
            holder.reactionTallyCard.setVisibility(View.GONE);
        }

        if (openReactionIndex == position) {
            holder.reactionPopupCard.setVisibility(View.VISIBLE);
        } else {
            holder.reactionPopupCard.setVisibility(View.GONE);
        }


        String myDeviceId = android.provider.Settings.Secure.getString(holder.itemView.getContext().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        String myReaction = comment.getReactions().get(myDeviceId);

        holder.reactionGroup.setOnCheckedStateChangeListener(null);
        holder.reactionGroup.clearCheck();

        if (myReaction != null) {
            switch (myReaction) {
                case "👍": holder.reactionGroup.check(R.id.chipThumbsUp); break;
                case "❤️": holder.reactionGroup.check(R.id.chipHeart); break;
                case "😂": holder.reactionGroup.check(R.id.chipLaugh); break;
                case "👎": holder.reactionGroup.check(R.id.chipThumbsDown); break;
            }
        }


        holder.reactionGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                reactListener.onCommentReact(comment, null);
            } else {
                Chip selectedChip = group.findViewById(checkedIds.get(0));
                String emoji = selectedChip.getText().toString();
                reactListener.onCommentReact(comment, emoji);
            }


        });


        GestureDetector gestureDetector = new GestureDetector(holder.itemView.getContext(), new android.view.GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) { return true; }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos == RecyclerView.NO_POSITION) return false;

                // Single tapping closes reaction popup
                if (openReactionIndex != -1) {
                    int oldIndex = openReactionIndex;
                    openReactionIndex = -1;
                    notifyItemChanged(oldIndex);
                } else {
                    // Act regular to open replies
                    clickListener.onCommentClick(commentList.get(currentPos));
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos == RecyclerView.NO_POSITION) return false;

                int previousIndex = openReactionIndex;

                if (openReactionIndex == currentPos) {
                    openReactionIndex = -1; // Close if tapping the currently open one
                } else {
                    openReactionIndex = currentPos; // Open the new one
                }

                if (previousIndex != -1){
                    notifyItemChanged(previousIndex);
                }
                notifyItemChanged(currentPos);
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    longClickListener.onCommentLongClick(commentList.get(currentPos));
                }
            }
        });

        holder.itemView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }


    /**
     * Defines what is in the comment view holder, it is an extension of a recycler view which allows for scrolling and recycling views
     * */
    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        TextView commentText;
        TextView organizerTag;
        TextView timestampText;
        TextView replyIndicator;
        TextView reactionTallyText;
        ChipGroup reactionGroup;
        CardView reactionPopupCard;
        CardView reactionTallyCard;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.commentingUser);
            commentText = itemView.findViewById(R.id.commentContent);
            organizerTag = itemView.findViewById(R.id.isOrganizerTag);
            timestampText = itemView.findViewById(R.id.commentTimestamp);
            replyIndicator = itemView.findViewById(R.id.replyIndicatorText);
            reactionGroup = itemView.findViewById(R.id.reactionChipGroup);
            reactionTallyText = itemView.findViewById(R.id.reactionTallyText);
            reactionPopupCard = itemView.findViewById(R.id.reactionPopupCard);
            reactionTallyCard = itemView.findViewById(R.id.reactionTallyCard);

        }
    }

    /**
     * Calculates how many comments are in the comment list
     * @return
     *  The size of the comment list
     * */
    @Override
    public int getItemCount() {
        return commentList.size();
    }

    /**
     * This is to know which comment has the reaction pop up opened
     * @return
     *  Index of the comment where the reaction pop up is
     * */
    public int getOpenReactionIndex() {
        return openReactionIndex;
    }

    /**
     * This sets where the reaction popup currently is
     * @param index
     *  The index of the comment that has the popup opened
     * */
    public void setOpenReactionIndex(int index) {
        this.openReactionIndex = index;
    }

}
