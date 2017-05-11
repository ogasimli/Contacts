package ogasimli.org.contacts.ui.fragment;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ogasimli.org.contacts.R;
import ogasimli.org.contacts.helper.Constants;
import ogasimli.org.contacts.loader.ContactLoader;
import ogasimli.org.contacts.loader.ContactRemover;
import ogasimli.org.contacts.object.Contact;
import ogasimli.org.contacts.ui.adapter.ContactListAdapter;

/**
 * FavouritesFragment class
 *
 * Created by Orkhan Gasimli on 10.05.2017.
 */
public class FavouritesFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<ArrayList<Contact>> {

    private final String LOG_TAG = FavouritesFragment.class.getSimpleName();

    private ContactListAdapter mFavouriteListAdapter;

    private ArrayList<Contact> mFavouriteList;

    private Unbinder mUnbinder;

    private String mMobileNumber;

    @BindView(R.id.favourite_fragment_relative_layout)
    RelativeLayout mRelativeLayout;

    @BindView(R.id.recyclerview_favourite)
    RecyclerView mRecyclerView;
    @BindView(R.id.no_favourite_view)
    View mNoContactsView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favourites, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        //Instantiate RecyclerView adapter
        mFavouriteListAdapter = new ContactListAdapter(LOG_TAG);

        //Instantiate RecyclerView
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mFavouriteListAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mFavouriteListAdapter.setOnItemClickListener(itemClickListener);

        /* loadData if savedInstanceState is null, load from already fetched data if
         * savedInstanceSate is not null */
        if (savedInstanceState == null || !savedInstanceState.containsKey(Constants.FAVOURITES_LIST_STATE_KEY)
                || !savedInstanceState.containsKey(Constants.FAVOURITES_VIEW_STATE_KEY)) {
            loadData();
        } else {
            restoreInstanceState(savedInstanceState);
        }

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        int state;
        if (mNoContactsView.getVisibility() == View.VISIBLE) {
            state = Constants.FAVOURITES_VIEW_STATE_NO_RESULTS;
        } else {
            state = Constants.FAVOURITES_VIEW_STATE_RESULTS;
        }

        outState.putInt(Constants.FAVOURITES_VIEW_STATE_KEY, state);
        outState.putParcelableArrayList(Constants.FAVOURITES_LIST_STATE_KEY, mFavouriteList);
    }

    /**
     * Helper method to restore instance state
     */
    private void restoreInstanceState(Bundle savedInstanceState) {
        int state = savedInstanceState.getInt(Constants.FAVOURITES_VIEW_STATE_KEY,
                Constants.FAVOURITES_VIEW_STATE_NO_RESULTS);

        switch (state) {
            case Constants.FAVOURITES_VIEW_STATE_NO_RESULTS:
                showNoResultView();
                break;
            case Constants.FAVOURITES_VIEW_STATE_RESULTS:
                mFavouriteList = savedInstanceState.getParcelableArrayList(Constants.FAVOURITES_LIST_STATE_KEY);
                if (mFavouriteList != null && mFavouriteList.size() > 0) {
                    showResultView();
                } else {
                    loadData();
                }
                break;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            loadData();
        }
    }

    /**
     * Helper method to load contacts
     */
    private void loadData() {
        //Load corresponding entries from DB
        if (getActivity() != null) {
            getActivity().getSupportLoaderManager()
                    .restartLoader(Constants.FAVOURITES_LOADER_ID, null, this);
        }
    }

    /**
     * Helper method to construct views if data is loaded
     */
    private void showResultView() {

        //View and hide relevant Views
        if (mRecyclerView != null) {
            mRecyclerView.setVisibility(View.VISIBLE);
        }
        if (mNoContactsView != null) {
            mNoContactsView.setVisibility(View.GONE);
        }

        //Set adapter to RecyclerView
        mFavouriteListAdapter.setContactList(mFavouriteList);
    }

    /**
     * Helper method to construct views if no data is loaded
     */
    private void showNoResultView() {

        //View and hide relevant Views
        if (mRecyclerView != null) {
            mRecyclerView.setVisibility(View.GONE);
        }
        if (mNoContactsView != null) {
            mNoContactsView.setVisibility(View.VISIBLE);
        }
    }

    /* Callbacks to query data from favourites table */
    @Override
    public Loader<ArrayList<Contact>> onCreateLoader(int id, Bundle args) {
        return new ContactLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Contact>> loader, ArrayList<Contact> data) {
        mFavouriteList = data;
        //If there is no corresponding entries in DB then make API call and write to DB
        if (mFavouriteList != null && mFavouriteList.size() > 0) {
            Log.d(LOG_TAG, "Loaded from DB");
            showResultView();
        } else {
            showNoResultView();
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Contact>> loader) {

    }

    /* ItemClickListener for contact list items */
    private final ContactListAdapter.ClickListener itemClickListener
            = new ContactListAdapter.ClickListener() {
        @Override
        public void onItemClick(final int position, View v) {
            switch (v.getId()) {
                case R.id.call_button:
                    mMobileNumber = mFavouriteList.get(position).getPhone().getMobile();
                    askForPermission();
                    break;
                case R.id.favourite_button:
                    new ContactRemover(getActivity(), mFavouriteList.get(position)).execute();
                    final Snackbar snackBar = Snackbar
                            .make(mRelativeLayout, R.string.removed_from_favourites_snackbar, Snackbar.LENGTH_LONG);
                    snackBar.setAction(android.R.string.ok, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snackBar.dismiss();
                                }
                            }).show();
                    loadData();
                    break;
            }
        }
    };

    public void favoriteChanged() {
        loadData();
    }

    public void askForPermission() {
        final String permission = Manifest.permission.CALL_PHONE;
        final int permissionCode = Constants.MY_PERMISSIONS_REQUEST_CALL;
        int hasWriteContactsPermission = ContextCompat.checkSelfPermission(getActivity(), permission);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(permission)) {
                showRationaleDialog(getString(R.string.permission_rationale_message),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(new String[]{permission}, permissionCode);
                            }
                        });
                return;
            }
            requestPermissions(new String[]{permission}, permissionCode);
            return;
        }
        startCallIntent();
    }

    private void showRationaleDialog(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, okListener)
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }

    public void startCallIntent() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DIAL);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            getActivity().startActivity(new Intent(Intent.ACTION_DIAL,
                    Uri.fromParts("tel", mMobileNumber, null)));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.MY_PERMISSIONS_REQUEST_CALL:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Permission granted
                    startCallIntent();
                } else {
                    // Permission Denied
                    Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
