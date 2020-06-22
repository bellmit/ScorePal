package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.handlers.StatisticsRecyclerAdapter;

public class StatisticsActivity extends BaseListedActivity {

    private StatisticsRecyclerAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        // set the title of this
        setupActivity(R.string.menu_opponent_history);

        // setup the list adapter
        this.listAdapter = new StatisticsRecyclerAdapter(this);
        setupRecyclerView(R.id.recyclerView, 2, this.listAdapter);
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
