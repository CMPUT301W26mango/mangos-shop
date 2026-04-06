package com.example.myapplication;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

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

    private OnCommentLongClickListener longClickListener;
    private OnCommentClickListener clickListener;


    /**
     * This is the constructor for the adapter, it sets the comment information, the organizer, and a listener for a delete feature
     * @param commentList
     *  This is an array of comments, where each comment is an object of the comment class
     * @param longClickListener
     *  This is a listener to a long click for a comment, it allows for the feature of deleting comments for admins and organizers
     * @param organizerId
     *  This is the id of the organizer that organized the event in which the comments are for
     * */
    public CommentAdapter(List<Comment> commentList, String organizerId, OnCommentLongClickListener longClickListener, OnCommentClickListener clickListener){
        this.commentList = commentList;
        this.organizerId = organizerId;
        this.longClickListener = longClickListener;
        this.clickListener = clickListener;
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
            holder.organizerTag.setVisibility(View.VISIBLE); // Show it
        } else {
            holder.organizerTag.setVisibility(View.GONE); // Hide it
        }

        // Write the timestamp in readable language
        // This was written with the help from stack overflow: https://stackoverflow.com/questions/7082518/android-getrelativetime-example
        if (comment.getTimestamp() != null){
            long millisecondTime = comment.getTimestamp().toDate().getTime();

            // Generate the readable string
            CharSequence readableTime = android.text.format.DateUtils.getRelativeTimeSpanString(
                    millisecondTime,
                    System.currentTimeMillis(),
                    android.text.format.DateUtils.MINUTE_IN_MILLIS
            );

            holder.timestampText.setText(readableTime);

        } else {
            // Do it just now as a default
            holder.timestampText.setText("Just Now");
        }

        // Handle the Reply UI
        if (comment.getReplyCount() > 0) {
            holder.replyIndicator.setVisibility(View.VISIBLE);
            holder.replyIndicator.setText(comment.getReplyCount() + " replies");
        } else {
            holder.replyIndicator.setVisibility(View.GONE);
        }

        // Handle the standard click to open a thread
        holder.itemView.setOnClickListener(v -> {
            clickListener.onCommentClick(comment);
        });


        // Listen for a long click to open a delete comment dialogue
        holder.itemView.setOnLongClickListener(v -> {
            longClickListener.onCommentLongClick(comment);
            return true;
        });


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

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.commentingUser);
            commentText = itemView.findViewById(R.id.commentContent);
            organizerTag = itemView.findViewById(R.id.isOrganizerTag);
            timestampText = itemView.findViewById(R.id.commentTimestamp);
            replyIndicator = itemView.findViewById(R.id.replyIndicatorText);
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

}
