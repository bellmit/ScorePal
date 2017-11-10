package uk.co.darkerwaters.scorepal.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import uk.co.darkerwaters.scorepal.ViewAnimator;
import uk.co.darkerwaters.scorepal.fragments.DeviceConnectionFragment;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.bluetooth.BtManager;
import uk.co.darkerwaters.scorepal.fragments.ScoreControlsFragment;
import uk.co.darkerwaters.scorepal.fragments.ScoreEntryFragment;
import uk.co.darkerwaters.scorepal.fragments.ScorePreviousSetsFragment;
import uk.co.darkerwaters.scorepal.storage.ScoreData;
import uk.co.darkerwaters.scorepal.storage.StorageManager;

public class DeviceScoreActivity extends AppCompatActivity implements BtManager.IBtManagerListener, StorageManager.IStorageManagerDataListener {

    private DeviceConnectionFragment topToolbar;
    private ScoreControlsFragment bottomToolbar;
    private ScoreEntryFragment scoreEntryFragment;
    private ScorePreviousSetsFragment scorePreviousSetsFragment;
    private ViewAnimator animator;

    private TextView gameTypeText;
    private boolean isActivityRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_score);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // create the animator
        this.animator = new ViewAnimator(this);
        // initialise the members so we can read / change / hide etc
        topToolbar = (DeviceConnectionFragment) getSupportFragmentManager().findFragmentById(R.id.deviceconnection_fragment);
        scoreEntryFragment = (ScoreEntryFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_score_entry);
        scorePreviousSetsFragment = (ScorePreviousSetsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_previous_sets);
        bottomToolbar = (ScoreControlsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_score_controls);
        gameTypeText = (TextView) findViewById(R.id.game_type_text);

        // register ourselves as a listener for changes
        BtManager.getManager().registerListener(this);
        StorageManager.getManager().registerListener(this);

        // update our display to the latest received score data
        displayScoreData(StorageManager.getManager().getCurrentScoreData());
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.isActivityRunning = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.isActivityRunning = true;

        // request focus from the main layout to prevent the edit box from taking it and showing
        // the keyboard all the time
        View topLayout = (View) findViewById(R.id.focusable_layout);
        if (null != topLayout) {
            topLayout.requestFocus();
        }

        // initialise any remembered settings
        recallStoredSettings();
        // and update the display to the latest score
        displayScoreData(StorageManager.getManager().getCurrentScoreData());
    }

    @Override
    protected void onDestroy() {
        // unregister our listeners
        BtManager.getManager().unregisterListener(this);
        StorageManager.getManager().unregisterListener(this);
        // and let the super destroy this activity
        super.onDestroy();
    }

    private void recallStoredSettings() {
        // recall any settings from the preferences and put the values onto the view
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        // get the player one title
        String defaultValue = getResources().getString(R.string.player_one);
        String playerOneTitle = sharedPref.getString(getString(R.string.stored_playerone_title), defaultValue);
        // and the player two title
        defaultValue = getResources().getString(R.string.player_two);
        String playerTwoTitle = sharedPref.getString(getString(R.string.stored_playertwo_title), defaultValue);
        // and put this data on the storage manager
        StorageManager.getManager().setCurrentPlayerTitles(playerOneTitle, playerTwoTitle);
    }

    @Override
    public void onPlayerTitlesUpdated(String playerOneTitle, String playerTwoTitle) {
        // we want to store these titles to remember them next time too
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.stored_playerone_title), playerOneTitle);
        editor.putString(getString(R.string.stored_playertwo_title), playerTwoTitle);
        editor.commit();
    }

    @Override
    public void onScoreDataUpdated(final ScoreData scoreData) {
        // this is interesting - show this score data
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                displayScoreData(scoreData);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_history:
                Intent intent = new Intent(getApplicationContext(), MatchHistoryActivity.class);
                startActivity(intent);
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayScoreData(ScoreData scoreData) {
        if (null == scoreData) {
            // show no  data
        } else {
            // show the game type
            displayGameTypeAndData(scoreData);

            // all our score is updated, but do we want to show it all?
            boolean showPrevious = true;
            switch (scoreData.currentScoreMode) {
                case K_TENNIS:
                case K_BADMINTON:
                    // show the previous points fragment
                    break;
                case K_POINTS:
                    // don't show previous sets or games for simple point counting mode
                    showPrevious = false;
                    break;
                default:
                    break;
            }
            if (isActivityRunning) {
                FragmentManager fm = getSupportFragmentManager();
                if (showPrevious == false) {
                    // hide the previous sets fragment as we don't use it
                    fm.beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .hide(scorePreviousSetsFragment)
                            .commit();
                } else {
                    // show the previous sets fragment as we want to use it
                    fm.beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .show(scorePreviousSetsFragment)
                            .commit();
                }
            }
        }
    }

    private void displayGameTypeAndData(ScoreData scoreData) {
        String typeText = getResources().getString(R.string.played_start) + " ";
        switch (scoreData.currentScoreMode) {
            case K_TENNIS:
                typeText += Integer.toString(scoreData.currentSetsOption) + " ";
                typeText += getResources().getString(R.string.played_end_tennis);
                break;
            case K_BADMINTON:
                typeText += Integer.toString(scoreData.currentSetsOption) + " ";
                typeText += getResources().getString(R.string.played_end_badminton);
                break;
            case K_POINTS:
            default:
                typeText += getResources().getString(R.string.played_end_points);
                break;
        }
        // now we can add the time to this string, hours and seconds
        int totalMinutes = (int)(scoreData.secondsGameDuration / 60.0);
        int hours = (int) (totalMinutes / 60.0);
        int minutes = (int) (totalMinutes - (hours * 60));
        // add the space
        typeText += " " + getResources().getString(R.string.sfor) + " ";
        // the hours
        typeText += Integer.toString(hours) + ":";
        // and add the minutes
        typeText += String.format("%02d", minutes);
        // and show to the user
        gameTypeText.setText(typeText);
    }

    @Override
    public void onBtStatusChanged() {
        // not very interesting
    }

    @Override
    public void onBtConnectionStatusChanged() {
        // not very interesting
    }
}
