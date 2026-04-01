package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import androidx.core.content.ContextCompat;

/**
 * Utility class for retrieving the device's last known location.
 * Uses Android's built-in LocationManager (no Google Play Services required).
 * The result is delivered asynchronously via a callback.
 */
public class LocationHelper {

    public interface LocationCallback {
        void onLocationResult(Double latitude, Double longitude);
    }

    /**
     * Gets the device's last known location and delivers it to the callback.
     * Returns (null, null) if permission is not granted or location is unavailable.
     *
     * @param context  application or activity context
     * @param callback receives the latitude and longitude, or (null, null) on failure
     */
    public static void getCurrentLocation(Context context, LocationCallback callback) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            callback.onLocationResult(null, null);
            return;
        }

        LocationManager locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager == null) {
            callback.onLocationResult(null, null);
            return;
        }

        // Try GPS first, fall back to network provider
        Location location = null;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (location == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (location != null) {
            callback.onLocationResult(location.getLatitude(), location.getLongitude());
        } else {
            callback.onLocationResult(null, null);
        }
    }
}