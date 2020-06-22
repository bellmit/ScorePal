package uk.co.darkerwaters.scorepal.activities.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import uk.co.darkerwaters.scorepal.activities.PlayActivity;
import uk.co.darkerwaters.scorepal.application.Application;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.handlers.MatchRecyclerAdapter;
import uk.co.darkerwaters.scorepal.application.GamePlayCommunicator;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.MatchWriter;
import uk.co.darkerwaters.scorepal.score.base.MatchMessage;
import uk.co.darkerwaters.scorepal.score.MatchPersistenceManager;
import uk.co.darkerwaters.scorepal.score.MatchSettings;
import uk.co.darkerwaters.scorepal.score.base.Sport;

public class CardHolderMatch extends RecyclerView.ViewHolder {

    public static final String K_SELECTED_CARD_FULL_NAME = "selected_parent";
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 102;

    private final View parent;

    private final TextView itemTitle;
    private final TextView sportTitle;
    private final ImageView itemImage;
    private final TextView itemDetail;

    private final ImageButton restoreButton;
    private final ImageButton deleteButton;
    private final ImageButton resumeImageButton;
    private final ImageButton hideImageButton;

    private final TextView matchCompletedText;

    private final View progressLayout;
    private final View dataLayout;
    private final boolean isExpandedCard;
    private MatchRecyclerAdapter.MatchFileListener listener;

    private final ViewGroup scoreSummaryContainer;

    private LayoutScoreSummary summaryLayout = null;

    private MatchRecyclerAdapter adapter;

    private String matchId;
    private Match loadedMatch;
    private MatchSettings loadedSettings;

    public CardHolderMatch(@NonNull View itemView, boolean isExpandedCard) {
        super(itemView);
        this.parent = itemView;
        this.isExpandedCard = isExpandedCard;
        // card is created, find all our children views and stuff here
        this.progressLayout = this.parent.findViewById(R.id.progressLayout);
        this.dataLayout = this.parent.findViewById(R.id.dataLayout);

        this.itemImage = this.parent.findViewById(R.id.item_image);
        this.sportTitle = this.parent.findViewById(R.id.item_sport_title);
        this.itemTitle = this.parent.findViewById(R.id.item_title);
        this.itemDetail = this.parent.findViewById(R.id.item_detail);
        this.restoreButton = this.parent.findViewById(R.id.restoreImageButton);
        this.deleteButton = this.parent.findViewById(R.id.deleteImageButton);
        this.resumeImageButton = this.parent.findViewById(R.id.resumeImageButton);
        this.hideImageButton = this.parent.findViewById(R.id.hideImageButton);
        this.matchCompletedText = this.parent.findViewById(R.id.matchCompletedText);

        this.scoreSummaryContainer = this.parent.findViewById(R.id.scoreSummaryLayout);

        // show the progress
        this.progressLayout.setVisibility(View.VISIBLE);
        this.dataLayout.setVisibility(View.INVISIBLE);
    }

    public void initialiseCard(MatchRecyclerAdapter.MatchFileListener listener, final String matchId, MatchRecyclerAdapter adapter) {
        this.listener = listener;
        this.adapter = adapter;
        // hide the old game progress view
        new Thread(new Runnable() {
            @Override
            public void run() {
                // load the data in a thread to save blocking up this view
                loadMatchData(matchId);
            }
        }).start();
    }

