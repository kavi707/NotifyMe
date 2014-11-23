package com.android.minu.notifyme.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.minu.notifyme.R;
import com.android.minu.notifyme.database.LocalDatabaseSQLiteOpenHelper;
import com.android.minu.notifyme.database.LocationData;
import com.android.minu.notifyme.services.ActivityUserPermissionServices;
import com.android.minu.notifyme.services.location.LocatorCalls;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kavi707 on 11/8/14.
 */
public class NewLocationActivity extends Activity implements LocationListener {

    private TextView cellIdTextView;
    private TextView lacTextView;
    private TextView longitudeTextView;
    private TextView latitudeTextView;

    private EditText locationNameEditText;
    private EditText locationDetailsEditText;

    private Button addNewLocationButton;
    private Button refreshButton;
    private Button cancelButton;

    private Context context = this;
    private LocationManager locationManager;
    private Location initLocation;
    private Location getLocation;
    private Criteria criteria;

    private String provider;
    private boolean isGPSOn = false;
    private boolean isLocationAlreadySaved = false;
    private LocatorCalls locatorCalls = new LocatorCalls();
    private LocalDatabaseSQLiteOpenHelper localDatabaseSQLiteOpenHelper = new LocalDatabaseSQLiteOpenHelper(context);
    private ActivityUserPermissionServices activityUserPermissionServices = new ActivityUserPermissionServices();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_location);

        setUpViews();
    }

    private void setUpViews() {

        // Initialise UI object from activity_new_location.xml
        cellIdTextView = (TextView) findViewById(R.id.cellIdTextView);
        lacTextView = (TextView) findViewById(R.id.lacTextView);
        longitudeTextView = (TextView) findViewById(R.id.longitudeTextView);
        latitudeTextView = (TextView) findViewById(R.id.latitudeTextView);

        locationNameEditText = (EditText) findViewById(R.id.locationNameEditText);
        locationDetailsEditText = (EditText) findViewById(R.id.locationDetailsEditText);

        addNewLocationButton = (Button) findViewById(R.id.addLocationButton);
        refreshButton = (Button) findViewById(R.id.refreshButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);


        // Check the GPS status on the device
        isGPSOn = activityUserPermissionServices.isGPSOn(context);

        Log.d("GPS availability", "STATUS :" + isGPSOn);

        // Check the current location details
        refreshCurrentLocation();

        addNewLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cellId = cellIdTextView.getText().toString();
                String lac = lacTextView.getText().toString();
                String latitude = latitudeTextView.getText().toString();
                String longitude = longitudeTextView.getText().toString();
                String locationName = locationNameEditText.getText().toString();
                String locationDescription = locationDetailsEditText.getText().toString();

                if (isLocationAlreadySaved) {
                    Toast.makeText(context, "Current location is already saved in application", Toast.LENGTH_SHORT).show();
                } else {
                    if (latitude.equals("0.0")) {
                        Toast.makeText(context, "Couldn't find exact location, please refresh", Toast.LENGTH_SHORT).show();
                    } else {
                        if (locationName.equals(null) || locationName.equals("")) {
                            Toast.makeText(context, "Please add name to your new location", Toast.LENGTH_SHORT).show();
                        } else {

                            LocationData locationData = new LocationData();
                            locationData.setLocationName(locationName);
                            locationData.setLocationDescription(locationDescription);
                            locationData.setCellId(Integer.parseInt(cellId));
                            locationData.setLac(Integer.parseInt(lac));
                            locationData.setLatitude(Double.parseDouble(latitude));
                            locationData.setLongitude(Double.parseDouble(longitude));

                            localDatabaseSQLiteOpenHelper.saveNewLocationData(locationData);

                            Toast.makeText(context, "Added new location to application", Toast.LENGTH_SHORT).show();

                            finish();
                        }
                    }
                }
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshCurrentLocation();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Check the current location, using GPS or Cell tracking
     */
    private void refreshCurrentLocation() {

        Map<String, Integer> cellInfo = locatorCalls.getCellInformation(context);
        cellIdTextView.setText(String.valueOf(cellInfo.get("cellId")));
        lacTextView.setText(String.valueOf(cellInfo.get("lac")));

        Map<String, Double> logAndLatInfo = new HashMap<String, Double>();

        LocationData savedLocation = localDatabaseSQLiteOpenHelper.getLocationFromCellAndLac(cellInfo.get("cellId"), cellInfo.get("lac"));


        if (isGPSOn) {
            //Check with GPS location listener
            localLocationListener();

            if (getLocation != null) {
                // Saved new initLocation from GPS
                logAndLatInfo.put("log", getLocation.getLongitude());
                logAndLatInfo.put("lat", getLocation.getLatitude());

                if (savedLocation != null) {
                    if (getLocation.getLongitude() == savedLocation.getLongitude() && getLocation.getLatitude() == savedLocation.getLatitude()) {
                        isLocationAlreadySaved = true;
                    }
                }
            } else {
                // Couldn't find initLocation from GPS. Try from cell tracking
                if (activityUserPermissionServices.isOnline(NewLocationActivity.this)) {
                    logAndLatInfo = getLocationFromCell(cellInfo);
                    if (savedLocation != null) {
                        if (logAndLatInfo.get("log") == savedLocation.getLongitude() && logAndLatInfo.get("lat") == savedLocation.getLatitude()) {
                            isLocationAlreadySaved = true;
                        }
                    }
                } else
                    Toast.makeText(context, "Device is in offline", Toast.LENGTH_SHORT).show();
            }
        } else {
            // GPS not available. Try initLocation from cell tracking
            if (activityUserPermissionServices.isOnline(NewLocationActivity.this)) {
                logAndLatInfo = getLocationFromCell(cellInfo);
                if (savedLocation != null) {
                    if (logAndLatInfo.get("log") == savedLocation.getLongitude() && logAndLatInfo.get("lat") == savedLocation.getLatitude()) {
                        isLocationAlreadySaved = true;
                    }
                }
            } else {
                logAndLatInfo.put("log", 0.0);
                logAndLatInfo.put("lat", 0.0);

                // leads to the settings because there is no last known initLocation
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }

        longitudeTextView.setText(logAndLatInfo.get("log").toString());
        latitudeTextView.setText(logAndLatInfo.get("lat").toString());
    }

    /**
     * GPS location listener method.
     */
    private void localLocationListener() {

        /********** get Gps location service LocationManager object ***********/
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the initLocation provider
        criteria = new Criteria();

        //coarse accuracy selected
        //criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        //fine accuracy selected
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        criteria.setCostAllowed(false);
        // get the best provider depending on the criteria
        provider = locationManager.getBestProvider(criteria, false);

        // the last known initLocation of this provider
        initLocation = locationManager.getLastKnownLocation(provider);

        if (initLocation != null) {
            this.onLocationChanged(initLocation);
        }
        // initLocation updates: at least 1 meter and 200 milly secs change
        locationManager.requestLocationUpdates(provider, 200, 1, this);
    }

    /**
     * Search location from Cell tracking
     * @param cellInfo
     * @return Map<String, Double> locationDetails
     */
    private Map<String, Double> getLocationFromCell(Map<String, Integer> cellInfo) {
        Map<String, Double> locationDetails;
        // Grep the location params from Google APIs
        locationDetails = locatorCalls.getLogAndLatLocations(cellInfo.get("cellId"), cellInfo.get("lac"));

        if (locationDetails.get("log") == 0.0) {
            Toast.makeText(context, "Couldn't find location", Toast.LENGTH_SHORT).show();
        }

        return locationDetails;
    }


    // Location listener implemented methods

    @Override
    public void onLocationChanged(Location location) {
        // Initialize the initLocation fields
        Log.d("Location Params : ", "Latitude: " + String.valueOf(location.getLatitude()));
        Log.d("Location Params : ", "Longitude: " + String.valueOf(location.getLongitude()));
        Log.d("Location Params : ", provider + " provider has been selected.");

        this.getLocation = location;

        Toast.makeText(NewLocationActivity.this, "Location changed!",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Toast.makeText(NewLocationActivity.this, provider + "'s status changed to " + status + "!",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(NewLocationActivity.this, "Provider " + provider + " enabled!",
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(NewLocationActivity.this, "Provider " + provider + " disabled!",
                Toast.LENGTH_SHORT).show();
    }
}