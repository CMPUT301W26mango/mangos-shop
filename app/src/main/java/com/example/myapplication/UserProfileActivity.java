package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Activity responsible for displaying and managing the user's main profile.
 * Handles the retrieval of user data (name, role, profile picture) from Firestore,
 * and manages the custom cropping and uploading of new profile pictures via Firebase Storage.
 */
public class UserProfileActivity extends BaseActivity {

    private TextView tvProfileName;
    private TextView tvProfileRole;
    private FirebaseFirestore db;
    private String deviceId;
    private ImageView profileImageView;
    private Button btnUploadProfilePic;
    private androidx.activity.result.ActivityResultLauncher<String> galleryLauncher;
    private androidx.activity.result.ActivityResultLauncher<Intent> customCropLauncher;
    private com.google.firebase.storage.FirebaseStorage storage;
    private Button btnRemoveProfilePic;

    /**
     * Initializes the activity, binds UI components, and configures image picker launchers.
     * Establishes connections to Firestore and Firebase Storage, then retrieves and populates
     * the user's current profile data in a single database call.
     *
     * Written with the assistance of Gemini
     * Prompt used : "how can I upload images to firebase to make it appear on app?"
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     * being shut down, this contains the most recent data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Button backToAdmin = findViewById(R.id.buttonBackToAdmin);

        backToAdmin.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("ROLE_PREF", MODE_PRIVATE);
            prefs.edit().remove("currentRole").apply();

            Intent intent = new Intent(this, AdminBrowseEventsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // init views
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileRole = findViewById(R.id.tvProfileRole);
        profileImageView = findViewById(R.id.profileImageView);
        btnUploadProfilePic = findViewById(R.id.btnUploadProfilePic);
        btnRemoveProfilePic = findViewById(R.id.btnRemoveProfilePic);
        btnRemoveProfilePic.setOnClickListener(v -> removeProfilePicture());
        storage = com.google.firebase.storage.FirebaseStorage.getInstance();

        // setup firebase and grab the device id
        db = FirebaseFirestore.getInstance();
        Profiles profiles = new Profiles();
        deviceId = profiles.getDeviceId(this);

        // populate the profile screen
        loadUserData();

        // menu click listeners
        findViewById(R.id.rowEditProfile).setOnClickListener(v -> {
            startActivity(new Intent(UserProfileActivity.this, EntrantAccount.class));
        });

        findViewById(R.id.rowNotifications).setOnClickListener(v -> {
            startActivity(new Intent(UserProfileActivity.this, NotificationSettingsActivity.class));
        });

        // Photo picker
        customCropLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        android.net.Uri croppedUri = result.getData().getParcelableExtra("croppedUri");

                        // circle it
                        com.bumptech.glide.Glide.with(this)
                                .load(croppedUri)
                                .circleCrop()
                                .into(profileImageView);

                        // Upload it to Firebase
                        uploadProfilePicture(croppedUri);
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        Intent intent = new Intent(this, CustomCropActivity.class);
                        intent.putExtra("imageUri", uri);
                        customCropLauncher.launch(intent);
                    }
                }
        );

        btnUploadProfilePic.setOnClickListener(v -> {
            galleryLauncher.launch("image/*");
        });
    }

    /**
     * Retrieves the user's data from Firestore, including name, role, and profile image URL.
     * Populates the UI fields and loads the image via Glide if available, utilizing a single
     * document read to optimize database performance.
     */
    private void loadUserData() {
        db.collection("users").document(deviceId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                // update name if it exists, otherwise default to Guest
                if (doc.contains("name")) {
                    tvProfileName.setText(doc.getString("name"));
                } else {
                    tvProfileName.setText("Guest");
                }

                // If Firebase says they are an admin, un-hide the escape button
                if (Boolean.TRUE.equals(doc.getBoolean("isAdmin")) || "Admin".equalsIgnoreCase(doc.getString("role"))) {
                    findViewById(R.id.buttonBackToAdmin).setVisibility(android.view.View.VISIBLE);
                }

                // update role
                if (doc.contains("role")) {
                    String role = doc.getString("role");
                    tvProfileRole.setText(role);

                    // i think this might fix issue
                    android.content.SharedPreferences prefs = getSharedPreferences("ROLE_PREF", MODE_PRIVATE);
                    String activeRole = prefs.getString("currentRole", role);

                    // Pass the role dynamically so it shows the right bar
                    setupBottomNavigation(activeRole);
                }
                // Update profile picture
                if (doc.contains("profileImageUrl")) {
                    String imageUrl = doc.getString("profileImageUrl");
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        profileImageView.setPadding(0, 0, 0, 0);
                        com.bumptech.glide.Glide.with(this)
                                .load(imageUrl)
                                .placeholder(android.R.color.darker_gray)
                                .circleCrop()
                                .into(profileImageView);
                    }
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
     * Removes the user's profile picture by deleting the image file from Firebase Storage
     * and removing the URL reference from my Firestore document. Reverts UI to default.
     *
     * Written with the assistance of Gemini
     * Prompt used : "how can I upload images to firebase to make it appear on app?"
     */
    private void removeProfilePicture() {
        // Remove the URL from Firestore using FieldValue.delete()
        db.collection("users").document(deviceId)
                .update("profileImageUrl", com.google.firebase.firestore.FieldValue.delete())
                .addOnSuccessListener(aVoid -> {
                    // Clear Glide and revert the ImageView to the placeholder
                    com.bumptech.glide.Glide.with(this).clear(profileImageView);
                    profileImageView.setPadding(24, 24, 24, 24);
                    profileImageView.setImageResource(android.R.drawable.ic_menu_myplaces);

                    android.widget.Toast.makeText(this, "Profile picture removed", android.widget.Toast.LENGTH_SHORT).show();

                    // Delete the actual file from Firebase Storage to save space
                    storage.getReference().child("profile_pictures/" + deviceId + ".jpg").delete()
                            .addOnFailureListener(e -> Log.e("ProfilePic", "Failed to delete storage file", e));
                })
                .addOnFailureListener(e -> {
                    android.widget.Toast.makeText(this, "Failed to remove picture", android.widget.Toast.LENGTH_SHORT).show();
                    Log.e("ProfilePic", "Failed to update Firestore", e);
                });
    }
}