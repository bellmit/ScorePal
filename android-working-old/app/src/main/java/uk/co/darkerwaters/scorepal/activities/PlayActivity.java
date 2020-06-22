package uk.co.darkerwaters.scorepal.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.Toast;

import java.util.Date;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.handlers.MatchPlayNavigationHandler;
import uk.co.darkerwaters.scorepal.application.GamePlayCommunicator;
import uk.co.darkerwaters.scorepal.application.GamePlayService;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.MatchWriter;
import uk.co.darkerwaters.scorepal.score.base.MatchMessage;
import uk.co.darkerwaters.scorepal.score.MatchSettings;
import uk.co.darkerwaters.scorepal.score.base.Sport;
import uk.co.darkerwaters.scorepal.views.ControllerInputView;

public abstract class PlayActivity extends FlicPlayActivity implements Match.MatchListener {

    public static final String K_ISFROMSETTINGS = "IsInitiatedFromSettings";
    private static final long K_HIDING_TOOLBAR_DELAY = 5000;

    protected MatchPlayNavigationHandler navigationActor;
    private ControllerInputView controllerInputView;

    private Button undoButton;
    private Button stopPlayButton;

    private ViewGroup mainLayout;

    private boolean isScreenLocked = false;
    private Toolbar toolbar;
    private View toolbarLayout;
    private Handler toolbarHider;
    private Runnable hiderRunnable;

