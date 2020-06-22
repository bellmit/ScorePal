package uk.co.darkerwaters.scorepal.activities;

import android.Manifest;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.handlers.ContactArrayAdapter;
import uk.co.darkerwaters.scorepal.activities.handlers.ContactListAdapter;
import uk.co.darkerwaters.scorepal.activities.handlers.PermissionHandler;

import static uk.co.darkerwaters.scorepal.activities.handlers.PermissionHandler.MY_PERMISSIONS_REQUEST_READ_CONTACTS;

public abstract class BaseContactsActivity extends BaseActivity {

    private PermissionHandler permissionHandler;
    private ContactArrayAdapter contactAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // we need to be sure to have permission to access contacts here
        this.permissionHandler = new PermissionHandler(this,
                R.string.contact_access_explanation,
                MY_PERMISSIONS_REQUEST_READ_CONTACTS,
                Manifest.permission.READ_CONTACTS,
                new PermissionHandler.PermissionsHandlerConstructor() {
                    @Override
                    public boolean getIsRequestPermission() {
                        return application.getSettings().getIsRequestContactsPermission();
                    }
                    @Override
                    public void onPermissionsDenied(String[] permissions) {
                        application.getSettings().setIsRequestContactsPermission(false);
                        createAlternativeContactList();
                    }
                    @Override
                    public void onPermissionsGranted(String[] permissions) {
                        createContactsAdapter();
                    }
                });
        // check / request access to contacts and setup the editing controls accordingly
        this.permissionHandler.requestPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // pass this message to our handler
        if (!this.permissionHandler.processPermissionsResult(requestCode, permissions, grantResults)) {
            // the handler didn't do anything, pass it on
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    protected ArrayAdapter getCursorAdapter() {
        return this.contactAdapter;
    }

    private void createAlternativeContactList() {
        // if here then they said 'no' to contacts - stop asking already!
        this.application.getSettings().setIsRequestContactsPermission(false);
        // and create an alternative adapter to do the auto-completion of names
        //TODO create an auto-completion adapter for all the players we played previously
    }

    private void createContactsAdapter() {
        this.contactAdapter = new ContactArrayAdapter(this);
        //TODO add the auto-completion answers for all the players we played previously?
        // and remove duplicates from the contact list as we get and show it
        setupAdapters(this.contactAdapter);
    }

    protected abstract void setupAdapters(ArrayAdapter adapter);
}
