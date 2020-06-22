package uk.co.darkerwaters.scorepal.activities.handlers;

import android.content.Intent;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

/*
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
*/
import java.util.Collections;

import uk.co.darkerwaters.scorepal.activities.BaseActivity;
import uk.co.darkerwaters.scorepal.activities.LoginActivity;
import uk.co.darkerwaters.scorepal.application.Application;
import uk.co.darkerwaters.scorepal.application.DriveServiceHelper;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.settings.Settings;

public class SignInHandler {

    public static final int RC_SIGN_IN = 140;

    private final GoogleSignInClient signInClient;
    private final BaseActivity activity;
    private final SignInListener listener;
    private boolean isSignedIn = false;

    public interface SignInListener {
        void showSignedInUI(GoogleSignInAccount account);
        void showSignedOutUI();
    }

    public SignInHandler(BaseActivity activity, SignInListener listener) {
        this.activity = activity;
        this.listener = listener;
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        this.signInClient = GoogleSignIn.getClient(this.activity, gso);
    }

    public void initialiseSignIn() {
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this.activity);
        if (null != account) {
            this.isSignedIn = true;
            this.listener.showSignedInUI(account);
            // initialise our credentials
            initialiseCredentials();
        } else {
            // we are not signed in
            this.isSignedIn = false;
            this.listener.showSignedOutUI();
        }
    }

    private void initialiseCredentials() {
        /*
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this.activity, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(account.getAccount());
        com.google.api.services.drive.Drive googleDriveService =
                new com.google.api.services.drive.Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential)
                        .setApplicationName(Log.K_APPLICATION)
                        .build();
        DriveServiceHelper.InitialiseService(googleDriveService);
        */
    }

    public void signInToGoogle(boolean isFirstSignOut) {
        if (isFirstSignOut && this.isSignedIn) {
            this.isSignedIn = false;
            signInClient.signOut();
        }
        Intent signInIntent = signInClient.getSignInIntent();
        this.activity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public boolean handleActivityResult(int requestCode, Intent data) {
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
            return true;
        }
        else {
            // don't want this
            return false;
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            // get the account
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            this.isSignedIn = true;
            this.listener.showSignedInUI(account);
            initialiseCredentials();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.error("signInResult:failed code=" + e.getStatusCode(), e);
            this.listener.showSignedOutUI();
        }
    }
}
