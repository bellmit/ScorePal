package uk.co.darkerwaters.scorepal.activities.handlers;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.transition.ChangeBounds;
import android.support.transition.Scene;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.BaseActivity;
import uk.co.darkerwaters.scorepal.application.GamePlayCommunicator;
import uk.co.darkerwaters.scorepal.players.CourtPosition;
import uk.co.darkerwaters.scorepal.players.Player;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.MatchSettings;

public class TeamEndSceneHandler {

    private final View mainView;
    private final Listener listener;
    private final GamePlayCommunicator communicator;
    private final boolean isDoubles;

    public interface Listener {
        View.OnClickListener createTeamButtonListener(int teamIndex);
    }

    private class TeamScene {
        int teamColor;
        int activeScene = -1;
        int teamIndex;
        ViewGroup root;
        Scene[] scenes;
    }

    private final TeamScene teamOneScene;
    private final TeamScene teamTwoScene;

    public TeamEndSceneHandler(View parent, Listener listener) {
        this.mainView = parent;
        this.listener = listener;
        this.communicator = GamePlayCommunicator.GetActiveCommunicator();
        Context context = this.mainView.getContext();
        boolean doubles = false;
        if (null != this.communicator) {
            MatchSettings settings = this.communicator.getCurrentSettings();
            if (null != settings) {
                doubles = settings.getIsDoubles();
            }
        }
        this.isDoubles = doubles;
        int northSceneId, southSceneId;
        if (this.isDoubles) {
            northSceneId = R.layout.scene_player_north_doubles;
            southSceneId = R.layout.scene_player_south_doubles;
        }
        else {
            northSceneId = R.layout.scene_player_north_singles;
            southSceneId = R.layout.scene_player_south_singles;
        }
        // create each class then populate with all the controls that could be there
        this.teamOneScene = new TeamScene();
        this.teamTwoScene = new TeamScene();
        // set the colours
        this.teamOneScene.teamColor = R.color.teamOneColor;
        this.teamTwoScene.teamColor = R.color.teamTwoColor;
        // set the teams here
        this.teamOneScene.teamIndex = 0;
        this.teamTwoScene.teamIndex = 1;
        // find the roots to the scenes
        this.teamOneScene.root = mainView.findViewById(R.id.team_one_scene);
        this.teamTwoScene.root = mainView.findViewById(R.id.team_two_scene);
        // Create the two scenes
        this.teamOneScene.scenes = new Scene[2];
        this.teamOneScene.scenes[CourtPosition.NORTH.ordinal()] = Scene.getSceneForLayout(this.teamOneScene.root, northSceneId, context);
        this.teamOneScene.scenes[CourtPosition.SOUTH.ordinal()] = Scene.getSceneForLayout(this.teamOneScene.root, southSceneId, context);
        // and team two
        this.teamTwoScene.scenes = new Scene[2];
        this.teamTwoScene.scenes[CourtPosition.NORTH.ordinal()] = Scene.getSceneForLayout(this.teamTwoScene.root, northSceneId, context);
        this.teamTwoScene.scenes[CourtPosition.SOUTH.ordinal()] = Scene.getSceneForLayout(this.teamTwoScene.root, southSceneId, context);
    }

    private void scrollTeamText(TeamScene scene) {
        if (this.isDoubles) {
            scrollTextView((TextView)scene.root.findViewById(R.id.team_textViewOne));
            scrollTextView((TextView)scene.root.findViewById(R.id.team_textViewTwo));
        }
        else {
            scrollTextView((TextView) scene.root.findViewById(R.id.team_textView));
        }
    }

    private void scrollTextView(TextView view) {
        // reset it back to not scrolling first
        view.setSelected(false);
        // reset te marquee limit
        view.setMarqueeRepeatLimit(1);
        // and start again
        view.setSelected(true);
    }

    public void setEndScenes(boolean isForceChange) {
        Match currentMatch = this.communicator.getCurrentMatch();
        if (null == currentMatch) {
            // cannot as there is no match data
            return;
        }
        CourtPosition t1Position = currentMatch.getTeamOne().getCourtPosition();
        CourtPosition t2Position = currentMatch.getTeamTwo().getCourtPosition();
        // animate the movement to this court position
        if (isForceChange ||
                teamOneScene.activeScene != t1Position.ordinal() ||
                teamTwoScene.activeScene != t2Position.ordinal()) {
            // change this value
            teamOneScene.activeScene = t1Position.ordinal();
            teamTwoScene.activeScene = t2Position.ordinal();
            // and transition
            TransitionManager.go(teamOneScene.scenes[teamOneScene.activeScene], createTransition(teamOneScene));
            TransitionManager.go(teamTwoScene.scenes[teamTwoScene.activeScene], createTransition(teamTwoScene));
        }
    }

