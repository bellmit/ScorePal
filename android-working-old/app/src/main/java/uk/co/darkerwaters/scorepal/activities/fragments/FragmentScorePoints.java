package uk.co.darkerwaters.scorepal.activities.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextSwitcher;

import uk.co.darkerwaters.scorepal.R;

public class FragmentScorePoints extends FragmentScore {

    private final static int K_NO_LEVELS = 1;

    public FragmentScorePoints() {
        super(K_NO_LEVELS);
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_score_points, container, false);
        // init the activity
        setupActivity(mainView);
        // and return the view
        return mainView;
    }

    @Override
    protected void createViewSwitchers(TextSwitcher[][] switchers, View mainView) {
        // find the text switcher here for team one
        switchers[0][0] = mainView.findViewById(R.id.points_teamOne);
        // and team two
        switchers[1][0] = mainView.findViewById(R.id.points_teamTwo);
    }
}
