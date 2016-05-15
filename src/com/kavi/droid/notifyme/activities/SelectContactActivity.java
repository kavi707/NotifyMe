package com.kavi.droid.notifyme.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kavi.droid.notifyme.adapters.ContactItemAdapter;
import com.kavi.droid.notifyme.database.ContactData;
import com.kavi.droid.notifyme.database.LocalDatabaseSQLiteOpenHelper;
import com.kavi.droid.notifyme.fragments.ContactsFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kavi on 11/11/14.
 * @author Kavimal Wijewardana <kavi707@gmail.com>
 */
public class SelectContactActivity extends Activity {

    private ListView allContactListView;
    private ProgressBar loadingProgressBar;
    private TextView loadingMessageTextView;

    protected static final int TIMER_RUNTIME = 10000;
    private Context context = this;
    private boolean mbActive;
    private ContactItemAdapter contactItemAdapter;

    private List<ContactData> contactDataList = new ArrayList<ContactData>();
    private LocalDatabaseSQLiteOpenHelper localDatabaseSQLiteOpenHelper = new LocalDatabaseSQLiteOpenHelper(context);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.kavi.droid.notifyme.R.layout.activity_select_contact);

        setUpViews();
    }

    private void setUpViews() {

        // Initialise UI object from activity_select_contact.xml
        allContactListView = (ListView) findViewById(com.kavi.droid.notifyme.R.id.allContactListView);
        loadingProgressBar = (ProgressBar) findViewById(com.kavi.droid.notifyme.R.id.loadingProgressBar);
        loadingMessageTextView = (TextView) findViewById(com.kavi.droid.notifyme.R.id.loadingMessageTextView);

        // Separate thread to view progress dialog.
        // Delay the view till load contacts from device
        final Thread loadingThread = new Thread() {
            @Override
            public void run() {
                super.run();
                mbActive = true;
                int progress = 0;

                try {
                    int waited = 0;
                    while (mbActive && (waited < TIMER_RUNTIME)) {
                        sleep(200);

                        if (mbActive) {
                            waited += 200;
                            progress = updateProgress(waited);

                            switch (progress) {
                                case 50:sleep(100);
                                    break;
                                case 100:onContinue();
                                    break;
                            }
                            loadingProgressBar.setProgress(progress);
                        }
                    }
                } catch (InterruptedException ex) {
                    //do nothing
                }
            }
        };
        loadingThread.start();

        // Contact ListView click event
        // Select contact from device and saved to application
        allContactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setSelected(true);
                ContactData selectedContact = (ContactData) allContactListView.getItemAtPosition(position);
                if (selectedContact != null) {
                    String contactNo = selectedContact.getContactNumberData();
                    if (!(contactNo.equals("") || contactNo.equals(null))) {
//                        String[] splitArray = contactNo.split(",");
//                        selectedContact.setContactNumberData(splitArray[0]);
                        selectedContact.setContactNumberData(contactNo);

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

    /**
     * This method for set progress value to progress bar against the given TIMER_RUNTIME
     * @param timePassed
     * @return Integer progress
     */
    private int updateProgress(final int timePassed) {
        if (null != loadingProgressBar) {
            // Ignore rounding error here
            final int progress = loadingProgressBar.getMax() * timePassed / TIMER_RUNTIME;
            return progress;
        }
        return 0;
    }

    /**
     * Process @ after complete the loading
     * Open the next view intent
     */
    private void onContinue() {
        contactDataList = ContactsFragment.getContactDataList();
        if (contactDataList.size() != 0) {
            contactItemAdapter = new ContactItemAdapter(contactDataList, context);
            allContactListView.post(new Runnable() {
                @Override
                public void run() {
                    allContactListView.setAdapter(contactItemAdapter);
                }
            });
            loadingProgressBar.post(new Runnable() {
                @Override
                public void run() {
                    loadingProgressBar.setVisibility(View.INVISIBLE);
                }
            });
            loadingMessageTextView.post(new Runnable() {
                @Override
                public void run() {
                    loadingMessageTextView.setVisibility(View.INVISIBLE);
                }
            });
        } else {
            Toast.makeText(context, "No contact found in the device", Toast.LENGTH_LONG).show();
        }
    }
}