    private void setupTeamButtons(final TeamScene scene) {
        scene.root.setOnClickListener(listener.createTeamButtonListener(scene.teamIndex));
        // do when click receiver button
        ImageButton button = scene.root.findViewById(R.id.team_receiverButton);
        button.setOnClickListener(listener.createTeamButtonListener(scene.teamIndex));
        // and when click server button
        button = scene.root.findViewById(R.id.team_serverButton);
        button.setOnClickListener(listener.createTeamButtonListener(scene.teamIndex));
    }

    private void setupDoublesServerIcons(TeamScene scene) {
        // in doubles we have serving icons too
        ImageView playerOneServing = scene.root.findViewById(R.id.team_serverOneImageView);
        ImageView playerTwoServing = scene.root.findViewById(R.id.team_serverTwoImageView);
        if (null == playerOneServing || null == playerTwoServing) {
            // oops
            return;
        }
        Team servingTeam = getServingTeam();
        Team sceneTeam = getTeam(scene.teamIndex);
        if (servingTeam.equals(sceneTeam)) {
            // we are the serving team, show the serve image
            if (servingTeam.getServingPlayer().equals(servingTeam.getPlayer(0))) {
                // the first player is serving
                playerOneServing.setVisibility(View.VISIBLE);
                playerTwoServing.setVisibility(View.INVISIBLE);
                // just set the color for the one we can see
                //BaseActivity.setupButtonIcon(playerOneServing, R.drawable.ic_baseline_keyboard_arrow_right, scene.teamColor);
                // quite like the big block of colour you get when you color the whole thing
                BaseActivity.setupButtonIcon(playerOneServing, scene.teamColor);
            }
            else {
                // the second player is serving
                playerOneServing.setVisibility(View.INVISIBLE);
                playerTwoServing.setVisibility(View.VISIBLE);
                // just set the color for the one we can see
                //BaseActivity.setupButtonIcon(playerTwoServing, R.drawable.ic_baseline_keyboard_arrow_left, scene.teamColor);
                // quite like the big block of colour you get when you color the whole thing
                BaseActivity.setupButtonIcon(playerTwoServing, scene.teamColor);
            }
        }
        else {
            // neither are serving
            playerOneServing.setVisibility(View.INVISIBLE);
            playerTwoServing.setVisibility(View.INVISIBLE);
        }
    }

    public void setServerIcons() {
        Match currentMatch = this.communicator.getCurrentMatch();
        if (null == currentMatch) {
            // cannot as there is no match data
            return;
        }
        Team servingTeam = currentMatch.getTeamServing();
        // which team is serving?
        ImageButton fromImage, toImage;
        if (getTeam(0) == servingTeam) {
            // team one has the server, change from receiver to server icon
            fromImage = this.teamOneScene.root.findViewById(R.id.team_receiverButton);
            toImage = this.teamOneScene.root.findViewById(R.id.team_serverButton);
            // animate this change for nice
            animateIconChange(fromImage, toImage);

            // the other scene has to go the other way around
            fromImage = this.teamTwoScene.root.findViewById(R.id.team_serverButton);
            toImage = this.teamTwoScene.root.findViewById(R.id.team_receiverButton);
            // animate this change for nice
            animateIconChange(fromImage, toImage);
        }
        else {
            // other way around
            fromImage = this.teamOneScene.root.findViewById(R.id.team_serverButton);
            toImage = this.teamOneScene.root.findViewById(R.id.team_receiverButton);
            // animate this change for nice
            animateIconChange(fromImage, toImage);

            // the other scene has to go the other way around
            fromImage = this.teamTwoScene.root.findViewById(R.id.team_receiverButton);
            toImage = this.teamTwoScene.root.findViewById(R.id.team_serverButton);
            // animate this change for nice
            animateIconChange(fromImage, toImage);
        }
        if (isDoubles) {
            // update the doubles player server icon too
            setupDoublesServerIcons(this.teamOneScene);
            setupDoublesServerIcons(this.teamTwoScene);
        }
    }

    public void swapServerInTeam() {
        MatchSettings currentSettings = this.communicator.getCurrentSettings();
        if (null == currentSettings) {
            // cannot as there is no match data
            return;
        }
        // the team that is currently serving wants to start with the other player serving
        Team teamServing = currentSettings.getStartingTeam();
        Player currentServer = teamServing.getServingPlayer();
        // use the other player from the team as the starting server
        for (Player player : teamServing.getPlayers()) {
            if (player != currentServer) {
                // this is the other player
                currentSettings.setTeamStartingServer(player);
                break;
            }
        }
        // update the display of this
        setServerIcons();
    }

    public void swapStartingTeam() {
        MatchSettings currentSettings = this.communicator.getCurrentSettings();
        if (null == currentSettings) {
            // cannot as there is no match data
            return;
        }
        // swap over the team that is starting the match
        Team teamStarting = currentSettings.getStartingTeam();
        if (teamStarting == currentSettings.getTeamOne()) {
            // team one is starting, change this
            currentSettings.setStartingTeam(currentSettings.getTeamTwo());
        }
        else {
            // team two is starting, change this
            currentSettings.setStartingTeam(currentSettings.getTeamOne());
        }
        // update the display of this
        setServerIcons();
        setEndScenes(false);
    }

