package ogasimli.org.contacts.ui.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ogasimli.org.contacts.R;
import ogasimli.org.contacts.helper.Constants;
import ogasimli.org.contacts.loader.ContactLoader;
import ogasimli.org.contacts.loader.ContactRemover;
import ogasimli.org.contacts.object.Contact;
import ogasimli.org.contacts.ui.adapter.FavouriteListAdapter;

/**
 * Created by Orkhan Gasimli on 10.05.2017.
 */
public class FavouritesFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<ArrayList<Contact>> {

    private final String LOG_TAG = FavouritesFragment.class.getSimpleName();

    private FavouriteListAdapter mFavouriteListAdapter;

    private ArrayList<Contact> mFavouriteList;

    private ProgressDialog mFavouriteProgressDialog;

    private Unbinder mUnbinder;

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
        mFavouriteListAdapter = new FavouriteListAdapter();

        //Instantiate RecyclerView
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mFavouriteListAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mFavouriteListAdapter.setOnItemClickListener(itemClickListener);

        /**
        * loadData if savedInstanceState is null, load from already fetched data
        * if savedInstanceSate is not null
        */
        if (savedInstanceState == null || !savedInstanceState.containsKey(Constants.FAVOURITES_LIST_STATE_KEY)
                || !savedInstanceState.containsKey(Constants.FAVOURITES_VIEW_STATE_KEY)) {
            loadData();
        } else {
            restoreInstanceState(savedInstanceState);
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mFavouriteProgressDialog != null && mFavouriteProgressDialog.isShowing()) {
            mFavouriteProgressDialog.dismiss();
        }

        mUnbinder.unbind();
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

    /**
     * Helper method to load contacts
     */
    public void loadData() {
        showProgressDialog(true);
        //Load corresponding entries from DB
        getActivity().getSupportLoaderManager()
                .restartLoader(Constants.FAVOURITES_LOADER_ID, null, this);
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

        //Hide ProgressDialog
        showProgressDialog(false);
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

        //Hide ProgressDialog
        showProgressDialog(false);
    }

    /*
    * Helper method to show and hide ProgressDialog
    */
    private void showProgressDialog(boolean show) {
        if (show && (mFavouriteProgressDialog == null || !mFavouriteProgressDialog.isShowing())) {
            mFavouriteProgressDialog = ProgressDialog.show(getActivity(),
                    getActivity().getString(R.string.progress_dialog_title),
                    getActivity().getString(R.string.progress_dialog_content), true, false);
        } else {
            if (mFavouriteProgressDialog != null && mFavouriteProgressDialog.isShowing()) {
                mFavouriteProgressDialog.dismiss();
            }
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
    private final FavouriteListAdapter.ClickListener itemClickListener
            = new FavouriteListAdapter.ClickListener() {
        @Override
        public void onItemClick(final int position, View v) {
            switch (v.getId()) {
                case R.id.call_button:
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_DIAL);
                    String mobileNumb = mFavouriteList.get(position).getPhone().getMobile();
                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                        getActivity().startActivity(new Intent(Intent.ACTION_DIAL,
                                Uri.fromParts("tel", mobileNumb, null)));
                    }
                    break;
                case R.id.favourite_button:
                    new ContactRemover(getActivity(), mFavouriteList.get(position)).execute();
                    final Snackbar snackBar = Snackbar
                            .make(mRelativeLayout, R.string.removed_from_favourites, Snackbar.LENGTH_LONG);
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
}
