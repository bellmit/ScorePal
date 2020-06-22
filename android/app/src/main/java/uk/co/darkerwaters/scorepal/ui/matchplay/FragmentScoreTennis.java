package uk.co.darkerwaters.scorepal.ui.matchplay;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextSwitcher;
import android.widget.TextView;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.data.TennisMatch;
import uk.co.darkerwaters.scorepal.data.TennisScore;

public class FragmentScoreTennis extends FragmentScore<TennisMatch> {


    private TextSwitcher sets;
    private TextSwitcher games;
    private TextSwitcher points;

    private TextView setsTitle;
    private TextView gamesTitle;
    private TextView pointsTitle;

    public FragmentScoreTennis() {
        // Required empty public constructor
        super(R.layout.fragment_score_tennis);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = super.onCreateView(inflater, container, savedInstanceState);

        sets = root.findViewById(R.id.teamSets);
        points = root.findViewById(R.id.teamPoints);
        games = root.findViewById(R.id.teamRounds);

        setsTitle = root.findViewById(R.id.teamSetsTitle);
        pointsTitle = root.findViewById(R.id.teamPointsTitle);
        gamesTitle = root.findViewById(R.id.teamRoundsTitle);

        return root;
    }

    @Override
    public void setupControls(MatchSetup.Team team) {
        super.setupControls(team);
        int color;
        if (team == MatchSetup.Team.T_TWO) {
            color = getActivity().getColor(R.color.teamTwoColor);
        } else {
            color = getActivity().getColor(R.color.teamOneColor);
        }
        // set the control colours
        setTeamColor(color, new TextSwitcher[]{sets, points, games });
        setTeamColor(color, new TextView[]{setsTitle, pointsTitle, gamesTitle });

        // listen for the user clicking one of the switchers
        points.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickPoints();
            }
        });
        games.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickGames();
            }
        });
        // DON'T LET THEM CLICK SETS - at this time doesn't work too well - doesn't clear points scored
        /*sets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickSets();
            }
        });*/
    }

    @Override
    public void displayCurrentScore() {
        if (null != activeMatch) {
            Context context = getContext();
            setSwitcherText(sets, activeMatch.getDisplayPoint(TennisScore.LEVEL_SET, team).displayString(context));
            setSwitcherText(games, activeMatch.getDisplayPoint(TennisScore.LEVEL_GAME, team).displayString(context));
            setSwitcherText(points, activeMatch.getDisplayPoint(TennisScore.LEVEL_POINT, team).displayString(context));
        }
    }

    private void onClickPoints() {
        // add a 'point' to the score
        if (null != activeMatch && !activeMatch.isMatchOver()) {
            activeMatch.incrementPoint(this.team);
        }
    }

    private void onClickGames() {
        // add a 'game' to the score
        if (null != activeMatch && !activeMatch.isMatchOver()) {
            activeMatch.incrementGame(this.team);
        }
    }

    private void onClickSets() {
        // add a 'set' to the score
        if (null != activeMatch && !activeMatch.isMatchOver()) {
            activeMatch.incrementSet(this.team);
        }
    }
}
