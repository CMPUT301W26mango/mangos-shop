package com.example.myapplication;
/**
 * Activity that allows an administrator to browse all event images,
 * view event details, and remove event images or events.
 *
 * Role in application:
 * - Admin control screen for managing event images.
 *
 * Outstanding issues:
 * - None
 */

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminBrowseImagesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<AdminImageItem> list = new ArrayList<>();
    private AdminImageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_browse_images);
        TextView textViewAdminTitle = findViewById(R.id.textViewAdminTitle);

        Profiles profiles = new Profiles();
        String userId = profiles.getDeviceId(this);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        if (name == null || name.isEmpty()) {
                            name = "Admin";
                        }
                        textViewAdminTitle.setText(name);
                    }
                });

        recyclerView = findViewById(R.id.recyclerViewImages);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        int spacing = (int) (16 * getResources().getDisplayMetrics().density);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacing, true));

        adapter = new AdminImageAdapter(list, item -> showPopup(item));
        recyclerView.setAdapter(adapter);

        loadImages();

        LinearLayout navEvents = findViewById(R.id.nav_admin_events);
        LinearLayout navProfiles = findViewById(R.id.nav_admin_profiles);
        LinearLayout navLogs = findViewById(R.id.nav_admin_logs);

        navEvents.setOnClickListener(v ->
                startActivity(new Intent(this, AdminBrowseEventsActivity.class)));

        navProfiles.setOnClickListener(v ->
                startActivity(new Intent(this, AdminBrowseProfilesActivity.class)));

        navLogs.setOnClickListener(v ->
                startActivity(new Intent(this, AdminLogsActivity.class)));
    }

    private void loadImages() {
        FirebaseFirestore.getInstance().collection("events")
                .get()
                .addOnSuccessListener(query -> {
                    list.clear();

                    for (QueryDocumentSnapshot doc : query) {
                        String id = doc.getId();
                        String url = doc.getString("posterURL");

                        if (url != null && !url.isEmpty()) {
                            list.add(new AdminImageItem(id, url));
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load images", Toast.LENGTH_SHORT).show()
                );
    }

    private void showPopup(AdminImageItem item) {

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_admin_event_detail, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        TextView tvTitle = dialogView.findViewById(R.id.tv_event_title);
        TextView tvLocation = dialogView.findViewById(R.id.tv_event_location);
        TextView tvType = dialogView.findViewById(R.id.tv_event_type);
        TextView tvCount = dialogView.findViewById(R.id.tv_spots_available);

        Button btnDeleteEvent = dialogView.findViewById(R.id.btn_delete_event);
        Button btnDeleteImage = dialogView.findViewById(R.id.btn_delete_image);
        ImageButton btnClose = dialogView.findViewById(R.id.btn_close);

        FirebaseFirestore.getInstance()
                .collection("events")
                .document(item.getEventId())
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists()) {

                        String title = doc.getString("title");
                        String location = doc.getString("location");
                        Long count = doc.getLong("capacity");

                        if (title == null) title = "Event";
                        if (location == null) location = "No location";

                        tvTitle.setText(title);
                        tvLocation.setText(location);
                        tvType.setText("CULTURAL");
                        tvCount.setText(count != null ? String.valueOf(count) : "0");
                    }
                });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnDeleteEvent.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("events")
                    .document(item.getEventId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadImages();
                    });
        });

        btnDeleteImage.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("events")
                    .document(item.getEventId())
                    .update("posterURL", "")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadImages();
                    });
        });

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }
}