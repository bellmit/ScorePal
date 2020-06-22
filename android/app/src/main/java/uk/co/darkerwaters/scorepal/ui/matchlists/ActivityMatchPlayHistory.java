package uk.co.darkerwaters.scorepal.ui.matchlists;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.MatchService;
import uk.co.darkerwaters.scorepal.data.Match;
import uk.co.darkerwaters.scorepal.data.Score;
import uk.co.darkerwaters.scorepal.data.ScoreState;
import uk.co.darkerwaters.scorepal.ui.matchresults.ActivityMatch;

public class ActivityMatchPlayHistory extends ActivityMatch implements Match.MatchListener<Score> {

    private RecyclerView recyclerView;
    private MatchHistoryViewAdapter historyViewAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Match.MatchListener<Score> redoMatchListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_play_history);

        // set the title of this
        setTitle(R.string.playHistoryTitle);

        // find the history view and set this up
        recyclerView = findViewById(R.id.recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter with the data - arranged by history
        historyViewAdapter = new MatchHistoryViewAdapter();
        recyclerView.setAdapter(historyViewAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        // when we show this, the list of history items might not be labelled - only loaded
        // rather than reconstructed. To fix this we can simply reconstruct the score
        if (null != activeMatch) {
            activeMatch.addListener(this);
            // and create the score redo listener to populate the stack as it is refreshed
            if (null == MatchService.GetRunningService()) {
                // there is no service doing our good work, let's do it ourselves here instead
                redoMatchListener = MatchService.CreateMatchRedoHandler(this, activeMatch);
            }
            activeMatch.applyChangedMatchSettings();
        }
        // and put the data in a list
        updateListContents();
    }

    private void updateListContents() {
        // now we can set the new history values into the list
        historyViewAdapter.setHistoryValues(activeMatch);
        historyViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        if (null != activeMatch) {
            activeMatch.removeListener(this);
            if (null != redoMatchListener) {
                activeMatch.removeListener(redoMatchListener);
                redoMatchListener = null;
            }
        }
        super.onPause();
    }

    @Override
    public void onMatchStateChanged(Score score, ScoreState state) {
        // the state of the match changed, update our list
        if (!state.isChanged(ScoreState.ScoreChange.INCREMENT_REDO)) {
            // this is not part of our redo refresh
            updateListContents();
        }
    }
}
