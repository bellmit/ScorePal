package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ViewGroup;

import uk.co.darkerwaters.scorepal.activities.fragments.FragmentScore;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentTime;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentPreviousSets;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentScoreTennis;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.GamePlayCommunicator;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.base.MatchMessage;
import uk.co.darkerwaters.scorepal.score.base.Sport;
import uk.co.darkerwaters.scorepal.score.tennis.TennisMatch;
import uk.co.darkerwaters.scorepal.score.tennis.TennisScore;

public class TennisPlayActivity extends PlayMatchActivity implements
        FragmentPreviousSets.FragmentPreviousSetsInteractionListener {

    private FragmentPreviousSets previousSetsFragment;
    private FragmentScoreTennis scoreFragment;
    private FragmentTime timeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tennis_play);

        // initialise our activity
        setupActivity(Sport.TENNIS, (ViewGroup)findViewById(R.id.main_layout));
    }

    @Override
    protected Fragment[] createMatchFragments() {
        // create the fragments here
        if (null == this.previousSetsFragment) {
            this.previousSetsFragment = new FragmentPreviousSets();
        }
        if (null == this.scoreFragment) {
            this.scoreFragment = new FragmentScoreTennis();
        }
        if  (null == this.timeFragment) {
            this.timeFragment = new FragmentTime();
        }
        // return our fragments as a list to the base
        return new Fragment[] {
                this.scoreFragment,
                this.previousSetsFragment,
                this.timeFragment
        };
    }

    @Override
    protected boolean setupActivity(Sport sport, ViewGroup mainLayout) {
        // first things first - have we a tennis match to play?
        GamePlayCommunicator communicator = GamePlayCommunicator.ActivateCommunicator(this);
        Match currentMatch = communicator.getCurrentMatch();
        if (false == currentMatch instanceof TennisMatch) {
            // the current match is not a tennis match, so we are responsible for starting
            // a new one now we are here not having had one made for us
            communicator.sendRequest(MatchMessage.CREATE_MATCH);
        }
        // and let the base initialise this
        return super.setupActivity(sport, mainLayout);
    }

    @Override
    public void onAttachFragment(FragmentPreviousSets fragment) {
        this.previousSetsFragment = fragment;
    }

    @Override
    public void onAttachFragment(FragmentScore fragment) {
        super.onAttachFragment(fragment);
        this.scoreFragment = (FragmentScoreTennis) fragment;
    }

    @Override
    protected void updateActiveFragment(int index) {
        switch (index) {
            case 0:
                showActiveScore();
                break;
            case 1:
                showActivePreviousSets();
                break;
            case 2:
                onTimeChanged();
                break;
        }
    }

    @Override
    protected void showActiveScore() {
        // and update the score on the controls
        if (null != this.scoreFragment) {
            Match currentMatch = this.communicator.getCurrentMatch();
            if (null != currentMatch) {
                Team teamOne = currentMatch.getTeamOne();
                Team teamTwo = currentMatch.getTeamTwo();

                // get the score
                TennisScore score = (TennisScore) currentMatch.getScore();
                // set the sets - just numbers
                this.scoreFragment.setSetValue(0, Integer.toString(score.getSets(teamOne)));
                this.scoreFragment.setSetValue(1, Integer.toString(score.getSets(teamTwo)));
                // and the games
                this.scoreFragment.setGamesValue(0, Integer.toString(score.getGames(teamOne, -1)));
                this.scoreFragment.setGamesValue(1, Integer.toString(score.getGames(teamTwo, -1)));
                // and the points
                this.scoreFragment.setPointsValue(0, score.getDisplayPoint(teamOne));
                this.scoreFragment.setPointsValue(1, score.getDisplayPoint(teamTwo));
            }
        }
        // and let the base
        super.showActiveScore();
    }

    private void showActivePreviousSets() {
        if (null != this.previousSetsFragment) {
            Match currentMatch = this.communicator.getCurrentMatch();
            // and update the score on the controls
            TennisScore score = (TennisScore) currentMatch.getScore();
            Team teamOne = currentMatch.getTeamOne();
            Team teamTwo = currentMatch.getTeamTwo();
            // want to do the previous sets
            for (int i = 0; i < score.getPlayedSets(); ++i) {
                int gamesOne = score.getGames(teamOne, i);
                int gamesTwo = score.getGames(teamTwo, i);
                this.previousSetsFragment.setSetValue(0, i, gamesOne, gamesOne > gamesTwo);
                this.previousSetsFragment.setSetValue(1, i, gamesTwo, gamesTwo > gamesOne);
                if (score.isSetTieBreak(i)) {
                    // this set is / was a tie, show the score of this in brackets
                    int[] tiePoints = score.getPoints(i, gamesOne + gamesTwo - 1);
                    this.previousSetsFragment.setTieBreakResult(i, tiePoints[0], tiePoints[1]);
                }
            }
        }
    }
}
