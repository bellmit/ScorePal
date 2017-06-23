package uk.co.darkerwaters.scorepal.storage;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.MainActivity;
import uk.co.darkerwaters.scorepal.bluetooth.BtManager;

/**
 * Created by douglasbrain on 13/06/2017.
 */

public class StorageManager {

    private static final String WEB_APP_ID = "AIzaSyATE7xgvUU6qiqbckYojn3eCH-kwaOT_ow";
    public static final int RC_SIGN_IN = 9001;

    private static final StorageManager INSTANCE = new StorageManager();
    private MainActivity main;

    private GoogleApiClient mGoogleApiClient = null;
    private FirebaseAuth mFirebaseAuth = null;

    private GoogleSignInAccount googleAcct = null;
    private FirebaseUser firebaseUser = null;
    private DatabaseReference mDatabase = null;

    private User currentUser = null;

    private Match currentMatchData;

    public interface IStorageManagerListener {
        public void onGoogleSigninResult(GoogleSignInAccount acct);
        public void onFirebaseSigninResult(FirebaseUser acct);
        public void onUserSigninResult(User currentUser);
    }

    public interface IStorageManagerDataListener {
        void onPlayerTitlesUpdated(String playerOneTitle, String playerTwoTitle);
        void onScoreDataUpdated(ScoreData scoreData);
    }

    private final ArrayList<IStorageManagerListener> listeners;
    private final ArrayList<IStorageManagerDataListener> dataListeners;

    private StorageManager() {
        currentMatchData = null;
        // and create the members to use
        this.listeners = new ArrayList<IStorageManagerListener>();
        this.dataListeners = new ArrayList<IStorageManagerDataListener>();
    }

