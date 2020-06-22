package uk.co.darkerwaters.scorepal.ui.matchlists;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.SortedListAdapterCallback;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.Match;
import uk.co.darkerwaters.scorepal.data.MatchId;

public class CardMatchRecyclerAdapter extends RecyclerView.Adapter<CardMatchHolder> {

    public interface MatchFileListener<M extends Match> {
        void deleteMatchFile(M match);
        void shareMatchFile(M match);
        void hideMatchFile(M loadedMatch);
        void restoreMatchFile(M loadedMatch);
        Context getContext();
    }

    // create the cards here
    private final SortedList<MatchId> matches;
    private final MatchFileListener listener;

    private final boolean isCreatedExpandedCards;

    private final Activity context;
    private final FragmentManager fragmentManager;

    public CardMatchRecyclerAdapter(Activity context, FragmentManager fragmentManager, MatchFileListener listener, boolean isCreatedExpandedCards) {
        this(context, fragmentManager, listener, new MatchId[0], isCreatedExpandedCards);
    }

    public CardMatchRecyclerAdapter(Activity context, FragmentManager fragmentManager, MatchFileListener listener, MatchId[] matchIds, boolean isCreatedExpandedCards) {
        // create the list of cards to show here
        this.listener = listener;
        this.fragmentManager = fragmentManager;
        this.context = context;
        this.isCreatedExpandedCards = isCreatedExpandedCards;
        // create the sorted list to show in reverse order
        this.matches = new SortedList<>(MatchId.class, new SortedListAdapterCallback<MatchId>(this) {
            @Override
            public boolean areContentsTheSame(MatchId oldItem, MatchId newItem) {
                return oldItem.toString().equals(newItem.toString());
            }

            @Override
            public boolean areItemsTheSame(MatchId item1, MatchId item2) {
                return item1.toString().equals(item2.toString());
            }

            @Override
            public int compare(MatchId o1, MatchId o2) {
                // sort in reverse filename order to put the latest at the top
                return -(o1.toString().compareTo(o2.toString()));
            }
        });
        // add all the items to the list, will sort them all too
        this.matches.beginBatchedUpdates();
        for (MatchId matchId : matchIds) {
            this.matches.add(matchId);
        }
        this.matches.endBatchedUpdates();
    }

    public void updateMatches(MatchId[] matchIds) {
        this.matches.beginBatchedUpdates();
        this.matches.clear();
        for (MatchId matchId : matchIds) {
            this.matches.add(matchId);
        }
        this.matches.endBatchedUpdates();
        // and notify that this is different now
        notifyDataSetChanged();
    }

    public void remove(MatchId matchId) {
        int index = this.matches.indexOf(matchId);
        if (index != -1) {
            this.matches.removeItemAt(index);
            notifyItemRemoved(index);
            notifyItemRangeChanged(index, this.matches.size());
        }
    }

    @Override
    public CardMatchHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        //GRR - have to use our own counter for getting the class to create as i == 0 regardless...
        View view = LayoutInflater.from(context).inflate(R.layout.card_match, viewGroup, false);
        // create the holder and return it
        return new CardMatchHolder(context, fragmentManager, view, this.isCreatedExpandedCards);
    }

    @Override
    public void onBindViewHolder(CardMatchHolder viewHolder, int i) {
        // initialise the card holder here
        viewHolder.initialiseCard(this.matches.get(i), this.listener);
    }

    @Override
    public int getItemCount() {
        return this.matches.size();
    }
}
