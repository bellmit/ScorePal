package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Locale;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.settings.SettingsTennis;
import uk.co.darkerwaters.scorepal.score.base.MatchMessage;
import uk.co.darkerwaters.scorepal.score.MatchSettings;
import uk.co.darkerwaters.scorepal.score.base.Sport;
import uk.co.darkerwaters.scorepal.score.tennis.TennisMatchSettings;
import uk.co.darkerwaters.scorepal.score.tennis.TennisSets;

public class TennisSetupActivity extends SetupTeamActivity<TennisMatchSettings, SettingsTennis> {

    private enum FinalSetTarget {
        K_SIMPLE(4),
        K_IMMEDIATE(6),
        K_EIGHT(8),
        K_TEN(10),
        K_WIMBLEDON(12),
        K_EIGHTEEN(18),
        K_TWENTYFOUR(24);

        static final FinalSetTarget K_DEFAULT = K_IMMEDIATE;

        final int games;
        FinalSetTarget(int games) {
            this.games = games;
        }

        public static FinalSetTarget fromGames(int games) {
            for (FinalSetTarget target : values()) {
                if (games == target.games) {
                    return target;
                }
            }
            return K_DEFAULT;
        }

        public FinalSetTarget next() {
            boolean isFound = false;
            for (FinalSetTarget target : values()) {
                if (isFound) {
                    // this is the next one
                    return target;
                }
                else if (target == this) {
                    isFound = true;
                }
            }
            // overflowed, return the first
            return values()[0];
        }

        @Override
        public String toString() {
            return this.games + " - " + this.games;
        }
    }

    private TextView setsText;
    private ImageButton setsLessButton;
    private ImageButton setsMoreButton;

    private Switch deuceDecidingPointSwitch;
    private Switch tieOnFinalSetSwitch;

    private TextView tieFinalSetTargetTextView;
    private TextView gamesToPlayTextView;
    private View finalSetTieLayout;
    private View gamesToPlayLayout;
    private TextView moreSummaryTextView;

    private ImageButton moreButton;
    private ImageButton resetButton;

    private boolean isMoreShown = false;

    public TennisSetupActivity() {
        super(Sport.TENNIS);
    }

