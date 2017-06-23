package uk.co.darkerwaters.scorepal.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import uk.co.darkerwaters.scorepal.activities.DeviceScoreActivity;
import uk.co.darkerwaters.scorepal.bluetooth.BtManager;
import uk.co.darkerwaters.scorepal.storage.ScoreData;

public class ScoreEntryFragment extends Fragment {
    private Context parentContext = null;
    private ViewAnimator animator;

    private TextView winnerText;
    private TextView gamesTitleText;

    private EditText playerOneTitleText;
    private EditText playerTwoTitleText;

    private TextSwitcher playerOneGamesText;
    private TextSwitcher playerTwoGamesText;

    private TextSwitcher playerOnePointsText;
    private TextSwitcher playerTwoPointsText;

    private View playerOneServeRadio;
    private View playerTwoServeRadio;

    private boolean isPlayerOneServeStateOn = false;
    private boolean isPlayerTwoServeStateOn = false;

    public interface IScoreEntryFragmentListener {
        void onPlayerTitlesUpdated(String playerOneTitle, String playerTwoTitle);
    }

    private IScoreEntryFragmentListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_score_entry, container, false);

        // find all the members on this fragment
        winnerText = (TextView) view.findViewById(R.id.winner_text_view);
        gamesTitleText = (TextView) view.findViewById(R.id.games_text);

        playerOneTitleText = (EditText) view.findViewById(R.id.player_one_name_text_view);
        playerTwoTitleText = (EditText) view.findViewById(R.id.player_two_name_text_view);

        playerOnePointsText = (TextSwitcher) view.findViewById(R.id.player_one_points_text);
        // Set the ViewFactory of the TextSwitcher that will create TextView object when asked
        animator.setTextSwitcherFactories(playerOnePointsText, animator.pointViewFactory);

        playerTwoPointsText = (TextSwitcher) view.findViewById(R.id.player_two_points_text);
        // Set the ViewFactory of the TextSwitcher that will create TextView object when asked
        animator.setTextSwitcherFactories(playerTwoPointsText, animator.pointViewFactory);

        playerOneServeRadio = (View) view.findViewById(R.id.player_one_serve_signal);
        playerTwoServeRadio = (View) view.findViewById(R.id.player_two_serve_signal);

        playerOneGamesText = (TextSwitcher) view.findViewById(R.id.player_one_games_text);
        animator.setTextSwitcherFactories(playerOneGamesText, animator.scoreViewFactory);
        playerTwoGamesText = (TextSwitcher) view.findViewById(R.id.player_two_games_text);
        animator.setTextSwitcherFactories(playerTwoGamesText, animator.scoreViewFactory);

        // now we can initialise the user interactions now all the components are filled
        initialiseUserInteractions();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        // remember the context we attach to (or owning Activity)
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            parentContext = (Activity) context;
            listener = (IScoreEntryFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement IScoreEntryFragmentListener and Activity");
        }
        // can create the animator here
        animator = new ViewAnimator(context);
        // and let the base have a go
        super.onAttach(context);
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
                listener.onPlayerTitlesUpdated(getPlayerOneTitle(), getPlayerTwoTitle());
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
                listener.onPlayerTitlesUpdated(getPlayerOneTitle(), getPlayerTwoTitle());
            }
        });
    }

    public void displayScoreData(ScoreData scoreData) {
        // show the points in the points views
        animator.setTextSwitcherContent(scoreData.getPointsAsString(scoreData.points.first), playerOnePointsText);
        animator.setTextSwitcherContent(scoreData.getPointsAsString(scoreData.points.second), playerTwoPointsText);
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

        // show the winner if there is one, hiding a load of other stuff to stop interactions
        int visibility;
        if (null != scoreData.matchWinner) {
            winnerText.setVisibility(View.VISIBLE);
            visibility = View.INVISIBLE;
            String title = (scoreData.matchWinner.intValue() == 0 ?
                    getPlayerOneTitle() : getPlayerTwoTitle());
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
                    }
                    break;
                case K_SCOREBADMINTON5:
                    // in a 5 game of badminton there is no place to put the final score, so
                    // leave it in the points display
                    int noGames = scoreData.previousSets.size();
                    if (noGames > 0 && scoreData.sets.first + scoreData.sets.second > 0) {
                        // they are playing sets - show the games for the last set in the games boxes
                        int playerOneFinalSetGames = scoreData.previousSets.get(noGames - 1).first;
                        int playerTwoFinalSetGames = scoreData.previousSets.get(noGames - 1).second;
                        playerOnePointsText.setCurrentText(Integer.toString(playerOneFinalSetGames));
                        playerTwoPointsText.setCurrentText(Integer.toString(playerTwoFinalSetGames));
                        // and show the controls
                        visibility = View.VISIBLE;
                    }
                    break;
            }
        } else {
            // hide the winning text and show the points
            winnerText.setVisibility(View.INVISIBLE);
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
            animator.setTextSwitcherContent(Integer.toString(gameResult.first), playerOneGamesText);
            animator.setTextSwitcherContent(Integer.toString(gameResult.second), playerTwoGamesText);
        }
        else {
            // set the current games
            animator.setTextSwitcherContent(Integer.toString(scoreData.games.first), playerOneGamesText);
            animator.setTextSwitcherContent(Integer.toString(scoreData.games.second), playerTwoGamesText);
        }
    }

    private boolean addPoint(int player) {
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
        return result;
    }

    public void setTitles(String playerOneTitle, String playerTwoTitle) {
        // set the titles on these controls, get the titles as cleaned strings
        playerOneTitle = getOnlyStrings(playerOneTitle);
        playerTwoTitle = getOnlyStrings(playerTwoTitle);
        // set the text
        playerOneTitleText.setText(playerOneTitle);
        playerTwoTitleText.setText(playerTwoTitle);
        // update any listeners
        listener.onPlayerTitlesUpdated(playerOneTitle, playerTwoTitle);
    }

    public String getPlayerOneTitle() {
        String playerTitle = getOnlyStrings(playerOneTitleText.getText().toString());
        if (null == playerTitle || playerTitle.isEmpty()) {
            playerTitle = getResources().getString(R.string.player_one);
        }
        return playerTitle;
    }

    public String getPlayerTwoTitle() {
        String playerTitle = getOnlyStrings(playerTwoTitleText.getText().toString());
        if (null == playerTitle || playerTitle.isEmpty()) {
            playerTitle = getResources().getString(R.string.player_two);
        }
        return playerTitle;
    }

    @NonNull
    public static String getOnlyStrings(String s) {
        Pattern pattern = Pattern.compile("[^a-z A-Z0-9]");
        Matcher matcher = pattern.matcher(s);
        return matcher.replaceAll("").trim();
    }
}
