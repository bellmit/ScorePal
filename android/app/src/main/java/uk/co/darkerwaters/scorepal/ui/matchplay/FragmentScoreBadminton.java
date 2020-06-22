package uk.co.darkerwaters.scorepal.ui.matchplay;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextSwitcher;
import android.widget.TextView;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.BadmintonMatch;
import uk.co.darkerwaters.scorepal.data.BadmintonScore;
import uk.co.darkerwaters.scorepal.data.MatchSetup;

public class FragmentScoreBadminton extends FragmentScore<BadmintonMatch> {

    private TextSwitcher games;
    private TextSwitcher points;

    private TextView gamesTitle;
    private TextView pointsTitle;

    public FragmentScoreBadminton() {
        // Required empty public constructor
        super(R.layout.fragment_score_badminton);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = super.onCreateView(inflater, container, savedInstanceState);

        points = root.findViewById(R.id.teamPoints);
        games = root.findViewById(R.id.teamRounds);

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
        setTeamColor(color, new TextSwitcher[]{ points, games });
        setTeamColor(color, new TextView[]{ pointsTitle, gamesTitle });

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
    }

    public void displayCurrentScore() {
        if (null != activeMatch) {
            Context context = getContext();
            setSwitcherText(games, activeMatch.getDisplayPoint(BadmintonScore.LEVEL_GAME, team).displayString(context));
            setSwitcherText(points, activeMatch.getDisplayPoint(BadmintonScore.LEVEL_POINT, team).displayString(context));
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
}
