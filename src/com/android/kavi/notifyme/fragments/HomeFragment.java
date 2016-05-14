package com.android.kavi.notifyme.fragments;

import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.kavi.notifyme.R;
import com.android.kavi.notifyme.activities.NotifyMeActivity;
import com.android.kavi.notifyme.database.LocalDatabaseSQLiteOpenHelper;
import com.android.kavi.notifyme.database.LocationData;
import com.android.kavi.notifyme.services.ActivityUserPermissionServices;
import com.android.kavi.notifyme.services.location.LocationService;
import com.android.kavi.notifyme.services.location.LocatorCalls;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kavi on 11/5/14.
 * @author Kavimal Wijewardana <kavi707@gmail.com>
 */
public class HomeFragment extends Fragment implements LocationListener{

    private View homeFragmentView;

    private Button getLocationButton;
    private Button showOnMapButton;
    private Button startServiceButton;
    private Button stopServiceButton;

    private TextView cellInfoTextView;
    private TextView lacInfoTextView;
    private TextView locationInfoTextView;
    private TextView locationLongitudeInfoTextView;

    private NotificationManager mNotificationManager;
    private Context context;

    private LocationManager locationManager;
    // Declaring a Location Manager
    protected LocationManager networkLocationManager;
    private Location initLocation;
    private Location getLocation;
    // Flag for network status
    boolean isNetworkEnabled = false;
    // Flag for GPS status
    boolean canGetLocation = false;
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    private Map<String, Double> myLocationInfo = new HashMap<String, Double>();

    private boolean isGPSOn = false;
    private String provider;
    private Criteria criteria;

    private LocatorCalls locatorCalls = new LocatorCalls();
    private LocalDatabaseSQLiteOpenHelper localDatabaseSQLiteOpenHelper;
    private ActivityUserPermissionServices activityUserPermissionServices = new ActivityUserPermissionServices();

    public HomeFragment(){}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        this.context = getActivity();
        this.localDatabaseSQLiteOpenHelper = new LocalDatabaseSQLiteOpenHelper(context);
        this.mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        homeFragmentView = inflater.inflate(R.layout.fragment_home, container, false);
        setUpViews();
         
