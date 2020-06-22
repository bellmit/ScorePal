package uk.co.darkerwaters.scorepal.activities.fragments;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.GamePlayCommunicator;
import uk.co.darkerwaters.scorepal.settings.SettingsMatch;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.MatchSettings;

public class FragmentTeam extends Fragment {

    private static final long K_ANIMATION_DURATION = 1000;
    private static final String K_TEXTREGEX = "[^a-zA-Z0-9., ]";

    public enum TeamNamingMode {
        SURNAME_INITIAL,
        FIRST_NAME,
        LAST_NAME,
        FULL_NAME;

        TeamNamingMode next() {
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

    private TextView title;
    private ImageView titleModeButton;
    private CardView teamCard;
    private AutoCompleteTextView playerName;
    private AutoCompleteTextView partnerName;
    private ImageView playerClear;
    private ImageView partnerClear;
    private ViewGroup partnerNameLayout;
    private ArrayAdapter adapter;

    private SettingsMatch appSettings = null;

    private float cardHeight;
    private boolean currentlyDoubles;
    private float animationAmount;
    private int animationAttempts;

    private TeamNamingMode currentMode = TeamNamingMode.SURNAME_INITIAL;

    private ObjectAnimator animation = null;

    public interface FragmentTeamInteractionListener {
        // the listener to set the data from this fragment
        void onAttachFragment(FragmentTeam fragmentTeam);
        void onAnimationUpdated(Float value);
        void onTeamNameChanged(FragmentTeam fragmentTeam);
    }
    
    private FragmentTeamInteractionListener listener;

    private int teamNumber = 0;

    public FragmentTeam() {
        // Required empty public constructor
    }

    public void setAppSettings(SettingsMatch appSettings) {
        this.appSettings = appSettings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View parent = inflater.inflate(R.layout.fragment_team, container, false);

        // reset all our members
        this.cardHeight = 0f;
        this.currentlyDoubles = true;
        this.animationAmount = 0f;
        this.animationAttempts = 0;

        this.title = parent.findViewById(R.id.titleText);
        this.titleModeButton = parent.findViewById(R.id.teamTitleModeButton);
        this.playerName = parent.findViewById(R.id.playerAutoTextView);
        this.partnerName = parent.findViewById(R.id.playerPartnerAutoTextView);
        this.partnerNameLayout = parent.findViewById(R.id.partnerNameLayout);
        this.teamCard = parent.findViewById(R.id.team_card);
        this.playerClear = parent.findViewById(R.id.playerDeleteNameButton);
        this.partnerClear = parent.findViewById(R.id.playerPartnerDeleteNameButton);

        // set the current mode to be the last one they selected
        if (null != this.appSettings) {
            this.currentMode = this.appSettings.getCurrentTeamNameMode();
        }
        else {
            this.currentMode = TeamNamingMode.SURNAME_INITIAL;
        }

        // listen for changes in each name to construct the title from them
        //this.playerName.setOnItemSelectedListener(createSelectedItemListener());
        //this.partnerName.setOnItemSelectedListener(createSelectedItemListener());
        this.playerName.addTextChangedListener(createTextChangeListener());
        this.partnerName.addTextChangedListener(createTextChangeListener());

        this.titleModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change the mode
                setTeamNameMode(currentMode.next());
            }
        });

