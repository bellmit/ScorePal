package uk.co.darkerwaters.scorepal.activities;

import android.content.Intent;
import android.os.Bundle;

import java.util.Date;

import io.flic.lib.FlicAppNotInstalledException;
import io.flic.lib.FlicManager;
import uk.co.darkerwaters.scorepal.application.GamePlayCommunicator;
import uk.co.darkerwaters.scorepal.application.GamePlayService;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.controllers.FlicButtonBroadcastReceiver;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.base.MatchMessage;
import uk.co.darkerwaters.scorepal.score.base.PointChange;

public abstract class FlicPlayActivity extends BaseActivity implements
        FlicButtonBroadcastReceiver.FlicActivityInterface, Match.MatchListener, GamePlayService.GamePlayListener {

    protected GamePlayCommunicator communicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // first things first, activate the communicator we want to be talking to
        this.communicator = GamePlayCommunicator.ActivateCommunicator(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // setup flic if we want
        FlicButtonBroadcastReceiver.InitialiseFlic(this.application, this);

        // start up any controllers on the communicator now we are playing
        GamePlayCommunicator.GetActiveCommunicator().initialiseControllers();

        // and start communications on the active communicator
        this.communicator.startCommunications(this);

        // we might have changed the settings by going back and forth, tell the communicator this
        this.communicator.updateActiveMatchWithSettings();

        // we want to listen to this match to show the score as it changes
        this.communicator.addStateListener(this);
        this.communicator.addMatchListener(this);

        // show the state of this match in the notification bar
        this.communicator.showMatchNotification();
    }

    @Override
    protected void onPause() {
        // every time we pause, add the time so it is shown correctly
        this.communicator.sendRequest(MatchMessage.STORE_STATE);
        // remove us as a listener on the service
        this.communicator.removeStateListener(this);
        this.communicator.removeMatchListener(this);

        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // let the base have it
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FlicManager.GRAB_BUTTON_REQUEST_CODE) {
            // and if flic is wanted deal with that here too
            FlicButtonBroadcastReceiver.HandleRequestResult(this, requestCode, resultCode, data);
        }
    }

    @Override
    public void onFlicInitialised() {
        Log.info("Flic successfully initialised");
    }

    @Override
    public void onFlicUninitialised() {
        Log.info("Flic successfully uninitialised");
    }

    @Override
    public void onFlicNotInstalled(FlicAppNotInstalledException err) {
        Log.error("Using flic but not installed", err);
    }

    @Override
    public void onPlayStateChanged(Date playStarted, Date playEnded) {
        // update the notification just in case
        this.communicator.showMatchNotification();
    }

    @Override
    public void onMatchChanged(Match.MatchChange type) {
        // whatever, they are being updated from the service for this already
    }

    @Override
    public void onMatchPointsChanged(PointChange[] levelsChanged) {
        // whatever, they are being updated from the service for this already
    }
}
