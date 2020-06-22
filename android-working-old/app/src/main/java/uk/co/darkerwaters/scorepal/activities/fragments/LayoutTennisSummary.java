package uk.co.darkerwaters.scorepal.activities.fragments;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.BaseActivity;
import uk.co.darkerwaters.scorepal.activities.handlers.MatchRecyclerAdapter;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.base.Point;
import uk.co.darkerwaters.scorepal.score.tennis.TennisMatch;
import uk.co.darkerwaters.scorepal.score.tennis.TennisScore;

public class LayoutTennisSummary extends LayoutScoreSummary<TennisMatch> {

    private final static long K_ANIMATION_DURATION = 1000;

    private static final int K_TITLES = 0;
    private static final int K_TEAM1 = 1;
    private static final int K_TIE = 2;
    private static final int K_TEAM2 = 3;

    private static final int K_ROWS = 4;
    private static final int K_COLS = 6;

    private final TextView[] teamOneTitles = new TextView[2];
    private final TextView[] teamTwoTitles = new TextView[2];
    
    private TextView[][] textViews;

    private final TextView[] totalPoints = new TextView[2];
    private final TextView[] breakPoints = new TextView[2];

    private ImageView servingImageView;

    public LayoutTennisSummary() {
        super();
    }


    @Override
    public View createView(LayoutInflater inflater, ViewGroup container) {
        // create the layout on the parent view
        View mainView = inflater.inflate(R.layout.layout_tennis_summary, container, false);
        initialiseViewContents(mainView);
        return mainView;
    }

    @Override
    public void initialiseViewContents(View mainView) {
        // this main view is our parent, use this
        super.initialiseViewContents(mainView);

        // setup all the members
        this.teamOneTitles[0] = this.parent.findViewById(R.id.textViewTeamOne);
        this.teamTwoTitles[0] = this.parent.findViewById(R.id.textViewTeamTwo);
        this.teamOneTitles[1] = this.parent.findViewById(R.id.textViewTeamOneWide);
        this.teamTwoTitles[1] = this.parent.findViewById(R.id.textViewTeamTwoWide);

        // create the array - to prevent to many members
        this.textViews = new TextView[K_ROWS][K_COLS];
        this.servingImageView = this.parent.findViewById(R.id.servingImageView);
        this.servingImageView.setVisibility(View.INVISIBLE);

        // find all the titles
        this.textViews[K_TITLES][0] = this.parent.findViewById(R.id.textViewPointsTitle);
        this.textViews[K_TITLES][1] = this.parent.findViewById(R.id.textViewSet1Title);
        this.textViews[K_TITLES][2] = this.parent.findViewById(R.id.textViewSet2Title);
        this.textViews[K_TITLES][3] = this.parent.findViewById(R.id.textViewSet3Title);
        this.textViews[K_TITLES][4] = this.parent.findViewById(R.id.textViewSet4Title);
        this.textViews[K_TITLES][5] = this.parent.findViewById(R.id.textViewSet5Title);

        // find all the text textViews here for team one
        this.textViews[K_TEAM1][0] = this.parent.findViewById(R.id.teamOne_Points);
        this.textViews[K_TEAM1][1] = this.parent.findViewById(R.id.teamOne_setOne);
        this.textViews[K_TEAM1][2] = this.parent.findViewById(R.id.teamOne_setTwo);
        this.textViews[K_TEAM1][3] = this.parent.findViewById(R.id.teamOne_setThree);
        this.textViews[K_TEAM1][4] = this.parent.findViewById(R.id.teamOne_setFour);
        this.textViews[K_TEAM1][5] = this.parent.findViewById(R.id.teamOne_setFive);

        // and the text views for the tie-break results
        this.textViews[K_TIE][0] = null;
        this.textViews[K_TIE][1] = this.parent.findViewById(R.id.tieBreak_setOne);
        this.textViews[K_TIE][2] = this.parent.findViewById(R.id.tieBreak_setTwo);
        this.textViews[K_TIE][3] = this.parent.findViewById(R.id.tieBreak_setThree);
        this.textViews[K_TIE][4] = this.parent.findViewById(R.id.tieBreak_setFour);
        this.textViews[K_TIE][5] = this.parent.findViewById(R.id.tieBreak_setFive);

        // and team two
        this.textViews[K_TEAM2][0] = this.parent.findViewById(R.id.teamTwo_Points);
        this.textViews[K_TEAM2][1] = this.parent.findViewById(R.id.teamTwo_setOne);
        this.textViews[K_TEAM2][2] = this.parent.findViewById(R.id.teamTwo_setTwo);
        this.textViews[K_TEAM2][3] = this.parent.findViewById(R.id.teamTwo_setThree);
        this.textViews[K_TEAM2][4] = this.parent.findViewById(R.id.teamTwo_setFour);
        this.textViews[K_TEAM2][5] = this.parent.findViewById(R.id.teamTwo_setFive);

        // set the colour for team one
        int color = parent.getContext().getColor(R.color.teamOneColor);
        this.teamOneTitles[0].setTextColor(color);
        this.teamOneTitles[1].setTextColor(color);
        setTextColor(K_TEAM1, color);

        // and team two
        color = parent.getContext().getColor(R.color.teamTwoColor);
        this.teamTwoTitles[0].setTextColor(color);
        this.teamTwoTitles[1].setTextColor(color);
        setTextColor(K_TEAM2, color);

        // and the score breakdown we collected
        this.totalPoints[0] = this.parent.findViewById(R.id.totalPointsText_teamOne);
        this.totalPoints[1] = this.parent.findViewById(R.id.totalPointsText_teamTwo);
        this.breakPoints[0] = this.parent.findViewById(R.id.breakPointsText_teamOne);
        this.breakPoints[1] = this.parent.findViewById(R.id.breakPointsText_teamTwo);
    }

