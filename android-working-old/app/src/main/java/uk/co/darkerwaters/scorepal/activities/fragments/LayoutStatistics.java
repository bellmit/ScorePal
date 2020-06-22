package uk.co.darkerwaters.scorepal.activities.fragments;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.StatisticsActivity;
import uk.co.darkerwaters.scorepal.application.Application;
import uk.co.darkerwaters.scorepal.score.MatchStatistics;
import uk.co.darkerwaters.scorepal.views.CircularProgressBar;

public class LayoutStatistics {

    private final TextView statsTotalText;

    private final CircularProgressBar statsWinsProgress;
    private final CircularProgressBar statsLossesProgress;

    protected final View parent;

    public LayoutStatistics(View mainView) {
        // this main view is our parent, use this
        this.parent = mainView;

        this.statsTotalText = mainView.findViewById(R.id.statsTotalText);

        this.statsWinsProgress = mainView.findViewById(R.id.progressWins);
        this.statsLossesProgress = mainView.findViewById(R.id.progressLosses);
    }

    public void updateDisplay(Application application, Context context) {
        MatchStatistics matchStatistics = MatchStatistics.GetInstance(application, context);
        // load the data we need
        matchStatistics.loadIfNeeded(context);
        // and get the numbers to show
        float total = matchStatistics.getRecentMatchesRecorded();
        int wins = matchStatistics.getRecentWinsTotal();
        int losses = matchStatistics.getRecentLossesTotal();

        this.statsTotalText.setText(String.format(Locale.getDefault(), "%d", (int)total));

        if (total > 0) {
            float progress = wins / total * 100f;
            this.statsWinsProgress.setProgress((int)progress);
            progress = losses / total * 100f;
            this.statsLossesProgress.setProgress((int)progress);
        }
        else {
            this.statsWinsProgress.setProgress(0);
            this.statsLossesProgress.setProgress(0);
        }
        // set the text for the numbers
        this.statsWinsProgress.setTitle(Integer.toString(wins));
        this.statsLossesProgress.setTitle(Integer.toString(losses));
        // and the subtitles
        this.statsWinsProgress.setSubTitle(context.getString(R.string.statistics_wins));
        this.statsLossesProgress.setSubTitle(context.getString(R.string.statistics_losses));

        this.statsWinsProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createStatsClickHandler();
            }
        });
        this.statsLossesProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createStatsClickHandler();
            }
        });
    }

    private void createStatsClickHandler() {
        // show the stats activity when they click a graph
        Context context = this.parent.getContext();
        Intent myIntent = new Intent(context, StatisticsActivity.class);
        context.startActivity(myIntent);
    }
}
