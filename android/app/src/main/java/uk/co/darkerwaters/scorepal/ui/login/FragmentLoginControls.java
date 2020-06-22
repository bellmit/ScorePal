package uk.co.darkerwaters.scorepal.ui.login;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.ApplicationPreferences;
import uk.co.darkerwaters.scorepal.application.ApplicationState;
import uk.co.darkerwaters.scorepal.controllers.Flic1Controller;
import uk.co.darkerwaters.scorepal.ui.PermissionsHandler;
import uk.co.darkerwaters.scorepal.ui.appsettings.ActivityFlic2Setup;
import uk.co.darkerwaters.scorepal.ui.appsettings.ActivityMediaRemoteSetup;
import uk.co.darkerwaters.scorepal.ui.appsettings.FragmentAppSettingsGeneral;
import uk.co.darkerwaters.scorepal.ui.views.CheckableIndicatorButton;
import uk.co.darkerwaters.scorepal.ui.views.RadioIndicatorButton;

import static uk.co.darkerwaters.scorepal.ui.appsettings.FragmentAppSettingsGeneral.PERMISSIONS_BT;
import static uk.co.darkerwaters.scorepal.ui.appsettings.FragmentAppSettingsGeneral.PERMISSIONS_CONTACTS;
import static uk.co.darkerwaters.scorepal.ui.appsettings.FragmentAppSettingsGeneral.PERMISSIONS_LOCATION;

public class FragmentLoginControls extends FragmentLogin implements PermissionsHandler.PermissionsListener {

    private PermissionsHandler permissionsHandler;

    private RadioIndicatorButton controlTeams;
    private RadioIndicatorButton controlServe;

    private CheckableIndicatorButton controlVolume;
    private CheckableIndicatorButton controlFlic1;
    private CheckableIndicatorButton controlFlic2;

    private TextView clickSingleText;
    private TextView clickDoubleText;
    private ImageView flicLink;

    private Button flic1SetupButton;
    private Button flic2SetupButton;

    private ApplicationPreferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // create this layout in this view
        View root = inflater.inflate(R.layout.fragment_login_controls, container, false);

        preferences = ApplicationState.Instance().getPreferences();
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

        controlTeams = root.findViewById(R.id.controlTypeTeams);
        controlServe = root.findViewById(R.id.controlTypeServerReceiver);

        controlVolume = root.findViewById(R.id.volControlButton);
        controlFlic1 = root.findViewById(R.id.flic1ControlButton);
        controlFlic2 = root.findViewById(R.id.flic2ControlButton);
        clickSingleText = root.findViewById(R.id.clickSingleTextView);
        clickDoubleText = root.findViewById(R.id.clickDoubleTextView);

        flicLink = root.findViewById(R.id.flicLink);

        flic1SetupButton = root.findViewById(R.id.flic1SetupButton);
        flic2SetupButton = root.findViewById(R.id.flic2SetupButton);

        // listen for changes to the control type
        controlTeams.addOnCheckChangeListener(new CheckableIndicatorButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                if (isChecked) {
                    setControlType(true);
                }
            }
        });
        controlServe.addOnCheckChangeListener(new CheckableIndicatorButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                if (isChecked) {
                    setControlType(false);
                }
            }
        });

        // and turning on or off the types of control
        controlVolume.addOnCheckChangeListener(new CheckableIndicatorButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                preferences.setIsControlVol(isChecked);
            }
        });
        controlFlic1.addOnCheckChangeListener(new CheckableIndicatorButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                setIsControlFlic1(isChecked);
            }
        });
        controlFlic2.addOnCheckChangeListener(new CheckableIndicatorButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                setIsControlFlic2(isChecked);
            }
        });

        // the setup buttons
        flic1SetupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Flic1Controller.Initialise(getActivity(), true);
            }
        });
        flic2SetupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ActivityFlic2Setup.class);
                getActivity().startActivity(intent);
            }
        });

        // and the links
        flicLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // send the user to the web shopping
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, getString(R.string.purchase_flic_remote_query));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                startActivity(intent);
            }
        });

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

    private void setIsControlFlic1(boolean isChecked) {
        // set flic to be on or off
        preferences.setIsControlFlic1(isChecked);
        // and show / hide the button
        if (isChecked) {
            flic1SetupButton.animate()
                    .alpha(1f)
                    .setDuration(1000)
                    .withStartAction(new Runnable() {
                        @Override
                        public void run() {
                            flic1SetupButton.setVisibility(View.VISIBLE);
                        }
                    })
                    .start();
        }
        else {
            flic1SetupButton.setVisibility(View.GONE);
            flic1SetupButton.setAlpha(0f);
        }
    }

    private void setIsControlFlic2(boolean isChecked) {
        // set flic to be on or off
        preferences.setIsControlFlic2(isChecked);
        // and show / hide the button
        if (isChecked) {
            flic2SetupButton.animate()
                    .alpha(1f)
                    .setDuration(1000)
                    .withStartAction(new Runnable() {
                        @Override
                        public void run() {
                            flic2SetupButton.setVisibility(View.VISIBLE);
                        }
                    })
                    .start();
        }
        else {
            flic2SetupButton.setVisibility(View.GONE);
            flic2SetupButton.setAlpha(0f);
        }
    }

    private void setControlType(boolean isControlUseTeams) {
        // set this on the preferences
        preferences.setIsControlTeams(isControlUseTeams);
        // and update the labels for what everything does
        updateLabels();
    }

    private void updateLabels() {
        boolean teams = preferences.getIsControlTeams();

        clickSingleText.setText(teams ? R.string.controlClickSingleTeamExplain : R.string.controlClickSingleServingExplain);
        clickDoubleText.setText(teams ? R.string.controlClickDoubleTeamExplain : R.string.controlClickDoubleServingExplain);
    }

    private boolean isPermissionsGranted(String[] permissions) {
        return null != permissionsHandler && permissionsHandler.isPermissionsGranted(permissions);
    }

    @Override
    public void updateUI() {
        // update the display
        updateUIDisplay();
        // and check our permissions
        if (preferences.getIsControlFlic1() || preferences.getIsControlFlic2()) {
            if (null != permissionsHandler) {
                permissionsHandler.checkPermissions(R.string.bluetoothRationale, R.drawable.ic_bluetooth_black_24dp, PERMISSIONS_BT, true);
            }
        }
    }

    private void updateUIDisplay() {
        // just update the display of items here - checking permission will send an update to update the UI
        // ad-infinitum
        if (preferences.getIsControlTeams()) {
            this.controlTeams.setChecked(true);
        }
        else {
            this.controlServe.setChecked(true);
        }
        this.controlVolume.setChecked(preferences.getIsControlVol());
        this.controlFlic1.setChecked(preferences.getIsControlFlic1());
        this.controlFlic2.setChecked(preferences.getIsControlFlic2());
        // and update the labels for what everything does
        updateLabels();

        // and the buttons
        setIsControlFlic1(preferences.getIsControlFlic1());
        setIsControlFlic2(preferences.getIsControlFlic2());
    }

    @Override
    public void onPermissionsChanged(String[] permissions, int[] grantResults) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateUIDisplay();
            }
        });
    }
}
