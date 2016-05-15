package com.kavi.droid.notifyme.database;

/**
 * Created by Kavi on 11/11/14.
 * @author Kavimal Wijewardana <kavi707@gmail.com>
 */
public class ContactData {

    private int contactId;
    private String contactName;
    private String contactNumberData;

    public int getContactId() {
        return contactId;
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactNumberData() {
        return contactNumberData;
    }

    public void setContactNumberData(String contactNumberData) {
        this.contactNumberData = contactNumberData;
    }
}
