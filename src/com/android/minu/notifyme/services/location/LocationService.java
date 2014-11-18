package com.android.minu.notifyme.services.location;

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

import com.android.minu.notifyme.database.ContactData;
import com.android.minu.notifyme.database.LocalDatabaseSQLiteOpenHelper;
import com.android.minu.notifyme.database.LocationData;
import com.android.minu.notifyme.services.ActivityUserPermissionServices;
import com.android.minu.notifyme.services.Sms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by kwijewardana on 11/3/14.
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
        Log.d("LocationService:onCreate / @Overide", "NotifyMy! LocationService Created");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        Toast.makeText(this, "NotifyMe LocationService Started", Toast.LENGTH_SHORT).show();
        Log.d("LocationService:onStart / @Overide", "NotifyMe LocationService Started");

        savedLocations = localDatabaseSQLiteOpenHelper.getAllLocations();
        savedContacts = localDatabaseSQLiteOpenHelper.getAllContacts();

        startTimer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Toast.makeText(this, "NotifyMe LocationService Stopped", Toast.LENGTH_SHORT).show();
        Log.d("LocationService:onDestroy / @Overide", "NotifyMe LocationService Stopped");

        stopTimerTask();
    }

    private void startTimer() {
        //set a new Timer
        locationTimer = new Timer();

        //initialize the TimerTask's job
        initializeLocationTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
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
                                Log.d("LocationService:initializeLocationTimerTask / Current Latitude: ", String.valueOf(locationInfo.get("lat")));
                                Log.d("LocationService:initializeLocationTimerTask / Saved location Latitude: ", String.valueOf(savedLocation.getLatitude()));

                                latitudeMatching = true;
                            } else {
                                latitudeMatching = false;
                            }

                            //check the longitude
                            if (locationInfo.get("log") < (savedLocation.getLongitude() + 0.01) &&
                                    locationInfo.get("log") > (savedLocation.getLongitude() - 0.01)) {

                                Log.d("LocationService:initializeLocationTimerTask / Current Longitude: ", String.valueOf(locationInfo.get("log")));
                                Log.d("LocationService:initializeLocationTimerTask / Saved location Longitude: ", String.valueOf(savedLocation.getLongitude()));

                                longitudeMatching = true;
                            } else {
                                longitudeMatching = false;
                            }

                            if (latitudeMatching && longitudeMatching) {
                                Log.d("LocationService:initializeLocationTimerTask / Found Location Name: ", "Location: " + savedLocation.getLocationName());

                                if (!sentLastLocation.equals(savedLocation.getLocationName())) {
                                    timeCounter = 0;
                                    sentLastLocation = savedLocation.getLocationName();
                                    //send the new location to selected contacts
                                    //msg = "Hey guys, Now I'm @ " + savedLocation.getLocationName() + ". Do you come to join with me?";
                                    //number = "0713063362";
                                    //sms.SendSms(number, msg, context);

                                    sendSmsToSelectedContacts(savedLocation);
                                    Toast.makeText(context, "New SMS sent", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (timeCounter > 60) {
                                        timeCounter = 0;
                                        //send the location to selected contacts
                                        //msg = "Hey guys, Now I'm @ " + savedLocation.getLocationName() + ". Do you come to join with me?";
                                        //number = "0713063362";
                                        //sms.SendSms(number, msg, context);

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

        Log.d("LocationService:refreshCurrentLocation / cell id: ", String.valueOf(cellInfo.get("cellId")));
        Log.d("LocationService:refreshCurrentLocation / L.A.C: ", String.valueOf(cellInfo.get("lac")));

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
                } else {
                    logAndLatInfo.put("log", 0.0);
                    logAndLatInfo.put("lat", 0.0);

                    Log.d("LocationService:refreshCurrentLocation / STATUS ", "Device is in offline");
                }
            }
        } else {
            // GPS not available. Try initLocation from cell tracking
            if (activityUserPermissionServices.isOnline(context)) {
                logAndLatInfo = getLocationFromCell(cellInfo);
            } else {
                logAndLatInfo.put("log", 0.0);
                logAndLatInfo.put("lat", 0.0);

                Log.d("LocationService:refreshCurrentLocation / STATUS ", "Device is in offline");
            }
        }

        Log.d("LocationService:refreshCurrentLocation / longitude: ", String.valueOf(logAndLatInfo.get("log")));
        Log.d("LocationService:refreshCurrentLocation / latitude: ", String.valueOf(logAndLatInfo.get("lat")));

        return logAndLatInfo;
    }

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

    private Map<String, Double> getLocationFromCell(Map<String, Integer> cellInfo) {
        Map<String, Double> locationDetails;
        // Grep the location params from Google APIs
        locationDetails = locatorCalls.getLogAndLatLocations(cellInfo.get("cellId"), cellInfo.get("lac"));

        if (locationDetails.get("log") == 0.0) {
            Log.d("LocationService:getLocationFromCell / STATUS ", "Couldn't find location");
        }

        return locationDetails;
    }

    private void sendSmsToSelectedContacts (LocationData savedLocation) {

        String msg;
        String number;

        if (savedContacts.size() != 0) {
            for (ContactData savedContact : savedContacts) {
                msg = "Hey "+ savedContact.getContactName() +", Now I'm @ " + savedLocation.getLocationName() + ". Do you come to join with me?";
                //TODO: We need to check this. QA part
                number = savedContact.getContactNumberData().split(",")[0];
                sms.SendSms(number, msg, context);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Initialize the initLocation fields
        Log.d("LocationService:onLocationChanged / Location Params : ", "Latitude: " + String.valueOf(location.getLatitude()));
        Log.d("LocationService:onLocationChanged /Location Params : ", "Longitude: " + String.valueOf(location.getLongitude()));
        Log.d("LocationService:onLocationChanged /Location Params : ", provider + " provider has been selected.");

        this.getLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("LocationService:onDestroy / @Overide", "Provider " + provider + " enabled!");
        Toast.makeText(this, "Provider " + provider + " enabled!",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("LocationService:onDestroy / @Overide", "Provider " + provider + " disabled!");
        Toast.makeText(this, "Provider " + provider + " disabled!",
                Toast.LENGTH_SHORT).show();
    }
}
