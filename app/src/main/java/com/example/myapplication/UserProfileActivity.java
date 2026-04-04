package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfileActivity extends AppCompatActivity {

    private TextView tvProfileName;
    private TextView tvProfileRole;
    private FirebaseFirestore db;
    private String deviceId;
    private ImageView profileImageView;
    private Button btnUploadProfilePic;
    private androidx.activity.result.ActivityResultLauncher<android.content.Intent> photoPickerLauncher;
    private com.google.firebase.storage.FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // init views
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileRole = findViewById(R.id.tvProfileRole);
        profileImageView = findViewById(R.id.profileImageView);
        btnUploadProfilePic = findViewById(R.id.btnUploadProfilePic);
        storage = com.google.firebase.storage.FirebaseStorage.getInstance();

        // setup firebase and grab the device id
        db = FirebaseFirestore.getInstance();
        Profiles profiles = new Profiles();
        deviceId = profiles.getDeviceId(this);

        // populate the profile screen
        loadUserData();
        //maybe works now?
        loadProfilePicture();

        // menu click listeners
        findViewById(R.id.rowEditProfile).setOnClickListener(v -> {
            startActivity(new Intent(UserProfileActivity.this, EntrantAccount.class));
        });

        findViewById(R.id.rowNotifications).setOnClickListener(v -> {
            startActivity(new Intent(UserProfileActivity.this, NotificationSettingsActivity.class));
        });

        // Photo picker
        photoPickerLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        android.net.Uri imageUri = result.getData().getData();

                        // Show the image instantly using Glide (make it a circle!)
                        com.bumptech.glide.Glide.with(this)
                                .load(imageUri)
                                .circleCrop()
                                .into(profileImageView);

                        // Upload it to Firebase
                        uploadProfilePicture(imageUri);
                    }
                }
        );

        // Gallarey for the photo
        btnUploadProfilePic.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_PICK);
            intent.setType("image/*");
            photoPickerLauncher.launch(intent);
        });
    }

    private void loadUserData() {
        db.collection("users").document(deviceId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                // update name if it exists, otherwise default to Guest
                if (doc.contains("name")) {
                    tvProfileName.setText(doc.getString("name"));
                } else {
                    tvProfileName.setText("Guest");
                }

                // update role
                if (doc.contains("role")) {
                    tvProfileRole.setText(doc.getString("role"));
                }
            }
        }).addOnFailureListener(e -> Log.e("UserProfile", "Failed to load user data", e));
    }

    /**
     * Uploads the selected image to Firebase Storage and saves the generated URL
     * to the user's Firestore document. Overwrites any existing profile picture.
     *
     * Written with the assistance of Gemini
     * Prompt used : "how can I upload images to firebase to make it appear on app?"
     *
     * @param imageUri The local URI of the image selected from the device gallery.
     */
    private void uploadProfilePicture(android.net.Uri imageUri) {
        if (imageUri == null) return;

        // Save it under profile_pictures / deviceId.jpg
        com.google.firebase.storage.StorageReference storageRef = storage.getReference()
                .child("profile_pictures/" + deviceId + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the public download URL
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();

                        // Save the URL to the user's Firestore profile
                        db.collection("users").document(deviceId)
                                .update("profileImageUrl", downloadUrl)
                                .addOnSuccessListener(aVoid ->
                                        android.widget.Toast.makeText(this, "Profile Picture Updated!", android.widget.Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        android.util.Log.e("ProfilePic", "Failed to link URL to user", e));
                    });
                })
                .addOnFailureListener(e -> {
                    android.widget.Toast.makeText(this, "Upload Failed", android.widget.Toast.LENGTH_SHORT).show();
                    android.util.Log.e("ProfilePic", "Firebase Storage Upload Failed", e);
                });
    }

    /**
     * Retrieves the user's profile image URL from Firestore and displays it using Glide.
     * Should be called in onCreate() or alongside loading other user data.
     */
    private void loadProfilePicture() {
        db.collection("users").document(deviceId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.contains("profileImageUrl")) {
                String imageUrl = documentSnapshot.getString("profileImageUrl");

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    com.bumptech.glide.Glide.with(this)
                            .load(imageUrl)
                            .placeholder(android.R.color.darker_gray)
                            .circleCrop()
                            .into(profileImageView);
                }
            }
        });
    }
}