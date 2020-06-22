package uk.co.darkerwaters.scorepal.ui.matchlists;

import android.os.Bundle;

import uk.co.darkerwaters.scorepal.R;

public class ActivityMatchStatistics extends BaseListedActivity {

    private StatisticsRecyclerAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_statistics);

        // setup the list adapter
        this.listAdapter = new StatisticsRecyclerAdapter(this);
        setupRecyclerView(R.id.recycler_view, 3, this.listAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // setup the list to show each time we are shown in case another one appeared
        updateStatistics();
    }

    protected void updateStatistics() {
        // refresh the contents of the list adapter
        this.listAdapter.updateStatistics();
    }
}