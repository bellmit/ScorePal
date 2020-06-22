package uk.co.darkerwaters.scorepal.score;

import org.junit.Test;

import java.util.List;
import java.util.Random;

import uk.co.darkerwaters.scorepal.players.CourtPosition;
import uk.co.darkerwaters.scorepal.players.Player;
import uk.co.darkerwaters.scorepal.players.Team;

import static org.junit.Assert.assertEquals;

public class ScoreTest {

    protected final Team[] teams = new Team[] {
            new Team(0, new Player[] {new Player(0)}, CourtPosition.NORTH),
            new Team(1, new Player[] {new Player(10)}, CourtPosition.SOUTH),
    };

    protected final Team[] doubles = new Team[] {
            new Team(0, new Player[] {new Player(0, "playerOneA"), new Player(1, "playerOneB")}, CourtPosition.NORTH),
            new Team(1, new Player[] {new Player(10, "playerTwoA"), new Player(11, "playerTwoB")}, CourtPosition.SOUTH),
    };

    private boolean isThrown(Runnable code) {
        boolean isThrown = false;
        try {
            code.run();
        }
        catch (Throwable e) {
            isThrown = true;
        }
        return isThrown;
    }

    private class ScoreMatchSettings extends MatchSettings {
        ScoreMatchSettings() {
            super(null);
        }
        @Override
        public Match createMatch() {
            return null;
        }
    }

    @Test
    public void construction() {

        assertEquals("null teams crashes", true, isThrown(new Runnable() {
            @Override
            public void run() {
                new Score(null, new ScoreMatchSettings(), 1);
            }
        }));
        Score score = new Score(this.teams, new ScoreMatchSettings(), 1);
        assertEquals("incorrect team", 0, score.getPoint(0, new Team(0, new Player[0], CourtPosition.DEFAULT)));

        assertEquals("Score never over", false, score.isMatchOver());
    }

    @Test
    public void scoreString() {
        Score score = new Score(this.teams, new ScoreMatchSettings(), 1);

        for (int i = 0; i < 100; ++i) {
            score.setPoint(0, this.teams[0], i);
            score.setPoint(0, this.teams[1], 100 - i);
            assertEquals("team one pt string", "" + i, score.getDisplayPoint(0, this.teams[0]).displayString(null));
            assertEquals("team two pt string", "" + (100 - i), score.getDisplayPoint(0, this.teams[1]).displayString(null));
        }
    }

    @Test
    public void resettingValues() {
        Score score = new Score(this.teams, new ScoreMatchSettings(), 3);
        assertEquals("levels", 3, score.getLevels());
        Random random = new Random();
        for (int i = 0; i < 100; ++i) {
            score.incrementPoint(teams[random.nextInt(1)]);
        }

        score.resetScore(this.teams, new ScoreMatchSettings());
        assertEquals("Score should be reset", 0, score.getPoint(0, teams[0]));
        assertEquals("Score should be reset", 0, score.getPoint(0, teams[1]));
        assertEquals("Score should be reset", 0, score.getPoint(1, teams[0]));
        assertEquals("Score should be reset", 0, score.getPoint(1, teams[1]));
        assertEquals("Score should be reset", 0, score.getPoint(2, teams[0]));
        assertEquals("Score should be reset", 0, score.getPoint(2, teams[1]));

        assertEquals("Score should be reset", null, score.getPointHistory(0));
        assertEquals("Score should be reset", null, score.getPointHistory(1));
        assertEquals("Score should be reset", null, score.getPointHistory(2));
    }

    @Test
    public void settingValues() {
        // create a series of games
        Score score = new Score(this.teams, new ScoreMatchSettings(), 3);
        for (int iSets = 0; iSets < 3; ++iSets) {
            for (int iGames = 0; iGames < 6; ++iGames) {
                // set the points to player one wins
                for (int iPoints = 0; iPoints < 5; ++iPoints) {
                    score.setPoint(0, this.teams[0], iPoints);
                    score.setPoint(0, this.teams[1], 0);
                }
                // set the game won
                assertEquals("Points should be four to win", 4, score.getPoint(0,teams[0]));
                score.setPoint(1, this.teams[0], iGames + 1);
                score.setPoint(1, this.teams[1], 0);
                score.clearLevel(0);
                assertEquals("Points should be zero after winning game", 0, score.getPoint(0, teams[0]));
            }
            assertEquals("Games should be six", 6, score.getPoint(1, teams[0]));
            // set the set won
            score.setPoint(2, this.teams[0], iSets + 1);
            score.setPoint(2, this.teams[1], 0);
            score.clearLevel(1);
            assertEquals("Games should be zero after winning set", 0, score.getPoint(1, teams[0]));
        }

        // so the points are nothing
        assertEquals("Points should be zero after winning set", 0, score.getPoint(0, teams[0]));
        assertEquals("Games should be zero after winning set", 0, score.getPoint(1, teams[0]));
        assertEquals("Sets should be 3 after winning set", 3, score.getPoint(2, teams[0]));
        score.clearLevel(2);

        // so check the history here
        List<int[]> gameHistory = score.getPointHistory(1);
        // there should be 6 games, all won with 4 points each here
        assertEquals("History should contain 3 sets", 3, gameHistory.size());
        for (int[] gamePoints : gameHistory) {
            // there should be a point per team
            assertEquals("History game should have two points in it", 2, gamePoints.length);
            // each should be 4 points to zero
            assertEquals("History game should be six games for team 1", 6, gamePoints[0]);
            assertEquals("History game should be love games for team 2", 0, gamePoints[1]);
        }

        assertEquals("winner is", this.teams[0], score.getWinner(2));
    }
}