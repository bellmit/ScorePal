package uk.co.darkerwaters.scorepal.ui.matchresults;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.data.PingPongMatch;
import uk.co.darkerwaters.scorepal.data.PingPongSetup;
import uk.co.darkerwaters.scorepal.points.Point;
import uk.co.darkerwaters.scorepal.points.Sport;
import uk.co.darkerwaters.scorepal.ui.UiHelper;

public class FragmentResultsPingPong extends FragmentMatchResults<PingPongMatch> {

    private final static long K_ANIMATION_DURATION = 750;

    TextView teamOneTitle;
    TextView teamTwoTitle;

    TextView teamOnePoints;
    TextView teamTwoPoints;

    TextView teamOneRounds;
    TextView teamTwoRounds;

    private final TextView[] summaryPoints = new TextView[2];
    private final TextView[] totalPoints = new TextView[2];

    private ImageView servingImageView;

    public FragmentResultsPingPong() {
        this(false);
    }
    
    public FragmentResultsPingPong(boolean isAllowExpandContract) {
        super(Sport.BADMINTON, R.layout.fragment_results_pingpong, isAllowExpandContract);
    }

    @Override
    protected void setupControls(View root) {
        super.setupControls(root);
        // setup all the members
        Context context = root == null ? null : root.getContext();

        this.teamOneTitle = root.findViewById(R.id.textViewTeamOne);
        this.teamTwoTitle = root.findViewById(R.id.textViewTeamTwo);
        this.teamOnePoints = root.findViewById(R.id.teamOne_Points);
        this.teamTwoPoints = root.findViewById(R.id.teamTwo_Points);
        this.teamOneRounds = root.findViewById(R.id.teamOne_Rounds);
        this.teamTwoRounds = root.findViewById(R.id.teamTwo_Rounds);

        // set the text colour for team one
        if (null != context) {
            int color = context.getColor(R.color.teamOneColor);
            this.teamOneTitle.setTextColor(color);
            this.teamOnePoints.setTextColor(color);
            this.teamOneRounds.setTextColor(color);
        }

        if (null != context) {
            int color = context.getColor(R.color.teamTwoColor);
            this.teamTwoTitle.setTextColor(color);
            this.teamTwoPoints.setTextColor(color);
            this.teamTwoRounds.setTextColor(color);
        }

        // and the score breakdown we collected
        this.summaryPoints[0] = root.findViewById(R.id.summaryTitleText_teamOne);
        this.summaryPoints[1] = root.findViewById(R.id.summaryTitleText_teamTwo);
        this.totalPoints[0] = root.findViewById(R.id.totalPointsText_teamOne);
        this.totalPoints[1] = root.findViewById(R.id.totalPointsText_teamTwo);

        this.servingImageView = root.findViewById(R.id.servingImageView);
        this.servingImageView.setVisibility(View.INVISIBLE);
        
    }

    @Override
    public void showCurrentServer(PingPongMatch match) {
        if (null != this.servingImageView) {
            if (null != match && !match.isMatchOver()) {
                MatchSetup.Team teamServing = match.getServingTeam();
                if (teamServing.equals(MatchSetup.Team.T_ONE)) {
                    // animate back to where we started
                    this.servingImageView.animate()
                            .translationX(0f)
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
                    servingImageView.setImageResource(R.drawable.ic_keyboard_arrow_right_black_24dp);
                    //BaseActivity.setupButtonIcon(servingImageView, R.drawable.ic_baseline_arrow_forward, R.color.teamOneColor);
                } else {
                    // work out how far down from the top set text changer to the bottom we need to go
                    float distance = (teamTwoPoints.getX() - teamOnePoints.getX()) + teamOnePoints.getWidth() + servingImageView.getWidth() + 4;
                    // and animate to here
                    this.servingImageView.animate()
                            .translationX(distance)
                            .withStartAction(new Runnable() {
                                @Override
                                public void run() {
                                    servingImageView.setVisibility(View.VISIBLE);
                                    //TODO set the colour to team one
                                    //BaseActivity.setupButtonIcon(servingImageView, R.drawable.ic_baseline_arrow_back, R.color.teamTwoColor);
                                }
                            })
                            .setDuration(K_ANIMATION_DURATION)
                            .start();
                    servingImageView.setImageResource(R.drawable.ic_keyboard_arrow_left_black_24dp);
                    //BaseActivity.setupButtonIcon(servingImageView, R.drawable.ic_baseline_arrow_back, R.color.teamTwoColor);
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
            PingPongSetup setup = activeMatch.getSetup();
            Context context = parent == null ? null : parent.getContext();

            // set the titles
            this.teamOneTitle.setText(setup.getTeamName(context, MatchSetup.Team.T_ONE));
            this.teamTwoTitle.setText(setup.getTeamName(context, MatchSetup.Team.T_TWO));

            if (matchWinner.equals(MatchSetup.Team.T_ONE)) {
                // make team one's name bold
                UiHelper.setTextViewBold(this.teamOneTitle);
            } else if (matchWinner.equals(MatchSetup.Team.T_TWO)) {
                // make team two's name bold
                UiHelper.setTextViewBold(this.teamTwoTitle);
            }

            // scroll these names
            this.teamOneTitle.setSelected(true);
            this.teamTwoTitle.setSelected(true);

            // set the points
            Point teamOnePoint = activeMatch.getDisplayPoint(MatchSetup.Team.T_ONE);
            this.teamOnePoints.setText(teamOnePoint.displayString(context));
            Point teamTwoPoint = activeMatch.getDisplayPoint(MatchSetup.Team.T_TWO);
            this.teamTwoPoints.setText(teamTwoPoint.displayString(context));

            if (teamOnePoint.val() > teamTwoPoint.val()) {
                setTextViewBold(this.teamOnePoints);
                setTextViewNoBold(this.teamTwoPoints);
            } else if (teamTwoPoint.val() > teamOnePoint.val()) {
                setTextViewBold(this.teamTwoPoints);
                setTextViewNoBold(this.teamOnePoints);
            } else {
                setTextViewNoBold(this.teamOnePoints);
                setTextViewNoBold(this.teamTwoPoints);
            }

            // set the rounds
            Point teamOneRound = activeMatch.getDisplayRound(MatchSetup.Team.T_ONE);
            this.teamOneRounds.setText(teamOneRound.displayString(context));
            Point teamTwoRound = activeMatch.getDisplayRound(MatchSetup.Team.T_TWO);
            this.teamTwoRounds.setText(teamTwoRound.displayString(context));

            if (teamOneRound.val() > teamTwoRound.val()) {
                setTextViewBold(this.teamOneRounds);
                setTextViewNoBold(this.teamTwoRounds);
            } else if (teamTwoRound.val() > teamOneRound.val()) {
                setTextViewBold(this.teamTwoRounds);
                setTextViewNoBold(this.teamOneRounds);
            } else {
                setTextViewNoBold(this.teamOneRounds);
                setTextViewNoBold(this.teamTwoRounds);
            }

            // set the titles for the summary
            this.summaryPoints[0].setText(setup.getTeamName(context, MatchSetup.Team.T_ONE));
            this.summaryPoints[1].setText(setup.getTeamName(context, MatchSetup.Team.T_TWO));
            // set the total points counters
            this.totalPoints[0].setText(String.format(Locale.getDefault(), "%d", activeMatch.getPointsTotal(0, MatchSetup.Team.T_ONE)));
            this.totalPoints[1].setText(String.format(Locale.getDefault(), "%d", activeMatch.getPointsTotal(0, MatchSetup.Team.T_TWO)));
        }
    }
}
