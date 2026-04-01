package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

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

        loadLogs();

        LinearLayout buttonBrowseEvents = findViewById(R.id.buttonBrowseEvents);
        LinearLayout buttonBrowseProfiles = findViewById(R.id.buttonBrowseProfiles);
        LinearLayout buttonBrowseImages = findViewById(R.id.buttonBrowseImages);

        if (buttonBrowseEvents != null) {
            buttonBrowseEvents.setOnClickListener(v ->
                    startActivity(new Intent(this, AdminBrowseEventsActivity.class)));
        }

        if (buttonBrowseProfiles != null) {
            buttonBrowseProfiles.setOnClickListener(v ->
                    startActivity(new Intent(this, AdminBrowseProfilesActivity.class)));
        }

        if (buttonBrowseImages != null) {
            buttonBrowseImages.setOnClickListener(v ->
                    startActivity(new Intent(this, AdminBrowseImagesActivity.class)));
        }
    }

    private void loadLogs() {
        FirebaseFirestore.getInstance()
                .collection("notifications")
                .get()
                .addOnSuccessListener(query -> {

                    list.clear();

                    for (QueryDocumentSnapshot doc : query) {

                        String message = doc.getString("message");
                        String sender = doc.getString("senderId");

                        list.add(new AdminLogItem(
                                message != null ? message : "No message",
                                sender != null ? sender : "Unknown",
                                "Time not set"
                        ));
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load logs", Toast.LENGTH_SHORT).show()
                );
    }
}