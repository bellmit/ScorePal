package uk.co.darkerwaters.scorepal.ui.matchinit;

import android.view.View;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.TennisSetup;
import uk.co.darkerwaters.scorepal.points.Sport;

public class FragmentInitTennis extends FragmentInitServer<TennisSetup> {

    public FragmentInitTennis() {
        super(Sport.TENNIS, R.layout.fragment_init_tennis);
    }

    @Override
    protected void setupControls(View root) {
        super.setupControls(root);
    }

    @Override
    public void onResume() {
        super.onResume();

        // set all the data to be up-to-date now
        setDataToControls();
    }

    private void setDataToControls() {
        // setup the data on this page, first is the number of sets to play

    }
}