        return homeFragmentView;
    }

    private void setUpViews() {
        getLocationButton = (Button) homeFragmentView.findViewById(R.id.getMyLocationButton);
        showOnMapButton = (Button) homeFragmentView.findViewById(R.id.showMeOnMapButton);
        startServiceButton = (Button) homeFragmentView.findViewById(R.id.startLocationServiceButton);
        stopServiceButton = (Button) homeFragmentView.findViewById(R.id.stopLocationServiceButton);
        cellInfoTextView = (TextView) homeFragmentView.findViewById(R.id.cellInfoTextView);
        lacInfoTextView = (TextView) homeFragmentView.findViewById(R.id.lacInfoTextView);
        locationInfoTextView = (TextView) homeFragmentView.findViewById(R.id.locationInfoTextView);
        locationLongitudeInfoTextView = (TextView) homeFragmentView.findViewById(R.id.locationLongitudeInfoTextView);

        getLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isGPSOn = activityUserPermissionServices.isGPSOn(context);
                Log.d("GPS availability", "STATUS :" + isGPSOn);

                refreshCurrentLocation();
            }
        });

        showOnMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //---display Google Maps---
                if (myLocationInfo.get("lat") != 0.0) {
                    String uriString = "geo:" + myLocationInfo.get("lat")
                            + "," + myLocationInfo.get("log");
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse(uriString));
                    startActivity(intent);
                } else {
                    Toast.makeText(context, "Can't show location. Clear location not found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        startServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startService(new Intent(getActivity(), LocationService.class));
                showServiceNotification();
            }
        });

        stopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().stopService(new Intent(getActivity(), LocationService.class));
                mNotificationManager.cancel(1);
            }
        });
    }

    /**
     * Check the current location, using GPS or Cell tracking
     */
    private void refreshCurrentLocation() {

        Map<String, Integer> cellInfo = locatorCalls.getCellInformation(context);
        cellInfoTextView.setText("CellId: " + cellInfo.get("cellId"));
        lacInfoTextView.setText("Lac: " + cellInfo.get("lac"));

        Map<String, Double> logAndLatInfo = new HashMap<String, Double>();

        LocationData savedLocation = localDatabaseSQLiteOpenHelper.getLocationFromCellAndLac(cellInfo.get("cellId"), cellInfo.get("lac"));

        if (savedLocation != null) {
            // Grep the location params from local database
            logAndLatInfo.put("log", savedLocation.getLongitude());
            logAndLatInfo.put("lat", savedLocation.getLatitude());
        } else {
            if (isGPSOn) {
                //Check with GPS location listener
                localLocationListener();

                if (getLocation != null) {
                    // Saved new initLocation from GPS
                    logAndLatInfo.put("log", getLocation.getLongitude());
                    logAndLatInfo.put("lat", getLocation.getLatitude());
                } else {
                    // Couldn't find initLocation from GPS. Try from cell tracking
                    if (activityUserPermissionServices.isOnline(context)) {
                        logAndLatInfo = getLocationFromCell(cellInfo);
                        if (logAndLatInfo.get("log") == 0.0) {
                            // Couldn't find location from Cell tracking. Try from NETWORK_PROVIDER
                            logAndLatInfo = getLocationFromNetworkProvider();
                        }
                    } else
                        Toast.makeText(context, "Device is in offline", Toast.LENGTH_SHORT).show();
                }
            } else {
                // GPS not available. Try initLocation from cell tracking
                if (activityUserPermissionServices.isOnline(context)) {
                    logAndLatInfo = getLocationFromCell(cellInfo);
                    if (logAndLatInfo.get("log") == 0.0) {
                        // Couldn't find location from Cell tracking. Try from NETWORK_PROVIDER
                        logAndLatInfo = getLocationFromNetworkProvider();
                    }
                } else {
                    logAndLatInfo.put("log", 0.0);
                    logAndLatInfo.put("lat", 0.0);

                    // leads to the settings because there is no last known initLocation
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            }
        }

        myLocationInfo.put("lat", logAndLatInfo.get("lat"));
        myLocationInfo.put("log", logAndLatInfo.get("log"));

        locationInfoTextView.setText(String.valueOf(logAndLatInfo.get("lat")));
        locationLongitudeInfoTextView.setText(String.valueOf(logAndLatInfo.get("log")));
    }

    /**
     * GPS location listener method.
     */
    private void localLocationListener() {

        /********** get Gps location service LocationManager object ***********/
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
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
     * Get locations from Google location service
     * @return
     */
    private Map<String, Double> getLocationFromNetworkProvider() {

        Map<String, Double> latLongInfo = new HashMap<String, Double>();
        Location location;
        Double latitude, longitude;

        try {
            networkLocationManager = (LocationManager) context
                    .getSystemService(context.LOCATION_SERVICE);

            // Getting network status
            isNetworkEnabled = networkLocationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            this.canGetLocation = true;
            if (isNetworkEnabled) {
                networkLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                Log.d("Network", "Network Location finder");
                if (networkLocationManager != null) {
                    location = networkLocationManager
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        latLongInfo.put("log", longitude);
                        latLongInfo.put("lat", latitude);
                    } else {
                        Toast.makeText(context, "Couldn't find location", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return latLongInfo;
    }

    private Map<String, Double> getLocationFromCell(Map<String, Integer> cellInfo) {
        Map<String, Double> locationDetails;
        // Grep the location params from Google APIs
        locationDetails = locatorCalls.getLogAndLatLocations(cellInfo.get("cellId"), cellInfo.get("lac"));

        if (locationDetails.get("log") != 0.0) {
            locationInfoTextView.setText("Longitude: " + locationDetails.get("log") + " Latitude: " + locationDetails.get("lat"));
        } else {
            Toast.makeText(context, "Couldn't find location", Toast.LENGTH_SHORT).show();
        }

        return locationDetails;
    }

    /**
     * Showing icon for location service starting
     */
    private void showServiceNotification(){
        Notification notification = new Notification(R.drawable.ic_locator, "NotifyMe service started", System.currentTimeMillis());
        PendingIntent contentIntent = PendingIntent.getActivity(getActivity(), 0, new Intent(getActivity(), NotifyMeActivity.class), PendingIntent.FLAG_NO_CREATE);
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.setLatestEventInfo(getActivity(), "NotifyMe", "Searching your location", contentIntent);
        mNotificationManager.notify(1, notification);
    }

    @Override
    public void onLocationChanged(Location location) {
        this.getLocation = location;
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
