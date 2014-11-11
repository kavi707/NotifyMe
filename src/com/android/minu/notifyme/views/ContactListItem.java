package com.android.minu.notifyme.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.minu.notifyme.R;
import com.android.minu.notifyme.database.ContactData;
import com.android.minu.notifyme.database.LocationData;

/**
 * Created by kwijewardana on 11/11/14.
 */
public class ContactListItem extends LinearLayout {

    private TextView contactNameTextView;
    private TextView contactNumberTextView;

    private ContactData contactData;

    public ContactListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        contactNameTextView = (TextView) findViewById(R.id.contactItemNameTextView);
        contactNumberTextView = (TextView) findViewById(R.id.contactItemDetailsTextView);
    }

    public ContactData getContactData() {
        return contactData;
    }

    public void setContactData(ContactData contactData) {
        this.contactData = contactData;

        contactNameTextView.setText(contactData.getContactName());
        contactNumberTextView.setText(contactData.getContactNumberData());
    }
}
