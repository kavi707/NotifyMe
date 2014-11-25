package com.android.minu.notifyme.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.android.minu.notifyme.R;

/**
 * Created by kavi707 on 11/25/14.
 */
public class TestLocationActivity extends Activity implements LocationListener {

    private Context context = this;

    // Flag for GPS status
    boolean isGPSEnabled = false;

    // Flag for network status
    boolean isNetworkEnabled = false;

    // Flag for GPS status
    boolean canGetLocation = false;

    Location location; // Location
    double latitude; // Latitude
    double longitude; // Longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        getMyLocation();
    }

    public Location getMyLocation() {
        try {
            locationManager = (LocationManager) context
                    .getSystemService(LOCATION_SERVICE);

            // Getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            this.canGetLocation = true;
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                Log.d("Network", "Network");
                if (locationManager != null) {
                    location = locationManager
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        String uriString = "geo:" + latitude
                                + "," + longitude;
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                Uri.parse(uriString));
                        startActivity(intent);

                        Log.d(">>>>>>>>>>>>>> Longitude: ", String.valueOf(longitude));
                        Log.d(">>>>>>>>>>>>>> Latitude: ", String.valueOf(latitude));
                    } else {
                        Log.d(">>>>>>>>>>>>>>>>>>>>>>", "Location not found");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
