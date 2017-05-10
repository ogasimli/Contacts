package ogasimli.org.contacts.helper;

/**
 * Created by Orkhan Gasimli on 10.05.2017.
 */

public class Constants {

    //Activity state keys
    public static final int CONTACTS_VIEW_STATE_RESULTS = 0;
    public static final int CONTACTS_VIEW_STATE_NO_RESULTS = 1;
    public static final int CONTACTS_VIEW_STATE_ERROR = 2;
    public static final int FAVOURITES_VIEW_STATE_RESULTS = 0;
    public static final int FAVOURITES_VIEW_STATE_NO_RESULTS = 1;

    //Bundle keys
    public static final String CONTACTS_LIST_STATE_KEY = "CONTACTS_LIST_STATE_KEY";
    public static final String CONTACTS_VIEW_STATE_KEY = "CONTACTS_VIEW_STATE_KEY";
    public static final String FAVOURITES_LIST_STATE_KEY = "FAVOURITES_LIST_STATE_KEY";
    public static final String FAVOURITES_VIEW_STATE_KEY = "FAVOURITES_VIEW_STATE_KEY";

    //Database constants
    public static final String DB_NAME = "favourites_db";
    public static final int DB_VERSION = 1;
    public static final String DB_AUTHORITY = "content://org.ogasimli.contacts/";
    public static final int FAVOURITES_LOADER_ID = 0;

    /** Duration of splash screen **/
    public static final int SPLASH_DISPLAY_LENGTH = 1000;
}
