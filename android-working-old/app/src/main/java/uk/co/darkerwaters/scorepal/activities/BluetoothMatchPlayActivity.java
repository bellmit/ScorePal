package uk.co.darkerwaters.scorepal.activities;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.fragments.LayoutPointsSummary;
import uk.co.darkerwaters.scorepal.activities.fragments.LayoutScoreSummary;
import uk.co.darkerwaters.scorepal.activities.fragments.LayoutTennisSummary;
import uk.co.darkerwaters.scorepal.application.GamePlayBroadcaster;
import uk.co.darkerwaters.scorepal.application.GamePlayCommunicator;
import uk.co.darkerwaters.scorepal.application.GamePlayService;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.settings.SettingsControl;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.bluetooth.BluetoothMatch;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.base.MatchMessage;
import uk.co.darkerwaters.scorepal.score.MatchSettings;
import uk.co.darkerwaters.scorepal.score.base.Sport;
import uk.co.darkerwaters.scorepal.score.base.PointChange;

public class BluetoothMatchPlayActivity extends FlicPlayActivity implements GamePlayBroadcaster.GamePlayBroadcastListener {

    private GamePlayBroadcaster broadcaster;
    private LayoutScoreSummary summaryLayout;

    private Match containedMatch = null;

    private ViewGroup scoreSummaryContainer;
    private ViewGroup disconnectedLayout;
    private Button reconnectButton;
    private Button endMatchButton;
    private ProgressBar reconnectProgress;
    
    private ViewGroup pointTeamLayout;
    private Button[] teamButtons = new Button[4];

    private ViewGroup pointServerLayout;
    private Button[] serverButtons = new Button[4];

