package com.android.minu.notifyme.database;

/**
 * Created by Kavi on 11/2/14.
 * @author Kavimal Wijewardana <kavi707@gmail.com>
 */
public class LocationData {

    private int locationId;
    private String locationName;
    private String locationDescription;
    private int cellId;
    private int lac;
    private double longitude;
    private double latitude;
    private int secondaryCell;
    private int secondaryLac;

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationDescription() {
        return locationDescription;
    }

    public void setLocationDescription(String locationDescription) {
        this.locationDescription = locationDescription;
    }

    public int getCellId() {
        return cellId;
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
    }

    public int getLac() {
        return lac;
    }

    public void setLac(int lac) {
        this.lac = lac;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getSecondaryCell() {
        return secondaryCell;
    }

    public void setSecondaryCell(int secondaryCell) {
        this.secondaryCell = secondaryCell;
    }

    public int getSecondaryLac() {
        return secondaryLac;
    }

    public void setSecondaryLac(int secondaryLac) {
        this.secondaryLac = secondaryLac;
    }
}
