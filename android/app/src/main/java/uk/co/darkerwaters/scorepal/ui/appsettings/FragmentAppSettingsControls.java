package uk.co.darkerwaters.scorepal.ui.appsettings;

import android.app.SearchManager;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.ApplicationPreferences;
import uk.co.darkerwaters.scorepal.application.ApplicationState;
import uk.co.darkerwaters.scorepal.application.MatchService;
import uk.co.darkerwaters.scorepal.controllers.Flic1Controller;
import uk.co.darkerwaters.scorepal.ui.views.CheckableIndicatorButton;
import uk.co.darkerwaters.scorepal.ui.views.RadioIndicatorButton;

public class FragmentAppSettingsControls extends FragmentAppSettings {

    private RadioIndicatorButton controlTeams;
    private RadioIndicatorButton controlServe;

    private CheckableIndicatorButton controlVolume;
    private CheckableIndicatorButton controlFlic1;
    private CheckableIndicatorButton controlFlic2;
    private CheckableIndicatorButton controlMedia;

    private TextView clickSingleText;
    private TextView clickDoubleText;

    /*
    private TextView volUpText;
    private TextView volDownText;

    private TextView mediaNextText;
    private TextView mediaPrevText;

    private TextView flicSingleText;
    private TextView flicDoubleText;
*/
    private ImageView flicLink;
    private ImageView mediaLink;

    private Button flic1SetupButton;
    private Button flic2SetupButton;
    private Button mediaSetupButton;

    private ApplicationPreferences preferences;

    public FragmentAppSettingsControls() {
        super(R.layout.fragment_app_settings_controls, R.id.nav_app_settings_controls);
    }

