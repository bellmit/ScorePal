package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.handlers.SportRecyclerAdapter;
import uk.co.darkerwaters.scorepal.application.GamePlayCommunicator;
import uk.co.darkerwaters.scorepal.score.base.MatchMessage;

public class SelectSportActivity extends BaseListedActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_sport);

        // set the title of this
        setupActivity(R.string.menu_chooseSport);

        setupRecyclerView(R.id.recyclerView, 10, new SportRecyclerAdapter(application, this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // if we are here then we are no longer interested in the match we started playing, if
        // we did indeed start one, stop any that are started
        GamePlayCommunicator communicator = GamePlayCommunicator.GetActiveCommunicator();
        if (null != communicator) {
            communicator.sendRequest(MatchMessage.STOP_PLAY);
        }
    }
}