    public void resetMatchStartedDate(int secondsOffset) {
        if (null == this.currentMatchData) {
            Log.e(MainActivity.TAG, "Resetting match date before initialising data");
        }
        else {
            Date matchStartedDate = this.currentMatchData.getMatchPlayedDate();
            // there might be an offset from when the game actually started, pull back the start date
            // to be the actual time the player started their game rather than the time they connected
            // their phone to the device
            if (secondsOffset != 0) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(matchStartedDate);
                cal.add(Calendar.SECOND, secondsOffset);
                matchStartedDate = cal.getTime();
                // and put this back on the match
                this.currentMatchData.setMatchPlayedDate(matchStartedDate);
            }
        }
    }

    public void setCurrentPlayers(String playerOne, String playerTwo) {
        // set the players on the match
        if (null == this.currentMatchData) {
            Log.e(MainActivity.TAG, "Setting match players before initialising data");
        }
        else if (this.currentMatchData.playerOne == null || false == this.currentMatchData.playerOne.equals(playerOne) ||
                    this.currentMatchData.playerTwo == null || false == this.currentMatchData.playerTwo.equals(playerTwo)) {
            // this is a change in data, so set this data on the match we are using to store our data
            this.currentMatchData.playerOne = playerOne;
            this.currentMatchData.playerTwo = playerTwo;
            // inform listeners of this
            synchronized (this.listeners) {
                for (IStorageManagerDataListener listener : this.dataListeners) {
                    listener.onPlayerTitlesUpdated(playerOne, playerTwo);
                }
            }
        }
    }

    public String getCurrentPlayerOne() {
        if (null == this.currentMatchData) {
            Log.e(MainActivity.TAG, "Getting match player before initialising data");
            return "";
        }
        else {
            return this.currentMatchData.playerOne;
        }
    }

    public String getCurrentPlayerTwo() {
        if (null == this.currentMatchData) {
            Log.e(MainActivity.TAG, "Getting match player before initialising data");
            return "";
        }
        else {
            return this.currentMatchData.playerTwo;
        }
    }

    public ScoreData getCurrentScoreData() {
        if (null == this.currentMatchData) {
            Log.e(MainActivity.TAG, "Getting match score data before initialising data");
            return new ScoreData();
        }
        else {
            return this.currentMatchData.getScoreData();
        }
    }

    public void onNewScoreData(ScoreData scoreData) {
        // this score data should update our currently stored match
        if (null == this.currentMatchData) {
            Log.e(MainActivity.TAG, "Setting match score data before initialising data");
        }
        else {
            // set this data on the match we are using to store our data
            this.currentMatchData.setCurrentScoreData(scoreData);
            // inform listeners of this
            synchronized (this.listeners) {
                for (IStorageManagerDataListener listener : this.dataListeners) {
                    listener.onScoreDataUpdated(scoreData);
                }
            }
        }
    }

    public Date getMatchStartedDate() {
        if (null == this.currentMatchData) {
            Log.e(MainActivity.TAG, "Getting match date before initialising data");
            return new Date();
        }
        else {
            return this.currentMatchData.getMatchPlayedDate();
        }
    }

    public static StorageManager getManager() {
        return INSTANCE;
    }

    public void initialise(MainActivity main) {
        this.main = main;
        ScoreData newData = BtManager.getManager().getLatestScoreData();
        if (null == newData) {
            newData = new ScoreData();
        }
        // create the match we are playing here now we are initialised
        this.currentMatchData = new Match(this.currentUser,
                main.getString(R.string.player_one),
                main.getString(R.string.player_two),
                ScoreData.getScoreString(this.main, newData),
                newData,
                new Date());

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        // Also the Firebase web-client id for the app to which we are connecting
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(main.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this.main)
                .enableAutoManage(this.main, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        // connection to the google sign-in failed, do something
                        onGoogleSigninResult(null);
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // also intialise the Firebase members to access our data
        mFirebaseAuth = FirebaseAuth.getInstance();
        // and try to get the current user - to skip the login if we don't need it
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_database_ref]

        if (null == firebaseUser || null == currentUser) {
            // start the signin intent to sign into our Google account, and thence Firebase too
            signInToGoogle(main);
        }
    }

    public void signInToGoogle(Activity context) {
        Intent signInIntent = StorageManager.getManager().getSignInIntent();
        context.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void signout() {
        // and signout from firebase
        FirebaseAuth.getInstance().signOut();
    }

    public boolean registerListener(IStorageManagerListener listener) {
        boolean result;
        synchronized (this.listeners) {
            result = this.listeners.add(listener);
        }
        return result;
    }

    public boolean unregisterListener(IStorageManagerListener listener) {
        boolean result;
        synchronized (this.listeners) {
            result = this.listeners.remove(listener);
        }
        return result;
    }
    public boolean registerListener(IStorageManagerDataListener listener) {
        boolean result;
        synchronized (this.dataListeners) {
            result = this.dataListeners.add(listener);
        }
        return result;
    }

    public boolean unregisterListener(IStorageManagerDataListener listener) {
        boolean result;
        synchronized (this.dataListeners) {
            result = this.dataListeners.remove(listener);
        }
        return result;
    }

    private void onGoogleSigninResult(GoogleSignInAccount acct) {
        // remember the result of this signin to google
        this.googleAcct = acct;
        synchronized (this.listeners) {
            for (IStorageManagerListener listener : this.listeners) {
                listener.onGoogleSigninResult(acct);
            }
        }
    }

    public DatabaseReference getTopLevel() {
        return mDatabase;
    }

    private void onUserSigninResult(User currentUser) {
        // remember the result of this signin to google
        this.currentUser = currentUser;
        // set this on our match data
        if (null != this.currentMatchData) {
            this.currentMatchData.setCurrentUser(this.currentUser);
        }
        synchronized (this.listeners) {
            for (IStorageManagerListener listener : this.listeners) {
                listener.onUserSigninResult(this.currentUser);
            }
        }
    }

    private void onFirebaseSigninResult(final FirebaseUser acct) {
        // remember the result of this signin to firebase
        this.firebaseUser = acct;
        if (null != acct) {
            // this is a nice special case as creates a user for this user, let's make it sure
            // that this data is stored in the firebase database
            User.getUser(mDatabase, acct.getUid(), new StorageResult<User>() {
                @Override
                public void onResult(User data) {
                    // is the user in the database?
                    if (null == data) {
                        // create the user object
                        data = new User(acct.getUid(), acct.getDisplayName());
                        // set the data on this user object
                        data.email = acct.getEmail();
                        data.photoUrl = acct.getPhotoUrl().toString();
                        // and put in the database
                        data.updateInDatabase(mDatabase);
                    }
                    // and inform everyone of this, and store the user object from the db
                    onUserSigninResult(data);
                }
            });

        }
        //inform all the listeners of this change
        synchronized (this.listeners) {
            for (IStorageManagerListener listener : this.listeners) {
                listener.onFirebaseSigninResult(acct);
            }
        }
    }

    public User getCurrentUser() { return this.currentUser; }

    public Intent getSignInIntent() {
        return Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
    }

    public void onSignInResult(Intent data) {
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        if (result.isSuccess()) {
            Log.d(MainActivity.TAG, "handleSignInResult:" + result.isSuccess());
            if (result.isSuccess()) {
                // Signed in successfully, show authenticated UI.
                GoogleSignInAccount acct = result.getSignInAccount();
                onGoogleSigninResult(acct);
                //mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
                //updateUI(true);
            } else {
                // Signed out, show unauthenticated UI.
                //updateUI(false);
            }

            // Google Sign In was successful, authenticate with Firebase
            GoogleSignInAccount account = result.getSignInAccount();
            firebaseAuthWithGoogle(account);
        } else {
            // Google Sign In failed, update UI appropriately
            onGoogleSigninResult(null);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this.main, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(MainActivity.TAG, "signInWithCredential:success");
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            onFirebaseSigninResult(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(MainActivity.TAG, "signInWithCredential:failure", task.getException());
                            onFirebaseSigninResult(null);
                        }
                    }
                });
    }
}
