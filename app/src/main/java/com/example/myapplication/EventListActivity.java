package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;


/**
 * This is an activity that shows all events to entrants
 * It queries the firebase databse for load events
 * This enables it so that the users can:
 * - View event details by selecting an event
 * - Scan a QR code to open the corresponding event
 * - View lottery guidelines
 * - Navigate to their profile editing screen
 * The activity also filters events based on their registration start
 * and end times so that only currently active events are displayed.
 */
public class EventListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;
    private FirebaseFirestore db;

    private ImageButton lotteryinfoButton;
    private ImageButton scanQRButton;
    private ImageButton closeInfoButton;
    private ImageButton profileButton; // go to edit profile

    private com.google.firebase.firestore.ListenerRegistration statusListener;

    private com.google.firebase.firestore.ListenerRegistration eventChangesListener;

    private ActivityResultLauncher<ScanOptions> scannerLauncher;

    @SuppressLint("NotifyDataSetChanged")
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
//        profileButton = findViewById(R.id.btn_to_edit_profile);

        eventList = new ArrayList<>();
        adapter = new EventAdapter(eventList, getSupportFragmentManager());

        db = FirebaseFirestore.getInstance();
        loadEvents();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);



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

//        profileButton.setOnClickListener(v -> {
//            Intent intent = new Intent(EventListActivity.this, UserProfileActivity.class);
//            startActivity(intent);
//        });
//

        LinearLayout myProfile = findViewById(R.id.nav_profile);
        myProfile.setOnClickListener(v -> {
            Intent intent = new Intent(EventListActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });

        LinearLayout myNotifications = findViewById(R.id.nav_notifications);
        myNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(EventListActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });

        // ask for notification permission the second they open this screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        listenForStatusChanges();
        listenForEventChanges();
        listenForNewNotifications();



    }


    private void loadEvents() {
        Timestamp now = Timestamp.now();
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        db.collection("events")
                .whereLessThanOrEqualTo("regStart", now)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        event.setId(doc.getId());

                        boolean isPublic = !Boolean.TRUE.equals(event.getPrivateEvent());
                        boolean isCoOrg = event.getCoOrganizers() != null
                                && event.getCoOrganizers().contains(deviceId);
                        boolean isActive = event.getRegEnd() != null
                                && event.getRegEnd().compareTo(now) >= 0;

                        if (isActive && (isPublic || isCoOrg)) {
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


    private void listenForStatusChanges() {
        String deviceId = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ANDROID_ID);

        statusListener = db.collectionGroup("waitingList")
                .whereEqualTo("userId", deviceId)
                .addSnapshotListener((snapshots, e) -> {

                    if (e != null) {
                        Log.e("FirestoreListener", "Error", e);
                        return;
                    }

                    Log.d("FirestoreListener", "Status changed!");


                    loadEvents();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (statusListener != null) {
            statusListener.remove();
        }
        if (eventChangesListener != null) {
            eventChangesListener.remove();
        }
    }

    private void listenForNewNotifications() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String deviceId = new Profiles().getDeviceId(this);

        // setup channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("EVENT_ALERTS", "Event Alerts", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        db.collection("users").document(deviceId).collection("notifications")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        // only trigger on brand new messages, ignore old ones
                        if (dc.getType() == DocumentChange.Type.ADDED) {

                            // check if they actually want notifications
                            db.collection("users").document(deviceId).get().addOnSuccessListener(doc -> {
                                Boolean wantsNotis = doc.getBoolean("notificationsEnabled");

                                // default to true if null just in case
                                if (wantsNotis == null || wantsNotis) {
                                    String message = dc.getDocument().getString("message");
                                    showSystemNotification(message);
                                }
                            });
                        }
                    }
                });
    }

    private void showSystemNotification(String messageText) {
        // Needed for the newer andriod apparantly
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "EVENT_ALERTS")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("New Event Update!")
                .setContentText(messageText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void listenForEventChanges() {
        eventChangesListener = db.collection("events")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("EventListActivity", "Event listener error", e);
                        return;
                    }
                    loadEvents();
                });
    }
}

