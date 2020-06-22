package uk.co.darkerwaters.scorepal.dataui;

import android.content.Context;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.ApplicationPreferences;
import uk.co.darkerwaters.scorepal.application.ApplicationState;
import uk.co.darkerwaters.scorepal.data.Match;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.data.ScoreState;
import uk.co.darkerwaters.scorepal.points.Point;

import static uk.co.darkerwaters.scorepal.data.ScoreState.ScoreChange.DECIDING_POINT;
import static uk.co.darkerwaters.scorepal.data.ScoreState.ScoreChange.DECREMENT;
import static uk.co.darkerwaters.scorepal.data.ScoreState.ScoreChange.ENDS;
import static uk.co.darkerwaters.scorepal.data.ScoreState.ScoreChange.INCREMENT;
import static uk.co.darkerwaters.scorepal.data.ScoreState.ScoreChange.SERVER;
import static uk.co.darkerwaters.scorepal.data.ScoreState.ScoreChange.TIE_BREAK;

public class MatchSpeaker<T extends Match> {

    public MatchSpeaker() {
    }

    public String createPointsPhrase(T match, Context context, MatchSetup.Team team, int level) {
        MatchSetup setup = match.getSetup();

        MatchSetup.Team otherTeam = setup.getOtherTeam(team);
        // formulate the message
        StringBuilder builder = new StringBuilder();
        builder.append(getSpeakingTeamName(context, setup, team));
        builder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
        builder.append(match.getPoint(level, team));
        builder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
        builder.append(context.getString(R.string.speak_points));
        builder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
        builder.append(getSpeakingTeamName(context, setup, otherTeam));
        builder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
        builder.append(match.getPoint(level, otherTeam));
        builder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
        builder.append(context.getString(R.string.speak_points));
        // and return this phrase as a nice string
        return builder.toString();
    }

    public String createScorePhrase(T match, Context context, MatchSetup.Team team, int level) {
        return createPointsPhrase(match, context, team, level);
    }

    public String getLevelTitle(int level, Context context) {
        if (null == context) {
            return "Level " + (level + 1);
        }
        else {
            return context.getString(R.string.scoreSummaryLevel, level);
        }
    }

    public static String getSpeakingTeamName(Context context, MatchSetup setup, MatchSetup.Team team) {
        if (ApplicationState.Initialise(context).getPreferences().getSoundUseSpeakingNames()) {
            // use the player's names to speak
            String teamName = setup.getTeamName(context, team);
            // remove all the punctuation from the team name so there are no weird pauses in it.
            if (teamName == null || teamName.isEmpty()) {
                return teamName;
            } else {
                return teamName.replaceAll("[.]", "");
            }
        }
        else {
            if (setup.getType() == MatchSetup.MatchType.DOUBLES) {
                // return the pre-determined team string
                return context == null ? "Doubles" : context.getString(team.stringRes);
            }
            else {
                // it's just the player on their own
                return context == null ? "Singles" : context.getString(setup.getTeamPlayer(team).stringRes);
            }
        }
    }

    public static String getSpeakingPlayerName(Context context, MatchSetup setup, MatchSetup.Player player) {
        if (ApplicationState.Initialise(context).getPreferences().getSoundUseSpeakingNames()) {
            // use the player's names to speak
            String playerName = setup.getNamer().getPlayerName(context, player);
            // remove all the punctuation from the team name so there are no weird pauses in it.
            if (playerName == null || playerName.isEmpty()) {
                return playerName;
            } else {
                return playerName.replaceAll("[.]", "");
            }
        }
        else {
            // return the pre-determined string
            return context == null ? "Player" : context.getString(player.stringRes);
        }
    }

    public String createPointsAnnouncement(T match, Context context) {
        int topLevel = match.getScoreLevels() - 1;
        MatchSetup.Team team = match.getServingTeam();
        return createPointsPhrase(match, context, team, topLevel);
    }

