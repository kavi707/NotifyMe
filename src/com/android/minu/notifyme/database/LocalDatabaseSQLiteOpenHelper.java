package com.android.minu.notifyme.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by kavi707 on 11/1/14.
 */
public class LocalDatabaseSQLiteOpenHelper extends SQLiteOpenHelper {

    private SQLiteDatabase locationNotifierDb;

    public static final String DB_NAME = "local_notifier_me.sqlite";
    public static final int VERSION = 1;

    public static final String LOCATIONS_TABLE_NAME = "locations";
    public static final String LOCATION_ID = "location_id";
    public static final String CELL_ID = "cell_id";
    public static final String LAC = "lac";
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String SECONDARY_CELL = "secondary_cell";
    public static final String SECONDARY_LAC = "secondary_lac";

    private Context dbContext;

    public LocalDatabaseSQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
        this.dbContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createLocationsTable(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        //This is for application updated with database update.
    }

    private void createLocationsTable(SQLiteDatabase sqLiteDatabase) {
        String createTableQuery = "create table " + LOCATIONS_TABLE_NAME + " (" +
                LOCATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT not null, " +
                CELL_ID + " int, " +
                LAC + " int, " +
                LONGITUDE + " real, " +
                LATITUDE + " real, " +
                SECONDARY_CELL + " int, " +
                SECONDARY_LAC + " int " +
                ");";
        sqLiteDatabase.execSQL(createTableQuery);
    }

    public void saveNewLocationData(LocationData location) {
        locationNotifierDb = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(CELL_ID, location.getCellId());
        values.put(LAC, location.getLac());
        values.put(LONGITUDE, location.getLongitude());
        values.put(LATITUDE, location.getLatitude());
        values.put(SECONDARY_CELL, location.getSecondaryCell());
        values.put(SECONDARY_LAC, location.getSecondaryLac());

        try {
            locationNotifierDb.insert(LOCATIONS_TABLE_NAME, null, values);
        } catch (SQLiteException ex) {
            throw ex;
        }
    }

    public LocationData getLocationFromCellAndLac(int cellId, int lac) {

        locationNotifierDb = this.getWritableDatabase();
        LocationData getLocation = null;

        try {
            String queryString = "SELECT * FROM " + LOCATIONS_TABLE_NAME +
                    " WHERE " + CELL_ID + " = " + cellId + " and " + LAC + " = " + lac;
            Cursor locationCursor = locationNotifierDb.rawQuery(queryString, null);

            locationCursor.moveToFirst();

            if (!locationCursor.isAfterLast()) {
                do {
                    getLocation = new LocationData();
                    getLocation.setLocationId(locationCursor.getInt(0));
                    getLocation.setCellId(locationCursor.getInt(1));
                    getLocation.setLac(locationCursor.getInt(2));
                    getLocation.setLongitude(locationCursor.getDouble(3));
                    getLocation.setLatitude(locationCursor.getDouble(4));
                    getLocation.setSecondaryCell(locationCursor.getInt(5));
                    getLocation.setSecondaryLac(locationCursor.getInt(6));
                } while (locationCursor.moveToNext());
            }
            locationCursor.close();
        } catch (SQLiteException ex) {
            throw ex;
        }

        return getLocation;
    }
}