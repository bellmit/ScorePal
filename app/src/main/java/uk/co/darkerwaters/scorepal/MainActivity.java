package uk.co.darkerwaters.scorepal;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.co.darkerwaters.scorepal.bluetooth.BtConnectActivity;
import uk.co.darkerwaters.scorepal.bluetooth.BtManager;
import uk.co.darkerwaters.scorepal.history.HistoryFile;
import uk.co.darkerwaters.scorepal.history.HistoryManager;
import uk.co.darkerwaters.scorepal.storage.StorageManager;

public class MainActivity extends AppCompatActivity implements StorageManager.IStorageManagerListener {

    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 9001;

    private boolean isUserLoginDataShown = false;
    private Bitmap userBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialise the Bluetooth manager
        BtManager.initialise(this);

        // get rid of the account details until we sign in
        ((TextView) findViewById(R.id.user_name)).setVisibility(View.GONE);
        ((ImageView) findViewById(R.id.user_image)).setVisibility(View.GONE);
        ((SignInButton) findViewById(R.id.sign_in_button)).setVisibility(View.GONE);
        // this hides the controls
        isUserLoginDataShown = false;

        // configure all the google stuff in the storage manager
        StorageManager storageManager = StorageManager.getManager();
        // listen to changes on this manager
        storageManager.registerListener(this);
        // and initialise the manager
        storageManager.initialise(this);

        // and initialise the sign in button
        ((SignInButton)findViewById(R.id.sign_in_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInToGoogle();
            }
        });

        // start the signin intent to sign into our Google account, and thence Firebase too
        signInToGoogle();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // setup the correct controls
        FirebaseUser acct = StorageManager.getManager().getFirebaseUser();
        // and show this data
        if (null == acct) {
            showUserLoginData(null, null);
        }
        else {
            showUserLoginData(acct.getDisplayName(), acct.getPhotoUrl());
        }
    }

    @Override
    protected void onDestroy() {
        // unregister our listeners
        BtManager.getManager().unregisterGlobalListeners();
        StorageManager.getManager().unregiserListener(this);
        // clean the bitmap
        userBitmap.recycle();
        super.onDestroy();
    }

    public void signInToGoogle() {
        Intent signInIntent = StorageManager.getManager().getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onGoogleSigninResult(GoogleSignInAccount acct) {
        // we are signed into our google account - this is fine, but just
        // a step to the firebase account really, leave it till then to do the good stuff

    }

    @Override
    public void onFirebaseSigninResult(final FirebaseUser acct) {
        // show the user's account all nice here
        if (null == acct) {
            showUserLoginData(null, null);
        }
        else {
            showUserLoginData(acct.getDisplayName(), acct.getPhotoUrl());
        }
    }

    private void showUserLoginData(final String name, final Uri photoUri) {
        TextView nameView = (TextView) findViewById(R.id.user_name);
        final ImageView imageView = (ImageView) findViewById(R.id.user_image);
        final SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        // slide these controls in to show them
        imageView.setImageResource(R.drawable.court);
        if (false == isUserLoginDataShown) {
            // slide in the controls, only slide in the signin button if there is no account signed in
            ViewAnimator.slideControlsDownAndIn(this, new ViewAnimator.IViewAnimator() {
                @Override
                public void onAnimationEnd() {
                    if (null == name) {
                        signInButton.setVisibility(View.VISIBLE);
                    } else {
                        signInButton.setVisibility(View.GONE);
                    }
                }
            }, nameView, imageView, signInButton);
            isUserLoginDataShown = true;
        }
        else {
            // set the signin button visibility properly
            if (null == name) {
                signInButton.setVisibility(View.VISIBLE);
            } else {
                signInButton.setVisibility(View.GONE);
            }
        }
        if (name == null || photoUri == null) {
            // show that the user didn't log in okay
            nameView.setText(R.string.signin_failed);
        }
        else {
            // show the logged in user details
            nameView.setText(name);
            if (null != userBitmap) {
                imageView.setImageBitmap(userBitmap);
            }
            else {
                // and finally show the image from the user - load it here
                new AsyncTask<String, Void, Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(String... params) {
                        try {
                            InputStream in = new URL(photoUri.toString()).openStream();
                            userBitmap = BitmapFactory.decodeStream(in);

                        } catch (Exception e) {
                            Log.e("Error", e.getMessage());
                            e.printStackTrace();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            // pass this back to the manager
            StorageManager.getManager().onSignInResult(result);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    public void onScore(View view) {
        Intent intent = new Intent(getApplicationContext(), ScoreActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.manage_device:
                intent = new Intent(getApplicationContext(), BtConnectActivity.class);
                startActivity(intent);
                return true;
            case R.id.history:
                intent = new Intent(getApplicationContext(), HistoryActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
