package uk.co.darkerwaters.scorepal.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.announcer.SpeakService;
import uk.co.darkerwaters.scorepal.application.ContactResolver;
import uk.co.darkerwaters.scorepal.application.GamePlayCommunicator;
import uk.co.darkerwaters.scorepal.settings.Settings;
import uk.co.darkerwaters.scorepal.settings.SettingsSounds;
import uk.co.darkerwaters.scorepal.controllers.Controller;
import uk.co.darkerwaters.scorepal.score.MatchPersistenceManager;
import uk.co.darkerwaters.scorepal.score.MatchStatistics;
import uk.co.darkerwaters.scorepal.score.base.Point;
import uk.co.darkerwaters.scorepal.score.tennis.TennisPoint;

public class MainSettingsActivity extends BaseContactsActivity {
    /*GOOGLE SIGN-IN
    implements SignInHandler.SignInListener
    */

    private Switch useContacts;
    private Switch storeLocations;

    private Switch makeClickSounds;
    private Switch makeActionSounds;
    private Switch makeActionSpeak;
    private Switch makeActionVibrations;
    private Switch makePointsAnnouncements;
    private Switch makeMessagesAnnouncements;

    private SeekBar mediaVolumeSeekBar;

    private AutoCompleteTextView selfNameEdit;
    private Button selfNameEditButton;
    /*
    REMOVED THE GOOGLE SIGN-IN OPTION FOR SIMPLICITY AT THIS TIME
    private Switch selfNameOverride;
    private SignInButton signInButton;
    private TextView signInState;*/

    private Switch dataWipeSwitch;
    private Button dataWipeButton;
    private View dataWipeExtraLayout;

    /*GOOGLE SIGN-IN
    private SignInHandler signInHandler;
    */

    private boolean isInitialising = true;
    private int maxMediaVol = 15;
    private boolean isPlayExamples = true;

    private SpeakService speakService = null;
    private ContactResolver contactResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // set the title of this
        setupActivity(R.string.menu_settings);

        this.useContacts = findViewById(R.id.useContactSwitch);
        this.storeLocations = findViewById(R.id.storeLocationSwitch);

        this.makeClickSounds = findViewById(R.id.makeClickSoundSwitch);
        this.makeActionSounds = findViewById(R.id.makeActionSoundSwitch);
        this.makeActionSpeak = findViewById(R.id.makeActionSoundSpeakSwitch);
        this.makeActionVibrations = findViewById(R.id.makeActionVibrationSwitch);
        this.makePointsAnnouncements = findViewById(R.id.makePointsAnnouncementSwitch);
        this.makeMessagesAnnouncements = findViewById(R.id.makeMessagesAnnouncementSwitch);

        this.mediaVolumeSeekBar = findViewById(R.id.mediaVolumeSeekBar);

