package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentScore;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentScorePoints;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentTime;
import uk.co.darkerwaters.scorepal.activities.handlers.DepthPageTransformer;
import uk.co.darkerwaters.scorepal.activities.handlers.ScreenSliderPagerAdapter;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.base.MatchMessage;
import uk.co.darkerwaters.scorepal.score.base.Sport;

public abstract class PlayMatchActivity extends PlayTeamActivity implements
        FragmentScorePoints.FragmentScoreInteractionListener,
        FragmentTime.FragmentTimeInteractionListener {

    private ViewPager scorePager;
    private PagerAdapter pagerAdapter;

    private Fragment[] matchFragments;

    private ImageView pageRight;
    private ImageView pageLeft;

    private FragmentScore scoreFragment;
    private FragmentTime timeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tennis_play);
    }

    protected boolean setupActivity(Sport sport, ViewGroup mainLayout) {
        // before we do anything, check to see if the match is correctly initialised
        if (super.setupActivity(sport.titleResId, mainLayout)) {
            // setup all the controls on the base
            setupPlayControls(sport, mainLayout);

            // now we can do ours too
            this.pageLeft = findViewById(R.id.viewPageLeftButton);
            this.pageRight = findViewById(R.id.viewPageRightButton);

            this.pageLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    changeScorePagerPage(-1);
                }
            });
            this.pageRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    changeScorePagerPage(+1);
                }
            });

            // create the fragments to show
            this.matchFragments = createMatchFragments();

            // Instantiate a ViewPager and a PagerAdapter to transition between scores
            this.scorePager = findViewById(R.id.score_pager);
            this.pagerAdapter = new ScreenSliderPagerAdapter(getSupportFragmentManager(), this.matchFragments);
            this.scorePager.setAdapter(this.pagerAdapter);
            this.scorePager.setPageTransformer(true, new DepthPageTransformer());
            this.scorePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int i, float v, int i1) {
                    // nothing to do
                }

                @Override
                public void onPageSelected(int i) {
                    // show the correct images
                    setScoreNavigationImages();
                    // and be sure the score is set okay
                    updateActiveFragment();
                }

                @Override
                public void onPageScrollStateChanged(int i) {
                    // nothing to do
                }
            });
            // be sure the button is correct
            setScoreNavigationImages();
            // and the editing controls
            setupEditingControls();
            // and the server display
            setServerDisplay();
            // this is successful
            return true;
        }
        else {
            // the base failed, we failed
            return false;
        }
    }

    @Override
    public void onAttachFragment(FragmentTime fragment) {
        this.timeFragment = fragment;
    }

    @Override
    public void onAttachFragment(FragmentScore fragment) {
        this.scoreFragment = fragment;
    }

    protected abstract Fragment[] createMatchFragments();

    private void updateActiveFragment() {
        int index = this.scorePager.getCurrentItem();
        updateActiveFragment(index);
    }

    protected void showActiveScore() {
        // can change server if play isn't started
        if (null != this.scoreFragment) {
            this.scoreFragment.setIsServeChangeEnabled(!this.communicator.isPlayStarted());
            // show that the match is over
            if (this.communicator.isMatchOver()) {
                // show this message
                this.scoreFragment.showMatchState(FragmentScore.ScoreState.COMPLETED);
            }
            else if (this.scoreFragment.getMatchState() == FragmentScore.ScoreState.COMPLETED) {
                // cancel this completion message we showed
                this.scoreFragment.cancelMatchState();
            }
        }
        // update the graph of history that the time fragment is showing
        if (null != this.timeFragment) {
            this.timeFragment.setMatchHistory(this.communicator.getCurrentMatch());
        }
        // and update the server display
        setServerDisplay();
    }

    @Override
    public void onFragmentScoreServerMoved() {
        // change the server
        if (!this.communicator.isPlayStarted()) {
            this.communicator.sendRequest(MatchMessage.CHANGE_STARTING_SERVER);
        }
    }

    @Override
    public void onFragmentScorePointsClick(int teamIndex) {
        // clicked on a points button, increment the point
        Team winningTeam;
        switch (teamIndex) {
            case 0:
                winningTeam = this.communicator.getCurrentMatch().getTeamOne();
                break;
            case 1:
                winningTeam = this.communicator.getCurrentMatch().getTeamTwo();
                break;
            default:
                Log.error("unknown winning team " + teamIndex);
                winningTeam = null;
                break;
        }
        if (null != winningTeam) {
            this.communicator.sendRequest(MatchMessage.INCREMENT_POINT, winningTeam);
        }
    }

    protected void setServerDisplay() {
        // be sure to set the server display on the score fragment
        if (null != this.scoreFragment) {
            Match currentMatch = this.communicator.getCurrentMatch();
            if (null != currentMatch && currentMatch.getTeamServing() == currentMatch.getTeamOne()) {
                // team one is serving
                this.scoreFragment.setTeamServer(0);
            }
            else {
                // team two is serving
                this.scoreFragment.setTeamServer(1);
            }
        }
    }

    @Override
    protected void setupEditingControls() {
        super.setupEditingControls();
        // and update the match time displayed on the fragment
        onTimeChanged();
    }

    protected abstract void updateActiveFragment(int index);

    private void setScoreNavigationImages() {
        int currentPage = scorePager.getCurrentItem();
        if (currentPage == 0) {
            // can't do less
            pageLeft.setVisibility(View.INVISIBLE);
            // can show more
            pageRight.setVisibility(View.VISIBLE);
        }
        else {
            // we can
            pageLeft.setVisibility(View.VISIBLE);
        }
        if (currentPage < matchFragments.length - 1) {
            // can show more
            pageRight.setVisibility(View.VISIBLE);
        }
        else {
            // can't
            pageRight.setVisibility(View.INVISIBLE);
        }
    }

    private void changeScorePagerPage(int delta) {
        int newPage = this.scorePager.getCurrentItem() + delta;
        if (newPage >= 0 && newPage < this.scorePager.getChildCount()) {
            // this page index is ok to use
            setCurrentPage(newPage);
        }
    }

    protected void setCurrentPage(int pageIndex) {
        this.scorePager.setCurrentItem(pageIndex, true);
        // update the images
        setScoreNavigationImages();
    }

    protected int getCurrentPage() {
        return this.scorePager.getCurrentItem();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.isInitialisedCorrectly) {
            // start up the screen to set everything up
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // and show this
                    updateActiveFragment();
                }
            }, 500);
        }
    }

    @Override
    public void onTimeChanged() {
        // need to update the match time to include this session
        int minutesPlayed = this.communicator.getMatchMinutesPlayed();
        if (minutesPlayed >= 0 && null != this.timeFragment) {
            this.timeFragment.setMatchTime(minutesPlayed);
        }
        // this is a little tick we can rely on - why don't we store the match results
        // in case there is a little crash...
        this.communicator.sendRequest(MatchMessage.STORE_STATE);
    }

    @Override
    public void onMatchChanged(final Match.MatchChange type) {
        // let the base do its thing
        super.onMatchChanged(type);
        // and process the change on the UI thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                processMatchChange(type);
            }
        });
    }

    private void processMatchChange(Match.MatchChange type) {
        // now handle the tennis things here
        switch (type) {
            case SETTINGS_CHANGED:
                // changing the settings can change our server, update the view
                showActiveScore();
                break;
            case BREAK_POINT:
            case BREAK_POINT_CONVERTED:
                // this might be a little more interesting, either way (at the moment) not a msg
                break;
            case DECREMENT:
                // fall through to update the score
            case INCREMENT:
                // flow through to show the score
            case RESET:
                /*if (null != this.scoreFragment && false == this.playService.isMessageStarted()) {
                    // this is after we showed something and added the point
                    // cancel the state scrolling already
                    this.scoreFragment.cancelMatchState();
                }*/
                // the points have changed, reflect this properly
                showActiveScore();
                break;
            case DECIDING_POINT:
                // inform the players that this is 'sudden death'
                if (null != this.scoreFragment) {
                    // show this message
                    this.scoreFragment.showMatchState(FragmentScore.ScoreState.DECIDING_POINT);
                }
                break;
            case TIE_BREAK:
                // this is fine, we will say it but no need to show it
                break;
            case ENDS:
                // we want to show and animate this change, show the score screen
                /*if (getCurrentPage() != 0) {
                    // change to this new page
                    setCurrentPage(0);
                }*/
                // this requires a message, so send it
                if (null != this.scoreFragment) {
                    this.scoreFragment.showMatchState(FragmentScore.ScoreState.CHANGE_ENDS);
                }
                break;
            case SERVER:
                // we want to show and animate this change, show the score screen
                /*if (getCurrentPage() != 0) {
                    // change to this new page
                    setCurrentPage(0);
                }*/
                // change server
                if (null != this.scoreFragment) {
                    // didn't change ends, but we have changed server, show this
                    this.scoreFragment.showMatchState(FragmentScore.ScoreState.CHANGE_SERVER);
                }
                // and update the display on the score fragment of who is serving.
                setServerDisplay();
                break;
        }
    }
}
