package ogasimli.org.contacts.loader;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import ogasimli.org.contacts.object.Contact;
import ogasimli.org.contacts.provigen.ContactContract;

/**
 * Async task loader to remove favourite contacts
 *
 * Created by Orkhan Gasimli on 10.05.2017.
 */

public class ContactRemover extends AsyncTask<Void, Void, Void> {

    private final Context mContext;

    private final Contact mContact;

    public ContactRemover(Context context, Contact contact) {
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
        }
        return null;
    }
}
