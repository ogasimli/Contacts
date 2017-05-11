package ogasimli.org.contacts.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * FragmentPagerAdapter class
 *
 * Created by Orkhan Gasimli on 10.05.2017.
 */

public class ContactsFragmentPagerAdapter extends FragmentPagerAdapter {

    //List of fragments
    private final List<Fragment> mFragmentList = new ArrayList<>();

    public ContactsFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // return null to display only the icon
        return null;
    }

    public void addFrag(Fragment fragment) {
        mFragmentList.add(fragment);
    }
}
