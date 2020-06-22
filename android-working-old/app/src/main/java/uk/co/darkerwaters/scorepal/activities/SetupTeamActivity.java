package uk.co.darkerwaters.scorepal.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentTeam;
import uk.co.darkerwaters.scorepal.application.GamePlayCommunicator;
import uk.co.darkerwaters.scorepal.settings.SettingsMatch;
import uk.co.darkerwaters.scorepal.players.Player;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.MatchWriter;
import uk.co.darkerwaters.scorepal.score.base.MatchMessage;
import uk.co.darkerwaters.scorepal.score.MatchSettings;
import uk.co.darkerwaters.scorepal.score.base.Sport;

public abstract class SetupTeamActivity<T extends MatchSettings, S extends SettingsMatch> extends BaseTeamActivity {

    public static final String K_ISFROMPLAY = "IsInitiatedFromPlay";

    protected final Sport sport;
    protected GamePlayCommunicator communicator;
    private T matchSettings;
    private S appSettings;

    private Switch singlesDoublesSwitch;

    private View layoutDoublesSingles;
    private View summaryCard;
    private TextView matchSummary;
    private Button resetButton;

    private FloatingActionButton fabPlay;
    private boolean isInitiatedFromPlay = false;

    private TextView teamOneName;
    private TextView teamTwoName;

    private TextView startingServerName;
    private ImageView changeServerButton;

    public SetupTeamActivity(Sport sport) {
        this.sport = sport;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // remember if we came from the Settings activity
        this.isInitiatedFromPlay = getIntent().getBooleanExtra(K_ISFROMPLAY, this.isInitiatedFromPlay);
    }

