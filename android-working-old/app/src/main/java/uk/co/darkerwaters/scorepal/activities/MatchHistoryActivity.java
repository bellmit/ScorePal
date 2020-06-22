package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.fragments.CardHolderMatch;
import uk.co.darkerwaters.scorepal.activities.handlers.SwipeMatchHandler;
import uk.co.darkerwaters.scorepal.score.MatchPersistenceManager;

public class MatchHistoryActivity extends BaseMatchListActivity {

    private ImageButton prevButton;
    private ImageButton nextButton;
    private TextView yearTitle;

    private int currentYear;
    private MatchPersistenceManager persistenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_history);

        SwipeMatchHandler.SwipeMatchInterface swipeMatchInterface = new SwipeMatchHandler.SwipeMatchInterface() {
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
            public int getLeftIconResId() { return R.drawable.ic_baseline_undo; }
            @Override
            public int getRightIconResId() { return R.drawable.ic_baseline_delete; }
            @Override
            public int getLeftColor() { return getColor(R.color.undo); }
            @Override
            public int getRightColor() { return getColor(R.color.resetColor); }
            @Override
            public void handleSwipeLeft(CardHolderMatch viewHolder) {
                // setup left to undo the hide - restore
                viewHolder.restoreMatchFile();
            }
            @Override
            public void handleSwipeRight(CardHolderMatch viewHolder) {
                // setup left to delete
                viewHolder.deleteMatchFile();
            }
        };
        // setup the match list activity now
        setupActivity(R.string.menu_history, swipeMatchInterface, 3, false);

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
    protected String[] getMatchList() {
        return MatchPersistenceManager.GetInstance().listHiddenMatches(this.currentYear, this);
    }
}