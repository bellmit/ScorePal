package uk.co.darkerwaters.scorepal.ui.matchlists;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.Date;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.MatchPersistenceManager;
import uk.co.darkerwaters.scorepal.data.Match;
import uk.co.darkerwaters.scorepal.data.MatchId;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.dataui.MatchWriter;
import uk.co.darkerwaters.scorepal.points.Point;
import uk.co.darkerwaters.scorepal.points.Sport;
import uk.co.darkerwaters.scorepal.ui.matchresults.ActivityMatch;
import uk.co.darkerwaters.scorepal.ui.matchresults.ActivityResultsMatch;
import uk.co.darkerwaters.scorepal.ui.matchresults.FragmentMatchResults;

public class CardMatchHolder extends RecyclerView.ViewHolder {
    private final Activity context;
    private final FragmentManager fragmentManager;
    private final View parent;

    private final TextView itemTitle;
    private final TextView sportTitle;
    private final ImageView itemImage;
    private final TextView itemDetail;

    private final TextView winnerTitle;
    private final TextView loserTitle;
    private final TextView beatingView;
    private final TextView playedLevel;
    private final TextView winnerScore;
    private final TextView loserScore;


    private final TextView matchCompletedText;

    private final View progressLayout;
    private final View dataLayout;
    private final boolean isExpandedCard;
    private CardMatchRecyclerAdapter.MatchFileListener listener;

    private final ViewGroup scoreSummaryContainer;

    private FragmentMatchResults resultsFragment;

    private MatchId matchId;
    private Match loadedMatch;

    public CardMatchHolder(Activity context, FragmentManager fragmentManager, View itemView, boolean isExpandedCard) {
        super(itemView);
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.parent = itemView;
        this.isExpandedCard = isExpandedCard;
        // card is created, find all our children views and stuff here
        this.progressLayout = this.parent.findViewById(R.id.progressLayout);
        this.dataLayout = this.parent.findViewById(R.id.dataLayout);

        this.itemImage = this.parent.findViewById(R.id.item_image);
        this.sportTitle = this.parent.findViewById(R.id.item_sport_title);
        this.itemTitle = this.parent.findViewById(R.id.item_title);
        this.itemDetail = this.parent.findViewById(R.id.item_detail);
        this.matchCompletedText = this.parent.findViewById(R.id.matchCompletedText);

        // and setup this display now
        this.winnerTitle = this.parent.findViewById(R.id.matchWinnerTitle);
        this.loserTitle = this.parent.findViewById(R.id.matchLoserTitle);
        this.playedLevel = this.parent.findViewById(R.id.matchPlayedLevel);
        this.beatingView = this.parent.findViewById(R.id.beatTextView);
        this.winnerScore = this.parent.findViewById(R.id.matchWinnerScore);
        this.loserScore = this.parent.findViewById(R.id.matchLoserScore);

        this.scoreSummaryContainer = this.parent.findViewById(R.id.scoreSummaryLayout);

        // show the progress
        this.progressLayout.setVisibility(View.VISIBLE);
        this.dataLayout.setVisibility(View.INVISIBLE);
    }

    public void initialiseCard(final MatchId matchId, CardMatchRecyclerAdapter.MatchFileListener listener) {
        this.listener = listener;
        // hide the old game progress view
        new Thread(new Runnable() {
            @Override
            public void run() {
                // load the data in a thread to save blocking up this view
                loadMatchData(matchId);
            }
        }).start();
    }

