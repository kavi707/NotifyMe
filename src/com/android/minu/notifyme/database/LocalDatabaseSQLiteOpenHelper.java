package com.android.minu.notifyme.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by minuri on 11/1/14.
 * @author Minuri Rajapaksha <rpmchathu@gmail.com>
 */
public class LocalDatabaseSQLiteOpenHelper extends SQLiteOpenHelper {

    private SQLiteDatabase locationNotifierDb;

    public static final String DB_NAME = "local_notifier_me.sqlite";
    public static final int VERSION = 1;

    public static final String LOCATIONS_TABLE_NAME = "locations";
    public static final String LOCATION_ID = "location_id";
    public static final String LOCATION_NAME = "location_name";
    public static final String LOCATION_DESCRIPTION = "location_description";
    public static final String CELL_ID = "cell_id";
    public static final String LAC = "lac";
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String SECONDARY_CELL = "secondary_cell";
    public static final String SECONDARY_LAC = "secondary_lac";

    public static final String CONTACTS_TABLE_NAME = "contacts";
    public static final String CONTACT_ID = "contact_id";
    public static final String CONTACT_NAME = "contact_name";
    public static final String CONTACT_NUMBER = "contact_number";

    private Context dbContext;

    public LocalDatabaseSQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
        this.dbContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createLocationsTable(sqLiteDatabase);
        createContactTable(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        //This is for application updated with database update.
    }

    private void createLocationsTable(SQLiteDatabase sqLiteDatabase) {
        String createTableQuery = "create table " + LOCATIONS_TABLE_NAME + " (" +
                LOCATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT not null, " +
                LOCATION_NAME + " text, " +
                LOCATION_DESCRIPTION + " text, " +
                CELL_ID + " int, " +
                LAC + " int, " +
                LONGITUDE + " real, " +
                LATITUDE + " real, " +
                SECONDARY_CELL + " int, " +
                SECONDARY_LAC + " int " +
                ");";
        sqLiteDatabase.execSQL(createTableQuery);
    }

    private void createContactTable(SQLiteDatabase sqLiteDatabase) {
        String createTableQuery = "create table " + CONTACTS_TABLE_NAME + " (" +
                CONTACT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT not null, " +
                CONTACT_NAME + " text, " +
                CONTACT_NUMBER + " text " +
                ");";
        sqLiteDatabase.execSQL(createTableQuery);
    }

    /************************/
    /*Location Table methods*/
    /************************/

    public void saveNewLocationData(LocationData location) {
        locationNotifierDb = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(LOCATION_NAME, location.getLocationName());
        values.put(LOCATION_DESCRIPTION, location.getLocationDescription());
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

    public List<LocationData> getAllLocations() {

        List<LocationData> locations = new ArrayList<LocationData>();
        locationNotifierDb = this.getWritableDatabase();
        LocationData getLocation = null;

        try {
            String queryString = "SELECT * FROM " + LOCATIONS_TABLE_NAME;
            Cursor locationCursor = locationNotifierDb.rawQuery(queryString, null);

            locationCursor.moveToFirst();

            if (!locationCursor.isAfterLast()) {
                do {
                    getLocation = new LocationData();
                    getLocation.setLocationId(locationCursor.getInt(0));
                    getLocation.setLocationName(locationCursor.getString(1));
                    getLocation.setLocationDescription(locationCursor.getString(2));
                    getLocation.setCellId(locationCursor.getInt(3));
                    getLocation.setLac(locationCursor.getInt(4));
                    getLocation.setLongitude(locationCursor.getDouble(5));
                    getLocation.setLatitude(locationCursor.getDouble(6));
                    getLocation.setSecondaryCell(locationCursor.getInt(7));
                    getLocation.setSecondaryLac(locationCursor.getInt(8));

                    locations.add(getLocation);

                } while (locationCursor.moveToNext());
            }
            locationCursor.close();
        } catch (SQLiteException ex) {
            throw ex;
        }

        return locations;
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
                    getLocation.setLocationName(locationCursor.getString(1));
                    getLocation.setLocationDescription(locationCursor.getString(2));
                    getLocation.setCellId(locationCursor.getInt(3));
                    getLocation.setLac(locationCursor.getInt(4));
                    getLocation.setLongitude(locationCursor.getDouble(5));
                    getLocation.setLatitude(locationCursor.getDouble(6));
                    getLocation.setSecondaryCell(locationCursor.getInt(7));
                    getLocation.setSecondaryLac(locationCursor.getInt(8));
                } while (locationCursor.moveToNext());
            }
            locationCursor.close();
        } catch (SQLiteException ex) {
            throw ex;
        }

        return getLocation;
    }

    public long deleteLocationFromLocationId(int locationId) {

        long deleteStatus = 0;
        locationNotifierDb = this.getWritableDatabase();

        try {
            String deleteQry = LOCATION_ID + " = " + locationId;
            deleteStatus = locationNotifierDb.delete(LOCATIONS_TABLE_NAME, deleteQry, null);
        } catch (SQLiteException ex) {
            throw ex;
        }

        return deleteStatus;
    }

    /***********************/
    /*Contacts table method*/
    /***********************/

    public void saveNewContact(ContactData contactData) {
        locationNotifierDb = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(CONTACT_NAME, contactData.getContactName());
        values.put(CONTACT_NUMBER, contactData.getContactNumberData());

        try {
            locationNotifierDb.insert(CONTACTS_TABLE_NAME, null, values);
        } catch (SQLiteException ex) {
            throw ex;
        }
    }

    public List<ContactData> getAllContacts () {
        List<ContactData> contacts = new ArrayList<ContactData>();
        locationNotifierDb = this.getWritableDatabase();
        ContactData getContact = null;

        try {
            String queryString = "SELECT * FROM " + CONTACTS_TABLE_NAME;
            Cursor contactCursor = locationNotifierDb.rawQuery(queryString, null);

            contactCursor.moveToFirst();

            if (!contactCursor.isAfterLast()) {
                do {
                    getContact = new ContactData();
                    getContact.setContactId(contactCursor.getInt(0));
                    getContact.setContactName(contactCursor.getString(1));
                    getContact.setContactNumberData(contactCursor.getString(2));

                    contacts.add(getContact);

                } while (contactCursor.moveToNext());
            }
            contactCursor.close();
        } catch (SQLiteException ex) {
            throw ex;
        }

        return contacts;
    }

    public long deleteContactFromContactId(int contactId) {

        long deleteStatus = 0;
        locationNotifierDb = this.getWritableDatabase();

        try {
            String deleteQry = CONTACT_ID + " = " + contactId;
            deleteStatus = locationNotifierDb.delete(CONTACTS_TABLE_NAME, deleteQry, null);
        } catch (SQLiteException ex) {
            throw ex;
        }

        return deleteStatus;
    }
}
