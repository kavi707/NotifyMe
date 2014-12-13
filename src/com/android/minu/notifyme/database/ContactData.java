package com.android.minu.notifyme.database;

/**
 * Created by minuri on 11/11/14.
 * @author Minuri Rajapaksha <rpmchathu@gmail.com>
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
