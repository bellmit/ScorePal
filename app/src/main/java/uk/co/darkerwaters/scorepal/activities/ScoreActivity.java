package uk.co.darkerwaters.scorepal.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
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

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.ViewAnimator;
import uk.co.darkerwaters.scorepal.bluetooth.BtConnectActivity;
import uk.co.darkerwaters.scorepal.bluetooth.BtManager;
import uk.co.darkerwaters.scorepal.storage.Match;
import uk.co.darkerwaters.scorepal.storage.ScoreData;
import uk.co.darkerwaters.scorepal.storage.StorageManager;

public class ScoreActivity extends AppCompatActivity implements BtManager.IBtManagerListener {

    private TextView connectionText;
    private View connectButton;
    private View topToolbar;

    private TextView winnerText;
    private TextView gameTypeText;

    private EditText playerOneTitleText;
    private EditText playerTwoTitleText;

    private TextSwitcher playerOnePointsText;
    private TextSwitcher playerTwoPointsText;

    private View playerOneServeRadio;
    private View playerTwoServeRadio;

    private TextSwitcher playerOneGamesText;
    private TextSwitcher playerTwoGamesText;

    private TextSwitcher playerOneSetsText;
    private TextSwitcher playerTwoSetsText;

    private TextView playerOneSetsLabelText;
    private TextView playerTwoSetsLabelText;

    private TextView playerOneGamesLabelText;
    private TextView playerTwoGamesLabelText;

    private TextView setsTitleText;
    private TextView gamesTitleText;
    private TextView previousSetsTitleText;
    private TextView previousGamesTitleText;

    private TextSwitcher[] playerOnePreviousSets = new TextSwitcher[4];
    private TextSwitcher[] playerTwoPreviousSets = new TextSwitcher[4];

    private TextSwitcher[] playerOnePreviousGames = new TextSwitcher[4];
    private TextSwitcher[] playerTwoPreviousGames = new TextSwitcher[4];

    private boolean isPlayerOneServeStateOn = false;
    private boolean isPlayerTwoServeStateOn = false;

    private ViewSwitcher previousViewSwitcher;

    boolean isConnectivityControlsShown = true;
    boolean isSetsViewShown = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // initialise the members so we can read / change / hide etc
        initialiseMembersFromView();

        // initialise any remembered settings
        recallStoredSettings();

        // now we can initialise the user interactions now all the components are filled
        initialiseUserInteractions();

        // update our display to the latest received score data
        displayScoreData(BtManager.getManager().getLatestScoreData());
        // register ourselves as a listener for changes
        BtManager.getManager().registerListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // update our current connectivity
        updateConnectionDisplay();
        // and update the titles for the players
        updatePlayerTitles();

        // request focus from the main layout to prevent the edit box from taking it and showing
        // the keyboard all the time
        View topLayout = (View) findViewById(R.id.focusable_layout);
        if (null != topLayout) {
            topLayout.requestFocus();
        }

        // and try to connect to the last device
        final ProgressDialog progress = ProgressDialog.show(this,
                getResources().getString(R.string.connecting),
                getResources().getString(R.string.please_wait_connecting), false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final BtManager manager = BtManager.getManager();
                // try the connection here, can take a little while
                manager.connectToLastDevice();
                // dismiss the progress dialog
                progress.dismiss();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (manager.getConnectedDevice() == null) {
                            Toast.makeText(ScoreActivity.this, R.string.failed_to_connect, Toast.LENGTH_SHORT).show();
                        }
                        updateConnectionDisplay();
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        // unregister our listeners
        BtManager.getManager().unregisterListener(this);
        // and let the super destroy this activity
        super.onDestroy();
    }

