package uk.co.darkerwaters.scorepal;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Format;
import java.util.Date;

import uk.co.darkerwaters.scorepal.bluetooth.BtManager;
import uk.co.darkerwaters.scorepal.history.HistoryFile;

public class HistoryDetailsActivity extends AppCompatActivity {
    private ScoreData scoreData = null;
    private String historyFileName = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_details);

        Bundle bundle = getIntent().getExtras();
        historyFileName = bundle.getString("uk.co.darkerwaters.scorepal.historyfilename");
        String historyFileContent = bundle.getString("uk.co.darkerwaters.scorepal.historyfile");

        if (historyFileContent == null) {
            // no data
            scoreData = new ScoreData();
        } else {
            // load in the data from the string
            scoreData = new ScoreData(new StringBuilder(historyFileContent));
        }

        // set the game summary
        populateGameSummary();
        // set the player titles
        populatePlayerTitles();
        // so load all the member variables and populate them with the data
        populateViewTitles();
        // populate the sets text
        populateScore();
        // populate total points
        populateTotalPointsHistory();
        // populate the sending button
        setupSendingButton();
    }

    private void setupSendingButton() {
        Button sendButton = (Button) findViewById(R.id.history_send_to_button);
        // are we connected to anything?
        BtManager manager = BtManager.getManager();
        if (null != scoreData.matchWinner || null == manager.getConnectedDevice()) {
            // are not connected or there is a winner and the match is over
            sendButton.setVisibility(View.GONE);
        }
        else {
            // the button will send the data so they players can finish their game
        }
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // string up the message to send
                BtManager manager = BtManager.getManager();
                String message = "{" + scoreData.toString() + "}";
                if (false == manager.sendMessage(message)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(HistoryDetailsActivity.this, R.string.failed_to_send_history_file, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void populateGameSummary() {
        TextView gameSummaryText = (TextView) findViewById(R.id.history_game_mode_summary);

        switch (HistoryFile.getGameMode(historyFileName)) {
            case K_SCOREWIMBLEDON5:
                gameSummaryText.setText(R.string.played_wimbledon5);
                break;
            case K_SCOREWIMBLEDON3:
                gameSummaryText.setText(R.string.played_wimbedon3);
                break;
            case K_SCOREBADMINTON3:
                gameSummaryText.setText(R.string.played_badminton3);
                break;
            case K_SCOREBADMINTON5:
                gameSummaryText.setText(R.string.played_badminton3);
                break;
            case K_SCOREFAST4:
                gameSummaryText.setText(R.string.played_fast_four);
                break;
            case K_SCOREPOINTS:
            default:
                gameSummaryText.setText(R.string.played_points);
                break;
        }
    }

    private void populatePlayerTitles() {
        TextView playerOneText = (TextView) findViewById(R.id.history_player_one_sets_label);
        TextView playerTwoText = (TextView) findViewById(R.id.history_player_two_sets_label);
        TextView playerOneGamesText = (TextView) findViewById(R.id.history_player_one_games_label);
        TextView playerTwoGamesText = (TextView) findViewById(R.id.history_player_two_games_label);
        TextView playerOnePointsText = (TextView) findViewById(R.id.history_player_one_points_label);
        TextView playerTwoPointsText = (TextView) findViewById(R.id.history_player_two_points_label);

        String playerOne = HistoryFile.getPlayerOne(this, historyFileName);
        String playerTwo = HistoryFile.getPlayerTwo(this, historyFileName);

        playerOneText.setText(playerOne);
        playerOnePointsText.setText(playerOne);

        playerTwoText.setText(playerTwo);
        playerTwoPointsText.setText(playerTwo);

        playerOneGamesText.setText(playerOne);
        playerTwoGamesText.setText(playerTwo);

        TextView gameWinnerText = (TextView) findViewById(R.id.history_winner_label);
        // is there a winner?
        if (null == scoreData.matchWinner) {
            // game was not completed
            gameWinnerText.setText(R.string.game_not_completed);
        }
        else {
            // show the winner
            String winner = scoreData.matchWinner == 0 ? playerOne : playerTwo;
            gameWinnerText.setText(winner + " " + getResources().getString(R.string.won));
        }
    }

    private void populateViewTitles() {
        ImageView imageView = (ImageView) findViewById(R.id.history_image);
        TextView namesText = (TextView) findViewById(R.id.history_names);
        TextView dateText = (TextView) findViewById(R.id.history_date);
        TextView scoreTypeText = (TextView) findViewById(R.id.history_score_type);
        TextView scoreText = (TextView) findViewById(R.id.history_score_result);

        String title = HistoryFile.getPlayerOne(this, historyFileName) +
            " " +
            getResources().getString(R.string.vs) +
            " " +
            HistoryFile.getPlayerTwo(this, historyFileName);
            namesText.setText(title);
        // set the nice image
        switch (HistoryFile.getGameMode(historyFileName)) {
            case K_SCOREWIMBLEDON5:
            case K_SCOREWIMBLEDON3:
            case K_SCOREFAST4:
                // this is a nice game of tennis
                imageView.setImageResource(R.drawable.tennis);
                break;
            case K_SCOREBADMINTON5:
            case K_SCOREBADMINTON3:
                // this is a nice game of badminton
                imageView.setImageResource(R.drawable.badminton);
                break;
            default:
                // this is something we score points in
                imageView.setImageResource(R.drawable.points);
                break;
        }
        Date datePlayed = HistoryFile.getDatePlayed(historyFileName);
        // get the date and time played to show this
        String datePlayedText;
        if (null == datePlayed) {
            datePlayedText = "Sorry, no data...";
        }
        else {
            Format dateFormat = DateFormat.getDateFormat(getApplicationContext());
            datePlayedText = dateFormat.format(datePlayed);

            Format timeFormat = DateFormat.getTimeFormat(getApplicationContext());
            datePlayedText += " " + timeFormat.format(datePlayed);
        }
        dateText.setText(datePlayedText);
        // get the score type to show this
        String scoreString = HistoryFile.getScoreString(this, historyFileName);
        // get the type
        scoreTypeText.setText(HistoryFile.getScoreStringType(this, scoreString));
        // and the actual points
        scoreText.setText(HistoryFile.getScoreStringPoints(this, scoreString));
    }

    private void populateScore() {
        TextView setsLabel = (TextView) findViewById(R.id.history_sets_label);
        TextView gamesLabel = (TextView) findViewById(R.id.history_games_label);
        TextView playerTwoLabel = (TextView) findViewById(R.id.history_player_one_sets_label);
        TextView playerOneLabel = (TextView) findViewById(R.id.history_player_two_sets_label);

        TextView playerOneSets = (TextView) findViewById(R.id.history_player_one_sets);
        TextView playerTwoSets = (TextView) findViewById(R.id.history_player_two_sets);

        TextView playerOneGames[] = new TextView[5];
        playerOneGames[0] = (TextView) findViewById(R.id.history_player_one_set_one);
        playerOneGames[1] = (TextView) findViewById(R.id.history_player_one_set_two);
        playerOneGames[2] = (TextView) findViewById(R.id.history_player_one_set_three);
        playerOneGames[3] = (TextView) findViewById(R.id.history_player_one_set_four);
        playerOneGames[4] = (TextView) findViewById(R.id.history_player_one_set_five);

        TextView playerTwoGames[] = new TextView[5];
        playerTwoGames[0] = (TextView) findViewById(R.id.history_player_two_set_one);
        playerTwoGames[1] = (TextView) findViewById(R.id.history_player_two_set_two);
        playerTwoGames[2] = (TextView) findViewById(R.id.history_player_two_set_three);
        playerTwoGames[3] = (TextView) findViewById(R.id.history_player_two_set_four);
        playerTwoGames[4] = (TextView) findViewById(R.id.history_player_two_set_five);

        int noSets = scoreData.sets.first + scoreData.sets.second;
        int noGames = scoreData.games.first + scoreData.games.second;
        boolean isSets = false;
        if (noSets > 0) {
            switch (scoreData.currentScoreMode) {
                case K_SCOREWIMBLEDON3:
                case K_SCOREWIMBLEDON5:
                    // this is tennis, leave the labels alone
                    isSets = true;
                    break;
                case K_SCOREBADMINTON3:
                case K_SCOREBADMINTON5:
                case K_SCOREFAST4:
                    // this is badminton (or fast4 - no sets), the sets is the number of games
                    setsLabel.setText(R.string.games);
                    // and the games label is the hisory of points
                    gamesLabel.setText(R.string.game_points);
                    break;
                default:
                    // leave the labels alone
                    break;
            }
            // setup the view to show the sets
            playerOneSets.setText(Integer.toString(scoreData.sets.first));
            playerTwoSets.setText(Integer.toString(scoreData.sets.second));
            for (int i = 0; i < 5; ++i) {
                if (i < scoreData.previousSets.size()) {
                    Pair<Integer, Integer> pair = scoreData.previousSets.get(i);
                    // show the result for the set
                    playerOneGames[i].setText(Integer.toString(pair.first));
                    playerTwoGames[i].setText(Integer.toString(pair.second));
                }
                else if (scoreData.previousSets.size() == i && isSets) {
                    // show the final games result
                    playerOneGames[i].setText(Integer.toString(scoreData.games.first));
                    playerTwoGames[i].setText(Integer.toString(scoreData.games.second));
                }
                else {
                    // no data for this box
                    playerOneGames[i].setVisibility(View.INVISIBLE);
                    playerTwoGames[i].setVisibility(View.INVISIBLE);
                }
            }
        }
        else if (noGames > 0) {
            // setup the view to show the games, hide the sets controls
            setsLabel.setText(R.string.games);
            playerOneSets.setText(Integer.toString(scoreData.games.first));
            playerTwoSets.setText(Integer.toString(scoreData.games.second));
            // now show the points for each game won in the history boxes, these results
            // are in the previous sets structure
            // but this isn't showing games now, it is showing points instead
            gamesLabel.setText(R.string.points);
            for (int i = 0; i < 5; ++i) {
                if (i < scoreData.previousSets.size()) {
                    Pair<Integer, Integer> pair = scoreData.previousSets.get(i);
                    // show the result for the set
                    playerOneGames[i].setText(Integer.toString(pair.first));
                    playerTwoGames[i].setText(Integer.toString(pair.second));
                }
                else if (scoreData.previousSets.size() == i) {
                    // show the final game's result
                    playerOneGames[i].setText(Integer.toString(scoreData.points.first));
                    playerTwoGames[i].setText(Integer.toString(scoreData.points.second));
                }
                else {
                    // no data for this box
                    playerOneGames[i].setVisibility(View.INVISIBLE);
                    playerTwoGames[i].setVisibility(View.INVISIBLE);
                }
            }
        }
        else {
            // setup the view to show points only
            setsLabel.setText(R.string.points);
            playerOneSets.setText(Integer.toString(scoreData.points.first));
            playerTwoSets.setText(Integer.toString(scoreData.points.second));
            // and hide the history of the games / sets
            findViewById(R.id.history_include_games_layout).setVisibility(View.GONE);
        }
    }

    private void populateTotalPointsHistory() {
        TextView playerOneTotalPoints = (TextView) findViewById(R.id.history_player_one_total_points);
        TextView playerTwoTotalPoints = (TextView) findViewById(R.id.history_player_two_total_points);

        playerOneTotalPoints.setText(Integer.toString(scoreData.totalPoints.first));
        playerTwoTotalPoints.setText(Integer.toString(scoreData.totalPoints.second));

        if (scoreData.points.first == scoreData.totalPoints.first &&
                scoreData.points.second == scoreData.totalPoints.second) {
            // no difference in points and total points...
            findViewById(R.id.history_include_points_layout).setVisibility(View.GONE);
        }
        //TODO the nice graph of points history to the right of this summary of the score
    }
}
