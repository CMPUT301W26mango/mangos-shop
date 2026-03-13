/**
 * Activity that displays detailed information for a selected event and allows
 * the administrator to remove the event poster image.
 *
 * Role in application:
 * - Admin moderation screen for event content.
 *
 * Outstanding issues:
 * - Remove-image depends on valid posterURL data in Firestore.
 */

package com.example.myapplication;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AdminEventDetailActivity extends AppCompatActivity {

    private TextView title;
    private TextView location;
    private TextView organizer;
    private ImageView eventPoster;
    private Button buttonRemoveImage;

    private FirebaseFirestore db;

    private String eventId;
    private String posterURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_event_detail);

        title = findViewById(R.id.textEventTitle);
        location = findViewById(R.id.textEventLocation);
        organizer = findViewById(R.id.textEventOrganizer);
        eventPoster = findViewById(R.id.imageEventPoster);
        buttonRemoveImage = findViewById(R.id.buttonRemoveImage);

        db = FirebaseFirestore.getInstance();

        eventId = getIntent().getStringExtra("eventId");
        String eventTitle = getIntent().getStringExtra("title");
        String eventLocation = getIntent().getStringExtra("location");
        String eventOrganizer = getIntent().getStringExtra("organizer");
        posterURL = getIntent().getStringExtra("posterURL");

        if (eventTitle == null || eventTitle.isEmpty()) {
            eventTitle = "Untitled Event";
        }
        if (eventLocation == null || eventLocation.isEmpty()) {
            eventLocation = "No location";
        }
        if (eventOrganizer == null || eventOrganizer.isEmpty()) {
            eventOrganizer = "Unknown organizer";
        }

        title.setText(eventTitle);
        location.setText("Location: " + eventLocation);
        organizer.setText("Organizer: " + eventOrganizer);

        loadPoster();

        buttonRemoveImage.setOnClickListener(v -> showRemoveImageDialog());
    }

    private void loadPoster() {
        if (posterURL != null && !posterURL.isEmpty()) {
            Glide.with(this)
                    .load(posterURL)
                    .into(eventPoster);
        } else {
            eventPoster.setImageResource(android.R.color.darker_gray);
        }
    }

    private void showRemoveImageDialog() {
        if (posterURL == null || posterURL.isEmpty()) {
            Toast.makeText(this, "No image to remove", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Remove Image")
                .setMessage("Are you sure you want to remove this event image?")
                .setPositiveButton("Remove", (dialog, which) -> removeImage())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removeImage() {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Missing event ID", Toast.LENGTH_SHORT).show();
            return;
        }

        if (posterURL == null || posterURL.isEmpty()) {
            Toast.makeText(this, "No image to remove", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean looksLikeFirebaseStorageUrl =
                posterURL.startsWith("gs://") ||
                        posterURL.contains("firebasestorage.googleapis.com") ||
                        posterURL.contains("firebasestorage.app");

        if (looksLikeFirebaseStorageUrl) {
            try {
                StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(posterURL);

                imageRef.delete()
                        .addOnSuccessListener(unused -> clearPosterUrlFromFirestore())
                        .addOnFailureListener(e -> {
                            // Even if storage delete fails, still clear the app reference
                            clearPosterUrlFromFirestore();
                        });

            } catch (Exception e) {
                clearPosterUrlFromFirestore();
            }
        } else {
            clearPosterUrlFromFirestore();
        }
    }

    private void clearPosterUrlFromFirestore() {
        db.collection("events")
                .document(eventId)
                .update("posterURL", "")
                .addOnSuccessListener(unused -> {
                    posterURL = "";
                    eventPoster.setImageResource(android.R.color.darker_gray);
                    Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update event", Toast.LENGTH_SHORT).show()
                );
    }
}