    private void setTextColor(int row, int color) {
        for (int i = 0; i < K_COLS; ++i) {
            this.textViews[row][i].setText("0");
            this.textViews[row][i].setTextColor(color);
        }
    }

    @Override
    public void showCurrentServer(TennisMatch match) {
        if (null != this.servingImageView) {
            if (null != match && !match.isMatchOver()) {
                Team teamServing = match.getTeamServing();
                if (teamServing == match.getTeamOne()) {
                    // animate back to where we started
                    this.servingImageView.animate()
                            .translationY(0f)
                            .withStartAction(new Runnable() {
                                @Override
                                public void run() {
                                    servingImageView.setVisibility(View.VISIBLE);
                                    BaseActivity.setupButtonIcon(servingImageView, R.drawable.ic_baseline_arrow_forward, R.color.teamOneColor);
                                }
                            })
                            .setDuration(K_ANIMATION_DURATION)
                            .start();
                } else {
                    // work out how far down from the top set text changer to the bottom we need to go
                    float distance = this.textViews[K_TEAM2][0].getY() - this.textViews[K_TEAM1][0].getY();
                    // and animate to here
                    this.servingImageView.animate()
                            .translationY(distance)
                            .withStartAction(new Runnable() {
                                @Override
                                public void run() {
                                    servingImageView.setVisibility(View.VISIBLE);
                                    BaseActivity.setupButtonIcon(servingImageView, R.drawable.ic_baseline_arrow_forward, R.color.teamTwoColor);
                                }
                            })
                            .setDuration(K_ANIMATION_DURATION)
                            .start();
                }
            }
            else {
                this.servingImageView.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void setMatchData(final TennisMatch match, final MatchRecyclerAdapter.MatchFileListener source) {
        super.setMatchData(match, source);
        // set all the data from this match on this view
        Context context = parent.getContext();
        TennisScore score = match.getScore();
        Team teamOne = match.getTeamOne();
        Team teamTwo = match.getTeamTwo();

        // who won
        Team matchWinner = match.getMatchWinner();

        // set the titles
        this.teamOneTitles[0].setText(teamOne.getTeamName());
        this.teamOneTitles[1].setText(teamOne.getTeamName());
        this.teamTwoTitles[0].setText(teamTwo.getTeamName());
        this.teamTwoTitles[1].setText(teamTwo.getTeamName());

        if (matchWinner == teamOne) {
            // make team one's name bold
            BaseActivity.setTextViewBold(this.teamOneTitles[0]);
            BaseActivity.setTextViewBold(this.teamOneTitles[1]);
            // make team two's name un-bold
            BaseActivity.setTextViewNoBold(this.teamTwoTitles[0]);
            BaseActivity.setTextViewNoBold(this.teamTwoTitles[1]);
        }
        else if (matchWinner == teamTwo) {
            // make team two's name bold
            BaseActivity.setTextViewBold(this.teamTwoTitles[0]);
            BaseActivity.setTextViewBold(this.teamTwoTitles[1]);
            // make team one's name un-bold
            BaseActivity.setTextViewNoBold(this.teamOneTitles[0]);
            BaseActivity.setTextViewNoBold(this.teamOneTitles[1]);
        }

        // scroll these names
        this.teamOneTitles[0].setSelected(true);
        this.teamOneTitles[1].setSelected(true);
        this.teamTwoTitles[0].setSelected(true);
        this.teamTwoTitles[1].setSelected(true);

        // set the points
        Point teamOnePoint = score.getDisplayPoint(teamOne);
        this.textViews[K_TEAM1][0].setText(teamOnePoint.displayString(context));

        // set the points
        Point teamTwoPoint = score.getDisplayPoint(teamTwo);
        this.textViews[K_TEAM2][0].setText(teamTwoPoint.displayString(context));

        if (teamOnePoint.val() > teamTwoPoint.val()) {
            setTextViewBold(this.textViews[K_TEAM1][0]);
            setTextViewNoBold(this.textViews[K_TEAM2][0]);
        }
        else if (teamTwoPoint.val() > teamOnePoint.val()) {
            setTextViewBold(this.textViews[K_TEAM2][0]);
            setTextViewNoBold(this.textViews[K_TEAM1][0]);
        }
        else {
            setTextViewNoBold(this.textViews[K_TEAM1][0]);
            setTextViewNoBold(this.textViews[K_TEAM2][0]);
        }

        if (score.isMatchOver()) {
            // match is over, get rid of the points boxes
            setColumnVisibility(0, View.INVISIBLE);
            // change the title from points to sets
            this.textViews[K_TITLES][0].setText(R.string.sets);
            this.textViews[K_TITLES][0].setVisibility(View.VISIBLE);

            // this leave us some room to show the wide titles
            this.teamOneTitles[0].setVisibility(View.INVISIBLE);
            this.teamOneTitles[1].setVisibility(View.VISIBLE);

            this.teamTwoTitles[0].setVisibility(View.INVISIBLE);
            this.teamTwoTitles[1].setVisibility(View.VISIBLE);
        }
        else {
            // show the columns
            setColumnVisibility(0, View.VISIBLE);
            // change the title back to points from sets
            this.textViews[K_TITLES][0].setText(R.string.points);
            this.textViews[K_TITLES][0].setVisibility(View.VISIBLE);

            // this leave us some room to show the wide titles
            this.teamOneTitles[0].setVisibility(View.VISIBLE);
            this.teamOneTitles[1].setVisibility(View.INVISIBLE);

            this.teamTwoTitles[0].setVisibility(View.VISIBLE);
            this.teamTwoTitles[1].setVisibility(View.INVISIBLE);
        }

        // and all the previous sets
        int setsPlayed = score.getPlayedSets();
        int colIndex;
        for (int i = 0; i < K_COLS - 1; ++i) {
            // the set index is from 0 to 5, the column index will be 1-6
            colIndex = i + 1;
            int playerOneGames = score.getGames(teamOne, i);
            int playerTwoGames = score.getGames(teamTwo, i);
            if (i  > setsPlayed || (playerOneGames == 0 && playerTwoGames == 0)) {
                // this set wasn't played, need to hide this column (don't delete them all
                // as this makes things massive!)
                setColumnVisibility(colIndex, View.INVISIBLE);
            }
            else {
                // this has data so we want to see it
                setColumnVisibility(colIndex, View.VISIBLE);
                // set the text to be the number of games
                this.textViews[K_TEAM1][colIndex].setText(String.format(Locale.getDefault(), "%d", playerOneGames));
                this.textViews[K_TEAM2][colIndex].setText(String.format(Locale.getDefault(), "%d", playerTwoGames));
                if (playerOneGames > playerTwoGames) {
                    // player one is winning
                    setTextViewBold(this.textViews[K_TEAM1][colIndex]);
                    setTextViewNoBold(this.textViews[K_TEAM2][colIndex]);
                }
                else if (playerTwoGames > playerOneGames) {
                    // player two is winning
                    setTextViewBold(this.textViews[K_TEAM2][colIndex]);
                    setTextViewNoBold(this.textViews[K_TEAM1][colIndex]);
                }
                else {
                    setTextViewNoBold(this.textViews[K_TEAM1][colIndex]);
                    setTextViewNoBold(this.textViews[K_TEAM2][colIndex]);
                }

                int[] tiePoints = null;
                if (score.isSetTieBreak(i)) {
                    // this set is / was a tie, show the score of this in brackets
                    tiePoints = score.getPoints(i, playerOneGames + playerTwoGames - 1);
                    if (null == tiePoints || tiePoints.length < 2) {
                        tiePoints = score.getPoints(i, playerOneGames + playerTwoGames);
                    }
                }
                if (null != tiePoints && tiePoints.length > 1) {
                    String tieResult = "(" + tiePoints[0] + "-" + tiePoints[1] + ")";
                    this.textViews[K_TIE][colIndex].setVisibility(View.VISIBLE);
                    this.textViews[K_TIE][colIndex].setText(tieResult);
                }
                else {
                    this.textViews[K_TIE][colIndex].setVisibility(View.INVISIBLE);
                }
            }
        }

        // set the total points counters
        this.totalPoints[0].setText(String.format(Locale.getDefault(), "%d", match.getPointsTotal(0, 0)));
        this.totalPoints[1].setText(String.format(Locale.getDefault(), "%d", match.getPointsTotal(0, 1)));

        // and the break point counters
        this.breakPoints[0].setText(context.getString(R.string.break_points_converted
                , score.getBreakPointsConverted(0)
                , score.getBreakPoints(0)));
        this.breakPoints[1].setText(context.getString(R.string.break_points_converted
                , score.getBreakPointsConverted(1)
                , score.getBreakPoints(1)));
    }

    private void setColumnVisibility(int colIndex, int visibility) {
        for (int i = 0; i < K_ROWS; ++i) {
            TextView view = this.textViews[i][colIndex];
            if (null != view) {
                view.setVisibility(visibility);
            }
        }
    }
}
