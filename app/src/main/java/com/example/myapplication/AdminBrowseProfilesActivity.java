/**
 * Activity that allows an administrator to browse user profiles, search by
 * name or email, and open profile details.
 *
 * Role in application:
 * - Admin control screen for viewing user records.
 *
 * Outstanding issues:
 * - None for now
 */

package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.myapplication.Profiles;

import java.util.ArrayList;
import java.util.List;

public class AdminBrowseProfilesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewAdminProfiles;
    private SearchView searchViewProfiles;
    private TextView textViewEmptyProfiles;

    private FirebaseFirestore db;
    private AdminProfileAdapter adapter;

    private final List<AdminProfileItem> allProfiles = new ArrayList<>();
    private final List<AdminProfileItem> filteredProfiles = new ArrayList<>();
    private String currentUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_browse_profiles);
        Button switchButton = findViewById(R.id.buttonSwitchRole);

        switchButton.setOnClickListener(v -> showSwitchDialog());

        recyclerViewAdminProfiles = findViewById(R.id.recyclerViewAdminProfiles);
        searchViewProfiles = findViewById(R.id.searchViewProfiles);
        textViewEmptyProfiles = findViewById(R.id.textViewEmptyProfiles);

        db = FirebaseFirestore.getInstance();
        Profiles profiles = new Profiles();
        currentUserId = profiles.getDeviceId(this);

        recyclerViewAdminProfiles.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminProfileAdapter(filteredProfiles, profileItem -> {

            View dialogView = getLayoutInflater().inflate(R.layout.dialog_admin_profile_detail, null);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create();

            TextView tvName = dialogView.findViewById(R.id.tv_name);
            TextView tvEmail = dialogView.findViewById(R.id.tv_email);
            TextView tvRole = dialogView.findViewById(R.id.tv_role);
            Button btnDelete = dialogView.findViewById(R.id.btn_delete_profile);
            ImageButton btnClose = dialogView.findViewById(R.id.btn_close);

            tvName.setText(profileItem.getName());
            tvEmail.setText(profileItem.getEmail());
            tvRole.setText(profileItem.getRole());

            btnClose.setOnClickListener(v -> dialog.dismiss());

            btnDelete.setOnClickListener(v -> {

                String deletedUserId = profileItem.getUserId();

                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(deletedUserId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {

                            Toast.makeText(this, "Profile deleted", Toast.LENGTH_SHORT).show();

                            if (currentUserId != null && currentUserId.equals(deletedUserId)) {

                                Intent intent = new Intent(AdminBrowseProfilesActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);

                            } else {
                                dialog.dismiss();
                                loadProfiles();
                            }

                        });
            });

            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.show();
        });

        LinearLayout buttonBrowseEvents = findViewById(R.id.buttonBrowseEvents);
        LinearLayout buttonBrowseImages = findViewById(R.id.buttonBrowseImages);
        LinearLayout buttonLogs = findViewById(R.id.buttonLogs);

        buttonBrowseEvents.setOnClickListener(v ->
                startActivity(new Intent(this, AdminBrowseEventsActivity.class)));

        buttonBrowseImages.setOnClickListener(v ->
                startActivity(new Intent(this, AdminBrowseImagesActivity.class)));

        buttonLogs.setOnClickListener(v ->
                startActivity(new Intent(this, AdminLogsActivity.class)));

        recyclerViewAdminProfiles.setAdapter(adapter);

        loadProfiles();
        setupSearch();
    }

    private void loadProfiles() {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allProfiles.clear();
                    filteredProfiles.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String userId = document.getId();
                        String name = document.getString("name");
                        String email = document.getString("email");
                        String role = document.getString("role");

                        if (name == null || name.isEmpty()) {
                            name = "Unnamed User";
                        }

                        AdminProfileItem profileItem = new AdminProfileItem(userId, name, email, role);
                        allProfiles.add(profileItem);
                    }

                    filteredProfiles.addAll(allProfiles);
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profiles", Toast.LENGTH_SHORT).show()
                );
    }

    private void setupSearch() {
        searchViewProfiles.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterProfiles(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterProfiles(newText);
                return true;
            }
        });
    }

    private void filterProfiles(String query) {
        filteredProfiles.clear();

        if (query == null || query.trim().isEmpty()) {
            filteredProfiles.addAll(allProfiles);
        } else {
            String lowerQuery = query.toLowerCase().trim();

            for (AdminProfileItem profile : allProfiles) {
                String name = profile.getName() == null ? "" : profile.getName().toLowerCase();
                String email = profile.getEmail() == null ? "" : profile.getEmail().toLowerCase();

                if (name.contains(lowerQuery) || email.contains(lowerQuery)) {
                    filteredProfiles.add(profile);
                }
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredProfiles.isEmpty()) {
            textViewEmptyProfiles.setVisibility(View.VISIBLE);
            recyclerViewAdminProfiles.setVisibility(View.GONE);
        } else {
            textViewEmptyProfiles.setVisibility(View.GONE);
            recyclerViewAdminProfiles.setVisibility(View.VISIBLE);
        }
    }

    private void showSwitchDialog() {
        String[] roles = {"Entrant", "Organizer"};

        new AlertDialog.Builder(this)
                .setTitle("Switch Role")
                .setItems(roles, (dialog, which) -> {

                    SharedPreferences prefs = getSharedPreferences("ROLE_PREF", MODE_PRIVATE);

                    if (which == 0) {
                        prefs.edit().putString("currentRole", "Entrant").apply();

                        Intent intent = new Intent(this, EventListActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    } else {
                        prefs.edit().putString("currentRole", "Organizer").apply();

                        Intent intent = new Intent(this, OrganizerDashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }

                })
                .show();
    }
}