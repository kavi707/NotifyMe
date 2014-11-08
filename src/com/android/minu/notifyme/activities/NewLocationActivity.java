package com.android.minu.notifyme.activities;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.minu.notifyme.R;

/**
 * Created by kavi707 on 11/8/14.
 */
public class NewLocationActivity extends Activity implements LocationListener {

    private TextView cellIdTextView;
    private TextView lacTextView;
    private TextView longitudeTextView;
    private TextView latitudeTextView;

    private EditText locationNameEditText;

    private Button addNewLocationButton;
    private Button resetButton;
    private Button cancelButton;

    private Context context = this;
    private LocationManager locationManager;
    private Location location;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_location);

        /********** get Gps location service LocationManager object ***********/
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        setUpViews();
    }

    private void setUpViews() {
        cellIdTextView = (TextView) findViewById(R.id.cellIdTextView);
        lacTextView = (TextView) findViewById(R.id.lacTextView);
        longitudeTextView = (TextView) findViewById(R.id.longitudeTextView);
        latitudeTextView = (TextView) findViewById(R.id.latitudeTextView);

        locationNameEditText = (EditText) findViewById(R.id.locationNameEditText);

        addNewLocationButton = (Button) findViewById(R.id.addLocationButton);
        resetButton = (Button) findViewById(R.id.resetButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);


        addNewLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationNameEditText.setText(null);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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