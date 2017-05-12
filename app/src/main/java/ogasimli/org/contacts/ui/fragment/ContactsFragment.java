package ogasimli.org.contacts.ui.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ogasimli.org.contacts.R;
import ogasimli.org.contacts.api.ContactApiService;
import ogasimli.org.contacts.helper.Constants;
import ogasimli.org.contacts.loader.ContactRemover;
import ogasimli.org.contacts.loader.ContactSaver;
import ogasimli.org.contacts.object.Contact;
import ogasimli.org.contacts.object.ContactList;
import ogasimli.org.contacts.ui.adapter.ContactListAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ContactsFragment class
 *
 * Created by Orkhan Gasimli on 10.05.2017.
 */

public class ContactsFragment extends BaseFragment {

    private final String LOG_TAG = ContactsFragment.class.getSimpleName();

    private ArrayList<Contact> mContactList;

    private ProgressDialog mContactProgressDialog;

    @BindView(R.id.contact_fragment_relative_layout)
    RelativeLayout mRelativeLayout;

    @BindView(R.id.recyclerview_contacts)
    RecyclerView mRecyclerView;

    @BindView(R.id.no_contacts_view)
    View mNoContactsView;

    @BindView(R.id.contacts_error_view)
    View mContactsErrorView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fragmentName = LOG_TAG;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        //Instantiate RecyclerView adapter
        mContactListAdapter = new ContactListAdapter(fragmentName);
        mRecyclerView.setAdapter(mContactListAdapter);
        mContactListAdapter.setOnItemClickListener(itemClickListener);

        /*
        * loadData if savedInstanceState is null, load from already fetched data
        * if savedInstanceSate is not null
        */
        if (savedInstanceState == null || !savedInstanceState.containsKey(Constants.CONTACTS_LIST_STATE_KEY)
                || !savedInstanceState.containsKey(Constants.CONTACTS_VIEW_STATE_KEY)) {
            loadData();
        } else {
            restoreInstanceState(savedInstanceState);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        int state;
        if (mContactsErrorView.getVisibility() == View.VISIBLE) {
            state = Constants.CONTACTS_VIEW_STATE_ERROR;
        } else if (mNoContactsView.getVisibility() == View.VISIBLE) {
            state = Constants.CONTACTS_VIEW_STATE_NO_RESULTS;
        } else {
            state = Constants.CONTACTS_VIEW_STATE_RESULTS;
        }

        outState.putInt(Constants.CONTACTS_VIEW_STATE_KEY, state);
        outState.putParcelableArrayList(Constants.CONTACTS_LIST_STATE_KEY, mContactList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mContactProgressDialog != null && mContactProgressDialog.isShowing()) {
            mContactProgressDialog.dismiss();
        }
    }

    /**
     * Helper method to restore instance state
     */
    private void restoreInstanceState(Bundle savedInstanceState) {
        int state = savedInstanceState.getInt(Constants.CONTACTS_VIEW_STATE_KEY,
                Constants.CONTACTS_VIEW_STATE_ERROR);

        switch (state) {
            case Constants.CONTACTS_VIEW_STATE_NO_RESULTS:
                showNoResultView();
                break;
            case Constants.CONTACTS_VIEW_STATE_ERROR:
                showErrorView();
                break;
            case Constants.CONTACTS_VIEW_STATE_RESULTS:
                mContactList = savedInstanceState.getParcelableArrayList(Constants.CONTACTS_LIST_STATE_KEY);
                if (mContactList != null && mContactList.size() > 0) {
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
    private void loadData() {
        showProgressDialog(true);
        ContactApiService contactService = ContactApiService.retrofit
                .create(ContactApiService.class);
        final Call<ContactList> call = contactService.getContactList();
        call.enqueue(new Callback<ContactList>() {
            @Override
            public void onResponse(Call<ContactList> call, Response<ContactList> response) {
                mContactList = new ArrayList<>();
                mContactList = response.body().getContacts();
                if (mContactList != null && mContactList.size() > 0) {
                    Log.d(LOG_TAG, "Loaded from API");
                    Log.d(LOG_TAG, "userList size : " + mContactList.size());
                    showResultView();
                } else {
                    showNoResultView();
                }
            }

            @Override
            public void onFailure(Call<ContactList> call, Throwable t) {
                showErrorView();
                Log.d("Network Error", t.toString());
            }
        });
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
        if (mContactsErrorView != null) {
            mContactsErrorView.setVisibility(View.GONE);
        }

        //Set adapter to RecyclerView
        mContactListAdapter.setContactList(mContactList);

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
        if (mContactsErrorView != null) {
            mContactsErrorView.setVisibility(View.GONE);
        }

        //Hide ProgressDialog
        showProgressDialog(false);
    }

    /**
     * Helper method to show error notification if it is failed to load data
     */
    private void showErrorView() {

        //View and hide relevant Views
        if (mRecyclerView != null) {
            mRecyclerView.setVisibility(View.GONE);
        }
        if (mNoContactsView != null) {
            mNoContactsView.setVisibility(View.GONE);
        }
        if (mContactsErrorView != null) {
            mContactsErrorView.setVisibility(View.VISIBLE);
        }

        //Hide ProgressDialog
        showProgressDialog(false);
    }

    /*
    * Helper method to show and hide ProgressDialog
    */
    private void showProgressDialog(boolean show) {
        if (show && (mContactProgressDialog == null || !mContactProgressDialog.isShowing())) {
            mContactProgressDialog = ProgressDialog.show(getActivity(),
                    getActivity().getString(R.string.progress_dialog_title),
                    getActivity().getString(R.string.progress_dialog_content), true, false);
        } else {
            if (mContactProgressDialog != null && mContactProgressDialog.isShowing()) {
                mContactProgressDialog.dismiss();
            }
        }
    }

    @OnClick(R.id.reload_text)
    public void reloadTextViewClick(Button button) {
        loadData();
    }

    /* ItemClickListener for contact list items */
    private final ContactListAdapter.ClickListener itemClickListener
            = new ContactListAdapter.ClickListener() {
        @Override
        public void onItemClick(final int position, View v) {
            switch (v.getId()) {
                case R.id.call_button:
                    mobileNumber = mContactList.get(position).getPhone().getMobile();
                    askForPermission();
                    break;
                case R.id.favourite_button:
                    new ContactSaver(getActivity(), mContactList.get(position)).execute();
                    Snackbar.
                            make(mRelativeLayout, R.string.added_to_favorites_snackbar, Snackbar.LENGTH_LONG)
                            .setAction(R.string.undo_message_snackbar, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new ContactRemover(getActivity(), mContactList.get(position)).execute();
                                }
                            }).show();
                    break;
            }
        }
    };
}
