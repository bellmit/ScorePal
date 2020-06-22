package uk.co.darkerwaters.scorepal.score.points;

import android.content.Context;

import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.base.HistoryValue;
import uk.co.darkerwaters.scorepal.score.Match;

public class PointsMatch extends Match<PointsScore, PointsMatchSettings> {

    public PointsMatch(PointsMatchSettings settings) {
        super(settings, new PointsMatchSpeaker(), new PointsMatchWriter());
    }

    @Override
    protected PointsScore createScore(Team[] teams, PointsMatchSettings settings) {
        return new PointsScore(teams, settings);
    }

    @Override
    protected void updateHistoryValue(Context context, HistoryValue history) {
        // let the base do it's thing
        super.updateHistoryValue(context, history);
        // and update if the game was just won
        history.importance = getCurrentHistoryImportance();
    }

    private HistoryValue.Importance getCurrentHistoryImportance() {
        PointsScore score = getScore();
        if (score.isNewEnd()) {
            // this is most important
            return HistoryValue.Importance.HIGH;
        }
        else if (score.isNewServer()) {
            // this is middle
            return HistoryValue.Importance.MEDIUM;
        }
        else {
            // we are low
            return HistoryValue.Importance.LOW;
        }
    }

    @Override
    protected HistoryValue createHistoryValue(int teamIndex, String scoreString) {
        // instead of creating the base history value, we want to create
        // a tennis one that includes the flag if a game was just started
        HistoryValue historyValue = super.createHistoryValue(teamIndex, scoreString);
        // set the importance
        historyValue.importance = getCurrentHistoryImportance();
        // and return
        return historyValue;
    }
}
