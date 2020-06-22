package uk.co.darkerwaters.scorepal.ui.matchlists;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.Locale;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.MatchPersistenceManager;
import uk.co.darkerwaters.scorepal.data.MatchId;

public class ActivityMatchHistory extends BaseMatchListActivity {

    private ImageButton prevButton;
    private ImageButton nextButton;
    private TextView yearTitle;

    private int currentYear;
    private MatchPersistenceManager persistenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_history);

        MatchSwipeHandler.SwipeMatchInterface swipeMatchInterface = new MatchSwipeHandler.SwipeMatchInterface() {
            @Override
            public int getSwipeMode() {
                int modes = ItemTouchHelper.RIGHT;
                if (MatchPersistenceManager.K_ISDORECENT) {
                    // we are doing recent, enable restoration to the recent list
                    modes |= ItemTouchHelper.LEFT;
                }
                return modes;
            }
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
                    // setup left to undo the hide - restore
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

        this.persistenceManager = MatchPersistenceManager.GetInstance();

        this.currentYear = Calendar.getInstance().get(Calendar.YEAR);

        this.prevButton = findViewById(R.id.historyYearPrev);
        this.nextButton = findViewById(R.id.historyYearNext);
        this.yearTitle = findViewById(R.id.historyYearTitle);

        this.prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeYear(-1);
            }
        });
        this.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeYear(+1);
            }
        });
        // set our current year
        changeYear(0);
    }

    private void changeYear(int delta) {
        int newYear = this.currentYear + delta;
        Integer[] years = persistenceManager.listMatchYears(this);
        boolean isYearValid = false;
        for (Integer year : years) {
            if (newYear == year) {
                // this new year is ok
                isYearValid = true;
                break;
            }
        }
        if (isYearValid) {
            // this is a good year, show the data for this
            this.currentYear = newYear;
            this.yearTitle.setText(String.format(Locale.getDefault(), "%d", this.currentYear));
        }
        if (delta != 0) {
            // refresh the list
            updateMatches();
        }
    }

    @Override
    protected MatchId[] getMatchList() {
        return MatchPersistenceManager.GetInstance().listHiddenMatches(this.currentYear, this);
    }
}