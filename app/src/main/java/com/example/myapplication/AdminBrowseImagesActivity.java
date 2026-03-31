package com.example.myapplication;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class AdminBrowseImagesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageAdapter adapter;
    private List<ImageItem> imageList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_browse_images);

        recyclerView = findViewById(R.id.recyclerViewImages);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        imageList = new ArrayList<>();
        adapter = new ImageAdapter(imageList, this::showDeleteDialog);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadImages();
    }

    private void loadImages() {
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    imageList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String url = doc.getString("posterURL");
                        String eventId = doc.getId();

                        if (url != null && !url.isEmpty()) {
                            imageList.add(new ImageItem(url, eventId));
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private void showDeleteDialog(ImageItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Image")
                .setMessage("Delete this image?")
                .setPositiveButton("Yes", (dialog, which) -> deleteImage(item))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteImage(ImageItem item) {
        String url = item.getUrl();

        try {
            StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(url);

            ref.delete().addOnSuccessListener(aVoid -> {
                db.collection("events")
                        .document(item.getEventId())
                        .update("posterURL", "");

                Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
                loadImages();
            });

        } catch (Exception e) {
            Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show();
        }
    }
}