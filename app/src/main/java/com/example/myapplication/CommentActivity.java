package com.example.myapplication;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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


public class CommentActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CommentAdapter adapter;
    private List<Comment> commentList;
    private FirebaseFirestore db;
    private String eventId;

    private EditText commentInput;
    private Button sendButton;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the view
        setContentView(R.layout.whole_comment_list);

        // Get the event ID passed from the EventAdapter
        eventId = getIntent().getStringExtra("eventId");

        // Also get the organizer id from that intent
        String organizerId = getIntent().getStringExtra("organizerId");

        if (eventId == null) {
            Toast.makeText(this, "Error loading comments", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        // Set all the variables
        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerViewComments);
        commentInput = findViewById(R.id.commentInput);
        sendButton = findViewById(R.id.buttonSendComment);
        backButton = findViewById(R.id.btnBack);

        commentList = new ArrayList<>();
        adapter = new CommentAdapter(commentList, organizerId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Show all the comments
        loadComments();

        // Listeners for the button
        sendButton.setOnClickListener(v -> postComment());
        backButton.setOnClickListener(v -> finish());

    }

    private void loadComments(){
        // Load from the comments subcollection in firebase
        db.collection("events").document(eventId).collection("comments")
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
                    db.collection("events").document(eventId).collection("comments")
                            .add(newComment)
                            .addOnSuccessListener(documentReference -> {
                                commentInput.setText(""); // Clear input on success
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to post comment", Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to get user profile", Toast.LENGTH_SHORT).show();
                });
    }
}