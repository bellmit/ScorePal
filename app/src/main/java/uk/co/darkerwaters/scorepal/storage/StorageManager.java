package uk.co.darkerwaters.scorepal.storage;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import uk.co.darkerwaters.scorepal.MainActivity;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.ScoreData;

/**
 * Created by douglasbrain on 13/06/2017.
 */

public class StorageManager {

    private static final String TAG = "StorageManager";
    private static final String WEB_APP_ID = "AIzaSyATE7xgvUU6qiqbckYojn3eCH-kwaOT_ow";

    private static final StorageManager INSTANCE = new StorageManager();
    private MainActivity main;

    private GoogleApiClient mGoogleApiClient = null;
    private FirebaseAuth mFirebaseAuth = null;

    private GoogleSignInAccount googleAcct = null;
    private FirebaseUser firebaseUser = null;
    private DatabaseReference mDatabase = null;
    private DatabaseReference mMatchesReference = null;
    private ValueEventListener mPostListener = null;

    private User currentUser = null;

    public interface IStorageManagerListener {
        public void onGoogleSigninResult(GoogleSignInAccount acct);
        public void onFirebaseSigninResult(FirebaseUser acct);
    }

    private final ArrayList<IStorageManagerListener> listeners;

    private StorageManager() {
        this.listeners = new ArrayList<IStorageManagerListener>();
    }

    public static StorageManager getManager() {
        return INSTANCE;
    }

    public void initialise(MainActivity main) {
        this.main = main;

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

        // Initialize Database
        mMatchesReference = mDatabase.child("matches");
        listenForPosts();
    }

    private void listenForPosts() {
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get match object that has just changed
                Post post = dataSnapshot.getValue(Post.class);
                onPostDataChanged(post);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        mDatabase.child("posts").addValueEventListener(postListener);
        // [END post_value_event_listener]

        // Keep copy of listener so we can remove it when app stops
        mPostListener = postListener;
    }

    private void onPostDataChanged(Post post) {

    }

    public void signout() {
        // remove any listeners
        mDatabase.child("posts").removeEventListener(mPostListener);
        mPostListener = null;
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

    public boolean unregiserListener(IStorageManagerListener listener) {
        boolean result;
        synchronized (this.listeners) {
            result = this.listeners.remove(listener);
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
                        currentUser = new User(acct.getUid(), acct.getDisplayName());
                        // set the data on this user object
                        currentUser.email = acct.getEmail();
                        currentUser.photoUrl = acct.getPhotoUrl().toString();
                        // and put in the database
                        currentUser.updateInDatabase(mDatabase);
                    }
                    else {
                        // have the user object, cool
                        currentUser = data;
                    }
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

    public boolean isGoogleConnected() {
        return null != this.googleAcct;
    }

    public boolean isFirebaseConnected() {
        return null != this.firebaseUser;
    }

    public FirebaseUser getFirebaseUser() { return this.firebaseUser; }

    public Intent getSignInIntent() {
        return Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
    }

    public void addMatchData(Match match) {
        mDatabase.child("matches").child(match.ID).setValue(match);
    }

    public void setMatchData(Match match, ScoreData score) {
        // create the match data at /matches/$matchId and at
        // /scores/$matchId simultaneously
        String matchId = match.ID;
        Map<String, Object> childUpdates = new HashMap<String, Object>();
        childUpdates.put("/matches/" + matchId, match);
        childUpdates.put("/scores/" + matchId, score);
        // and push this to the database
        mDatabase.updateChildren(childUpdates);
    }

    public void onSignInResult(GoogleSignInResult result) {

        if (result.isSuccess()) {
            Log.d(TAG, "handleSignInResult:" + result.isSuccess());
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
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this.main, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            onFirebaseSigninResult(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            onFirebaseSigninResult(null);
                        }
                    }
                });
    }
}
