package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Locale;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.settings.SettingsPoints;
import uk.co.darkerwaters.scorepal.score.base.MatchMessage;
import uk.co.darkerwaters.scorepal.score.MatchSettings;
import uk.co.darkerwaters.scorepal.score.points.PointsMatchSettings;
import uk.co.darkerwaters.scorepal.score.base.Sport;

public class PointsSetupActivity extends SetupTeamActivity<PointsMatchSettings, SettingsPoints> {

    private TextView pointsTextTitle;
    private ImageButton pointsLessButton;
    private ImageButton pointsMoreButton;

    private Switch twoPointsAheadSwitch;
    private TextView pointsBeforeChangeEndTextView;
    private TextView pointsBeforeChangeServerTextView;
    
    private View changeEndLayout;
    private View changeServerLayout;

    private TextView moreSummaryTextView;
    private ImageButton moreButton;
    private ImageButton resetButton;

    private boolean isMoreShown = false;

    public PointsSetupActivity() {
        super(Sport.POINTS);
    }

    @Override
    public PointsMatchSettings createSettings() {
        // does it have any settings?
        MatchSettings currentSettings = this.communicator.getCurrentSettings();
        PointsMatchSettings settings;
        if (currentSettings instanceof PointsMatchSettings) {
            // already there are some, use these
            settings = (PointsMatchSettings) currentSettings;
        }
        else {
            // the current settings are null, or not points settings, we have to fix this
            settings = new PointsMatchSettings(this);
            // set these new settings we are using on the communicator
            this.communicator.sendRequest(MatchMessage.SETUP_NEW_MATCH, settings);
        }
        return settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points_setup);
        // setup this activity now it is laid out
        setupActivity(R.string.points_setup);
    }

    @Override
    protected void setupActivity(int titleStringId) {
        super.setupActivity(titleStringId);

        // get the created settings
        final PointsMatchSettings settings = getMatchSettings();

        this.pointsTextTitle = findViewById(R.id.pointsGoalNumberText);
        this.pointsMoreButton = findViewById(R.id.pointsMoreImageButton);
        this.pointsLessButton = findViewById(R.id.pointsLessImageButton);
        this.twoPointsAheadSwitch = findViewById(R.id.twoPointsAheadSwitch);
        this.pointsBeforeChangeEndTextView = findViewById(R.id.pointsToChangeEndsTextView);
        this.pointsBeforeChangeServerTextView = findViewById(R.id.pointsToChangeServerTextView);
        this.changeEndLayout = findViewById(R.id.pointsToChangeEndsLayout);
        this.changeServerLayout = findViewById(R.id.pointsToChangeServerLayout);

        this.pointsMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int scoreGoal = getNewScoreGoal(settings.getScoreGoal(), +1);
                settings.setScoreGoal(scoreGoal);
                setActivityDataShown(settings);
            }
        });
        this.pointsLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int scoreGoal = getNewScoreGoal(settings.getScoreGoal(), -1);
                settings.setScoreGoal(scoreGoal);
                setActivityDataShown(settings);
            }
        });
        this.twoPointsAheadSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                settings.setIsTwoPointsAheadRequired(b);
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
        this.pointsBeforeChangeEndTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int points = settings.getPointsToChangeEnds() + 6;
                if (points > 18) {
                    // start again
                    points = 6;
                }
                settings.setPointsToChangeEnds(points);
                setActivityDataShown(settings);
            }
        });
        this.pointsBeforeChangeServerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int points = settings.getPointsToChangeServer() + 2;
                if (points > 8) {
                    // start again
                    points = 2;
                }
                settings.setPointsToChangeServer(points);
                setActivityDataShown(settings);
            }
        });
    }

    private void toggleMoreLess(PointsMatchSettings settings) {
        this.isMoreShown = !this.isMoreShown;
        // and show the data
        setActivityDataShown(settings);
    }

    private void resetSettings(PointsMatchSettings settings) {
        this.isMoreShown = false;
        settings.resetSettings();
        // and show the data
        setActivityDataShown(settings);
    }

    private int getNewScoreGoal(int scoreGoal, int direction) {
        if (direction > 0) {
            if (scoreGoal < 30) {
                scoreGoal = scoreGoal + 2;
            } else if (scoreGoal < 60) {
                scoreGoal = scoreGoal + 4;
            } else {
                scoreGoal = scoreGoal + 8;
            }
        }
        else {
            if (scoreGoal < 30) {
                scoreGoal = scoreGoal - 2;
            } else if (scoreGoal < 60) {
                scoreGoal = scoreGoal - 4;
            } else {
                scoreGoal = scoreGoal - 8;
            }
        }
        if (scoreGoal > PointsMatchSettings.K_MAX_POINTS) {
            scoreGoal = PointsMatchSettings.K_MAX_POINTS;
        }
        if (scoreGoal < PointsMatchSettings.K_MIN_POINTS) {
            scoreGoal = PointsMatchSettings.K_MIN_POINTS;
        }
        return scoreGoal;
    }

    @Override
    protected SettingsPoints getAppSettings() {
        return new SettingsPoints(this.application);
    }

    @Override
    public void initialiseSettings(SettingsPoints appSettings, PointsMatchSettings settings) {
        super.initialiseSettings(appSettings, settings);
        // setup all these defaults on our settings object
        settings.setScoreGoal(appSettings.getPointsGoal());
        settings.setIsTwoPointsAheadRequired(appSettings.getIsTwoPointsAheadRequired());
        settings.setPointsToChangeEnds(appSettings.getPointsToChangeEnds());
        settings.setPointsToChangeServer(appSettings.getPointsToChangeServer());
    }

    @Override
    protected void updateAppSettings(SettingsPoints appSettings, PointsMatchSettings settings) {
        super.updateAppSettings(appSettings, settings);
        // set the defaults back on the application
        appSettings.setPointsGoal(settings.getScoreGoal());
        appSettings.setIsTwoPointsAheadRequired(settings.isTwoPointsRequired());
        appSettings.setPointsToChangeEnds(settings.getPointsToChangeEnds());
        appSettings.setPointsToChangeServer(settings.getPointsToChangeServer());
    }

    @Override
    protected void setActivityDataShown(PointsMatchSettings settings) {
        super.setActivityDataShown(settings);

        this.twoPointsAheadSwitch.setChecked(settings.isTwoPointsRequired());
        this.pointsTextTitle.setText(String.format(Locale.getDefault(), "%d", settings.getScoreGoal()));
        this.pointsBeforeChangeEndTextView.setText(String.format(Locale.getDefault(), "%d", settings.getPointsToChangeEnds()));
        this.pointsBeforeChangeServerTextView.setText(String.format(Locale.getDefault(), "%d", settings.getPointsToChangeServer()));

        if (!this.isMoreShown) {
            this.twoPointsAheadSwitch.setVisibility(View.GONE);
            this.changeEndLayout.setVisibility(View.GONE);
            this.changeServerLayout.setVisibility(View.GONE);

            setupButtonIcon(this.moreButton, R.drawable.ic_baseline_keyboard_arrow_right);
        }
        else {
            this.twoPointsAheadSwitch.setVisibility(View.VISIBLE);
            this.changeEndLayout.setVisibility(View.VISIBLE);
            this.changeServerLayout.setVisibility(View.VISIBLE);

            setupButtonIcon(this.moreButton, R.drawable.ic_baseline_keyboard_arrow_left);
        }

        // explain this when it is shrunk
        this.moreSummaryTextView.setText(
                String.format(getString(R.string.points_settings_summary),
                        settings.getScoreGoal(),
                        settings.isTwoPointsRequired() ? getString(R.string.two_points_ahead_insert) : "",
                        settings.getPointsToChangeServer(),
                        settings.getPointsToChangeEnds())
        );
    }
}
