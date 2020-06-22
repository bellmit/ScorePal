package uk.co.darkerwaters.scorepal.score.tennis;

import android.content.Context;

import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.base.HistoryValue;
import uk.co.darkerwaters.scorepal.score.Match;

public class TennisMatch extends Match<TennisScore, TennisMatchSettings> {

    public TennisMatch(TennisMatchSettings settings) {
        super(settings, new TennisMatchSpeaker(), new TennisMatchWriter());
    }

    @Override
    protected TennisScore createScore(Team[] teams, TennisMatchSettings settings) {
        return new TennisScore(teams, TennisSets.FIVE, settings);
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
            // this is a new game, it might also be a new set
            if (0 == this.getScore().getGames(getTeamOne(), -1) &&
                    0 == this.getScore().getGames(getTeamTwo(), -1)) {
                // yeah, this is actually a new set
                return HistoryValue.Importance.HIGH;
            }
            else {
                // just a new game
                return HistoryValue.Importance.MEDIUM;
            }
        }
        else {
            // this is normal - low
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