    @Override
    protected void setupControls(View root) {

        preferences = ApplicationState.Instance().getPreferences();

        controlTeams = root.findViewById(R.id.controlTypeTeams);
        controlServe = root.findViewById(R.id.controlTypeServerReceiver);

        controlVolume = root.findViewById(R.id.volControlButton);
        controlFlic1 = root.findViewById(R.id.flic1ControlButton);
        controlFlic2 = root.findViewById(R.id.flic2ControlButton);
        controlMedia = root.findViewById(R.id.mediaControlButton);

        /*
        volUpText = root.findViewById(R.id.volumeUpTextView);
        volDownText = root.findViewById(R.id.volumeDownTextView);

        mediaNextText = root.findViewById(R.id.mediaNextTextView);
        mediaPrevText = root.findViewById(R.id.mediaPrevTextView);

        flicSingleText = root.findViewById(R.id.flicSingleTextView);
        flicDoubleText = root.findViewById(R.id.flicDoubleTextView);
        */
        clickSingleText = root.findViewById(R.id.clickSingleTextView);
        clickDoubleText = root.findViewById(R.id.clickDoubleTextView);

        flicLink = root.findViewById(R.id.flicLink);
        mediaLink = root.findViewById(R.id.mediaLink);

        flic1SetupButton = root.findViewById(R.id.flic1SetupButton);
        flic2SetupButton = root.findViewById(R.id.flic2SetupButton);
        mediaSetupButton = root.findViewById(R.id.mediaSetupButton);

        if (!preferences.getIsAllowMedia()) {
            root.findViewById(R.id.controlMediaTitle).setVisibility(View.GONE);
            root.findViewById(R.id.controlMediaLayout).setVisibility(View.GONE);
        }

        // listen for changes to the control type
        controlTeams.addOnCheckChangeListener(new CheckableIndicatorButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                if (isChecked) {
                    setControlType(true);
                }
            }
        });
        controlServe.addOnCheckChangeListener(new CheckableIndicatorButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                if (isChecked) {
                    setControlType(false);
                }
            }
        });

        // and turning on or off the types of control
        controlVolume.addOnCheckChangeListener(new CheckableIndicatorButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                preferences.setIsControlVol(isChecked);
                // if the match service is running we need to update the controllers running now
                updateMatchServiceControllers();
            }
        });
        controlMedia.addOnCheckChangeListener(new CheckableIndicatorButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                setIsControlMedia(isChecked);
            }
        });
        controlFlic1.addOnCheckChangeListener(new CheckableIndicatorButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                setIsControlFlic1(isChecked);
            }
        });
        controlFlic2.addOnCheckChangeListener(new CheckableIndicatorButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                setIsControlFlic2(isChecked);
            }
        });

        // the setup buttons
        flic1SetupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Flic1Controller.Initialise(getActivity(), true);
            }
        });
        flic2SetupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ActivityFlic2Setup.class);
                getActivity().startActivity(intent);
            }
        });
        mediaSetupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ActivityMediaRemoteSetup.class);
                getActivity().startActivity(intent);
            }
        });

        // and the links
        flicLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // send the user to the web shopping
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, getString(R.string.purchase_flic_remote_query));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                startActivity(intent);
            }
        });
        mediaLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // send the user to the web shopping
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, getString(R.string.purchase_media_remote_query));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                startActivity(intent);
            }
        });
    }

    private void setIsControlFlic1(boolean isChecked) {
        // set flic to be on or off
        preferences.setIsControlFlic1(isChecked);
        // and show / hide the button
        if (isChecked) {
            flic1SetupButton.animate()
                    .alpha(1f)
                    .setDuration(1000)
                    .withStartAction(new Runnable() {
                        @Override
                        public void run() {
                            flic1SetupButton.setVisibility(View.VISIBLE);
                        }
                    })
                    .start();
        }
        else {
            flic1SetupButton.setVisibility(View.GONE);
            flic1SetupButton.setAlpha(0f);
        }
        // if the match service is running we need to update the controllers running now
        updateMatchServiceControllers();
    }

    private void setIsControlFlic2(boolean isChecked) {
        // set flic to be on or off
        preferences.setIsControlFlic2(isChecked);
        // and show / hide the button
        if (isChecked) {
            flic2SetupButton.animate()
                    .alpha(1f)
                    .setDuration(1000)
                    .withStartAction(new Runnable() {
                        @Override
                        public void run() {
                            flic2SetupButton.setVisibility(View.VISIBLE);
                        }
                    })
                    .start();
        }
        else {
            flic2SetupButton.setVisibility(View.GONE);
            flic2SetupButton.setAlpha(0f);
        }
        // if the match service is running we need to update the controllers running now
        updateMatchServiceControllers();
    }

    private void setIsControlMedia(boolean isChecked) {
        preferences.setIsControlMedia(isChecked);
        // and show / hide the button
        if (isChecked) {
            mediaSetupButton.animate()
                    .alpha(1f)
                    .setDuration(1000)
                    .withStartAction(new Runnable() {
                        @Override
                        public void run() {
                            mediaSetupButton.setVisibility(View.VISIBLE);
                        }
                    })
                    .start();
        }
        else {
            mediaSetupButton.setVisibility(View.GONE);
            mediaSetupButton.setAlpha(0f);
        }
        // if the match service is running we need to update the controllers running now
        updateMatchServiceControllers();
    }

    private void updateMatchServiceControllers() {
        MatchService service = MatchService.GetRunningService();
        if (null != service) {
            service.initialiseControllers();
        }
    }

    private void setControlType(boolean isControlUseTeams) {
        // set this on the preferences
        preferences.setIsControlTeams(isControlUseTeams);
        // and update the labels for what everything does
        updateLabels();
    }

    private void updateLabels() {
        boolean teams = preferences.getIsControlTeams();

        /*
        volUpText.setText(teams ? R.string.controlVolUpTeamExplain : R.string.controlVolUpServingExplain);
        volDownText.setText(teams ? R.string.controlVolDownTeamExplain : R.string.controlVolDownServingExplain);
        //mediaPlayText.setText(R.string.controlMediaPlayPauseExplain);
        mediaNextText.setText(teams ? R.string.controlMediaNextTeamExplain : R.string.controlMediaNextServingExplain);
        mediaPrevText.setText(teams ? R.string.controlMediaPrevTeamExplain : R.string.controlMediaPrevServingExplain);
        flicSingleText.setText(teams ? R.string.controlFlicSingleTeamExplain : R.string.controlFlicSingleServingExplain);
        flicDoubleText.setText(teams ? R.string.controlFlicDoubleTeamExplain : R.string.controlFlicDoubleServingExplain);
        //flicLongText.setText(R.string.controlFlicLongExplain);

         */
        clickSingleText.setText(teams ? R.string.controlClickSingleTeamExplain : R.string.controlClickSingleServingExplain);
        clickDoubleText.setText(teams ? R.string.controlClickDoubleTeamExplain : R.string.controlClickDoubleServingExplain);
    }

    @Override
    protected void setDataToControls() {
        // set the control type
        if (preferences.getIsControlTeams()) {
            this.controlTeams.setChecked(true);
        }
        else {
            this.controlServe.setChecked(true);
        }
        this.controlVolume.setChecked(preferences.getIsControlVol());
        this.controlFlic1.setChecked(preferences.getIsControlFlic1());
        this.controlFlic2.setChecked(preferences.getIsControlFlic2());
        this.controlMedia.setChecked(preferences.getIsControlMedia());
        // and update the labels for what everything does
        updateLabels();

        // and the buttons
        setIsControlFlic1(preferences.getIsControlFlic1());
        setIsControlFlic2(preferences.getIsControlFlic2());
        setIsControlMedia(preferences.getIsControlMedia());
    }
}
