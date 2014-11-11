package com.android.minu.notifyme.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.android.minu.notifyme.R;
import com.android.minu.notifyme.activities.SelectContactActivity;
import com.android.minu.notifyme.adapters.ContactItemAdapter;
import com.android.minu.notifyme.adapters.LocationItemAdapter;
import com.android.minu.notifyme.database.ContactData;
import com.android.minu.notifyme.database.LocalDatabaseSQLiteOpenHelper;
import com.android.minu.notifyme.database.LocationData;

import java.util.List;

public class ContactsFragment extends Fragment {

    private View contactsFragmentView;

    private Button addNewContactsViewButton;
    private ListView selectedContactsListView;

    private Context context;
    private ContactItemAdapter contactItemAdapter;
    private LocalDatabaseSQLiteOpenHelper localDatabaseSQLiteOpenHelper;

	public ContactsFragment(Context context){
        this.context = context;
        localDatabaseSQLiteOpenHelper = new LocalDatabaseSQLiteOpenHelper(context);
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
}
