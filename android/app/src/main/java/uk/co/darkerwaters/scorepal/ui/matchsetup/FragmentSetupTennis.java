package uk.co.darkerwaters.scorepal.ui.matchsetup;

import android.view.View;
import android.widget.ArrayAdapter;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.data.TennisSetup;
import uk.co.darkerwaters.scorepal.points.Sport;
import uk.co.darkerwaters.scorepal.ui.views.CheckableIndicatorButton;
import uk.co.darkerwaters.scorepal.ui.views.RadioIndicatorButton;

public class FragmentSetupTennis extends FragmentSetupMatch<TennisSetup> {

    private RadioIndicatorButton setsOne;
    private RadioIndicatorButton setsThree;
    private RadioIndicatorButton setsFive;

    private RadioIndicatorButton singles;
    private RadioIndicatorButton doubles;

    private RadioIndicatorButton fourGames;
    private RadioIndicatorButton sixGames;

    private CheckableIndicatorButton deuceSuddenDeath;
    private CheckableIndicatorButton finalSetTie;

    private FragmentTeam teamOne;
    private FragmentTeam teamTwo;

    public FragmentSetupTennis() {
        super(Sport.TENNIS, R.layout.fragment_setup_tennis);
    }

    @Override
    protected void setupControls(View root) {
        setsOne = root.findViewById(R.id.tennisSetsOne);
        setsThree = root.findViewById(R.id.tennisSetsThree);
        setsFive = root.findViewById(R.id.tennisSetsFive);

        singles = root.findViewById(R.id.tennisSingles);
        doubles = root.findViewById(R.id.tennisDoubles);

        fourGames = root.findViewById(R.id.tennisGamesFour);
        sixGames = root.findViewById(R.id.tennisGamesSix);

        deuceSuddenDeath = root.findViewById(R.id.deuceSuddenDeathButton);
        finalSetTie = root.findViewById(R.id.finalSetTieBreakButton);

        teamOne = (FragmentTeam) getChildFragmentManager().findFragmentById(R.id.fragmentTeamOne);
        teamTwo = (FragmentTeam) getChildFragmentManager().findFragmentById(R.id.fragmentTeamTwo);

        // listen for the clicks to set the data back to the match setup
        listenForChanges(setsOne, TennisSetup.TennisSet.ONE);
        listenForChanges(setsThree, TennisSetup.TennisSet.THREE);
        listenForChanges(setsFive, TennisSetup.TennisSet.FIVE);

        // and changes to singles or doubles
        listenForChanges(singles, MatchSetup.MatchType.SINGLES);
        listenForChanges(doubles, MatchSetup.MatchType.DOUBLES);

        // and four or six games
        listenForChanges(fourGames, TennisSetup.TennisGame.FOUR);
        listenForChanges(sixGames, TennisSetup.TennisGame.SIX);

        deuceSuddenDeath.addOnCheckChangeListener(new CheckableIndicatorButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                matchSetup.setDeuceSuddenDeath(isChecked);
            }
        });
        finalSetTie.addOnCheckChangeListener(new CheckableIndicatorButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                matchSetup.setIsFinalSetTie(isChecked);
            }
        });
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

    private void listenForChanges(RadioIndicatorButton setRadio, final TennisSetup.TennisSet noSets) {
        setRadio.addOnCheckChangeListener(new CheckableIndicatorButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                if (isChecked)
                    matchSetup.setNumberSets(noSets);
            }
        });
    }

    private void listenForChanges(RadioIndicatorButton gameRadio, final TennisSetup.TennisGame noGames) {
        gameRadio.addOnCheckChangeListener(new CheckableIndicatorButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                if (isChecked)
                    matchSetup.setNumberGames(noGames);
            }
        });
    }

    @Override
    protected void setDataToControls() {
        // setup the data on this page, first is the number of sets to play
        if (null != this.matchSetup) {
            // number sets
            setsOne.setChecked(this.matchSetup.getNumberSets() == TennisSetup.TennisSet.ONE);
            setsThree.setChecked(this.matchSetup.getNumberSets() == TennisSetup.TennisSet.THREE);
            setsFive.setChecked(this.matchSetup.getNumberSets() == TennisSetup.TennisSet.FIVE);

            // now singles or doubles
            singles.setChecked(this.matchSetup.getType() == MatchSetup.MatchType.SINGLES);
            doubles.setChecked(this.matchSetup.getType() == MatchSetup.MatchType.DOUBLES);

            // four or six games
            fourGames.setChecked(this.matchSetup.getNumberGames() == TennisSetup.TennisGame.FOUR);
            sixGames.setChecked(this.matchSetup.getNumberGames() == TennisSetup.TennisGame.SIX);

            // advanced flags
            deuceSuddenDeath.setChecked(this.matchSetup.getIsDeuceSuddenDeath());
            finalSetTie.setChecked(this.matchSetup.getIsFinalSetTie());
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
