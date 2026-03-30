package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;


public class EventDetailActivity extends AppCompatActivity {

    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        // Get the ID passed from the Dashboard
        eventId = getIntent().getStringExtra("EVENT_ID");
        ImageView shareBtn = findViewById(R.id.btn_share_qr);
        ImageView settingsBtn = findViewById(R.id.btn_settings_cog);

        EventStore eventStore = new EventStore();

        eventStore.getEventById(eventId, event -> {
            if (event.getPrivateEvent())  {
                shareBtn.setVisibility(View.GONE);
            } else {
                shareBtn.setVisibility(View.VISIBLE);
            }
        });

        // Settings Cog logic
        settingsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventCreateActivity.class);
            intent.putExtra("MODE", "EDIT");
            intent.putExtra("EVENT_ID", eventId);
            startActivity(intent);
        });

        shareBtn.setOnClickListener(v -> {
            showQRCodePopup();
        });
    }

    private void showQRCodePopup() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Event QR Code");

        android.widget.ImageView qrView = new android.widget.ImageView(this);
        qrView.setPadding(40, 40, 40, 40);

        try {
            // using QR HELPER
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