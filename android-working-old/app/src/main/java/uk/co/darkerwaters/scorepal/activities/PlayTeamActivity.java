package uk.co.darkerwaters.scorepal.activities;

import android.view.View;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.handlers.TeamEndSceneHandler;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.base.MatchMessage;

public abstract class PlayTeamActivity extends PlayActivity implements TeamEndSceneHandler.Listener {

    private TeamEndSceneHandler endSceneHandler;

    @Override
    protected void onResume() {
        super.onResume();
        if (this.isInitialisedCorrectly) {
            // setup the controls from the active scenes
            this.endSceneHandler = new TeamEndSceneHandler(findViewById(R.id.main_layout), this);
            // transition to the correct ends and show the server icons properly
            this.endSceneHandler.setServerIcons();
            // transition these to setup everything well
            this.endSceneHandler.setEndScenes(true);
        }

    }

    @Override
    public View.OnClickListener createTeamButtonListener(final int teamIndex) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // add the point when clicked
                Match currentMatch = communicator.getCurrentMatch();
                Team sceneTeam = teamIndex == 0 ? currentMatch.getTeamOne() : currentMatch.getTeamTwo();
                // have the team from the current match, we are good to send the request
                communicator.sendRequest(MatchMessage.INCREMENT_POINT, sceneTeam);
            }
        };
    }

    @Override
    public void onMatchChanged(final Match.MatchChange type) {
        // let the base try
        super.onMatchChanged(type);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // handle our team swapping here
                switch (type) {
                    case DECREMENT:
                    case SETTINGS_CHANGED:
                        // decrement is kind of special, we might have changed sides or ends
                        // without knowing about it, set the end scene and server scenes here
                        endSceneHandler.setEndScenes(false);
                        endSceneHandler.setServerIcons();
                        break;
                    case ENDS:
                        // change ends
                        endSceneHandler.setEndScenes(false);
                        break;
                    case SERVER:
                        // change server
                        endSceneHandler.setServerIcons();
                        break;
                }
            }
        });
    }
}
