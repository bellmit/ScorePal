package uk.co.darkerwaters.scorepal.ui.matchsetup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.ApplicationPreferences;
import uk.co.darkerwaters.scorepal.application.ApplicationState;
import uk.co.darkerwaters.scorepal.application.MatchService;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.points.Sport;
import uk.co.darkerwaters.scorepal.ui.ActivityMain;
import uk.co.darkerwaters.scorepal.ui.matchinit.ActivityInitMatch;
import uk.co.darkerwaters.scorepal.ui.views.ContactArrayAdapter;

public abstract class FragmentSetupMatch<TSetup extends MatchSetup> extends Fragment {
    protected final int fragmentId;

    protected TSetup matchSetup = null;
    protected final Sport sport;

    private FloatingActionButton startFab;
    private BottomNavigationView sportSelector;
    private ContactArrayAdapter contactAdapter;

    protected FragmentSetupMatch(Sport sport, int fragmentId) {
        this.fragmentId = fragmentId;
        this.sport = sport;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(this.fragmentId, container, false);

        // setup the common controls here - like the play button
        startFab = root.findViewById(R.id.startButton);
        final Activity parentActivity = getActivity();
        startFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create the service to do the score recording now
                playSport(parentActivity);
            }
        });
        startFab.hide();

        sportSelector = root.findViewById(R.id.sport_nav_view);
        sportSelector.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // show this page on the main activity now
                FragmentActivity activity = getActivity();
                if (activity instanceof ActivityMain) {
                    // this can jump to the correct sport then
                    Sport newSport = sport;
                    switch (menuItem.getItemId()) {
                        case R.id.nav_sport_tennis:
                            newSport = Sport.TENNIS;
                            break;
                        case R.id.nav_sport_badminton:
                            newSport = Sport.BADMINTON;
                            break;
                        case R.id.nav_sport_pingpong:
                            newSport = Sport.PINGPONG;
                            break;
                    }
                    if (newSport != sport) {
                        // this is some change
                        ((ActivityMain) activity).playNewMatch(newSport);
                    }
                }
                return true;
            }
        });
        // setup the navigation controls right away
        setupNavControls();
        // setup the controls on this new fragment
        setupControls(root);
        // and return the view
        return root;
    }

    protected void setupNavControls() {
        // set the nav icon to show what this sport is
        switch (sport) {
            case TENNIS:
                sportSelector.setSelectedItemId(R.id.nav_sport_tennis);
                break;
            case BADMINTON:
                sportSelector.setSelectedItemId(R.id.nav_sport_badminton);
                break;
            case PINGPONG:
                sportSelector.setSelectedItemId(R.id.nav_sport_pingpong);
                break;
        }
        // we need to get the correct setup for the match then
        MatchService service = MatchService.GetRunningService();
        MatchSetup runningSetup = service == null ? null : (TSetup) service.getPreparedMatchSetup();
        // there might not be a running match to get the setup for though - so get the default
        if (null == runningSetup || runningSetup.getSport() != sport) {
            // there isn't one runing - or it's for the wrong sport
            ApplicationState applicationState = ApplicationState.Instance();
            matchSetup = (TSetup) applicationState.getDefaultMatchSetup(getContext(), sport);
            // let them start another
            sportSelector.setVisibility(View.VISIBLE);
            startFab.show();
        } else {
            matchSetup = (TSetup) runningSetup;
            // there is one running - don't let them start another
            sportSelector.setVisibility(View.GONE);
            startFab.hide();
        }
    }

    public Sport getSport() { return sport; }

    @Override
    public void onResume() {
        super.onResume();

        if (ApplicationState.Instance().getPreferences().getIsUseContacts()) {
            // create the contacts adapter to show in our list of names
            this.contactAdapter = new ContactArrayAdapter(getContext());
            setupAdapters(this.contactAdapter);
        }
        // and be sure the nav controls are initialised
        setupNavControls();

        // set all the data to be up-to-date now
        setDataToControls();
    }

    @Override
    public void onPause() {
        // as we are pausing, store the default setup so they come back to what they left
        if (null != getActivity() && null != matchSetup) {
            // remember this was the last selected in our preferences
            ApplicationState applicationState = ApplicationState.Instance();
            // this is the last sport chosen
            applicationState.getPreferences().setLastSport(sport);
            // and the default is as we are
            applicationState.storeDefaultMatchSetup(getActivity(), matchSetup);
        }
        super.onPause();
    }

    protected abstract void setDataToControls();

    protected abstract void setupControls(View root);

    protected ArrayAdapter getCursorAdapter() {
        return this.contactAdapter;
    }

    protected abstract void setupAdapters(ArrayAdapter adapter);

    private void playSport(Activity parentActivity) {
        // store the setup for the next time we play this sport
        ApplicationState.Instance().storeDefaultMatchSetup(parentActivity, matchSetup);
        // and this is the settings we want to use to play the match, set this data in our match
        MatchService.PrepareNewMatch(matchSetup);
        // and show the activity to select the final stages
        Intent intent = new Intent(parentActivity, ActivityInitMatch.class);
        // need to send the sport to this activity for it to initialise properly
        Bundle b = new Bundle();
        b.putString(ActivityInitMatch.PARAM_SPORT, matchSetup.getSport().name());
        intent.putExtras(b);
        parentActivity.startActivity(intent);
    }
}
