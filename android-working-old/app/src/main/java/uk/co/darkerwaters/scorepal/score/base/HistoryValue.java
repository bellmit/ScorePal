package uk.co.darkerwaters.scorepal.score.base;

public class HistoryValue {

    public enum Importance {
        LOW,
        MEDIUM,
        HIGH,
    }

    public int teamIndex;
    public String scoreString;
    public Importance importance;

    public HistoryValue(int teamIndex, String scoreString, Importance importance) {
        this.teamIndex = teamIndex;
        this.scoreString = scoreString;
        this.importance = importance;
    }

    public HistoryValue copy() {
        return new HistoryValue(this.teamIndex, this.scoreString, this.importance);
    }
}
