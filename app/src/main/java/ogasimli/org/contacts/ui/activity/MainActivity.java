package ogasimli.org.contacts.ui.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import ogasimli.org.contacts.R;
import ogasimli.org.contacts.ui.adapter.ContactsFragmentPagerAdapter;
import ogasimli.org.contacts.ui.fragment.ContactsFragment;
import ogasimli.org.contacts.ui.fragment.FavouritesFragment;

/**
 * MainActivity class
 *
 * Created by Orkhan Gasimli on 10.05.2017.
 */

public class MainActivity extends AppCompatActivity
        implements ContactsFragment.ContactActionListener{

    private FavouritesFragment mFavouritesFragment;

    @BindView(R.id.toolbar)
    Toolbar mToolbarView;

    @BindView(R.id.tabs)
    TabLayout mTabs;

    @BindView(R.id.pager)
    ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Set toolbar
        setSupportActionBar(mToolbarView);
        setupViewPager(mPager);
        mTabs.setupWithViewPager(mPager);
        setupTabIcons();
    }

    private void setupViewPager(ViewPager viewPager) {
        ContactsFragmentPagerAdapter adapter = new ContactsFragmentPagerAdapter(getSupportFragmentManager());
        mFavouritesFragment = new FavouritesFragment();
        adapter.addFrag(new ContactsFragment());
        adapter.addFrag(mFavouritesFragment);
        viewPager.setOffscreenPageLimit(0);
        viewPager.setAdapter(adapter);
    }

    @SuppressWarnings("ConstantConditions")
    private void setupTabIcons() {
        mTabs.getTabAt(0).setIcon(R.drawable.ic_people_filled);
        mTabs.getTabAt(1).setIcon(R.drawable.ic_star_filled);
    }

    @Override
    public void onFavoriteChanged() {
        mFavouritesFragment.favoriteChanged();
    }
}
