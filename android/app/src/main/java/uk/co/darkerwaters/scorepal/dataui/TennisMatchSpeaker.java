package uk.co.darkerwaters.scorepal.dataui;

import android.content.Context;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.data.TennisMatch;
import uk.co.darkerwaters.scorepal.data.TennisScore;
import uk.co.darkerwaters.scorepal.data.TennisSetup;
import uk.co.darkerwaters.scorepal.points.Point;
import uk.co.darkerwaters.scorepal.points.TennisPoint;

public class TennisMatchSpeaker extends MatchSpeaker<TennisMatch> {

    @Override
    public String createPointsPhrase(TennisMatch match, Context context, MatchSetup.Team team, int level) {
        String message = "";
        switch (level) {
            case TennisScore.LEVEL_POINT:
                // the points changed, announce the points
                message = createPhrasePoints(match, context, team);
                break;
            case TennisScore.LEVEL_GAME:
                // the games changed, announce who won the game
                message = createPhraseGames(match, context, team);
                break;
            case TennisScore.LEVEL_SET:
                // the sets changed, announce who won the game and the set
                message = createPhraseSets(match, context, team);
                break;
        }
        // and return the complicated message to speak
        return message;
    }

    @Override
    public String getLevelTitle(int level, Context context) {
        switch (level) {
            case TennisScore.LEVEL_POINT :
                return TennisPoint.POINT.speakString(context);
            case TennisScore.LEVEL_GAME :
                return TennisPoint.GAME.speakString(context);
            case TennisScore.LEVEL_SET :
                return TennisPoint.SET.speakString(context);
        }
        return super.getLevelTitle(level, context);
    }

    public String createScorePhrase(TennisMatch match, Context context, MatchSetup.Team team, int level) {
        String message = "";
        switch (level) {
            case TennisScore.LEVEL_POINT:
                // the points changed, don't say anything else
                break;
            case TennisScore.LEVEL_GAME:
                // the games changed, tell them the current score state
                message = createScorePhraseGames(match, context, team);
                break;
            case TennisScore.LEVEL_SET:
                // the sets changed, tell them the current score state
                message = createScorePhraseSets(match, context, team);
                break;
        }
        // and return the complicated message to speak
        return message;
    }

    private String createPhraseSets(TennisMatch match, Context context, MatchSetup.Team changeTeam) {
        TennisSetup setup = match.getSetup();
        // create the announcement of the set result on this match
        String changeTeamString = getSpeakingTeamName(context, setup, changeTeam);
        StringBuilder message = new StringBuilder();

        append(message, TennisPoint.GAME.speakString(context), Point.K_SPEAKING_PAUSE);
        append(message, TennisPoint.SET.speakString(context), Point.K_SPEAKING_PAUSE);
        // also match?
        if (match.isMatchOver()) {
            append(message, TennisPoint.MATCH.speakString(context), Point.K_SPEAKING_PAUSE);
        }
        // add the winner's name
        append(message, changeTeamString);
        // and return this
        return message.toString();
    }

