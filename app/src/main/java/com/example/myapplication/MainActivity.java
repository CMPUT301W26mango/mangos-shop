package com.example.myapplication;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

public class MainActivity extends AppCompatActivity {


    /*ImageButton scanQRButton;*/
   // private ActivityResultLauncher<ScanOptions> scannerLauncher;
    Button eventListBtn;
    Button eventDetailsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        eventListBtn = findViewById(R.id.eventlist_btn);
        eventListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                eventListBtn.setVisibility(View.GONE);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main, new EventListFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });


       eventDetailsBtn = findViewById(R.id.eventdetails_btn);
        eventDetailsBtn.setOnClickListener(v -> {
          eventDetailsBtn.setVisibility(View.GONE);
            Bundle bundle = new Bundle();
            bundle.putString("eventId", "12345");
            EventDetailsFragment fragment = new EventDetailsFragment();
            fragment.setArguments(bundle);
            fragment.show(getSupportFragmentManager(), "eventDetails");
        });

    }
}