package uk.co.darkerwaters.scorepal.ui.matchsetup;

import android.view.View;
import android.widget.ArrayAdapter;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.BadmintonSetup;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.data.PingPongSetup;
import uk.co.darkerwaters.scorepal.points.Sport;
import uk.co.darkerwaters.scorepal.ui.views.CheckableIndicatorButton;
import uk.co.darkerwaters.scorepal.ui.views.RadioIndicatorButton;

public class FragmentSetupPingPong extends FragmentSetupMatch<PingPongSetup> {

    private RadioIndicatorButton roundsOne;
    private RadioIndicatorButton roundsThree;
    private RadioIndicatorButton roundsFive;

    private RadioIndicatorButton singles;
    private RadioIndicatorButton doubles;

    private RadioIndicatorButton elevenPoints;
    private RadioIndicatorButton twentyOnePoints;

    private FragmentTeam teamOne;
    private FragmentTeam teamTwo;

    public FragmentSetupPingPong() {
        super(Sport.PINGPONG, R.layout.fragment_setup_pingpong);
    }

    @Override
    protected void setupControls(View root) {
        roundsOne = root.findViewById(R.id.pingPongRoundsOne);
        roundsThree = root.findViewById(R.id.pingPongRoundsThree);
        roundsFive = root.findViewById(R.id.pingPongRoundsFive);

        singles = root.findViewById(R.id.pingPongSingles);
        doubles = root.findViewById(R.id.pingPongDoubles);

        elevenPoints = root.findViewById(R.id.pingPongPointsEleven);
        twentyOnePoints = root.findViewById(R.id.pingPongPointsTwentyOne);

        teamOne = (FragmentTeam) getChildFragmentManager().findFragmentById(R.id.fragmentTeamOne);
        teamTwo = (FragmentTeam) getChildFragmentManager().findFragmentById(R.id.fragmentTeamTwo);

        // listen for the clicks to set the data back to the match setup
        listenForChanges(roundsOne, PingPongSetup.PingPongRoundOption.ONE);
        listenForChanges(roundsThree, PingPongSetup.PingPongRoundOption.THREE);
        listenForChanges(roundsFive, PingPongSetup.PingPongRoundOption.FIVE);

        // and changes to singles or doubles
        listenForChanges(singles, MatchSetup.MatchType.SINGLES);
        listenForChanges(doubles, MatchSetup.MatchType.DOUBLES);

        // and four or six points
        listenForChanges(elevenPoints, PingPongSetup.PingPongPointOption.ELEVEN);
        listenForChanges(twentyOnePoints, PingPongSetup.PingPongPointOption.TWENTY_ONE);
    }

    @Override
    protected void setupAdapters(ArrayAdapter adapter) {
        // if we have an adapter - give this to the team fragements that let us enter names
        teamOne.setAutoCompleteAdapter(adapter);
        teamTwo.setAutoCompleteAdapter(adapter);
    }

    private void listenForChanges(RadioIndicatorButton typeRadio, final MatchSetup.MatchType type) {
        typeRadio.addOnCheckChangeListener(new CheckableIndicatorButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                if (isChecked) {
                    matchSetup.setType(type);
                    // and update the data to show / hide the partner's names accordingly
                    showHideDoublesControls();
                    // and refresh the team titles
                    showTeamNames();
                }
            }
        });
    }

    private void listenForChanges(RadioIndicatorButton setRadio, final PingPongSetup.PingPongRoundOption noRounds) {
        setRadio.addOnCheckChangeListener(new CheckableIndicatorButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                if (isChecked)
                    matchSetup.setRoundsInMatch(noRounds);
            }
        });
    }

    private void listenForChanges(RadioIndicatorButton pointRadio, final PingPongSetup.PingPongPointOption noPoints) {
        pointRadio.addOnCheckChangeListener(new CheckableIndicatorButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                if (isChecked)
                    matchSetup.setPointsInRound(noPoints);
            }
        });
    }

    @Override
    protected void setDataToControls() {
        // setup the data on this page, first is the number of rounds to play
        if (null != this.matchSetup) {
            // set the number of rounds
            roundsOne.setChecked(this.matchSetup.getRoundsInMatch() == PingPongSetup.PingPongRoundOption.ONE);
            roundsThree.setChecked(this.matchSetup.getRoundsInMatch() == PingPongSetup.PingPongRoundOption.THREE);
            roundsFive.setChecked(this.matchSetup.getRoundsInMatch() == PingPongSetup.PingPongRoundOption.FIVE);

            // now singles or doubles
            singles.setChecked(this.matchSetup.getType() == MatchSetup.MatchType.SINGLES);
            doubles.setChecked(this.matchSetup.getType() == MatchSetup.MatchType.DOUBLES);

            //  points
            elevenPoints.setChecked(this.matchSetup.getPointsInRound() == PingPongSetup.PingPongPointOption.ELEVEN);
            twentyOnePoints.setChecked(this.matchSetup.getPointsInRound() == PingPongSetup.PingPongPointOption.TWENTY_ONE);

            // and the team's data
            teamOne.setDataToControls(this.matchSetup, MatchSetup.Team.T_ONE);
            teamTwo.setDataToControls(this.matchSetup, MatchSetup.Team.T_TWO);
        }
    }

    private void showTeamNames() {
        teamOne.showTeamName();
        teamTwo.showTeamName();
    }

    private void showHideDoublesControls() {
        teamOne.showHideDoublesControls();
        teamTwo.showHideDoublesControls();
    }
}
