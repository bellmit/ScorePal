package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.fragments.LayoutPointsSummary;
import uk.co.darkerwaters.scorepal.score.points.PointsMatch;

public class PointsSummaryActivity extends SummaryActivity<LayoutPointsSummary, PointsMatch> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points_summary);

        setupActivity(R.string.points_description);
    }

    @Override
    protected LayoutPointsSummary createSummaryLayout() {
        LayoutPointsSummary summary = new LayoutPointsSummary();
        summary.initialiseViewContents(findViewById(R.id.pointsLayout));
        // set the data on the layout properly
        summary.setMatchData(this.activeMatch, this);
        return summary;
    }
}
