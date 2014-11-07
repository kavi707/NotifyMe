package com.android.minu.notifyme.services.connection;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by kavi707 on 11/1/14.
 */
public class Connector {

    private String postRequestUrl;
    private int cellId;
    private int lac;
    private Map<String, Double> httpCommonResponse = null;

    /**
     * Calling the background task to sending HTTP Post using JSON data
     * @param url
     * @param reqParams
     */
    protected Map<String, Double> sendHttpPostReq(String url, Map<String, Integer> reqParams) {
        Log.d("Connector", "Connector:sendHttpPostReq");
        this.postRequestUrl = url;
        this.cellId = reqParams.get("cellId");
        this.lac = reqParams.get("lac");

        try {
            SendHttpPostTask sendHttpPostTask = new SendHttpPostTask();
            httpCommonResponse = sendHttpPostTask.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return httpCommonResponse;
    }

    /**
     * Background Task for send HTTP POST with JSON data
     */
    private class SendHttpPostTask extends AsyncTask<Void, Void, Map<String, Double>> {

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p/>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected Map<String, Double> doInBackground(Void... params) {

            Map<String, Double> lagAndLatInfo = new HashMap<String, Double>();
            Log.d("Connector", "Connector:SendHttpPostTask");

            try {
                Log.d("Connector:SendHttpPostTask / Req Url : ", postRequestUrl);

                URL url = new URL(postRequestUrl);
                URLConnection conn = url.openConnection();
                HttpURLConnection httpConn = (HttpURLConnection) conn;
                httpConn.setRequestMethod("POST");
                httpConn.setDoOutput(true);
                httpConn.setDoInput(true);
                httpConn.connect();

                //---write some custom data to Google Maps API---
                OutputStream outputStream = httpConn.getOutputStream();
                WriteData(outputStream, cellId, lac);

                //---get the response---
                InputStream inputStream = httpConn.getInputStream();
                DataInputStream dataInputStream = new DataInputStream(inputStream);

                //---interpret the response obtained---
                dataInputStream.readShort();
                dataInputStream.readByte();
                int code = dataInputStream.readInt();

                if (code == 0) {
                    double lat = (double) dataInputStream.readInt() / 1000000D;
                    double lng = (double) dataInputStream.readInt() / 1000000D;
                    dataInputStream.readInt();
                    dataInputStream.readInt();
                    dataInputStream.readUTF();

                    lagAndLatInfo.put("log", lng);
                    lagAndLatInfo.put("lat", lat);
                } else {
                    lagAndLatInfo.put("log", 0.0);
                    lagAndLatInfo.put("lat", 0.0);
                }

            } catch (Exception ex) {
                Log.d("Connector:SendHttpPostTask / Exception", ex.toString());
            }

            return lagAndLatInfo;
        }
    }

    private void WriteData(OutputStream out, int cellID, int lac)
            throws IOException
    {
        DataOutputStream dataOutputStream = new DataOutputStream(out);
        dataOutputStream.writeShort(21);
        dataOutputStream.writeLong(0);
        dataOutputStream.writeUTF("en");
        dataOutputStream.writeUTF("Android");
        dataOutputStream.writeUTF("1.0");
        dataOutputStream.writeUTF("Web");
        dataOutputStream.writeByte(27);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(3);
        dataOutputStream.writeUTF("");

        dataOutputStream.writeInt(cellID);
        dataOutputStream.writeInt(lac);

        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.flush();
    }
}
