package uk.co.darkerwaters.scorepal.ui.login;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;

import java.util.List;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.ui.PagingFragmentAdapter;
import uk.co.darkerwaters.scorepal.ui.PermissionsHandler;
import uk.co.darkerwaters.scorepal.ui.views.CustomSnackbar;

public class ActivityLogin extends AppCompatActivity implements PermissionsHandler.PermissionsListener, PermissionsHandler.Container, PagingFragmentAdapter.BackPressedHandler {

    public static final int RC_SIGN_IN = 101;

    public static final String[] PERMISSIONS_ACCOUNT = new String[] {
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.INTERNET };

    private ViewPager2 fragmentContainer;
    private PagingFragmentAdapter<FragmentLogin> pagerAdapter;

    private PermissionsHandler permissionsHandler;

    @Override
    public PermissionsHandler getPermissionsHandler() {
        return permissionsHandler;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        permissionsHandler = new PermissionsHandler(this);

        fragmentContainer = findViewById(R.id.fragment_container);
        pagerAdapter = new PagingFragmentAdapter<>(getSupportFragmentManager(), getLifecycle(),
                fragmentContainer, new Class[] {
                        FragmentLoginName.class,
                        FragmentLoginPermissions.class,
                        FragmentLoginControls.class
        });
        pagerAdapter.addListener(this);
        fragmentContainer.setAdapter(pagerAdapter);
        // setup the button
        changeViews(0);
    }

    public void changeViews(int delta) {
        int newViewIndex = fragmentContainer.getCurrentItem() + delta;
        if (newViewIndex >= 0 && newViewIndex < pagerAdapter.getItemCount()) {
            // this is good
            fragmentContainer.setCurrentItem(newViewIndex);
        }
        else {
            // this is the end
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // listen to the permissions handler
        permissionsHandler.addListener(this);
    }

    @Override
    protected void onPause() {
        if (null != permissionsHandler) {
            permissionsHandler.removeListener(this);
        }
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PermissionsHandler.PERMISSIONS_REQUEST) {
            // pass this to the handler
            this.permissionsHandler.processPermissionsResult(permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            // pass this onto the name fragment to deal with
            FragmentLoginName fragmentLoginName = PagingFragmentAdapter.FindFragment(this, FragmentLoginName.class);
            if (null != fragmentLoginName) {
                fragmentLoginName.handleSignInResult(task);
            }
        }
    }

    @Override
    public void onPermissionsChanged(String[] permissions, int[] grantResults) {
        // permissions have changed, we might have access to things now
    }

    @Override
    public boolean handleBackPressed() {
        boolean isHandled = false;
        if(!pagerAdapter.emptyHistory()) {
            // set the current item - making sure we don't store this
            pagerAdapter.setIsSaveToHistory(false);
            fragmentContainer.setCurrentItem(pagerAdapter.popHistory());
            pagerAdapter.setIsSaveToHistory(true);
            isHandled = true;
        }
        return isHandled;
    }



    @Override
    public void onBackPressed() {
        boolean isBackHandled = false;
        if (!handleBackPressed()) {
            // no back handling by our children / bases
            if (fragmentContainer.getCurrentItem() != 0) {
                // the last back should always go back home, without adding to the history
                pagerAdapter.setIsSaveToHistory(false);
                fragmentContainer.setCurrentItem(0);
                pagerAdapter.setIsSaveToHistory(true);
                isBackHandled = true;
            }
        }
        else {
            isBackHandled = true;
        }
        if (!isBackHandled) {
            // but don't let them back all the way out
            //super.onBackPressed();
        }
    }
}
