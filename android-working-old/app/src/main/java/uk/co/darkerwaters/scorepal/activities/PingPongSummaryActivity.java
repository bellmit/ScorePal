package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.fragments.LayoutPingPongSummary;
import uk.co.darkerwaters.scorepal.score.pingpong.PingPongMatch;

public class PingPongSummaryActivity extends SummaryActivity<LayoutPingPongSummary, PingPongMatch> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ping_pong_summary);

        setupActivity(R.string.pingPongSubtitle);
    }

    @Override
    protected LayoutPingPongSummary createSummaryLayout() {
        LayoutPingPongSummary summary = new LayoutPingPongSummary();
        summary.initialiseViewContents(findViewById(R.id.pingPongLayout));
        // set the data on the layout properly
        summary.setMatchData(this.activeMatch, this);
        return summary;
    }
}
