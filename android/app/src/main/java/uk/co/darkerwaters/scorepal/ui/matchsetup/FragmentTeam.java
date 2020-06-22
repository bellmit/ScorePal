package uk.co.darkerwaters.scorepal.ui.matchsetup;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.MatchSetup;

public class FragmentTeam extends Fragment {
    private ImageView teamImage;
    private TextView teamTitle;
    private AutoCompleteTextView teamPlayerName;
    private AutoCompleteTextView teamPartnerName;
    
    private MatchSetup matchSetup;
    private MatchSetup.Player player = MatchSetup.Player.P_ONE;
    private MatchSetup.Player partner = MatchSetup.Player.PT_ONE;
    private MatchSetup.Team team = MatchSetup.Team.T_ONE;

    public FragmentTeam() {
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
        View root = inflater.inflate(R.layout.fragment_team, container, false);

        teamImage = root.findViewById(R.id.teamImage);
        teamTitle = root.findViewById(R.id.teamTitle);
        teamPlayerName = root.findViewById(R.id.teamPlayerName);
        teamPartnerName = root.findViewById(R.id.teamPartnerName);

        // listen for changes to the player's names to set the new names into the setup
        listenForChanges(teamPlayerName, true);
        listenForChanges(teamPartnerName, false);

        return root;
    }

    private void listenForChanges(EditText nameEdit, final boolean isPlayer) {
        nameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // set the new name
                if (null != matchSetup) {
                    matchSetup.setPlayerName(charSequence.toString(), isPlayer ? player : partner);
                }
                // and refresh the team titles
                showTeamName();
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    public void setDataToControls(MatchSetup matchSetup, MatchSetup.Team team) {
        this.matchSetup = matchSetup;
        this.team = team;
        switch (team) {
            case T_ONE:
                player = MatchSetup.Player.P_ONE;
                partner = MatchSetup.Player.PT_ONE;
                break;
            case T_TWO:
                player = MatchSetup.Player.P_TWO;
                partner = MatchSetup.Player.PT_TWO;
                break;
        }
        // show or hide the partner name correctly
        showHideDoublesControls();
        // and setup all the names
        showPlayerName(teamPlayerName, player);
        showPlayerName(teamPartnerName, partner);

        // and the team's names
        showTeamName();
    }

    private void showPlayerName(EditText editText, MatchSetup.Player player) {
        if (null != matchSetup) {
            String name = this.matchSetup.getPlayerName(player);
            if (null != name) {
                editText.setText(name);
            }
            // also set the hint to be the default (depends on the team they are on)
            editText.setHint(this.matchSetup.getNamer().defaultPlayerName(getContext(), player));
        }
    }

    public void showTeamName() {
        // use the namer to get a nice default if things are null in the settings
        if (null != matchSetup) {
            teamTitle.setText(this.matchSetup.getNamer().getTeamName(getContext(), team));
        }
    }

    public void showHideDoublesControls() {
        if (null != matchSetup) {
            // hide the partners if we are playing singles
            if (this.matchSetup.getType() == MatchSetup.MatchType.SINGLES) {
                teamPartnerName.setVisibility(View.INVISIBLE);
                // setup a nice image
                teamImage.setImageResource( team == matchSetup.getFirstServingTeam()
                        ? R.drawable.ic_player_serving_black_24dp
                        : R.drawable.ic_player_receiving_backhand_black_24dp);
            }
            else {
                teamPartnerName.setVisibility(View.VISIBLE);
                // setup a nice image
                teamImage.setImageResource( team == matchSetup.getFirstServingTeam()
                        ? R.drawable.ic_player_serving_doubles_black_24dp
                        : R.drawable.ic_player_receiving_doubles_black_24dp);
            }
        }
    }

    public void setAutoCompleteAdapter(ArrayAdapter adapter) {
        teamPlayerName.setAdapter(adapter);
        teamPartnerName.setAdapter(adapter);
    }
}
