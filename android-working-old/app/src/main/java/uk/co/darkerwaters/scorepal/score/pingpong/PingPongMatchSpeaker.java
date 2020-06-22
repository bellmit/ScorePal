package uk.co.darkerwaters.scorepal.score.pingpong;

import android.content.Context;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.MatchSpeaker;
import uk.co.darkerwaters.scorepal.score.base.Point;
import uk.co.darkerwaters.scorepal.score.base.PointChange;
import uk.co.darkerwaters.scorepal.score.tennis.TennisPoint;

public class PingPongMatchSpeaker extends MatchSpeaker<PingPongMatch> {

    // special string to annouce our expedite system
    private String expediteMessageString = null;

    @Override
    public String createPointsPhrase(PingPongMatch match, Context context, PointChange change) {
        PingPongScore score = match.getScore();
        Team teamOne = match.getTeamOne();
        Team teamTwo = match.getTeamTwo();
        StringBuilder message = new StringBuilder();
        String teamOneString = teamOne.getSpeakingTeamName();
        String teamTwoString = teamTwo.getSpeakingTeamName();
        String changeTeamString = change.team.getSpeakingTeamName();
        switch (change.level) {
            case PingPongScore.LEVEL_POINT:
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
                    // they are the same, read this out
                    if (t1Point.val() == match.getDecidingPoint()) {
                        // we have a draw at deciding point, this is called 'deuce' in ping-pong
                        message.append(PingPongPoint.DEUCE.speakString(context));
                    }
                    else {
                        // the points are the same, say 15-all or whatever
                        message.append(t1Point.speakAllString(context));
                    }
                }
                break;
            case PingPongScore.LEVEL_ROUND:
                // the rounds changed, announce who won the game
                message.append(PingPongPoint.ROUND.speakString(context));
                message.append(Point.K_SPEAKING_PAUSE);
                // also match?
                if (score.isMatchOver()) {
                    message.append(PingPongPoint.MATCH.speakString(context));
                    message.append(Point.K_SPEAKING_PAUSE);
                }
                message.append(changeTeamString);
                // also we want to say the rounds as they stand
                message.append(Point.K_SPEAKING_PAUSE_LONG);

                // team one first
                int rounds = score.getRounds(teamOne);
                message.append(teamOneString);
                message.append(rounds);
                message.append(PingPongPoint.ROUND.speakString(context, rounds));
                message.append(Point.K_SPEAKING_PAUSE);

                // team two now
                rounds = score.getRounds(teamTwo);
                message.append(teamTwoString);
                message.append(rounds);
                message.append(PingPongPoint.ROUND.speakString(context, rounds));
                message.append(Point.K_SPEAKING_PAUSE);
                break;
        }
        if (match.getIsAnnounceExpediteSystem()) {
            // we are to announce the commencement of the expedite system
            message.append(Point.K_SPEAKING_PAUSE);
            if (this.expediteMessageString == null) {
                this.expediteMessageString = context.getString(R.string.speak_expedite_system);
            }
            message.append(this.expediteMessageString);
            match.expediteSystemAnnounced();
        }
        // and return the complicated message to speak
        return message.toString();
    }

    @Override
    public String createPointsAnnouncement(PingPongMatch match, Context context) {
        PingPongScore score = match.getScore();

        Team teamServing = match.getTeamServing();
        Team teamReceiving = match.getOtherTeam(teamServing);
        int serverPoints = score.getPoints(teamServing);
        int receiverPoints = score.getPoints(teamReceiving);

        String serverString = teamServing.getSpeakingTeamName();
        String receiverString = teamReceiving.getSpeakingTeamName();

        String message;
        if (serverPoints + receiverPoints > 0) {
            // there are points, say the points
            message = createPointsPhrase(match, context, new PointChange(teamServing, PingPongScore.LEVEL_POINT, serverPoints));
        }
        else {
            // no points scored, are there any rounds?
            int serverRounds = score.getRounds(teamServing);
            int receiverRounds = score.getRounds(teamReceiving);
            StringBuilder messageBuilder = new StringBuilder();
            if (serverRounds + receiverRounds > 0) {
                // announce the games
                messageBuilder.append(serverString);
                messageBuilder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
                messageBuilder.append(serverRounds);
                messageBuilder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
                messageBuilder.append(PingPongPoint.ROUND.speakString(context, serverRounds));
                messageBuilder.append(Point.K_SPEAKING_PAUSE);
                messageBuilder.append(receiverString);
                messageBuilder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
                messageBuilder.append(receiverRounds);
                messageBuilder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
                messageBuilder.append(PingPongPoint.ROUND.speakString(context, receiverRounds));
                messageBuilder.append(context.getString(R.string.rounds));
            }
            message = messageBuilder.toString();

        }
        // return the message created
        return message;
    }
}
