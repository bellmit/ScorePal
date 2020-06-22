package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.widget.Toast;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.fragments.CardHolderMatch;
import uk.co.darkerwaters.scorepal.activities.handlers.SwipeMatchHandler;
import uk.co.darkerwaters.scorepal.score.MatchPersistenceManager;

public class MatchTrashActivity extends BaseMatchListActivity {

    private MatchPersistenceManager persistenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_trash);

        this.persistenceManager = MatchPersistenceManager.GetInstance();

        SwipeMatchHandler.SwipeMatchInterface swipeMatchInterface = new SwipeMatchHandler.SwipeMatchInterface() {
            @Override
            public int getSwipeMode() { return ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT; }
            @Override
            public int getLeftIconResId() { return R.drawable.ic_baseline_undo; }
            @Override
            public int getRightIconResId() { return R.drawable.ic_baseline_delete_forever; }
            @Override
            public int getLeftColor() { return getColor(R.color.undo); }
            @Override
            public int getRightColor() { return getColor(R.color.resetColor); }
            @Override
            public void handleSwipeLeft(CardHolderMatch viewHolder) {
                // setup left to undo the bin - restore
                viewHolder.restoreMatchFile();
            }
            @Override
            public void handleSwipeRight(CardHolderMatch viewHolder) {
                // setup left to delete
                viewHolder.deleteMatchFile();
            }
        };
        // setup the match list activity now
        setupActivity(R.string.menu_trash, swipeMatchInterface, 3, false);
    }

    @Override
    protected String[] getMatchList() {
        return MatchPersistenceManager.GetInstance().listDeletedMatches(-1, this);
    }
}