package uk.co.darkerwaters.scorepal.ui.matchplay;

import android.view.View;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.TennisMatch;
import uk.co.darkerwaters.scorepal.data.TennisScore;
import uk.co.darkerwaters.scorepal.points.Sport;

public class FragmentPlayTennis extends FragmentPlayScore<TennisMatch, TennisScore> {


    public FragmentPlayTennis() {
        super(Sport.TENNIS, R.layout.fragment_play_tennis);
    }

    @Override
    protected void setupControls(View root) {
        super.setupControls(root);
        // get all the controls to use
    }
}
