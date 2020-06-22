package uk.co.darkerwaters.scorepal.dataui;

import android.content.Context;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.data.PingPongMatch;
import uk.co.darkerwaters.scorepal.data.PingPongScore;
import uk.co.darkerwaters.scorepal.data.PingPongSetup;
import uk.co.darkerwaters.scorepal.points.PingPongPoint;
import uk.co.darkerwaters.scorepal.points.Point;

public class PingPongMatchSpeaker extends MatchSpeaker<PingPongMatch> {

    // special string to annouce our expedite system
    private String expediteMessageString = null;

    @Override
    public String createPointsPhrase(PingPongMatch match, Context context, MatchSetup.Team changeTeam, int level) {
        PingPongSetup setup = match.getSetup();
        StringBuilder message = new StringBuilder();
        String teamOneString = getSpeakingTeamName(context, setup, MatchSetup.Team.T_ONE);
        String teamTwoString = getSpeakingTeamName(context, setup, MatchSetup.Team.T_TWO);
        String changeTeamString = getSpeakingTeamName(context, setup, changeTeam);
        switch (level) {
            case PingPongScore.LEVEL_POINT:
                // the points changed, announce the points
                Point t1Point = match.getDisplayPoint(PingPongScore.LEVEL_POINT, MatchSetup.Team.T_ONE);
                Point t2Point = match.getDisplayPoint(PingPongScore.LEVEL_POINT, MatchSetup.Team.T_TWO);
                // read out the points, winner first
                if (t1Point.val() > t2Point.val()) {
                    // player one has more
                    append(message, t1Point.speakString(context), Point.K_SPEAKING_SPACE);
                    append(message, t2Point.speakString(context), Point.K_SPEAKING_SPACE);
                    append(message, teamOneString);
                } else if (t2Point.val() > t1Point.val()){
                    // player two has more
                    append(message, t2Point.speakString(context), Point.K_SPEAKING_SPACE);
                    append(message, t1Point.speakString(context), Point.K_SPEAKING_SPACE);
                    append(message, teamTwoString);
                } else {
                    // they are the same, read this out
                    if (t1Point.val() == setup.getDecidingPoint()) {
                        // we have a draw at deciding point, this is called 'deuce' in ping-pong
                        append(message, PingPongPoint.DEUCE.speakString(context));
                    }
                    else {
                        // the points are the same, say 15-all or whatever
                        append(message, t1Point.speakAllString(context));
                    }
                }
                break;
            case PingPongScore.LEVEL_ROUND:
                // the rounds changed, announce who won the game
                append(message, PingPongPoint.ROUND.speakString(context), Point.K_SPEAKING_PAUSE);
                // also match?
                if (match.isMatchOver()) {
                    append(message, PingPongPoint.MATCH.speakString(context), Point.K_SPEAKING_PAUSE);
                }
                append(message, changeTeamString, Point.K_SPEAKING_PAUSE_LONG);
                // also we want to say the rounds as they stand

                // team one first
                int rounds = match.getPoint(PingPongScore.LEVEL_ROUND, MatchSetup.Team.T_ONE);
                append(message, teamOneString);
                append(message, rounds);
                append(message, PingPongPoint.ROUND.speakString(context, rounds), Point.K_SPEAKING_PAUSE);

                // team two now
                rounds = match.getPoint(PingPongScore.LEVEL_ROUND, MatchSetup.Team.T_TWO);
                append(message, teamTwoString);
                append(message, rounds);
                append(message, PingPongPoint.ROUND.speakString(context, rounds), Point.K_SPEAKING_PAUSE);
                break;
        }
        if (match.getIsAnnounceExpediteSystem()) {
            // we are to announce the commencement of the expedite system
            append(message, Point.K_SPEAKING_PAUSE);
            if (this.expediteMessageString == null) {
                this.expediteMessageString = context == null ? "commence expedite" : context.getString(R.string.speak_expedite_system);
            }
            append(message, this.expediteMessageString);
            match.expediteSystemAnnounced();
        }
        // and return the complicated message to speak
        return message.toString();
    }

    @Override
    public String getLevelTitle(int level, Context context) {
        switch (level) {
            case PingPongScore.LEVEL_POINT :
                return PingPongPoint.POINT.speakString(context);
            case PingPongScore.LEVEL_ROUND :
                return PingPongPoint.ROUND.speakString(context);
        }
        return super.getLevelTitle(level, context);
    }

    @Override
    public String createPointsAnnouncement(PingPongMatch match, Context context) {
        PingPongSetup setup = match.getSetup();

        MatchSetup.Team teamServing = match.getServingTeam();
        MatchSetup.Team teamReceiving = setup.getOtherTeam(teamServing);
        int serverPoints = match.getPoint(PingPongScore.LEVEL_POINT, teamServing);
        int receiverPoints = match.getPoint(PingPongScore.LEVEL_POINT, teamReceiving);

        String serverString = getSpeakingTeamName(context, setup, teamServing);
        String receiverString = getSpeakingTeamName(context, setup, teamReceiving);

        String message;
        if (serverPoints + receiverPoints > 0) {
            // there are points, say the points
            message = createPointsPhrase(match, context, teamServing, PingPongScore.LEVEL_POINT);
        }
        else {
            // no points scored, are there any rounds?
            int serverRounds = match.getPoint(PingPongScore.LEVEL_ROUND, teamServing);
            int receiverRounds = match.getPoint(PingPongScore.LEVEL_ROUND, teamReceiving);
            StringBuilder messageBuilder = new StringBuilder();
            if (serverRounds + receiverRounds > 0) {
                // announce the games
                append(messageBuilder, serverString, Point.K_SPEAKING_PAUSE_SLIGHT);
                append(messageBuilder, serverRounds, Point.K_SPEAKING_PAUSE_SLIGHT);
                append(messageBuilder, PingPongPoint.ROUND.speakString(context, serverRounds), Point.K_SPEAKING_PAUSE);
                append(messageBuilder, receiverString, Point.K_SPEAKING_PAUSE_SLIGHT);
                append(messageBuilder, receiverRounds, Point.K_SPEAKING_PAUSE_SLIGHT);
                append(messageBuilder, PingPongPoint.ROUND.speakString(context, receiverRounds));
                append(messageBuilder, context == null ? "rounds" : context.getString(R.string.rounds));
            }
            message = messageBuilder.toString();

        }
        // return the message created
        return message;
    }
}
