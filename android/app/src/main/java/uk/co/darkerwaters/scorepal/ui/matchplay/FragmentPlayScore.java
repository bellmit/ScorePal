package uk.co.darkerwaters.scorepal.ui.matchplay;

import android.content.res.ColorStateList;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import androidx.core.widget.ImageViewCompat;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.Match;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.data.Score;
import uk.co.darkerwaters.scorepal.points.Sport;
import uk.co.darkerwaters.scorepal.ui.views.ChangeTeamServerSnackbar;

public abstract class FragmentPlayScore <TMatch extends Match, TScore extends Score> extends FragmentMatchPlay<TMatch, TScore> {

    private FragmentScoreTeam teamOne;
    private FragmentScoreTeam teamTwo;
    private FragmentScore<TMatch> scoreOne;
    private FragmentScore<TMatch> scoreTwo;

    private ImageView activeServerImage;
    private ImageView activeServerTeamImage;
    private ImageView teamOneServerImage;
    private ImageView teamTwoServerImage;

    private ImageView teamOnePlayerServe;
    private ImageView teamOnePartnerServe;
    private ImageView teamTwoPlayerServe;
    private ImageView teamTwoPartnerServe;

    private ChangeTeamServerSnackbar changeServerSnackbar;

    public FragmentPlayScore(Sport sport, int fragmentId) {
        super(sport, fragmentId);
    }

    @Override
    protected void setupControls(View root) {
        super.setupControls(root);
        // get all the controls to use
        changeServerSnackbar = null;
        activeServerImage = root.findViewById(R.id.activeServerImage);
        activeServerTeamImage = root.findViewById(R.id.activeServerImageTeam);
        // get all the fragments that constitute this fragment
        teamOne = (FragmentScoreTeam) getChildFragmentManager().findFragmentById(R.id.teamOneFragment);
        scoreOne = (FragmentScore<TMatch>) getChildFragmentManager().findFragmentById(R.id.teamOneScoreFragment);

        teamTwo = (FragmentScoreTeam) getChildFragmentManager().findFragmentById(R.id.teamTwoFragment);
        scoreTwo = (FragmentScore<TMatch>) getChildFragmentManager().findFragmentById(R.id.teamTwoScoreFragment);

        // and setup the fragment's controls
        teamOne.setupControls(MatchSetup.Team.T_ONE);
        teamTwo.setupControls(MatchSetup.Team.T_TWO);
        // and the score
        scoreOne.setupControls(MatchSetup.Team.T_ONE);
        scoreTwo.setupControls(MatchSetup.Team.T_TWO);

        teamOneServerImage = scoreOne.findViewById(R.id.activeServingTeamImage);
        teamTwoServerImage = scoreTwo.findViewById(R.id.activeServingTeamImage);

        // get all the serve icons in the fragment
        teamOnePlayerServe = teamOne.findViewById(R.id.teamPlayerServe);
        teamOnePartnerServe = teamOne.findViewById(R.id.teamPartnerServe);
        teamTwoPlayerServe = teamTwo.findViewById(R.id.teamPlayerServe);
        teamTwoPartnerServe = teamTwo.findViewById(R.id.teamPartnerServe);
    }

    @Override
    public void onResume() {
        super.onResume();
        // let the screen show and animate the icon into place
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                animateServerIconToServer();
            }
        }, 1000);
    }

    @Override
    protected void setDataToControls(TMatch activeMatch, TScore score) {
        if (score == null) {
            // this is the initial setup, not a score update - set everything properly
            teamOne.setDataToControls(activeMatch);
            teamTwo.setDataToControls(activeMatch);
            // update the score on the score controls
            scoreOne.setDataToControls(activeMatch);
            scoreTwo.setDataToControls(activeMatch);
            // make sure we are not showing the change server thing
            closeChangeServerSnackbar();
        } else {
            // we have a score as well, so this is a change to the score
            scoreOne.displayCurrentScore();
            scoreTwo.displayCurrentScore();
            // do we let them change the server now?
            if (score.isTeamServerChangeAllowed()) {
                // we are allowed to change server here, so let them
                changeServerSnackbar = new ChangeTeamServerSnackbar(getActivity(), activeMatch.getSetup(), activeMatch.getServingTeam(), new ChangeTeamServerSnackbar.Listener() {
                    @Override
                    public void onTeamServerChanged(MatchSetup.Team team, MatchSetup.Player newServer) {
                        updateServerIcon();
                    }
                });
            }
            else {
                closeChangeServerSnackbar();
            }
        }
        // and do the server icons
        updateServerIcon();
    }

    private void updateServerIcon() {
        // show the correct icon on the controls
        teamOne.displayServerIcon();
        teamTwo.displayServerIcon();
        // and animate the icon to the new position
        animateServerIconToServer();
    }

    private void closeChangeServerSnackbar() {
        if (null != changeServerSnackbar) {
            changeServerSnackbar.dismiss();
            changeServerSnackbar = null;
        }
    }

    private void animateServerIconToServer() {
        ImageView target = null;
        ImageView teamTarget = null;
        int colorId = R.color.teamOneColor;
        if (null != activeMatch) {
            switch (activeMatch.getServingPlayer()) {
                case P_ONE:
                    target = teamOnePlayerServe;
                    teamTarget = teamOneServerImage;
                    // and hide the invalid one
                    teamTwoServerImage.setVisibility(View.INVISIBLE);
                    colorId = R.color.teamOneColor;
                    break;
                case PT_ONE:
                    target = teamOnePartnerServe;
                    teamTarget = teamOneServerImage;
                    // and hide the invalid one
                    teamTwoServerImage.setVisibility(View.INVISIBLE);
                    colorId = R.color.teamOneColor;
                    break;
                case P_TWO:
                    target = teamTwoPlayerServe;
                    teamTarget = teamTwoServerImage;
                    // and hide the invalid one
                    teamOneServerImage.setVisibility(View.INVISIBLE);
                    colorId = R.color.teamTwoColor;
                    break;
                case PT_TWO:
                    target = teamTwoPartnerServe;
                    teamTarget = teamTwoServerImage;
                    // and hide the invalid one
                    teamOneServerImage.setVisibility(View.INVISIBLE);
                    colorId = R.color.teamTwoColor;
                    break;
            }
        }
        if (target == null) {
            activeServerImage.setVisibility(View.INVISIBLE);
        }
        else {
            // show the image
            activeServerImage.setVisibility(View.VISIBLE);
            ImageViewCompat.setImageTintList(activeServerImage, ColorStateList.valueOf(getActivity().getColor(colorId)));
            // and animate it into place
            int[] targetLocation = new int[2];
            target.getLocationOnScreen(targetLocation);
            float yDifference = activeServerImage.getY() - targetLocation[1];
            float xDifference = activeServerImage.getX() - targetLocation[0];
            this.activeServerImage
                    .animate()
                    .translationXBy(-xDifference)
                    .translationYBy(-yDifference);
        }

        if (teamTarget == null) {
            activeServerTeamImage.setVisibility(View.INVISIBLE);
        }
        else {
            // show the image
            activeServerTeamImage.setVisibility(View.VISIBLE);
            // and animate it into place
            int[] targetLocation = new int[2];
            teamTarget.getLocationOnScreen(targetLocation);
            float yDifference = activeServerTeamImage.getY() - targetLocation[1];
            float xDifference = activeServerTeamImage.getX() - targetLocation[0];
            this.activeServerTeamImage
                    .animate()
                    .translationXBy(-xDifference)
                    .translationYBy(-yDifference);
        }
    }
}
