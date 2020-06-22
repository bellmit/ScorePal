package uk.co.darkerwaters.scorepal.score.base;

import uk.co.darkerwaters.scorepal.players.Team;

public class PointChange {
    public final Team team;
    public final int level;
    public final int point;

    public PointChange(Team team, int level, int point) {
        this.team = team;
        this.level = level;
        this.point = point;
    }
}
