package uk.co.darkerwaters.scorepal.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.fragments.LayoutStatistics;
import uk.co.darkerwaters.scorepal.activities.handlers.SportSquareRecyclerAdapter;
import uk.co.darkerwaters.scorepal.application.GamePlayCommunicator;
import uk.co.darkerwaters.scorepal.application.WidgetBroadcastReceiver;
import uk.co.darkerwaters.scorepal.score.base.MatchMessage;
import uk.co.darkerwaters.scorepal.score.base.Sport;

public class MainActivity extends BaseListedActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set the title of this
        setupActivity(R.string.main_activity);

        setupRecyclerView(R.id.recyclerView, 10, 275f, new SportSquareRecyclerAdapter(application, this));

        WidgetBroadcastReceiver.UpdateAppWidgets(this.application, this);

        // let's just quickly resolve the sport IDs to strings for nice
        Sport.ResolveSportTitles(this);

        // check that they have specified a user name for nice
        String userName = application.getSettings().getSelfName();
        if (null == userName || userName.isEmpty()) {
            // make them choose one
            Intent myIntent = new Intent(this, LoginActivity.class);
            this.startActivity(myIntent);
        }
        else if (application.getSettings().isSignedOn()) {
            // they want to be signed on
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
            if (null == acct || acct.isExpired()) {
                // they want to be signed on but they aren't - ask them to sign on again then...
                Intent myIntent = new Intent(this, LoginActivity.class);
                this.startActivity(myIntent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // if we are here then we are no longer interested in the match we started playing, if
        // we did indeed start one, stop any that are started
        GamePlayCommunicator communicator = GamePlayCommunicator.GetActiveCommunicator();
        if (null != communicator) {
            communicator.sendRequest(MatchMessage.STOP_PLAY);
        }
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
}