    private void loadMatchData(String matchId) {
        // remember the id
        this.matchId = matchId;
        // load the data
        MatchPersistenceManager persistenceManager = MatchPersistenceManager.GetInstance();
        Pair<Match, MatchSettings> matchData = persistenceManager.loadMatch(matchId, this.parent.getContext());
        if (null != matchData) {
            this.loadedMatch = matchData.first;
            this.loadedSettings = matchData.second;
        }
        // and put ourselves back in the UI thread
        Handler mainHandler = new Handler(this.parent.getContext().getMainLooper());
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

        final Context context = this.parent.getContext();

        // first remove anything hanging around that might not get filled
        // by the subsequent layout
        if (null != this.scoreSummaryContainer) {
            this.scoreSummaryContainer.removeAllViews();
        }

        if (null != this.loadedMatch && null != this.loadedSettings) {
            Date matchPlayedDate = this.loadedMatch.getMatchPlayedDate();
            // set the title from the date
            String title = String.format(context.getString(R.string.date_played)
                    , DateFormat.getTimeInstance(DateFormat.SHORT).format(matchPlayedDate)
                    , DateFormat.getDateInstance(DateFormat.LONG).format(matchPlayedDate));
            this.itemTitle.setText(title);
            this.itemTitle.setOnClickListener(createSportClickListener(context));

            final Sport sport = this.loadedSettings.getSport();
            this.sportTitle.setText(sport.titleResId);
            switch (sport) {
                case TENNIS:
                    // inflate the tennis score summary and show the data
                    this.summaryLayout = new LayoutTennisSummary();
                    break;
                case POINTS:
                    this.summaryLayout = new LayoutPointsSummary();
                    break;
                case PING_PONG:
                    this.summaryLayout = new LayoutPingPongSummary();
                    break;
                case SQUASH:
                    this.summaryLayout = new LayoutSquashSummary();
                    break;
                case BADMINTON:
                    this.summaryLayout = new LayoutBadmintonSummary();
                    break;
                default:
                    this.summaryLayout = null;
                    Log.error("No card summary layout for " + sport.toString());
                    break;
            }
            if (null != sport.imageFilename && false == sport.imageFilename.isEmpty()) {
                this.itemImage.setImageBitmap(Application.GetBitmapFromAssets(sport.imageFilename, context));
            }
            this.itemImage.setOnClickListener(createSportClickListener(context));

            if (this.isExpandedCard) {
                // so when we are here we need to get the layout for the match type
                // and inflate it to show the summary of the score
                if (null != this.summaryLayout && null != this.scoreSummaryContainer) {
                    // now we can inflate the new view required by the layout class
                    LayoutInflater inflater = LayoutInflater.from(this.parent.getContext());
                    View layout = this.summaryLayout.createView(inflater, this.scoreSummaryContainer);
                    // add this to the container
                    this.scoreSummaryContainer.addView(layout);
                    String matchId = loadedMatch.getMatchId(this.parent.getContext());
                    // now we are added, we need to initialise the data here too
                    this.summaryLayout.setMatchData(this.loadedMatch, listener);

                    // just to see what it is like - let's hide the <more> section
                    this.scoreSummaryContainer.findViewById(R.id.moreLessLayout).setVisibility(View.GONE);
                }

                // create the description short
                this.itemDetail.setText(this.loadedMatch.getDescription(MatchWriter.DescriptionLevel.SHORT, context));
                // we are showing resume, hide the delete and restore image buttons
                this.deleteButton.setVisibility(View.GONE);
                this.restoreButton.setVisibility(View.GONE);
            }
            else {
                // not expanded - show more in the text
                this.itemDetail.setText(this.loadedMatch.getDescription(MatchWriter.DescriptionLevel.LONG, context));
                // setup the restore button correctly
                setupRestoreButton();

                // and listen for delete
                this.deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteMatchFile();
                    }
                });
            }

            if (this.loadedMatch.isMatchOver()) {
                // show that this is completed
                this.matchCompletedText.setVisibility(View.VISIBLE);
                this.matchCompletedText.setText(this.loadedMatch.getDescription(MatchWriter.DescriptionLevel.ONELINETOP, context));
                this.resumeImageButton.setVisibility(View.GONE);
            } else {
                // show the match is not over
                this.matchCompletedText.setVisibility(View.INVISIBLE);
                this.resumeImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        resumeMatch(context, sport);
                    }
                });
            }

            // if we are not hidden and not deleted, show the hide button
            MatchPersistenceManager manager = MatchPersistenceManager.GetInstance();
            if (manager.isFileRecent(this.loadedMatch.getMatchId(context), context)) {
                // this is a recent file, show the option to hide it
                this.hideImageButton.setVisibility(View.VISIBLE);
                this.hideImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        hideMatchFile();
                    }
                });
            }
        }
        else {
            // show that this failed to load
            this.itemDetail.setText(R.string.loading_failure);
            this.restoreButton.setVisibility(View.GONE);
            this.resumeImageButton.setVisibility(View.GONE);
            this.hideImageButton.setVisibility(View.GONE);
            // and listen for delete
            this.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteMatchFile();
                }
            });
        }
    }

    private View.OnClickListener createSportClickListener(final Context context) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // set the active match on the application
                String matchId = loadedMatch.getMatchId(context);
                GamePlayCommunicator communicator = GamePlayCommunicator.GetActiveCommunicator();
                communicator.sendRequest(MatchMessage.SETUP_EXISTING_MATCH,
                        loadedMatch,
                        loadedSettings,
                        new MatchMessage.StringParam(matchId));
                // get the summary class to show the content of this sport
                Sport sport = loadedSettings.getSport();
                // and start the play activity to resume this match
                if (null != sport.summariseActivityClass) {
                    Intent intent = new Intent(context, sport.summariseActivityClass);
                    context.startActivity(intent);
                } else {
                    Log.error("No summarise activity for " + sport.toString());
                }
            }
        };
    }

    private void setupRestoreButton() {
        String matchId = loadedMatch.getMatchId(this.parent.getContext());
        Context context = this.parent.getContext();
        MatchPersistenceManager manager = MatchPersistenceManager.GetInstance();
        // if we are doing recent, then if hidden show the button. Or if we are deleted, show it
        if ( (MatchPersistenceManager.K_ISDORECENT && manager.isFileHidden(matchId, context))
                || manager.isFileDeleted(matchId, context)) {
            // can only restore if hidden or deleted
            this.restoreButton.setVisibility(View.VISIBLE);
            // and handle the click of restore and delete
            this.restoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    restoreMatchFile();
                }
            });
            if (manager.isFileDeleted(matchId, context)) {
                // delete will delete forever, adjust the icon on the delete button
                this.deleteButton.setImageResource(R.drawable.ic_baseline_delete_forever);
            }
        }
        else {
            this.restoreButton.setVisibility(View.GONE);
        }
    }

    private void resumeMatch(Context context, Sport sport) {
        // set the active match on the application
        String matchId = loadedMatch.getMatchId(this.parent.getContext());
        GamePlayCommunicator communicator = GamePlayCommunicator.GetActiveCommunicator();
        communicator.sendRequest(MatchMessage.SETUP_EXISTING_MATCH,
                loadedMatch,
                loadedSettings,
                new MatchMessage.StringParam(matchId));
        // and start the play activity to resume this match
        if (null != sport.playActivityClass) {
            Intent intent = new Intent(context, sport.playActivityClass);
            intent.putExtra(PlayActivity.K_ISFROMSETTINGS, false);
            context.startActivity(intent);
        } else {
            Log.error("No play activity for " + sport.toString());
        }
    }

    public void deleteMatchFile() {
        if (null != this.loadedMatch) {
            // ask the top listener to delete this match file
            this.listener.deleteMatchFile(this.loadedMatch);
        }
        else if (null != this.matchId) {
            // we have the match id, delete that instead
            Context context = this.parent.getContext();
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
