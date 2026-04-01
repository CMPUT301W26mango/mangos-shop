package com.example.myapplication;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class UserSearchActivity extends AppCompatActivity {

    private UserSearchAdapter adapter;
    private Profiles profilesHelper;
    private String eventId;

    private String eventName = "Event";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_search);

        // Get the event ID passed from EventDetailActivity
        eventId = getIntent().getStringExtra("EVENT_ID");

        // Get the event name passed from EventDetailActivity
        if (getIntent().hasExtra("EVENT_NAME")) {
            eventName = getIntent().getStringExtra("EVENT_NAME");
        }

        EditText searchBar = findViewById(R.id.et_search_bar);
        RecyclerView recyclerView = findViewById(R.id.rv_search_results);

        profilesHelper = new Profiles();
        boolean isPrivate = getIntent().getBooleanExtra("IS_PRIVATE", false);
        boolean isCoOrg = getIntent().getBooleanExtra("IS_CO_ORG", false);
        adapter = new UserSearchAdapter(this, new ArrayList<>(), eventId,eventName, isPrivate, isCoOrg);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fetchEventName();
        // Listen for typing in the search bar
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();

                // Only search if they typed at least 2 characters
                if (query.length() >= 2) {
                    profilesHelper.searchUsers(query, users -> {
                        adapter.updateList(users);
                    });
                } else {
                    adapter.updateList(new ArrayList<>()); // Clear list if empty
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }


    /**
     * Fetches the event title from Firestore to ensure notifications have the correct name.
     */
    private void fetchEventName() {
        if (eventId == null) return;

        FirebaseFirestore.getInstance().collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fetchedName = documentSnapshot.getString("title");
                        if (fetchedName != null) {
                            this.eventName = fetchedName;
                            adapter.setEventName(fetchedName);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("UserSearchActivity", "Error fetching event name", e));
    }
}