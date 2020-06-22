package uk.co.darkerwaters.scorepal.ui.appsettings;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.snackbar.Snackbar;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.ApplicationPreferences;
import uk.co.darkerwaters.scorepal.application.ApplicationState;
import uk.co.darkerwaters.scorepal.application.MatchPersistenceManager;
import uk.co.darkerwaters.scorepal.application.MatchStatistics;
import uk.co.darkerwaters.scorepal.data.MatchId;
import uk.co.darkerwaters.scorepal.ui.ActivityAttributions;
import uk.co.darkerwaters.scorepal.ui.PermissionsHandler;
import uk.co.darkerwaters.scorepal.ui.login.ActivityLogin;
import uk.co.darkerwaters.scorepal.ui.matchlists.ActivityMatchTrash;
import uk.co.darkerwaters.scorepal.ui.views.CustomSnackbar;

public class FragmentAppSettingsGeneral extends FragmentAppSettings implements PermissionsHandler.PermissionsListener {

    private static final int FILE_CHOSEN = 141;

    public static final String[] PERMISSIONS_BT = new String[] {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION };

    public static final String[] PERMISSIONS_LOCATION = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION };

    public static final String[] PERMISSIONS_CONTACTS = new String[] {
            Manifest.permission.READ_CONTACTS };

    public static final String[] PERMISSIONS_FILES = new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };

    private ApplicationPreferences preferences;
    private PermissionsHandler permissionsHandler;

    TextView accountNameText;
    TextView accountEmailText;
    ImageView accountImage;
    TextView accountExplanationText;
    Button accountLoginButton;
    Button trashButton;
    Button importButton;
    Button attributionButton;

    Switch locationSwitch;
    Switch contactsSwitch;
    Switch bluetoothSwitch;

    Switch wipeDataSwitch;
    Button wipeDataButton;

    public FragmentAppSettingsGeneral() {
        super(R.layout.fragment_app_settings_general, R.id.nav_app_settings_general);
    }

    @Override
    protected void setupControls(View root) {
        // find all the controls
        accountNameText = root.findViewById(R.id.accountNameText);
        accountEmailText = root.findViewById(R.id.accountEmailText);
        accountImage = root.findViewById(R.id.accountImage);
        accountExplanationText = root.findViewById(R.id.accountExplanationText);
        accountLoginButton = root.findViewById(R.id.accountLogoutLoginButton);
        trashButton = root.findViewById(R.id.buttonViewTrash);
        importButton = root.findViewById(R.id.buttonViewImport);
        attributionButton = root.findViewById(R.id.buttonViewAttributions);

        locationSwitch = root.findViewById(R.id.useLocationSwitch);
        contactsSwitch = root.findViewById(R.id.useContactSwitch);
        bluetoothSwitch = root.findViewById(R.id.useBluetoothSwitch);

        wipeDataSwitch = root.findViewById(R.id.wipeDataSwitch);
        wipeDataButton = root.findViewById(R.id.wipeAllDataButton);

        preferences = ApplicationState.Initialise(getContext()).getPreferences();
        FragmentActivity activity = getActivity();
        if (null != activity && activity instanceof PermissionsHandler.Container) {
            permissionsHandler = ((PermissionsHandler.Container)activity).getPermissionsHandler();
            permissionsHandler.addListener(this);
        }
        // listen to changes on these buttons
        locationSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences.setIsStoreLocations(locationSwitch.isChecked());
                updateSwitchCheck(locationSwitch);
                if (locationSwitch.isChecked()) {
                    // request location permissions if they are not enabled
                    checkPermissions(R.string.locationRationale, R.drawable.ic_near_me_black_24dp, PERMISSIONS_LOCATION);
                }
            }
        });
        contactsSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences.setIsUseContacts(contactsSwitch.isChecked());
                updateSwitchCheck(contactsSwitch);
                if (contactsSwitch.isChecked()) {
                    // request contacts permissions if they are not enabled
                    checkPermissions(R.string.contactsRationale, R.drawable.ic_contact_mail_black_24dp, PERMISSIONS_CONTACTS);
                }
            }
        });
        bluetoothSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // this isn't a preference as such, just make sure that BT permissions are on
                updateSwitchCheck(bluetoothSwitch);
                if (bluetoothSwitch.isChecked()) {
                    // request contacts permissions if they are not enabled
                    checkPermissions(R.string.bluetoothRationale, R.drawable.ic_bluetooth_black_24dp, PERMISSIONS_BT);
                }
            }
        });

        accountLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ActivityLogin.class);
                getActivity().startActivity(intent);
            }
        });
        trashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ActivityMatchTrash.class);
                getActivity().startActivity(intent);
            }
        });
        attributionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ActivityAttributions.class);
                getActivity().startActivity(intent);
            }
        });
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // import our match data by showing the file chooser intent to select a file to import
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                //sets the select file to all types of files
                intent.setType("*/*");
                // Only get openable files
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                //starts new activity to select file and return data
                startActivityForResult(Intent.createChooser(intent, getString(R.string.fileChooseUploadTitle)), FILE_CHOSEN);
            }
        });
        wipeDataSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSwitchCheck(wipeDataSwitch);
                if (wipeDataSwitch.isChecked()) {
                    wipeDataButton.animate()
                            .alpha(1.0f)
                            .setDuration(1000)
                            .withStartAction(new Runnable() {
                                @Override
                                public void run() {
                                    wipeDataButton.setVisibility(View.VISIBLE);
                                }
                            })
                            .start();
                }
                else {
                    wipeDataButton.animate()
                            .alpha(0.0f)
                            .setDuration(1000)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    wipeDataButton.setVisibility(View.INVISIBLE);
                                }
                            })
                            .start();
                }
            }
        });
        wipeDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CustomSnackbar(getActivity(),
                        R.string.areYouSureWipeData,
                        R.drawable.ic_delete_sweep_black_24dp,
                        R.string.yes, R.string.no,
                        new CustomSnackbar.SnackbarListener() {
                    @Override
                    public void onButtonOnePressed() {
                        //Yes button clicked
                        Context context = getContext();
                        //!!! we need to do the stats first as it uses the players name in the settings
                        MatchStatistics.GetInstance(context, true).wipeStatisticsFile(context);
                        // now we can delete the setup files and clear our preferences
                        ApplicationState instance = ApplicationState.Instance();
                        instance.wipeDefaultMatchSetups(context);
                        instance.getPreferences().wipeAllSettings();
                        // also we can delete all the files the user has stored...
                        MatchPersistenceManager.GetInstance().wipeAllMatchFiles(context);
                        // and reset the data on the controls to hide the clear option now it's done
                        setDataToControls();
                    }
                    @Override
                    public void onButtonTwoPressed() {
                        // no, reset the data (will hide the clear option)
                        setDataToControls();
                    }
                    @Override
                    public void onDismissed() {
                        // closed, reset the data (will hide the clear option)
                        setDataToControls();
                    }
                });
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case FILE_CHOSEN:
                if (null != data) {
                    MatchId importedMatchId = MatchPersistenceManager.GetInstance().importMatchData(getContext(), data.getData());
                    if (null != importedMatchId && importedMatchId.isValid()) {
                        // tell the user that this worked
                        String successString = String.format(getString(R.string.successful_import), importedMatchId.toString());
                        Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.container), successString, Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                    else {
                        Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.container), R.string.importFailed, Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                }
                break;
        }
    }

    @Override
    public void onPause() {
        // stop listening to the permissions handler
        if (null != permissionsHandler) {
            permissionsHandler.removeListener(this);
        }
        super.onPause();
    }

    @Override
    protected void setDataToControls() {
        // get our user name
        accountNameText.setText(preferences.getUserName());
        accountEmailText.setVisibility(View.INVISIBLE);
        accountImage.setImageResource(R.drawable.ic_person_black_24dp);
        if (preferences.getIsUseGoogleLogin()) {
            accountExplanationText.setText(R.string.currentLoginGoogle);
            // do we have an email and an image?
            ApplicationState state = ApplicationState.Instance();
            if (null != state) {
                String email = state.getUserEmail();
                accountEmailText.setText(email == null ? "" : email);
                accountEmailText.setVisibility(View.VISIBLE);
                Uri userImage = state.getUserImage();
                if (null != userImage && !userImage.toString().isEmpty()) {
                    accountImage.setImageURI(userImage);
                }
            }
        }
        else {
            accountExplanationText.setText(R.string.currentLoginNameOnly);

        }
        // on update, turn off the wipe and hide the button
        wipeDataButton.setVisibility(View.INVISIBLE);
        setSwitchChecked(wipeDataSwitch, false);
        // checked if they want to use the feature and the permissions are enabled for it
        setSwitchChecked(locationSwitch, preferences.getIsStoreLocations() && isPermissionsGranted(PERMISSIONS_LOCATION));
        setSwitchChecked(contactsSwitch, preferences.getIsUseContacts() && isPermissionsGranted(PERMISSIONS_CONTACTS));
        // if BT permissions are on, they can use them
        setSwitchChecked(bluetoothSwitch, isPermissionsGranted(PERMISSIONS_BT));
    }

    private boolean isPermissionsGranted(String[] permissions) {
        return null != permissionsHandler && permissionsHandler.isPermissionsGranted(permissions);
    }

    private void checkPermissions(int rationaleString, int iconRes, String[] permissions) {
        // check for these permissions
        if (null != permissionsHandler) {
            permissionsHandler.checkPermissions(rationaleString, iconRes, permissions, true);
        }
    }

    @Override
    public void onPermissionsChanged(String[] permissions, int[] grantResults) {
        setDataToControls();
    }
}
