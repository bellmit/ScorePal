package uk.co.darkerwaters.scorepal.ui.matchlists;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.ui.views.CircularProgressBar;

public class MatchDateViewItem extends RecyclerView.ViewHolder {

    private static final SimpleDateFormat MONTH_DATE = new SimpleDateFormat("MMM");

    public final View root;

    private final CircularProgressBar matchProgress;
    private final TextView matchDateTitle;
    private final TextView matchDateSubtitle;
    private final TextView matchTimeText;
    private final TextView matchWinsText;

    private final RecyclerView recyclerView;
    private final View historyLayout;
    private final View mainLayout;
    private MatchListViewAdapter listAdapter;
    private RecyclerView.LayoutManager layoutManager;

    boolean isHistoryShown = true;

    public MatchDateViewItem(View root, final MatchDateViewAdapter parent) {
        super(root);
        this.root = root;

        matchProgress = root.findViewById(R.id.matchProgress);
        matchDateTitle = root.findViewById(R.id.matchHistoryTitle);
        matchDateSubtitle = root.findViewById(R.id.matchDateSubtitle);
        matchTimeText = root.findViewById(R.id.matchTimeText);
        matchWinsText = root.findViewById(R.id.matchWinsText);

        mainLayout = root.findViewById(R.id.matchMainLayout);
        historyLayout = root.findViewById(R.id.matchHistoryLayout);
        // Not sure I like the hiding and showing - causes problems in the drawing
        /*mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleHistory();
            }
        });
         */

        // can setup this item now to show the contents of the match list
        recyclerView = root.findViewById(R.id.matchHistoryBreakdownList);

        // use a linear layout manager
        final Context context = root.getContext();
        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        // let them swipe matches away from here to the trash
        MatchSwipeHandler.SwipeMatchInterface swipeMatchInterface = new MatchSwipeHandler.SwipeMatchInterface() {
            @Override
            public int getSwipeMode() {
                //!turned off when introduced swiping between screens - too easy to delete by mistake
                return 0;
                //return ItemTouchHelper.RIGHT;
            }
            @Override
            public int getLeftIconResId() { return R.drawable.ic_redo_black_24dp; }
            @Override
            public int getRightIconResId() { return R.drawable.ic_delete_black_24dp; }
            @Override
            public int getLeftColor() { return context.getColor(R.color.secondaryColor); }
            @Override
            public int getRightColor() { return context.getColor(R.color.deleteColor); }
            @Override
            public void handleSwipeLeft(RecyclerView.ViewHolder viewHolder) {
                // whatever
            }
            @Override
            public void handleSwipeRight(RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof MatchListViewItem) {
                    // setup right to delete
                    ((MatchListViewItem)viewHolder).deleteMatchFile();
                    // recreate the list
                    parent.createDataToShow(context);
                    // and update it
                    parent.notifyDataSetChanged();
                }
            }
        };
        // setup the swipe handler for the list now
        MatchSwipeHandler swipeHandler = new MatchSwipeHandler(context, swipeMatchInterface);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeHandler);
        itemTouchHelper.attachToRecyclerView(this.recyclerView);
    }

    private void toggleHistory() {
        if (isHistoryShown) {
            historyLayout.setVisibility(View.GONE);
            isHistoryShown = false;
        }
        else {
            historyLayout.setVisibility(View.VISIBLE);
            isHistoryShown = true;
        }
    }

    public void setData(MatchDateViewAdapter.Month month) {
        // this is our data to show - to show it we need to load all the matches in and extract their
        // data accordingly, first though - there are some items we can do without
        final Context context = root.getContext();
        Calendar cal = Calendar.getInstance();
        cal.set(month.year, month.month, 1);
        matchDateTitle.setText(context.getString(R.string.matchMonth, MONTH_DATE.format(cal.getTime()), month.year));
        matchDateSubtitle.setText(context.getString(R.string.matchesInMonth, month.matchIds.size()));

        // specify an adapter with the data - first sort the IDs into a nice order
        month.sortIdList();
        // and set this data on the adapter
        listAdapter = new MatchListViewAdapter(month.matchIds, root.getContext());
        recyclerView.setAdapter(listAdapter);

        // the adapter calculate the wins and time
        int minutesPlayed = (int)(listAdapter.getTimePlayed() / 60f);
        int hoursPlayed = (int)(minutesPlayed / 60f);
        minutesPlayed = minutesPlayed - (hoursPlayed * 60);

        matchTimeText.setText(context.getString(R.string.hoursPlayed, hoursPlayed, String.format("%02d", minutesPlayed)));
        matchWinsText.setText(context.getString(R.string.matchesWon, listAdapter.getWins()));

        // and the number of wins / matches played
        if (month.matchIds.size() > 0) {
            float progress = (listAdapter.getWins() / (float)month.matchIds.size()) * 100f;
            matchProgress.setProgress((int)progress);
        }
        else {
            matchProgress.setProgress(0);
        }
        // set the text for the numbers
        matchProgress.setTitle(Integer.toString(listAdapter.getWins()));
        //matchProgress.setSubTitle(context.getString(R.string.matchesWonOutOfPlayed, listAdapter.getWins(), month.matchIds.size()));
    }

}
