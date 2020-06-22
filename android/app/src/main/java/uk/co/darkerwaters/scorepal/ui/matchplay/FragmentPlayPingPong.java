package uk.co.darkerwaters.scorepal.ui.matchplay;

import android.view.View;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.PingPongMatch;
import uk.co.darkerwaters.scorepal.data.PingPongScore;
import uk.co.darkerwaters.scorepal.points.Sport;

public class FragmentPlayPingPong extends FragmentPlayScore<PingPongMatch, PingPongScore> {

    public FragmentPlayPingPong() {
        super(Sport.PINGPONG, R.layout.fragment_play_pingpong);
    }

    @Override
    protected void setupControls(View root) {
        super.setupControls(root);
        // get all the controls to use
    }
}