    @Override
    protected void setupActivity(int titleStringId) {
        super.setupActivity(titleStringId);
        // get the active match communicator
        this.communicator = GamePlayCommunicator.ActivateCommunicator(this);
        // ensure the match settings are created / set by the most derived class
        this.matchSettings = createSettings();
        this.appSettings = getAppSettings();

        // setup the doubles / singles stuff shared on all these matchSettings activities
        this.singlesDoublesSwitch = findViewById(R.id.switchSinglesDoubles);
        this.layoutDoublesSingles = findViewById(R.id.layoutDoublesSingles);
        this.summaryCard = findViewById(R.id.layout_summaryCard);
        this.matchSummary = findViewById(R.id.match_summary_text);
        this.resetButton = findViewById(R.id.match_reset_button);

        this.teamOneName = findViewById(R.id.textViewTeamOne);
        this.teamTwoName = findViewById(R.id.textViewTeamTwo);

        this.startingServerName = findViewById(R.id.textViewStartingServer);
        this.changeServerButton = findViewById(R.id.cycleServerButton);

        this.changeServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cycleServer();
            }
        });

        if (null != this.resetButton) {
            // set the icon button to be white
            setupButtonIcon(this.resetButton, R.drawable.ic_baseline_delete, 0);
            // set the click handler for resetting the match
            this.resetButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    //Yes button clicked
                                    communicator.sendRequest(MatchMessage.RESET);
                                    setActivityDataShown(matchSettings);
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };
                    // show the dialog to check for totally sure
                    AlertDialog.Builder builder = new AlertDialog.Builder(SetupTeamActivity.this);
                    builder.setMessage(R.string.matchWipeConfirmation)
                            .setPositiveButton(R.string.yes, dialogClickListener)
                            .setNegativeButton(R.string.no, dialogClickListener).show();
                }
            });
        }
        this.singlesDoublesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                matchSettings.setIsDoubles(b);
                setActivityDataShown(matchSettings);
            }
        });

        this.fabPlay = findViewById(R.id.fab_play);
        this.fabPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // user wants to play
                if (isInitiatedFromPlay) {
                    // we came from play, just finish this to go back
                    finish();
                }
                else {
                    // show the play, telling it that we initiated it
                    Intent myIntent = new Intent(SetupTeamActivity.this, sport.playActivityClass);
                    myIntent.putExtra(PlayActivity.K_ISFROMSETTINGS, true);
                    SetupTeamActivity.this.startActivity(myIntent);
                }
            }
        });
    }

    protected void initialiseSettings(S appSettings, T settings) {
        // setup all these defaults on our matchSettings object
        settings.setIsDoubles(appSettings.getIsDoubles());
    }

    protected void updateAppSettings(S appSettings, T settings) {
        // set the defaults back on the application
        appSettings.setIsDoubles(settings.getIsDoubles());
    }

    private void cycleServer() {
        // cycle the server on the matchSettings
        this.matchSettings.cycleStartingServer();
        // and show the data
        setTeamNames();
    }

    protected abstract T createSettings();

    protected T getMatchSettings() {
        return this.matchSettings;
    }

    protected abstract S getAppSettings();

    @Override
    protected void setupAdapters(ArrayAdapter adapter) {
        // called to setup the adapters for the team fragments, do that
        super.setupAdapters(adapter);
        // also set the settings on the fragments so they know where to get their names from
        if (null != this.teamOneFragment) {
            this.teamOneFragment.setAppSettings(getAppSettings());
        }
        if (null != this.teamTwoFragment) {
            this.teamTwoFragment.setAppSettings(getAppSettings());
        }
    }

    @Override
    public void onTeamNameChanged(FragmentTeam fragmentTeam) {
        // as the names change we need to set them on the active match
        if (null != this.matchSettings) {
            if (this.teamOneFragment == fragmentTeam) {
                // set the team one names
                this.matchSettings.setPlayerOneName(this.teamOneFragment.getPlayerName());
                this.matchSettings.setPlayerOnePartnerName(this.teamOneFragment.getPlayerPartnerName());
                this.matchSettings.setTeamOneName(this.teamOneFragment.getTeamName());
                // change the mode of the other fragment to match the mode of this one
                this.teamTwoFragment.setTeamNameMode(this.teamOneFragment.getTeamNameMode());
            } else {
                // set the team two names
                this.matchSettings.setPlayerTwoName(this.teamTwoFragment.getPlayerName());
                this.matchSettings.setPlayerTwoPartnerName(this.teamTwoFragment.getPlayerPartnerName());
                this.matchSettings.setTeamTwoName(this.teamTwoFragment.getTeamName());
                // change the mode of the other fragment to match the mode of this one
                this.teamOneFragment.setTeamNameMode(this.teamTwoFragment.getTeamNameMode());
            }
            // change the team names shown
            setTeamNames();
        }
    }

    private void setTeamNames() {
        if (null != this.matchSettings) {
            // set the names of the teams playing
            this.teamOneName.setText(matchSettings.getTeamOne().getTeamName());
            this.teamTwoName.setText(matchSettings.getTeamTwo().getTeamName());

            // get the server
            Team startingTeam = matchSettings.getStartingTeam();
            Player startingServer = matchSettings.getStartingServer(startingTeam);
            this.startingServerName.setText(startingServer.getName());
            if (startingTeam.equals(matchSettings.getTeamOne())) {
                // set the color for this
                this.startingServerName.setTextColor(getColor(R.color.teamOneColor));
            }
            else {
                // set the color for this
                this.startingServerName.setTextColor(getColor(R.color.teamTwoColor));
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // set the names on the teams, even when the user didn't change anything
        onTeamNameChanged(this.teamOneFragment);
        onTeamNameChanged(this.teamTwoFragment);

        // and update the matchSettings
        updateAppSettings(getAppSettings(), this.matchSettings);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // initialise the matchSettings from the application
        initialiseSettings(getAppSettings(), this.matchSettings);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // set this data on the activity
                setActivityDataShown(matchSettings);
            }
        }, 200);
    }

    protected void setActivityDataShown(T settings) {
        // setup this activity from the matchSettings
        boolean isDoubles = settings.getIsDoubles();
        this.singlesDoublesSwitch.setChecked(isDoubles);

        this.teamOneFragment.setIsDoubles(isDoubles, false);
        this.teamTwoFragment.setIsDoubles(isDoubles, false);

        // we need to show the summary of the current match
        Match currentMatch = this.communicator.getCurrentMatch();
        if (null != currentMatch && this.communicator.isMatchStarted()) {
            // show the summary and the card
            this.matchSummary.setText(currentMatch.getDescription(MatchWriter.DescriptionLevel.SUMMARY, this));
            if (null != this.summaryCard) {
                this.summaryCard.setVisibility(View.VISIBLE);
            }
            // can't change between doubles and singles any more - already started
            this.layoutDoublesSingles.setVisibility(View.GONE);
        }
        else {
            // hide the card - this is a new game
            if (null != this.summaryCard) {
                this.summaryCard.setVisibility(View.GONE);
            }
            // and show the stuff to setup a new match
            this.layoutDoublesSingles.setVisibility(View.VISIBLE);
        }

        // and be sure the team names and server is set
        setTeamNames();
    }
}
