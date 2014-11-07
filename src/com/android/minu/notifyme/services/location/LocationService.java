package com.android.minu.notifyme.services.location;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kwijewardana on 11/3/14.
 */
public class LocationService extends Service implements LocationListener {

    private Map<String, Integer> cellInfo = new HashMap<String, Integer>();
    private Map<String, Double> locationInfo = new HashMap<String, Double>();
    private boolean isLocationServiceStart = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Toast.makeText(this, "Congrats! MyService Created", Toast.LENGTH_LONG).show();
        Log.d("Location Service", "onCreate");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
        Log.d("Location Service", "onStart");
        isLocationServiceStart = true;

        final Thread locationFinderThread = new Thread() {
            @Override
            public void run() {
                super.run();

                while (isLocationServiceStart) {
                    LocatorCalls locatorCalls = new LocatorCalls();
                    //locationInfo = locatorCalls.findCellLocationInfo(getApplicationContext());
                    cellInfo = locatorCalls.getCellInformation(getApplicationContext());
                    locationInfo = locatorCalls.getLogAndLatLocations(cellInfo.get("cellId"), cellInfo.get("lac"));
                    Log.d(">>>>>>>>>>>>>>>>>>>>>>> ", "Longitude: " + locationInfo.get("log") + " Latitude: " + locationInfo.get("lat"));
                }
            }
        };
        locationFinderThread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Toast.makeText(this, "MyService Stopped", Toast.LENGTH_LONG).show();
        Log.d("Location Service", "onDestroy");

        isLocationServiceStart = false;
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
