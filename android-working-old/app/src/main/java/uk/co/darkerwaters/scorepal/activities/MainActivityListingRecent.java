package uk.co.darkerwaters.scorepal.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.fragments.CardHolderMatch;
import uk.co.darkerwaters.scorepal.activities.fragments.LayoutStatistics;
import uk.co.darkerwaters.scorepal.activities.handlers.SwipeMatchHandler;
import uk.co.darkerwaters.scorepal.application.WidgetBroadcastReceiver;
import uk.co.darkerwaters.scorepal.score.MatchPersistenceManager;
import uk.co.darkerwaters.scorepal.score.base.Sport;

public class MainActivityListingRecent extends BaseMatchListActivity {

    private FloatingActionButton fabPlay;
    private LayoutStatistics statisticsLayout;

    /*GOOGLE SIGN-IN
    private SignInHandler signInHandler;
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainlistingrecent);

        SwipeMatchHandler.SwipeMatchInterface swipeMatchInterface = new SwipeMatchHandler.SwipeMatchInterface() {
            @Override
            public int getSwipeMode() {
                //TODO disabled the swipe as don't like it...
                return 0;//ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT;
            }
            @Override
            public int getLeftIconResId() {
                return R.drawable.ic_baseline_history;
            }

            @Override
            public int getRightIconResId() {
                return R.drawable.ic_baseline_delete;
            }

            @Override
            public int getLeftColor() {
                return getColor(R.color.primaryTextColor);
            }

            @Override
            public int getRightColor() {
                return getColor(R.color.resetColor);
            }

            @Override
            public void handleSwipeLeft(CardHolderMatch viewHolder) {
                // setup left to hide
                viewHolder.hideMatchFile();
            }

            @Override
            public void handleSwipeRight(CardHolderMatch viewHolder) {
                // setup left to delete
                viewHolder.deleteMatchFile();
            }
        };
        // setup the match list activity now
        setupActivity(R.string.menu_home, swipeMatchInterface, 3, true);

        WidgetBroadcastReceiver.UpdateAppWidgets(this.application, this);

        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(getString(R.string.app_name));

        // create the stats layout
        this.statisticsLayout = new LayoutStatistics(collapsingToolbar);

        this.fabPlay = findViewById(R.id.fab_play);
        this.fabPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // user wants to play
                Intent myIntent = new Intent(MainActivityListingRecent.this, SelectSportActivity.class);
                MainActivityListingRecent.this.startActivity(myIntent);
            }
        });

        // let's just quickly resolve the sport IDs to strings for nice
        Sport.ResolveSportTitles(this);

        /*GOOGLE SIGN-IN
        // create the sign in handler
        this.signInHandler = new SignInHandler(this, this.navigationActor);
        */

        // check that they have specified a user name for nice
        String userName = application.getSettings().getSelfName();
        if (null == userName || userName.isEmpty()) {
            // make them choose one
            Intent myIntent = new Intent(this, LoginActivity.class);
            this.startActivity(myIntent);
        }
    }

    @Override
    protected String[] getMatchList() {
        return MatchPersistenceManager.GetInstance().listRecentMatches(-1, this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        /*GOOGLE SIGN-IN
        // try to sign in
        this.signInHandler.initialiseSignIn();
        */
        // update the stats now we are signed in
        this.statisticsLayout.updateDisplay(this.application, this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /*GOOGLE SIGN-IN
    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case SignInHandler.RC_SIGN_IN:
                // pass this message to the sign-in handler to process
                this.signInHandler.handleActivityResult(requestCode, data);
                // update the stats now we are signed in
                this.statisticsLayout.updateDisplay(this.application, this);
                break;
        }
    }

    public void signInToGoogle() {
        this.signInHandler.signInToGoogle(false);
    }
    */
}
