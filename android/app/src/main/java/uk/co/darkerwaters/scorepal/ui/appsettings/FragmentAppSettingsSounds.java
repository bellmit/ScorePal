package uk.co.darkerwaters.scorepal.ui.appsettings;

import android.content.Context;
import android.media.AudioManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.ApplicationPreferences;
import uk.co.darkerwaters.scorepal.application.ApplicationState;
import uk.co.darkerwaters.scorepal.application.SpeakService;

public class FragmentAppSettingsSounds extends FragmentAppSettings {

    private ApplicationPreferences preferences;
    private SpeakService speakService;

    private Switch buttonClick;
    private Switch actionUseNames;
    private Switch actionSpeak;
    private Switch announceChange;
    private ImageView announceChangeVolImg;
    private SeekBar announceChangeVol;
    private Switch announcePoints;
    private Switch announceChangeEnds;
    private Switch announceServer;
    private Switch announceScore;

    private int maxMediaVol = 15;
    private boolean isAllDataSet = false;

    public FragmentAppSettingsSounds() {
        super(R.layout.fragment_app_settings_sounds, R.id.nav_app_settings_sounds);
    }

    @Override
    public void onResume() {
        // create the speech service to use
        this.speakService = new SpeakService(getContext());
        isAllDataSet = false;
        super.onResume();
    }

    @Override
    public void onPause() {
        // kill the speaking service we used
        if (null != this.speakService) {
            this.speakService.close();
            this.speakService = null;
        }
        super.onPause();
    }

    @Override
    protected void setupControls(View root) {
        // get all the switches
        buttonClick = root.findViewById(R.id.buttonClickSwitch);
        actionUseNames = root.findViewById(R.id.speakNamesSwitch);
        actionSpeak = root.findViewById(R.id.actionSpeakSwitch);
        announceChange = root.findViewById(R.id.announceChangeSwitch);
        announceChangeVolImg = root.findViewById(R.id.actionVolumeImage);
        announceChangeVol = root.findViewById(R.id.announceChangeVolume);

        announcePoints = root.findViewById(R.id.announcePointsSwitch);
        announceChangeEnds = root.findViewById(R.id.announceChangeEndsSwitch);
        announceServer = root.findViewById(R.id.announceServerSwitch);
        announceScore = root.findViewById(R.id.announceScoreSwitch);

        Object service = getActivity().getSystemService(Context.AUDIO_SERVICE);
        if (service instanceof AudioManager) {
            // we can find the max of the phone to set the range to
            maxMediaVol = ((AudioManager) service).getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        }
        announceChangeVol.setMax(maxMediaVol);

        preferences = ApplicationState.Initialise(getContext()).getPreferences();
        // and listen to them all
        buttonClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences.setSoundButtonClick(buttonClick.isChecked());
                updateSwitchCheck(buttonClick);
            }
        });
        actionSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences.setSoundActionSpeak(actionSpeak.isChecked());
                updateSwitchCheck(actionSpeak);
            }
        });
        actionUseNames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences.setSoundUseSpeakingNames(actionUseNames.isChecked());
                updateSwitchCheck(actionUseNames);
            }
        });
        announceChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences.setSoundAnnounceChange(announceChange.isChecked());
                updateSwitchCheck(announceChange);
                updateAnnounceControls();
            }
        });
        announceChangeVolImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSampleSpeech(R.string.speechVolumeExample);
            }
        });
        announceChangeVol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int mediaVol = announceChangeVol.getProgress();
                if (mediaVol == maxMediaVol) {
                    // they want the max we can achieve (remember as -1)
                    mediaVol = -1;
                }
                // set this value
                preferences.setSoundAnnounceVolume(mediaVol, true);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        announcePoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences.setSoundAnnounceChangePoints(announcePoints.isChecked());
                updateSwitchCheck(announcePoints);
                updateAnnounceControls();
            }
        });
        announceChangeEnds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences.setSoundAnnounceChangeEnds(announceChangeEnds.isChecked());
                updateSwitchCheck(announceChangeEnds);
                updateAnnounceControls();
            }
        });
        announceServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences.setSoundAnnounceChangeServer(announceServer.isChecked());
                updateSwitchCheck(announceServer);
                updateAnnounceControls();
            }
        });
        announceScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences.setSoundAnnounceChangeScore(announceScore.isChecked());
                updateSwitchCheck(announceScore);
                updateAnnounceControls();
            }
        });
    }

    private void playSampleSpeech(int strId) {
        Context context = getContext();
        if (isAllDataSet && null != speakService && null != context) {
            speakService.speakMessage(context.getString(strId));
        }
    }

    private void updateAnnounceControls() {
        boolean isEnabled = preferences.getSoundAnnounceChange();
        announcePoints.setEnabled(isEnabled);
        announceChangeEnds.setEnabled(isEnabled);
        announceServer.setEnabled(isEnabled);
        announceScore.setEnabled(isEnabled);

        // leave vol alone now at the top and more global
        //announceChangeVol.setEnabled(isEnabled);

        // be sure to set the colours too
        updateSwitchCheck(announcePoints);
        updateSwitchCheck(announceChangeEnds);
        updateSwitchCheck(announceServer);
        updateSwitchCheck(announceScore);

        isAllDataSet = true;
    }

    @Override
    protected void setDataToControls() {
        setSwitchChecked(buttonClick, preferences.getSoundButtonClick());
        setSwitchChecked(buttonClick, preferences.getSoundButtonClick());
        setSwitchChecked(actionSpeak, preferences.getSoundActionSpeak());
        setSwitchChecked(actionUseNames, preferences.getSoundUseSpeakingNames());
        setSwitchChecked(announceChange, preferences.getSoundAnnounceChange());
        setSwitchChecked(announcePoints, preferences.getSoundAnnounceChangePoints());
        setSwitchChecked(announceChangeEnds, preferences.getSoundAnnounceChangeEnds());
        setSwitchChecked(announceServer, preferences.getSoundAnnounceChangeServer());
        setSwitchChecked(announceScore, preferences.getSoundAnnounceChangeScore());

        int mediaVol = preferences.getSoundAnnounceVolume();
        // set the vol (if they want -1 then they want the max really)
        announceChangeVol.setProgress(mediaVol < 0 ? maxMediaVol : mediaVol);
        // and be sure to enable / disable the individual sound controls
        updateAnnounceControls();
    }
}