    private boolean isConnectionAttemptInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_match_play);

        // set the title of this
        setupActivity(R.string.bluetooth_match_connected);

        this.scoreSummaryContainer = findViewById(R.id.scoreSummaryLayout);
        this.disconnectedLayout = findViewById(R.id.broadcastDisconnectedLayout);
        this.reconnectButton = findViewById(R.id.broadcastDisconnectConnectButton);
        this.reconnectProgress = findViewById(R.id.socketDisconnectedProgress);
        this.endMatchButton = findViewById(R.id.endMatchButton);

        setupButtonIcon(this.reconnectButton, R.drawable.ic_baseline_replay, 0);
        setupButtonIcon(this.endMatchButton, R.drawable.ic_baseline_stop, 0);

        this.reconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptConnection();
            }
        });

        this.endMatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endMatch();
            }
        });
        
        this.pointTeamLayout = findViewById(R.id.bluetoothPointsTeamLayout);
        teamButtons[0] = findViewById(R.id.pointTeamOneButton);
        teamButtons[1] = findViewById(R.id.pointTeamTwoButton);
        teamButtons[2] = findViewById(R.id.pointTeamUndoButton);
        teamButtons[3] = findViewById(R.id.pointTeamAnnounceButton);

        BaseActivity.setupButtonIcon(teamButtons[0], R.drawable.ic_team_one_color, 0);
        BaseActivity.setupButtonIcon(teamButtons[1], 0, R.drawable.ic_team_two_color);
        BaseActivity.setupButtonIcon(teamButtons[2], R.drawable.ic_baseline_undo, 0);
        BaseActivity.setupButtonIcon(teamButtons[3], 0, R.drawable.ic_baseline_volume_up);

        this.pointServerLayout = findViewById(R.id.bluetoothPointsServerLayout);
        this.serverButtons[0] = findViewById(R.id.pointServerOneButton);
        this.serverButtons[1] = findViewById(R.id.pointServerTwoButton);
        this.serverButtons[2] = findViewById(R.id.pointServerUndoButton);
        this.serverButtons[3] = findViewById(R.id.pointServerAnnounceButton);

        BaseActivity.setupButtonIcon(serverButtons[0], R.drawable.ic_tennis_serve, 0);
        BaseActivity.setupButtonIcon(serverButtons[1], 0, R.drawable.ic_tennis_receive);
        BaseActivity.setupButtonIcon(serverButtons[2], R.drawable.ic_baseline_undo, 0);
        BaseActivity.setupButtonIcon(serverButtons[3], 0, R.drawable.ic_baseline_volume_up);

        if (null != this.application && new SettingsControl(this.application).getIsControlTeams()) {
            this.pointServerLayout.setVisibility(View.GONE);
        }
        else {
            this.pointTeamLayout.setVisibility(View.GONE);
        }
        // setup the click handers
        this.teamButtons[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // add the point when clicked
                incrementTeamOnePoint();
            }
        });
        this.teamButtons[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // add the point when clicked
                incrementTeamTwoPoint();
            }
        });
        this.teamButtons[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // undo the last point
                undoLastPoint();
            }
        });
        this.teamButtons[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // announce the last points
                announceLastPoints();
            }
        });

        // and for server / receiver
        this.serverButtons[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // add the point when clicked
                incrementServerPoint();
            }
        });
        this.serverButtons[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // add the point when clicked
                incrementReceiverPoint();
            }
        });
        this.serverButtons[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // undo the last point
                undoLastPoint();
            }
        });
        this.serverButtons[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // announce the last points
                announceLastPoints();
            }
        });
    }

    private void undoLastPoint() {
        GamePlayCommunicator communicator = GamePlayCommunicator.GetActiveCommunicator();
        if (null != communicator) {
            GamePlayCommunicator.GetActiveCommunicator().sendRequest(MatchMessage.UNDO_POINT);
        }
    }

    private Match getCurrentMatch() {
        if (null == communicator) {
            communicator = GamePlayCommunicator.GetActiveCommunicator();
        }
        if (null != communicator) {
            return communicator.getCurrentMatch();
        }
        return null;
    }

    private void incrementTeamOnePoint() {
        Match currentMatch = getCurrentMatch();
        if (null != currentMatch) {
            // and send the message for the other team
            Team team = currentMatch.getTeamOne();
            communicator.sendRequest(MatchMessage.INCREMENT_POINT, team);
        }
    }

    private void incrementTeamTwoPoint() {
        Match currentMatch = getCurrentMatch();
        if (null != currentMatch) {
            // and send the message for the other team
            Team team = currentMatch.getTeamTwo();
            communicator.sendRequest(MatchMessage.INCREMENT_POINT, team);
        }
    }

    private void incrementServerPoint() {
        Match currentMatch = getCurrentMatch();
        if (null != currentMatch) {
            // and send the message for the other team
            Team serving = currentMatch.getTeamServing();
            communicator.sendRequest(MatchMessage.INCREMENT_POINT, serving);
        }
    }

    private void incrementReceiverPoint() {
        Match currentMatch = getCurrentMatch();
        if (null != currentMatch) {
            // and send the message for the other team
            Team serving = currentMatch.getTeamServing();
            Team receiving = currentMatch.getOtherTeam(serving);
            communicator.sendRequest(MatchMessage.INCREMENT_POINT, receiving);
        }
    }

    private void announceLastPoints() {
        Match currentMatch = getCurrentMatch();
        if (null != currentMatch) {
            // create the announcement
            String announcement = currentMatch.createPointsAnnouncement(this);
            // send the request to announce the points
            communicator.sendRequest(MatchMessage.ANNOUNCE_POINTS,
                    new MatchMessage.StringParam(announcement));
        }
    }

    @Override
    protected void onResume() {
        // activate our broadcaster each time we are shown
        this.broadcaster = GamePlayBroadcaster.ActivateBroadcaster(this);
        this.broadcaster.addListener(this);
        this.isConnectionAttemptInProgress = false;
        // try to connect to the selected device
        attemptConnection();
        // resume the base class
        super.onResume();
        // update the match data in case we received data before we were created
        updateMatchData();
    }

    private void startMatch(boolean createServiceIfNone) {
        if (createServiceIfNone && null == this.getGamePlayService()) {
            // there is no service, and we will want one, create one here please
            this.createGamePlayService(new ServiceBindListener() {
                @Override
                public void onServiceConnected(GamePlayService service) {
                    // the service is connected, call the function again
                    BluetoothMatchPlayActivity.this.startMatch(false);
                }
                @Override
                public void onServiceDisconnected() {

                }
            });
        }
        // be sure a match is started okay
        Match currentMatch = getCurrentMatch();
        if (false == currentMatch instanceof BluetoothMatch) {
            // the current match is not a points match, so we are responsible for starting
            // a new one now we are here not having had one made for us
            communicator.sendRequest(MatchMessage.CREATE_MATCH);
        }
        if (null != communicator) {
            communicator.sendRequest(MatchMessage.START_PLAY);
        }
        // update the match data now we have started it
        updateMatchData();
    }

    private void endMatch() {
        if (null == communicator) {
            communicator = GamePlayCommunicator.GetActiveCommunicator();
        }
        // end this match now
        if (null != communicator) {
            this.communicator.sendRequest(MatchMessage.STOP_PLAY);
        }
        // this is a bluetooth match ending, so instead of a BT match, let's pretend
        // that we were playing the match that it contained
        Match currentMatch = getCurrentMatch();
        boolean isActivityStarted = false;
        if (currentMatch instanceof BluetoothMatch) {
            // this was indeed a bluetooth match, show the contained match stuff instead
            Match containedMatch = ((BluetoothMatch) currentMatch).getContainedMatch();
            MatchSettings containedMatchSettings = ((BluetoothMatch) currentMatch).getContainedMatchSettings();
            if (null != containedMatch && null != containedMatchSettings) {
                // need to start this as the match we were actually playing
                String matchId = containedMatch.getMatchId(this);
                this.communicator.sendRequest(MatchMessage.SETUP_EXISTING_MATCH,
                        containedMatch,
                        containedMatchSettings,
                        new MatchMessage.StringParam(matchId));
                // and save this data
                this.communicator.sendRequest(MatchMessage.STORE_STATE);
                // and shwo the summary class for this match setup
                Class<? extends Activity> aClass = containedMatchSettings.getSport().summariseActivityClass;
                if (null != aClass) {
                    // there is a summary to show, show it then, but asking it to go back to the main
                    // activity when it closes
                    Intent intent = new Intent(this, aClass);
                    intent.putExtra(SummaryActivity.K_RETURNTOMAINKEY, true);
                    // start this main activity now then
                    startActivity(intent);
                    isActivityStarted = true;
                }
            }
        }
        if (!isActivityStarted) {
            // this was something else - oops just go all the way back to main
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            // start this main activity now then
            startActivity(intent);
        }
        // back to the main activity and clear the activity history so back
        // doesn't come back here
        if (null != communicator) {
            MatchSettings currentSettings = this.communicator.getCurrentSettings();
            if (null != currentSettings) {
                Class<? extends Activity> aClass = currentSettings.getSport().summariseActivityClass;
                if (null != aClass) {
                    // there is a summary to show, show it then, but asking it to go back to the main
                    // activity when it closes
                    Intent intent = new Intent(this, aClass);
                    intent.putExtra(SummaryActivity.K_RETURNTOMAINKEY, true);
                    // start this main activity now then
                    startActivity(intent);
                }
            }
        }
    }

    private void showConnectedState(final boolean isConnected) {
        // show that we are disconnected from the broadcaster
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                disconnectedLayout.setVisibility(isConnected ? View.GONE : View.VISIBLE);
                reconnectButton.setVisibility(isConnectionAttemptInProgress ? View.INVISIBLE : View.VISIBLE);
                reconnectProgress.setVisibility(isConnectionAttemptInProgress ? View.VISIBLE : View.INVISIBLE);
            }
        });
    }

    private void attemptConnection() {
        if (false == this.isConnectionAttemptInProgress) {
            // always run this on the UI thread
            this.isConnectionAttemptInProgress = true;
            // show we are not connected
            showConnectedState(false);
            BluetoothDevice[] devices = this.broadcaster.getConnectedClientDevices();
            if (null == devices || devices.length == 0) {
                // we are not connected, try to connect now
                BluetoothDevice device;
                if (null != broadcaster && null != (device = broadcaster.getDeviceToConnect())) {
                    // and connect to the device broadcasting the match
                    Log.info("Starting connection attempt now on " + device.getName());
                    broadcaster.connectClientToBroadcaster(device, new GamePlayBroadcaster.ClientConnectionInterface() {
                        @Override
                        public void onSocketConnectionSuccess() {
                            // attempt completed
                            isConnectionAttemptInProgress = false;
                            // we are connected
                            showConnectedState(true);
                            // now we are connected, start up the match
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    startMatch(true);
                                }
                            });
                        }

                        @Override
                        public void onSocketConnectionFailed() {
                            isConnectionAttemptInProgress = false;
                            // the attempt is done, but we will start again so leave the flag at true
                            // show that this is failed to connect
                            showConnectedState(false);
                        }
                    });
                }
            } else {
                // didn't start an attempt
                this.isConnectionAttemptInProgress = false;
                // show we are connected
                showConnectedState(true);
            }
        }
    }

    @Override
    protected void onPause() {
        // stop listening to the broadcaster
        this.broadcaster.removeListener(this);
        // and pause the activity
        super.onPause();
    }

    @Override
    public void onDeviceSocketConnected(BluetoothDevice connectedDevice) {
        // we are connected once again, hide the disconnected state
        showConnectedState(true);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // star the match for the connected socket
                startMatch(true);
            }
        });
    }

    @Override
    public void onDeviceSocketDisconnected(BluetoothDevice disconnectedDevice) {
        // and show that we are disconnected
        showConnectedState(false);
        /*
        // try to connect to the selected device
        if (!this.isConnectionAttemptInProgress) {
            Log.info("Socked disconnected initiating attempt to connect");
            this.isConnectionAttemptInProgress = true;
            // we are not in the attempt loop - initiate it again from here
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // attempt the connect now
                    if (!isConnectionAttemptInProgress) {
                        attemptConnection();
                    }
                }
            });
        }*/
    }

    @Override
    public void onMatchPointsChanged(PointChange[] levelsChanged) {
        super.onMatchPointsChanged(levelsChanged);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateMatchData();
            }
        });
    }


    private void updateMatchData() {
        Match currentMatch = getCurrentMatch();
        if (currentMatch instanceof BluetoothMatch) {
            showMatchStatus((BluetoothMatch)currentMatch);
        }
    }

    private void showMatchStatus(BluetoothMatch match) {
        Match containedMatch = match == null ? null : match.getContainedMatch();
        MatchSettings containedSettings = match == null ? null : match.getContainedMatchSettings();
        if (null != containedMatch && null != containedSettings) {
            // have a contained match, get the contained match inside
            if (this.summaryLayout == null || null == this.containedMatch || containedMatch.getClass() != this.containedMatch.getClass()) {
                final Sport sport = containedSettings.getSport();
                switch (sport) {
                    case TENNIS:
                        // inflate the tennis score summary and show the data
                        this.summaryLayout = new LayoutTennisSummary();
                        break;
                    case POINTS:
                        this.summaryLayout = new LayoutPointsSummary();
                        break;
                    default:
                        Log.error("No card summary layout for " + sport.toString());
                        break;
                }
                if (null != this.summaryLayout) {
                    // create this layout, first remove anything hanging around
                    this.scoreSummaryContainer.removeAllViews();
                    // now we can inflate the new view required by the layout class
                    LayoutInflater inflater = LayoutInflater.from(this);
                    View layout = this.summaryLayout.createView(inflater, this.scoreSummaryContainer);
                    // add this to the container
                    this.scoreSummaryContainer.addView(layout);
                    // now we are added, we need to initialise the data here too
                    this.summaryLayout.setMatchData(containedMatch, null);
                    this.summaryLayout.showCurrentServer(containedMatch);

                    // just to see what it is like - let's hide the <more> section
                    View moreLayout = this.scoreSummaryContainer.findViewById(R.id.moreLessLayout);
                    if (null != moreLayout) {
                        moreLayout.setVisibility(View.GONE);
                    }
                }
                // remember the contained match
                this.containedMatch = containedMatch;
            }
            else if (null != this.summaryLayout) {
                this.containedMatch = containedMatch;
                this.summaryLayout.setMatchData(this.containedMatch, null);
                this.summaryLayout.showCurrentServer(this.containedMatch);
            }
        }
        else {
            // we are without a match, ask for one to show the contents
            GamePlayBroadcaster.MatchChanged(MatchMessage.REQUEST_MATCH_UPDATE);
        }
    }
}
