package com.android.minu.notifyme.services.location;

import android.content.Context;

import java.util.Map;

/**
 * Created by kavi707 on 11/2/14.
 */
public class LocatorCalls extends Locator {

    public Map<String, Integer> getCellInformation(Context context) {
        return getCellInfo(context);
    }

    public Map<String, Double> getLogAndLatLocations(int cellId, int lac) {
        return getLogAndLat(cellId, lac);
    }

    /*public Map<String, Double> findCellLocationInfo(Context context) {
        return findCellLocation(context);
    }*/
}
