package ogasimli.org.contacts.loader;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import ogasimli.org.contacts.object.Contact;
import ogasimli.org.contacts.object.PhoneNumber;
import ogasimli.org.contacts.provigen.ContactContract;

/**
 * Async task loader to save favourite contracts
 *
 * Created by Orkhan Gasimli on 10.05.2017.
 */

public class ContactSaver extends AsyncTask<Void, Void, Void> {

    private final Context mContext;

    private final Contact mContact;

    public ContactSaver(Context context, Contact contact) {
        mContext = context;
        mContact = contact;
    }

    @Override
    protected final Void doInBackground(Void... voids) {
        if (mContext != null) {
            //Get ContentResolver
            Uri contentUri = ContactContract.CONTENT_URI;
            ContentResolver contentResolver = mContext.getContentResolver();
            //First delete old data
            contentResolver.delete(contentUri,
                    ContactContract.CONTACT_ID + " = ? ",
                    new String[]{mContact.getId()});

            //Insert new values
            ContentValues newValues = new ContentValues();
            PhoneNumber phoneNumber = mContact.getPhone();
            newValues.put(ContactContract.CONTACT_ID, mContact.getId());
            newValues.put(ContactContract.NAME, mContact.getName());
            newValues.put(ContactContract.MOBILE, phoneNumber.getMobile());
            newValues.put(ContactContract.HOME, phoneNumber.getHome());
            newValues.put(ContactContract.OFFICE, phoneNumber.getOffice());
            newValues.put(ContactContract.EMAIL, mContact.getEmail());
            newValues.put(ContactContract.ADDRESS, mContact.getAddress());
            newValues.put(ContactContract.GENDER, mContact.getGender());
            contentResolver.insert(contentUri, newValues);
        }
        return null;
    }
}
