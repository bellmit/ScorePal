package uk.co.darkerwaters.scorepal.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import io.flic.lib.FlicAppNotInstalledException;
import io.flic.lib.FlicManager;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.settings.SettingsControl;
import uk.co.darkerwaters.scorepal.controllers.Controller;
import uk.co.darkerwaters.scorepal.controllers.FlicButtonBroadcastReceiver;

public class RemoteSetupActivity extends BaseBluetoothActivity {

    private RadioGroup awardPointsRadioGroup;

    private View puckEnabledLayout;
    private Switch remotePuckSwitch;

    private View purchasePuckLayout;
    private View puckSettingsButton;

    private TextView puckSingleClickAction;
    private TextView puckDoubleClickAction;
    //private TextView puckTripleClickAction;
    //private TextView puckLongClickAction;

    private View flicEnabledLayout;
    private Switch remoteFlicSwitch;
    private RadioButton flicVersionOneRadio;
    private RadioButton flicVersionTwoRadio;

    private View purchaseFlicLayout;
    private View flicSettingsButton;

    private TextView flicSingleClickAction;
    private TextView flicDoubleClickAction;
    //private TextView flicTripleClickAction;
    //private TextView flicLongClickAction;

    private View volumeEnabledLayout;
    private Switch remoteVolumeSwitch;

    private TextView volumeSingleClickAction;
    private TextView volumeDoubleClickAction;
    //private TextView volumeTripleClickAction;
    //private TextView volumeLongClickAction;

    private View mediaEnabledLayout;
    private Switch remoteMediaSwitch;

    private TextView mediaNextClickAction;
    private TextView mediaPreviousClickAction;
    //private TextView mediaPauseClickAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_setup);

        // set the title of this
        setupActivity(R.string.menu_mediaControllerSetup);

        final SettingsControl appSettings = new SettingsControl(this.application);

