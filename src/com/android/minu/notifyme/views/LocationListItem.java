package com.android.minu.notifyme.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.minu.notifyme.R;
import com.android.minu.notifyme.database.LocationData;

/**
 * Created by minuri on 11/10/14.
 * @author Minuri Rajapaksha <rpmchathu@gmail.com>
 */
public class LocationListItem extends LinearLayout {

    private TextView locationNameTextView;
    private TextView locationDetailsTextView;

    private LocationData locationData;

    public LocationListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        locationNameTextView = (TextView) findViewById(R.id.locationItemNameTextView);
        locationDetailsTextView = (TextView) findViewById(R.id.locationItemDetailsTextView);
    }

    public LocationData getLocationData() {
        return locationData;
    }

    public void setLocationData(LocationData locationData) {
        this.locationData = locationData;

        locationNameTextView.setText(locationData.getLocationName());
        locationDetailsTextView.setText(locationData.getLocationDescription());
    }
}
