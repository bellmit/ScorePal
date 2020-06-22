package uk.co.darkerwaters.scorepal.ui.matchlists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.MatchPersistenceManager;
import uk.co.darkerwaters.scorepal.data.MatchId;

public class MatchDateViewAdapter extends RecyclerView.Adapter<MatchDateViewItem> {

    private static final int MONTHS_TO_SHOW = 24;
    private static final int MATCHES_TO_SHOW = 100;

    public class Month {
        final int month;
        final int year;
        final List<MatchId> matchIds;
        Month(int month, int year) {
            this.month = month;
            this.year = year;
            this.matchIds = new ArrayList<>();
        }

        void sortIdList() {
            Collections.sort(this.matchIds, new Comparator<MatchId>() {
                @Override
                public int compare(MatchId lhs, MatchId rhs) {
                    // match ID strings are just reverse order dates - can compare direct
                    return rhs.toString().compareTo(lhs.toString());
                }
            });
        }
    }

    private final List<Month> months;
    private MatchPersistenceManager persistenceManager;

    // Provide a suitable constructor (depends on the kind of dataset)
    public MatchDateViewAdapter(Context context) {
        this.months = new ArrayList<>();
        this.persistenceManager = MatchPersistenceManager.GetInstance();

        // get the data this adapter requires to show
        createDataToShow(context);
    }

    public void createDataToShow(Context context) {
        // set to now
        Calendar calMonth = Calendar.getInstance();
        calMonth.setTime(new Date());
        int year = -1;
        MatchId[] matchIds = null;
        int matchesShowing = 0;
        Calendar calMatch = Calendar.getInstance();
        this.months.clear();
        // and create 10 months from now down
        for (int monthAway = 0; monthAway < MONTHS_TO_SHOW; monthAway++) {
            // create this month
            Month month = new Month(calMonth.get(Calendar.MONTH), calMonth.get(Calendar.YEAR));
            // get all the file ids we can (for the year)
            if (month.year != year) {
                // get all the IDs for this year
                year = month.year;
                matchIds = persistenceManager.listMatches(month.year, context);
            }
            if (null != matchIds && matchIds.length > 0) {
                // and put each one for this month in
                for (MatchId matchId : matchIds) {
                    calMatch.setTime(matchId.getDate());
                    if (month.month == calMatch.get(Calendar.MONTH)) {
                        // this belongs in this one
                        month.matchIds.add(matchId);
                        ++matchesShowing;
                    }
                }
                if (!month.matchIds.isEmpty()) {
                    // we played something this month, add to the list
                    this.months.add(month);
                }
            }
            if (matchesShowing > MATCHES_TO_SHOW) {
                // this is enough now
                break;
            }
            // and remove one month
            calMonth.add(Calendar.MONTH, -1);
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MatchDateViewItem onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view for the match date contained
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_match_date_listitem, parent, false);
        // create the wrapped for this item and return it (will bind to the data later)
        return new MatchDateViewItem(root, this);
    }

    @Override
    public void onBindViewHolder(MatchDateViewItem holder, int position) {
        // - get element from your dataset at this position and update the element with the data
        Month month = months.get(position);
        // show all this data on the view holder
        holder.setData(month);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return months.size();
    }
}

