package com.android.minu.notifyme.activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.minu.notifyme.R;
import com.android.minu.notifyme.adapters.ContactItemAdapter;
import com.android.minu.notifyme.database.ContactData;
import com.android.minu.notifyme.database.LocalDatabaseSQLiteOpenHelper;
import com.android.minu.notifyme.fragments.ContactsFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kwijewardana on 11/11/14.
 */
public class SelectContactActivity extends Activity {

    private ListView allContactListView;
    private ProgressBar loadingProgressBar;

    private Context context = this;
    private ContactItemAdapter contactItemAdapter;

    private List<ContactData> contactDataList = new ArrayList<ContactData>();
    private LocalDatabaseSQLiteOpenHelper localDatabaseSQLiteOpenHelper = new LocalDatabaseSQLiteOpenHelper(context);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contact);

        setUpViews();
    }

    @Override
    protected void onStart() {
        super.onStart();

        contactDataList = ContactsFragment.getContactDataList();
        if (contactDataList.size() != 0) {
            loadingProgressBar.setVisibility(View.INVISIBLE);
            contactItemAdapter = new ContactItemAdapter(contactDataList, context);
            allContactListView.setAdapter(contactItemAdapter);
        } else {
            Toast.makeText(context, "No contact found in the device", Toast.LENGTH_LONG).show();
        }
    }

    private void setUpViews() {
        allContactListView = (ListView) findViewById(R.id.allContactListView);
        loadingProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);
        loadingProgressBar.setVisibility(View.VISIBLE);

        allContactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setSelected(true);
                ContactData selectedContact = (ContactData) allContactListView.getItemAtPosition(position);
                if (selectedContact != null) {
                    String contactNo = selectedContact.getContactNumberData();
                    if (!(contactNo.equals("") || contactNo.equals(null))) {
                        String[] splitArray = contactNo.split(",");
                        selectedContact.setContactNumberData(splitArray[0]);

                        localDatabaseSQLiteOpenHelper.saveNewContact(selectedContact);

                        Toast.makeText(context, "Selected contact [" + selectedContact.getContactName() +
                                "] added to application successfully", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, "Selected contact [" + selectedContact.getContactName() +
                                "] couldn't added to application", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
}
