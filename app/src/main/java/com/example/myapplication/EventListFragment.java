package com.example.myapplication;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

/**
 * EventListFragment - Displays the list of events and provides QR scanning
 * and lottery info functionality for entrants.
 */
public class EventListFragment extends Fragment {

    ImageButton lotteryinfoButton;
    ImageButton scanQRButton;
    ImageButton closeInfoButton;

    // ZXing QR scanner launcher - handles camera result callback
    private ActivityResultLauncher<ScanOptions> scannerLauncher;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public EventListFragment() {
        // Required empty public constructor
    }

    public static EventListFragment newInstance(String param1, String param2) {
        EventListFragment fragment = new EventListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Register the QR scanner launcher - must be done in onCreate
        scannerLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                String scannedValue = result.getContents();
                try {
                    Integer.parseInt(scannedValue); // check if its a valid number
                    Bundle bundle = new Bundle();
                    bundle.putString("eventId", scannedValue);

                    EventDetailsFragment eventDetailsFragment = new EventDetailsFragment();
                    eventDetailsFragment.setArguments(bundle);
                    eventDetailsFragment.show(getParentFragmentManager(), "eventDetails");

                } catch (NumberFormatException e) {
                    // invalid QR code - not a number
                    Toast.makeText(getContext(), "Invalid or unrecognized QR code", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Scan cancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        lotteryinfoButton = view.findViewById(R.id.lotteryinfoButton);
        scanQRButton = view.findViewById(R.id.scanQRButton);

        // Lottery info button - shows guidelines dialog (US 01.05.05)
        lotteryinfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(requireContext());
                dialog.setContentView(R.layout.lottery_guidelines_dialog);

                closeInfoButton = dialog.findViewById(R.id.closeButton);
                closeInfoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.show();
            }
        });

        // QR scan button - launches ZXing camera scanner (US 01.06.01)
        scanQRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchQRScanner();
            }
        });

        return view;
    }

    /**
     * Configures and launches the ZXing QR code scanner.
     * Called when the scan QR button is clicked.
     */
    private void launchQRScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan an event QR code");
        options.setBeepEnabled(false);
        options.setOrientationLocked(false);
        options.setBarcodeImageEnabled(false);
        scannerLauncher.launch(options);
    }
}