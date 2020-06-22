package uk.co.darkerwaters.scorepal.ui.matchinit;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.MatchService;
import uk.co.darkerwaters.scorepal.data.Match;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.points.Sport;
import uk.co.darkerwaters.scorepal.ui.matchplay.ActivityHelper;

public class ActivityInitMatch extends AppCompatActivity {

    public static final String PARAM_SPORT = "sport";
    private Sport sport;

    private FloatingActionButton startFab;
    private FragmentMatchInit initFragment = null;

    private ActivityHelper activityHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_match);

        this.activityHelper = new ActivityHelper(this);

        // The sport that is showing should be in the extras
        Bundle b = getIntent().getExtras();
        this.sport = Sport.valueOf(b.getString(PARAM_SPORT, Sport.TENNIS.name()));

        // so we can get the fragment from this and replace the blank entry
        initFragment = sport.newInitFragment();

        // now we can put this fragment on the activity
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, initFragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();

        startFab = findViewById(R.id.startButton);
        startFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create the service to do the score recording now
                startService();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // if we are not logged in then show the login screen
        this.activityHelper.checkApplicationState();

        // and initialise the data on the initialisation fragment
        initFragment.setMatchSetup(MatchService.GetPreparedMatch());
    }

    @Override
    protected void onPause() {
        super.onPause();
        // we are paused, pause our helper too
        this.activityHelper.closeApplicationState();
    }

    private void startService() {
        // we can get the match type from the prepared setup
        MatchSetup matchSetup = MatchService.GetPreparedMatch();
        if (null != matchSetup) {
            // so we can create the actual match from this
            Match newMatch = matchSetup.createNewMatch();
            // first - before we start to play the match, be sure that the service is started ready
            this.activityHelper.startMatchService(newMatch);
        }
    }
}
