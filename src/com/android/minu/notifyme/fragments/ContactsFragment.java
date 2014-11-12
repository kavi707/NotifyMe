package com.android.minu.notifyme.fragments;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.minu.notifyme.R;
import com.android.minu.notifyme.activities.SelectContactActivity;
import com.android.minu.notifyme.adapters.ContactItemAdapter;
import com.android.minu.notifyme.adapters.LocationItemAdapter;
import com.android.minu.notifyme.database.ContactData;
import com.android.minu.notifyme.database.LocalDatabaseSQLiteOpenHelper;
import com.android.minu.notifyme.database.LocationData;

import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment {

    private View contactsFragmentView;

    private Button addNewContactsViewButton;
    private ListView selectedContactsListView;

    private Context context;
    private ContactItemAdapter contactItemAdapter;
    private LocalDatabaseSQLiteOpenHelper localDatabaseSQLiteOpenHelper;

    private static List<ContactData> contactDataList = new ArrayList<ContactData>();

	public ContactsFragment(Context context){
        this.context = context;
        localDatabaseSQLiteOpenHelper = new LocalDatabaseSQLiteOpenHelper(context);
        new  LoadContactsFromDeviceTask().execute();
    }

	public ContactsFragment(){}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        contactsFragmentView = inflater.inflate(R.layout.fragment_contacts, container, false);
        setUpViews();
         
        return contactsFragmentView;
    }

    private void setUpViews() {

        addNewContactsViewButton = (Button) contactsFragmentView.findViewById(R.id.addNewContactsViewButton);
        selectedContactsListView = (ListView) contactsFragmentView.findViewById(R.id.selectedContactsListView);

        addNewContactsViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectContactIntent = new Intent(getActivity(), SelectContactActivity.class);
                startActivity(selectContactIntent);
            }
        });

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

            ContentResolver contentResolver = getActivity().getContentResolver();

            Cursor cursor = contentResolver.query(CONTENT_URI, null,null, null, null);

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                ContactData contactData;
                while (cursor.moveToNext()) {
                    contactData = new ContactData();
                    String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));

                    String name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));
                    contactData.setContactName(name);

                    String phoneNumber = "";
                    String finalPhoneString = "";
                    int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( HAS_PHONE_NUMBER )));

                    if (hasPhoneNumber > 0) {
                        Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);
                        while (phoneCursor.moveToNext()) {
                            phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                            if (finalPhoneString.equals("")) {
                                finalPhoneString = finalPhoneString + phoneNumber;
                            } else {
                                finalPhoneString = finalPhoneString + "," + phoneNumber;
                            }
                        }
                        contactData.setContactNumberData(finalPhoneString);
                        phoneCursor.close();
                    }

                    contactDataList.add(contactData);
                }

            }
            cursor.close();
            return null;
        }
    }

    public static List<ContactData> getContactDataList() {
        return contactDataList;
    }
}
