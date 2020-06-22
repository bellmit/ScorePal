package uk.co.darkerwaters.scorepal.activities;

import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;

import uk.co.darkerwaters.scorepal.activities.fragments.FragmentTeam;
import uk.co.darkerwaters.scorepal.R;

public abstract class BaseTeamActivity extends BaseContactsActivity implements FragmentTeam.FragmentTeamInteractionListener {

    protected FragmentTeam teamOneFragment;
    protected FragmentTeam teamTwoFragment;

    @Override
    public void onAttachFragment(FragmentTeam fragmentTeam) {
        // called as a fragment is attached
        switch (fragmentTeam.getId()) {
            case R.id.team_one_fragment:
                // this is team one
                this.teamOneFragment = fragmentTeam;
                this.teamOneFragment.setLabels(1);
                break;
            case R.id.team_two_fragment:
                this.teamTwoFragment = fragmentTeam;
                this.teamTwoFragment.setLabels(2);
                break;
        }
        // setup any adapters we have created here
        setupAdapters(getCursorAdapter());
    }

    @Override
    public void onAnimationUpdated(Float value) {
        // nothing to do
    }

    @Override
    protected void setupAdapters(ArrayAdapter adapter) {
        if (null != teamOneFragment) {
            this.teamOneFragment.setAutoCompleteAdapter(adapter);
        }
        if (null != teamTwoFragment) {
            this.teamTwoFragment.setAutoCompleteAdapter(adapter);
        }
    }
}
