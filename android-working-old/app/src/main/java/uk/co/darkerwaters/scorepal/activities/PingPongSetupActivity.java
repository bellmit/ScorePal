package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Locale;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.settings.SettingsPingPong;
import uk.co.darkerwaters.scorepal.score.base.MatchMessage;
import uk.co.darkerwaters.scorepal.score.MatchSettings;
import uk.co.darkerwaters.scorepal.score.pingpong.PingPongMatchSettings;
import uk.co.darkerwaters.scorepal.score.base.Sport;

public class PingPongSetupActivity extends SetupTeamActivity<PingPongMatchSettings, SettingsPingPong> {

    private TextView pingPongTextTitle;
    private ImageButton pingPongLessButton;
    private ImageButton pingPongMoreButton;

    private TextView pingPongBeforeRoundTargetTextView;
    private Switch expediteSystemSwitch;
    private TextView expediteMinutesTextView;
    private TextView expeditePointsTextView;

    private View roundTargetLayout;
    private View expediteSystemLayout;

    private TextView moreSummaryTextView;
    private ImageButton moreButton;
    private ImageButton resetButton;

    private boolean isMoreShown = false;

    public PingPongSetupActivity() {
        super(Sport.PING_PONG);
    }

    @Override
    public PingPongMatchSettings createSettings() {
        // does it have any settings?
        MatchSettings currentSettings = this.communicator.getCurrentSettings();
        PingPongMatchSettings settings;
        if (currentSettings instanceof PingPongMatchSettings) {
            // already there are some, use these
            settings = (PingPongMatchSettings) currentSettings;
        }
        else {
            // the current settings are null, or not pingPong settings, we have to fix this
            settings = new PingPongMatchSettings(this);
            // set these new settings we are using on the communicator
            this.communicator.sendRequest(MatchMessage.SETUP_NEW_MATCH, settings);
        }
        return settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ping_pong_setup);
        // setup this activity now it is laid out
        setupActivity(R.string.ping_pong_setup);
    }

    @Override
    protected void setupActivity(int titleId) {
        super.setupActivity(titleId);

        // get the created settings
        final PingPongMatchSettings settings = getMatchSettings();

        this.pingPongTextTitle = findViewById(R.id.pingPongGoalNumberText);
        this.pingPongMoreButton = findViewById(R.id.pingPongMoreImageButton);
        this.pingPongLessButton = findViewById(R.id.pingPongLessImageButton);
        this.pingPongBeforeRoundTargetTextView = findViewById(R.id.pingPongRoundTargetTextView);

        this.expediteSystemSwitch = findViewById(R.id.switchPingPongExpediteSystem);
        this.expediteMinutesTextView = findViewById(R.id.expediteTimeTextView);
        this.expeditePointsTextView = findViewById(R.id.expeditePointsTextView);
        this.roundTargetLayout = findViewById(R.id.pingPongRoundTargetLayout);
        this.expediteSystemLayout = findViewById(R.id.pingPongExpediteSystemLayout);

        this.pingPongMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int scoreGoal = getNewScoreGoal(settings.getScoreGoal(), +1);
                settings.setScoreGoal(scoreGoal);
                setActivityDataShown(settings);
            }
        });
        this.pingPongLessButton.setOnClickListener(new View.OnClickListener() {
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
        this.pingPongBeforeRoundTargetTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pingPong = settings.getPointsInRound();
                if (pingPong == 11) {
                    // make 21
                    pingPong = 21;
                }
                else {
                    pingPong = 11;
                }
                settings.setPointsInRound(pingPong);
                setActivityDataShown(settings);
            }
        });

        this.expediteSystemSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                // set if the system is enabled or not on the settings
                settings.setIsExpediteSystemEnabled(b);
                setActivityDataShown(settings);
            }
        });
        this.expediteMinutesTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // set if the system is enabled or not on the settings
                // cycle the minutes
                int minutes = settings.getExpediteSystemMinutes() + 5;
                if (minutes > 20) {
                    minutes = 5;
                }
                settings.setExpediteSystemMinutes(minutes);
                setActivityDataShown(settings);
            }
        });
        this.expeditePointsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // set if the system is enabled or not on the settings
                // cycle the points
                int points = settings.getExpediteSystemPoints() + 2;
                if (points > 22) {
                    points = 16;
                }
                settings.setExpediteSystemPoints(points);
                setActivityDataShown(settings);
            }
        });
    }

    private void toggleMoreLess(PingPongMatchSettings settings) {
        this.isMoreShown = !this.isMoreShown;
        // and show the data
        setActivityDataShown(settings);
    }

    private void resetSettings(PingPongMatchSettings settings) {
        this.isMoreShown = false;
        settings.resetSettings();
        // and show the data
        setActivityDataShown(settings);
    }

    private int getNewScoreGoal(int scoreGoal, int direction) {
        // move the score goal in the right direction
        scoreGoal += direction * 2;

        if (scoreGoal > PingPongMatchSettings.K_MAX_ROUNDS) {
            scoreGoal = PingPongMatchSettings.K_MAX_ROUNDS;
        }
        if (scoreGoal < PingPongMatchSettings.K_MIN_ROUNDS) {
            scoreGoal = PingPongMatchSettings.K_MIN_ROUNDS;
        }
        return scoreGoal;
    }

    @Override
    protected SettingsPingPong getAppSettings() {
        return new SettingsPingPong(this.application);
    }

    @Override
    public void initialiseSettings(SettingsPingPong appSettings, PingPongMatchSettings settings) {
        super.initialiseSettings(appSettings, settings);
        // setup all these defaults on our settings object
        settings.setRoundsInMatch(appSettings.getRoundsGoal());
        settings.setPointsInRound(appSettings.getPointsGoal());
        settings.setExpediteSystemMinutes(appSettings.getExpediteSystemMinutes());
        settings.setExpediteSystemPoints(appSettings.getExpediteSystemPoints());
        settings.setIsExpediteSystemEnabled(appSettings.getExpediteSystemEnabled());
    }

    @Override
    protected void updateAppSettings(SettingsPingPong appSettings, PingPongMatchSettings settings) {
        super.updateAppSettings(appSettings, settings);
        // set the defaults back on the application
        appSettings.setRoundsGoal(settings.getRoundsInMatch());
        appSettings.setPointsGoal(settings.getPointsInRound());
        appSettings.setExpediteSystemMinutes(settings.getExpediteSystemMinutes());
        appSettings.setExpediteSystemPoints(settings.getExpediteSystemPoints());
        appSettings.setExpediteSystemEnabled(settings.isExpediteSystemEnabled());
    }

    @Override
    protected void setActivityDataShown(PingPongMatchSettings settings) {
        super.setActivityDataShown(settings);

        this.pingPongTextTitle.setText(String.format(Locale.getDefault(), "%d", settings.getRoundsInMatch()));
        this.pingPongBeforeRoundTargetTextView.setText(String.format(Locale.getDefault(), "%d", settings.getPointsInRound()));

        this.expediteSystemSwitch.setChecked(settings.isExpediteSystemEnabled());
        this.expediteMinutesTextView.setText(String.format(Locale.getDefault(), "%d", settings.getExpediteSystemMinutes()));
        this.expeditePointsTextView.setText(String.format(Locale.getDefault(), "%d", settings.getExpediteSystemPoints()));

        if (!this.isMoreShown) {
            this.roundTargetLayout.setVisibility(View.GONE);
            this.expediteSystemSwitch.setVisibility(View.GONE);
            this.expediteSystemLayout.setVisibility(View.GONE);

            setupButtonIcon(this.moreButton, R.drawable.ic_baseline_keyboard_arrow_right);
        }
        else {
            this.roundTargetLayout.setVisibility(View.VISIBLE);
            this.expediteSystemSwitch.setVisibility(View.VISIBLE);
            if (settings.isExpediteSystemEnabled()) {
                this.expediteSystemLayout.setVisibility(View.VISIBLE);
            }
            else {
                this.expediteSystemLayout.setVisibility(View.GONE);
            }

            setupButtonIcon(this.moreButton, R.drawable.ic_baseline_keyboard_arrow_left);
        }

        // explain this when it is shrunk
        String expediteString = String.format(getString(R.string.ping_pong_settings_expedite_system),
                settings.getExpediteSystemMinutes(),
                settings.getExpediteSystemPoints());
        this.moreSummaryTextView.setText(
                String.format(getString(R.string.ping_pong_settings_summary),
                        settings.getRoundsInMatch(),
                        settings.getPointsInRound(),
                        settings.isExpediteSystemEnabled() ? expediteString : "")
        );
    }
}