    private boolean isInitiatedFromSettings = false;
    protected boolean isInitialisedCorrectly = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Remove System bar (and retain title bar)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // remember if we came from the settings activity
        this.isInitiatedFromSettings = getIntent().getBooleanExtra(K_ISFROMSETTINGS, this.isInitiatedFromSettings);
    }

    protected boolean setupActivity(int titleStringId, ViewGroup mainLayout) {
        // DO NOT CALL THE BASE CLASS, WE DON'T WANT THE MAIN MENU!
        //super.setupActivity(titleStringId);
        // we can set our title ourselves
        setTitle(titleStringId);
        // and see if we can initialise things here
        GamePlayCommunicator communicator = GamePlayCommunicator.GetActiveCommunicator();
        boolean isMatchInitialised
                = communicator != null
                && communicator.getCurrentSettings() != null
                && communicator.getCurrentMatch() != null;
        if (!isMatchInitialised) {
            // the match is not initialised, the user killed their app, so just go back to the main
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            // start this main activity now then
            startActivity(intent);
            // if here then this is not a good activity to set our data on
            isInitialisedCorrectly = false;
        }
        return isMatchInitialised;
    }

    protected void setupPlayControls(Sport sport, ViewGroup mainLayout) {
        this.mainLayout = mainLayout;

        // find the controls on this activity
        this.undoButton = findViewById(R.id.undoButton);
        this.stopPlayButton = findViewById(R.id.endMatchButton);
        this.controllerInputView = findViewById(R.id.controllerInputView);

        // be sure these icons are tinted correctly
        setupButtonIcon(this.undoButton, R.drawable.ic_baseline_undo, 0);
        setupButtonIcon(this.stopPlayButton, R.drawable.ic_baseline_stop, 0);

        this.undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // just undo the last point
                undoLastPoint();
            }
        });
        this.stopPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // end / play the match
                stopPlayMatch(true);
                setupStopPlayButton();
            }
        });

        // initialise the stop / play button
        setupStopPlayButton();
        // and initialise the editing controls
        setupEditingControls();

        // and setup the navigation drawer and toolbar
        this.toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.toolbarLayout = findViewById(R.id.app_bar_layout);

        Match currentMatch = this.communicator.getCurrentMatch();
        if (null != currentMatch) {
            this.toolbar.setTitle(currentMatch.getDescription(MatchWriter.DescriptionLevel.BRIEF, this));
        }

        // create the nav listener
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        this.navigationActor = new MatchPlayNavigationHandler(this, drawer, toolbar);
        this.navigationActor.setHeaderDisplay(sport);

        // if we are sent a message to open the drawer, open it
        boolean isOpenDrawer = this.getIntent().getBooleanExtra(MainActivity.K_OPEN_DRAWER, false);
        if (isOpenDrawer) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawer.openDrawer(GravityCompat.START, true);
                }
            }, 250);
        }
    }

    public void hideToolbar() {
        int orientation = getResources().getConfiguration().orientation;
        if (null != toolbarLayout && orientation == Configuration.ORIENTATION_LANDSCAPE) {
            toolbarLayout
                    .animate()
                    .translationY(-toolbar.getBottom())
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            toolbarLayout.setVisibility(View.GONE);
                        }
                    })
                    .setInterpolator(new AccelerateInterpolator()).start();
        }
    }

    public void showToolbar() {
        if (null != toolbarLayout) {
            toolbarLayout
                    .animate()
                    .translationY(0)
                    .withStartAction(new Runnable() {
                        @Override
                        public void run() {
                            toolbarLayout.setVisibility(View.VISIBLE);
                        }
                    })
                    .setInterpolator(new DecelerateInterpolator()).start();
            if (null == this.toolbarHider) {
                // need a handler to hide after a delay
                this.toolbarHider = new Handler(getMainLooper());
                // need a runnable too
                this.hiderRunnable = new Runnable() {
                    @Override
                    public void run() {
                        hideToolbar();
                    }
                };
            }
            else {
                // cancel any running
                this.toolbarHider.removeCallbacks(this.hiderRunnable);
            }
            // post this runnable to hide the toolbar after a short delay
            this.toolbarHider.postDelayed(this.hiderRunnable, K_HIDING_TOOLBAR_DELAY);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.isInitialisedCorrectly) {
            // show the toolbar, which will ironically hide it in a bit...
            showToolbar();
            // and refresh the editing controls
            setupEditingControls();
        }
    }

    public void lockUnlockActivity(MenuItem item) {
        // flip the flag
        this.isScreenLocked = !this.isScreenLocked;
        if (this.isScreenLocked) {
            // lock
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            // refresh the tint
            setupButtonIcon(this, item, R.drawable.ic_baseline_lock);
            setupButtonIcon(this.stopPlayButton, R.drawable.ic_baseline_stop, 0);
            Toast.makeText(this, R.string.warn_screen_locked, Toast.LENGTH_LONG).show();
        }
        else {
            // unlock
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            // refresh the tint
            setupButtonIcon(this, item, R.drawable.ic_baseline_lock_open);
        }
    }

    public void undoLastPoint() { this.communicator.sendRequest(MatchMessage.UNDO_POINT); }

    public void changeEnds() { this.communicator.sendRequest(MatchMessage.CHANGE_STARTING_ENDS); }

    public void changeServer() { this.communicator.sendRequest(MatchMessage.CHANGE_STARTING_SERVER); }

    public void changeStarter() { this.communicator.sendRequest(MatchMessage.CHANGE_STARTER); }

    public void stopPlayMatch(boolean createServiceIfNone) {
        if (createServiceIfNone && null == this.getGamePlayService()) {
            // there is no service, and we will want one, create one here please
            this.createGamePlayService(new ServiceBindListener() {
                @Override
                public void onServiceConnected(GamePlayService service) {
                    // the service is connected, call the function again
                    PlayActivity.this.stopPlayMatch(false);
                }
                @Override
                public void onServiceDisconnected() {

                }
            });
        }
        // if we are playing then end the match, else start the match
        if (false == this.communicator.isPlayStarted()) {
            // we are not playing, start playing the match
            this.communicator.sendRequest(MatchMessage.START_PLAY);
        }
        else {
            // we are started, end the match now by going
            this.communicator.sendRequest(MatchMessage.STOP_PLAY);
            // back to the main activity and clear the activity history so back
            // doesn't come back here
            MatchSettings currentSettings = this.communicator.getCurrentSettings();
            Class<? extends Activity> aClass = currentSettings.getSport().summariseActivityClass;
            if (null != aClass) {
                // there is a summary to show, show it then, but asking it to go back to the main
                // activity when it closes
                Intent intent = new Intent(this, aClass);
                intent.putExtra(SummaryActivity.K_RETURNTOMAINKEY, true);
                // start this main activity now then
                startActivity(intent);
            }
            else {
                // just go all the way back to main
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                // start this main activity now then
                startActivity(intent);
            }
        }
    }

    public void showMatchSettings() {
        if (this.isInitiatedFromSettings) {
            // we came from their, just go back to it
            finish();
        }
        else {
            // show it from here
            Sport sport = this.communicator.getCurrentSettings().getSport();
            Intent intent = new Intent(this, sport.setupActivityClass);
            intent.putExtra(SetupTeamActivity.K_ISFROMPLAY, true);
            // start this main activity now then
            startActivity(intent);
        }
    }

    @Override
    public void onPlayStateChanged(Date playStarted, Date playEnded) {
        super.onPlayStateChanged(playStarted, playEnded);
        // as play is started, setup our editing controls properly
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // change the editing controls to what they should be
                setupEditingControls();
                // and show the toolbar for this significant change
                showToolbar();
            }
        });
    }

    protected void setupEditingControls() {
        // show the sound and control buttons only when play is started
        showPlayingControls(this.communicator.isPlayStarted());
        // the start stop button changes as the state of play changes too
        setupStopPlayButton();
    }

    private void showPlayingControls(boolean isShowControls) {
        if (isShowControls) {
            // show the sounds fragment
            this.controllerInputView.setVisibility(View.VISIBLE);
        }
        else {
            // hide the fragment
            this.controllerInputView.setVisibility(View.GONE);
        }
    }

    protected void setupStopPlayButton() {
        // if we are started then the button is the stop button
        if (this.communicator.isMatchStarted() || this.communicator.isPlayStarted()) {
            // we are started
            if (false == this.communicator.isPlayStarted()) {
                // start tracking that we are playing
                stopPlayMatch(true);
            }
            this.stopPlayButton.setText(R.string.btn_endMatch);
            setupButtonIcon(this.stopPlayButton, R.drawable.ic_baseline_stop, 0);

            if (null != navigationActor) {
                // do this on the menu too
                navigationActor.updateMenuItem(R.id.nav_startStopMatch, R.string.btn_endMatch, R.drawable.ic_baseline_stop, R.color.stop);
                // change server and ends is not possible when started
                navigationActor.setMenuItemVisible(R.id.nav_changeEnds, false);
                navigationActor.setMenuItemVisible(R.id.nav_changeServer, false);
            }
        }
        else {
            // we are not started, show the start button
            this.stopPlayButton.setText(R.string.btn_startMatch);
            setupButtonIcon(this.stopPlayButton, R.drawable.ic_baseline_play_circle_outline, 0);

            if (null != navigationActor) {
                // do this on the menu too
                navigationActor.updateMenuItem(R.id.nav_startStopMatch, R.string.btn_startMatch, R.drawable.ic_baseline_play_circle_outline, R.color.play);
                // change server and ends is possible when not started
                navigationActor.setMenuItemVisible(R.id.nav_changeEnds, true);
                navigationActor.setMenuItemVisible(R.id.nav_changeServer, true);
                //navigationActor.setMenuItemVisible(R.id.nav_changeStarter, true);
            }
        }
    }
}
