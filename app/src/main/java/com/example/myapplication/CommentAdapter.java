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

    public CommentAdapter(List<Comment> commentList, String organizerId){
        this.commentList = commentList;
        this.organizerId = organizerId;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create the view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_card, parent, false);
        return new CommentViewHolder(view);
    }

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
    }


    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        TextView commentText;
        TextView organizerTag;
        TextView timestampText;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.commentingUser);
            commentText = itemView.findViewById(R.id.commentContent);
            organizerTag = itemView.findViewById(R.id.isOrganizerTag);
            timestampText = itemView.findViewById(R.id.commentTimestamp);
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

}
