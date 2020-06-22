package uk.co.darkerwaters.scorepal.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.fragments.LayoutScoreSummary;
import uk.co.darkerwaters.scorepal.activities.handlers.MatchRecyclerAdapter;
import uk.co.darkerwaters.scorepal.activities.handlers.PermissionHandler;
import uk.co.darkerwaters.scorepal.application.GamePlayCommunicator;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.MatchPersistenceManager;
import uk.co.darkerwaters.scorepal.score.MatchSettings;
import uk.co.darkerwaters.scorepal.score.MatchStatistics;
import uk.co.darkerwaters.scorepal.score.MatchWriter;
import uk.co.darkerwaters.scorepal.views.MatchMomentumGraph;

import static uk.co.darkerwaters.scorepal.activities.handlers.PermissionHandler.MY_PERMISSIONS_REQUEST_READ_FILES;

public abstract class SummaryActivity<T extends LayoutScoreSummary, M extends Match> extends BaseActivity implements MatchRecyclerAdapter.MatchFileListener<M> {

    public static final String K_RETURNTOMAINKEY = "returnToMain";
    private M matchToShare;
    private PermissionHandler permissionHandler;

    private boolean isReturnToMainOnClose;

    private TextView descriptionText;
    private Button acceptButton;
    private Button resumeButton;

    protected MatchMomentumGraph matchMomentumGraph;
    protected Button momentumFocusButton;
    private int momentumFocus = 0;

    protected M activeMatch;
    protected MatchSettings activeSettings;

    protected T summaryLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get any messages sent
        this.isReturnToMainOnClose = getIntent().getBooleanExtra(K_RETURNTOMAINKEY, false);

