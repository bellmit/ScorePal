package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.storage.Match;
import uk.co.darkerwaters.scorepal.storage.StorageManager;
import uk.co.darkerwaters.scorepal.storage.StorageResult;

public class MatchHistoryActivity extends AppCompatActivity {

    private ListView historyListView;
    private MatchHistoryListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        historyListView = (ListView) findViewById(R.id.history_list_view);

        // create the list view adapters
        listAdapter = new MatchHistoryListAdapter(this);
        historyListView.setAdapter(listAdapter);

        // and fill the list if we can
        populateList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // history can be shown from score and main, go back instead of up for
                // consistency of behaviour here then
                onBackPressed();
                return true;
        }
        return false;
    }

    private void populateList() {
        // and be sure the adapter is set
        historyListView.setAdapter(listAdapter);
        // and get all the data to put into the list
        StorageManager manager = StorageManager.getManager();
        if (null == manager || null == manager.getCurrentUser()) {
            Toast.makeText(this, R.string.error_user_not_signedin, Toast.LENGTH_SHORT).show();
        }
        else {
            Match.getMatches(manager.getTopLevel(), manager.getCurrentUser().getId(), new StorageResult<Match>() {
                @Override
                public void onResult(Match data) {
                    listAdapter.add(data);
                }
            });
        }
    }
}
