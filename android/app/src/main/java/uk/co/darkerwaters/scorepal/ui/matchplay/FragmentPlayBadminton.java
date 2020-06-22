package uk.co.darkerwaters.scorepal.ui.matchplay;

import android.view.View;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.BadmintonMatch;
import uk.co.darkerwaters.scorepal.data.BadmintonScore;
import uk.co.darkerwaters.scorepal.points.Sport;

public class FragmentPlayBadminton extends FragmentPlayScore<BadmintonMatch, BadmintonScore> {

    public FragmentPlayBadminton() {
        super(Sport.BADMINTON, R.layout.fragment_play_badminton);
    }

    @Override
    protected void setupControls(View root) {
        super.setupControls(root);
        // get all the controls to use
    }
}