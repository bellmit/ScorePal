package uk.co.darkerwaters.scorepal.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextSwitcher;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.ViewAnimator;
import uk.co.darkerwaters.scorepal.bluetooth.BtManager;
import uk.co.darkerwaters.scorepal.storage.ScoreData;

public class ScorePreviousSetsFragment extends Fragment {
    private Context parentContext = null;
    private ViewAnimator animator;

    private TextSwitcher playerOneSetsText;
    private TextSwitcher playerTwoSetsText;

    private TextView playerOneSetsLabelText;
    private TextView playerTwoSetsLabelText;

    private TextView setsTitleText;
    private TextView previousSetsTitleText;

    private TextSwitcher[] playerOnePreviousSets = new TextSwitcher[4];
    private TextSwitcher[] playerTwoPreviousSets = new TextSwitcher[4];


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_previous_sets, container, false);

        // find all the members on this fragment
        playerOneSetsText = (TextSwitcher) view.findViewById(R.id.player_one_sets_text);
        animator.setTextSwitcherFactories(playerOneSetsText, animator.scoreViewFactory);
        playerTwoSetsText = (TextSwitcher) view.findViewById(R.id.player_two_sets_text);
        animator.setTextSwitcherFactories(playerTwoSetsText, animator.scoreViewFactory);

        playerOneSetsLabelText = (TextView) view.findViewById(R.id.player_one_sets_label_text);
        playerTwoSetsLabelText = (TextView) view.findViewById(R.id.player_two_sets_label_text);

        setsTitleText = (TextView) view.findViewById(R.id.sets_text);
        previousSetsTitleText = (TextView) view.findViewById(R.id.previous_sets_text);

        // also do the player one and two previous sets display boxes
        playerOnePreviousSets[0] = (TextSwitcher) view.findViewById(R.id.player_one_set_one_text);
        animator.setTextSwitcherFactories(playerOnePreviousSets[0], animator.scoreViewFactory);
        playerOnePreviousSets[1] = (TextSwitcher) view.findViewById(R.id.player_one_set_two_text);
        animator.setTextSwitcherFactories(playerOnePreviousSets[1], animator.scoreViewFactory);
        playerOnePreviousSets[2] = (TextSwitcher) view.findViewById(R.id.player_one_set_three_text);
        animator.setTextSwitcherFactories(playerOnePreviousSets[2], animator.scoreViewFactory);
        playerOnePreviousSets[3] = (TextSwitcher) view.findViewById(R.id.player_one_set_four_text);
        animator.setTextSwitcherFactories(playerOnePreviousSets[3], animator.scoreViewFactory);

        playerTwoPreviousSets[0] = (TextSwitcher) view.findViewById(R.id.player_two_set_one_text);
        animator.setTextSwitcherFactories(playerTwoPreviousSets[0], animator.scoreViewFactory);
        playerTwoPreviousSets[1] = (TextSwitcher) view.findViewById(R.id.player_two_set_two_text);
        animator.setTextSwitcherFactories(playerTwoPreviousSets[1], animator.scoreViewFactory);
        playerTwoPreviousSets[2] = (TextSwitcher) view.findViewById(R.id.player_two_set_three_text);
        animator.setTextSwitcherFactories(playerTwoPreviousSets[2], animator.scoreViewFactory);
        playerTwoPreviousSets[3] = (TextSwitcher) view.findViewById(R.id.player_two_set_four_text);
        animator.setTextSwitcherFactories(playerTwoPreviousSets[3], animator.scoreViewFactory);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        // remember the context we attach to (or owning Activity)
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            parentContext = (Activity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement Activity");
        }
        // can create the animator here
        animator = new ViewAnimator(context);
        // and let the base have a go
        super.onAttach(context);
    }

    public void displayScoreData(ScoreData scoreData) {
        // show the winner if there is one, hiding a load of other stuff to stop interactions
        if (null != scoreData.matchWinner) {
            // a won match with set data is special, the final games score goes in the games
            // boxes with the previous sets not showing the final set's points
            switch(scoreData.currentScoreMode) {
                case K_SCOREWIMBLEDON3:
                case K_SCOREWIMBLEDON5:
                    int noSets = scoreData.previousSets.size();
                    if (noSets > 0 && scoreData.sets.first + scoreData.sets.second > 0) {
                        // if the final set is not number 5 (not shown) then clear the boxes
                        if (noSets < 5) {
                            playerOnePreviousSets[noSets - 1].setText("");
                            playerTwoPreviousSets[noSets - 1].setText("");
                        }
                    }
                    break;
            }
        }

        // now do the games
        int playerOneGames = 0;
        int playerTwoGames = 0;
        int i = 0;
        for (i = 0; i < scoreData.previousSets.size() && i < 4; ++i) {
            Pair<Integer, Integer> gameResult = scoreData.previousSets.get(i);
            animator.setTextSwitcherContent(Integer.toString(gameResult.first), playerOnePreviousSets[i]);
            animator.setTextSwitcherContent(Integer.toString(gameResult.second), playerTwoPreviousSets[i]);
            // keep a running total of this
            playerOneGames += gameResult.first;
            playerTwoGames += gameResult.second;
        }
        boolean isSets = false;
        // show all the information about games if we are playing games
        switch (scoreData.currentScoreMode) {
            case K_SCOREWIMBLEDON5:
            case K_SCOREWIMBLEDON3:
                isSets = true;
                break;
        }

        if (false == isSets || scoreData.previousSets.size() < 5) {
            // clear the results that remain in the text boxes from any previous matches
            for (; i < 4; ++i) {
                animator.setTextSwitcherContent("", playerOnePreviousSets[i]);
                animator.setTextSwitcherContent("", playerTwoPreviousSets[i]);
            }
        }

        // do the display of sets for players one and two
        int totalSets = scoreData.sets.first + scoreData.sets.second;
        // set the correct values on the text boxes
        animator.setTextSwitcherContent(Integer.toString(scoreData.sets.first), playerOneSetsText);
        animator.setTextSwitcherContent(Integer.toString(scoreData.sets.second), playerTwoSetsText);

        // show all the information about sets if we are playing sets
        int visibility;
        String historyTitle = getResources().getString(R.string.games);
        switch (scoreData.currentScoreMode) {
            case K_SCOREWIMBLEDON5:
            case K_SCOREWIMBLEDON3:
                // show sets
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
        // set the sets label
        setsTitleText.setText(historyTitle);
        String previousTitle = getResources().getString(R.string.previous_sets);
        if (false == isSets) {
            // replace the 'sets' in the title with 'games'
            previousTitle = previousTitle.replace(getResources().getString(R.string.sets), historyTitle);
            //TODO hide the sets controls when showing just games data
        }
        previousSetsTitleText.setText(previousTitle);
    }

    public void updatePlayerTitles(String playerOneTitle, String playerTwoTitle) {
        // update our titles
        if (null != playerOneSetsLabelText) {
            playerOneSetsLabelText.setText(playerOneTitle);
        }
        // and do player two
        if (null != playerTwoSetsLabelText) {
            playerTwoSetsLabelText.setText(playerTwoTitle);
        }
    }
}
