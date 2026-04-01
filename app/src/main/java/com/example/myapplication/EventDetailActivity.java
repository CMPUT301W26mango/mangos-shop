package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;


public class EventDetailActivity extends AppCompatActivity {

    private String eventId;
    private boolean isPrivate = false;
    private boolean isCoOrg = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        eventId = getIntent().getStringExtra("EVENT_ID");

        // set up click listeners
        ImageView shareBtn = findViewById(R.id.btn_share_qr);
        ImageView settingsBtn = findViewById(R.id.btn_settings_cog);
        ImageView btnInvite = findViewById(R.id.btn_invite_users);

        settingsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventCreateActivity.class);
            intent.putExtra("MODE", "EDIT");
            intent.putExtra("EVENT_ID", eventId);
            startActivity(intent);
        });

        shareBtn.setOnClickListener(v -> showQRCodePopup());

        btnInvite.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserSearchActivity.class);
            intent.putExtra("EVENT_ID", eventId);
            intent.putExtra("IS_PRIVATE", isPrivate);
            intent.putExtra("IS_CO_ORG", isCoOrg);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        ImageView shareBtn = findViewById(R.id.btn_share_qr);
        ImageView btnInvite = findViewById(R.id.btn_invite_users);
        ImageView settingsBtn = findViewById(R.id.btn_settings_cog);

        String currentDeviceId = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        EventStore eventStore = new EventStore();
        eventStore.getEventById(eventId, event -> {
            isPrivate = Boolean.TRUE.equals(event.getPrivateEvent());
            isCoOrg = event.getCoOrganizers() != null && event.getCoOrganizers().contains(currentDeviceId);
            boolean isOwner = currentDeviceId.equals(event.getDeviceId());

            runOnUiThread(() -> {
                // settings cog — owner only not co org
                settingsBtn.setVisibility(isOwner ? View.VISIBLE : View.GONE);

                // QR share
                shareBtn.setVisibility(!isPrivate ? View.VISIBLE : View.GONE);

                // invite/search
                btnInvite.setVisibility((isOwner || (isCoOrg && isPrivate)) ? View.VISIBLE : View.GONE);
            });
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