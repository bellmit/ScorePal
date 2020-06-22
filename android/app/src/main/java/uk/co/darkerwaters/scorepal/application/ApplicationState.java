package uk.co.darkerwaters.scorepal.application;

import android.content.Context;
import android.net.Uri;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import uk.co.darkerwaters.scorepal.data.Match;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.points.Sport;

public class ApplicationState {
    private static ApplicationState INSTANCE = null;
    private static final Object LOCK = new Object();

    public static ApplicationState Instance() {
        return ApplicationState.INSTANCE;
    }

    public static ApplicationState Initialise(Context context) {
        synchronized (ApplicationState.LOCK) {
            if (null == ApplicationState.INSTANCE) {
                // create one
                ApplicationState.INSTANCE = new ApplicationState(context);
            }
            return ApplicationState.INSTANCE;
        }
    }

    private final ApplicationPreferences preferences;

    private GoogleSignInAccount activeAccount;
    private boolean isLoggedIn;

    private Match activeMatch;

    private ApplicationState(Context context) {
        this.isLoggedIn = false;
        this.activeAccount = null;
        this.activeMatch = null;
        this.preferences = new ApplicationPreferences(context);

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        boolean isUseAccount = this.preferences.getIsUseGoogleLogin();
        // set our name to use by default
        setUserName(preferences.getUserName(), !isUseAccount);
        // and set the last active google account
        if (null != account) {
            // we are logged in already, just set this data and quit right away to use this
            // last logged in state
            setActiveAccount(account, isUseAccount);
        }
    }

    public ApplicationPreferences getPreferences() {
        return preferences;
    }

    public Match getActiveMatch() {
        return this.activeMatch;
    }

    public void setActiveMatch(Match newMatch) {
        this.activeMatch = newMatch;
    }

    public void setActiveAccount(GoogleSignInAccount account, boolean isUseThisAccount) {
        this.activeAccount = account;
        this.preferences.setIsUseGoogleLogin(isUseThisAccount);
        this.preferences.setUserName(account.getDisplayName());
        if (isUseThisAccount && null != this.activeAccount) {
            // we are to use this account and there is an account to use, we are logged in now
            this.isLoggedIn = true;
        }
    }

    public GoogleSignInAccount getActiveAccount() {
        return this.activeAccount;
    }

    public String getUserName() {
        if (null != this.activeAccount && this.preferences.getIsUseGoogleLogin()) {
            // there is an account and we are using it
            return this.activeAccount.getDisplayName();
        } else {
            // use the name they entered instead
            return this.preferences.getUserName();
        }
    }

    public String getUserEmail() {
        if (null != this.activeAccount) {
            return this.activeAccount.getEmail();
        }
        else {
            return null;
        }
    }

    public Uri getUserImage() {
        if (null != this.activeAccount) {
            return this.activeAccount.getPhotoUrl();
        }
        else {
            return null;
        }
    }

    public void setUserName(String userName, boolean isUseThisName) {
        this.preferences.setUserName(userName);
        this.preferences.setIsUseGoogleLogin(!isUseThisName);
        if (isUseThisName && !getUserName().isEmpty()) {
            // we are using this name and there is a user name specified, we are logged in
            this.isLoggedIn = true;
        }
    }

    public boolean isLoggedIn() {
        return this.isLoggedIn;
    }

    public MatchSetup getDefaultMatchSetup(Context context, Sport sport) {
        // so we want to store this as the default setup, just create a file and overwrite
        MatchSetup defaultSetup = sport.newSetup();
        try {
            // need to use the name of the sport to get the file to load
            File settingsFile = new File(context.getFilesDir(), "settings_" + defaultSetup.getSport().name() + ".json");
            if (settingsFile.exists()) {
                // the file is there, load it
                StringBuilder text = new StringBuilder();
                BufferedReader br = new BufferedReader(new FileReader(settingsFile));
                String line;
                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
                // get the data as a string and transform to the setup object
                MatchSetup fileSetup = MatchSetup.CreateFromJSON(text.toString());
                if (null != fileSetup) {
                    // this load worked, use this one instead
                    defaultSetup = fileSetup;
                }
            }
        } catch (IOException e) {
            Log.error("Failed to load the default setup", e);
        }
        if (null != defaultSetup) {
            // but, there is one difference we want to press on users. That being the
            // person who's app this is should be in team one
            defaultSetup.setUsernameInTeamOne(getPreferences().getUserName());
        }
        return defaultSetup;
    }

    public void storeDefaultMatchSetup(final Context context, final MatchSetup matchSetup) {
        // so we want to store this as the default setup, just create a file and overwrite
        if (null != matchSetup) {
            // this can take a second so just do in a quick thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        File settingsFile = new File(context.getFilesDir(),
                                "settings_" + matchSetup.getSport().name() + ".json");
                        FileWriter writer = new FileWriter(settingsFile);
                        writer.append(matchSetup.getAsJSON().toString());
                        writer.flush();
                        writer.close();
                    } catch (IOException e) {
                        Log.error("Failed to save the settings file: IO exception", e);
                    } catch (Exception e) {
                        Log.error("Failed to save the settings file", e);
                    }
                }
            }).start();
        }
    }

    public void wipeDefaultMatchSetups(final Context context) {
        // delete all the settings file we have created
        // this can take a second so just do in a quick thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                File[] files = context.getFilesDir().listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        if (file.exists() && file.isFile()) {
                            // this is a file - is it a settings file
                            String fileName = file.getName().toLowerCase();
                            return fileName.startsWith("settings_") &&
                                    fileName.endsWith(".json");
                        }
                        return false;
                    }
                });
                for (File settingsFile : files) {
                    if (!settingsFile.delete()) {
                        settingsFile.deleteOnExit();
                    }
                }
            }
        }).start();
    }
}
