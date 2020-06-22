package uk.co.darkerwaters.scorepal.ui.appsettings;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.ApplicationPreferences;
import uk.co.darkerwaters.scorepal.application.ApplicationState;

public class ActivityMediaRemoteSetup extends ActivityBluetoothSetup {

    private ApplicationPreferences preferences;

    public ActivityMediaRemoteSetup() {
        super(R.layout.activity_mediaremotesetup);
    }

    @Override
    protected void setupViewControls() {
        // setup all our controls here now that we are created
        preferences = ApplicationState.Instance().getPreferences();


    }

    @Override
    protected void onBluetoothEnabled() {
        // enable the scan button

    }

}
