package uk.co.darkerwaters.scorepal.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextSwitcher;
import android.widget.TextView;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.ViewAnimator;
import uk.co.darkerwaters.scorepal.storage.ScoreData;
import uk.co.darkerwaters.scorepal.storage.StorageManager;

public class ScorePreviousSetsFragment extends Fragment implements StorageManager.IStorageManagerDataListener {
    private Activity parentContext = null;
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

        // and listen to changes in the storage of data
        StorageManager store = StorageManager.getManager();
        // and listen to changes in the storage of data
        store.registerListener(this);
        // display the correct titles for the players
        onPlayerTitlesUpdated(store.getCurrentPlayerOneTitle(), store.getCurrentPlayerTwoTitle());
        // and update the display to the latest score
        displayScoreData(store.getCurrentScoreData());

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // un-register us as a listener now are are destroyed
        StorageManager.getManager().unregisterListener(this);
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

    @Override
    public void onScoreDataUpdated(final ScoreData scoreData) {
        // this is interesting - show this score data
        this.parentContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                displayScoreData(scoreData);
            }
        });
    }

    private void displayScoreData(ScoreData scoreData) {
        // show the correct title for the previous scores (either sets or games)
        String historyTitle = getResources().getString(R.string.sets);
        String previousTitle = getResources().getString(R.string.previous_sets);
        // show all the information about games if we are playing games
        switch (scoreData.currentScoreMode) {
            case K_TENNIS:
                break;
            case K_BADMINTON:
            case K_POINTS:
                // show 'games' instead of 'sets'
                historyTitle = getResources().getString(R.string.games);
                // replace the 'sets' in the title with 'games'
                previousTitle = previousTitle.replace(getResources().getString(R.string.sets), historyTitle);
                break;
        }
        // show the games they have won on previous sets
        int i = 0;
        // show the data in the previous sets content of the scoreData
        for (i = 0; i < scoreData.previousSets.size() && i < 4; ++i) {
            Pair<Integer, Integer> gameResult = scoreData.previousSets.get(i);
            animator.setTextSwitcherContent(Integer.toString(gameResult.first), playerOnePreviousSets[i]);
            animator.setTextSwitcherContent(Integer.toString(gameResult.second), playerTwoPreviousSets[i]);
        }
        // clear the results that remain in the text boxes left over from any previous matches
        for (; i < 4; ++i) {
            animator.setTextSwitcherContent("", playerOnePreviousSets[i]);
            animator.setTextSwitcherContent("", playerTwoPreviousSets[i]);
        }
        // hide the final (number 4) set display if we are only going to play 3
        if (scoreData.currentScoreMode == ScoreData.ScoreMode.K_BADMINTON) {
            // we are playing 3 sets or games, no need for showing the final one then...
            playerOnePreviousSets[3].setVisibility(View.INVISIBLE);
            playerTwoPreviousSets[3].setVisibility(View.INVISIBLE);
        }
        else {
            // show the final one, might need it
            playerOnePreviousSets[3].setVisibility(View.VISIBLE);
            playerTwoPreviousSets[3].setVisibility(View.VISIBLE);
        }

        // set the correct values on the text boxes
        animator.setTextSwitcherContent(Integer.toString(scoreData.sets.first), playerOneSetsText);
        animator.setTextSwitcherContent(Integer.toString(scoreData.sets.second), playerTwoSetsText);

        // set the sets labels
        setsTitleText.setText(historyTitle);
        previousSetsTitleText.setText(previousTitle);
    }

    @Override
    public void onPlayerTitlesUpdated(String playerOneTitle, String playerTwoTitle) {
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
