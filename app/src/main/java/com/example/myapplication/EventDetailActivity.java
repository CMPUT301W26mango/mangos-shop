package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class EventDetailActivity extends AppCompatActivity {

    private static final String TAG = "EventDetailActivity";

    private String eventId;
    private String eventName;
    private boolean isPrivate = false;
    private boolean isCoOrg = false;

    private MapView mapView;
    private FirebaseFirestore db;

    private ImageView shareBtn;
    private ImageView btnInvite;
    private ImageView settingsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // osmdroid configuration must happen before setContentView
        Configuration.getInstance().load(
                getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE));

        setContentView(R.layout.activity_event_details);

        eventId = getIntent().getStringExtra("EVENT_ID");
        db = FirebaseFirestore.getInstance();

        // Back button
        ImageView backBtn = findViewById(R.id.btn_back);
        backBtn.setOnClickListener(v -> finish());

        shareBtn = findViewById(R.id.btn_share_qr);
        btnInvite = findViewById(R.id.btn_invite_users);
        settingsBtn = findViewById(R.id.btn_settings_cog);


        // Share/QR button
        shareBtn.setOnClickListener(v -> showQRCodePopup());


        settingsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailActivity.this, EventCreateActivity.class);
            intent.putExtra("MODE", "EDIT");
            intent.putExtra("EVENT_ID", eventId);
            startActivity(intent);
        });


        btnInvite.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailActivity.this, UserSearchActivity.class);
            intent.putExtra("EVENT_ID", eventId);
            intent.putExtra("EVENT_NAME", eventName != null ? eventName : "");
            intent.putExtra("IS_PRIVATE", isPrivate);
            intent.putExtra("IS_CO_ORG", isCoOrg);
            startActivity(intent);
        });

        // osmdroid map setup
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(4.0);
        mapView.getController().setCenter(new GeoPoint(0.0, 0.0));

        // "Event Details" button - WaitingListActivity
        Button btnEventDetails = findViewById(R.id.btn_event_details);
        btnEventDetails.setOnClickListener(v -> {
            db.collection("events").document(eventId).get()
                    .addOnSuccessListener(doc -> {
                        Boolean drawCompleted = doc.getBoolean("drawCompleted");
                        if (Boolean.TRUE.equals(drawCompleted)) {
                            // after draw - selected users screen
                            Intent intent = new Intent(EventDetailActivity.this, SelectedUsersActivity.class);
                            intent.putExtra("eventId", eventId);
                            intent.putExtra("eventName", eventName != null ? eventName : "");
                            startActivity(intent);
                        } else {
                            // before draw - waiting list screen
                            Intent intent = new Intent(EventDetailActivity.this, WaitingListActivity.class);
                            intent.putExtra("eventId", eventId);
                            intent.putExtra("eventName", eventName != null ? eventName : "");
                            startActivity(intent);
                        }
                    });
        });

        if (eventId != null) {
            loadEventData();
            loadEntrantLocations();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();

        String currentDeviceId = android.provider.Settings.Secure.getString(
                getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        EventStore eventStore = new EventStore();
        eventStore.getEventById(eventId, event -> {
            isPrivate = Boolean.TRUE.equals(event.getPrivateEvent());
            isCoOrg = event.getCoOrganizers() != null && event.getCoOrganizers().contains(currentDeviceId);
            boolean isOwner = currentDeviceId.equals(event.getDeviceId());

            runOnUiThread(() -> {
                settingsBtn.setVisibility(isOwner ? View.VISIBLE : View.GONE);
                shareBtn.setVisibility(!isPrivate ? View.VISIBLE : View.GONE);
                btnInvite.setVisibility((isOwner || (isCoOrg && isPrivate)) ? View.VISIBLE : View.GONE);
            });
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDetach();
    }

    /**
     * Loads event metadata (name, date, capacity, registration status, description)
     * from Firestore and populates the UI.
     */
    private void loadEventData() {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    eventName = doc.getString("title");

                    TextView tvName = findViewById(R.id.tv_event_name);
                    TextView tvDate = findViewById(R.id.tv_event_date_range);
                    TextView tvCapacity = findViewById(R.id.tv_capacity);
                    TextView tvRegStatus = findViewById(R.id.tv_registration_status);
                    TextView tvDescription = findViewById(R.id.tv_description);

                    tvName.setText(eventName != null ? eventName : "Untitled Event");

                    String dateEvent = doc.getString("dateEvent");
                    tvDate.setText(dateEvent != null ? dateEvent : "");

                    // Registration status
                    Timestamp regEnd = doc.getTimestamp("regEnd");
                    if (regEnd != null) {
                        boolean open = Timestamp.now().compareTo(regEnd) <= 0;
                        tvRegStatus.setText("Registration: " + (open ? "Open" : "Closed"));
                    } else {
                        tvRegStatus.setText("Registration: Unknown");
                    }

                    // Description
                    String description = doc.getString("description");
                    tvDescription.setText(description != null ? description : "No description available.");

                    // Capacity: enrolled / max
                    Long capacity = doc.getLong("capacity");
                    String capStr = capacity != null ? String.valueOf(capacity) : "?";
                    db.collection("events").document(eventId).collection("waitingList").get()
                            .addOnSuccessListener(wl ->
                                    tvCapacity.setText("Capacity: " + wl.size() + " / " + capStr));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load event data", e));
    }

    /**
     * Queries the waiting list for documents with latitude/longitude and
     * places a marker on the map for each one. Auto-zooms to fit all markers.
     */
    private void loadEntrantLocations() {
        db.collection("events").document(eventId).collection("waitingList").get()
                .addOnSuccessListener(snapshots -> {
                    List<GeoPoint> points = new ArrayList<>();
                    mapView.getOverlays().clear();

                    int locatedCount = 0;
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Double lat = doc.getDouble("latitude");
                        Double lng = doc.getDouble("longitude");
                        if (lat == null || lng == null) continue;

                        locatedCount++;
                        GeoPoint point = new GeoPoint(lat, lng);
                        points.add(point);

                        String userId = doc.getString("userId");
                        mapView.getOverlays().add(
                                createVisibleMarker(mapView, point, userId != null ? userId : "Entrant"));
                    }
                    Log.d(TAG, "loadEntrantLocations: " + locatedCount + " of " + snapshots.size() + " entrants have location data");

                    if (points.size() == 1) {
                        mapView.getController().setZoom(12.0);
                        mapView.getController().setCenter(points.get(0));
                    } else if (points.size() > 1) {
                        BoundingBox box = BoundingBox.fromGeoPointsSafe(points);
                        mapView.zoomToBoundingBox(box, true, 100);
                    }
                    mapView.invalidate();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load entrant locations", e));
    }

    private Marker createVisibleMarker(MapView map, GeoPoint point, String title) {
        Marker marker = new Marker(map);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);

        int size = (int) (30 * getResources().getDisplayMetrics().density);
        android.graphics.Bitmap bmp = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(bmp);
        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setColor(android.graphics.Color.RED);
        paint.setAntiAlias(true);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
        paint.setColor(android.graphics.Color.WHITE);
        paint.setStyle(android.graphics.Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2f, paint);

        marker.setIcon(new android.graphics.drawable.BitmapDrawable(getResources(), bmp));
        marker.setTitle(title);
        return marker;
    }

    private void showQRCodePopup() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Event QR Code");

        android.widget.ImageView qrView = new android.widget.ImageView(this);
        qrView.setPadding(40, 40, 40, 40);

        try {
            android.graphics.Bitmap bitmap = QrHelper.generateQrCode(eventId);
            qrView.setImageBitmap(bitmap);
        } catch (Exception e) {
            qrView.setImageResource(android.R.drawable.stat_notify_error);
        }

        builder.setView(qrView);
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}