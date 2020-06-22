package uk.co.darkerwaters.scorepal.ui.matchplay;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.Match;
import uk.co.darkerwaters.scorepal.data.MatchSetup;

public class FragmentScoreTeam extends Fragment {

    private View root = null;
    private MatchSetup.Team team = null;

    private ImageView playerServeIcon;
    private ImageView partnerServeIcon;

    private TextView player;
    private TextView playerSingles;
    private TextView partner;

    private Match activeMatch = null;

    public FragmentScoreTeam() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_score_team, container, false);

        playerServeIcon = root.findViewById(R.id.teamPlayerServe);
        partnerServeIcon = root.findViewById(R.id.teamPartnerServe);

        player = root.findViewById(R.id.teamPlayerName);
        playerSingles = root.findViewById(R.id.teamSinglesPlayerName);
        partner = root.findViewById(R.id.teamPartnerName);

        return root;
    }

    public <T extends View> T findViewById(int viewId) {
        return root.findViewById(viewId);
    }

    public void setupControls(MatchSetup.Team team) {
        this.team = team;
        // set the control colours
        this.team = team;
        int color;
        if (team == MatchSetup.Team.T_TWO) {
            color = getActivity().getColor(R.color.teamTwoColor);
        } else {
            color = getActivity().getColor(R.color.teamOneColor);
        }
        // set the control colours
        setTeamColor(color, new ImageView[]{playerServeIcon, partnerServeIcon });
        setTeamColor(color, new TextView[]{player, playerSingles, partner });
    }

    private void setTeamColor(int color, TextView[] textViews) {
        for (TextView view: textViews) {
            view.setTextColor(color);
        }
    }

    private void setTeamColor(int color, ImageView[] imageViews) {
        for (ImageView view: imageViews) {
            ImageViewCompat.setImageTintList(view, ColorStateList.valueOf(color));
        }
    }

    public void setDataToControls(Match match) {
        activeMatch = match;
        if (null != match) {
            MatchSetup setup = activeMatch.getSetup();
            String playerName = setup.getPlayerName(setup.getTeamPlayer(team));
            player.setText(playerName);
            playerSingles.setText(playerName);
            partner.setText(setup.getPlayerName(setup.getTeamPartner(team)));
            // set the visibillity of the controls in doubles
            int vis;
            if (setup.getType() == MatchSetup.MatchType.SINGLES) {
                vis = View.INVISIBLE;
                // but show the singles one
                playerSingles.setVisibility(View.VISIBLE);
            } else {
                vis = View.VISIBLE;
                // and hide singles
                playerSingles.setVisibility(View.INVISIBLE);
            }
            // and show / hide the others as a set
            player.setVisibility(vis);
            partner.setVisibility(vis);
            partnerServeIcon.setVisibility(vis);

            // also set the serve icon properly
            displayServerIcon();
        }
    }
    public void displayServerIcon() {
        if (null != activeMatch) {
            MatchSetup setup = activeMatch.getSetup();
            if (activeMatch.getServingTeam() == team) {
                // we are serving, show the server only
                if (activeMatch.getServingPlayer() == setup.getTeamPlayer(team)) {
                    // the 'player' is serving
                    playerServeIcon.setVisibility(View.VISIBLE);
                    partnerServeIcon.setVisibility(View.INVISIBLE);
                } else {
                    // 'partner' is serving
                    playerServeIcon.setVisibility(View.INVISIBLE);
                    partnerServeIcon.setVisibility(View.VISIBLE);
                }
            } else {
                // the other team is serving
                playerServeIcon.setVisibility(View.INVISIBLE);
                partnerServeIcon.setVisibility(View.INVISIBLE);
            }
        }
    }
}
