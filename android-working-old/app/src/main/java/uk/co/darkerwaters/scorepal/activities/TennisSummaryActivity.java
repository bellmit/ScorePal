package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.fragments.LayoutTennisSummary;
import uk.co.darkerwaters.scorepal.score.tennis.TennisMatch;

public class TennisSummaryActivity extends SummaryActivity<LayoutTennisSummary, TennisMatch>  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tennis_summary);

        // setup the activity
        setupActivity(R.string.tennisSubtitle);
    }

    @Override
    protected LayoutTennisSummary createSummaryLayout() {
        LayoutTennisSummary summary = new LayoutTennisSummary();
        summary.initialiseViewContents(findViewById(R.id.tennisLayout));
        // set the data on the layout properly
        summary.setMatchData(this.activeMatch, this);
        return summary;
    }
}
