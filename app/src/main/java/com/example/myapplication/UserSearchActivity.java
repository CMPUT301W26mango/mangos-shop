package com.example.myapplication;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UserSearchActivity extends AppCompatActivity {

    private UserSearchAdapter adapter;
    private Profiles profilesHelper;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_search);

        // Get the event ID passed from EventDetailActivity
        eventId = getIntent().getStringExtra("EVENT_ID");

        EditText searchBar = findViewById(R.id.et_search_bar);
        RecyclerView recyclerView = findViewById(R.id.rv_search_results);

        profilesHelper = new Profiles();
        boolean isPrivate = getIntent().getBooleanExtra("IS_PRIVATE", false);
        boolean isCoOrg = getIntent().getBooleanExtra("IS_CO_ORG", false);
        adapter = new UserSearchAdapter(this, new ArrayList<>(), eventId, isPrivate, isCoOrg);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

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
}