    public String getSpeakingStateMessage(Context context, T match, ScoreState state) {
        ApplicationPreferences preferences = ApplicationState.Instance().getPreferences();
        // now handle the changes here to announce what happened
        StringBuilder spokenMessage = new StringBuilder();
        MatchSetup setup = match.getSetup();
        if (preferences.getSoundButtonClick()) {
            //TODO the click of the button
        }
        if (preferences.getSoundActionSpeak() && null != context) {
            if (state.isChanged(DECREMENT)) {
                if (!preferences.getSoundAnnounceChange()) {
                    // we wont say the correction message - so say here
                    append(spokenMessage, context.getString(R.string.correction), Point.K_SPEAKING_SPACE);
                }
            }
            else if (state.isChanged(INCREMENT)) {
                // only speak the action if we are not speaking the score as points change
                if (state.getLevelChanged() == 0 ||
                        !preferences.getSoundAnnounceChange() ||
                        !preferences.getSoundAnnounceChangePoints()) {
                    // the level is points - say, or we arn't announcing points - so announce that they pushed the button
                    append(spokenMessage, getLevelTitle(state.getLevelChanged(), context), Point.K_SPEAKING_SPACE);
                    append(spokenMessage, MatchSpeaker.getSpeakingTeamName(context, setup, state.getTeamChanged()), Point.K_SPEAKING_PAUSE);
                }
            }
        }
        if (preferences.getSoundAnnounceChange() && null != context) {
            // they want us to announce any change, so build the string to say
            if (state.isChanged(DECREMENT)) {
                // this is a correction
                append(spokenMessage, context.getString(R.string.correction), Point.K_SPEAKING_PAUSE);
                // and just remind them of the points
                if (preferences.getSoundAnnounceChangePoints()) {
                    // and we want to say it
                    append(spokenMessage, match.createPointsPhrase(context, state.getTeamChanged(), 0), Point.K_SPEAKING_PAUSE);
                }
            }
            else {
                if (state.isChanged(INCREMENT)) {
                    // this is a change in the score, add the score to the announcement
                    if (preferences.getSoundAnnounceChangePoints()) {
                        // and we want to say it
                        append(spokenMessage, match.createPointsPhrase(context, state.getTeamChanged(), state.getLevelChanged()), Point.K_SPEAKING_PAUSE);
                    }
                }
                // add the extra details here
                if (!match.isMatchOver()) {
                    // don't announce any of this change in state if the match is over
                    if (state.isChanged(DECIDING_POINT)) {
                        append(spokenMessage, context.getString(R.string.deciding_point), Point.K_SPEAKING_SPACE);
                    }
                    if (state.isChanged(ENDS) && preferences.getSoundAnnounceChangeEnds()) {
                        append(spokenMessage, context.getString(R.string.change_ends), Point.K_SPEAKING_SPACE);
                    }
                    if (state.isChanged(SERVER) && preferences.getSoundAnnounceChangeServer()) {
                        // change the server
                        //append(spokenMessage, context.getString(R.string.change_server));
                        // append the name of the server
                        String serverName = MatchSpeaker.getSpeakingPlayerName(context, setup, match.getServingPlayer());
                        // and make the message include the name of the player to serve
                        append(spokenMessage, context.getString(R.string.change_server_server, serverName), Point.K_SPEAKING_SPACE);
                    }
                    if (state.isChanged(TIE_BREAK)) {
                        // this is a tie, say this as it's super important
                        append(spokenMessage, context.getString(R.string.tie_break), Point.K_SPEAKING_SPACE);
                    }
                }
                if (state.isChanged(INCREMENT)) {
                    // and summarise the larger score
                    if (preferences.getSoundAnnounceChangeScore()) {
                        append(spokenMessage, Point.K_SPEAKING_PAUSE_LONG);
                        append(spokenMessage, match.createScorePhrase(context, state.getTeamChanged(), state.getLevelChanged()), Point.K_SPEAKING_SPACE);
                    }
                }
            }
        }
        // and speak this
        return spokenMessage.toString();
    }

    protected void append(StringBuilder message, String spokenMessage) {
        append(message, spokenMessage, "");
    }

    protected void append(StringBuilder message, int spokenNumber) {
        append(message, Integer.toString(spokenNumber), "");
    }

    protected void append(StringBuilder message, int spokenNumber, String pause) {
        append(message, Integer.toString(spokenNumber), pause);
    }

    protected void append(StringBuilder message, String spokenMessage, String pause) {
        if (null != spokenMessage && false == spokenMessage.isEmpty()) {
            // there is a state showing and the match isn't over, speak it here
            message.append(spokenMessage + pause);
        }
    }
}
