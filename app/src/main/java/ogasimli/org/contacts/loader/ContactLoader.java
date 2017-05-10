package ogasimli.org.contacts.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;

import ogasimli.org.contacts.object.Contact;
import ogasimli.org.contacts.object.PhoneNumber;
import ogasimli.org.contacts.provigen.ContactContract;

/**
 * Created by Orkhan Gasimli on 10.05.2017.
 */

public class ContactLoader extends AsyncTaskLoader<ArrayList<Contact>> {

    public ContactLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public ArrayList<Contact> loadInBackground() {
        ArrayList<Contact> favouriteList = new ArrayList<>();
        Uri uri = ContactContract.CONTENT_URI;
        Cursor cursor = getContext().getContentResolver().
                query(uri, null, null, null, "");
        if (null == cursor) {
            return null;
        } else if (cursor.getCount() < 1) {
            cursor.close();
            return new ArrayList<>();
        } else {
            int id = cursor.getColumnIndex(ContactContract.CONTACT_ID);
            int name = cursor.getColumnIndex(ContactContract.NAME);
            int mobile = cursor.getColumnIndex(ContactContract.MOBILE);
            int home = cursor.getColumnIndex(ContactContract.HOME);
            int office = cursor.getColumnIndex(ContactContract.OFFICE);
            int email = cursor.getColumnIndex(ContactContract.EMAIL);
            int address = cursor.getColumnIndex(ContactContract.ADDRESS);
            int gender = cursor.getColumnIndex(ContactContract.GENDER);
            while (cursor.moveToNext()) {
                Contact contact = new Contact();
                PhoneNumber phoneNumber = new PhoneNumber();
                contact.setId(cursor.getString(id));
                contact.setName(cursor.getString(name));
                phoneNumber.setMobile(cursor.getString(mobile));
                phoneNumber.setHome(cursor.getString(home));
                phoneNumber.setOffice(cursor.getString(office));
                contact.setPhone(phoneNumber);
                contact.setEmail(cursor.getString(email));
                contact.setAddress(cursor.getString(address));
                contact.setGender(cursor.getString(gender));
                favouriteList.add(contact);
            }
        }
        cursor.close();
        return favouriteList;
    }

    @Override
    public void deliverResult(ArrayList<Contact> data) {
        super.deliverResult(data);
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }
}
