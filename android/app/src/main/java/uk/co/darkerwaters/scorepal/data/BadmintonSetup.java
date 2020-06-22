package uk.co.darkerwaters.scorepal.data;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.darkerwaters.scorepal.points.Sport;

public class BadmintonSetup extends MatchSetup {

    public enum BadmintonGameOption {
        ONE(1), THREE(3), FIVE(5);
        public final int num;
        public final int target;
        BadmintonGameOption(int num) {
            this.num = num;
            this.target = (int)((num + 1f) / 2f);
        }

        public static BadmintonGameOption fromValue(int value) {
            for (BadmintonGameOption game : BadmintonGameOption.values()) {
                if (game.num == value) {
                    // this is it
                    return game;
                }
            }
            // oops
            return THREE;
        }
    }

    public enum BadmintonPointOption {
        ELEVEN(11), FIFTEEN(15), TWENTY_ONE(21);
        public final int num;
        BadmintonPointOption(int num) {
            this.num = num;
        }

        public static BadmintonPointOption fromValue(int value) {
            for (BadmintonPointOption option : BadmintonPointOption.values()) {
                if (option.num == value) {
                    // this is it
                    return option;
                }
            }
            // oops
            return TWENTY_ONE;
        }
    }

    public enum BadmintonDecider {
        NINETEEN(19), TWENTY_FIVE(25), TWENTY_NINE(29);
        public final int num;
        BadmintonDecider(int num) {
            this.num = num;
        }

        public static BadmintonDecider fromValue(int value) {
            for (BadmintonDecider option : BadmintonDecider.values()) {
                if (option.num == value) {
                    // this is it
                    return option;
                }
            }
            // oops
            return TWENTY_NINE;
        }
    }

    private BadmintonGameOption numberGames = BadmintonGameOption.THREE;
    private BadmintonPointOption pointsInGame = BadmintonPointOption.TWENTY_ONE;

    private BadmintonDecider decidingPoint = BadmintonDecider.TWENTY_NINE;

    public BadmintonSetup() {
        super(Sport.BADMINTON);
    }

    @Override
    public Match createNewMatch() {
        return new BadmintonMatch(this);
    }

    @Override
    protected void storeJSONData(JSONObject data) throws JSONException {
        super.storeJSONData(data);
        // store our data in this object too
        data.put("games", numberGames.num);
        data.put("points", pointsInGame.num);
        data.put("decdng", decidingPoint.num);
    }

    protected void restoreFromJSON(JSONObject data, int version) throws JSONException {
        super.restoreFromJSON(data, version);
        // and get our data from this object
        this.numberGames = BadmintonGameOption.fromValue(data.getInt("games"));
        this.pointsInGame = BadmintonPointOption.fromValue(data.getInt("points"));
        this.decidingPoint = BadmintonDecider.fromValue(data.getInt("decdng"));
    }

    @Override
    public int[] getStraightPointsToWin() {
        // points levels are 3!
        return new int[] {
                1, // 1 point to win a point
                pointsInGame.num, // 21 points to win a game
        };
    }

    public BadmintonDecider getDecidingPoint() {
        return this.decidingPoint;
    }

    public BadmintonGameOption getGamesInMatch() {
        return numberGames;
    }

    public BadmintonPointOption getPointsInGame() { return pointsInGame; }

    public void setPointsInGame(BadmintonPointOption points) {
        if (this.pointsInGame != points) {
            this.pointsInGame = points;
            // this changes the setup
            informMatchSetupChanged(SetupChange.POINTS_STRUCTURE);
        }
    }

    public void setDecidingPoint(BadmintonDecider decidingPoint) {
        if (this.decidingPoint != decidingPoint) {
            this.decidingPoint = decidingPoint;
            // this changes the setup
            informMatchSetupChanged(SetupChange.POINTS_STRUCTURE);
        }
    }

    public void setGamesInMatch(BadmintonGameOption games) {
        if (numberGames != games) {
            numberGames = games;
            // this changes the setup
            informMatchSetupChanged(SetupChange.POINTS_STRUCTURE);
        }
    }
}
