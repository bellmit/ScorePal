package uk.co.darkerwaters.scorepal.data;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.darkerwaters.scorepal.points.Sport;

public class TennisSetup extends MatchSetup<TennisMatch> {

    public enum TennisSet {
        ONE(1), THREE(3), FIVE(5);
        public final int num;
        public final int target;
        TennisSet(int num) {
            this.num = num;
            this.target = (int)((num + 1f) / 2f);
        }

        public static TennisSet fromValue(int setsValue) {
            for (TennisSet set : TennisSet.values()) {
                if (set.num == setsValue) {
                    // this is it
                    return set;
                }
            }
            // oops
            return FIVE;
        }
    }

    public enum TennisGame {
        FOUR(4), SIX(6);
        public final int num;
        TennisGame(int num) {
            this.num = num;
        }
    }

    private TennisSet numberSets = TennisSet.THREE;
    private TennisGame numberGames = TennisGame.SIX;

    // this is the game at which to play a tie in the final set
    private int finalSetTieGame = 0;

    private boolean isDeuceSuddenDeath = false;

    public TennisSetup() {
        super(Sport.TENNIS);
    }

    @Override
    public TennisMatch createNewMatch() {
        return new TennisMatch(this);
    }

    @Override
    protected void storeJSONData(JSONObject data) throws JSONException {
        super.storeJSONData(data);
        // store our data in this object too
        data.put("sets", this.numberSets.name());
        data.put("games", this.numberGames.name());
        data.put("finalSetTie", finalSetTieGame);
        data.put("deuceDeath", isDeuceSuddenDeath);
    }

    protected void restoreFromJSON(JSONObject data, int version) throws JSONException {
        super.restoreFromJSON(data, version);
        // and get our data from this object
        this.numberSets = TennisSet.valueOf(data.getString("sets"));
        this.numberGames = TennisGame.valueOf(data.getString("games"));
        this.finalSetTieGame = data.getInt("finalSetTie");
        this.isDeuceSuddenDeath = data.getBoolean("deuceDeath");
    }

    public TennisSet getNumberSets() {
        return numberSets;
    }

    public void setNumberSets(TennisSet value) {
        if (this.numberSets != value) {
            this.numberSets = value;
            // this changes the setup
            informMatchSetupChanged(SetupChange.POINTS_STRUCTURE);
        }
    }

    public TennisGame getNumberGames() {
        return numberGames;
    }

    @Override
    public int[] getStraightPointsToWin() {
        // points levels are 3!
        return new int[] {
                1, // 1 point to win a point
                4, // 4 points to win a game
                numberGames.num, // number games (4 or 6) to win a set
        };
    }

    public void setNumberGames(TennisGame value) {
        if (finalSetTieGame == numberGames.num) {
            // the tie is to happen in the final game, change this too
            setFinalSetTieGame(value.num);
        }
        if (numberGames != value) {
            // set the number games
            this.numberGames = value;
            // this changes the setup
            informMatchSetupChanged(SetupChange.POINTS_STRUCTURE);
        }
    }

    public int getFinalSetTieGame() {
        return finalSetTieGame;
    }

    public void setFinalSetTieGame(int value) {
        if (finalSetTieGame != value) {
            this.finalSetTieGame = value;
            // this changes the setup
            informMatchSetupChanged(SetupChange.POINTS_STRUCTURE);
        }
    }

    public void setIsFinalSetTie(boolean isFinalSetTie) {
        int newGame = 0;
        if (isFinalSetTie) {
            // tie in the final set - default to the number of games
            newGame = this.numberGames.num;
        }
        if (this.finalSetTieGame != newGame) {
            // not to tie at all
            this.finalSetTieGame = newGame;
            // this changes the setup
            informMatchSetupChanged(SetupChange.POINTS_STRUCTURE);
        }
    }

    public boolean getIsFinalSetTie() {
        return this.finalSetTieGame > 0;
    }

    public void setDeuceSuddenDeath(boolean deuceSuddenDeath) {
        if (isDeuceSuddenDeath != deuceSuddenDeath) {
            isDeuceSuddenDeath = deuceSuddenDeath;
            // this changes the setup
            informMatchSetupChanged(SetupChange.POINTS_STRUCTURE);
        }
    }

    public boolean getIsDeuceSuddenDeath() {
        return isDeuceSuddenDeath;
    }
}