    public void swapStartingEnds() {
        MatchSettings currentSettings = this.communicator.getCurrentSettings();
        if (null == currentSettings) {
            // cannot as there is no match data
            return;
        }
        // for each team, set their starting end to be the next one from where they currently are
        currentSettings.cycleTeamStartingEnds();
        // update the display of this
        setEndScenes(false);
    }

    private void animateIconChange(final View fromIcon, final View toIcon) {
        ObjectAnimator outAnimator = ObjectAnimator.ofFloat(fromIcon, "alpha", 0f);
        outAnimator.setDuration(500);
        outAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                fromIcon.setAlpha(1f);
                fromIcon.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animator animator) {
                // totally hide the one we faded out
                fromIcon.setVisibility(View.INVISIBLE);
                fromIcon.setAlpha(0f);
                // and fade the new one in
                ObjectAnimator inAnimator = ObjectAnimator.ofFloat(toIcon, "alpha", 1f);
                inAnimator.setDuration(500);
                inAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        toIcon.setAlpha(0f);
                        toIcon.setVisibility(View.VISIBLE);
                    }
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        // and we are done
                        toIcon.setAlpha(1f);
                    }
                    @Override
                    public void onAnimationCancel(Animator animator) {
                    }
                    @Override
                    public void onAnimationRepeat(Animator animator) {
                    }
                });
                inAnimator.start();
            }
            @Override
            public void onAnimationCancel(Animator animator) {
            }
            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        // and start the animation
        outAnimator.start();
    }

    private Transition createTransition(final TeamScene scene) {
        ChangeBounds animator = new ChangeBounds();
        animator.setDuration(3000);
        animator.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {
                // we need the name of the team we are referencing, get the team
                Team sceneTeam = getTeam(scene.teamIndex);
                Context context = scene.root.getContext();
                int color = context.getColor(scene.teamColor);
                // if we are not doubles then hide the other two views and show the singles text view
                if (isDoubles) {
                    // get the controls we want to set / change
                    TextView playerOneName = scene.root.findViewById(R.id.team_textViewOne);
                    TextView playerTwoName = scene.root.findViewById(R.id.team_textViewTwo);
                    TextView playerNameSep = scene.root.findViewById(R.id.team_textViewSep);
                    // have the team from the current match, we are good to send the request
                    playerOneName.setText(sceneTeam.getPlayerName(0));
                    playerTwoName.setText(sceneTeam.getPlayerName(1));
                    // set the correct colours for the names too
                    playerNameSep.setTextColor(color);
                    playerOneName.setTextColor(color);
                    playerTwoName.setTextColor(color);
                    // setup the server icon for doubles too
                    setupDoublesServerIcons(scene);
                }
                else {
                    // get the controls we want to set / change
                    TextView teamName = scene.root.findViewById(R.id.team_textView);
                    // have the team from the current match, we are good to send the request
                    teamName.setText(sceneTeam.getTeamName());
                    // set the correct colour for this name
                    teamName.setTextColor(color);
                }

                // set the colours of the buttons also
                ImageButton rxButton = scene.root.findViewById(R.id.team_receiverButton);
                BaseActivity.setupButtonIcon(rxButton, R.drawable.ic_tennis_receive, scene.teamColor);
                // colour the server too
                ImageButton txButton = scene.root.findViewById(R.id.team_serverButton);
                BaseActivity.setupButtonIcon(txButton, R.drawable.ic_tennis_serve, scene.teamColor);
                // and show / hide the rx button accordingly
                if (getServingTeam().equals(sceneTeam)) {
                    // this team is currently serving
                    animateIconChange(rxButton, txButton);
                }
                else {
                    // else the team is receiving
                    animateIconChange(txButton, rxButton);
                }
            }
            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                // when the transition ends, setup the buttons again
                setupTeamButtons(scene);
                // and take the opportunity to marquee the team titles
                scrollTeamText(teamOneScene);
                scrollTeamText(teamTwoScene);
                // also setup the server icons as we are showing doubles icons
                if (isDoubles) {
                    setupDoublesServerIcons(scene);
                }
            }
            @Override
            public void onTransitionCancel(@NonNull Transition transition) {
            }
            @Override
            public void onTransitionPause(@NonNull Transition transition) {
            }
            @Override
            public void onTransitionResume(@NonNull Transition transition) {
            }
        });
        return animator;
    }

    private Team getTeam(int teamIndex) {
        Match currentMatch = this.communicator.getCurrentMatch();
        if (null == currentMatch) {
            // cannot as there is no match data
            return null;
        }
        return teamIndex == 0 ? currentMatch.getTeamOne() : currentMatch.getTeamTwo();
    }

    private Team getServingTeam() {
        Match currentMatch = this.communicator.getCurrentMatch();
        if (null == currentMatch) {
            // cannot as there is no match data
            return null;
        }
        return currentMatch.getTeamServing();
    }
}
