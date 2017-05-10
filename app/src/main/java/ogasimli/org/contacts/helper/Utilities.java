package ogasimli.org.contacts.helper;

import ogasimli.org.contacts.R;
import ogasimli.org.contacts.ui.fragment.FavouritesFragment;

/**
 * Created by Orkhan Gasimli on 10.05.2017.
 */

public class Utilities {

    /* Helper method to set appropriate avatar */
    public static int setAvatar(String avatar) {
        return avatar.equals("male") ? R.drawable.ic_face_male : R.drawable.ic_face_female;
    }

    public static int setFavouriteImage(String fragmentName) {
        return fragmentName.equals(FavouritesFragment.class.getSimpleName())
                ? R.drawable.ic_star_filled_green : R.drawable.ic_star_empty_green;
    }
}