    private String createScorePhraseSets(TennisMatch match, Context context, MatchSetup.Team changeTeam) {
        TennisSetup setup = match.getSetup();
        // create the announcement of the set result on this match
        String teamOneString = getSpeakingTeamName(context, setup, MatchSetup.Team.T_ONE);
        String teamTwoString = getSpeakingTeamName(context, setup, MatchSetup.Team.T_TWO);
        StringBuilder message = new StringBuilder();

        if (null != match && !match.isMatchOver()) {
            // match isn't over, want to read out the sets we have one
            append(message, Point.K_SPEAKING_PAUSE);

            int setsOne = match.getPoint(TennisScore.LEVEL_SET, MatchSetup.Team.T_ONE);
            int setsTwo = match.getPoint(TennisScore.LEVEL_SET, MatchSetup.Team.T_TWO);

            if (setsOne == setsTwo) {
                // we are equal, say '1 set(s) all'
                append(message, null == context ? (setsOne + " all") :
                        context.getString(R.string.speak_number_all,
                        setsOne,
                        TennisPoint.SET.speakString(context, setsOne)), Point.K_SPEAKING_PAUSE);
            }
            else {
                // team one first
                append(message, teamOneString);
                append(message, setsOne);
                append(message, TennisPoint.SET.speakString(context, setsOne), Point.K_SPEAKING_PAUSE);

                // team two first
                append(message, teamTwoString);
                append(message, setsTwo);
                append(message, TennisPoint.SET.speakString(context, setsTwo), Point.K_SPEAKING_PAUSE);
            }
        }
        else {
            // we want to also read out the games from each set
            append(message, Point.K_SPEAKING_PAUSE_LONG);

            MatchSetup.Team winnerTeam;
            MatchSetup.Team loserTeam;
            if (changeTeam == MatchSetup.Team.T_ONE) {
                // team one is the winner
                winnerTeam = MatchSetup.Team.T_ONE;
                loserTeam = MatchSetup.Team.T_TWO;
            }
            else {
                // team two is the winner
                winnerTeam = MatchSetup.Team.T_TWO;
                loserTeam = MatchSetup.Team.T_ONE;
            }
            for (int i = 0; i < match.getPlayedSets(); ++i) {
                append(message, Point.K_SPEAKING_PAUSE_LONG);
                append(message, match.getGames(winnerTeam, i).speakString(context), Point.K_SPEAKING_PAUSE_SLIGHT);
                append(message, match.getGames(loserTeam, i).speakString(context), Point.K_SPEAKING_PAUSE_SLIGHT);
            }
        }
        return message.toString();
    }

    private String createPhraseGames(TennisMatch match, Context context, MatchSetup.Team changeTeam) {
        TennisSetup setup = match.getSetup();
        // create the announcement of games on this match
        String changeTeamString = getSpeakingTeamName(context, setup, changeTeam);
        StringBuilder message = new StringBuilder();

        append(message, TennisPoint.GAME.speakString(context), Point.K_SPEAKING_PAUSE);
        append(message, changeTeamString);
        return message.toString();
    }

    private String createScorePhraseGames(TennisMatch match, Context context, MatchSetup.Team changeTeam) {
        TennisSetup setup = match.getSetup();
        // create the announcement of games on this match
        String teamOneString = getSpeakingTeamName(context, setup, MatchSetup.Team.T_ONE);
        String teamTwoString = getSpeakingTeamName(context, setup, MatchSetup.Team.T_TWO);
        String changeTeamString = getSpeakingTeamName(context, setup, changeTeam);
        StringBuilder message = new StringBuilder();
        // so we want to say the games as they stand
        append(message, Point.K_SPEAKING_PAUSE_LONG);

        int gamesOne = match.getGames(MatchSetup.Team.T_ONE, -1).val();
        int gamesTwo = match.getGames(MatchSetup.Team.T_TWO, -1).val();

        if (gamesOne == gamesTwo) {
            // we are equal, say '1 game(s) all'
            append(message, context == null ? (gamesOne + " all") :
                    context.getString(R.string.speak_number_all,
                    gamesOne,
                    TennisPoint.GAME.speakString(context, gamesOne)), Point.K_SPEAKING_PAUSE);
        }
        else {
            // team one first
            append(message, teamOneString);
            append(message, gamesOne);
            append(message, TennisPoint.GAME.speakString(context, gamesOne), Point.K_SPEAKING_PAUSE);

            // team two first
            append(message, teamTwoString);
            append(message, gamesTwo);
            append(message, TennisPoint.GAME.speakString(context, gamesTwo), Point.K_SPEAKING_PAUSE);
        }

        return message.toString();
    }

