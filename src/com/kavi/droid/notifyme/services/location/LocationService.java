package com.kavi.droid.notifyme.services.location;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.kavi.droid.notifyme.database.ContactData;
import com.kavi.droid.notifyme.database.LocalDatabaseSQLiteOpenHelper;
import com.kavi.droid.notifyme.database.LocationData;
import com.kavi.droid.notifyme.services.ActivityUserPermissionServices;
import com.kavi.droid.notifyme.services.Sms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Kavi on 11/3/14.
 * @author Kavimal Wijewardana <kavi707@gmail.com>
 */
public class LocationService extends Service implements LocationListener {

    private Map<String, Integer> cellInfo = new HashMap<String, Integer>();
    private Map<String, Double> locationInfo = new HashMap<String, Double>();
    private boolean isGPSOn = false;
    private List<LocationData> savedLocations = new ArrayList<LocationData>();
    private List<ContactData> savedContacts = new ArrayList<ContactData>();
    private int timeCounter = 0;
    private String sentLastLocation = "";

    private LocationManager locationManager;
    private String provider;
    private Location initLocation;
    private Location getLocation;
    private Criteria criteria;

    // Declaring a Location Manager
    protected LocationManager networkLocationManager;

    // Flag for network status
    boolean isNetworkEnabled = false;

    // Flag for GPS status
    boolean canGetLocation = false;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    private Context context = this;
    private LocatorCalls locatorCalls = new LocatorCalls();
    private LocalDatabaseSQLiteOpenHelper localDatabaseSQLiteOpenHelper = new LocalDatabaseSQLiteOpenHelper(context);
    private ActivityUserPermissionServices activityUserPermissionServices = new ActivityUserPermissionServices();
    private Sms sms = new Sms();

    private Timer locationTimer;
    private TimerTask locationTimeTask;

    //we are going to use a handler to be able to run in our TimerTask
    private final Handler handler = new Handler();


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Toast.makeText(this, "NotifyMy! LocationService Created", Toast.LENGTH_SHORT).show();
        Log.d("NotifyMe", "LocationService:onCreate / @Overide: NotifyMy! LocationService Created");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        Toast.makeText(this, "NotifyMe LocationService Started", Toast.LENGTH_SHORT).show();
        Log.d("NotifyMe", "LocationService:onStart / @Overide: NotifyMe LocationService Started");

        savedLocations = localDatabaseSQLiteOpenHelper.getAllLocations();
        savedContacts = localDatabaseSQLiteOpenHelper.getAllContacts();

        startTimer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Toast.makeText(this, "NotifyMe LocationService Stopped", Toast.LENGTH_SHORT).show();
        Log.d("NotifyMe", "LocationService:onDestroy / @Overide: NotifyMe LocationService Stopped");

