package uk.co.darkerwaters.scorepal.activities.handlers;

import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.fragments.CardHolderMatch;
import uk.co.darkerwaters.scorepal.score.Match;

public class MatchRecyclerAdapter extends RecyclerView.Adapter<CardHolderMatch> {

    private final boolean isCreatedExpandedCards;

    public interface MatchFileListener<M extends Match> {
        void deleteMatchFile(M match);
        void shareMatchFile(M match);
        void hideMatchFile(M loadedMatch);
        void restoreMatchFile(M loadedMatch);
    }

    // create the cards here
    private final SortedList<String> matches;
    private final MatchFileListener listener;

    public MatchRecyclerAdapter(MatchFileListener listener, boolean isCreatedExpandedCards) {
        this(listener, new String[0], isCreatedExpandedCards);
    }

    public MatchRecyclerAdapter(MatchFileListener listener, String[] matchIds, boolean isCreatedExpandedCards) {
        // create the list of cards to show here
        this.listener = listener;
        this.isCreatedExpandedCards = isCreatedExpandedCards;
        // create the sorted list to show in reverse order
        this.matches = new SortedList<>(String.class, new SortedListAdapterCallback<String>(this) {
            @Override
            public boolean areContentsTheSame(String oldItem, String newItem) {
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areItemsTheSame(String item1, String item2) {
                return item1.equals(item2);
            }

            @Override
            public int compare(String o1, String o2) {
                // sort in reverse filename order to put the latest at the top
                return -(o1.compareTo(o2));
            }
        });
        // add all the items to the list, will sort them all too
        this.matches.beginBatchedUpdates();
        for (String matchId : matchIds) {
            this.matches.add(matchId);
        }
        this.matches.endBatchedUpdates();
    }

    public void updateMatches(String[] matchIds) {
        this.matches.beginBatchedUpdates();
        this.matches.clear();
        for (String matchId : matchIds) {
            this.matches.add(matchId);
        }
        this.matches.endBatchedUpdates();
        // and notify that this is different now
        notifyDataSetChanged();
    }

    public void remove(String matchId) {
        int index = this.matches.indexOf(matchId);
        if (index != -1) {
            this.matches.removeItemAt(index);
            notifyItemRemoved(index);
            notifyItemRangeChanged(index, this.matches.size());
        }
    }

    @Override
    public CardHolderMatch onCreateViewHolder(ViewGroup viewGroup, int i) {
        //GRR - have to use our own counter for getting the class to create as i == 0 regardless...
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_match, viewGroup, false);
        // create the holder and return it
        return new CardHolderMatch(v, this.isCreatedExpandedCards);
    }

    @Override
    public void onBindViewHolder(CardHolderMatch viewHolder, int i) {
        // initialise the card holder here
        viewHolder.initialiseCard(this.listener, this.matches.get(i), this);
    }

    @Override
    public int getItemCount() {
        return this.matches.size();
    }
}
