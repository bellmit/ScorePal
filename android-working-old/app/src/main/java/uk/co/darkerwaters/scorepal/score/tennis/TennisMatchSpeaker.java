package uk.co.darkerwaters.scorepal.score.tennis;

import android.content.Context;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.MatchSpeaker;
import uk.co.darkerwaters.scorepal.score.base.Point;
import uk.co.darkerwaters.scorepal.score.base.PointChange;

public class TennisMatchSpeaker extends MatchSpeaker<TennisMatch> {

    @Override
    public String createPointsPhrase(TennisMatch match, Context context, PointChange change) {
        String message = "";
        switch (change.level) {
            case TennisScore.LEVEL_POINT:
                // the points changed, announce the points
                message = createPhrasePoints(match, context, change);
                break;
            case TennisScore.LEVEL_GAME:
                // the games changed, announce who won the game
                message = createPhraseGames(match, context, change);
                break;
            case TennisScore.LEVEL_SET:
                // the sets changed, announce who won the game and the set
                message = createPhraseSets(match, context, change);
                break;
        }
        // and return the complicated message to speak
        return message;
    }

    private String createPhraseSets(TennisMatch match, Context context, PointChange change) {
        // create the announcement of the set result on this match
        TennisScore score = match.getScore();
        Team teamOne = match.getTeamOne();
        Team teamTwo = match.getTeamTwo();
        String teamOneString = teamOne.getSpeakingTeamName();
        String teamTwoString = teamTwo.getSpeakingTeamName();
        String changeTeamString = change.team.getSpeakingTeamName();
        StringBuilder message = new StringBuilder();

        message.append(TennisPoint.GAME.speakString(context));
        message.append(Point.K_SPEAKING_PAUSE);
        message.append(TennisPoint.SET.speakString(context));
        message.append(Point.K_SPEAKING_PAUSE);
        // also match?
        if (score.isMatchOver()) {
            message.append(TennisPoint.MATCH.speakString(context));
            message.append(Point.K_SPEAKING_PAUSE);
        }
        // add the winner's name
        message.append(changeTeamString);

        if (!score.isMatchOver()) {
            // match isn't over, want to read out the sets we have one
            message.append(Point.K_SPEAKING_PAUSE);

            int setsOne = score.getSets(teamOne);
            int setsTwo = score.getSets(teamTwo);

            if (setsOne == setsTwo) {
                // we are equal, say '1 set(s) all'
                message.append(context.getString(R.string.speak_number_all,
                        setsOne,
                        TennisPoint.SET.speakString(context, setsOne)));
                message.append(Point.K_SPEAKING_PAUSE);
            }
            else {
                // team one first
                message.append(teamOneString);
                message.append(setsOne);
                message.append(TennisPoint.SET.speakString(context, setsOne));
                message.append(Point.K_SPEAKING_PAUSE);

                // team two first
                message.append(teamTwoString);
                message.append(setsTwo);
                message.append(TennisPoint.SET.speakString(context, setsTwo));
                message.append(Point.K_SPEAKING_PAUSE);
            }
        }
        else {
            // we want to also read out the games from each set
            message.append(Point.K_SPEAKING_PAUSE_LONG);

            Team winnerTeam;
            Team loserTeam;
            if (change.team.equals(teamOne)) {
                // team one is the winner
                winnerTeam = teamOne;
                loserTeam = teamTwo;
            }
            else {
                // team two is the winner
                winnerTeam = teamTwo;
                loserTeam = teamOne;
            }
            for (int i = 0; i < score.getPlayedSets(); ++i) {
                message.append(Point.K_SPEAKING_PAUSE_LONG);
                message.append(score.getGames(winnerTeam, i));
                message.append(Point.K_SPEAKING_PAUSE_SLIGHT);
                message.append(score.getGames(loserTeam, i));
                message.append(Point.K_SPEAKING_PAUSE_SLIGHT);
            }
        }
        return message.toString();
    }

    private String createPhraseGames(TennisMatch match, Context context, PointChange change) {
        // create the announcement of games on this match
        TennisScore score = match.getScore();
        Team teamOne = match.getTeamOne();
        Team teamTwo = match.getTeamTwo();
        String teamOneString = teamOne.getSpeakingTeamName();
        String teamTwoString = teamTwo.getSpeakingTeamName();
        String changeTeamString = change.team.getSpeakingTeamName();
        StringBuilder message = new StringBuilder();

        message.append(TennisPoint.GAME.speakString(context));
        message.append(Point.K_SPEAKING_PAUSE);
        message.append(changeTeamString);
        // also we want to say the games as they stand
        message.append(Point.K_SPEAKING_PAUSE_LONG);

        int gamesOne = score.getGames(teamOne, -1);
        int gamesTwo = score.getGames(teamTwo, -1);

        if (gamesOne == gamesTwo) {
            // we are equal, say '1 game(s) all'
            message.append(context.getString(R.string.speak_number_all,
                    gamesOne,
                    TennisPoint.GAME.speakString(context, gamesOne)));
            message.append(Point.K_SPEAKING_PAUSE);
        }
        else {
            // team one first
            message.append(teamOneString);
            message.append(gamesOne);
            message.append(TennisPoint.GAME.speakString(context, gamesOne));
            message.append(Point.K_SPEAKING_PAUSE);

            // team two first
            message.append(teamTwoString);
            message.append(gamesTwo);
            message.append(TennisPoint.GAME.speakString(context, gamesTwo));
            message.append(Point.K_SPEAKING_PAUSE);
        }

        return message.toString();
    }