        this.playerClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerName.setText("");
            }
        });
        this.partnerClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                partnerName.setText("");
            }
        });

        // set our labels
        setLabels(this.teamNumber);
        // setup our adapters here
        setupAdapters();
        return parent;
    }

    private TextWatcher createTextChangeListener() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // nothing
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // update the team name
                createTeamName();
            }
            @Override
            public void afterTextChanged(Editable editable) {
                // nothing
            }
        };
    }

    private AdapterView.OnItemSelectedListener createSelectedItemListener() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // user selected a contact to use as a name, setup the name
                createTeamName();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // nothing selected, default team name will be chosen
                createTeamName();
            }
        };
    }

    public TeamNamingMode getTeamNameMode() {
        return this.currentMode;
    }

    public void setTeamNameMode(TeamNamingMode mode) {
        // only change if changing as will send a modification message
        if (mode != this.currentMode) {
            this.currentMode = mode;
            if (null != this.appSettings) {
                // set this back to the application settings
                this.appSettings.setCurrentTeamNameMode(this.currentMode);
            }
            // update the team name
            createTeamName();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentTeamInteractionListener) {
            listener = (FragmentTeamInteractionListener) context;
            // and inform this listener of our attachment
            listener.onAttachFragment(this);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentTeamInteractionListener");
        }
    }

    public void setAutoCompleteAdapter(ArrayAdapter adapter) {
        this.adapter = adapter;
        // setup our adapters here
        setupAdapters();
    }

    private void animatePartnerName(final float animValue) {
        if (this.cardHeight <= 0f) {
            this.cardHeight = this.teamCard.getHeight();
        }
        if (null != this.animation) {
            this.animation.end();
        }
        this.animation = ObjectAnimator.ofFloat(this.partnerNameLayout, "translationY", animValue);
        this.animation.setDuration(K_ANIMATION_DURATION);
        this.animation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                partnerNameLayout.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animator animator) {
                if (animValue < 0f) {
                    partnerNameLayout.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onAnimationCancel(Animator animator) {
            }
            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        this.animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // get the value the interpolator is a
                Float value = (Float) animation.getAnimatedValue();
                partnerName.setAlpha(1 - (value / animationAmount));
                // I'm going to set the layout's height 1:1 to the tick
                teamCard.getLayoutParams().height = (int)(cardHeight + value);
                teamCard.requestLayout();
                // and inform the listener of this change
                if (null != FragmentTeam.this.listener) {
                    FragmentTeam.this.listener.onAnimationUpdated(value);
                }
            }
        });
        this.animation.start();
    }

    public void setIsDoubles(final boolean isDoubles, final boolean instantChange) {
        if (instantChange) {
            // just hide / show the controls
            this.partnerNameLayout.setVisibility(isDoubles ? View.VISIBLE : View.GONE);
        }
        else {
            if (this.teamCard.getHeight() <= 0f) {
                // there is no height - do not animate this change yet - come back later once we have a height
                if (++this.animationAttempts < 5) {
                    new Handler(getContext().getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setIsDoubles(isDoubles, instantChange);
                        }
                    }, 200);
                    // I don't want to try forever just in case we stick in an infinite loop
                }
                return;
            }
            else {
                // we are successful in having a height and will proceed to animate
                this.animationAttempts = 0;
            }
            // do the animation
            if (isDoubles) {
                if (!this.currentlyDoubles) {
                    // are not currently doubles, put the partner name back in place
                    animatePartnerName(0f);
                }
            } else if (this.currentlyDoubles) {
                // are currently doubles, put the partner name up under the player name
                this.animationAmount = this.partnerNameLayout.getY() - this.playerName.getY();
                //animate the right amount (shrinking the parent will adjust this)
                this.animationAmount *= -0.7f;
                animatePartnerName(this.animationAmount);
            }
        }
        // remember where we are
        this.currentlyDoubles = isDoubles;
        // and update the team name
        createTeamName();
    }

    private void setupAdapters() {
        if (null != this.playerName) {
            this.playerName.setAdapter(this.adapter);
        }
        if (null != this.partnerName) {
            this.partnerName.setAdapter(this.adapter);
        }
    }

    @Override
    public void onPause() {
        // these are the new defaults to use the next time we come here
        if (null != this.appSettings) {
            this.appSettings.setPlayerName(getPlayerName(), this.teamNumber - 1, 0);
            this.appSettings.setPlayerName(getPlayerPartnerName(), this.teamNumber - 1, 1);
        }
        // and pause
        super.onPause();
    }

    @Override
    public void onDetach() {
        // detach the fragment
        super.onDetach();
        listener = null;
    }

    private void createTeamName() {
        // sort out what we are doing with our names, by default in doubles
        // we are a team, in singles we are player one
        String teamName = "";
        // combine the name in the correct chosen way
        switch (this.currentMode) {
            case SURNAME_INITIAL:
                teamName = createSurnameTeamName();
                break;
            case FIRST_NAME:
                teamName = createFirstNameTeamName();
                break;
            case LAST_NAME:
                teamName = createLastNameTeamName();
                break;
            case FULL_NAME:
                teamName = createFullNameTeamName();
                break;
        }
        if (null == teamName || teamName.isEmpty()) {
            // there is no team name, use the default
            switch (this.teamNumber) {
                case 1:
                    if (currentlyDoubles) {
                        teamName = getContext().getString(R.string.team_one_title);
                    }
                    else {
                        teamName = getContext().getString(R.string.default_playerOneName);
                    }
                    break;
                case 2:
                    if (currentlyDoubles) {
                        teamName = getContext().getString(R.string.team_two_title);
                    }
                    else {
                        teamName = getContext().getString(R.string.default_playerTwoName);
                    }
                    break;
            }
        }
        // set the title
        this.title.setText(teamName);
        // inform the listener of this
        if (null != this.listener) {
            this.listener.onTeamNameChanged(this);
        }
    }

    private String createSurnameTeamName() {
        if (this.currentlyDoubles) {
            return combineTwoNames(splitSurname(getPlayerName()), splitSurname(getPlayerPartnerName()));
        }
        else {
            return splitSurname(getPlayerName());
        }
    }

    private String createFirstNameTeamName() {
        if (this.currentlyDoubles) {
            return combineTwoNames(splitFirstName(getPlayerName()), splitFirstName(getPlayerPartnerName()));
        }
        else {
            return splitFirstName(getPlayerName());
        }
    }

    private String createLastNameTeamName() {
        if (this.currentlyDoubles) {
            return combineTwoNames(splitLastName(getPlayerName()), splitLastName(getPlayerPartnerName()));
        }
        else {
            return splitLastName(getPlayerName());
        }
    }

    private String createFullNameTeamName() {
        if (this.currentlyDoubles) {
            return combineTwoNames(getPlayerName(), getPlayerPartnerName());
        }
        else {
            return getPlayerName();
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
            builder.append(Team.K_NAME_SEPARATOR);
            builder.append(name2);
            // return this string
            return builder.toString();
        }
    }

    public String getTeamName() {
        return this.title.getText().toString();
    }

    public String getPlayerName() {
        String name = this.playerName.getText().toString().replaceAll(K_TEXTREGEX, "");
        if (name.isEmpty()) {
            // get the default (the hint)
            name = this.playerName.getHint().toString();
        }
        return name;
    }

    public String getPlayerPartnerName() {
        String name = this.partnerName.getText().toString().replaceAll(K_TEXTREGEX, "");
        if (name.isEmpty()) {
            // get the default (the hint)
            name = this.partnerName.getHint().toString();
        }
        return name;
    }

    public void setLabels(int teamNumber) {
        this.teamNumber = teamNumber;

        // set the names and hints to use
        if (null != this.title) {
            int playerHint = 0;
            int partnerHint = 0;
            int colorResId = 0;
            switch (this.teamNumber) {
                case 1:
                    colorResId = R.color.teamOneColor;
                    playerHint = R.string.default_playerOneName;
                    partnerHint = R.string.default_playerOnePartnerName;
                    break;
                case 2:
                    colorResId = R.color.teamTwoColor;
                    playerHint = R.string.default_playerTwoName;
                    partnerHint = R.string.default_playerTwoPartnerName;
                    break;
            }
            // set the hints
            this.playerName.setHint(playerHint);
            this.partnerName.setHint(partnerHint);
            // set the colours
            int color = getContext().getColor(colorResId);
            this.title.setTextColor(color);
            this.playerName.setTextColor(color);
            this.partnerName.setTextColor(color);
            // and do the team title
            createTeamName();

            String nameOne, nameTwo;
            GamePlayCommunicator communicator = GamePlayCommunicator.GetActiveCommunicator();
            MatchSettings currentSettings = null;
            if (null != communicator) {
                currentSettings = communicator.getCurrentSettings();
            }
            if (null == currentSettings) {
                // there are no settings, get the defaults from the application settings
                nameOne = getPlayerName();
                nameTwo = getPlayerPartnerName();
                if (null != this.appSettings) {
                    // we have app settings, use the names from here
                    nameOne = this.appSettings.getPlayerName(teamNumber - 1, 0, nameOne);
                    nameTwo = this.appSettings.getPlayerName(teamNumber - 1, 1, nameTwo);
                }
            }
            else {
                // there are settings, use their names
                if (teamNumber == 1) {
                    // there are settings, get the names from team one on these
                    nameOne = currentSettings.getTeamOne().getPlayer(0).getName();
                    nameTwo = currentSettings.getTeamOne().getPlayer(1).getName();
                } else /*if (teamNumber == 2)*/ {
                    // get the names from team two
                    nameOne = currentSettings.getTeamTwo().getPlayer(0).getName();
                    nameTwo = currentSettings.getTeamTwo().getPlayer(1).getName();
                }

                if (null != this.appSettings) {
                    if (null == nameOne || nameOne.isEmpty()) {
                        // there is no name in the settings, get the player name from the app settings
                        nameOne = this.appSettings.getPlayerName(teamNumber - 1, 0, nameOne);
                    }
                    if (null == nameTwo || nameTwo.isEmpty()) {
                        // there is no name in the settings, get the player name from the app settings
                        nameTwo = this.appSettings.getPlayerName(teamNumber - 1, 1, nameTwo);
                    }
                }
            }
            // and set this on the controls
            this.playerName.setText(nameOne);
            this.partnerName.setText(nameTwo);
        }
    }
}
