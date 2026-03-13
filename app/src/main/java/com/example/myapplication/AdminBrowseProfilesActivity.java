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
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_browse_profiles);

        recyclerViewAdminProfiles = findViewById(R.id.recyclerViewAdminProfiles);
        searchViewProfiles = findViewById(R.id.searchViewProfiles);
        textViewEmptyProfiles = findViewById(R.id.textViewEmptyProfiles);

        db = FirebaseFirestore.getInstance();

        recyclerViewAdminProfiles.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminProfileAdapter(filteredProfiles, profileItem -> {
            Intent intent = new Intent(this, AdminProfileDetailActivity.class);
            intent.putExtra("userId", profileItem.getUserId());
            startActivity(intent);
        });

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
}