    private String createPhrasePoints(TennisMatch match, Context context, PointChange change) {
        // create the announcement of points on this match
        TennisScore score = match.getScore();
        Team teamOne = match.getTeamOne();
        Team teamTwo = match.getTeamTwo();
        String teamOneString = teamOne.getSpeakingTeamName();
        String teamTwoString = teamTwo.getSpeakingTeamName();
        Point t1Point = score.getDisplayPoint(teamOne);
        Point t2Point = score.getDisplayPoint(teamTwo);
        StringBuilder message = new StringBuilder();

        if (t1Point == TennisPoint.ADVANTAGE) {
            // read advantage team one
            message.append(t1Point.speakString(context));
            message.append(Point.K_SPEAKING_SPACE);
            message.append(teamOneString);
        } else if (t2Point == TennisPoint.ADVANTAGE) {
            // read advantage team two
            message.append(t2Point.speakString(context));
            message.append(Point.K_SPEAKING_SPACE);
            message.append(teamTwoString);
        } else if (t1Point == TennisPoint.DEUCE
                && t2Point == TennisPoint.DEUCE) {
            // read deuce
            message.append(t1Point.speakString(context));
        } else if (t1Point.val() == t2Point.val()) {
            // they have the same score, use the special "all" values
            message.append(t1Point.speakAllString(context));
        } else if (score.isInTieBreak()) {
            // in a tie-break we read the score with the winner first
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
        } else {
            // just read the numbers out, but we want to say the server first
            // so who is that?
            if (teamOne.isPlayerInTeam(match.getCurrentServer())) {
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
        }
        return message.toString();
    }

    @Override
    public String createPointsAnnouncement(TennisMatch match, Context context) {
        TennisScore score = match.getScore();

        Team teamServing = match.getTeamServing();
        Team teamReceiving = match.getOtherTeam(teamServing);
        int serverPoints = score.getPoints(teamServing);
        int receiverPoints = score.getPoints(teamReceiving);

        String serverString = teamServing.getSpeakingTeamName();
        String receiverString = teamReceiving.getSpeakingTeamName();

        String message;
        if (serverPoints + receiverPoints > 0) {
            // there are points, say the points
            message = createPointsPhrase(match, context, new PointChange(teamServing, TennisScore.LEVEL_POINT, serverPoints));
        }
        else {
            // no points scored, are there any games?
            int serverGames = score.getGames(teamServing, -1);
            int receiverGames = score.getGames(teamReceiving, -1);
            StringBuilder messageBuilder = new StringBuilder();
            if (serverGames + receiverGames > 0) {
                // announce the games
                messageBuilder.append(serverString);
                messageBuilder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
                messageBuilder.append(serverGames);
                messageBuilder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
                messageBuilder.append(TennisPoint.GAME.speakString(context, serverGames));
                messageBuilder.append(Point.K_SPEAKING_PAUSE);
                messageBuilder.append(receiverString);
                messageBuilder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
                messageBuilder.append(receiverGames);
                messageBuilder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
                messageBuilder.append(TennisPoint.GAME.speakString(context, receiverGames));
            }
            else {
                // do the sets
                int serverSets = score.getSets(teamServing);
                int receiverSets = score.getSets(teamReceiving);
                if (receiverSets + serverSets > 0) {
                    // announce the sets
                    messageBuilder.append(serverString);
                    messageBuilder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
                    messageBuilder.append(serverSets);
                    messageBuilder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
                    messageBuilder.append(TennisPoint.SET.speakString(context, serverSets));
                    messageBuilder.append(Point.K_SPEAKING_PAUSE);
                    messageBuilder.append(receiverString);
                    messageBuilder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
                    messageBuilder.append(receiverSets);
                    messageBuilder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
                    messageBuilder.append(TennisPoint.SET.speakString(context, receiverSets));
                }
                else {
                    messageBuilder.append(context.getString(R.string.speak_no_score));
                }
            }
            message = messageBuilder.toString();

        }
        // return the message created
        return message;
    }
}
