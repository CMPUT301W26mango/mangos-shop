package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the activity which shows the comments for a particular event
 * This displays the comment, the user who wrote the comment, and when they wrote it
 * It also allows for organizers and admins to delete comments
 * */
public class CommentActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CommentAdapter adapter;
    private List<Comment> commentList;
    private FirebaseFirestore db;
    private String eventId;

    private EditText commentInput;
    private Button sendButton;
    private ImageButton backButton;

    private String organizerId;

    private String currentCollectionPath;
    private String parentDocumentPath;
    private boolean isAdmin = false;


    /**
     * This function run when the activity is created, it initializes the screen and sets the listeners
     * @param savedInstanceState
     *  It obtains the saved instance as a bundle to restore the previous state
     *
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the view
        setContentView(R.layout.whole_comment_list);

        // Get the event ID passed from the EventAdapter
        eventId = getIntent().getStringExtra("eventId");

        // Also get the organizer id from that intent
        organizerId = getIntent().getStringExtra("organizerId");

        // Check if we were given a specific subcollection to load
        currentCollectionPath = getIntent().getStringExtra("collectionPath");
        parentDocumentPath = getIntent().getStringExtra("parentPath");

        // If currentCollectionPath is null then were at root level
        if (currentCollectionPath == null) {
            currentCollectionPath = "events/" + eventId + "/comments";
        }


        if (eventId == null) {
            Toast.makeText(this, "Error loading comments", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        isAdmin = getIntent().getBooleanExtra("isAdmin", false);


        // Set all the variables
        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerViewComments);
        commentInput = findViewById(R.id.commentInput);
        sendButton = findViewById(R.id.buttonSendComment);
        backButton = findViewById(R.id.btnBack);
        TextView screenTitle = findViewById(R.id.screenTitle);

        if (parentDocumentPath != null) {
            // Get the username
            String parentUsername = getIntent().getStringExtra("parentUsername");

            // Update the UI elements
            screenTitle.setText("Replies for " + parentUsername);
            commentInput.setHint("Reply to " + parentUsername);
            sendButton.setText("Reply");
        }

        commentList = new ArrayList<>();
        adapter = new CommentAdapter(commentList, organizerId,
                comment -> checkRoleAndDelete(comment),
                comment -> openThread(comment),
                (comment, emoji) -> saveReaction(comment, emoji)
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Show all the comments
        loadComments();

        // Listeners for the button
        sendButton.setOnClickListener(v -> postComment());
        backButton.setOnClickListener(v -> finish());

        recyclerView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (adapter.getOpenReactionIndex() != -1) {
                    int oldIndex = adapter.getOpenReactionIndex();
                    adapter.setOpenReactionIndex(-1);
                    adapter.notifyItemChanged(oldIndex);
                }
            }
            return false;
        });

    }

    private void loadComments(){
        // Load from the comments subcollection in firebase
        db.collection(currentCollectionPath)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, eventException) -> {
                    if (eventException != null){
                        Log.e("Firestore Error", "Something went wrong when fetching from firebase", eventException);
                        return;
                    }

                    // Clear the comment List so that we can re add everything from firebase
                    commentList.clear();

                    // Re add everything to the list
                    assert snapshots != null;
                    for(QueryDocumentSnapshot doc: snapshots){
                        // Convert the document into the comment class
                        Comment comment = doc.toObject(Comment.class);
                        comment.setCommentId(doc.getId());
                        commentList.add(comment);
                    }

                    // Refresh the page
                    adapter.notifyDataSetChanged();

                    // Scroll to the bottom when a new comment is loaded
                    if (!commentList.isEmpty()) {
                        recyclerView.scrollToPosition(commentList.size() - 1);
                    }
                });
    }

    private void postComment() {
        String text = commentInput.getText().toString().trim();

        if (text.isEmpty()) {
            return;
        }

        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // First get the device id
        db.collection("users").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {

                    // Default username incase its not found
                    String username = "Anonymous";


                    // Else set the name
                    if (documentSnapshot.exists() && documentSnapshot.contains("name")) {
                        username = documentSnapshot.getString("name");
                    }

                    // Create the comment object
                    Comment newComment = new Comment(text, username, deviceId, Timestamp.now());

                    // post the comment to the event's subcollection
                    db.collection(currentCollectionPath).add(newComment)
                            .addOnSuccessListener(documentReference -> {
                                commentInput.setText(""); // Clear input on success

                                // If this is a nested thread then update the parents reply count
                                if (parentDocumentPath != null) {
                                    String[] segments = parentDocumentPath.split("/");

                                    // The path starts with events/{eventId}
                                    StringBuilder currentPath = new StringBuilder(segments[0] + "/" + segments[1]);

                                    // Loop through the path and add +1 to every comment/reply document in the chain
                                    for (int i = 2; i < segments.length; i += 2) {
                                        currentPath.append("/").append(segments[i]).append("/").append(segments[i+1]);

                                        db.document(currentPath.toString())
                                                .update("replyCount", com.google.firebase.firestore.FieldValue.increment(1));
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to post comment", Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to get user profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkRoleAndDelete(Comment comment) {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Check the users collection to see if they are an organizer first
        db.collection("users").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");

                        // If they are an Organizer OR if they are the original event creator or admin
                        if (isAdmin || "Organizer".equals(role) || deviceId.equals(organizerId)) {
                            showDeleteDialog(comment);
                        }
                        // If they aren't, the method just ends and nothing happens
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CommentActivity", "Failed to check user role", e);
                });
    }

    private void showDeleteDialog(Comment comment) {
        // Make the popup
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete Comment")
                .setMessage("Are you sure you want to delete this comment?")
                .setPositiveButton("Delete", (dialog, which) -> {

                    db.collection(currentCollectionPath).document(comment.getCommentId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Comment deleted", Toast.LENGTH_SHORT).show();

                                // Decrement the reply count for EVERY parent in the chain
                                if (parentDocumentPath != null) {
                                    String[] segments = parentDocumentPath.split("/");
                                    StringBuilder currentPath = new StringBuilder(segments[0] + "/" + segments[1]);

                                    for (int i = 2; i < segments.length; i += 2) {
                                        currentPath.append("/").append(segments[i]).append("/").append(segments[i+1]);

                                        db.document(currentPath.toString())
                                                .update("replyCount",
                                                        com.google.firebase.firestore.FieldValue.increment(-1));
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to delete comment", Toast.LENGTH_SHORT).show();
                            });

                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void openThread(Comment comment) {
        Intent intent = new Intent(this, CommentActivity.class);
        intent.putExtra("eventId", eventId);
        intent.putExtra("organizerId", organizerId);
        intent.putExtra("collectionPath", currentCollectionPath + "/" + comment.getCommentId() + "/replies");
        intent.putExtra("parentPath", currentCollectionPath + "/" + comment.getCommentId());
        intent.putExtra("parentUsername", comment.getUserName());
        intent.putExtra("isAdmin", isAdmin);

        startActivity(intent);
    }

    private void saveReaction(Comment comment, String emoji) {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        Object updateValue;

        if (emoji == null) {
            updateValue = com.google.firebase.firestore.FieldValue.delete();
        } else {
            updateValue = emoji;
        }


        // use dot notation to update just this specific users reaction inside the map
        db.collection(currentCollectionPath).document(comment.getCommentId())
                .update("reactions." + deviceId, updateValue)
                .addOnSuccessListener(aVoid -> {
                    // the snapshot will automatically refresh
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add reaction", Toast.LENGTH_SHORT).show();
                });
    }
}