package uk.co.darkerwaters.scorepal.score.squash;

import android.content.Context;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.MatchSpeaker;
import uk.co.darkerwaters.scorepal.score.base.Point;
import uk.co.darkerwaters.scorepal.score.base.PointChange;

public class SquashMatchSpeaker extends MatchSpeaker<SquashMatch> {
    
    @Override
    public String createPointsPhrase(SquashMatch match, Context context, PointChange change) {
        SquashScore score = match.getScore();
        Team teamOne = match.getTeamOne();
        Team teamTwo = match.getTeamTwo();
        StringBuilder message = new StringBuilder();
        String teamOneString = teamOne.getSpeakingTeamName();
        String teamTwoString = teamTwo.getSpeakingTeamName();
        String changeTeamString = change.team.getSpeakingTeamName();
        switch (change.level) {
            case SquashScore.LEVEL_POINT:
                // the points changed, announce the points
                Point t1Point = score.getDisplayPoint(0, teamOne);
                Point t2Point = score.getDisplayPoint(0, teamTwo);
                // read out the points, winner first
                if (t1Point.val() > t2Point.val()) {
                    // player one has more
                    message.append(t1Point.speakString(context));
                    message.append(Point.K_SPEAKING_SPACE);
                    message.append(t2Point.speakString(context));
                    message.append(Point.K_SPEAKING_SPACE);
                    message.append(teamOneString);
                } else if (t2Point.val() > t1Point.val()){
                    // player two has more
                    message.append(t2Point.speakString(context));
                    message.append(Point.K_SPEAKING_SPACE);
                    message.append(t1Point.speakString(context));
                    message.append(Point.K_SPEAKING_SPACE);
                    message.append(teamTwoString);
                } else {
                    // the points are the same
                    message.append(t1Point.speakAllString(context));
                }
                break;
            case SquashScore.LEVEL_GAME:
                // the games changed, announce who won the game
                message.append(SquashPoint.GAME.speakString(context));
                message.append(Point.K_SPEAKING_PAUSE);
                // also match?
                if (score.isMatchOver()) {
                    message.append(SquashPoint.MATCH.speakString(context));
                    message.append(Point.K_SPEAKING_PAUSE);
                }
                message.append(changeTeamString);
                // also we want to say the games as they stand
                message.append(Point.K_SPEAKING_PAUSE_LONG);

                // team one first
                int games = score.getGames(teamOne);
                message.append(teamOneString);
                message.append(games);
                message.append(SquashPoint.GAME.speakString(context, games));
                message.append(Point.K_SPEAKING_PAUSE);

                // team two now
                games = score.getGames(teamTwo);
                message.append(teamTwoString);
                message.append(games);
                message.append(SquashPoint.GAME.speakString(context, games));
                message.append(Point.K_SPEAKING_PAUSE);
                break;
        }
        // and return the complicated message to speak
        return message.toString();
    }

    @Override
    public String createPointsAnnouncement(SquashMatch match, Context context) {
        SquashScore score = match.getScore();

        Team teamServing = match.getTeamServing();
        Team teamReceiving = match.getOtherTeam(teamServing);
        int serverPoints = score.getPoints(teamServing);
        int receiverPoints = score.getPoints(teamReceiving);

        String serverString = teamServing.getSpeakingTeamName();
        String receiverString = teamReceiving.getSpeakingTeamName();

        String message;
        if (serverPoints + receiverPoints > 0) {
            // there are points, say the points
            message = createPointsPhrase(match, context, new PointChange(teamServing, SquashScore.LEVEL_POINT, serverPoints));
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
                messageBuilder.append(SquashPoint.GAME.speakString(context, serverGames));
                messageBuilder.append(Point.K_SPEAKING_PAUSE);
                messageBuilder.append(receiverString);
                messageBuilder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
                messageBuilder.append(receiverGames);
                messageBuilder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
                messageBuilder.append(SquashPoint.GAME.speakString(context, receiverGames));
                messageBuilder.append(context.getString(R.string.games));
            }
            message = messageBuilder.toString();

        }
        // return the message created
        return message;
    }
}
