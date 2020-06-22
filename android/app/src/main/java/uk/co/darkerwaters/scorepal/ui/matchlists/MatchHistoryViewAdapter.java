package uk.co.darkerwaters.scorepal.ui.matchlists;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.Match;
import uk.co.darkerwaters.scorepal.data.ScoreHistory;

public class MatchHistoryViewAdapter extends RecyclerView.Adapter<MatchHistoryViewItem> {

    private final List<ScoreHistory.HistoryValue> historyValues;
    private Match match;

    public MatchHistoryViewAdapter() {
        this.historyValues = new ArrayList<>();
    }

    public void setHistoryValues(Match match) {
        synchronized (this.historyValues) {
            this.match = match;
            this.historyValues.clear();
            this.historyValues.addAll(Arrays.asList(match.getWinnersHistory()));
            // this is in history order - let's put the most recent on top
            Collections.reverse(this.historyValues);
        }

    }

    // Create new views (invoked by the layout manager)
    @Override
    public MatchHistoryViewItem onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view for the match date contained
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_play_history_listitem, parent, false);
        // create the wrapped for this item and return it (will bind to the data later)
        return new MatchHistoryViewItem(root);
    }

    @Override
    public void onBindViewHolder(MatchHistoryViewItem holder, int position) {
        // - get element from your dataset at this position and update the element with the data
        ScoreHistory.HistoryValue historyValue;
        synchronized (this.historyValues) {
            historyValue = historyValues.get(position);
        }
        // show all this data on the view holder
        holder.setData(historyValue, match);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        synchronized (this.historyValues) {
            return this.historyValues.size();
        }
    }
}

