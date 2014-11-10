package com.android.minu.notifyme.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.minu.notifyme.R;
import com.android.minu.notifyme.activities.NewLocationActivity;
import com.android.minu.notifyme.database.LocalDatabaseSQLiteOpenHelper;
import com.android.minu.notifyme.database.LocationData;
import com.android.minu.notifyme.services.ActivityUserPermissionServices;
import com.android.minu.notifyme.services.location.LocationService;
import com.android.minu.notifyme.services.location.LocatorCalls;

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment{

    private View homeFragmentView;

    private Button getLocationButton;
    private Button showOnMapButton;
    private Button startServiceButton;
    private Button stopServiceButton;

    private TextView cellInfoTextView;
    private TextView locationInfoTextView;

    //private Context context = getActivity().getApplicationContext();
    private Context context;
    private LocationManager locationManager;
    private Location location;
    private Map<String, Double> myLocationInfo;

    private boolean isGPSOn = false;

    private LocatorCalls locatorCalls = new LocatorCalls();
    private LocalDatabaseSQLiteOpenHelper localDatabaseSQLiteOpenHelper;
    private ActivityUserPermissionServices activityUserPermissionServices = new ActivityUserPermissionServices();

    public HomeFragment(){}

    public HomeFragment(Context context){
        this.context = context;
        this.localDatabaseSQLiteOpenHelper = new LocalDatabaseSQLiteOpenHelper(context);
    }

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

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
        locationInfoTextView = (TextView) homeFragmentView.findViewById(R.id.locationInfoTextView);

        getLocationButton.setOnClickListener(new View.OnClickListener() {
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
                            if (activityUserPermissionServices.isOnline(getActivity()))
                                logAndLatInfo = getLocationFromCell(cellInfo);
                            else
                                Toast.makeText(context, "Device is in offline", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // GPS not available. Try location from cell tracking
                        if (activityUserPermissionServices.isOnline(getActivity()))
                            logAndLatInfo = getLocationFromCell(cellInfo);
                        else
                            Toast.makeText(context, "Device is in offline", Toast.LENGTH_LONG).show();
                    }
                }

                myLocationInfo = logAndLatInfo;
                cellInfoTextView.setText("CellId: " + cellInfo.get("cellId") + "\nLac: " + cellInfo.get("lac"));
                locationInfoTextView.setText("Longitude: " + logAndLatInfo.get("log") + "\nLatitude: " + logAndLatInfo.get("lat"));
                Log.d("CELL", "cell ID: " + cellInfo.get("cellId") + " Lac: " + cellInfo.get("lac"));
                Log.d("LOCATIONS", "Log: " + logAndLatInfo.get("log") + " Lat: " + logAndLatInfo.get("lat"));
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
                    Toast.makeText(context, "Can't show location. Clear location not found", Toast.LENGTH_LONG).show();
                }
            }
        });

        startServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startService(new Intent(getActivity(), LocationService.class));
            }
        });

        stopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().stopService(new Intent(getActivity(), LocationService.class));
            }
        });
    }

    private Map<String, Double> getLocationFromCell(Map<String, Integer> cellInfo) {
        Map<String, Double> locationDetails;
        // Grep the location params from Google APIs
        locationDetails = locatorCalls.getLogAndLatLocations(cellInfo.get("cellId"), cellInfo.get("lac"));

        if (locationDetails.get("log") != 0.0) {
            locationInfoTextView.setText("Longitude: " + locationDetails.get("log") + " Latitude: " + locationDetails.get("lat"));

            // Save new location details to local database
            LocationData newLocation = new LocationData();
            newLocation.setCellId(cellInfo.get("cellId"));
            newLocation.setLac(cellInfo.get("lac"));
            newLocation.setLongitude(locationDetails.get("log"));
            newLocation.setLatitude(locationDetails.get("lat"));
            localDatabaseSQLiteOpenHelper.saveNewLocationData(newLocation);
        } else {
            Toast.makeText(context, "Couldn't find location", Toast.LENGTH_LONG).show();
        }

        return locationDetails;
    }
}
