package uk.co.darkerwaters.scorepal.activities;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.SignInButton;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.handlers.SignInHandler;
import uk.co.darkerwaters.scorepal.application.Application;
import uk.co.darkerwaters.scorepal.application.ContactResolver;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.score.MatchPersistenceManager;
import uk.co.darkerwaters.scorepal.score.MatchStatistics;
import uk.co.darkerwaters.scorepal.settings.Settings;

public class LoginActivity extends BaseContactsActivity implements SignInHandler.SignInListener {

    private AutoCompleteTextView usernameEditText;
    private Button loginButton;
    private SignInButton signInButton;
    private ImageView usernameImage;
    private ProgressBar loadingProgressBar;

    private ContactResolver contactResolver;

    private SignInHandler signInHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final Settings settings = application.getSettings();

        this.usernameEditText = findViewById(R.id.username);
        this.loginButton = findViewById(R.id.login);
        this.signInButton = findViewById(R.id.sign_in_button);
        this.usernameImage = findViewById(R.id.usernameImage);
        this.loadingProgressBar = findViewById(R.id.loading);

        this.usernameEditText.setAdapter(getCursorAdapter());

        this.usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                // put this new name back to the settings
                String userImage = getUserImage(editable.toString());
                settings.setSelfName(editable.toString(), userImage, true, LoginActivity.this);
                if (null != userImage && !userImage.isEmpty()) {
                    usernameImage.setImageURI(Uri.parse(userImage));
                }
                else {
                    usernameImage.setImageResource(R.drawable.ic_baseline_person_outline);
                }
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show the dialog to check for totally sure
                final String selfName = application.getSettings().getSelfName();
                if (selfName.isEmpty()) {
                    new AlertDialog.Builder(LoginActivity.this)
                            .setTitle(R.string.noNameEntered)
                            .setMessage(R.string.noNameEnteredExplain)
                            .setNeutralButton(R.string.ok, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                else {
                    // show the alert to warn them of the error of their ways
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    new AlertDialog.Builder(LoginActivity.this)
                            .setTitle(R.string.noLogin)
                            .setMessage(R.string.areYouSureToNotLogin)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            })
                            .setNegativeButton(R.string.no, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        });
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInToGoogle();
            }
        });

        // create the sign in handler
        this.signInHandler = new SignInHandler(this, this);
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (null != acct) {
            showSignedInState(acct.getDisplayName(), false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.usernameEditText.setText(this.application.getSettings().getSystemSelfName());
    }

    @Override
    protected void onPause() {
        // clear the contacts we loaded
        this.contactResolver = null;
        // and pause
        super.onPause();
    }

    @Override
    protected void setupAdapters(ArrayAdapter adapter) {
        if (null != this.usernameEditText) {
            this.usernameEditText.setAdapter(adapter);
        }
    }

    private String getUserImage(String userName) {
        if (null == this.contactResolver) {
            this.contactResolver = new ContactResolver(this);
        }
        return this.contactResolver.getContactImage(userName);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SignInHandler.RC_SIGN_IN:
                // pass this message to the sign-in handler to process
                this.signInHandler.handleActivityResult(requestCode, data);
                break;
        }
    }

    public void signInToGoogle() {
        this.signInHandler.signInToGoogle(false);
    }

    private void showSignedInState(final String displayName, final boolean isFinishOnShown) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // fill in the name
                usernameEditText.setText(displayName);
                if (isFinishOnShown) {
                    // and move on
                    finish();
                }
            }
        });
    }

    @Override
    public void showSignedInUI(GoogleSignInAccount account) {
        // we are signed in - set the user's name accordingly
        String displayName = account.getDisplayName();
        this.application.getSettings().setSystemSelfName(displayName);
        if (null != this.usernameEditText) {
            showSignedInState(displayName, true);
        }
        else {
            // signed in, which is cool
            finish();
        }
    }

    @Override
    public void showSignedOutUI() {
        final String selfName = this.application.getSettings().getSelfName();
        if (null != this.usernameEditText) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    usernameEditText.setText(selfName);
                }
            });
        }
    }
}
