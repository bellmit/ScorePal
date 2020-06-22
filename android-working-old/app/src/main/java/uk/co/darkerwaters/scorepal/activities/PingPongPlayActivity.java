package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ViewGroup;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentScore;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentScorePingPong;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentTime;
import uk.co.darkerwaters.scorepal.application.GamePlayCommunicator;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.base.MatchMessage;
import uk.co.darkerwaters.scorepal.score.base.Sport;
import uk.co.darkerwaters.scorepal.score.pingpong.PingPongMatch;
import uk.co.darkerwaters.scorepal.score.pingpong.PingPongScore;

public class PingPongPlayActivity extends PlayMatchActivity {

    private FragmentScorePingPong scoreFragment;
    private FragmentTime timeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ping_pong_play);

        // initialise our activity
        setupActivity(Sport.PING_PONG, (ViewGroup)findViewById(R.id.main_layout));
    }

    @Override
    protected Fragment[] createMatchFragments() {
        // create the fragments here
        if (null == this.scoreFragment) {
            this.scoreFragment = new FragmentScorePingPong();
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
        if (false == currentMatch instanceof PingPongMatch) {
            // the current match is not a pingPong match, so we are responsible for starting
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
        this.scoreFragment = (FragmentScorePingPong) fragment;
    }

    @Override
    protected void showActiveScore() {
        // and update the score on the controls
        if (null != this.scoreFragment) {
            Match currentMatch = this.communicator.getCurrentMatch();
            Team teamOne = currentMatch.getTeamOne();
            Team teamTwo = currentMatch.getTeamTwo();

            // get the score
            PingPongScore score = (PingPongScore) currentMatch.getScore();
            // set the score
            this.scoreFragment.setPointsValue(0, score.getDisplayPoint(teamOne));
            this.scoreFragment.setGamesValue(0, score.getDisplayRound(teamOne).displayString(this));

            this.scoreFragment.setPointsValue(1, score.getDisplayPoint(teamTwo));
            this.scoreFragment.setGamesValue(1, score.getDisplayRound(teamTwo).displayString(this));
        }
        // and let the base
        super.showActiveScore();
    }
}
