package uk.co.darkerwaters.scorepal.data;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.darkerwaters.scorepal.points.Sport;

public class PingPongSetup extends MatchSetup<PingPongMatch> {

    public enum PingPongRoundOption {
        ONE(1), THREE(3), FIVE(5), SEVEN(7), NINE(9);
        public final int num;
        public final int target;
        PingPongRoundOption(int num) {
            this.num = num;
            this.target = (int)((num + 1f) / 2f);
        }

        public static PingPongSetup.PingPongRoundOption fromValue(int value) {
            for (PingPongSetup.PingPongRoundOption round : PingPongSetup.PingPongRoundOption.values()) {
                if (round.num == value) {
                    // this is it
                    return round;
                }
            }
            // oops
            return THREE;
        }
    }

    public enum PingPongPointOption {
        ELEVEN(11), TWENTY_ONE(21);
        public final int num;
        PingPongPointOption(int num) {
            this.num = num;
        }

        public static PingPongPointOption fromValue(int value) {
            for (PingPongPointOption point : PingPongPointOption.values()) {
                if (point.num == value) {
                    // this is it
                    return point;
                }
            }
            // oops
            return ELEVEN;
        }
    }

    public enum PingPongExpeditePoints {
        EIGHTEEN(18);
        public final int num;
        PingPongExpeditePoints(int num) {
            this.num = num;
        }

        public static PingPongExpeditePoints fromValue(int value) {
            for (PingPongExpeditePoints option : PingPongExpeditePoints.values()) {
                if (option.num == value) {
                    // this is it
                    return option;
                }
            }
            // oops
            return EIGHTEEN;
        }
    }

    public enum PingPongExpediteMinutes {
        TEN(10);
        public final int num;
        PingPongExpediteMinutes(int num) {
            this.num = num;
        }

        public static PingPongExpediteMinutes fromValue(int value) {
            for (PingPongExpediteMinutes option : PingPongExpediteMinutes.values()) {
                if (option.num == value) {
                    // this is it
                    return option;
                }
            }
            // oops
            return TEN;
        }
    }

    private PingPongRoundOption roundsInMatch;
    private PingPongPointOption pointsInRound;
    private PingPongExpeditePoints expediteSystemPoints;
    private PingPongExpediteMinutes expediteSystemMinutes;
    private boolean isExpediteSystemEnabled;

    public PingPongSetup() {
        super(Sport.PINGPONG);

        // and set ours
        this.roundsInMatch = PingPongRoundOption.THREE;
        this.pointsInRound = PingPongPointOption.ELEVEN;
        this.expediteSystemMinutes = PingPongExpediteMinutes.TEN;
        this.expediteSystemPoints = PingPongExpeditePoints.EIGHTEEN;
        this.isExpediteSystemEnabled = true;
    }

    @Override
    protected void storeJSONData(JSONObject data) throws JSONException {
        super.storeJSONData(data);
        // store our data in this object too
        data.put("rounds", roundsInMatch.num);
        data.put("points", pointsInRound.num);
        data.put("expMins", expediteSystemMinutes.num);
        data.put("expPts", expediteSystemPoints.num);
        data.put("expOn", isExpediteSystemEnabled);
    }

    protected void restoreFromJSON(JSONObject data, int version) throws JSONException {
        super.restoreFromJSON(data, version);
        // and get our data from this object
        this.roundsInMatch = PingPongRoundOption.fromValue(data.getInt("rounds"));
        this.pointsInRound = PingPongPointOption.fromValue(data.getInt("points"));
        this.expediteSystemMinutes = PingPongExpediteMinutes.fromValue(data.getInt("expMins"));
        this.expediteSystemPoints = PingPongExpeditePoints.fromValue(data.getInt("expPts"));
        this.isExpediteSystemEnabled = data.getBoolean("expOn");
    }

    @Override
    public PingPongMatch createNewMatch() {
        return new PingPongMatch(this);
    }

    @Override
    public int[] getStraightPointsToWin() {
        // points levels are 3!
        return new int[] {
                1, // 1 point to win a point
                this.pointsInRound.num, // 11 points to win a game
        };
    }

    public PingPongPointOption getPointsInRound() {
        return this.pointsInRound;
    }

    public int getDecidingPoint() {
        return this.pointsInRound.num - 1;
    }

    public PingPongRoundOption getRoundsInMatch() {
        return roundsInMatch;
    }

    public void setPointsInRound(PingPongPointOption points) {
        if (pointsInRound != points) {
            this.pointsInRound = points;
            // this changes the setup
            informMatchSetupChanged(SetupChange.POINTS_STRUCTURE);
        }
    }

    public void setRoundsInMatch(PingPongRoundOption rounds) {
        if (roundsInMatch != rounds) {
            roundsInMatch = rounds;
            // this changes the setup
            informMatchSetupChanged(SetupChange.POINTS_STRUCTURE);
        }
    }

    public boolean isExpediteSystemEnabled() {
        return this.isExpediteSystemEnabled;
    }

    public void setIsExpediteSystemEnabled(boolean isEnabled) {
        if (this.isExpediteSystemEnabled != isEnabled) {
            this.isExpediteSystemEnabled = isEnabled;
            // this changes the setup
            informMatchSetupChanged(SetupChange.POINTS_STRUCTURE);
        }
    }

    public PingPongExpeditePoints getExpediteSystemPoints() {
        return this.expediteSystemPoints;
    }

    public void setExpediteSystemPoints(PingPongExpeditePoints points) {
        if (this.expediteSystemPoints != points) {
            this.expediteSystemPoints = points;
            // this changes the setup
            informMatchSetupChanged(SetupChange.POINTS_STRUCTURE);
        }
    }

    public PingPongExpediteMinutes getExpediteSystemMinutes() {
        return this.expediteSystemMinutes;
    }

    public void setExpediteSystemMinutes(PingPongExpediteMinutes minutes) {
        if (this.expediteSystemMinutes != minutes) {
            this.expediteSystemMinutes = minutes;
            // this changes the setup
            informMatchSetupChanged(SetupChange.POINTS_STRUCTURE);
        }
    }
}
