package uk.co.darkerwaters.scorepal.score.squash;

import android.content.Context;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.base.HistoryValue;

public class SquashMatch extends Match<SquashScore, SquashMatchSettings> {
    
    public SquashMatch(SquashMatchSettings settings) {
        super(settings, new SquashMatchSpeaker(), new SquashMatchWriter());
    }

    @Override
    protected SquashScore createScore(Team[] teams, SquashMatchSettings settings) {
        return new SquashScore(teams, settings);
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
        // a squash one that includes the flag if a game was just started
        HistoryValue historyValue = super.createHistoryValue(teamIndex, scoreString);
        // set the importance
        historyValue.importance = getCurrentHistoryImportance();
        // and return
        return historyValue;
    }

    @Override
    public String appendSpokenMessage(Context context, String message, String spokenMessage) {
        // this is special because in squash we change (sides) a lot. Don't keep saying this
        // first let the base do it's thing
        message = super.appendSpokenMessage(context, message, spokenMessage);
        // but stop it saying 'change ends'
        return message.replace(context.getString(R.string.change_ends), "");
    }
}