    private void initialiseUserInteractions() {
        // listen for clicks on the points display to add a point
        playerOnePointsText.setClickable(true);
        playerOnePointsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPoint(0);
            }
        });
        // listen for clicks for player two also
        playerTwoPointsText.setClickable(true);
        playerTwoPointsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPoint(1);
            }
        });

        // listen for text changes to the user's names
        playerOneTitleText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // whatever
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // whatever
            }
            @Override
            public void afterTextChanged(Editable s) {
                // update the label
                updatePlayerTitles();
            }
        });
        // and player two
        playerTwoTitleText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // whatever
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // whatever
            }
            @Override
            public void afterTextChanged(Editable s) {
                // update the label
                updatePlayerTitles();
            }
        });
    }

    private void initialiseMembersFromView() {
        // create the text view animators
        Animation animationIn = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation animationOut = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        // and the text view factories
        ViewSwitcher.ViewFactory pointViewFactory = new ViewSwitcher.ViewFactory() {
            public View makeView() {
                // create a TextView for the switcher
                TextView myText = new TextView(ScoreActivity.this);
                myText.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                myText.setTextAppearance(ScoreActivity.this, R.style.ScorePoints);
                return myText;
            }
        };
        ViewSwitcher.ViewFactory scoreViewFactory = new ViewSwitcher.ViewFactory() {
            public View makeView() {
                // create a TextView for the switcher
                TextView myText = new TextView(ScoreActivity.this);
                myText.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                myText.setTextAppearance(ScoreActivity.this, R.style.ScoreValues);
                return myText;
            }
        };

        topToolbar = findViewById(R.id.top_toolbar_layout);

        connectionText = (TextView) findViewById(R.id.bt_connected_text);
        connectButton = (View) findViewById(R.id.connect_button);
        winnerText = (TextView) findViewById(R.id.winner_text_view);
        gameTypeText = (TextView) findViewById(R.id.game_type_text);

        playerOneTitleText = (EditText) findViewById(R.id.player_one_name_text_view);
        playerTwoTitleText = (EditText) findViewById(R.id.player_two_name_text_view);

        playerOnePointsText = (TextSwitcher) findViewById(R.id.player_one_points_text);
        // Set the ViewFactory of the TextSwitcher that will create TextView object when asked
        setTextSwitcherFactories(playerOnePointsText, pointViewFactory, animationIn, animationOut);

        playerTwoPointsText = (TextSwitcher) findViewById(R.id.player_two_points_text);
        // Set the ViewFactory of the TextSwitcher that will create TextView object when asked
        setTextSwitcherFactories(playerTwoPointsText, pointViewFactory, animationIn, animationOut);

        playerOneServeRadio = (View) findViewById(R.id.player_one_serve_signal);
        playerTwoServeRadio = (View) findViewById(R.id.player_two_serve_signal);

        playerOneGamesText = (TextSwitcher) findViewById(R.id.player_one_games_text);
        setTextSwitcherFactories(playerOneGamesText, scoreViewFactory, animationIn, animationOut);
        playerTwoGamesText = (TextSwitcher) findViewById(R.id.player_two_games_text);
        setTextSwitcherFactories(playerTwoGamesText, scoreViewFactory, animationIn, animationOut);

        playerOneSetsText = (TextSwitcher) findViewById(R.id.player_one_sets_text);
        setTextSwitcherFactories(playerOneSetsText, scoreViewFactory, animationIn, animationOut);
        playerTwoSetsText = (TextSwitcher) findViewById(R.id.player_two_sets_text);
        setTextSwitcherFactories(playerTwoSetsText, scoreViewFactory, animationIn, animationOut);

        playerOneSetsLabelText = (TextView) findViewById(R.id.player_one_sets_label_text);
        playerTwoSetsLabelText = (TextView) findViewById(R.id.player_two_sets_label_text);

        playerOneGamesLabelText = (TextView) findViewById(R.id.player_one_games_label_text);
        playerTwoGamesLabelText = (TextView) findViewById(R.id.player_two_games_label_text);

        setsTitleText = (TextView) findViewById(R.id.sets_text);
        gamesTitleText = (TextView) findViewById(R.id.games_text);
        previousSetsTitleText = (TextView) findViewById(R.id.previous_sets_text);
        previousGamesTitleText = (TextView) findViewById(R.id.previous_games_text);

        // also do the player one and two previous sets display boxes
        playerOnePreviousSets[0] = (TextSwitcher) findViewById(R.id.player_one_set_one_text);
        setTextSwitcherFactories(playerOnePreviousSets[0], scoreViewFactory, animationIn, animationOut);
        playerOnePreviousSets[1] = (TextSwitcher) findViewById(R.id.player_one_set_two_text);
        setTextSwitcherFactories(playerOnePreviousSets[1], scoreViewFactory, animationIn, animationOut);
        playerOnePreviousSets[2] = (TextSwitcher) findViewById(R.id.player_one_set_three_text);
        setTextSwitcherFactories(playerOnePreviousSets[2], scoreViewFactory, animationIn, animationOut);
        playerOnePreviousSets[3] = (TextSwitcher) findViewById(R.id.player_one_set_four_text);
        setTextSwitcherFactories(playerOnePreviousSets[3], scoreViewFactory, animationIn, animationOut);

        playerTwoPreviousSets[0] = (TextSwitcher) findViewById(R.id.player_two_set_one_text);
        setTextSwitcherFactories(playerTwoPreviousSets[0], scoreViewFactory, animationIn, animationOut);
        playerTwoPreviousSets[1] = (TextSwitcher) findViewById(R.id.player_two_set_two_text);
        setTextSwitcherFactories(playerTwoPreviousSets[1], scoreViewFactory, animationIn, animationOut);
        playerTwoPreviousSets[2] = (TextSwitcher) findViewById(R.id.player_two_set_three_text);
        setTextSwitcherFactories(playerTwoPreviousSets[2], scoreViewFactory, animationIn, animationOut);
        playerTwoPreviousSets[3] = (TextSwitcher) findViewById(R.id.player_two_set_four_text);
        setTextSwitcherFactories(playerTwoPreviousSets[3], scoreViewFactory, animationIn, animationOut);

        // also do the player one and two previous games display boxes
        playerOnePreviousGames[0] = (TextSwitcher) findViewById(R.id.player_one_game_one_text);
        setTextSwitcherFactories(playerOnePreviousGames[0], scoreViewFactory, animationIn, animationOut);
        playerOnePreviousGames[1] = (TextSwitcher) findViewById(R.id.player_one_game_two_text);
        setTextSwitcherFactories(playerOnePreviousGames[1], scoreViewFactory, animationIn, animationOut);
        playerOnePreviousGames[2] = (TextSwitcher) findViewById(R.id.player_one_game_three_text);
        setTextSwitcherFactories(playerOnePreviousGames[2], scoreViewFactory, animationIn, animationOut);
        playerOnePreviousGames[3] = (TextSwitcher) findViewById(R.id.player_one_game_four_text);
        setTextSwitcherFactories(playerOnePreviousGames[3], scoreViewFactory, animationIn, animationOut);

        playerTwoPreviousGames[0] = (TextSwitcher) findViewById(R.id.player_two_game_one_text);
        setTextSwitcherFactories(playerTwoPreviousGames[0], scoreViewFactory, animationIn, animationOut);
        playerTwoPreviousGames[1] = (TextSwitcher) findViewById(R.id.player_two_game_two_text);
        setTextSwitcherFactories(playerTwoPreviousGames[1], scoreViewFactory, animationIn, animationOut);
        playerTwoPreviousGames[2] = (TextSwitcher) findViewById(R.id.player_two_game_three_text);
        setTextSwitcherFactories(playerTwoPreviousGames[2], scoreViewFactory, animationIn, animationOut);
        playerTwoPreviousGames[3] = (TextSwitcher) findViewById(R.id.player_two_game_four_text);
        setTextSwitcherFactories(playerTwoPreviousGames[3], scoreViewFactory, animationIn, animationOut);

        previousViewSwitcher = (ViewSwitcher) findViewById(R.id.previous_view_switcher);
    }

    private void setTextSwitcherFactories(TextSwitcher view, ViewSwitcher.ViewFactory pointViewFactory, Animation animationIn, Animation animationOut) {
        if (null != view) {
            view.setFactory(pointViewFactory);
            view.setInAnimation(animationIn);
            view.setOutAnimation(animationOut);
        }
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
        playerOneTitleText.setText(playerOneTitle);
        playerTwoTitleText.setText(playerTwoTitle);
    }

    private void updatePlayerTitles() {
        // set the titles of the labels in the sets view
        String playerOneTitle = getOnlyStrings(playerOneTitleText.getText().toString());
        if (null == playerOneTitle || playerOneTitle.isEmpty()) {
            playerOneTitle = getResources().getString(R.string.player_one);
        }
        if (null != playerOneSetsLabelText) {
            playerOneSetsLabelText.setText(playerOneTitle);
        }
        if (null != playerOneGamesLabelText) {
            playerOneGamesLabelText.setText(playerOneTitle);
        }
        // and do player two
        String playerTwoTitle = getOnlyStrings(playerTwoTitleText.getText().toString());
        if (null == playerTwoTitle || playerTwoTitle.isEmpty()) {
            playerTwoTitle = getResources().getString(R.string.player_two);
        }
        if (null != playerTwoSetsLabelText) {
            playerTwoSetsLabelText.setText(playerTwoTitle);
        }
        if (null != playerTwoGamesLabelText) {
            playerTwoGamesLabelText.setText(playerTwoTitle);
        }

        // we want to store these titles to remember them next time too
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.stored_playerone_title), playerOneTitle);
        editor.putString(getString(R.string.stored_playertwo_title), playerTwoTitle);
        editor.commit();
    }

    private void addPoint(int player) {
        // send the command to add the point for the right player
        BtManager manager = BtManager.getManager();
        boolean result = false;
        if (player == 0) {
            // want to inc player one
            result = manager.sendMessage("{a7}");
        }
        else {
            // want to inc player two
            result = manager.sendMessage("{a8}");
        }
        if (result == isConnectivityControlsShown) {
            // the result is false, but the view is visible (or vice versa)
            // either way the connectivity display is incorrect
            updateConnectionDisplay(result, BtManager.getManager().getConnectedDevice());
        }
    }

    public void onClickConnect(View view) {
        Intent intent = new Intent(getApplicationContext(), BtConnectActivity.class);
        startActivity(intent);
    }

    private void updateConnectionDisplay() {
        BtManager manager = BtManager.getManager();
        String connectedDevice = manager.getConnectedDevice();
        updateConnectionDisplay(manager.isEnabled() && null != connectedDevice, connectedDevice);
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
            case R.id.nav_history :
                Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
                startActivity(intent);
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionDisplay(boolean isConencted, String connectedDevice) {
        if (isConencted && null != connectedDevice) {
            // get the connected device, if there is one
            connectionText.setText(connectedDevice);
            // just hide this
            if (isConnectivityControlsShown) {
                // slide the views away and make them gone
                ViewAnimator.slideControlsUpAndAway(this, null, topToolbar, connectionText, connectButton);
                // remember that we hid them
                isConnectivityControlsShown = false;
            }
        }
        else {
            // show that no device is connected
            connectionText.setText(R.string.bt_connected);
            if (false == isConnectivityControlsShown) {
                // slide the views in and make them gone
                ViewAnimator.slideControlsDownAndIn(this, null, topToolbar, connectionText, connectButton);
                // remember that we showed them
                isConnectivityControlsShown = true;
            }
        }
    }

    private void displayScoreData(ScoreData scoreData) {
        if (null == scoreData) {
            // show no  data
        }
        else {
            // show the game type
            displayGameTypeAndData(scoreData);
            // display the data for sets
            displayScoreSetData(scoreData);
            // display the data for games
            diplayScoreGameData(scoreData);
            // did someone win?
            displayScoreWinnerData(scoreData);
            // and the points
            displayScorePointsData(scoreData);
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
        int hours = (int)(scoreData.secondsGameDuration / 3600.0);
        int minutes = (int)((scoreData.secondsGameDuration - (hours * 60)) / 60.0);
        // add the space
        typeText += " " + getResources().getString(R.string.sfor) + " ";
        // the hours
        typeText += Integer.toString(hours) + ".";
        // and add the minutes
        typeText += Integer.toString(minutes);
        // and show to the user
        gameTypeText.setText(typeText);
    }

    private void displayScoreWinnerData(ScoreData scoreData) {
        // show the winner if there is one, hiding a load of other stuff to stop interactions
        int visibility;
        if (null != scoreData.matchWinner) {
            winnerText.setVisibility(View.VISIBLE);
            visibility = View.INVISIBLE;
            String title = (scoreData.matchWinner.intValue() == 0 ?
                    getOnlyStrings(playerOneTitleText.getText().toString()) :
                    getOnlyStrings(playerTwoTitleText.getText().toString()));
            if (title.isEmpty()) {
                title = scoreData.matchWinner == 0 ? getResources().getString(R.string.player_one) : getResources().getString(R.string.player_two);
            }
            winnerText.setText(title + " " + getResources().getString(R.string.wins));

            // a won match with set data is special, the final games score goes in the games
            // boxes with the previous sets not showing the final set's points
            switch(scoreData.currentScoreMode) {
                case K_SCOREWIMBLEDON3:
                case K_SCOREWIMBLEDON5:
                    int noSets = scoreData.previousSets.size();
                    if (noSets > 0 && scoreData.sets.first + scoreData.sets.second > 0) {
                        // they are playing sets - show the games for the last set in the games boxes
                        int playerOneFinalSetGames = scoreData.previousSets.get(noSets - 1).first;
                        int playerTwoFinalSetGames = scoreData.previousSets.get(noSets - 1).second;
                        playerOneGamesText.setText(Integer.toString(playerOneFinalSetGames));
                        playerTwoGamesText.setText(Integer.toString(playerTwoFinalSetGames));
                        // and if the final set is not number 5 (not shown) then clear the boxes
                        if (noSets < 5) {
                            playerOnePreviousSets[noSets - 1].setText("");
                            playerTwoPreviousSets[noSets - 1].setText("");
                            if (null != playerOnePreviousGames[noSets - 1]) {
                                playerOnePreviousGames[noSets - 1].setText("");
                                playerTwoPreviousGames[noSets - 1].setText("");
                            }
                        }
                    }
                    break;
            }
        } else {
            // hide the winning text and show the points
            winnerText.setVisibility(View.GONE);
            visibility = View.VISIBLE;
        }

        // show / hide the playing controls
        playerOnePointsText.setVisibility(visibility);
        playerTwoPointsText.setVisibility(visibility);

        // only show serving indicators when no-one has won and we are tracking that (tennis)
        switch (scoreData.currentScoreMode) {
            case K_SCOREWIMBLEDON5:
            case K_SCOREWIMBLEDON3:
            case K_SCOREFAST4:
                // show the server indicators for tennis types
                visibility = View.VISIBLE;
                break;
            default:
                // else do not
                visibility = View.INVISIBLE;
                break;
        }
        if (null != scoreData.matchWinner) {
            // there is a winner, hide the serve indicators always
            visibility = View.INVISIBLE;
        }

        // show / hide the serving indicators
        playerOneServeRadio.setVisibility(visibility);
        playerTwoServeRadio.setVisibility(visibility);
    }

    private void displayScorePointsData(ScoreData scoreData) {
        // show the points in the points views
        setTextSwitcherContent(scoreData.getPointsAsString(scoreData.points.first), playerOnePointsText);
        setTextSwitcherContent(scoreData.getPointsAsString(scoreData.points.second), playerTwoPointsText);
        // who is serving?
        if (playerOneServeRadio.getVisibility() == View.VISIBLE) {
            boolean isPlayerOneServing = scoreData.currentServer == 0;
            if ((false == isPlayerOneServeStateOn && isPlayerOneServing) ||
                    (isPlayerOneServeStateOn && false == isPlayerOneServing)) {
                // we are not showing the correst state for player one, correct this now
                TransitionDrawable background = (TransitionDrawable) playerOneServeRadio.getBackground();
                background.reverseTransition(300);
                // and remember the state we are in
                isPlayerOneServeStateOn = isPlayerOneServing;
            }
        }
        if (playerTwoServeRadio.getVisibility() == View.VISIBLE) {
            boolean isPlayerTwoServing = scoreData.currentServer == 1;
            if ((false == isPlayerTwoServeStateOn && isPlayerTwoServing) ||
                    (isPlayerTwoServeStateOn && false == isPlayerTwoServing)) {
                // we are not showing the correst state for player one, correct this now
                TransitionDrawable background = (TransitionDrawable) playerTwoServeRadio.getBackground();
                background.reverseTransition(300);
                // and remember the state we are in
                isPlayerTwoServeStateOn = isPlayerTwoServing;
            }
        }
    }

    private void setTextSwitcherContent(String content, TextSwitcher view) {
        // get the current view and then the current text
        TextView currentTextView = (TextView) view.getCurrentView();
        String currentText = currentTextView.getText().toString();
        if (false == currentText.equals(content)) {
            // change this data
            view.setText(content);
        }
    }

    private void diplayScoreGameData(ScoreData scoreData) {
        // now do the games
        int playerOneGames = 0;
        int playerTwoGames = 0;
        int i = 0;
        for (i = 0; i < scoreData.previousSets.size() && i < 4; ++i) {
            Pair<Integer, Integer> gameResult = scoreData.previousSets.get(i);
            setTextSwitcherContent(Integer.toString(gameResult.first), playerOnePreviousSets[i]);
            setTextSwitcherContent(Integer.toString(gameResult.second), playerTwoPreviousSets[i]);
            if (null != playerOnePreviousGames[i]) {
                setTextSwitcherContent(Integer.toString(gameResult.first), playerOnePreviousGames[i]);
                setTextSwitcherContent(Integer.toString(gameResult.second), playerTwoPreviousGames[i]);
            }
            playerOneGames += gameResult.first;
            playerTwoGames += gameResult.second;
        }
        int visibility;
        boolean isSets = false;
        // show all the information about games if we are playing games
        switch (scoreData.currentScoreMode) {
            case K_SCOREWIMBLEDON5:
            case K_SCOREWIMBLEDON3:
                isSets = true;
            case K_SCOREBADMINTON3:
            case K_SCOREBADMINTON5:
            case K_SCOREFAST4:
                // for tennis and badminton, show the games stuff
                visibility = View.VISIBLE;
                break;
            default:
                // else do not (points)
                visibility = View.INVISIBLE;
                break;
        }
        // set this on all the relevant controls
        playerOneGamesText.setVisibility(visibility);
        playerTwoGamesText.setVisibility(visibility);
        gamesTitleText.setVisibility(visibility);

        if (scoreData.previousSets.size() >= 5 && isSets) {
            // the final set is the won result of the current one - just show in games
            Pair<Integer, Integer> gameResult = scoreData.previousSets.get(4);
            // set the current games
            setTextSwitcherContent(Integer.toString(gameResult.first), playerOneGamesText);
            setTextSwitcherContent(Integer.toString(gameResult.second), playerTwoGamesText);
        } else {
            // clear the results that remain in the text boxes from any previous matches
            for (; i < 4; ++i) {
                setTextSwitcherContent("", playerOnePreviousSets[i]);
                setTextSwitcherContent("", playerTwoPreviousSets[i]);
                if (null != playerOnePreviousGames[i]) {
                    setTextSwitcherContent("", playerOnePreviousGames[i]);
                    setTextSwitcherContent("", playerTwoPreviousGames[i]);
                }
            }
            // set the current games
            setTextSwitcherContent(Integer.toString(scoreData.games.first), playerOneGamesText);
            setTextSwitcherContent(Integer.toString(scoreData.games.second), playerTwoGamesText);
        }
    }

    private void displayScoreSetData(ScoreData scoreData) {
        // do the display of sets for players one and two
        int totalSets = scoreData.sets.first + scoreData.sets.second;
        // set the correct values on the text boxes
        setTextSwitcherContent(Integer.toString(scoreData.sets.first), playerOneSetsText);
        setTextSwitcherContent(Integer.toString(scoreData.sets.second), playerTwoSetsText);

        // show all the information about sets if we are playing sets
        int visibility;
        boolean isSets = false;
        String historyTitle = getResources().getString(R.string.games);
        switch (scoreData.currentScoreMode) {
            case K_SCOREWIMBLEDON5:
            case K_SCOREWIMBLEDON3:
                // show sets
                isSets = true;
                historyTitle = getResources().getString(R.string.sets);
            case K_SCOREBADMINTON3:
            case K_SCOREBADMINTON5:
            case K_SCOREFAST4:
                // modes one and two are wimbledon - show the sets stuff
                visibility = View.VISIBLE;
                break;
            default:
                // else do not
                visibility = View.INVISIBLE;
                break;
        }
        int setsVisibility = visibility;
        if (null == previousViewSwitcher) {
            // set the sets label
            setsTitleText.setText(historyTitle);
            String previousTitle = getResources().getString(R.string.previous_sets);
            if (false == isSets) {
                // replace the 'sets' in the title with 'games'
                previousTitle = previousTitle.replace(getResources().getString(R.string.sets), historyTitle);
                // and hide the sets controls
                setsVisibility = View.GONE;
            }
            previousSetsTitleText.setText(previousTitle);
        }
        else {
            // switch the view
            if (isSets) {
                // showing sets
                if (false == this.isSetsViewShown) {
                    previousViewSwitcher.showPrevious();
                }
            }
            else {
                if (this.isSetsViewShown) {
                    // don't want to show sets
                    previousViewSwitcher.showNext();
                }
            }
            this.isSetsViewShown = isSets;
        }
        // set this on all the relevant controls
        if (null != playerOneSetsLabelText) {
            animateVisibilityChange(playerOneSetsLabelText, visibility);
        }
        if (null != playerOneGamesLabelText) {
            animateVisibilityChange(playerOneGamesLabelText, visibility);
        }
        if (null != playerTwoSetsLabelText) {
            animateVisibilityChange(playerTwoSetsLabelText, visibility);
        }
        if (null != playerTwoGamesLabelText) {
            animateVisibilityChange(playerTwoGamesLabelText, visibility);
        }
        animateVisibilityChange(playerOneSetsText, setsVisibility);
        animateVisibilityChange(playerTwoSetsText, setsVisibility);
        animateVisibilityChange(setsTitleText, setsVisibility);
        animateVisibilityChange(previousSetsTitleText, visibility);
        animateVisibilityChange(previousGamesTitleText, visibility);
        // and the list of previous sets results labels
        for (int i = 0; i < 4; ++i) {
            animateVisibilityChange(playerOnePreviousSets[i], visibility);
            animateVisibilityChange(playerTwoPreviousSets[i], visibility);
            animateVisibilityChange(playerOnePreviousGames[i], visibility);
            animateVisibilityChange(playerTwoPreviousGames[i], visibility);
        }
    }

    private void animateVisibilityChange(final View view, final int visibility) {
        if (null != view && view.getVisibility() != visibility) {
            float targetAlpha;
            if (visibility == View.VISIBLE) {
                // want to show
                targetAlpha = 1f;
                // but we actually want to make it visible now to fade in
                view.setAlpha(0f);
                view.setVisibility(View.VISIBLE);
            }
            else {
                targetAlpha = 0f;
            }
            view.animate()
                    .alpha(targetAlpha)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            view.setVisibility(visibility);
                        }
                    });
        }
    }

    @Override
    public void onBtDeviceFound(BluetoothDevice device) {
        // found a device - so what...
    }

    @Override
    public void onBtStatusChanged() {
        // the state of the bluetooth connectivity just changed, update the display
        updateConnectionDisplay();
    }

    @Override
    public void onBtConnectionStatusChanged() {
        // the state of the bluetooth connectivity just changed, update the display
        updateConnectionDisplay();
    }

    @Override
    public void onBtDataChanged(final ScoreData scoreData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                displayScoreData(scoreData);
            }
        });
    }

    public void onUndoPress(View view) {
        BtManager.getManager().sendMessage("{a3}");
    }

    public void onNewMatchPress(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getResources().getString(R.string.warning));
        alert.setMessage(getResources().getString(R.string.sure_to_reset));
        alert.setPositiveButton(getResources().getString(R.string.yes), new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // command the score board to reset
                if (BtManager.getManager().sendMessage("{a9}")) {
                    // and reset our current match date
                    StorageManager.getManager().resetMatchStartedDate(0);
                }
                else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // show the error
                            Toast.makeText(ScoreActivity.this, R.string.unsuccessful_reset, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        alert.setNegativeButton(getResources().getString(R.string.no), new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // whatever
            }
        });
        alert.show();
    }

    public void onSaveMatchPress(View view) {
        ScoreData data = BtManager.getManager().getLatestScoreData();
        StorageManager manager = StorageManager.getManager();
        if (null == manager || null == manager.getCurrentUser()) {
            Toast.makeText(this, R.string.error_user_not_signedin, Toast.LENGTH_SHORT).show();
        }
        else if (null != data) {
            // there is data to store, store it, first work out what we would want to call this
            String playerOne = getOnlyStrings(playerOneTitleText.getText().toString());
            String playerTwo = getOnlyStrings(playerTwoTitleText.getText().toString());

            // get the match data for this
            Match match = new Match(manager.getCurrentUser(),
                    playerOne, playerTwo,
                    ScoreData.getScoreString(this, data),
                    data,
                    manager.getMatchStartedDate());
            // and store this match / will overwrite any old on this key of userId / match date
            match.updateInDatabase(StorageManager.getManager().getTopLevel());
            // and tell the user this worked
            Toast.makeText(this, R.string.successful_save, Toast.LENGTH_SHORT).show();
            //TODO could show a fun little animation of a file wizzing off here to show it worked
        }
    }

    @NonNull
    public static String getOnlyStrings(String s) {
        Pattern pattern = Pattern.compile("[^a-z A-Z0-9]");
        Matcher matcher = pattern.matcher(s);
        return matcher.replaceAll("").trim();
    }

}
