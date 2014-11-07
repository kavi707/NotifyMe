package com.android.minu.notifyme.services.connection;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kavi707 on 11/1/14.
 */
public class ConnectorCalls extends Connector {

    public Map<String, Double> getLocation(String url, int cellId, int lac) {
        Map<String, Integer> reqParams = new HashMap<String, Integer>();
        reqParams.put("cellId", cellId);
        reqParams.put("lac", lac);
        return sendHttpPostReq(url, reqParams);
    }
}
