package ogasimli.org.contacts.ui.fragment;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import butterknife.Unbinder;
import ogasimli.org.contacts.R;
import ogasimli.org.contacts.helper.Constants;
import ogasimli.org.contacts.ui.adapter.ContactListAdapter;

/**
 * BaseFragment class
 *
 * Created by Orkhan Gasimli on 12.05.2017.
 */

public abstract class BaseFragment extends Fragment {

    ContactListAdapter mContactListAdapter;

    RecyclerView mRecyclerView;

    String fragmentName;

    String mobileNumber;

    Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //Instantiate RecyclerView
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(mLayoutManager);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    void askForPermission() {
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

    private void startCallIntent() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DIAL);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            getActivity().startActivity(new Intent(Intent.ACTION_DIAL,
                    Uri.fromParts("tel", mobileNumber, null)));
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
