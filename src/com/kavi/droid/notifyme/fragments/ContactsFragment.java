package com.kavi.droid.notifyme.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.kavi.droid.notifyme.activities.SelectContactActivity;
import com.kavi.droid.notifyme.adapters.ContactItemAdapter;
import com.kavi.droid.notifyme.database.ContactData;
import com.kavi.droid.notifyme.database.LocalDatabaseSQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Kavi on 11/5/14.
 * @author Kavimal Wijewardana <kavi707@gmail.com>
 */
public class ContactsFragment extends Fragment {

    private View contactsFragmentView;

    private Button addNewContactsViewButton;
    private ListView selectedContactsListView;
    private ImageView contactsImageView;
    private ProgressDialog progress;

    private Context context;
    private ContactItemAdapter contactItemAdapter;
    private LocalDatabaseSQLiteOpenHelper localDatabaseSQLiteOpenHelper;

    private AlertDialog messageBalloonAlertDialog;

    private static List<ContactData> contactDataList = new ArrayList<ContactData>();

	public ContactsFragment(){}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        this.context = getActivity();
        localDatabaseSQLiteOpenHelper = new LocalDatabaseSQLiteOpenHelper(context);
        new  LoadContactsFromDeviceTask().execute();

        contactsFragmentView = inflater.inflate(com.kavi.droid.notifyme.R.layout.fragment_contacts, container, false);
        setUpViews();
         
        return contactsFragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();

        loadContactsToListView();
    }

    private void setUpViews() {

        addNewContactsViewButton = (Button) contactsFragmentView.findViewById(com.kavi.droid.notifyme.R.id.addNewContactsViewButton);
        selectedContactsListView = (ListView) contactsFragmentView.findViewById(com.kavi.droid.notifyme.R.id.selectedContactsListView);
        contactsImageView = (ImageView) contactsFragmentView.findViewById(com.kavi.droid.notifyme.R.id.contactsImageView);

        addNewContactsViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent selectContactIntent = new Intent(getActivity(), SelectContactActivity.class);
                startActivity(selectContactIntent);
            }
        });

        contactsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadContactsToListView();
            }
        });

        selectedContactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ContactData selectedContactData = (ContactData) selectedContactsListView.getItemAtPosition(position);

                messageBalloonAlertDialog = new AlertDialog.Builder(context)
                        .setTitle("Saved Location")
                        .setMessage("Name: " + selectedContactData.getContactName() + "\n" +
                                "Number: " + selectedContactData.getContactNumberData() + "\n")
                        .setPositiveButton("Delete", new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                localDatabaseSQLiteOpenHelper.deleteContactFromContactId(selectedContactData.getContactId());
                                loadContactsToListView();
                            }
                        })
                        .setNeutralButton("Cancel", new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                messageBalloonAlertDialog.cancel();
                            }
                        }).create();
                messageBalloonAlertDialog.show();
            }
        });
    }

    private void loadContactsToListView() {
        List<ContactData> contactDataList = localDatabaseSQLiteOpenHelper.getAllContacts();
        if (contactDataList.size() != 0) {
            contactItemAdapter = new ContactItemAdapter(contactDataList, context);
            selectedContactsListView.setAdapter(contactItemAdapter);
        }
    }

    private class LoadContactsFromDeviceTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {

            Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
            String _ID = ContactsContract.Contacts._ID;
            String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
            String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

            Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
            String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

            ContentResolver contentResolver = context.getContentResolver();

            Cursor cursor = contentResolver.query(CONTENT_URI, null,null, null, null);

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                ContactData contactData;
                while (cursor.moveToNext()) {
                    String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
                    String name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));
                    String phoneNumber = "";
                    int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( HAS_PHONE_NUMBER )));

                    if (hasPhoneNumber > 0) {
                        Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);
                        while (phoneCursor.moveToNext()) {
                            contactData = new ContactData();
                            phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                            contactData.setContactName(name);
                            contactData.setContactNumberData(phoneNumber);
                            contactDataList.add(contactData);
                        }
                        phoneCursor.close();
                    }
                }

            }

            Collections.sort(contactDataList, new Comparator<ContactData>() {
                @Override
                public int compare(ContactData lhs, ContactData rhs) {
                    return  lhs.getContactName().compareTo(rhs.getContactName());
                }
            });

            cursor.close();
            return null;
        }
    }

    public static List<ContactData> getContactDataList() {
        return contactDataList;
    }
}