        this.awardPointsRadioGroup = findViewById(R.id.awardPointsRadioGroup);
        this.awardPointsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.awardPointsTeamRadio :
                        appSettings.setIsControlTeams(true);
                        break;
                    case R.id.awardPointsServerRadio:
                        appSettings.setIsControlTeams(false);
                        break;
                }
                updateViewFromSettings(appSettings);
            }
        });

        // get the flic settings things
        this.remoteFlicSwitch = findViewById(R.id.remoteFlicSwitch);
        this.flicVersionOneRadio = findViewById(R.id.radioButtonFlicOne);
        this.flicVersionTwoRadio = findViewById(R.id.radioButtonFlicTwo);
        this.purchaseFlicLayout = findViewById(R.id.purchaseFlicLayout);
        this.flicSettingsButton = findViewById(R.id.flicSettingsButton);
        this.flicSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFlicActivity();
            }
        });
        this.flicEnabledLayout = findViewById(R.id.flicEnabledLayout);
        this.remoteFlicSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    // we are turning flic one, set the right version
                    if (flicVersionOneRadio.isChecked()) {
                        // using version one
                        appSettings.setIsControlUseFlic1(true);
                    }
                    else if (flicVersionTwoRadio.isChecked()) {
                        // using version two
                        appSettings.setIsControlUseFlic2(true);
                    }
                }
                else {
                    // not using flic at all
                    appSettings.setIsControlUseFlic1(false);
                    appSettings.setIsControlUseFlic2(false);
                }
                // show the layout if on, hide if off
                showEnabledLayout(flicEnabledLayout, b);
            }
        });
        this.flicVersionOneRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                appSettings.setIsControlUseFlic1(b);
            }
        });
        this.flicVersionTwoRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                appSettings.setIsControlUseFlic2(b);
            }
        });

        // and get all the text views to show data for
        this.flicSingleClickAction = findViewById(R.id.flicSingleClickAction);
        this.flicDoubleClickAction = findViewById(R.id.flicDoubleClickAction);
        //this.flicTripleClickAction = findViewById(R.id.flicTripleClickAction);
        //this.flicLongClickAction = findViewById(R.id.flicLongClickAction);
        this.purchaseFlicLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // send the user to the web shopping
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, getString(R.string.purchase_remote_query));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                startActivity(intent);
            }
        });

        // get the puck settings things
        this.remotePuckSwitch = findViewById(R.id.remotepuckSwitch);
        this.purchasePuckLayout = findViewById(R.id.purchasepuckLayout);
        this.puckSettingsButton = findViewById(R.id.puckSettingsButton);
        this.puckSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupPuck();
            }
        });
        this.puckEnabledLayout = findViewById(R.id.puckEnabledLayout);
        this.remotePuckSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                appSettings.setIsControlUsePuck(b);
                showEnabledLayout(puckEnabledLayout, b);
            }
        });
        // and get all the text views to show data for
        this.puckSingleClickAction = findViewById(R.id.puckSingleClickAction);
        this.puckDoubleClickAction = findViewById(R.id.puckDoubleClickAction);
        //this.puckTripleClickAction = findViewById(R.id.puckTripleClickAction);
        //this.puckLongClickAction = findViewById(R.id.puckLongClickAction);
        this.purchasePuckLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // send the user to our website
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.purchase_puck_site)));
                startActivity(i);
            }
        });

        // get the volume settings things
        this.remoteVolumeSwitch = findViewById(R.id.remoteVolumeSwitch);
        this.volumeEnabledLayout = findViewById(R.id.volumeEnabledLayout);
        this.remoteVolumeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                appSettings.setIsControlUseVol(b);
                showEnabledLayout(volumeEnabledLayout, b);
            }
        });
        // and get all the text views to show data for
        this.volumeSingleClickAction = findViewById(R.id.volumeSingleClickAction);
        this.volumeDoubleClickAction = findViewById(R.id.volumeDoubleClickAction);
        //this.volumeTripleClickAction = findViewById(R.id.volumeTripleClickAction);
        //this.volumeLongClickAction = findViewById(R.id.volumeLongClickAction);

        // get the media settings things
        this.remoteMediaSwitch = findViewById(R.id.remoteMediaSwitch);
        this.mediaEnabledLayout = findViewById(R.id.mediaEnabledLayout);
        this.remoteMediaSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                appSettings.setIsControlUseMedia(b);
                showEnabledLayout(mediaEnabledLayout, b);
                if (b) {
                    turnOnBluetooth();
                }
            }
        });
        // and get all the text views to show data for
        this.mediaNextClickAction = findViewById(R.id.mediaNextClickAction);
        this.mediaPreviousClickAction = findViewById(R.id.mediaPreviousClickAction);
        //this.mediaPauseClickAction = findViewById(R.id.mediaPauseClickAction);

        // update the view to reflect the settings
        updateViewFromSettings(appSettings);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // let the base have it
        super.onActivityResult(requestCode, resultCode, data);
        // and if flic is wanted deal with that here too
        if (requestCode == FlicManager.GRAB_BUTTON_REQUEST_CODE) {
            // and if flic is wanted deal with that here too
            FlicButtonBroadcastReceiver.HandleRequestResult(this, requestCode, resultCode, data);
        }
    }

    private void showFlicActivity() {
        // show the help screen that will setup flic
        Intent intent = new Intent(this, FlicHelpActivity.class);
        // start this activity now then
        startActivity(intent);
    }

    private void showFlicPurchaseLayout() {
        this.purchaseFlicLayout.setVisibility(View.VISIBLE);
        this.remoteFlicSwitch.setChecked(false);
        showEnabledLayout(this.flicEnabledLayout, false);
    }

    private void setupPuck() {
        // and try to setup puck if we can by connecting to it from BT settings
        Intent myIntent = new Intent(this, BluetoothRemoteSetupActivity.class);
        startActivity(myIntent);
    }

    private void showPuckPurchaseLayout() {
        this.purchasePuckLayout.setVisibility(View.VISIBLE);
        this.remotePuckSwitch.setChecked(false);
        showEnabledLayout(this.puckEnabledLayout, false);
    }

    private void showEnabledLayout(View layout, boolean isShow) {
        layout.setVisibility(isShow ? View.VISIBLE : View.GONE);
        if (layout == this.flicEnabledLayout) {
            this.purchaseFlicLayout.setVisibility(isShow? View.GONE : View.VISIBLE);
        }
        if (layout == this.puckEnabledLayout) {
            this.purchasePuckLayout.setVisibility(isShow? View.GONE : View.VISIBLE);
        }
    }

    private void updateViewFromSettings(SettingsControl appSettings) {
        // set the top-level switch
        if (appSettings.getIsControlTeams()) {
            this.awardPointsRadioGroup.check(R.id.awardPointsTeamRadio);
        }
        else {
            this.awardPointsRadioGroup.check(R.id.awardPointsServerRadio);
        }
        // set the switches
        this.remoteFlicSwitch.setChecked(appSettings.getIsControlUseFlic1() || appSettings.getIsControlUseFlic2());
        this.remotePuckSwitch.setChecked(appSettings.getIsControlUsePuck());
        this.remoteVolumeSwitch.setChecked(appSettings.getIsControlUseVol());
        this.remoteMediaSwitch.setChecked(appSettings.getIsControlUseMedia());

        // show / hide the layout accordingly
        showEnabledLayout(this.puckEnabledLayout, appSettings.getIsControlUsePuck());
        showEnabledLayout(this.flicEnabledLayout, appSettings.getIsControlUseFlic1() || appSettings.getIsControlUseFlic2());
        showEnabledLayout(this.volumeEnabledLayout, appSettings.getIsControlUseVol());
        showEnabledLayout(this.mediaEnabledLayout, appSettings.getIsControlUseMedia());

        // set the flic version to be that actually selected
        this.flicVersionOneRadio.setChecked(appSettings.getIsControlUseFlic1());
        this.flicVersionTwoRadio.setChecked(appSettings.getIsControlUseFlic2());

        // get the buttons we will use now
        String actionOne, actionTwo;
        if (appSettings.getIsControlTeams()) {
            actionOne = Controller.ControllerAction.PointTeamOne.toString(this);
            actionTwo = Controller.ControllerAction.PointTeamTwo.toString(this);
        }
        else {
            actionOne = Controller.ControllerAction.PointServer.toString(this);
            actionTwo = Controller.ControllerAction.PointReceiver.toString(this);
        }
        // change the labels accordingly - ignore if expanded or not
        this.flicSingleClickAction.setText(actionOne);
        this.flicDoubleClickAction.setText(actionTwo);
        this.puckSingleClickAction.setText(actionOne);
        this.puckDoubleClickAction.setText(actionTwo);
        this.volumeSingleClickAction.setText(actionOne);
        this.volumeDoubleClickAction.setText(actionTwo);
        this.mediaNextClickAction.setText(actionOne);
        this.mediaPreviousClickAction.setText(actionTwo);
    }
}
