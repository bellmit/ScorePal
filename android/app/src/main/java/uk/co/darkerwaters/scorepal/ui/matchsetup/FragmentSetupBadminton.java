package uk.co.darkerwaters.scorepal.ui.matchsetup;

import android.view.View;
import android.widget.ArrayAdapter;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.BadmintonSetup;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.points.Sport;
import uk.co.darkerwaters.scorepal.ui.views.CheckableIndicatorButton;
import uk.co.darkerwaters.scorepal.ui.views.RadioIndicatorButton;

public class FragmentSetupBadminton extends FragmentSetupMatch<BadmintonSetup> {

    private RadioIndicatorButton gamesOne;
    private RadioIndicatorButton gamesThree;
    private RadioIndicatorButton gamesFive;

    private RadioIndicatorButton singles;
    private RadioIndicatorButton doubles;

    private RadioIndicatorButton elevenPoints;
    private RadioIndicatorButton fiveteenPoints;
    private RadioIndicatorButton twentyOnePoints;

    private FragmentTeam teamOne;
    private FragmentTeam teamTwo;

    public FragmentSetupBadminton() {
        super(Sport.BADMINTON, R.layout.fragment_setup_badminton);
    }

    @Override
    protected void setupControls(View root) {
        gamesOne = root.findViewById(R.id.badmintonGamesOne);
        gamesThree = root.findViewById(R.id.badmintonGamesThree);
        gamesFive = root.findViewById(R.id.badmintonGamesFive);

        singles = root.findViewById(R.id.badmintonSingles);
        doubles = root.findViewById(R.id.badmintonDoubles);

        elevenPoints = root.findViewById(R.id.badmintonPointsEleven);
        fiveteenPoints = root.findViewById(R.id.badmintonPointsFiveteen);
        twentyOnePoints = root.findViewById(R.id.badmintonPointsTwentyOne);

        teamOne = (FragmentTeam) getChildFragmentManager().findFragmentById(R.id.fragmentTeamOne);
        teamTwo = (FragmentTeam) getChildFragmentManager().findFragmentById(R.id.fragmentTeamTwo);

        // listen for the clicks to set the data back to the match setup
        listenForChanges(gamesOne, BadmintonSetup.BadmintonGameOption.ONE);
        listenForChanges(gamesThree, BadmintonSetup.BadmintonGameOption.THREE);
        listenForChanges(gamesFive, BadmintonSetup.BadmintonGameOption.FIVE);

        // and changes to singles or doubles
        listenForChanges(singles, MatchSetup.MatchType.SINGLES);
        listenForChanges(doubles, MatchSetup.MatchType.DOUBLES);

        // and four or six points
        listenForChanges(elevenPoints, BadmintonSetup.BadmintonPointOption.ELEVEN);
        listenForChanges(fiveteenPoints, BadmintonSetup.BadmintonPointOption.FIFTEEN);
        listenForChanges(twentyOnePoints, BadmintonSetup.BadmintonPointOption.TWENTY_ONE);
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

    private void listenForChanges(RadioIndicatorButton setRadio, final BadmintonSetup.BadmintonGameOption noGames) {
        setRadio.addOnCheckChangeListener(new CheckableIndicatorButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                if (isChecked)
                    matchSetup.setGamesInMatch(noGames);
            }
        });
    }

    private void listenForChanges(RadioIndicatorButton pointRadio, final BadmintonSetup.BadmintonPointOption noPoints) {
        pointRadio.addOnCheckChangeListener(new CheckableIndicatorButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                if (isChecked)
                    matchSetup.setPointsInGame(noPoints);
            }
        });
    }

    @Override
    protected void setDataToControls() {
        // setup the data on this page, first is the number of games to play
        if (null != this.matchSetup) {
            // set the number of games
            gamesOne.setChecked(this.matchSetup.getGamesInMatch() == BadmintonSetup.BadmintonGameOption.ONE);
            gamesThree.setChecked(this.matchSetup.getGamesInMatch() == BadmintonSetup.BadmintonGameOption.THREE);
            gamesFive.setChecked(this.matchSetup.getGamesInMatch() == BadmintonSetup.BadmintonGameOption.FIVE);

            // now singles or doubles
            singles.setChecked(this.matchSetup.getType() == MatchSetup.MatchType.SINGLES);
            doubles.setChecked(this.matchSetup.getType() == MatchSetup.MatchType.DOUBLES);

            // points
            elevenPoints.setChecked(this.matchSetup.getPointsInGame() == BadmintonSetup.BadmintonPointOption.ELEVEN);
            fiveteenPoints.setChecked(this.matchSetup.getPointsInGame() == BadmintonSetup.BadmintonPointOption.FIFTEEN);
            twentyOnePoints.setChecked(this.matchSetup.getPointsInGame() == BadmintonSetup.BadmintonPointOption.TWENTY_ONE);

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
