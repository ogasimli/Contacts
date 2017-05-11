package ogasimli.org.contacts.ui.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
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

public class ContactsFragment extends Fragment {

    private final String LOG_TAG = ContactsFragment.class.getSimpleName();

    private ContactListAdapter mContactListAdapter;

    private ArrayList<Contact> mContactList;

    private ProgressDialog mContactProgressDialog;

    private Unbinder mUnbinder;

    private ContactActionListener mContactActionListener;

    private String mMobileNumber;

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
        if (context instanceof ContactActionListener) {
            mContactActionListener = (ContactActionListener) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        //Instantiate RecyclerView adapter
        mContactListAdapter = new ContactListAdapter(LOG_TAG);

        //Instantiate RecyclerView
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mContactListAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
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

        mUnbinder.unbind();
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
                    mMobileNumber = mContactList.get(position).getPhone().getMobile();
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
                                    if (mContactActionListener != null) {
                                        mContactActionListener.onFavoriteChanged();
                                    }
                                }
                            }).show();
                    if (mContactActionListener != null) {
                        mContactActionListener.onFavoriteChanged();
                    }
                    break;
            }
        }
    };

    /*Action listener that notifies if the favorite contact is changed*/
    public interface ContactActionListener {
        void onFavoriteChanged();
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
