package uk.co.darkerwaters.scorepal.dataui;

import android.content.Context;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.BadmintonMatch;
import uk.co.darkerwaters.scorepal.data.BadmintonScore;
import uk.co.darkerwaters.scorepal.data.BadmintonSetup;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.points.BadmintonPoint;
import uk.co.darkerwaters.scorepal.points.Point;

public class BadmintonMatchSpeaker extends MatchSpeaker<BadmintonMatch> {
    
    @Override
    public String createPointsPhrase(BadmintonMatch match, Context context, MatchSetup.Team changeTeam, int level) {
        BadmintonSetup setup = match.getSetup();
        StringBuilder message = new StringBuilder();
        String teamOneString = getSpeakingTeamName(context, setup, MatchSetup.Team.T_ONE);
        String teamTwoString = getSpeakingTeamName(context, setup, MatchSetup.Team.T_TWO);
        String changeTeamString = getSpeakingTeamName(context, setup, changeTeam);
        switch (level) {
            case BadmintonScore.LEVEL_POINT:
                // the points changed, announce the points
                Point t1Point = match.getDisplayPoint(0, MatchSetup.Team.T_ONE);
                Point t2Point = match.getDisplayPoint(0, MatchSetup.Team.T_TWO);
                // just read the numbers out, but we want to say the server first
                // so who is that?
                if (t1Point.val() == t2Point.val()) {
                    // we are drawing
                    append(message, t1Point.speakAllString(context));
                }
                else if (match.getServingTeam() == MatchSetup.Team.T_ONE) {
                    // team one is serving
                    append(message, t1Point.speakString(context), Point.K_SPEAKING_SPACE);
                    append(message, t2Point.speakString(context));
                } else {
                    // team two is serving
                    append(message, t2Point.speakString(context), Point.K_SPEAKING_SPACE);
                    append(message, t1Point.speakString(context));
                }
                break;
            case BadmintonScore.LEVEL_GAME:
                // the games changed, announce who won the game
                append(message, BadmintonPoint.GAME.speakString(context), Point.K_SPEAKING_PAUSE);
                // also match?
                if (match.isMatchOver()) {
                    append(message, BadmintonPoint.MATCH.speakString(context), Point.K_SPEAKING_PAUSE);
                }
                append(message, changeTeamString);
                // also we want to say the games as they stand
                append(message, Point.K_SPEAKING_PAUSE_LONG);

                // team one first
                int games = match.getPoint(BadmintonScore.LEVEL_GAME, MatchSetup.Team.T_ONE);
                append(message, teamOneString);
                append(message, games);
                append(message, BadmintonPoint.GAME.speakString(context, games), Point.K_SPEAKING_PAUSE);

                // team two now
                games = match.getPoint(BadmintonScore.LEVEL_GAME, MatchSetup.Team.T_TWO);
                append(message, teamTwoString);
                append(message, games);
                append(message, BadmintonPoint.GAME.speakString(context, games), Point.K_SPEAKING_PAUSE);
                break;
        }
        // and return the complicated message to speak
        return message.toString();
    }

    @Override
    public String getLevelTitle(int level, Context context) {
        switch (level) {
            case BadmintonScore.LEVEL_POINT :
                return BadmintonPoint.POINT.speakString(context);
            case BadmintonScore.LEVEL_GAME :
                return BadmintonPoint.GAME.speakString(context);
        }
        return super.getLevelTitle(level, context);
    }

    @Override
    public String createPointsAnnouncement(BadmintonMatch match, Context context) {
        BadmintonSetup setup = match.getSetup();

        MatchSetup.Team teamServing = match.getServingTeam();
        MatchSetup.Team teamReceiving = setup.getOtherTeam(teamServing);
        int serverPoints = match.getPoint(BadmintonScore.LEVEL_POINT, teamServing);
        int receiverPoints = match.getPoint(BadmintonScore.LEVEL_POINT, teamReceiving);

        String serverString = getSpeakingTeamName(context, setup, teamServing);
        String receiverString = getSpeakingTeamName(context, setup, teamReceiving);

        String message;
        if (serverPoints + receiverPoints > 0) {
            // there are points, say the points
            message = createPointsPhrase(match, context, teamServing, BadmintonScore.LEVEL_POINT);
        }
        else {
            // no points scored, are there any games?
            int serverGames = match.getPoint(BadmintonScore.LEVEL_GAME, teamServing);
            int receiverGames = match.getPoint(BadmintonScore.LEVEL_GAME, teamReceiving);
            StringBuilder messageBuilder = new StringBuilder();
            if (serverGames + receiverGames > 0) {
                // announce the games
                append(messageBuilder, serverString, Point.K_SPEAKING_PAUSE_SLIGHT);
                append(messageBuilder, serverGames, Point.K_SPEAKING_PAUSE_SLIGHT);
                append(messageBuilder, BadmintonPoint.GAME.speakString(context, serverGames), Point.K_SPEAKING_PAUSE);
                append(messageBuilder, receiverString, Point.K_SPEAKING_PAUSE_SLIGHT);
                append(messageBuilder, receiverGames, Point.K_SPEAKING_PAUSE_SLIGHT);
                append(messageBuilder, BadmintonPoint.GAME.speakString(context, receiverGames));
                append(messageBuilder, context.getString(R.string.games));
            }
            message = messageBuilder.toString();

        }
        // return the message created
        return message;
    }
}
