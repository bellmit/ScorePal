package uk.co.darkerwaters.scorepal.ui.matchplay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.application.MatchService;
import uk.co.darkerwaters.scorepal.data.Match;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.data.Score;
import uk.co.darkerwaters.scorepal.data.ScoreState;
import uk.co.darkerwaters.scorepal.ui.matchlists.CardMatchRecyclerAdapter;
import uk.co.darkerwaters.scorepal.ui.matchlists.MatchHistoryViewAdapter;
import uk.co.darkerwaters.scorepal.ui.matchresults.FragmentMatchResults;

public class FragmentPlayHistory extends Fragment implements Match.MatchListener<Score>, CardMatchRecyclerAdapter.MatchFileListener {

    private RecyclerView recyclerView;
    private MatchHistoryViewAdapter historyViewAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Match activeMatch;

    protected FloatingActionButton undoButton;

    private FragmentMatchResults resultsFragment;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_play_history, container, false);

        // setup the action button (can undo the points from here)
        undoButton = root.findViewById(R.id.undoLastButton);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                undoLastPoint();
            }
        });

        // find the history view and set this up
        recyclerView = root.findViewById(R.id.recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter with the data - arranged by history
        historyViewAdapter = new MatchHistoryViewAdapter();
        recyclerView.setAdapter(historyViewAdapter);

        // and return this created view
        return root;
    }

    private void undoLastPoint() {
        if (null != activeMatch) {
            // undo the last point on the score, will cause a message to be sent to update
            activeMatch.undoLastPoint();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // when we show this, the list of history items might not be labelled - only loaded
        // rather than reconstructed. To fix this we can simply reconstruct the score
        MatchService service = MatchService.GetRunningService();
        if (null != service) {
            activeMatch = service.getActiveMatch();
            if (null != activeMatch) {
                activeMatch.addListener(this);
                activeMatch.getSetup().informMatchSetupChanged(MatchSetup.SetupChange.POINTS_STRUCTURE);
            }
        }
        else {
            activeMatch = null;
        }

        if (null != activeMatch) {
            // so we can get the fragment from this and replace the blank entry
            resultsFragment = activeMatch.getSport().newResultsFragment(false);
            // and add this to the container where we want it
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            boolean isAddFragment = true;
            for (Fragment currentFragment : fragmentManager.getFragments()) {
                if (false == currentFragment instanceof NavHostFragment && currentFragment != this) {
                    // this is a setup fragment - in the settings, remove this
                    fragmentTransaction.remove(currentFragment);
                } else {
                    Log.debug("not removing " + currentFragment.getClass().toString());
                }
            }
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            if (isAddFragment) {
                fragmentTransaction.add(R.id.scoreSummaryLayout, resultsFragment);
            } else {
                fragmentTransaction.attach(resultsFragment);
            }
            fragmentTransaction.commit();
            // setup our data on this fragment
            resultsFragment.setMatchData(activeMatch, this);
            // hide all the extras - just want to show the current score
            resultsFragment.hideMoreSection();
        }

        // and put the data in a list
        updateListContents();
    }

    private void updateListContents() {
        // now we can set the new history values into the list
        historyViewAdapter.setHistoryValues(activeMatch);
        historyViewAdapter.notifyDataSetChanged();
        if (null != resultsFragment && resultsFragment.getIsFragmentCreated()) {
            // now we are added, we need to initialise the data here too
            resultsFragment.showMatchData();
        }
    }

    @Override
    public void onPause() {
        if (null != activeMatch) {
            activeMatch.removeListener(this);
            activeMatch = null;
        }
        super.onPause();
    }

    @Override
    public void onMatchStateChanged(Score score, ScoreState state) {
        // the state of the match changed, update our list
        updateListContents();
    }

    @Override
    public void deleteMatchFile(Match match) {
        // we are playing a match - don't let them delete
    }

    @Override
    public void shareMatchFile(Match match) {
        // we are playing a match - don't let them share
    }

    @Override
    public void hideMatchFile(Match loadedMatch) {
        // we are playing a match - don't let them hide
    }

    @Override
    public void restoreMatchFile(Match loadedMatch) {
        // we are playing a match - don't let them play
    }
}