        // setup our data
        this.permissionHandler = null;
    }

    protected abstract T createSummaryLayout();

    protected void setupActivity(int titleStringId) {
        super.setupActivity(titleStringId);
        GamePlayCommunicator communicator = GamePlayCommunicator.ActivateCommunicator(this);
        this.activeMatch = (M) communicator.getCurrentMatch();
        this.activeSettings = communicator.getCurrentSettings();

        this.momentumFocusButton = findViewById(R.id.matchMomentumFocusButton);
        this.momentumFocusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swapMomentumFocus();
            }
        });
        this.matchMomentumGraph = findViewById(R.id.matchMomentumGraph);
        this.matchMomentumGraph.setIsShowEntireMatchData(true);
        findViewById(R.id.matchMomentumButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show an activity for the momentum graph with zoom and navigation controls
                Intent intent = new Intent(SummaryActivity.this, MomentumGraphActivity.class);
                SummaryActivity.this.startActivity(intent);
            }
        });
        // set the data on the graph
        setMatchHistory(this.activeMatch);

        this.summaryLayout = createSummaryLayout();

        this.descriptionText = findViewById(R.id.descriptionTextView);
        this.descriptionText.setText(this.activeMatch.getDescription(MatchWriter.DescriptionLevel.SHORT, this));

        this.resumeButton = findViewById(R.id.resumeButton);
        this.acceptButton = findViewById(R.id.acceptButton);
        this.acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // have accepted these results, set them in the stats
                MatchStatistics.OnMatchResultsAccepted(application, activeMatch, activeSettings, SummaryActivity.this);
                // properly close this activity
                closeSummaryActivity(false);
            }
        });
        setupButtonIcon(this.acceptButton, R.drawable.ic_baseline_add, 0);
        setupButtonIcon(this.resumeButton, R.drawable.ic_baseline_play_circle_outline, 0);
        if (isFromPlayActivity()) {
            // we are showing from a play activity - user can just go 'back'
            this.resumeButton.setVisibility(View.GONE);
        }
        else {
            // we are from a playing activity, to resume we just go back
            this.resumeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // to resume we have to be from a play activity, stop
                    // this from going home
                    if (isFromPlayActivity()) {
                        closeSummaryActivity(true);
                    } else {
                        // we need to show the play activity instead
                        Intent intent = new Intent(SummaryActivity.this, activeSettings.getSport().playActivityClass);
                        intent.putExtra(PlayActivity.K_ISFROMSETTINGS, false);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        // start this main activity now then
                        startActivity(intent);
                    }
                }
            });
        }

        // set the data on this activity
        Date matchPlayedDate = this.activeMatch.getMatchPlayedDate();
        String title = this.activeSettings.getSport().getTitle(this);
        title += ": ";
        title += matchPlayedDate == null ? "" : DateFormat.getTimeInstance(DateFormat.SHORT).format(matchPlayedDate);
        title += " ";
        title += matchPlayedDate == null ? "" : DateFormat.getDateInstance(DateFormat.LONG).format(matchPlayedDate);
        setTitle(title);
    }

    private void swapMomentumFocus() {
        // swap the numbers
        this.momentumFocus = this.momentumFocus == 0 ? 1 : 0;
        // and update the display of this focus
        setMatchHistory(this.activeMatch);
    }

    public void setMatchHistory(Match match) {
        if (null != this.matchMomentumGraph && null != match) {
            // set the focus correctly
            this.matchMomentumGraph.setGraphFocus(this.momentumFocus);
            // and show this on the button
            this.momentumFocusButton.setText(
                    this.momentumFocus == 0 ?
                            match.getTeamOne().getTeamName() : match.getTeamTwo().getTeamName());
            // set the color of the text to the correct team color
            this.momentumFocusButton.setTextColor(getColor(this.momentumFocus == 0 ?
                    R.color.teamOneColor : R.color.teamTwoColor));
            // and set the data accordingly
            this.matchMomentumGraph.setMatchData(match);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!this.summaryLayout.isMoreShown()) {
            this.summaryLayout.showMoreLess();
            this.summaryLayout.hideMoreButton();
        }
        if (this.activeMatch == null) {
            // this should be hidden
            this.summaryLayout.hideHideButton();
        }
        else {
            String matchId = this.activeMatch.getMatchId(this);
            if (MatchPersistenceManager.GetInstance().isFileHidden(matchId, this)) {
                // this should be hidden
                this.summaryLayout.hideHideButton();
            }
        }
    }

    @Override
    public void deleteMatchFile(Match match) {
        MatchPersistenceManager manager = MatchPersistenceManager.GetInstance();
        String matchId = match.getMatchId(this);
        if (manager.isFileDeleted(matchId, this)) {
            // this file is deleted already, wipe it here
            manager.wipeDeletedMatchFile(match.getMatchId(this),
                    new Runnable() {
                        @Override
                        public void run() {
                            // it's all over
                            closeSummaryActivity(false);
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
            closeSummaryActivity(false);
        }
    }

    boolean isFromPlayActivity() {
        // we can tell we are from the playing activity because the returnToMainOnClose is true
        return this.isReturnToMainOnClose;
    }

    void closeSummaryActivity(boolean isIgnoreReturnToMain) {
        if (false == isIgnoreReturnToMain && this.isReturnToMainOnClose) {
            // just go all the way back to main
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            // start this main activity now then
            startActivity(intent);
        }
        else {
            finish();
        }
    }

    private void requestFileAccess(M match) {
        // need to request permission to access files for the sharing of match data
        this.matchToShare = match;
        // request permission to share the file
        if (null == this.permissionHandler) {
            // there is no handler, make one here
            this.permissionHandler = new PermissionHandler(this,
                    R.string.file_access_explanation,
                    MY_PERMISSIONS_REQUEST_READ_FILES,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    new PermissionHandler.PermissionsHandlerConstructor() {
                        @Override
                        public boolean getIsRequestPermission() {
                            return application.getSettings().getIsRequestFileAccessPermission();
                        }

                        @Override
                        public void onPermissionsDenied(String[] permissions) {
                            application.getSettings().setIsRequestFileAccessPermission(false);
                            shareMatchData(matchToShare, false);
                        }

                        @Override
                        public void onPermissionsGranted(String[] permissions) {
                            shareMatchData(matchToShare, true);
                        }
                    });
        }
        // check / request access to file writing to subsequently share the file
        this.permissionHandler.requestPermission();
    }

    private void shareMatchData(M match, boolean isSendFile) {
        // get the persistance manager to do this
        MatchPersistenceManager.GetInstance().shareMatchData(matchToShare, match.getMatchId(this), isSendFile, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // pass this message to our handler
        if (!this.permissionHandler.processPermissionsResult(requestCode, permissions, grantResults)) {
            // the handler didn't do anything, pass it on
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void shareMatchFile(M match) {
        // Check whether this app has write external storage permission or not.
        this.matchToShare = match;
        // request file access, this will come back to shareMatchData once that is resolved
        requestFileAccess(match);
    }

    @Override
    public void hideMatchFile(M match) {
        // hiding a file is just renaming it to the hidden extension and removing it
        // from the list
        MatchPersistenceManager manager = MatchPersistenceManager.GetInstance();
        if (manager.hideMatchFile(match.getMatchId(this), this)) {
            // hidden then, close the summary of this
            closeSummaryActivity(false);
        }
    }

    @Override
    public void restoreMatchFile(M match) {
        // restore a file is just renaming it to the recent extension and removing it
        // from the list
        MatchPersistenceManager manager = MatchPersistenceManager.GetInstance();
        manager.restoreMatchFile(match.getMatchId(this), this);
    }
}
