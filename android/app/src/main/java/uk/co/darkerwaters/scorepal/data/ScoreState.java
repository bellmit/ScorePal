package uk.co.darkerwaters.scorepal.data;

import java.util.ArrayList;
import java.util.List;

public class ScoreState {

    private int currentState;
    private MatchSetup.Team teamChanged;
    private int levelChanged;

    ScoreState() {
        reset();
    }

    public enum ScoreChange {
        NONE(0),
        INCREMENT(1),
        SERVER(2),
        ENDS(4),
        DECIDING_POINT(8),
        TIE_BREAK(16),
        DECREMENT(32),
        BREAK_POINT_CONVERTED(64),
        BREAK_POINT(128),
        INCREMENT_REDO(256);

        final int val;
        ScoreChange(int val) {
            this.val = val;
        }
    }

    public void reset() {
        this.currentState = 0;
        this.teamChanged = null;
        this.levelChanged = -1;
    }

    public boolean isEmpty() { return this.currentState == ScoreChange.NONE.val; }

    public void addChange(ScoreChange change) {
        currentState |= change.val;
    }

    public void addChange(ScoreChange change, MatchSetup.Team team, int level) {
        addChange(change);
        teamChanged = team;
        levelChanged = Math.max(level, levelChanged);
    }

    public static boolean Changed(int state, ScoreChange change) {
        return 0 != (state & change.val);
    }

    public boolean isChanged(ScoreChange change) {
        return 0 != (currentState & change.val);
    }

    public List<ScoreChange> getChanges() {
        // find all the activiated changes and return as a list
        List<ScoreChange> list = new ArrayList<>();
        for (ScoreChange change: ScoreChange.values()) {
            if (isChanged(change)) {
                list.add(change);
            }
        }
        return list;
    }

    public int getState() {
        return currentState;
    }

    public MatchSetup.Team getTeamChanged() { return teamChanged; }
    public int getLevelChanged() { return levelChanged; }

    public void setState(int state, int levelChanged, MatchSetup.Team teamChanged) {
        this.currentState = state;
        this.levelChanged = levelChanged;
        this.teamChanged = teamChanged;
    }
}
