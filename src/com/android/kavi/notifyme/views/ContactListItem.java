package com.android.kavi.notifyme.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.kavi.notifyme.R;
import com.android.kavi.notifyme.database.ContactData;

/**
 * Created by Kavi on 11/11/14.
 * @author Kavimal Wijewardana <kavi707@gmail.com>
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
