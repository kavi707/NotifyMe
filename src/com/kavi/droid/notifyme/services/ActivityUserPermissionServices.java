package com.kavi.droid.notifyme.services;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Kavi on 11/1/14.
 * @author Kavimal Wijewardana <kavi707@gmail.com>
 */
public class ActivityUserPermissionServices {

    /**
     * check the internet connection in the device for running application
     * @param context
     * @return boolean
     */
    public boolean isOnline(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            return true;
        }

        return false;
    }

    /**
     * check the GPS availability of the device
     * @param context
     * @return
     */
    public boolean isGPSOn(Context context) {

        boolean statusOfGPS = false;

        LocationManager manager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE );
        statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        return statusOfGPS;
    }

    /**
     * Return device user email.
     * @param context
     * @return
     */
    public String getUserEmail(Context context) {
        String userEmail = "";

        AccountManager accountManager = AccountManager.get(context);
        Account account = getAccount(accountManager);

        if (account == null) {
            userEmail = "default_email@gmail.com";
        } else {
            userEmail = account.name;
        }

        return userEmail;
    }

    /**
     * helper method to get registered account
     * @param accountManager
     * @return
     */
    private static Account getAccount(AccountManager accountManager) {
        Account[] accounts = accountManager.getAccountsByType("com.google");
        Account account;
        if (accounts.length > 0) {
            account = accounts[0];
        } else {
            account = null;
        }
        return account;
    }

}
