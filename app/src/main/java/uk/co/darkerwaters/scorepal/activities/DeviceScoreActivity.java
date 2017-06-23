package uk.co.darkerwaters.scorepal.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.co.darkerwaters.scorepal.ViewAnimator;
import uk.co.darkerwaters.scorepal.fragments.DeviceConnectionFragment;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.bluetooth.BtManager;
import uk.co.darkerwaters.scorepal.fragments.ScoreControlsFragment;
import uk.co.darkerwaters.scorepal.fragments.ScoreEntryFragment;
import uk.co.darkerwaters.scorepal.fragments.ScorePreviousSetsFragment;
import uk.co.darkerwaters.scorepal.storage.Match;
import uk.co.darkerwaters.scorepal.storage.ScoreData;
import uk.co.darkerwaters.scorepal.storage.StorageManager;

public class DeviceScoreActivity extends AppCompatActivity implements BtManager.IBtManagerListener, ScoreEntryFragment.IScoreEntryFragmentListener {

    private DeviceConnectionFragment topToolbar;
    private ScoreControlsFragment bottomToolbar;
    private ScoreEntryFragment scoreEntryFragment;
    private ScorePreviousSetsFragment scorePreviousSetsFragment;
    private ViewAnimator animator;

    private TextView gameTypeText;

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

        // update our display to the latest received score data
        displayScoreData(BtManager.getManager().getLatestScoreData());
        // register ourselves as a listener for changes
        BtManager.getManager().registerListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // request focus from the main layout to prevent the edit box from taking it and showing
        // the keyboard all the time
        View topLayout = (View) findViewById(R.id.focusable_layout);
        if (null != topLayout) {
            topLayout.requestFocus();
        }

        // initialise any remembered settings
        recallStoredSettings();
    }

    @Override
    protected void onDestroy() {
        // unregister our listeners
        BtManager.getManager().unregisterListener(this);
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
        // and put them into the boxes
        scoreEntryFragment.setTitles(playerOneTitle, playerTwoTitle);
    }

    @Override
    public void onPlayerTitlesUpdated(String playerOneTitle, String playerTwoTitle) {
        // set the titles of the labels in the sets view
        scorePreviousSetsFragment.updatePlayerTitles(playerOneTitle, playerTwoTitle);
        // we want to store these titles to remember them next time too
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.stored_playerone_title), playerOneTitle);
        editor.putString(getString(R.string.stored_playertwo_title), playerTwoTitle);
        editor.commit();
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
            // update the score entry fragment with this new score
            this.scoreEntryFragment.displayScoreData(scoreData);
            // and the previous games status
            this.scorePreviousSetsFragment.displayScoreData(scoreData);
            // show the game type
            displayGameTypeAndData(scoreData);
        }
    }

    private void displayGameTypeAndData(ScoreData scoreData) {
        String typeText = getResources().getString(R.string.played) + " ";
        switch (scoreData.currentScoreMode) {
            case K_SCOREWIMBLEDON5:
                typeText += getResources().getString(R.string.played_wimbledon5);
                break;
            case K_SCOREWIMBLEDON3:
                typeText += getResources().getString(R.string.played_wimbledon3);
                break;
            case K_SCOREBADMINTON5:
                typeText += getResources().getString(R.string.played_badminton5);
                break;
            case K_SCOREBADMINTON3:
                typeText += getResources().getString(R.string.played_badminton3);
                break;
            case K_SCOREFAST4:
                typeText += getResources().getString(R.string.played_fast_four);
                break;
            case K_SCOREPOINTS:
            default:
                typeText += getResources().getString(R.string.played_points);
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
        typeText += Integer.toString(minutes);
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

    @Override
    public void onBtDataChanged(final ScoreData scoreData) {
        // this is interesting - show this score data
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                displayScoreData(scoreData);
            }
        });
    }

    public String getPlayerOneTitle() {
        return scoreEntryFragment.getPlayerOneTitle();
    }

    public String getPlayerTwoTitle() {
        return scoreEntryFragment.getPlayerTwoTitle();
    }
}
