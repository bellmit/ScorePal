package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.fragments.LayoutBadmintonSummary;
import uk.co.darkerwaters.scorepal.score.badminton.BadmintonMatch;

public class BadmintonSummaryActivity extends SummaryActivity<LayoutBadmintonSummary, BadmintonMatch> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badminton_summary);

        setupActivity(R.string.badmintonSubtitle);
    }

    @Override
    protected LayoutBadmintonSummary createSummaryLayout() {
        LayoutBadmintonSummary summary = new LayoutBadmintonSummary();
        summary.initialiseViewContents(findViewById(R.id.badmintonLayout));
        // set the data on the layout properly
        summary.setMatchData(this.activeMatch, this);
        return summary;
    }
}
