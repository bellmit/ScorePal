package uk.co.darkerwaters.scorepal.ui.matchresults;

import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.data.TennisMatch;
import uk.co.darkerwaters.scorepal.data.TennisScore;
import uk.co.darkerwaters.scorepal.data.TennisSetup;
import uk.co.darkerwaters.scorepal.points.Point;
import uk.co.darkerwaters.scorepal.points.Sport;
import uk.co.darkerwaters.scorepal.ui.UiHelper;

public class FragmentResultsTennis extends FragmentMatchResults<TennisMatch> {

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

    private final TextView[] summaryPoints = new TextView[2];
    private final TextView[] totalPoints = new TextView[2];
    private final TextView[] breakPoints = new TextView[2];

    private ImageView servingImageView;

    public FragmentResultsTennis() {
        this(false);
    }

    public FragmentResultsTennis(boolean isAllowExpandContract) {
        super(Sport.TENNIS, R.layout.fragment_results_tennis, isAllowExpandContract);
    }

    @Override
    protected void setupControls(View root) {
        super.setupControls(root);
        Context context = root == null ? null : root.getContext();

        // setup all the members
        this.teamOneTitles[0] = root.findViewById(R.id.textViewTeamOne);
        this.teamTwoTitles[0] = root.findViewById(R.id.textViewTeamTwo);
        this.teamOneTitles[1] = root.findViewById(R.id.textViewTeamOneWide);
        this.teamTwoTitles[1] = root.findViewById(R.id.textViewTeamTwoWide);

        // create the array - to prevent to many members
        this.textViews = new TextView[K_ROWS][K_COLS];
        this.servingImageView = root.findViewById(R.id.servingImageView);
        this.servingImageView.setVisibility(View.INVISIBLE);

        // find all the titles
        this.textViews[K_TITLES][0] = root.findViewById(R.id.textViewPointsTitle);
        this.textViews[K_TITLES][1] = root.findViewById(R.id.textViewSet1Title);
        this.textViews[K_TITLES][2] = root.findViewById(R.id.textViewSet2Title);
        this.textViews[K_TITLES][3] = root.findViewById(R.id.textViewSet3Title);
        this.textViews[K_TITLES][4] = root.findViewById(R.id.textViewSet4Title);
        this.textViews[K_TITLES][5] = root.findViewById(R.id.textViewSet5Title);

        // find all the text textViews here for team one
        this.textViews[K_TEAM1][0] = root.findViewById(R.id.teamOne_Points);
        this.textViews[K_TEAM1][1] = root.findViewById(R.id.teamOne_setOne);
        this.textViews[K_TEAM1][2] = root.findViewById(R.id.teamOne_setTwo);
        this.textViews[K_TEAM1][3] = root.findViewById(R.id.teamOne_setThree);
        this.textViews[K_TEAM1][4] = root.findViewById(R.id.teamOne_setFour);
        this.textViews[K_TEAM1][5] = root.findViewById(R.id.teamOne_setFive);

        // and the text views for the tie-break results
        this.textViews[K_TIE][0] = null;
        this.textViews[K_TIE][1] = root.findViewById(R.id.tieBreak_setOne);
        this.textViews[K_TIE][2] = root.findViewById(R.id.tieBreak_setTwo);
        this.textViews[K_TIE][3] = root.findViewById(R.id.tieBreak_setThree);
        this.textViews[K_TIE][4] = root.findViewById(R.id.tieBreak_setFour);
        this.textViews[K_TIE][5] = root.findViewById(R.id.tieBreak_setFive);

        // and team two
        this.textViews[K_TEAM2][0] = root.findViewById(R.id.teamTwo_Points);
        this.textViews[K_TEAM2][1] = root.findViewById(R.id.teamTwo_setOne);
        this.textViews[K_TEAM2][2] = root.findViewById(R.id.teamTwo_setTwo);
        this.textViews[K_TEAM2][3] = root.findViewById(R.id.teamTwo_setThree);
        this.textViews[K_TEAM2][4] = root.findViewById(R.id.teamTwo_setFour);
        this.textViews[K_TEAM2][5] = root.findViewById(R.id.teamTwo_setFive);

        // set the colour for team one
        if (null != context) {
            int color = context.getColor(R.color.teamOneColor);
            this.teamOneTitles[0].setTextColor(color);
            this.teamOneTitles[1].setTextColor(color);
            setTextColor(K_TEAM1, color);
        }

        // and team two
        if (null != context) {
            int color = context.getColor(R.color.teamTwoColor);
            this.teamTwoTitles[0].setTextColor(color);
            this.teamTwoTitles[1].setTextColor(color);
            setTextColor(K_TEAM2, color);
        }

        // and the score breakdown we collected
        this.summaryPoints[0] = root.findViewById(R.id.summaryTitleText_teamOne);
        this.summaryPoints[1] = root.findViewById(R.id.summaryTitleText_teamTwo);
        this.totalPoints[0] = root.findViewById(R.id.totalPointsText_teamOne);
        this.totalPoints[1] = root.findViewById(R.id.totalPointsText_teamTwo);
        this.breakPoints[0] = root.findViewById(R.id.breakPointsText_teamOne);
        this.breakPoints[1] = root.findViewById(R.id.breakPointsText_teamTwo);
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
                MatchSetup.Team teamServing = match.getServingTeam();
                if (teamServing == MatchSetup.Team.T_ONE) {
                    // animate back to where we started
                    this.servingImageView.animate()
                            .translationY(0f)
                            .withStartAction(new Runnable() {
                                @Override
                                public void run() {
                                    servingImageView.setVisibility(View.VISIBLE);
                                    //TODO set the colour to team one
                                    //UiHelper.setupButtonIcon(servingImageView, R.drawable.ic_baseline_arrow_forward, R.color.teamOneColor);
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
                                    //TODO set the colour to team two
                                    //UiHelper.setupButtonIcon(servingImageView, R.drawable.ic_baseline_arrow_forward, R.color.teamTwoColor);
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
    public void showMatchData() {
        super.showMatchData();
        if (isMemberDataSet()) {
            // set all the data from this match on this view
            MatchSetup.Team matchWinner = activeMatch.getMatchWinner();
            TennisSetup setup = activeMatch.getSetup();
            Context context = parent == null ? null : parent.getContext();
            // set the titles
            this.teamOneTitles[0].setText(setup.getTeamName(context, MatchSetup.Team.T_ONE));
            this.teamOneTitles[1].setText(setup.getTeamName(context, MatchSetup.Team.T_ONE));
            this.teamTwoTitles[0].setText(setup.getTeamName(context, MatchSetup.Team.T_TWO));
            this.teamTwoTitles[1].setText(setup.getTeamName(context, MatchSetup.Team.T_TWO));

            if (matchWinner == MatchSetup.Team.T_ONE) {
                // make team one's name bold
                UiHelper.setTextViewBold(this.teamOneTitles[0]);
                UiHelper.setTextViewBold(this.teamOneTitles[1]);
                // make team two's name un-bold
                UiHelper.setTextViewNoBold(this.teamTwoTitles[0]);
                UiHelper.setTextViewNoBold(this.teamTwoTitles[1]);
            } else if (matchWinner == MatchSetup.Team.T_TWO) {
                // make team two's name bold
                UiHelper.setTextViewBold(this.teamTwoTitles[0]);
                UiHelper.setTextViewBold(this.teamTwoTitles[1]);
                // make team one's name un-bold
                UiHelper.setTextViewNoBold(this.teamOneTitles[0]);
                UiHelper.setTextViewNoBold(this.teamOneTitles[1]);
            }

            // scroll these names
            this.teamOneTitles[0].setSelected(true);
            this.teamOneTitles[1].setSelected(true);
            this.teamTwoTitles[0].setSelected(true);
            this.teamTwoTitles[1].setSelected(true);

            // set the points
            Point teamOnePoint = activeMatch.getDisplayPoint(TennisScore.LEVEL_POINT, MatchSetup.Team.T_ONE);
            this.textViews[K_TEAM1][0].setText(teamOnePoint.displayString(context));

            // set the points
            Point teamTwoPoint = activeMatch.getDisplayPoint(TennisScore.LEVEL_POINT, MatchSetup.Team.T_TWO);
            this.textViews[K_TEAM2][0].setText(teamTwoPoint.displayString(context));

            if (teamOnePoint.val() > teamTwoPoint.val()) {
                setTextViewBold(this.textViews[K_TEAM1][0]);
                setTextViewNoBold(this.textViews[K_TEAM2][0]);
            } else if (teamTwoPoint.val() > teamOnePoint.val()) {
                setTextViewBold(this.textViews[K_TEAM2][0]);
                setTextViewNoBold(this.textViews[K_TEAM1][0]);
            } else {
                setTextViewNoBold(this.textViews[K_TEAM1][0]);
                setTextViewNoBold(this.textViews[K_TEAM2][0]);
            }

            if (activeMatch.isMatchOver()) {
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
            } else {
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
            int setsPlayed = activeMatch.getPlayedSets();
            int colIndex;
            for (int i = 0; i < K_COLS - 1; ++i) {
                // the set index is from 0 to 5, the column index will be 1-6
                colIndex = i + 1;
                Point playerOneGames = activeMatch.getGames(MatchSetup.Team.T_ONE, i);
                Point playerTwoGames = activeMatch.getGames(MatchSetup.Team.T_TWO, i);
                if (i > setsPlayed || (playerOneGames.val() == 0 && playerTwoGames.val() == 0)) {
                    // this set wasn't played, need to hide this column (don't delete them all
                    // as this makes things massive!)
                    setColumnVisibility(colIndex, View.INVISIBLE);
                } else {
                    // this has data so we want to see it
                    setColumnVisibility(colIndex, View.VISIBLE);
                    // set the text to be the number of games
                    this.textViews[K_TEAM1][colIndex].setText(playerOneGames.displayString(context));
                    this.textViews[K_TEAM2][colIndex].setText(playerTwoGames.displayString(context));
                    if (playerOneGames.val() > playerTwoGames.val()) {
                        // player one is winning
                        setTextViewBold(this.textViews[K_TEAM1][colIndex]);
                        setTextViewNoBold(this.textViews[K_TEAM2][colIndex]);
                    } else if (playerTwoGames.val() > playerOneGames.val()) {
                        // player two is winning
                        setTextViewBold(this.textViews[K_TEAM2][colIndex]);
                        setTextViewNoBold(this.textViews[K_TEAM1][colIndex]);
                    } else {
                        setTextViewNoBold(this.textViews[K_TEAM1][colIndex]);
                        setTextViewNoBold(this.textViews[K_TEAM2][colIndex]);
                    }

                    Pair<Point, Point> tiePoints = null;
                    if (activeMatch.isSetTieBreak(i)) {
                        // this set is / was a tie, show the score of this in brackets
                        tiePoints = activeMatch.getPoints(i, playerOneGames.val() + playerTwoGames.val() - 1);
                    }
                    if (null != tiePoints) {
                        String tieResult = "(" + tiePoints.first.displayString(context) + "-" + tiePoints.second.displayString(context) + ")";
                        this.textViews[K_TIE][colIndex].setVisibility(View.VISIBLE);
                        this.textViews[K_TIE][colIndex].setText(tieResult);
                    } else {
                        this.textViews[K_TIE][colIndex].setVisibility(View.INVISIBLE);
                    }
                }
            }

            // set the titles for the summary
            this.summaryPoints[0].setText(setup.getTeamName(context, MatchSetup.Team.T_ONE));
            this.summaryPoints[1].setText(setup.getTeamName(context, MatchSetup.Team.T_TWO));
            // set the total points counters
            this.totalPoints[0].setText(String.format(Locale.getDefault(), "%d", activeMatch.getPointsTotal(0, MatchSetup.Team.T_ONE)));
            this.totalPoints[1].setText(String.format(Locale.getDefault(), "%d", activeMatch.getPointsTotal(0, MatchSetup.Team.T_TWO)));

            // and the break point counters
            if (null != context) {
                this.breakPoints[0].setText(context.getString(R.string.break_points_converted
                        , activeMatch.getBreakPointsConverted(MatchSetup.Team.T_ONE)
                        , activeMatch.getBreakPoints(MatchSetup.Team.T_ONE)));
                this.breakPoints[1].setText(context.getString(R.string.break_points_converted
                        , activeMatch.getBreakPointsConverted(MatchSetup.Team.T_TWO)
                        , activeMatch.getBreakPoints(MatchSetup.Team.T_TWO)));
            }
        }
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
