package uk.co.darkerwaters.scorepal.ui.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.ApplicationState;
import uk.co.darkerwaters.scorepal.application.Log;

public class FragmentLoginName extends FragmentLogin {

    private GoogleSignInClient mGoogleSignInClient = null;
    private TextView userNameText;
    private ImageView userImageView;
    private EditText userNameEdit;
    private Button userNameButton;
    private TextView userNameWarning;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // create this layout in this view
        View root = inflater.inflate(R.layout.fragment_login_name, container, false);

        // Set the dimensions of the sign-in button.
        SignInButton signInButton = root.findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        this.userNameText = root.findViewById(R.id.text_name_user);
        this.userImageView = root.findViewById(R.id.userImageView);
        this.userNameEdit = root.findViewById(R.id.userNameEditText);
        this.userNameWarning = root.findViewById(R.id.text_name_warning);
        this.userNameButton = root.findViewById(R.id.button_use_name);

        this.userNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // fine
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updateUserLoginButton();
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        root.findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // sign in
                onGoogleSignin();
            }
        });

        this.userNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // use the user name
                onUserNameClick();
            }
        });

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestId()
                .requestProfile()
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        return root;
    }

    private void onGoogleSignin() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        getActivity().startActivityForResult(signInIntent, ActivityLogin.RC_SIGN_IN);
    }

    private void onUserNameClick() {
        // use the user name instead of the google login
        ApplicationState instance = ApplicationState.Instance();
        instance.setUserName(this.userNameEdit.getText().toString(), true);
        // and exit this activity (we are logged in now)
        ActivityLogin parent = getParent();
        if (null != parent) {
            parent.changeViews(+1);
        }
    }

    @Override
    public void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        super.handleSignInResult(completedTask);
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, just exit out of this now
            ApplicationState instance = ApplicationState.Instance();
            instance.setActiveAccount(account, true);
            // and exit this activity (we are logged in now)
            ActivityLogin parent = getParent();
            if (null != parent) {
                parent.changeViews(+1);
            }
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.error("signInResult:failed code=" + e.getStatusCode());
            String errorString = getContext().getString(R.string.errorGoogleLogin, e.getMessage());
            Toast.makeText(getContext(), errorString, Toast.LENGTH_LONG).show();
            updateUI();
        }
    }

    @Override
    public void updateUI() {
        GoogleSignInAccount account = ApplicationState.Initialise(getActivity()).getActiveAccount();
        this.userNameText.setText(account == null ? "" : account.getDisplayName());
        this.userNameEdit.setText(account == null ? "" : account.getDisplayName());

        this.userImageView.setImageResource(R.drawable.ic_person_black_24dp);
        if (null != account) {
            Uri userImage = account.getPhotoUrl();
            if (null != userImage && !userImage.toString().isEmpty()) {
                this.userImageView.setImageURI(userImage);
            }
        }

        updateUserLoginButton();
    }

    private void updateUserLoginButton() {
        String userName = this.userNameEdit.getText().toString();
        this.userNameButton.setEnabled(!userName.isEmpty());
        this.userNameWarning.setVisibility(userName.isEmpty() ? View.INVISIBLE : View.VISIBLE);
    }

}
