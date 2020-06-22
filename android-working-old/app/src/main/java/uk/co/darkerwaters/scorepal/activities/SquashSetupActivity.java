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
import uk.co.darkerwaters.scorepal.score.squash.SquashMatchSettings;
import uk.co.darkerwaters.scorepal.settings.SettingsSquash;

public class SquashSetupActivity extends SetupTeamActivity<SquashMatchSettings, SettingsSquash> {

    private TextView squashTextTitle;
    private ImageButton squashLessButton;
    private ImageButton squashMoreButton;

    private TextView squashBeforeGameTargetTextView;

    private View gameTargetLayout;

    private TextView moreSummaryTextView;
    private ImageButton moreButton;
    private ImageButton resetButton;

    private boolean isMoreShown = false;

    public SquashSetupActivity() {
        super(Sport.SQUASH);
    }

    @Override
    public SquashMatchSettings createSettings() {
        // does it have any settings?
        MatchSettings currentSettings = this.communicator.getCurrentSettings();
        SquashMatchSettings settings;
        if (currentSettings instanceof SquashMatchSettings) {
            // already there are some, use these
            settings = (SquashMatchSettings) currentSettings;
        }
        else {
            // the current settings are null, or not squash settings, we have to fix this
            settings = new SquashMatchSettings(this);
            // set these new settings we are using on the communicator
            this.communicator.sendRequest(MatchMessage.SETUP_NEW_MATCH, settings);
        }
        return settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_squash_setup);
        // setup this activity now it is laid out
        setupActivity(R.string.squash_setup);
    }

    @Override
    protected void setupActivity(int titleId) {
        super.setupActivity(titleId);

        // get the created settings
        final SquashMatchSettings settings = getMatchSettings();

        this.squashTextTitle = findViewById(R.id.squashGoalNumberText);
        this.squashMoreButton = findViewById(R.id.squashMoreImageButton);
        this.squashLessButton = findViewById(R.id.squashLessImageButton);
        this.squashBeforeGameTargetTextView = findViewById(R.id.squashGameTargetTextView);

        this.gameTargetLayout = findViewById(R.id.squashGameTargetLayout);

        this.squashMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int scoreGoal = getNewScoreGoal(settings.getScoreGoal(), +1);
                settings.setScoreGoal(scoreGoal);
                setActivityDataShown(settings);
            }
        });
        this.squashLessButton.setOnClickListener(new View.OnClickListener() {
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
        this.squashBeforeGameTargetTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int squash = settings.getPointsInGame();
                if (squash == 11) {
                    // make 21
                    squash = 21;
                }
                else {
                    squash = 11;
                }
                settings.setPointsInGame(squash);
                setActivityDataShown(settings);
            }
        });
    }

    private void toggleMoreLess(SquashMatchSettings settings) {
        this.isMoreShown = !this.isMoreShown;
        // and show the data
        setActivityDataShown(settings);
    }

    private void resetSettings(SquashMatchSettings settings) {
        this.isMoreShown = false;
        settings.resetSettings();
        // and show the data
        setActivityDataShown(settings);
    }

    private int getNewScoreGoal(int scoreGoal, int direction) {
        // move the score goal in the right direction
        scoreGoal += direction * 2;

        if (scoreGoal > SquashMatchSettings.K_MAX_GAMES) {
            scoreGoal = SquashMatchSettings.K_MAX_GAMES;
        }
        if (scoreGoal < SquashMatchSettings.K_MIN_GAMES) {
            scoreGoal = SquashMatchSettings.K_MIN_GAMES;
        }
        return scoreGoal;
    }

    @Override
    protected SettingsSquash getAppSettings() {
        return new SettingsSquash(this.application);
    }

    @Override
    public void initialiseSettings(SettingsSquash appSettings, SquashMatchSettings settings) {
        super.initialiseSettings(appSettings, settings);
        // setup all these defaults on our settings object
        settings.setGamesInMatch(appSettings.getGamesGoal());
        settings.setPointsInGame(appSettings.getPointsGoal());
    }

    @Override
    protected void updateAppSettings(SettingsSquash appSettings, SquashMatchSettings settings) {
        super.updateAppSettings(appSettings, settings);
        // set the defaults back on the application
        appSettings.setGamesGoal(settings.getGamesInMatch());
        appSettings.setPointsGoal(settings.getPointsInGame());
    }

    @Override
    protected void setActivityDataShown(SquashMatchSettings settings) {
        super.setActivityDataShown(settings);

        this.squashTextTitle.setText(String.format(Locale.getDefault(), "%d", settings.getGamesInMatch()));
        this.squashBeforeGameTargetTextView.setText(String.format(Locale.getDefault(), "%d", settings.getPointsInGame()));

        if (!this.isMoreShown) {
            this.gameTargetLayout.setVisibility(View.GONE);

            setupButtonIcon(this.moreButton, R.drawable.ic_baseline_keyboard_arrow_right);
        }
        else {
            this.gameTargetLayout.setVisibility(View.VISIBLE);

            setupButtonIcon(this.moreButton, R.drawable.ic_baseline_keyboard_arrow_left);
        }

        // explain this when it is shrunk
        this.moreSummaryTextView.setText(
                String.format(getString(R.string.squash_settings_summary),
                        settings.getGamesInMatch(),
                        settings.getPointsInGame())
        );
    }
}
