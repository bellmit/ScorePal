package uk.co.darkerwaters.scorepal.ui.matchresults;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.text.DateFormat;
import java.util.Date;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.MatchPersistenceManager;
import uk.co.darkerwaters.scorepal.application.MatchService;
import uk.co.darkerwaters.scorepal.application.MatchStatistics;
import uk.co.darkerwaters.scorepal.data.Match;
import uk.co.darkerwaters.scorepal.data.MatchId;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.dataui.MatchWriter;
import uk.co.darkerwaters.scorepal.points.Sport;
import uk.co.darkerwaters.scorepal.ui.ActivityMain;
import uk.co.darkerwaters.scorepal.ui.ActivityMomentumGraph;
import uk.co.darkerwaters.scorepal.ui.PermissionsHandler;
import uk.co.darkerwaters.scorepal.ui.UiHelper;
import uk.co.darkerwaters.scorepal.ui.appsettings.FragmentAppSettingsGeneral;
import uk.co.darkerwaters.scorepal.ui.matchlists.ActivityMatchPlayHistory;
import uk.co.darkerwaters.scorepal.ui.matchlists.CardMatchRecyclerAdapter;
import uk.co.darkerwaters.scorepal.ui.matchplay.ActivityHelper;
import uk.co.darkerwaters.scorepal.ui.views.CheckableIndicatorButton;
import uk.co.darkerwaters.scorepal.ui.views.CustomSnackbar;
import uk.co.darkerwaters.scorepal.ui.views.MatchMomentumGraph;
import uk.co.darkerwaters.scorepal.ui.views.RadioIndicatorButton;

