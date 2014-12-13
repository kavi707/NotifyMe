package com.android.minu.notifyme.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.android.minu.notifyme.R;
import com.android.minu.notifyme.database.LocationData;
import com.android.minu.notifyme.views.LocationListItem;

import java.util.List;

/**
 * Created by minuri on 11/10/14.
 * @author Minuri Rajapaksha <rpmchathu@gmail.com>
 */
public class LocationItemAdapter extends BaseAdapter {

    private List<LocationData> locationDataList;
    private Context context;

    public LocationItemAdapter(List<LocationData> locationDataList, Context context) {
        this.locationDataList = locationDataList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return locationDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return (locationDataList == null)? null: locationDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LocationListItem locationListItem;
        if (convertView == null){
            locationListItem = (LocationListItem) View.inflate(context, R.layout.location_list_item, null);
        } else {
            locationListItem = (LocationListItem) convertView;
        }

        locationListItem.setLocationData(locationDataList.get(position));
        return locationListItem;
    }
}
