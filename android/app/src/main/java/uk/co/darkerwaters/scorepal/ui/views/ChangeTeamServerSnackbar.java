package uk.co.darkerwaters.scorepal.ui.views;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.MatchSetup;

public class ChangeTeamServerSnackbar {

    public interface Listener {
        void onTeamServerChanged(MatchSetup.Team team, MatchSetup.Player newServer);
    }

    private final Snackbar snackbar;

    private final TextView teamTitle;
    private final EditText playerText;
    private final EditText partnerText;
    private final RadioIndicatorButton playerServeButton;
    private final RadioIndicatorButton partnerServeButton;
    private final ImageButton closeButton;

    public ChangeTeamServerSnackbar(Activity context, final MatchSetup setup, final MatchSetup.Team team, final Listener listener) {
        // Create the Snackbar
        LinearLayout.LayoutParams objLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        snackbar = Snackbar.make(context.findViewById(android.R.id.content), "", Snackbar.LENGTH_INDEFINITE);

        // Get the Snackbar layout view
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();

        // Set snackbar layout params
        int navbarHeight = CustomSnackbar.getNavBarHeight(context);
        FrameLayout.LayoutParams parentParams = (FrameLayout.LayoutParams) layout.getLayoutParams();
        parentParams.setMargins(0, 0, 0, 0 - navbarHeight + 50);
        layout.setLayoutParams(parentParams);
        layout.setPadding(0, 0, 0, 0);
        layout.setLayoutParams(parentParams);

        // Inflate our custom view
        View snackView = context.getLayoutInflater().inflate(R.layout.layout_change_team_snackbar, null);

        // Configure our custom view
        teamTitle = snackView.findViewById(R.id.teamTitle);
        playerText = snackView.findViewById(R.id.teamPlayerName);
        partnerText = snackView.findViewById(R.id.teamPartnerName);

        playerServeButton = snackView.findViewById(R.id.playerServeButton);
        partnerServeButton = snackView.findViewById(R.id.partnerServeButton);

        teamTitle.setText(setup.getTeamName(context, team));
        playerText.setText(setup.getPlayerName(setup.getTeamPlayer(team)));
        partnerText.setText(setup.getPlayerName(setup.getTeamPartner(team)));

        if (setup.getFirstServingPlayer(team) == setup.getTeamPlayer(team)) {
            // the player is serving
            playerServeButton.setChecked(true);
        }
        else {
            // the partner is serving
            partnerServeButton.setChecked(true);
        }

        playerServeButton.addOnCheckChangeListener(new CheckableIndicatorButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                MatchSetup.Player newServer = isChecked ? setup.getTeamPlayer(team) : setup.getTeamPartner(team);
                // set this on the setup
                setup.setFirstTeamServer(newServer);
                if (null != listener) {
                    // inform the caller of this change
                    listener.onTeamServerChanged(team, newServer);
                }
            }
        });
        // done in the other one
        /*
        partnerServeButton.addOnCheckChangeListener(new CheckableIndicatorButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                setup.setFirstTeamServer(isChecked ? setup.getTeamPartner(team) : setup.getTeamPlayer(team));
            }
        });*/

        closeButton = snackView.findViewById(R.id.imageButtonClose);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        // Add our custom view to the Snackbar's layout
        layout.addView(snackView, objLayoutParams);
        // Show the Snackbar
        snackbar.show();
    }

    public void dismiss() {
        snackbar.dismiss();
    }
}
