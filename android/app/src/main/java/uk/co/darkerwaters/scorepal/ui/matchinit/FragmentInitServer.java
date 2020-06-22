package uk.co.darkerwaters.scorepal.ui.matchinit;

import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Random;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.dataui.TeamNamer;
import uk.co.darkerwaters.scorepal.points.Sport;

public class FragmentInitServer<TSetup extends MatchSetup> extends FragmentMatchInit<TSetup> {
    
    private View serverImage;

    private Button changeNamingButton;
    
    private TextView teamOneTitle;
    private EditText teamOnePlayerName;
    private EditText teamOnePartnerName;

    private TextView teamTwoTitle;
    private EditText teamTwoPlayerName;
    private EditText teamTwoPartnerName;

    private MatchSetup.Team servingTeam = null;

    private final Random randomiser = new Random();
    private final Handler handler = new Handler();

    public FragmentInitServer(Sport sport, int fragmentId) {
        super(sport, fragmentId);
    }

    @Override
    protected void setupControls(View root) {

        serverImage = root.findViewById(R.id.serverImage);

        teamOneTitle = root.findViewById(R.id.teamOneTitle);
        teamOnePlayerName = root.findViewById(R.id.teamOnePlayerName);
        teamOnePartnerName = root.findViewById(R.id.teamOnePartnerName);

        teamTwoTitle = root.findViewById(R.id.teamTwoTitle);
        teamTwoPlayerName = root.findViewById(R.id.teamTwoPlayerName);
        teamTwoPartnerName = root.findViewById(R.id.teamTwoPartnerName);

        changeNamingButton = root.findViewById(R.id.cycleNamingButton);
        // if we are choosing the server for the match - choose the naming scheme too
        changeNamingButton.setVisibility(servingTeam == null ? View.VISIBLE : View.INVISIBLE);
        changeNamingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cycleNamingScheme();
            }
        });

        root.findViewById(R.id.cycleServerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cycleNextServer(500);
            }
        });
        root.findViewById(R.id.randomServerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                randomServer();
            }
        });
    }

    private void cycleNamingScheme() {
        // change the namer into the next mode
        TeamNamer namer = matchSetup.getNamer();
        namer.setTeamNameMode(namer.getTeamNameMode().next());
        // and update our team names with this new namer in place
        showTeamNames();
    }

    private void randomServer() {
        // get the server at random please
        int newPlayerIndex = 1 + randomiser.nextInt(
                matchSetup.getType() == MatchSetup.MatchType.DOUBLES && null == servingTeam ? 4 : 2);
        // this is  number from 1 - 4 in doubles (when selecting starting match server)
        // or 1 - 2 in singles or when choosing a team starter
        // so someone at random, including the current one
        for (int i = 0; i < newPlayerIndex; ++i) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    cycleNextServer(200);
                }
            }, i * 250);
        }
    }

    private void cycleNextServer(int duration) {
        MatchSetup.Player server = this.matchSetup.getFirstServingPlayer(this.matchSetup.getFirstServingTeam());
        int newIndex;
        if (null == servingTeam) {
            // we are choosing the server for the whole match, find the correct one in everyone
            if (matchSetup.getType() == MatchSetup.MatchType.DOUBLES) {
                // we are in doubles, cycle through the list
                newIndex = server.index + 1;
                if (newIndex >= MatchSetup.Player.values().length) {
                    // over the end
                    newIndex = 0;
                }
                server = MatchSetup.Player.values()[newIndex];
            } else {
                // just swap between players one and two
                if (server == MatchSetup.Player.P_ONE) {
                    server = MatchSetup.Player.P_TWO;
                } else {
                    server = MatchSetup.Player.P_ONE;
                }
            }
        }
        else {
            // we have to let them choose a server for the team that is about to serve (in doubles)
            switch (servingTeam) {
                case T_ONE:
                    // team one is serving
                    if (server == MatchSetup.Player.P_ONE) {
                        server = MatchSetup.Player.PT_ONE;
                    } else {
                        server = MatchSetup.Player.P_ONE;
                    }
                    break;
                case T_TWO:
                    // team two is serving
                    if (server == MatchSetup.Player.P_TWO) {
                        server = MatchSetup.Player.PT_TWO;
                    } else {
                        server = MatchSetup.Player.P_TWO;
                    }
                    break;
            }
        }
        // set this server's team to be the serving team
        this.matchSetup.setFirstServingTeam(this.matchSetup.getPlayerTeam(server));
        this.matchSetup.setFirstTeamServer(server);
        // scroll the icon to this player
        scrollServingIcon(getPlayerControl(server), duration);
    }

    public void setMatchSetup(TSetup setup) {
        super.setMatchSetup(setup);
        // show or hide the partner name correctly
        showHideDoublesControls();
        // and setup all the names
        showPlayerName(teamOnePlayerName, MatchSetup.Player.P_ONE);
        showPlayerName(teamOnePartnerName, MatchSetup.Player.PT_ONE);
        showPlayerName(teamTwoPlayerName, MatchSetup.Player.P_TWO);
        showPlayerName(teamTwoPartnerName, MatchSetup.Player.PT_TWO);

        // and the team's names
        showTeamNames();

        // ensuring the server is correct
        matchSetup.correctPlayerErrors();

        // and scroll the serving icon to the correct location in a sec - when shown and positioned
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollToServer(250);
            }
        }, 750);
    }

    private void scrollToServer(int duration) {
        // show who is serving first now
        MatchSetup.Player server = matchSetup.getFirstServingPlayer(matchSetup.getFirstServingTeam());
        // scroll the icon to this player
        scrollServingIcon(getPlayerControl(server), duration);
    }

    private void scrollServingIcon(EditText targetControl, int duration) {
        float yDifference = this.serverImage.getY() - targetControl.getY();
        float xDifference = this.serverImage.getX() - teamOneTitle.getX();
        this.serverImage.setVisibility(View.VISIBLE);
        this.serverImage
                .animate()
                .translationXBy(-xDifference)
                .translationYBy(-yDifference)
                .setDuration(duration);
    }

    private EditText getPlayerControl(MatchSetup.Player player) {
        switch (player) {
            case P_ONE:
                return teamOnePlayerName;
            case PT_ONE:
                return teamOnePartnerName;
            case P_TWO:
                return teamTwoPlayerName;
            case PT_TWO:
                return teamTwoPartnerName;
        }
        return null;
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

    public void showTeamNames() {
        // use the namer to get a nice default if things are null in the settings
        if (null != matchSetup) {
            teamOneTitle.setText(this.matchSetup.getNamer().getTeamName(getContext(), MatchSetup.Team.T_ONE));
            teamTwoTitle.setText(this.matchSetup.getNamer().getTeamName(getContext(), MatchSetup.Team.T_TWO));
        }
    }

    public void showHideDoublesControls() {
        if (null != matchSetup) {
            // hide the partners if we are playing singles
            if (this.matchSetup.getType() == MatchSetup.MatchType.SINGLES) {
                teamOnePartnerName.setVisibility(View.INVISIBLE);
                teamTwoPartnerName.setVisibility(View.INVISIBLE);
            }
            else {
                teamOnePartnerName.setVisibility(View.VISIBLE);
                teamTwoPartnerName.setVisibility(View.VISIBLE);
            }
            if (null != servingTeam) {
                // just show the serving team's data
                switch (servingTeam) {
                    case T_ONE:
                        teamTwoTitle.setVisibility(View.GONE);
                        teamTwoPlayerName.setVisibility(View.GONE);
                        teamTwoPartnerName.setVisibility(View.GONE);
                        break;
                    case T_TWO:
                        teamOneTitle.setVisibility(View.GONE);
                        teamOnePlayerName.setVisibility(View.GONE);
                        teamOnePartnerName.setVisibility(View.GONE);
                        break;
                }
            }
        }
    }
}
