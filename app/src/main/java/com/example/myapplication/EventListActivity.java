package com.example.myapplication;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;

public class EventListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;
    private FirebaseFirestore db;

    private ImageButton lotteryinfoButton;
    private ImageButton scanQRButton;
    private ImageButton closeInfoButton;
    private ImageButton profileButton; // go to edit profile

    private ActivityResultLauncher<ScanOptions> scannerLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.whole_event_list);

        scannerLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                String scannedValue = result.getContents();

                Bundle bundle = new Bundle();
                bundle.putString("eventId", scannedValue);

                EventDetailsFragment eventDetailsFragment = new EventDetailsFragment();
                eventDetailsFragment.setArguments(bundle);
                eventDetailsFragment.show(getSupportFragmentManager(), "eventDetails");
            } else {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView = findViewById(R.id.recyclerViewEvents);
        lotteryinfoButton = findViewById(R.id.lotteryinfoButton);
        scanQRButton = findViewById(R.id.scanQRButton);
        profileButton = findViewById(R.id.btn_to_edit_profile);

        eventList = new ArrayList<>();
        adapter = new EventAdapter(eventList, getSupportFragmentManager());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadEvents();

        lotteryinfoButton.setOnClickListener(v -> {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.lottery_guidelines_dialog);

            closeInfoButton = dialog.findViewById(R.id.closeButton);
            closeInfoButton.setOnClickListener(closeView -> dialog.dismiss());

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
            dialog.show();
        });

        scanQRButton.setOnClickListener(v -> launchQRScanner());

        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventListActivity.this, EntrantAccount.class);
            startActivity(intent);
        });
    }

    private void loadEvents() {
        Timestamp now = Timestamp.now();
        db.collection("events")
                .whereLessThanOrEqualTo("regStart", now)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        event.setId(doc.getId());

                        if (event.getRegEnd() != null && event.getRegEnd().compareTo(now) >= 0) {
                            eventList.add(event);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Log.e("EventListActivity", "Error loading events", e));
    }

    private void launchQRScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Press back to cancel");
        options.setBeepEnabled(false);
        options.setOrientationLocked(false);
        options.setBarcodeImageEnabled(false);
        scannerLauncher.launch(options);
    }
}