    @Override
    public TennisMatchSettings createSettings() {
        // does it have any settings?
        MatchSettings currentSettings = this.communicator.getCurrentSettings();
        TennisMatchSettings settings;
        if (currentSettings instanceof TennisMatchSettings) {
            // already there are some, use these
            settings = (TennisMatchSettings) currentSettings;
        }
        else {
            // the current settings are null, or not points settings, we have to fix this
            settings = new TennisMatchSettings(this);
            // set these new settings we are using on the communicator
            this.communicator.sendRequest(MatchMessage.SETUP_NEW_MATCH, settings);
        }
        return settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tennis_setup);
        // setup this activity now it is laid out
        setupActivity(R.string.tennis_setup);
    }

    @Override
    protected void setupActivity(int titleStringId) {
        super.setupActivity(titleStringId);

        // get the created settings
        final TennisMatchSettings settings = getMatchSettings();

        this.setsText = findViewById(R.id.setsNumberText);
        this.setsMoreButton = findViewById(R.id.setsMoreImageButton);
        this.setsLessButton = findViewById(R.id.setsLessImageButton);

        this.deuceDecidingPointSwitch = findViewById(R.id.deuceDecidingPointSwitch);
        this.tieOnFinalSetSwitch = findViewById(R.id.tieOnFinalSetSwitch);
        this.tieFinalSetTargetTextView = findViewById(R.id.tieFinalSetTargetTextView);
        this.gamesToPlayTextView = findViewById(R.id.gamesToPlayTextView);
        this.finalSetTieLayout = findViewById(R.id.finalSetTieLayout);
        this.gamesToPlayLayout = findViewById(R.id.gamesToPlayLayout);
        this.moreButton = findViewById(R.id.moreButton);
        this.resetButton = findViewById(R.id.resetButton);
        this.moreSummaryTextView = findViewById(R.id.moreSummaryTextView);

        this.moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMoreLess(settings);
            }
        });
        this.resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetSettings(settings);
            }
        });

        this.setsMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSets(+1, settings);
            }
        });
        this.setsLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSets(-1, settings);
            }
        });
        this.deuceDecidingPointSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                settings.setIsDecidingPointOnDeuce(b);
                setActivityDataShown(settings);
            }
        });
        this.tieOnFinalSetSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    // turn on
                    int target = settings.getFinalSetTieTarget();
                    if (target < 0) {
                        // this isn't ok - we want it on - use the default
                        target = FinalSetTarget.K_DEFAULT.games;
                    }
                    settings.setFinalSetTieTarget(target);
                } else {
                    // turn off
                    settings.setFinalSetTieTarget(-1);
                }
                setActivityDataShown(settings);
            }
        });
        this.tieFinalSetTargetTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int target = settings.getFinalSetTieTarget();
                if (target > 0) {
                    // this is on, cycle it
                    settings.setFinalSetTieTarget(FinalSetTarget.fromGames(target).next().games);
                    setActivityDataShown(settings);
                }
            }
        });
        this.gamesToPlayTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int gamesToPlay = settings.getGamesInSet() + 2;
                if (gamesToPlay > 8) {
                    // start again
                    gamesToPlay = 4;
                }
                settings.setGamesInSet(gamesToPlay);
                setActivityDataShown(settings);
            }
        });
    }

    private void toggleMoreLess(TennisMatchSettings settings) {
        this.isMoreShown = !this.isMoreShown;
        // and show the data
        setActivityDataShown(settings);
    }

    private void resetSettings(TennisMatchSettings settings) {
        this.isMoreShown = false;
        settings.resetSettings();
        // and show the data
        setActivityDataShown(settings);
    }

    @Override
    protected SettingsTennis getAppSettings() {
        return new SettingsTennis(this.application);
    }

    @Override
    public void initialiseSettings(SettingsTennis appSettings, TennisMatchSettings settings) {
        super.initialiseSettings(appSettings, settings);

        // setup all these defaults on our settings object
        settings.setScoreGoal(appSettings.getTennisSets().val);
        settings.setFinalSetTieTarget(appSettings.getFinalSetTieTarget());
        settings.setGamesInSet(appSettings.getGamesInSet());
        settings.setIsDecidingPointOnDeuce(appSettings.getIsDecidingPointOnDeuce());
    }

    @Override
    protected void updateAppSettings(SettingsTennis appSettings, TennisMatchSettings settings) {
        super.updateAppSettings(appSettings, settings);

        // set the defaults back on the application
        appSettings.setTennisSets(TennisSets.fromValue(settings.getScoreGoal()));
        appSettings.setFinalSetTieTarget(settings.getFinalSetTieTarget());
        appSettings.setGamesInSet(settings.getGamesInSet());
        appSettings.setIsDecidingPointOnDeuce(settings.isDecidingPointOnDeuce());
    }

    private void changeSets(int delta, TennisMatchSettings settings) {
        // get the current set
        TennisSets currentSets = TennisSets.fromValue(settings.getScoreGoal());
        // and move it on, setting it on the match
        if (delta > 0) {
            settings.setScoreGoal(currentSets.next().val);
        }
        else {
            settings.setScoreGoal(currentSets.prev().val);
        }
        // and update the screen
        setActivityDataShown(settings);
    }

    @Override
    protected void setActivityDataShown(TennisMatchSettings settings) {
        super.setActivityDataShown(settings);

        switch (TennisSets.fromValue(settings.getScoreGoal())) {
            case ONE:
                this.setsText.setText(R.string.one_sets);
                break;
            case THREE:
                this.setsText.setText(R.string.three_sets);
                break;
            case FIVE:
                this.setsText.setText(R.string.five_sets);
                break;
        }

        if (!this.isMoreShown) {
            this.gamesToPlayLayout.setVisibility(View.GONE);
            this.deuceDecidingPointSwitch.setVisibility(View.GONE);
            this.tieOnFinalSetSwitch.setVisibility(View.GONE);

            BaseActivity.setupButtonIcon(this.moreButton, R.drawable.ic_baseline_keyboard_arrow_right);
        }
        else {
            this.gamesToPlayLayout.setVisibility(View.VISIBLE);
            this.deuceDecidingPointSwitch.setVisibility(View.VISIBLE);
            this.tieOnFinalSetSwitch.setVisibility(View.VISIBLE);

            setupButtonIcon(this.moreButton, R.drawable.ic_baseline_keyboard_arrow_left);
        }

        // set the data on this activity from the settings
        int gamesInSet = settings.getGamesInSet();
        this.gamesToPlayTextView.setText(String.format(Locale.getDefault(), "%d", gamesInSet));
        // set the deuce
        this.deuceDecidingPointSwitch.setChecked(settings.isDecidingPointOnDeuce());
        int target = settings.getFinalSetTieTarget();
        this.tieOnFinalSetSwitch.setChecked(target > 0);
        if (target > 0) {
            // this is set, set the value, has to be more than or equal to the games though
            if (gamesInSet > target) {
                // limit to being the games we are playing, duh
                target = gamesInSet;
                // and put this back into the settings
                settings.setFinalSetTieTarget(target);
            }
            this.tieFinalSetTargetTextView.setText(FinalSetTarget.fromGames(target).toString());
            this.finalSetTieLayout.setVisibility(this.isMoreShown ? View.VISIBLE : View.GONE);
        }
        else {
            // hide all this
            this.tieFinalSetTargetTextView.setText(FinalSetTarget.K_DEFAULT.toString());
            this.finalSetTieLayout.setVisibility(View.GONE);
        }
        // explain this when it is shrunk
        int tieTarget = settings.getFinalSetTieTarget();
        this.moreSummaryTextView.setText(
                String.format(getString(R.string.tennis_settings_summary),
                        settings.getScoreGoal(),
                        settings.getGamesInSet(),
                        settings.isDecidingPointOnDeuce() ? getString(R.string.no_advantage) : "",
                        tieTarget > 0 ? String.format(getString(R.string.tie_in_final_set), tieTarget) : "")
        );
    }
}