    private String createPhrasePoints(TennisMatch match, Context context, MatchSetup.Team changeTeam) {
        TennisSetup setup = match.getSetup();
        // create the announcement of points on this match
        String teamOneString = getSpeakingTeamName(context, setup, MatchSetup.Team.T_ONE);
        String teamTwoString = getSpeakingTeamName(context, setup, MatchSetup.Team.T_TWO);
        Point t1Point = match.getDisplayPoint(TennisScore.LEVEL_POINT, MatchSetup.Team.T_ONE);
        Point t2Point = match.getDisplayPoint(TennisScore.LEVEL_POINT, MatchSetup.Team.T_TWO);
        StringBuilder message = new StringBuilder();

        if (t1Point == TennisPoint.ADVANTAGE) {
            // read advantage team one
            append(message, t1Point.speakString(context), Point.K_SPEAKING_SPACE);
            append(message, teamOneString);
        } else if (t2Point == TennisPoint.ADVANTAGE) {
            // read advantage team two
            append(message, t2Point.speakString(context), Point.K_SPEAKING_SPACE);
            append(message, teamTwoString);
        } else if (t1Point == TennisPoint.DEUCE
                && t2Point == TennisPoint.DEUCE) {
            // read deuce
            append(message, t1Point.speakString(context));
        } else if (t1Point.val() == t2Point.val()) {
            // they have the same score, use the special "all" values
            append(message, t1Point.speakAllString(context));
        } else if (match.isInTieBreak()) {
            // in a tie-break we read the score with the winner first
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
                // the points are the same
                append(message, t1Point.speakAllString(context));
            }
        } else {
            // just read the numbers out, but we want to say the server first
            // so who is that?
            if (match.getServingTeam() == MatchSetup.Team.T_ONE) {
                // team one is serving
                append(message, t1Point.speakString(context), Point.K_SPEAKING_SPACE);
                append(message, t2Point.speakString(context));
            } else {
                // team two is serving
                append(message, t2Point.speakString(context), Point.K_SPEAKING_SPACE);
                append(message, t1Point.speakString(context));
            }
        }
        return message.toString();
    }

    @Override
    public String createPointsAnnouncement(TennisMatch match, Context context) {
        TennisSetup setup = match.getSetup();

        MatchSetup.Team teamServing = match.getServingTeam();
        MatchSetup.Team teamReceiving = setup.getOtherTeam(teamServing);
        int serverPoints = match.getPoint(TennisScore.LEVEL_POINT, teamServing);
        int receiverPoints = match.getPoint(TennisScore.LEVEL_POINT, teamReceiving);

        String serverString = getSpeakingTeamName(context, setup, teamServing);
        String receiverString = getSpeakingTeamName(context, setup, teamReceiving);

        String message;
        if (serverPoints + receiverPoints > 0) {
            // there are points, say the points
            message = createPointsPhrase(match, context, teamServing, TennisScore.LEVEL_POINT);
        }
        else {
            // no points scored, are there any games?
            int serverGames = match.getGames(teamServing, -1).val();
            int receiverGames = match.getGames(teamReceiving, -1).val();
            StringBuilder messageBuilder = new StringBuilder();
            if (serverGames + receiverGames > 0) {
                // announce the games
                append(messageBuilder, serverString, Point.K_SPEAKING_PAUSE_SLIGHT);
                append(messageBuilder, serverGames, Point.K_SPEAKING_PAUSE_SLIGHT);
                append(messageBuilder, TennisPoint.GAME.speakString(context, serverGames), Point.K_SPEAKING_PAUSE);
                append(messageBuilder, receiverString, Point.K_SPEAKING_PAUSE_SLIGHT);
                append(messageBuilder, receiverGames, Point.K_SPEAKING_PAUSE_SLIGHT);
                append(messageBuilder, TennisPoint.GAME.speakString(context, receiverGames));
            }
            else {
                // do the sets
                int serverSets = match.getPoint(TennisScore.LEVEL_SET, teamServing);
                int receiverSets = match.getPoint(TennisScore.LEVEL_SET, teamReceiving);
                if (receiverSets + serverSets > 0) {
                    // announce the sets
                    append(messageBuilder, serverString, Point.K_SPEAKING_PAUSE_SLIGHT);
                    append(messageBuilder, serverSets, Point.K_SPEAKING_PAUSE_SLIGHT);
                    append(messageBuilder, TennisPoint.SET.speakString(context, serverSets), Point.K_SPEAKING_PAUSE);
                    append(messageBuilder, receiverString, Point.K_SPEAKING_PAUSE_SLIGHT);
                    append(messageBuilder, receiverSets, Point.K_SPEAKING_PAUSE_SLIGHT);
                    append(messageBuilder, TennisPoint.SET.speakString(context, receiverSets));
                }
                else {
                    append(messageBuilder, context == null ? "game not started" : context.getString(R.string.speak_no_score));
                }
            }
            message = messageBuilder.toString();

        }
        // return the message created
        return message;
    }
}