        /*GOOGLE SIGN-IN
        this.selfNameOverride = findViewById(R.id.selfNameOverrideSwitch);
        this.signInState = findViewById(R.id.signInState);
         */
        this.selfNameEdit = findViewById(R.id.selfNameEditText);
        this.selfNameEdit.setAdapter(getCursorAdapter());
        this.selfNameEditButton = findViewById(R.id.selfNameEditButton);
        this.selfNameEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainSettingsActivity.this, LoginActivity.class);
                // start this login activity now then
                startActivity(intent);
            }
        });

        this.dataWipeButton = findViewById(R.id.dataWipeButton);
        this.dataWipeSwitch = findViewById(R.id.dataWipeSwitch);
        this.dataWipeExtraLayout = findViewById(R.id.dataWipeExtraLayout);

        final Settings settings = application.getSettings();
        final SettingsSounds sounds = new SettingsSounds(application);

        this.useContacts.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                settings.setIsRequestContactsPermission(b);
            }
        });
        this.storeLocations.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                settings.setIsStoreLocations(b);
            }
        });
        this.makeClickSounds.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sounds.setIsMakingBeepingSounds(b);
                if (b && !isInitialising) {
                    // play a little example
                    playExampleTone(Controller.KeyPress.Short);
                }
            }
        });
        this.makeActionSounds.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sounds.setIsMakingSoundingAction(b);
                if (b && !isInitialising) {
                    // play a little example
                    playExampleTone(Controller.ControllerAction.PointTeamOne);
                }
            }
        });
        this.makeActionSpeak.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sounds.setIsMakingSoundSpeakingAction(b);
                if (b && !isInitialising) {
                    // play a little example
                    playExampleSpeech(
                            TennisPoint.POINT.speakString(MainSettingsActivity.this, 1)
                            + getString(R.string.default_playerOneName));
                }
            }
        });
        this.makeActionVibrations.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sounds.setIsMakingVibrateAction(b);
                if (b && !isInitialising) {
                    // play a little example
                    playExampleVibrate(Controller.ControllerAction.PointTeamOne);
                }
            }
        });
        this.makePointsAnnouncements.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sounds.setIsSpeakingPoints(b);
                if (b && !isInitialising) {
                    // play a little example
                    playExampleSpeech(
                            TennisPoint.FIFTEEN.speakString(MainSettingsActivity.this)
                                    + Point.K_SPEAKING_SPACE
                                    + TennisPoint.LOVE.speakString(MainSettingsActivity.this));
                }
            }
        });
        this.makeMessagesAnnouncements.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sounds.setIsSpeakingMessages(b);
                if (b && !isInitialising) {
                    // play a little example
                    playExampleSpeech(getString(R.string.change_server));
                }
            }
        });

        Object service = this.getSystemService(Context.AUDIO_SERVICE);
        if (service instanceof AudioManager) {
            // we can find the max of the phone to set the range to
            this.maxMediaVol = ((AudioManager) service).getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        }
        this.mediaVolumeSeekBar.setMax(this.maxMediaVol);
        this.mediaVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i == maxMediaVol) {
                    // -1 is the max
                    sounds.setMediaVolume(-1);
                }
                else {
                    // set the value - don't allow zero - they have mute for this
                    sounds.setMediaVolume(Math.max(1, i));
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //whatever
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //whatever
            }
        });
        /*GOOGLE SIGN-IN
        this.selfNameOverride.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                selfNameEdit.setEnabled(b);
                settings.setIsOverridingSelfName(b);
                // the name might be different now
                selfNameEdit.setText(settings.getSelfName());
            }
        });
        // Set the dimensions of the sign-in button.
        this.signInButton = findViewById(R.id.sign_in_button);
        this.signInButton.setSize(SignInButton.SIZE_STANDARD);
        this.signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInHandler.signInToGoogle(true);
            }
        });
        */
        this.selfNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                // put this new name back to the settings
                boolean isSelfNameEditing = true;
                /*GOOGLE SIGN-IN
                isSelfNameEditing = selfNameOverride.isChecked();
                 */
                String userImage = getUserImage(editable.toString());
                settings.setSelfName(editable.toString(), userImage, isSelfNameEditing, MainSettingsActivity.this);
            }
        });

        this.dataWipeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    dataWipeExtraLayout.setVisibility(View.VISIBLE);
                }
                else {
                    dataWipeExtraLayout.setVisibility(View.GONE);
                }
            }
        });
        this.dataWipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                settings.wipeAllSettings();
                                // also we can delete all the files the user has stored...
                                Context context = MainSettingsActivity.this;
                                MatchPersistenceManager.GetInstance().wipeAllMatchFiles(context);
                                // and the statistics
                                MatchStatistics.GetInstance(application, context).wipeStatisticsFile(context);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                        // reset the data (will hide the clear option)
                        setDataFromApp();
                    }
                };
                // show the dialog to check for totally sure
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSettingsActivity.this);
                builder.setMessage(R.string.areYouSure).setPositiveButton(R.string.yes, dialogClickListener)
                        .setNegativeButton(R.string.no, dialogClickListener).show();

            }
        });

        /*GOOGLE SIGN-IN
        // create the sign in handler
        this.signInHandler = new SignInHandler(this, this);
        */
    }

    @Override
    protected void setupAdapters(ArrayAdapter adapter) {
        if (null != selfNameEdit) {
            this.selfNameEdit.setAdapter(adapter);
        }
    }

    private String getUserImage(String userName) {
        if (null == this.contactResolver) {
            this.contactResolver = new ContactResolver(this);
        }
        return this.contactResolver.getContactImage(userName);
    }

    private void playExampleSpeech(String string) {
        // we need our own speech service for this as the game service probably isn't running
        if (null == this.speakService) {
            // create the thing for speaking
            this.speakService = new SpeakService(this, this.application);
        }
        if (this.isPlayExamples) {
            // and speak our example now
            this.speakService.speakMessage(string, true);
        }
    }

    private void playExampleVibrate(Controller.ControllerAction action) {
        if (this.isPlayExamples) {
            GamePlayCommunicator.GetActiveCommunicator().vibrate(action);
        }
    }

    private void playExampleTone(Controller.ControllerAction action) {
        if (this.isPlayExamples) {
            GamePlayCommunicator.GetActiveCommunicator().playTone(action);
        }
    }

    private void playExampleTone(Controller.KeyPress keyPress) {
        if (this.isPlayExamples) {
            GamePlayCommunicator.GetActiveCommunicator().playTone(keyPress);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.isInitialising = false;
        /*GOOGLE SIGN-IN
        // try to sign in
        this.signInHandler.initialiseSignIn();
         */

    }

    @Override
    protected void onResume() {
        super.onResume();

        setDataFromApp();
    }

    @Override
    protected void onPause() {
        // stop speaking
        if (null != this.speakService) {
            this.speakService.close();
            this.speakService = null;
        }
        // clear the contacts we loaded
        this.contactResolver = null;

        super.onPause();
    }

    private void setDataFromApp() {
        this.isPlayExamples = false;
        Settings settings = this.application.getSettings();
        SettingsSounds sounds = new SettingsSounds(this.application);
        this.useContacts.setChecked(settings.getIsRequestContactsPermission());
        this.storeLocations.setChecked(settings.getIsStoreLocations());

        // sounds data
        this.makeClickSounds.setChecked(sounds.getIsMakingBeepingSounds());
        this.makeActionSounds.setChecked(sounds.getIsMakingSoundingAction());
        this.makeActionSpeak.setChecked(sounds.getIsMakingSoundSpeakingAction());
        this.makeActionVibrations.setChecked(sounds.getIsMakingVibrateAction());
        this.makePointsAnnouncements.setChecked(sounds.getIsSpeakingPoints());
        this.makeMessagesAnnouncements.setChecked(sounds.getIsSpeakingMessages());

        int mediaVol = sounds.getMediaVolume();
        if (mediaVol == -1) {
            // this is the max
            mediaVol = this.maxMediaVol;
        }
        this.mediaVolumeSeekBar.setProgress(mediaVol);

        // set the name up
        setSelfNameFromApp(settings);

        this.dataWipeSwitch.setChecked(false);
        this.isPlayExamples = true;
    }

    private void setSelfNameFromApp(Settings settings) {
        boolean isEnableEditing = true;
        /*GOOGLE SIGN-IN
        this.selfNameOverride.setChecked(settings.isSelfNameOverridden());
        isEnableEditing = this.selfNameOverride.isChecked();
        */
        this.selfNameEdit.setText(settings.getSelfName());
        selfNameEdit.setEnabled(isEnableEditing);
    }

    /*GOOGLE SIGN-IN
    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // pass this message to the sign-in handler to process
        this.signInHandler.handleActivityResult(requestCode, data);
    }

    @Override
    public void showSignedInUI(GoogleSignInAccount account) {
        // update the data on this activity to show the new self name
        setSelfNameFromApp(this.application.getMatchSettings());

        this.signInState.setText(getString(R.string.logged_in_user, account.getDisplayName()));
    }

    @Override
    public void showSignedOutUI() {
        // update the data on this activity to show the new self name
        setSelfNameFromApp(this.application.getMatchSettings());

        this.signInState.setText(getString(R.string.no_logged_in_user));
    }
    */
}
