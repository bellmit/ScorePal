package uk.co.darkerwaters.scorepal.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.ApplicationState;
import uk.co.darkerwaters.scorepal.application.MatchPersistenceManager;
import uk.co.darkerwaters.scorepal.application.MatchService;
import uk.co.darkerwaters.scorepal.ui.login.ActivityLogin;
import uk.co.darkerwaters.scorepal.ui.matchplay.ActivityPlayMatch;

public class ActivitySplash extends AppCompatActivity {
    private static boolean isFirstShow = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // initialise our application state
        ApplicationState.Initialise(this);

        // this is the default activity - did we get a filename passed to us to import?
        Uri data = getIntent().getData();
        if (null != data) {
            String encodedPath = data.getEncodedPath();
            if (null != encodedPath && !encodedPath.isEmpty()) {
                // try to import this
                MatchPersistenceManager.GetInstance().importMatchData(this, data);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (this.isFirstShow) {
            // the first time we are shown, wait a little before proceeding
            final Handler newHandler = new Handler();
            newHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isFirstShow = false;
                    proceedToNextActivity();
                }
            }, 2000);
        }
        else {
            // this isn't the first time shown, probably come back from the login activity
            // so go straight to the next activity
            proceedToNextActivity();
        }
    }

    private void proceedToNextActivity() {
        // if we are logged in, then go to the main activity
        final Intent intent;
        if (ApplicationState.Instance().isLoggedIn()) {
            // we are logged in, show the next activity
            if (null != MatchService.GetRunningService()) {
                // there is a running service, show the match playing screen
                intent = new Intent(ActivitySplash.this, ActivityPlayMatch.class);
            } else {
                // show the main screen
                intent = new Intent(ActivitySplash.this, ActivityMain.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
        } else {
            // make them log in before doing anything else
            intent = new Intent(ActivitySplash.this, ActivityLogin.class);
        }
        // start the right activity
        startActivity(intent);
    }
}
