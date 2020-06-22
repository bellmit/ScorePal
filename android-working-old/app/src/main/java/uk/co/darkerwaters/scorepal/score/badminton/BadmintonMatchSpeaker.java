package uk.co.darkerwaters.scorepal.score.badminton;

import android.content.Context;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.MatchSpeaker;
import uk.co.darkerwaters.scorepal.score.base.Point;
import uk.co.darkerwaters.scorepal.score.base.PointChange;

public class BadmintonMatchSpeaker extends MatchSpeaker<BadmintonMatch> {
    
    @Override
    public String createPointsPhrase(BadmintonMatch match, Context context, PointChange change) {
        BadmintonScore score = match.getScore();
        Team teamOne = match.getTeamOne();
        Team teamTwo = match.getTeamTwo();
        StringBuilder message = new StringBuilder();
        String teamOneString = teamOne.getSpeakingTeamName();
        String teamTwoString = teamTwo.getSpeakingTeamName();
        String changeTeamString = change.team.getSpeakingTeamName();
        switch (change.level) {
            case BadmintonScore.LEVEL_POINT:
                // the points changed, announce the points
                Point t1Point = score.getDisplayPoint(0, teamOne);
                Point t2Point = score.getDisplayPoint(0, teamTwo);
                // just read the numbers out, but we want to say the server first
                // so who is that?
                if (t1Point.val() == t2Point.val()) {
                    // we are drawing
                    message.append(t1Point.speakAllString(context));
                }
                else if (teamOne.isPlayerInTeam(match.getCurrentServer())) {
                    // team one is serving
                    message.append(t1Point.speakString(context));
                    message.append(Point.K_SPEAKING_SPACE);
                    message.append(t2Point.speakString(context));
                } else {
                    // team two is serving
                    message.append(t2Point.speakString(context));
                    message.append(Point.K_SPEAKING_SPACE);
                    message.append(t1Point.speakString(context));
                }
                break;
            case BadmintonScore.LEVEL_GAME:
                // the games changed, announce who won the game
                message.append(BadmintonPoint.GAME.speakString(context));
                message.append(Point.K_SPEAKING_PAUSE);
                // also match?
                if (score.isMatchOver()) {
                    message.append(BadmintonPoint.MATCH.speakString(context));
                    message.append(Point.K_SPEAKING_PAUSE);
                }
                message.append(changeTeamString);
                // also we want to say the games as they stand
                message.append(Point.K_SPEAKING_PAUSE_LONG);

                // team one first
                int games = score.getGames(teamOne);
                message.append(teamOneString);
                message.append(games);
                message.append(BadmintonPoint.GAME.speakString(context, games));
                message.append(Point.K_SPEAKING_PAUSE);

                // team two now
                games = score.getGames(teamTwo);
                message.append(teamTwoString);
                message.append(games);
                message.append(BadmintonPoint.GAME.speakString(context, games));
                message.append(Point.K_SPEAKING_PAUSE);
                break;
        }
        // and return the complicated message to speak
        return message.toString();
    }

    @Override
    public String createPointsAnnouncement(BadmintonMatch match, Context context) {
        BadmintonScore score = match.getScore();

        Team teamServing = match.getTeamServing();
        Team teamReceiving = match.getOtherTeam(teamServing);
        int serverPoints = score.getPoints(teamServing);
        int receiverPoints = score.getPoints(teamReceiving);

        String serverString = teamServing.getSpeakingTeamName();
        String receiverString = teamReceiving.getSpeakingTeamName();

        String message;
        if (serverPoints + receiverPoints > 0) {
            // there are points, say the points
            message = createPointsPhrase(match, context, new PointChange(teamServing, BadmintonScore.LEVEL_POINT, serverPoints));
        }
        else {
            // no points scored, are there any games?
            int serverGames = score.getGames(teamServing);
            int receiverGames = score.getGames(teamReceiving);
            StringBuilder messageBuilder = new StringBuilder();
            if (serverGames + receiverGames > 0) {
                // announce the games
                messageBuilder.append(serverString);
                messageBuilder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
                messageBuilder.append(serverGames);
                messageBuilder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
                messageBuilder.append(BadmintonPoint.GAME.speakString(context, serverGames));
                messageBuilder.append(Point.K_SPEAKING_PAUSE);
                messageBuilder.append(receiverString);
                messageBuilder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
                messageBuilder.append(receiverGames);
                messageBuilder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
                messageBuilder.append(BadmintonPoint.GAME.speakString(context, receiverGames));
                messageBuilder.append(context.getString(R.string.games));
            }
            message = messageBuilder.toString();

        }
        // return the message created
        return message;
    }
}
