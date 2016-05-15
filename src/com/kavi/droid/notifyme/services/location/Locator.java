package com.kavi.droid.notifyme.services.location;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.kavi.droid.notifyme.CommonUtils;
import com.kavi.droid.notifyme.services.connection.ConnectorCalls;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kavi on 11/4/14.
 * @author Kavimal Wijewardana <kavi707@gmail.com>
 */
public class Locator {

    private Context context;
    Map<String, Double> langAndLatInfo = new HashMap<String, Double>();

    protected Map<String, Integer> getCellInfo(Context context) {

        Log.d("Locator", "Locator:getCellInfo");
        GsmCellLocation location;
        Map<String, Integer> cellInfo = new HashMap<String, Integer>();

        TelephonyManager tm = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        location = (GsmCellLocation) tm.getCellLocation();

        cellInfo.put("cellId", location.getCid());
        cellInfo.put("lac", location.getLac());

        return cellInfo;
    }

    protected Map<String, Double> getLogAndLat(int cellId, int lac) {

        Log.d("Locator", "Locator:getLogAndLat");
        Map<String, Double> logAndLatInfo;

        ConnectorCalls connectorCalls = new ConnectorCalls();
        logAndLatInfo = connectorCalls.getLocation(CommonUtils.GOOGLE_MAPS_URL, cellId, lac);

        return logAndLatInfo;
    }

    /*protected Map<String, Double> findCellLocation (Context context) {
        Log.d("Locator", "Locator:findLocation");

        this.context = context;

        FindCellLocationTask findLocationTask = new FindCellLocationTask();
        try {
            langAndLatInfo = findLocationTask.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return langAndLatInfo;
    }

    private class FindCellLocationTask extends AsyncTask<Void, Void, Map<String, Double>> {

        @Override
        protected Map<String, Double> doInBackground(Void... params) {
            Map<String, Double> locationInfo = new HashMap<String, Double>();
            Log.d("Locator", "Locator:FindLocationTask");

            Map<String, Integer> cellInfo = getCellInfo(context);
            if (cellInfo != null) {
                locationInfo = getLogAndLat(cellInfo.get("cellId"), cellInfo.get("lac"));
            } else {
                locationInfo.put("log", 0.0);
                locationInfo.put("lat", 0.0);
            }

            return locationInfo;
        }
    }*/
}
