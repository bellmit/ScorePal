package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ViewGroup;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentScore;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentScoreSquash;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentTime;
import uk.co.darkerwaters.scorepal.application.GamePlayCommunicator;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.base.MatchMessage;
import uk.co.darkerwaters.scorepal.score.base.Sport;
import uk.co.darkerwaters.scorepal.score.squash.SquashMatch;
import uk.co.darkerwaters.scorepal.score.squash.SquashScore;

public class SquashPlayActivity extends PlayMatchActivity {

    private FragmentScoreSquash scoreFragment;
    private FragmentTime timeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_squash_play);

        // initialise our activity
        setupActivity(Sport.SQUASH, (ViewGroup)findViewById(R.id.main_layout));
    }

    @Override
    protected Fragment[] createMatchFragments() {
        // create the fragments here
        if (null == this.scoreFragment) {
            this.scoreFragment = new FragmentScoreSquash();
        }
        if  (null == this.timeFragment) {
            this.timeFragment = new FragmentTime();
        }
        // return our fragments as a list to the base
        return new Fragment[] {
                this.scoreFragment,
                this.timeFragment
        };
    }

    @Override
    protected boolean setupActivity(Sport sport, ViewGroup mainLayout) {
        // first things first - have we a tennis match to play?
        GamePlayCommunicator communicator = GamePlayCommunicator.ActivateCommunicator(this);
        Match currentMatch = communicator.getCurrentMatch();
        if (false == currentMatch instanceof SquashMatch) {
            // the current match is not a squash match, so we are responsible for starting
            // a new one now we are here not having had one made for us
            communicator.sendRequest(MatchMessage.CREATE_MATCH);
        }
        // and let the base initialise this
        return super.setupActivity(sport, mainLayout);
    }

    @Override
    protected void updateActiveFragment(int index) {
        switch (index) {
            case 0:
                showActiveScore();
                break;
            case 1:
                onTimeChanged();
                break;
        }
    }

    @Override
    public void onAttachFragment(FragmentScore fragment) {
        super.onAttachFragment(fragment);
        this.scoreFragment = (FragmentScoreSquash) fragment;
    }

    @Override
    protected void showActiveScore() {
        // and update the score on the controls
        if (null != this.scoreFragment) {
            Match currentMatch = this.communicator.getCurrentMatch();
            Team teamOne = currentMatch.getTeamOne();
            Team teamTwo = currentMatch.getTeamTwo();

            // get the score
            SquashScore score = (SquashScore) currentMatch.getScore();
            // set the score
            this.scoreFragment.setPointsValue(0, score.getDisplayPoint(teamOne));
            this.scoreFragment.setGamesValue(0, score.getDisplayGame(teamOne).displayString(this));

            this.scoreFragment.setPointsValue(1, score.getDisplayPoint(teamTwo));
            this.scoreFragment.setGamesValue(1, score.getDisplayGame(teamTwo).displayString(this));
        }
        // and let the base
        super.showActiveScore();
    }
}
