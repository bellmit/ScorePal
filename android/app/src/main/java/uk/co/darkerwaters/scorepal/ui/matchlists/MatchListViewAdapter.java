package uk.co.darkerwaters.scorepal.ui.matchlists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.ApplicationState;
import uk.co.darkerwaters.scorepal.application.MatchPersistenceManager;
import uk.co.darkerwaters.scorepal.data.Match;
import uk.co.darkerwaters.scorepal.data.MatchId;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.points.Point;

public class MatchListViewAdapter extends RecyclerView.Adapter<MatchListViewItem> {

    public class MatchData {
        final int timePlayed;
        final boolean isWon;
        final String teamWinner;
        final String teamLoser;
        final String winnerScore;
        final String playedLevel;
        final String loserScore;
        public MatchData(Match match, Context context, String username) {
            if (null == match) {
                this.timePlayed = 0;
                this.isWon = false;
                teamWinner = context.getString(R.string.title_teamOne);
                teamLoser = context.getString(R.string.title_teamTwo);
                playedLevel = "";
                winnerScore = context.getString(R.string.display_zero);
                loserScore = context.getString(R.string.display_zero);
            }
            else {
                this.timePlayed = match.getMatchTimePlayed();
                MatchSetup setup = match.getSetup();
                MatchSetup.Team matchWinner = match.getMatchWinner();
                MatchSetup.Team matchLoser = setup.getOtherTeam(matchWinner);
                teamWinner = setup.getTeamName(context, matchWinner);
                teamLoser = setup.getTeamName(context, matchLoser);

                // find the level to which we played and the scores at that level
                Point winnerPoint = null, loserPoint = null;
                int level = 0;
                for (int i = 0; i < match.getScoreLevels(); ++i) {
                    winnerPoint = match.getDisplayPoint(i, matchWinner);
                    loserPoint = match.getDisplayPoint(i, matchLoser);
                    if (null != winnerPoint && null != loserPoint && (
                            winnerPoint.val() > 0 || loserPoint.val() > 0)) {
                        // we have two display points and one of them isn't zero, don't go lower
                        level = i;
                        break;
                    }
                }
                playedLevel = match.getLevelTitle(level, context);
                winnerScore = null != winnerPoint ? winnerPoint.displayString(context) : context.getString(R.string.display_zero);
                loserScore = null != loserPoint ? loserPoint.displayString(context) : context.getString(R.string.display_zero);
                String pWin = setup.getPlayerName(setup.getTeamPlayer(matchWinner));
                String ptWin = setup.getPlayerName(setup.getTeamPartner(matchWinner));
                isWon = setup.usernameEquals(ptWin, username) || setup.usernameEquals(pWin, username);
            }
        }
    }

    private final String username;

    private final List<MatchId> matchIds;
    private final List<MatchData> loadedMatches = new ArrayList<>();

    private final MatchPersistenceManager persistenceManager;

    private int timePlayed = 0;
    private int wins = 0;

    // Provide a suitable constructor (depends on the kind of dataset)
    public MatchListViewAdapter(final List<MatchId> matchIds, Context context) {
        this.matchIds = matchIds;
        // we need to load all of these to extract the data we need
        this.persistenceManager = MatchPersistenceManager.GetInstance();
        username = ApplicationState.Instance().getPreferences().getUserName();
        for (MatchId matchId: matchIds) {
            // load each one
            Match loadedMatch = persistenceManager.loadMatch(matchId, context);
            if (null != loadedMatch && !persistenceManager.isFileDeleted(matchId, context)) {
                MatchData matchData = new MatchData(loadedMatch, context, username);
                timePlayed += matchData.timePlayed;
                wins += matchData.isWon ? 1 : 0;
                // and remember this data for the list
                loadedMatches.add(matchData);
            }
            else {
                // there is no data for this ID, set to NULL so they remain aligned
                loadedMatches.add(null);
            }
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MatchListViewItem onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view for the match date contained
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_match_listitem, parent, false);
        // return the wrapper for this view item, we will bind the data later
        return new MatchListViewItem(root);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchListViewItem holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.setData(matchIds.get(position), loadedMatches.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return matchIds.size();
    }

    public int getTimePlayed() { return this.timePlayed; }
    public int getWins() { return wins; }
}

