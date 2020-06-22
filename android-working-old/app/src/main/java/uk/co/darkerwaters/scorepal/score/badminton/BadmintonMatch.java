package uk.co.darkerwaters.scorepal.score.badminton;

import android.content.Context;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.base.HistoryValue;

public class BadmintonMatch extends Match<BadmintonScore, BadmintonMatchSettings> {
    
    public BadmintonMatch(BadmintonMatchSettings settings) {
        super(settings, new BadmintonMatchSpeaker(), new BadmintonMatchWriter());
    }

    @Override
    protected BadmintonScore createScore(Team[] teams, BadmintonMatchSettings settings) {
        return new BadmintonScore(teams, settings);
    }

    @Override
    protected void updateHistoryValue(Context context, HistoryValue history) {
        // let the base do it's thing
        super.updateHistoryValue(context, history);
        // and update if the game was just won
        history.importance = getCurrentHistoryImportance();
    }

    private HistoryValue.Importance getCurrentHistoryImportance() {
        if (0 == this.getScore().getPoints(getTeamOne()) && 0 == this.getScore().getPoints(getTeamTwo())) {
            // this is a new game, this is fairly important
            return HistoryValue.Importance.MEDIUM;
        }
        else {
            // this is normal - low
            return HistoryValue.Importance.LOW;
        }
    }

    @Override
    protected HistoryValue createHistoryValue(int teamIndex, String scoreString) {
        // instead of creating the base history value, we want to create
        // a badminton one that includes the flag if a game was just started
        HistoryValue historyValue = super.createHistoryValue(teamIndex, scoreString);
        // set the importance
        historyValue.importance = getCurrentHistoryImportance();
        // and return
        return historyValue;
    }
}
