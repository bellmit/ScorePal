package uk.co.darkerwaters.scorepal.ui.matchlists;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.SortedListAdapterCallback;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.Application;
import uk.co.darkerwaters.scorepal.application.MatchStatistics;
import uk.co.darkerwaters.scorepal.points.Sport;
import uk.co.darkerwaters.scorepal.ui.views.ContactResolver;

public class StatisticsRecyclerAdapter extends RecyclerView.Adapter<CardHolderStatistics> {

    private final Activity context;
    private final SortedList<CardHolderStatistics.StatisticsCard> dataList;
    private final ContactResolver contactResolver;
    private final Application application;

    public StatisticsRecyclerAdapter(Activity context) {
        this.context = context;
        this.application = (Application) context.getApplication();
        this.contactResolver = new ContactResolver(context);
        // create the sorted list to show in reverse order
        this.dataList = new SortedList<>(CardHolderStatistics.StatisticsCard.class, new SortedListAdapterCallback<CardHolderStatistics.StatisticsCard>(this) {
            @Override
            public boolean areContentsTheSame(CardHolderStatistics.StatisticsCard oldItem, CardHolderStatistics.StatisticsCard newItem) {
                return oldItem.title.equals(newItem.title);
            }
            @Override
            public boolean areItemsTheSame(CardHolderStatistics.StatisticsCard item1, CardHolderStatistics.StatisticsCard item2) {
                return item1.title.equals(item2.title);
            }

            @Override
            public int compare(CardHolderStatistics.StatisticsCard o1, CardHolderStatistics.StatisticsCard o2) {
                // sort so the most numbered items are at the top
                return (o2.winsLosses.first + o2.winsLosses.second) - (o1.winsLosses.first + o1.winsLosses.second);
            }
        });
        // and set our data
        updateStatistics();
    }

    public void updateStatistics() {
        this.dataList.beginBatchedUpdates();

        this.dataList.clear();

        MatchStatistics matchStatistics = MatchStatistics.GetInstance(this.context, true);
        // add the sports first
        for (Sport sport : Sport.values()) {
            if (matchStatistics.getTotalLosses(sport) > 0 ||
                matchStatistics.getTotalWins(sport) > 0) {
                // there is data for this sport, show it
                this.dataList.add(new CardHolderStatistics.StatisticsCard(context, sport));
            }
        }
        // and our opponents
        for (String opponent : matchStatistics.getOpponents()) {
            this.dataList.add(new CardHolderStatistics.StatisticsCard(context, opponent, this.contactResolver));
        }
        this.dataList.endBatchedUpdates();
        // and notify that this is different now
        notifyDataSetChanged();
    }

    @Override
    public CardHolderStatistics onCreateViewHolder(ViewGroup viewGroup, int i) {
        //GRR - have to use our own counter for getting the class to create as i == 0 regardless...
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_statistics, viewGroup, false);
        // create the holder and return it
        return new CardHolderStatistics(v);
    }

    @Override
    public void onBindViewHolder(CardHolderStatistics viewHolder, int i) {
        // initialise the card holder here
        viewHolder.initialiseCard(this.context, this.dataList.get(i));
    }

    @Override
    public int getItemCount() {
        return this.dataList.size();
    }
}
