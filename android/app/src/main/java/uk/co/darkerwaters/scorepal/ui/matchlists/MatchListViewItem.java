package uk.co.darkerwaters.scorepal.ui.matchlists;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.MatchPersistenceManager;
import uk.co.darkerwaters.scorepal.data.MatchId;
import uk.co.darkerwaters.scorepal.ui.matchresults.ActivityMatch;
import uk.co.darkerwaters.scorepal.ui.matchresults.ActivityResultsMatch;

public class MatchListViewItem extends RecyclerView.ViewHolder {

    private static final SimpleDateFormat DAY_DATE = new SimpleDateFormat("dd MMM");

    public final View root;
    private final ImageView sportView;
    private final ImageView matchTrophy;
    private final TextView matchDateTitle;
    private final TextView matchWinnerTitle;
    private final TextView matchLoserTitle;
    private final TextView matchPlayedLevel;
    private final TextView matchWinnerScore;
    private final TextView matchLoserScore;
    private final Context context;
    private MatchId matchId;
    private MatchListViewAdapter.MatchData matchData;
    private MatchListViewAdapter parent;

    public MatchListViewItem(View root) {
        super(root);
        this.root = root;
        context = root.getContext();

        sportView = root.findViewById(R.id.teamImage);
        matchTrophy = root.findViewById(R.id.imageViewWinner);
        matchDateTitle = root.findViewById(R.id.matchHistoryTitle);
        matchWinnerTitle = root.findViewById(R.id.matchWinnerTitle);
        matchPlayedLevel = root.findViewById(R.id.matchPlayedLevel);
        matchLoserTitle = root.findViewById(R.id.matchLoserTitle);
        matchWinnerScore = root.findViewById(R.id.matchWinnerScore);
        matchLoserScore = root.findViewById(R.id.matchLoserScore);
    }

    public void setData(final MatchId matchId, final MatchListViewAdapter.MatchData matchData) {
        // hide the old game progress view
        // remember the id
        this.matchId = matchId;
        this.matchData = matchData;
        if (null != matchData) {
            this.root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // show this match
                    showMatchDetails(matchId, matchData);
                }
            });

            int minutesPlayed = (int) (matchData.timePlayed / 60f);
            int hoursPlayed = (int) (minutesPlayed / 60f);
            minutesPlayed = minutesPlayed - (hoursPlayed * 60);
            matchDateTitle.setText(context.getString(R.string.dateAndhoursPlayed, DAY_DATE.format(matchId.getDate()), hoursPlayed, String.format("%02d", minutesPlayed)));
            // set the winner and loser titles
            matchWinnerTitle.setText(matchData.teamWinner);
            matchLoserTitle.setText(matchData.teamLoser);
            // and the final score for each member
            matchPlayedLevel.setText(matchData.playedLevel);
            matchWinnerScore.setText(matchData.winnerScore);
            matchLoserScore.setText(matchData.loserScore);
            // and the correct icon
            switch (matchId.getSport()) {
                case TENNIS:
                    sportView.setImageResource(R.drawable.ic_sports_tennisplay_black_24dp);
                    break;
                case BADMINTON:
                    sportView.setImageResource(R.drawable.ic_sports_badminton_black_24dp);
                    break;
                case PINGPONG:
                    sportView.setImageResource(R.drawable.ic_sports_pingpong_black_24dp);
                    break;
            }
            // we won
            matchTrophy.setVisibility(matchData.isWon ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void showMatchDetails(MatchId matchId, MatchListViewAdapter.MatchData matchData) {
        Intent intent = new Intent(context, ActivityResultsMatch.class);
        intent.putExtra(ActivityMatch.MATCHID, matchId.toString());
        intent.putExtra(ActivityMatch.FROMMATCH, false);
        // and show these results to let them end the match
        context.startActivity(intent);
    }

    public void deleteMatchFile() {
        if (null != this.matchId) {
            // we have the match id, delete that instead
            MatchPersistenceManager manager = MatchPersistenceManager.GetInstance();
            manager.deleteMatchFile(this.matchId, context);
        }
    }
}
