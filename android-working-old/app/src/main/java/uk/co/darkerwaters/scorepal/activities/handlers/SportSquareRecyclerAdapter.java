package uk.co.darkerwaters.scorepal.activities.handlers;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.fragments.CardHolderSportSquare;
import uk.co.darkerwaters.scorepal.application.Application;
import uk.co.darkerwaters.scorepal.score.base.Sport;
import uk.co.darkerwaters.scorepal.settings.Settings;

public class SportSquareRecyclerAdapter extends RecyclerView.Adapter<CardHolderSportSquare> {

    // create the cards here
    private final SortedList<Sport> sportList;
    private final Application application;
    private final Context context;

    public SportSquareRecyclerAdapter(final Application application, final Context context) {
        // create the list of cards to show here
        this.application = application;
        this.context = context;

        // create the sorted list to show in reverse order
        this.sportList = new SortedList<>(Sport.class, new SortedListAdapterCallback<Sport>(this) {
            @Override
            public boolean areContentsTheSame(Sport oldItem, Sport newItem) {
                return oldItem.value == newItem.value;
            }
            @Override
            public boolean areItemsTheSame(Sport item1, Sport item2) {
                return item1.value == item2.value;
            }

            @Override
            public int compare(Sport o1, Sport o2) {
                // sort so the most recent is at the top
                Settings settings = application.getSettings();
                Date date1 = settings.getSportLastPlayed(context, o1);
                Date date2 = settings.getSportLastPlayed(context, o2);
                if (date1 == null) {
                    return 1;
                }
                else if (date2 == null) {
                    return -1;
                }
                else {
                    return (int) (date2.getTime() - date1.getTime());
                }
            }
        });
        // add all the sports to the sorted list
        this.sportList.beginBatchedUpdates();
        for (Sport sport : Sport.values()) {
            this.sportList.add(sport);
        }
        this.sportList.endBatchedUpdates();
    }

    @Override
    public CardHolderSportSquare onCreateViewHolder(ViewGroup viewGroup, int i) {
        // GRR - have to use our own counter for getting the class to create as i == 0 regardless...
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_sport_square, viewGroup, false);
        // create the holder and return it
        return new CardHolderSportSquare(v, context);
    }

    @Override
    public void onBindViewHolder(CardHolderSportSquare viewHolder, int i) {
        // initialise the card holder here
        viewHolder.initialiseCard(this.sportList.get(i));
    }

    @Override
    public int getItemCount() {
        return this.sportList.size();
    }
}