        stopTimerTask();
    }

    private void startTimer() {
        //set a new Timer
        locationTimer = new Timer();

        //initialize the TimerTask's job
        initializeLocationTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 60000ms (per minute)
        locationTimer.schedule(locationTimeTask, 5000, 60000); //
    }

    private void stopTimerTask() {
        //stop the timer, if it's not already null
        if (locationTimer != null) {
            locationTimer.cancel();
            locationTimer = null;
        }
    }

    private void initializeLocationTimerTask() {

        locationTimeTask = new TimerTask() {
            public void run() {

                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {

                        // Increase time counter
                        timeCounter ++;

                        locationInfo = refreshCurrentLocation();
                        boolean longitudeMatching = false;
                        boolean latitudeMatching = false;

                        for (LocationData savedLocation : savedLocations) {

                            longitudeMatching = false;
                            latitudeMatching = false;

                            //check the latitude
                            if (locationInfo.get("lat") < (savedLocation.getLatitude() + 0.01) &&
                                    locationInfo.get("lat") > (savedLocation.getLatitude() - 0.01)) {
                                Log.d("NotifyMe", "LocationService:initializeLocationTimerTask / Current Latitude: " + String.valueOf(locationInfo.get("lat")));
                                Log.d("NotifyMe", "LocationService:initializeLocationTimerTask / Saved location Latitude: " + String.valueOf(savedLocation.getLatitude()));

                                latitudeMatching = true;
                            } else {
                                latitudeMatching = false;
                            }

                            //check the longitude
                            if (locationInfo.get("log") < (savedLocation.getLongitude() + 0.01) &&
                                    locationInfo.get("log") > (savedLocation.getLongitude() - 0.01)) {

                                Log.d("NotifyMe", "LocationService:initializeLocationTimerTask / Current Longitude: " + String.valueOf(locationInfo.get("log")));
                                Log.d("NotifyMe", "LocationService:initializeLocationTimerTask / Saved location Longitude: " + String.valueOf(savedLocation.getLongitude()));

                                longitudeMatching = true;
                            } else {
                                longitudeMatching = false;
                            }

                            if (latitudeMatching && longitudeMatching) {
                                Log.d("NotifyMe", "LocationService:initializeLocationTimerTask / Found Location Name: Location: " + savedLocation.getLocationName());

                                if (!sentLastLocation.equals(savedLocation.getLocationName())) {
                                    timeCounter = 0;
                                    sentLastLocation = savedLocation.getLocationName();
                                    //send the new location to selected contacts
                                    sendSmsToSelectedContacts(savedLocation);
                                    Toast.makeText(context, "New SMS sent", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (timeCounter > 60) {
                                        timeCounter = 0;
                                        //send the location to selected contacts
                                        sendSmsToSelectedContacts(savedLocation);
                                        Toast.makeText(context, "New SMS sent", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    }
                });
            }
        };
    }

    private Map<String, Double> refreshCurrentLocation() {

        Map<String, Integer> cellInfo = locatorCalls.getCellInformation(context);
        Map<String, Double> logAndLatInfo = new HashMap<String, Double>();

        Log.d("NotifyMe", "LocationService:refreshCurrentLocation / cell id: " + String.valueOf(cellInfo.get("cellId")));
        Log.d("NotifyMe", "LocationService:refreshCurrentLocation / L.A.C: " + String.valueOf(cellInfo.get("lac")));

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
                    if (logAndLatInfo.get("log") == 0.0 && logAndLatInfo.get("lat") == 0.0) {
                        logAndLatInfo = getLocationFromNetworkProvider();
                    }
                } else {
                    logAndLatInfo.put("log", 0.0);
                    logAndLatInfo.put("lat", 0.0);

                    Log.d("NotifyMe", "LocationService:refreshCurrentLocation / STATUS: Device is in offline");
                }
            }
        } else {
            // GPS not available. Try initLocation from cell tracking
            if (activityUserPermissionServices.isOnline(context)) {
                logAndLatInfo = getLocationFromCell(cellInfo);
                if (logAndLatInfo.get("log") == 0.0 && logAndLatInfo.get("lat") == 0.0) {
                    logAndLatInfo = getLocationFromNetworkProvider();
                }
            } else {
                logAndLatInfo.put("log", 0.0);
                logAndLatInfo.put("lat", 0.0);

                Log.d("NotifyMe", "LocationService:refreshCurrentLocation / STATUS: Device is in offline");
            }
        }

        Log.d("NotifyMe", "LocationService:refreshCurrentLocation / longitude: " + String.valueOf(logAndLatInfo.get("log")));
        Log.d("NotifyMe", "LocationService:refreshCurrentLocation / latitude: " + String.valueOf(logAndLatInfo.get("lat")));

        return logAndLatInfo;
    }


    /**
     * Fine location from GPS. If GPS available
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
     * Find location from Network cells
     * @param cellInfo
     * @return
     */
    private Map<String, Double> getLocationFromCell(Map<String, Integer> cellInfo) {
        Map<String, Double> locationDetails;
        // Grep the location params from Google APIs
        locationDetails = locatorCalls.getLogAndLatLocations(cellInfo.get("cellId"), cellInfo.get("lac"));

        if (locationDetails.get("log") == 0.0) {
            Log.d("NotifyMe", "LocationService:getLocationFromCell / STATUS: Couldn't find location");
        }

        return locationDetails;
    }

    /**
     * Find location from Network Provider
     * @return
     */
    private Map<String, Double> getLocationFromNetworkProvider() {

        Map<String, Double> latLongInfo = new HashMap<String, Double>();
        Location location;
        Double latitude, longitude;

        try {
            networkLocationManager = (LocationManager) context
                    .getSystemService(LOCATION_SERVICE);

            // Getting network status
            isNetworkEnabled = networkLocationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            this.canGetLocation = true;
            if (isNetworkEnabled) {
                networkLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                Log.d("NotifyMe", "LocationService:getLocationFromNetworkProvider / Network: Network Location finder");
                if (networkLocationManager != null) {
                    location = networkLocationManager
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        latLongInfo.put("log", longitude);
                        latLongInfo.put("lat", latitude);
                    } else {
                        Log.d("NotifyMe", "LocationService:getLocationFromNetworkProvider / STATUS: Couldn't find location");
                        Toast.makeText(context, "Couldn't find location", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return latLongInfo;
    }

    /**
     * Send sms to selected contact in the application
     * @param savedLocation
     */
    private void sendSmsToSelectedContacts (LocationData savedLocation) {

        String msg;
        String number;

        if (savedContacts.size() != 0) {
            for (ContactData savedContact : savedContacts) {
                msg = "Hey "+ savedContact.getContactName() +", Now I'm @ " + savedLocation.getLocationName() + ". Do you come to join with me?";
                //TODO: We need to check this. QA part
                number = savedContact.getContactNumberData();
                sms.SendSms(number, msg, context);

                // Without reports
                /*PendingIntent pi = PendingIntent.getActivity(this, 0,
                        new Intent(this, LocationService.class), 0);

                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(number, null, msg, pi, null);*/
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Initialize the initLocation fields
        Log.d("NotifyMe", "LocationService:onLocationChanged / Location Params : Latitude: " + String.valueOf(location.getLatitude()));
        Log.d("NotifyMe", "LocationService:onLocationChanged /Location Params : Longitude: " + String.valueOf(location.getLongitude()));
        Log.d("NotifyMe", "LocationService:onLocationChanged /Location Params : " + provider + " provider has been selected.");

        this.getLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("NotifyMe", "LocationService:onDestroy / @Overide: Provider " + provider + " enabled!");
        Toast.makeText(this, "Provider " + provider + " enabled!",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("NotifyMe", "LocationService:onDestroy / @Overide: Provider " + provider + " disabled!");
        Toast.makeText(this, "Provider " + provider + " disabled!",
                Toast.LENGTH_SHORT).show();
    }
}
