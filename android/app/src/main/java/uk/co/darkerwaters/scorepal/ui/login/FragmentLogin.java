package uk.co.darkerwaters.scorepal.ui.login;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;

import uk.co.darkerwaters.scorepal.ui.ActivityMain;

public abstract class FragmentLogin extends Fragment {

    public ActivityLogin getParent() {
        FragmentActivity activity = getActivity();
        if (activity instanceof ActivityLogin) {
            return ((ActivityLogin)activity);
        }
        else {
            return null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // and show this latest data
        updateUI();
    }

    public void handleSignInResult(Task<GoogleSignInAccount> task) {

    }

    public abstract void updateUI();
}
