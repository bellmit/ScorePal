package uk.co.darkerwaters.scorepal.ui.matchinit;

import android.view.View;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.PingPongSetup;
import uk.co.darkerwaters.scorepal.points.Sport;

public class FragmentInitPingPong extends FragmentInitServer<PingPongSetup> {


    public FragmentInitPingPong() {
        super(Sport.PINGPONG, R.layout.fragment_init_pingpong);
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
