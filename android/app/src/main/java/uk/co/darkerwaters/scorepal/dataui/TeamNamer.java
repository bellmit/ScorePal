package uk.co.darkerwaters.scorepal.dataui;

import android.content.Context;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.ApplicationState;
import uk.co.darkerwaters.scorepal.data.MatchSetup;

public class TeamNamer {

    public static final String TEAM_SEP = " -- ";

    public enum TeamNamingMode {
        SURNAME_INITIAL,
        FIRST_NAME,
        LAST_NAME,
        FULL_NAME;

        public TeamNamingMode next() {
            TeamNamingMode[] modes = values();
            boolean foundThis = false;
            for (TeamNamingMode mode : modes) {
                if (foundThis) {
                    // this is the next one
                    return mode;
                } else if (mode == this) {
                    foundThis = true;
                }
            }
            // if here then overflowed the list
            return SURNAME_INITIAL;
        }
    }

    private final MatchSetup setup;

    private TeamNamingMode currentMode = TeamNamingMode.SURNAME_INITIAL;

    public TeamNamer(MatchSetup matchSetup) {
        this.setup = matchSetup;
        currentMode = ApplicationState.Instance().getPreferences().getNamingStyle();
    }

    public TeamNamingMode getTeamNameMode() {
        return this.currentMode;
    }

    public void setTeamNameMode(TeamNamingMode mode) {
        // only change if changing as will send a modification message
        if (mode != this.currentMode) {
            this.currentMode = mode;
            // and store this in our preferences
            ApplicationState.Instance().getPreferences().setNamingStyle(currentMode);
        }
    }

    public String defaultPlayerName(Context context, MatchSetup.Player player) {
        // return the default team name
        return context == null ? "" : context.getString(player.stringRes);
    }

    public String getPlayerName(Context context, MatchSetup.Player player) {
        String name = this.setup.getPlayerName(player);
        if (null == name || name.isEmpty()) {
            name = defaultPlayerName(context, player);
        }
        return name;
    }

    public String getTeamName(Context context, MatchSetup.Team team) {
        // sort out what we are doing with our names, by default in doubles
        // we are a team, in singles we are player one
        String teamName = "";
        // combine the name in the correct chosen way
        switch (this.currentMode) {
            case SURNAME_INITIAL:
                teamName = createSurnameTeamName(context, team);
                break;
            case FIRST_NAME:
                teamName = createFirstNameTeamName(context, team);
                break;
            case LAST_NAME:
                teamName = createLastNameTeamName(context, team);
                break;
            case FULL_NAME:
                teamName = createFullNameTeamName(context, team);
                break;
        }
        if (null == teamName || teamName.isEmpty()) {
            MatchSetup.MatchType type = setup.getType();
            switch (team) {
                case T_ONE:
                    teamName = getPlayerName(context, MatchSetup.Player.P_ONE) + (type == MatchSetup.MatchType.SINGLES ? "" : TEAM_SEP + getPlayerName(context, MatchSetup.Player.PT_ONE));
                    break;
                case T_TWO:
                    teamName = getPlayerName(context, MatchSetup.Player.P_TWO) + (type == MatchSetup.MatchType.SINGLES ? "" : TEAM_SEP + getPlayerName(context, MatchSetup.Player.PT_TWO));
                    break;
                // no default please
            }
            // use the default
        }
        return teamName;
    }

    private String createSurnameTeamName(Context context, MatchSetup.Team team) {
        if (this.setup.getType() == MatchSetup.MatchType.DOUBLES) {
            return combineTwoNames(
                    splitSurname(getPlayerName(context, setup.getTeamPlayer(team))),
                    splitSurname(getPlayerName(context, setup.getTeamPartner(team))));
        }
        else {
            return splitSurname(
                    getPlayerName(context, setup.getTeamPlayer(team)));
        }
    }

    private String createFirstNameTeamName(Context context, MatchSetup.Team team) {
        if (this.setup.getType() == MatchSetup.MatchType.DOUBLES) {
            return combineTwoNames(
                    splitFirstName(getPlayerName(context, setup.getTeamPlayer(team))),
                    splitFirstName(getPlayerName(context, setup.getTeamPartner(team))));
        }
        else {
            return splitFirstName(getPlayerName(context, setup.getTeamPlayer(team)));
        }
    }

    private String createLastNameTeamName(Context context, MatchSetup.Team team) {
        if (this.setup.getType() == MatchSetup.MatchType.DOUBLES) {
            return combineTwoNames(
                    splitLastName(getPlayerName(context, setup.getTeamPlayer(team))),
                    splitLastName(getPlayerName(context, setup.getTeamPartner(team))));
        }
        else {
            return splitLastName(getPlayerName(context, setup.getTeamPlayer(team)));
        }
    }

    private String createFullNameTeamName(Context context, MatchSetup.Team team) {
        if (this.setup.getType() == MatchSetup.MatchType.DOUBLES) {
            return combineTwoNames(
                    getPlayerName(context, setup.getTeamPlayer(team)),
                    getPlayerName(context, setup.getTeamPartner(team)));
        }
        else {
            return getPlayerName(context, setup.getTeamPlayer(team));
        }
    }

    private String splitFirstName(String fullName) {
        String[] parts = fullName.split(" ");
        if (parts.length <= 1) {
            // no good
            return fullName;
        }
        else {
            // there are a number of parts, just use the first name
            return parts[0];
        }
    }

    private String splitLastName(String fullName) {
        String[] parts = fullName.split(" ");
        if (parts.length <= 1) {
            // no good
            return fullName;
        }
        else {
            // there are a number of parts, just use the last name
            return parts[parts.length - 1];
        }
    }

    private String splitSurname(String fullName) {
        String[] parts = fullName.split(" ");
        if (parts.length <= 1) {
            // no good
            return fullName;
        }
        else {
            // there are a number of parts, get all the initials
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < parts.length - 1; ++i) {
                if (false == parts[i].isEmpty()) {
                    // just append the first initial
                    builder.append(parts[i].charAt(0));
                    // append a dot after it
                    builder.append('.');
                }
            }
            // after the initials, we want a space
            builder.append(' ');
            // and finally the surname
            builder.append(parts[parts.length - 1]);
            // and return the string
            return builder.toString();
        }
    }

    private String combineTwoNames(String name1, String name2) {
        if (null == name1 || name1.isEmpty()) {
            // need to just use name 2
            return name2;
        }
        else if (null == name2 || name2.isEmpty()) {
            // need to just use name 1
            return name1;
        }
        else {
            // combine the two strings with a nice separator
            StringBuilder builder = new StringBuilder();
            builder.append(name1);
            builder.append(TEAM_SEP);
            builder.append(name2);
            // return this string
            return builder.toString();
        }
    }
}
