package com.example.myapplication;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * EventListFragment - Displays the list of events and provides QR scanning
 * and lottery info functionality for entrants.
 * Combines event list (teammate) with QR scanner and lottery info (US 01.06.01, US 01.05.05)
 * Used Claude AI to merge my EventList code with Aditya's EventList code
 * Prompt: "Help me integrate my info button and scan QR button onto this EventList page"
 * Date: Tuesday, March 10, 2026
 */
public class EventListFragment extends Fragment {


    RecyclerView recyclerView;
    EventAdapter adapter;
    List<Event> eventList;
    FirebaseFirestore db;


    ImageButton lotteryinfoButton;
    ImageButton scanQRButton;
    ImageButton closeInfoButton;

    // ZXing QR scanner launcher
    private ActivityResultLauncher<ScanOptions> scannerLauncher;

    public EventListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register the QR scanner launcher
        scannerLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                String scannedValue = result.getContents();

                    Bundle bundle = new Bundle();
                    bundle.putString("eventId", scannedValue);

                    EventDetailsFragment eventDetailsFragment = new EventDetailsFragment();
                    eventDetailsFragment.setArguments(bundle);
                    eventDetailsFragment.show(getParentFragmentManager(), "eventDetails");

            } else {
                Toast.makeText(getContext(), "Scan cancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.whole_event_list, container, false);


        recyclerView = view.findViewById(R.id.recyclerViewEvents);
        eventList = new ArrayList<>();
        adapter = new EventAdapter(eventList, getParentFragmentManager());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();
        loadEvents();

        // Setup your buttons
        lotteryinfoButton = view.findViewById(R.id.lotteryinfoButton);
        scanQRButton = view.findViewById(R.id.scanQRButton);

        // Lottery info button
        lotteryinfoButton.setOnClickListener(v -> {
            Dialog dialog = new Dialog(requireContext());
            dialog.setContentView(R.layout.lottery_guidelines_dialog);
            closeInfoButton = dialog.findViewById(R.id.closeButton);
            closeInfoButton.setOnClickListener(closeView -> dialog.dismiss());
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.show();
        });

        // QR scan button
        scanQRButton.setOnClickListener(v -> launchQRScanner());

        return view;
    }

    /**
     * Loads events from Firestore where registration is currently open.
     * Only shows events where regStart <= now <= regEnd
     */
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
                        // Only include events where registration is still open
                        if (event.getRegEnd() != null
                                && event.getRegEnd().compareTo(now) >= 0) {
                            eventList.add(event);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("EventListFragment", "Error loading events", e);
                });
    }

    /**
     * Configures and launches the ZXing QR code scanner.
     * Called when the scan QR button is clicked.
     */
    private void launchQRScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("");
        options.setBeepEnabled(false);
        options.setOrientationLocked(false);
        options.setBarcodeImageEnabled(false);
        scannerLauncher.launch(options);
    }
}
