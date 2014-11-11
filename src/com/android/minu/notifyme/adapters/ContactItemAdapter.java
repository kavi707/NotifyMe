package com.android.minu.notifyme.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.android.minu.notifyme.R;
import com.android.minu.notifyme.database.ContactData;
import com.android.minu.notifyme.views.ContactListItem;

import java.util.List;

/**
 * Created by kwijewardana on 11/10/14.
 */
public class ContactItemAdapter extends BaseAdapter {

    private List<ContactData> contactDataList;
    private Context context;

    public ContactItemAdapter(List<ContactData> contactDataList, Context context) {
        this.contactDataList = contactDataList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return contactDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return (contactDataList == null)? null: contactDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ContactListItem contactListItem;
        if (convertView == null){
            contactListItem = (ContactListItem) View.inflate(context, R.layout.contact_list_item, null);
        } else {
            contactListItem = (ContactListItem) convertView;
        }

        contactListItem.setContactData(contactDataList.get(position));
        return contactListItem;
    }
}
