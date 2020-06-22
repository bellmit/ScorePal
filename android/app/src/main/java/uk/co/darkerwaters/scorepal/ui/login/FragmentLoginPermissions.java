package uk.co.darkerwaters.scorepal.ui.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.ApplicationPreferences;
import uk.co.darkerwaters.scorepal.application.ApplicationState;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.ui.PermissionsHandler;
import uk.co.darkerwaters.scorepal.ui.appsettings.FragmentAppSettingsGeneral;

import static uk.co.darkerwaters.scorepal.ui.appsettings.FragmentAppSettingsGeneral.PERMISSIONS_BT;
import static uk.co.darkerwaters.scorepal.ui.appsettings.FragmentAppSettingsGeneral.PERMISSIONS_CONTACTS;
import static uk.co.darkerwaters.scorepal.ui.appsettings.FragmentAppSettingsGeneral.PERMISSIONS_LOCATION;

public class FragmentLoginPermissions extends FragmentLogin implements PermissionsHandler.PermissionsListener {

    private Switch locationSwitch;
    private Switch contactsSwitch;
    private Switch bluetoothSwitch;
    private final List<String> permissions = new ArrayList<>();
    private PermissionsHandler permissionsHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // create this layout in this view
        View root = inflater.inflate(R.layout.fragment_login_permissions, container, false);
        // handle the next button
        root.findViewById(R.id.nextButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityLogin parent = getParent();
                if (null != parent) {
                    parent.changeViews(+1);
                }
            }
        });

        locationSwitch = root.findViewById(R.id.useLocationSwitch);
        contactsSwitch = root.findViewById(R.id.useContactSwitch);
        bluetoothSwitch = root.findViewById(R.id.useBluetoothSwitch);

        this.locationSwitch.setChecked(true);
        this.contactsSwitch.setChecked(true);
        this.bluetoothSwitch.setChecked(true);

        return root;
    }

    @Override
    public void onResume() {
        FragmentActivity activity = getActivity();
        if (null != activity && activity instanceof PermissionsHandler.Container) {
            permissionsHandler = ((PermissionsHandler.Container)activity).getPermissionsHandler();
            permissionsHandler.addListener(this);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        ApplicationPreferences preferences = ApplicationState.Initialise(getContext()).getPreferences();
        preferences.setIsStoreLocations(locationSwitch.isChecked());
        preferences.setIsUseContacts(contactsSwitch.isChecked());
        permissions.clear();
        if (locationSwitch.isChecked()) {
            // request location permissions if they are not enabled
            addPermissions(PERMISSIONS_LOCATION);
        }
        if (contactsSwitch.isChecked()) {
            // request contacts permissions if they are not enabled
            addPermissions(PERMISSIONS_CONTACTS);
        }
        if (bluetoothSwitch.isChecked()) {
            // request contacts permissions if they are not enabled
            addPermissions(PERMISSIONS_BT);
        }
        if (null != permissionsHandler) {
            permissionsHandler.checkPermissions(R.string.permissionRationale, R.drawable.ic_sports_tennis_black_24dp, permissions.toArray(new String[0]), false);
        }
        super.onPause();
    }

    private void addPermissions(String[] toAdd) {
        for (String permission : toAdd) {
            if (!this.permissions.contains(permission)) {
                this.permissions.add(permission);
            }
        }
    }

    private boolean isPermissionsGranted(String[] permissions) {
        return null != permissionsHandler && permissionsHandler.isPermissionsGranted(permissions);
    }

    @Override
    public void updateUI() {
        this.locationSwitch.setChecked(true);
        this.contactsSwitch.setChecked(true);
        this.bluetoothSwitch.setChecked(true);
    }

    @Override
    public void onPermissionsChanged(String[] permissions, int[] grantResults) {
        ApplicationPreferences preferences = ApplicationState.Initialise(getContext()).getPreferences();
        // checked if they want to use the feature and the permissions are enabled for it
        locationSwitch.setChecked(preferences.getIsStoreLocations() && isPermissionsGranted(PERMISSIONS_LOCATION));
        contactsSwitch.setChecked(preferences.getIsUseContacts() && isPermissionsGranted(PERMISSIONS_CONTACTS));
        // if BT permissions are on, they can use them
        bluetoothSwitch.setChecked(isPermissionsGranted(PERMISSIONS_BT));
    }
}
