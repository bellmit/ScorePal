package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.fragments.LayoutSquashSummary;
import uk.co.darkerwaters.scorepal.score.squash.SquashMatch;

public class SquashSummaryActivity extends SummaryActivity<LayoutSquashSummary, SquashMatch> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_squash_summary);

        setupActivity(R.string.squashSubtitle);
    }

    @Override
    protected LayoutSquashSummary createSummaryLayout() {
        LayoutSquashSummary summary = new LayoutSquashSummary();
        summary.initialiseViewContents(findViewById(R.id.squashLayout));
        // set the data on the layout properly
        summary.setMatchData(this.activeMatch, this);
        return summary;
    }
}
