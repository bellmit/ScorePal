package uk.co.darkerwaters.scorepal.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseUser;

import java.io.InputStream;
import java.net.URL;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.ViewAnimator;
import uk.co.darkerwaters.scorepal.storage.StorageManager;
import uk.co.darkerwaters.scorepal.storage.uk.co.darkerwaters.scorepal.storage.data.User;

public class AccountActivity extends AppCompatActivity implements StorageManager.IStorageManagerListener {

    private Bitmap userBitmap = null;
    private boolean isUserLoginDataShown = false;
    private static final int RC_SIGN_IN = 9002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        StorageManager.getManager().registerListener(this);

        // don't let them edit the email address
        ((EditText) findViewById(R.id.user_email)).setFocusable(false);

        // and initialise the sign in button
        ((SignInButton)findViewById(R.id.sign_in_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StorageManager.getManager().signInToGoogle(AccountActivity.this);
            }
        });
    }

    @Override
    protected void onDestroy() {
        StorageManager.getManager().unregisterListener(this);
        if (null != userBitmap) {
            userBitmap.recycle();
            userBitmap = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // setup the correct controls
        User user = StorageManager.getManager().getCurrentUser();
        // and show this data
        showUserLoginData(user);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == StorageManager.RC_SIGN_IN) {
            // pass this back to the manager
            StorageManager.getManager().onSignInResult(data);
        }
    }

    @Override
    public void onGoogleSigninResult(GoogleSignInAccount acct) {
        // we are signed into our google account - this is fine, but just
        // a step to the firebase account really, leave it till then to do the good stuff
        if (null == acct) {
            Toast.makeText(this, R.string.failed_google_signin, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFirebaseSigninResult(final FirebaseUser acct) {
        // we are signed into our firebase account now - this is fine, but just
        // a step to the actual user object really, leave it till then to do the good stuff
        if (null == acct) {
            Toast.makeText(this, R.string.failed_firebase_signin, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUserSigninResult(User currentUser) {
        // we have a user signed in - show this content
        showUserLoginData(currentUser);
    }

    private void showUserLoginData(final User user) {
        TextView nameView = (TextView) findViewById(R.id.user_name);
        EditText emailView = (EditText) findViewById(R.id.user_email);
        final ImageView imageView = (ImageView) findViewById(R.id.user_image);
        final SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        // slide these controls in to show them
        imageView.setImageResource(R.drawable.court);
        if (false == isUserLoginDataShown) {
            // slide in the controls, only slide in the signin button if there is no account signed in
            ViewAnimator.slideControlsDownAndIn(this, new ViewAnimator.IViewAnimator() {
                @Override
                public void onAnimationEnd() {
                    if (null == user) {
                        signInButton.setVisibility(View.VISIBLE);
                    } else {
                        signInButton.setVisibility(View.GONE);
                    }
                }
            }, nameView, imageView, signInButton);
            isUserLoginDataShown = true;
        }
        if (user == null) {
            // show that the user didn't log in okay
            nameView.setText(R.string.signin_failed);
            signInButton.setVisibility(View.VISIBLE);
        }
        else {
            // show the logged in user details
            signInButton.setVisibility(View.GONE);
            nameView.setText(user.getNickname());
            emailView.setText(user.getEmail());
            if (null != userBitmap) {
                imageView.setImageBitmap(userBitmap);
            }
            else {
                // and finally show the image from the user - load it here
                new AsyncTask<String, Void, Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(String... params) {
                        try {
                            InputStream in = new URL(user.getPhotoUrl()).openStream();
                            userBitmap = BitmapFactory.decodeStream(in);

                        } catch (Exception e) {
                            Log.e(MainActivity.TAG, e.getMessage());
                        }
                        return userBitmap;
                    }

                    @Override
                    protected void onPostExecute(final Bitmap bitmap) {
                        // and set this
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                        super.onPostExecute(bitmap);
                    }
                }.execute();
            }
        };
    }
}
