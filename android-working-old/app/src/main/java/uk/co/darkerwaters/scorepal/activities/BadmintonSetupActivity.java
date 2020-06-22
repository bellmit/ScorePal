package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Locale;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.score.MatchSettings;
import uk.co.darkerwaters.scorepal.score.base.MatchMessage;
import uk.co.darkerwaters.scorepal.score.base.Sport;
import uk.co.darkerwaters.scorepal.score.badminton.BadmintonMatchSettings;
import uk.co.darkerwaters.scorepal.settings.SettingsBadminton;

public class BadmintonSetupActivity extends SetupTeamActivity<BadmintonMatchSettings, SettingsBadminton> {

    private TextView badmintonTextTitle;
    private ImageButton badmintonLessButton;
    private ImageButton badmintonMoreButton;

    private TextView badmintonGameTargetTextView;
    private Switch decidingPointSwitch;
    private TextView decidingPointTextView;

    private View gameTargetLayout;
    private View decidingPointLayout;
    private View decidingPointNumberLayout;

    private TextView moreSummaryTextView;
    private ImageButton moreButton;
    private ImageButton resetButton;

    private boolean isMoreShown = false;

    public BadmintonSetupActivity() {
        super(Sport.BADMINTON);
    }

    @Override
    public BadmintonMatchSettings createSettings() {
        // does it have any settings?
        MatchSettings currentSettings = this.communicator.getCurrentSettings();
        BadmintonMatchSettings settings;
        if (currentSettings instanceof BadmintonMatchSettings) {
            // already there are some, use these
            settings = (BadmintonMatchSettings) currentSettings;
        }
        else {
            // the current settings are null, or not badminton settings, we have to fix this
            settings = new BadmintonMatchSettings(this);
            // set these new settings we are using on the communicator
            this.communicator.sendRequest(MatchMessage.SETUP_NEW_MATCH, settings);
        }
        return settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badminton_setup);
        // setup this activity now it is laid out
        setupActivity(R.string.badminton_setup);
    }

    @Override
    protected void setupActivity(int titleId) {
        super.setupActivity(titleId);

        // get the created settings
        final BadmintonMatchSettings settings = getMatchSettings();

        this.badmintonTextTitle = findViewById(R.id.badmintonGoalNumberText);
        this.badmintonMoreButton = findViewById(R.id.badmintonMoreImageButton);
        this.badmintonLessButton = findViewById(R.id.badmintonLessImageButton);
        this.badmintonGameTargetTextView = findViewById(R.id.badmintonGameTargetTextView);
        this.decidingPointSwitch = findViewById(R.id.decidingPointSwitch);
        this.decidingPointTextView = findViewById(R.id.decidingPointTextView);
        this.decidingPointLayout = findViewById(R.id.decidingPointLayout);
        this.decidingPointNumberLayout = findViewById(R.id.decidingPointNumberLayout);

        this.gameTargetLayout = findViewById(R.id.badmintonGameTargetLayout);

        this.badmintonMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int scoreGoal = getNewScoreGoal(settings.getScoreGoal(), +1);
                settings.setScoreGoal(scoreGoal);
                setActivityDataShown(settings);
            }
        });
        this.badmintonLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int scoreGoal = getNewScoreGoal(settings.getScoreGoal(), -1);
                settings.setScoreGoal(scoreGoal);
                setActivityDataShown(settings);
            }
        });
        
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
        this.badmintonGameTargetTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = settings.getPointsInGameIndex() + 1;
                int points;
                if (index == -1 || index >= BadmintonMatchSettings.K_POINTS_OPTONS.length) {
                    // too far
                    points = BadmintonMatchSettings.K_POINTS_OPTONS[0];
                }
                else {
                    // use this
                    points = BadmintonMatchSettings.K_POINTS_OPTONS[index];
                }
                settings.setPointsInGame(points);
                if (settings.getDecidingPoint() > 0) {
                    // reset the deciding point as it is set for the current games
                    settings.setDecidingPoint(settings.getDefaultDecidingPoint());
                }
                setActivityDataShown(settings);
            }
        });

        this.decidingPointSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    // we are enabled, set the deciding point to the default
                    settings.setDecidingPoint(settings.getDefaultDecidingPoint());
                }
                else {
                    // disable it by setting to -1
                    settings.setDecidingPoint(-1);
                }
                setActivityDataShown(settings);
            }
        });
        this.decidingPointTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int decidingPoint = settings.getDecidingPoint() + 2;
                if (decidingPoint > settings.getMaxDecidingPoint()) {
                    decidingPoint = settings.getMinDecidingPoint();
                }
                settings.setDecidingPoint(decidingPoint);
                setActivityDataShown(settings);
            }
        });
    }

    private void toggleMoreLess(BadmintonMatchSettings settings) {
        this.isMoreShown = !this.isMoreShown;
        // and show the data
        setActivityDataShown(settings);
    }

    private void resetSettings(BadmintonMatchSettings settings) {
        this.isMoreShown = false;
        settings.resetSettings();
        // and show the data
        setActivityDataShown(settings);
    }

    private int getNewScoreGoal(int scoreGoal, int direction) {
        // move the score goal in the right direction
        scoreGoal += direction * 2;

        if (scoreGoal > BadmintonMatchSettings.K_MAX_GAMES) {
            scoreGoal = BadmintonMatchSettings.K_MAX_GAMES;
        }
        if (scoreGoal < BadmintonMatchSettings.K_MIN_GAMES) {
            scoreGoal = BadmintonMatchSettings.K_MIN_GAMES;
        }
        return scoreGoal;
    }

    @Override
    protected SettingsBadminton getAppSettings() {
        return new SettingsBadminton(this.application);
    }

    @Override
    public void initialiseSettings(SettingsBadminton appSettings, BadmintonMatchSettings settings) {
        super.initialiseSettings(appSettings, settings);
        // setup all these defaults on our settings object
        settings.setGamesInMatch(appSettings.getGamesGoal());
        settings.setPointsInGame(appSettings.getPointsGoal());
    }

    @Override
    protected void updateAppSettings(SettingsBadminton appSettings, BadmintonMatchSettings settings) {
        super.updateAppSettings(appSettings, settings);
        // set the defaults back on the application
        appSettings.setGamesGoal(settings.getGamesInMatch());
        appSettings.setPointsGoal(settings.getPointsInGame());
    }

    @Override
    protected void setActivityDataShown(BadmintonMatchSettings settings) {
        super.setActivityDataShown(settings);

        this.badmintonTextTitle.setText(String.format(Locale.getDefault(), "%d", settings.getGamesInMatch()));
        this.badmintonGameTargetTextView.setText(String.format(Locale.getDefault(), "%d", settings.getPointsInGame()));

        int decidingPoint = settings.getDecidingPoint();
        this.decidingPointSwitch.setChecked(decidingPoint != -1);
        if (decidingPoint == -1) {
            this.decidingPointNumberLayout.setVisibility(View.GONE);
        }
        else {
            this.decidingPointNumberLayout.setVisibility(View.VISIBLE);
            this.decidingPointTextView.setText(decidingPoint + " - " + decidingPoint);
        }
        if (!this.isMoreShown) {
            this.gameTargetLayout.setVisibility(View.GONE);
            this.decidingPointLayout.setVisibility(View.GONE);

            setupButtonIcon(this.moreButton, R.drawable.ic_baseline_keyboard_arrow_right);
        }
        else {
            this.gameTargetLayout.setVisibility(View.VISIBLE);
            this.decidingPointLayout.setVisibility(View.VISIBLE);

            setupButtonIcon(this.moreButton, R.drawable.ic_baseline_keyboard_arrow_left);
        }

        // explain this when it is shrunk

        this.moreSummaryTextView.setText(
                String.format(getString(R.string.badminton_settings_summary),
                        settings.getGamesInMatch(),
                        settings.getPointsInGame(),
                        decidingPoint == -1 ? "" : String.format(getString(R.string.badminton_deciding_point_summary), decidingPoint))
        );
    }
}
