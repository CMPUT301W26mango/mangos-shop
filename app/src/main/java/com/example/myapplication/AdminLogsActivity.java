package com.example.myapplication;
/**
 * Activity that displays notification logs sent by organizers,
 * allowing the administrator to view all notification activity.
 *
 * Role in application:
 * - Admin control screen for viewing notification logs.
 *
 * Outstanding issues:
 * - None
 */

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminLogsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<AdminLogItem> list = new ArrayList<>();
    private AdminLogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_logs);

        recyclerView = findViewById(R.id.recyclerViewLogs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminLogAdapter(list);
        recyclerView.setAdapter(adapter);
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

        loadLogs();

        LinearLayout navEvents = findViewById(R.id.nav_admin_events);
        LinearLayout navProfiles = findViewById(R.id.nav_admin_profiles);
        LinearLayout navImages = findViewById(R.id.nav_admin_images);

        navEvents.setOnClickListener(v ->
                startActivity(new Intent(this, AdminBrowseEventsActivity.class)));

        navProfiles.setOnClickListener(v ->
                startActivity(new Intent(this, AdminBrowseProfilesActivity.class)));

        navImages.setOnClickListener(v ->
                startActivity(new Intent(this, AdminBrowseImagesActivity.class)));
    }

    private void loadLogs() {
        FirebaseFirestore.getInstance()
                .collection("notifications")
                .get()
                .addOnSuccessListener(query -> {

                    list.clear();

                    for (QueryDocumentSnapshot doc : query) {
                        try {
                            final String message = doc.getString("message") != null
                                    ? doc.getString("message")
                                    : "No message";

                            final String eventName = doc.getString("eventName") != null
                                    ? doc.getString("eventName")
                                    : "Unknown Event";

                            String time = doc.getTimestamp("timestamp") != null
                                    ? doc.getTimestamp("timestamp").toDate().toString()
                                    : "No time";

                            list.add(new AdminLogItem(message, eventName, time));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}