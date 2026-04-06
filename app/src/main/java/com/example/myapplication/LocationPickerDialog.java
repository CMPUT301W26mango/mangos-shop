package com.example.myapplication;

import android.app.Dialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A full-screen DialogFragment that displays an osmdroid map for picking a location.
 *
 * The organizer can type an address in the search bar, tap Search to geocode it,
 * or tap directly on the map to reverse-geocode a position. Tapping Confirm
 * returns the selected address string to the caller via OnLocationSelectedListener.
 */
public class LocationPickerDialog extends DialogFragment {

    public interface OnLocationSelectedListener {
        void onLocationSelected(String address);
    }

    private OnLocationSelectedListener locationSelectedListener;
    private MapView mapView;
    private EditText searchInput;
    private Marker currentMarker;
    private String selectedAddress = null;

    public static LocationPickerDialog newInstance() {
        return new LocationPickerDialog();
    }

    public void setOnLocationSelectedListener(OnLocationSelectedListener listener) {
        this.locationSelectedListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_location_picker, container, false);

        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        mapView = view.findViewById(R.id.picker_map_view);
        searchInput = view.findViewById(R.id.location_search_input);
        Button searchButton = view.findViewById(R.id.location_search_button);
        Button confirmButton = view.findViewById(R.id.location_confirm_button);
        Button cancelButton = view.findViewById(R.id.location_cancel_button);

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(3.0);
        mapView.getController().setCenter(new GeoPoint(0.0, 0.0));

        MapEventsOverlay tapOverlay = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                placeMarker(p);
                reverseGeocode(p);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        });
        mapView.getOverlays().add(0, tapOverlay);

        searchButton.setOnClickListener(v ->
                searchAddress(searchInput.getText().toString().trim()));

        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchAddress(searchInput.getText().toString().trim());
                return true;
            }
            return false;
        });

        confirmButton.setOnClickListener(v -> {
            if (selectedAddress == null || selectedAddress.isEmpty()) {
                Toast.makeText(requireContext(),
                        "Please select a location on the map first", Toast.LENGTH_SHORT).show();
            } else {
                if (locationSelectedListener != null) {
                    locationSelectedListener.onLocationSelected(selectedAddress);
                }
                dismiss();
            }
        });

        cancelButton.setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    private void placeMarker(GeoPoint point) {
        if (currentMarker != null) {
            mapView.getOverlays().remove(currentMarker);
        }
        currentMarker = new Marker(mapView);
        currentMarker.setPosition(point);
        currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(currentMarker);
        mapView.getController().animateTo(point);
        mapView.invalidate();
    }

    private void searchAddress(String query) {
        if (query.isEmpty()) {
            Toast.makeText(requireContext(), "Enter an address to search", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Geocoder.isPresent()) {
            Toast.makeText(requireContext(),
                    "Address search is not available on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocationName(query, 5);
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (addresses == null || addresses.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "No results found. Try a different address.", Toast.LENGTH_SHORT).show();
                    } else {
                        Address first = addresses.get(0);
                        GeoPoint point = new GeoPoint(first.getLatitude(), first.getLongitude());
                        String addressLine = first.getAddressLine(0);
                        selectedAddress = (addressLine != null) ? addressLine : query;
                        searchInput.setText(selectedAddress);
                        placeMarker(point);
                        mapView.getController().setZoom(15.0);
                    }
                });
            } catch (IOException e) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(),
                                "Search failed. Check your internet connection.", Toast.LENGTH_SHORT).show()
                );
            }
        });
        executor.shutdown();
    }

    private void reverseGeocode(GeoPoint point) {
        if (!Geocoder.isPresent()) {
            selectedAddress = point.getLatitude() + ", " + point.getLongitude();
            searchInput.setText(selectedAddress);
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                List<Address> results = geocoder.getFromLocation(
                        point.getLatitude(), point.getLongitude(), 1);
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (results != null && !results.isEmpty()) {
                        String addressLine = results.get(0).getAddressLine(0);
                        selectedAddress = (addressLine != null)
                                ? addressLine
                                : point.getLatitude() + ", " + point.getLongitude();
                    } else {
                        selectedAddress = point.getLatitude() + ", " + point.getLongitude();
                    }
                    searchInput.setText(selectedAddress);
                });
            } catch (IOException e) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    selectedAddress = point.getLatitude() + ", " + point.getLongitude();
                    searchInput.setText(selectedAddress);
                });
            }
        });
        executor.shutdown();
    }
}