public class ActivityResultsMatch extends ActivityMatch
        implements CardMatchRecyclerAdapter.MatchFileListener,
                   PermissionsHandler.Container,
        PermissionsHandler.PermissionsListener {
    private PermissionsHandler permissionsHandler;

    private ImageView sportImage;
    private TextView descriptionText;
    private Button acceptButton;
    private Button resumeButton;

    protected MatchMomentumGraph matchMomentumGraph;
    protected TextView teamMomentumTitle;
    protected RadioIndicatorButton teamOneMomentum;
    protected RadioIndicatorButton teamTwoMomentum;
    private MatchSetup.Team momentumFocus = MatchSetup.Team.T_ONE;

    private Match matchToShare = null;
    private FragmentMatchResults resultsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_results);

        // setup this activity properly for this sport then
        setupActivity(lastMatch.getSport());
    }

    @Override
    public PermissionsHandler getPermissionsHandler() {
        return permissionsHandler;
    }

    @Override
    public Context getContext() {
        return this;
    }

    protected void setupActivity(Sport sport) {
        // set the title
        setTitle(sport.strRes);

        this.permissionsHandler = new PermissionsHandler(this);

        this.teamMomentumTitle = findViewById(R.id.teamMomentumTitle);
        this.teamOneMomentum = findViewById(R.id.teamOneMomentum);
        this.teamTwoMomentum = findViewById(R.id.teamTwoMomentum);
        this.teamOneMomentum.setChecked(this.momentumFocus == MatchSetup.Team.T_ONE);
        this.teamTwoMomentum.setChecked(this.momentumFocus == MatchSetup.Team.T_TWO);
        this.teamOneMomentum.addOnCheckChangeListener(new CheckableIndicatorButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                updateMomentumFocus();
            }
        });
        this.matchMomentumGraph = findViewById(R.id.matchMomentumGraph);
        this.matchMomentumGraph.setIsShowEntireMatchData(true);
        findViewById(R.id.matchMomentumButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show an activity for the momentum graph with zoom and navigation controls
                Intent intent = new Intent(ActivityResultsMatch.this, ActivityMomentumGraph.class);
                intent.putExtra(ActivityMatch.MATCHID, new MatchId(activeMatch).toString());
                intent.putExtra(ActivityMatch.FROMMATCH, isFromMatch);
                ActivityResultsMatch.this.startActivity(intent);
            }
        });
        findViewById(R.id.matchHistoryButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show an activity for the detailed history for the match
                Intent intent = new Intent(ActivityResultsMatch.this, ActivityMatchPlayHistory.class);
                intent.putExtra(ActivityMatch.MATCHID, new MatchId(activeMatch).toString());
                intent.putExtra(ActivityMatch.FROMMATCH, isFromMatch);
                ActivityResultsMatch.this.startActivity(intent);
            }
        });
        // set the data on the graph
        setMatchHistory(this.activeMatch);

        // so we can get the fragment from this and replace the blank entry
        resultsFragment = sport.newResultsFragment(false);

        // now we can put this fragment on the activity
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, resultsFragment);
        fragmentTransaction.attach(resultsFragment);
        fragmentTransaction.commit();

        resultsFragment.setMatchData(this.activeMatch, this);

        this.sportImage = findViewById(R.id.teamImage);
        this.sportImage.setImageResource(sport.iconRes);

        this.descriptionText = findViewById(R.id.descriptionTextView);
        this.descriptionText.setText(this.activeMatch.getDescription(MatchWriter.DescriptionLevel.SHORT, this));

        this.resumeButton = findViewById(R.id.resumeButton);
        this.acceptButton = findViewById(R.id.acceptButton);
        this.acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // have accepted these results, set them in the stats
                MatchStatistics.OnMatchResultsAccepted(activeMatch, ActivityResultsMatch.this);
                // and end the match
                endMatch(true);
            }
        });
        // resume will either go back or it will resume this playing match
        this.resumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // to resume we have to be from a play activity, stop
                // this from going home
                resumeMatch();
            }
        });

        // set the data on this activity
        Date matchPlayedDate = this.activeMatch.getDateMatchStarted();
        String title = getString(sport.strRes);
        title += ": ";
        title += matchPlayedDate == null ? "" : DateFormat.getTimeInstance(DateFormat.SHORT).format(matchPlayedDate);
        title += " ";
        title += matchPlayedDate == null ? "" : DateFormat.getDateInstance(DateFormat.LONG).format(matchPlayedDate);
        setTitle(title);
    }

    private void endMatch(boolean areResultsAccepted) {
        // properly close this activity by ending the match service
        MatchService service = MatchService.GetRunningService();
        if (null != service) {
            // end the match (and the service)
            service.endMatch(areResultsAccepted);
        }
        // and close down this activity
        closeResults();
    }

    private void updateMomentumFocus() {
        // swap the selection
        this.momentumFocus = teamOneMomentum.isChecked() ? MatchSetup.Team.T_ONE : MatchSetup.Team.T_TWO;
        // and update the display of this focus
        setMatchHistory(this.activeMatch);
    }

    public void setMatchHistory(Match match) {
        if (null != this.matchMomentumGraph && null != match) {
            // set the focus correctly
            this.matchMomentumGraph.setGraphFocus(this.momentumFocus);
            MatchSetup setup = match.getSetup();
            // and show this on the button
            this.teamMomentumTitle.setText(
                    this.momentumFocus == MatchSetup.Team.T_ONE ?
                            setup.getTeamName(this, MatchSetup.Team.T_ONE) : setup.getTeamName(this, MatchSetup.Team.T_TWO));
            // set the color of the text to the correct team color
            this.teamMomentumTitle.setTextColor(getColor(this.momentumFocus == MatchSetup.Team.T_ONE ?
                    R.color.teamOneColor : R.color.teamTwoColor));
            // and set the data accordingly
            this.matchMomentumGraph.setMatchData(match);
        }
    }

    @Override
    protected void onPause() {
        if (null != permissionsHandler) {
            permissionsHandler.removeListener(this);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // listen to the permissions handler
        permissionsHandler.addListener(this);
        /*
        if (this.activeMatch == null) {
            // this should be hidden
            this.summaryLayout.hideHideButton();
        }
        else {
            MatchId matchId = new MatchId(this.activeMatch);
            if (MatchPersistenceManager.GetInstance().isFileHidden(matchId, this)) {
                // this should be hidden
                this.summaryLayout.hideHideButton();
            }
        }*/
    }

    @Override
    public void deleteMatchFile(Match match) {
        MatchPersistenceManager manager = MatchPersistenceManager.GetInstance();
        MatchId matchId = new MatchId(this.activeMatch);
        if (manager.isFileDeleted(matchId, this)) {
            // this file is deleted already, wipe it here
            manager.wipeDeletedMatchFile(matchId,
                    new Runnable() {
                        @Override
                        public void run() {
                            // it's all over, end the match
                            endMatch(false);
                        }
                    }, new Runnable() {
                        @Override
                        public void run() {
                            // said 'no'
                        }
                    }, this);
        }
        else {
            // just delete the file
            manager.deleteMatchFile(matchId, this);
            // just deleted it instead, which worked just fine
            endMatch(false);
        }
    }

    private void resumeMatch() {
        if (isFromMatch) {
            // we are from the match, just go back
            finish();
        }
        else if (null != activeMatch) {
            // we need to load up all the data from this active match so they can play it
            MatchService service = MatchService.GetRunningService();
            if (null != service) {
                // we shouldn't be here because when not from a match there shouldn't be one running
                // oh well, let's recover from this now
                service.setActiveMatch(activeMatch);
            }
            else {
                ActivityHelper.StartNewMatch(activeMatch, this);
            }
        }
        else {
            // something is wrong
            UiHelper.showUserMessage(this, R.string.errorMatchNotLoaded);
        }
    }

    private void closeResults() {
        // close these results
        if (isFromMatch) {
            // we are from the match, we can't just go back a we want to close this (going back would resume)
            // so instead jump to the main screen
            Intent intent = new Intent(this, ActivityMain.class);
            // clearing the history
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // and finish this activity now we are done
            startActivity(intent);
        }
        else {
            // to close the results from the history or whatever, we can just close this activity
            finish();
        }
    }

    private void requestFileAccess(Match match) {
        // need to request permission to access files for the sharing of match data
        this.matchToShare = match;
        // request permission to share the file
        if (permissionsHandler.isPermissionsGranted(FragmentAppSettingsGeneral.PERMISSIONS_FILES)) {
            // we have the permissions granted now share the data
            shareMatchData(matchToShare, true);
        }
        else {
            // not allowed, ask for it
            permissionsHandler.checkPermissions(R.string.filesRationale, R.drawable.ic_attach_email_black_24dp, FragmentAppSettingsGeneral.PERMISSIONS_FILES, true);
        }
    }

    @Override
    public void onPermissionsChanged(String[] permissions, int[] grantResults) {
        // try to send the match data
        shareMatchData(matchToShare, permissionsHandler.isPermissionsGranted(FragmentAppSettingsGeneral.PERMISSIONS_FILES));
    }

    private void shareMatchData(Match match, boolean isSendFile) {
        // get the persistance manager to do this
        MatchId matchId = new MatchId(activeMatch);
        MatchPersistenceManager.GetInstance().shareMatchData(matchToShare, matchId, isSendFile, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PermissionsHandler.PERMISSIONS_REQUEST) {
            // pass this to the handler
            this.permissionsHandler.processPermissionsResult(permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void shareMatchFile(Match match) {
        // Check whether this app has write external storage permission or not.
        this.matchToShare = match;
        // request file access, this will come back to shareMatchData once that is resolved
        requestFileAccess(match);
    }

    @Override
    public void hideMatchFile(Match match) {
        // hiding a file is just renaming it to the hidden extension and removing it
        // from the list
        MatchId matchId = new MatchId(activeMatch);
        MatchPersistenceManager manager = MatchPersistenceManager.GetInstance();
        if (manager.hideMatchFile(matchId, this, null)) {
            // hidden then, close the summary of this
            closeResults();
        }
    }

    @Override
    public void restoreMatchFile(Match match) {
        // restore a file is just renaming it to the recent extension and removing it
        // from the list
        MatchId matchId = new MatchId(activeMatch);
        MatchPersistenceManager manager = MatchPersistenceManager.GetInstance();
        manager.restoreMatchFile(matchId, this);
    }
}