    private void loadMatchData(MatchId matchId) {
        // remember the id
        this.matchId = matchId;
        // load the data
        MatchPersistenceManager persistenceManager = MatchPersistenceManager.GetInstance();
        loadedMatch = persistenceManager.loadMatch(matchId, context);
        // and put ourselves back in the UI thread
        Handler mainHandler = new Handler(context.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                // setup match data
                setupMatchData();
            }
        });
    }

    private void setupMatchData() {
        // we are no longer loading, hide the progress
        this.progressLayout.setVisibility(View.GONE);
        this.dataLayout.setVisibility(View.VISIBLE);

        // first remove anything hanging around that might not get filled
        // by the subsequent layout
        if (null != this.scoreSummaryContainer) {
            this.scoreSummaryContainer.removeAllViews();
        }

        if (null != this.loadedMatch) {
            Date matchPlayedDate = this.loadedMatch.getDateMatchStarted();
            // set the title from the date
            String title = String.format(context.getString(R.string.date_played)
                    , DateFormat.getTimeInstance(DateFormat.SHORT).format(matchPlayedDate)
                    , DateFormat.getDateInstance(DateFormat.LONG).format(matchPlayedDate));
            this.itemTitle.setText(title);

            final Sport sport = loadedMatch.getSport();
            this.sportTitle.setText(sport.strRes);
            this.itemImage.setImageResource(sport.iconRes);
            this.itemImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showMatchDetails();
                }
            });

            MatchSetup setup = loadedMatch.getSetup();
            MatchSetup.Team winner = loadedMatch.getMatchWinner();
            MatchSetup.Team loser = setup.getOtherTeam(winner);
            // need to find the top level scores ATM
            Point winnerPoint = null, loserPoint = null;
            int level = 0;
            for (int i = 0; i < loadedMatch.getScoreLevels(); ++i) {
                winnerPoint = loadedMatch.getDisplayPoint(i, winner);
                loserPoint = loadedMatch.getDisplayPoint(i, loser);
                if (null != winnerPoint && null != loserPoint && (
                        winnerPoint.val() > 0 || loserPoint.val() > 0)) {
                    // we have two display points and one of them isn't zero, don't go lower
                    level = i;
                    break;
                }
            }
            // show the results of this match currently
            winnerTitle.setText(setup.getTeamName(context, winner));
            loserTitle.setText(setup.getTeamName(context, loser));
            playedLevel.setText(loadedMatch.getLevelTitle(level, context));
            winnerScore.setText(null != winnerPoint ? winnerPoint.displayString(context) : context.getString(R.string.display_zero));
            loserScore.setText(null != loserPoint ? loserPoint.displayString(context) : context.getString(R.string.display_zero));

            // so we can get the fragment from this and replace the blank entry
            resultsFragment = sport.newResultsFragment(false);

            // create the description short
            this.itemDetail.setText(this.loadedMatch.getDescription(MatchWriter.DescriptionLevel.SHORT, context));

            if (this.isExpandedCard) {
                // so when we are here we need to get the layout for the match type
                // and inflate it to show the summary of the score
                if (null != resultsFragment && null != this.scoreSummaryContainer) {
                    // now we can inflate the new view required by the layout class
                    // now we can put this fragment on the activity
                    // The trick is, create listview(Recyclerview) of "LinearLayout".
                    // Then dynamically create FrameLayout in adapter and assign different id's for each.
                    // Inflate Fragment to FrameLayout and this FrameLayout to LinearLayout.

                    FrameLayout frameLayout = new FrameLayout(context);
                    // let's use the time of the match ID as the ID
                    frameLayout.setId(View.generateViewId());
                    this.scoreSummaryContainer.addView(frameLayout);
                    // and put the fragment in here
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.add(frameLayout.getId(), resultsFragment);
                    fragmentTransaction.commit();
                    // now we are added, we need to initialise the data here too
                    resultsFragment.setMatchData(this.loadedMatch, listener);
                    resultsFragment.hideMoreSection();
                }
            }

            if (this.loadedMatch.isMatchOver()) {
                // show that this is completed
                this.matchCompletedText.setVisibility(View.VISIBLE);
                this.beatingView.setText(R.string.match_beat);
                //this.matchCompletedText.setText(this.loadedMatch.getDescription(MatchWriter.DescriptionLevel.ONELINETOP, context));
            } else {
                // show the match is not over
                this.matchCompletedText.setVisibility(View.GONE);
                this.beatingView.setText(R.string.match_beating);
            }
        }
        else {
            // show that this failed to load
            this.itemDetail.setText(R.string.loading_failure);
        }
    }

    private void showMatchDetails() {
        Intent intent = new Intent(context, ActivityResultsMatch.class);
        intent.putExtra(ActivityMatch.MATCHID, matchId.toString());
        intent.putExtra(ActivityMatch.FROMMATCH, false);
        // and show these results to let them end the match
        context.startActivity(intent);
    }

    public void deleteMatchFile() {
        if (null != this.loadedMatch) {
            // ask the top listener to delete this match file
            this.listener.deleteMatchFile(this.loadedMatch);
        }
        else if (null != this.matchId) {
            // we have the match id, delete that instead
            MatchPersistenceManager manager = MatchPersistenceManager.GetInstance();
            if (manager.isFileDeleted(matchId, context)) {
                // the file is deleted already - wipe it
                manager.wipeDeletedMatchFile(this.matchId, null, null, context);
            }
            else {
                // just delete it
                manager.deleteMatchFile(this.matchId, context);
            }
        }
    }

    public void shareMatchFile() {
        // ask the top listener to share this match file
        this.listener.shareMatchFile(this.loadedMatch);
    }

    public void hideMatchFile() {
        // ask the top listener to archive this match file
        this.listener.hideMatchFile(this.loadedMatch);
    }

    public void restoreMatchFile() {
        // ask the top listener to restore this match file
        this.listener.restoreMatchFile(this.loadedMatch);
    }
}
