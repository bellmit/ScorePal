package uk.co.darkerwaters.scorepal.ui.matchlists;

import android.os.Bundle;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.MatchPersistenceManager;
import uk.co.darkerwaters.scorepal.data.MatchId;

public class ActivityMatchTrash extends BaseMatchListActivity {

    private MatchPersistenceManager persistenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_trash);

        this.persistenceManager = MatchPersistenceManager.GetInstance();

        MatchSwipeHandler.SwipeMatchInterface swipeMatchInterface = new MatchSwipeHandler.SwipeMatchInterface() {
            @Override
            public int getSwipeMode() { return ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT; }
            @Override
            public int getLeftIconResId() { return R.drawable.ic_redo_black_24dp; }
            @Override
            public int getRightIconResId() { return R.drawable.ic_delete_black_24dp; }
            @Override
            public int getLeftColor() { return getColor(R.color.secondaryColor); }
            @Override
            public int getRightColor() { return getColor(R.color.deleteColor); }
            @Override
            public void handleSwipeLeft(RecyclerView.ViewHolder viewHolder) {
                CardMatchHolder card = null;
                if (viewHolder instanceof CardMatchHolder) {
                    card = (CardMatchHolder)viewHolder;
                }
                if (null != card) {
                    // setup left to undo the bin - restore
                    card.restoreMatchFile();
                }
            }
            @Override
            public void handleSwipeRight(RecyclerView.ViewHolder viewHolder) {
                CardMatchHolder card = null;
                if (viewHolder instanceof CardMatchHolder) {
                    card = (CardMatchHolder)viewHolder;
                }
                if (null != card) {
                    // setup right to delete
                    card.deleteMatchFile();
                }
            }
        };
        // setup the match list activity now
        setupActivity(R.string.history, swipeMatchInterface, 3, false);
    }

    @Override
    protected MatchId[] getMatchList() {
        return MatchPersistenceManager.GetInstance().listDeletedMatches(-1, this);
    }
}