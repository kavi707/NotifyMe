package com.android.minu.notifyme.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.minu.notifyme.R;
import com.android.minu.notifyme.database.LocalDatabaseSQLiteOpenHelper;
import com.android.minu.notifyme.database.LocationData;
import com.android.minu.notifyme.services.ActivityUserPermissionServices;
import com.android.minu.notifyme.services.location.LocationService;
import com.android.minu.notifyme.services.location.LocatorCalls;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kavi707 on 11/2/14.
 */
public class NotifierMenuActivity extends Activity implements LocationListener {

    private Button tempButton;
    private Button startServiceButton;
    private Button stopServiceButton;
    private Button nextViewButton;
    private TextView showMyLocationTextView;

    private LocatorCalls locatorCalls = new LocatorCalls();
    private Context context = this;
    private LocationManager locationManager;
    private Location location;

    private boolean isGPSOn = false;

    private LocalDatabaseSQLiteOpenHelper localDatabaseSQLiteOpenHelper = new LocalDatabaseSQLiteOpenHelper(context);
    private ActivityUserPermissionServices activityUserPermissionServices = new ActivityUserPermissionServices();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifier_menu);

        /********** get Gps location service LocationManager object ***********/
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 10, this);

        setUpViews();
    }

    private void setUpViews() {
        tempButton = (Button) findViewById(R.id.checkLocationButton);
        startServiceButton = (Button) findViewById(R.id.startServiceButton);
        stopServiceButton = (Button) findViewById(R.id.stopServiceButton);
        nextViewButton = (Button) findViewById(R.id.nextViewButton);
        showMyLocationTextView = (TextView) findViewById(R.id.showLocationTextView);

        tempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isGPSOn = activityUserPermissionServices.isGPSOn(context);
                Log.d("GPS availability", "STATUS :" + isGPSOn);

                Map<String, Integer> cellInfo = locatorCalls.getCellInformation(context);
                Map<String, Double> logAndLatInfo = new HashMap<String, Double>();

                LocationData savedLocation = localDatabaseSQLiteOpenHelper.getLocationFromCellAndLac(cellInfo.get("cellId"), cellInfo.get("lac"));
                if (savedLocation != null) {
                    // Grep the location params from local database
                    logAndLatInfo.put("log", savedLocation.getLongitude());
                    logAndLatInfo.put("lat", savedLocation.getLatitude());
                } else {
                    if (isGPSOn) {
                        if (location != null) {
                            // Saved new location from GPS
                            logAndLatInfo.put("log", location.getLongitude());
                            logAndLatInfo.put("lat", location.getLatitude());

                            LocationData newLocation = new LocationData();
                            newLocation.setCellId(cellInfo.get("cellId"));
                            newLocation.setLac(cellInfo.get("lac"));
                            newLocation.setLongitude(location.getLongitude());
                            newLocation.setLatitude(location.getLatitude());
                            localDatabaseSQLiteOpenHelper.saveNewLocationData(newLocation);
                        } else {
                            // Couldn't find location from GPS. Try from cell tracking
                            if (activityUserPermissionServices.isOnline(NotifierMenuActivity.this))
                                logAndLatInfo = getLocationFromCell(cellInfo);
                            else
                                Toast.makeText(context, "Device is in offline", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // GPS not available. Try location from cell tracking
                        if (activityUserPermissionServices.isOnline(NotifierMenuActivity.this))
                            logAndLatInfo = getLocationFromCell(cellInfo);
                        else
                            Toast.makeText(context, "Device is in offline", Toast.LENGTH_SHORT).show();
                    }
                }

                showMyLocationTextView.setText("Longitude: " + logAndLatInfo.get("log") + " Latitude: " + logAndLatInfo.get("lat"));
                Log.d("CELL", "cell ID: " + cellInfo.get("cellId") + " Lac: " + cellInfo.get("lac"));
                Log.d("LOCATIONS", "Log: " + logAndLatInfo.get("log") + " Lat: " + logAndLatInfo.get("lat"));
            }
        });

        startServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(NotifierMenuActivity.this, LocationService.class));
            }
        });

        stopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(NotifierMenuActivity.this, LocationService.class));
            }
        });

        nextViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NotifierMenuActivity.this, NotifyMeActivity.class));
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;

        String str = "Latitude: " + location.getLatitude() + " Longitude: " + location.getLongitude();
        Toast.makeText(getBaseContext(), str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        /******** Called when User on Gps  *********/
        Toast.makeText(getBaseContext(), "Gps turned on ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        /******** Called when User off Gps *********/
        Toast.makeText(getBaseContext(), "Gps turned off ", Toast.LENGTH_SHORT).show();
    }

    private Map<String, Double> getLocationFromCell(Map<String, Integer> cellInfo) {
        Map<String, Double> locationDetails;
        // Grep the location params from Google APIs
        locationDetails = locatorCalls.getLogAndLatLocations(cellInfo.get("cellId"), cellInfo.get("lac"));

        if (locationDetails.get("log") != 0.0) {
            showMyLocationTextView.setText("Longitude: " + locationDetails.get("log") + " Latitude: " + locationDetails.get("lat"));

            // Save new location details to local database
            LocationData newLocation = new LocationData();
            newLocation.setCellId(cellInfo.get("cellId"));
            newLocation.setLac(cellInfo.get("lac"));
            newLocation.setLongitude(locationDetails.get("log"));
            newLocation.setLatitude(locationDetails.get("lat"));
            localDatabaseSQLiteOpenHelper.saveNewLocationData(newLocation);
        } else {
            Toast.makeText(context, "Couldn't find location", Toast.LENGTH_SHORT).show();
        }

        return locationDetails;
    }
}
