package uk.co.darkerwaters.scorepal.ui.matchplay;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import io.flic.lib.FlicManager;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.ApplicationState;
import uk.co.darkerwaters.scorepal.application.MatchService;
import uk.co.darkerwaters.scorepal.controllers.Flic1Controller;
import uk.co.darkerwaters.scorepal.data.Match;
import uk.co.darkerwaters.scorepal.ui.PagingFragmentAdapter;
import uk.co.darkerwaters.scorepal.ui.PermissionsHandler;
import uk.co.darkerwaters.scorepal.ui.appsettings.FragmentAppSettingsControls;
import uk.co.darkerwaters.scorepal.ui.appsettings.FragmentAppSettingsGeneral;
import uk.co.darkerwaters.scorepal.ui.appsettings.FragmentAppSettingsSounds;
import uk.co.darkerwaters.scorepal.ui.views.CustomSnackbar;

public class ActivityPlayMatch extends AppCompatActivity
        implements PermissionsHandler.Container, PagingFragmentAdapter.BackPressedHandler<Fragment> {

    private Match activeMatch;
    private PermissionsHandler permissionsHandler = null;

    private BottomNavigationView navBar;

    private ActivityHelper activityHelper = null;
    private MatchService activeMatchService;

    private ViewPager2 fragmentContainer;
    private PagingFragmentAdapter<Fragment> pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_match);

        this.activityHelper = new ActivityHelper(this);
        this.permissionsHandler = new PermissionsHandler(this);

        // and the container for this, with the adapter that manages their display
        fragmentContainer = findViewById(R.id.fragment_container);

        // keep the screen on all the time they are playing the match
        setKeepScreenOn(true);

        // to be here, the service should be started
        this.activeMatchService = MatchService.GetRunningService();
        this.activeMatch = this.activeMatchService == null ? null : this.activeMatchService.getActiveMatch();

        if (null == activeMatch) {
            finish();
        }
        else {
            // setup the paging adapter etc for this page
            pagerAdapter = new PagingFragmentAdapter<>(getSupportFragmentManager(),
                    getLifecycle(), fragmentContainer,
                    new Class[] {
                        activeMatch.getSport().playFragmentClass,
                        FragmentPlayHistory.class,
                        activeMatch.getSport().setupFragmentClass,
                        FragmentAppSettingsGeneral.class,
                        FragmentAppSettingsSounds.class,
                        FragmentAppSettingsControls.class,
                    });
            // and set this adapter on the container
            fragmentContainer.setAdapter(pagerAdapter);
            navBar = findViewById(R.id.nav_view);
            // setup the mapping of IDs to pages here
            pagerAdapter.setupNavBarToViewPagerMapping(
                    navBar,
                    fragmentContainer,
                    new int[] {
                        R.id.navigation_play,
                        R.id.navigation_play_history,
                        R.id.navigation_play_setup,
                        R.id.navigation_main_setup,
                        R.id.navigation_main_setup,
                        R.id.navigation_main_setup,
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // if we are not logged in then show the login screen
        this.activityHelper.checkApplicationState();
        // initialise our local controllers here if we want to
        if (ApplicationState.Initialise(this).getPreferences().getIsControlFlic1()) {
            // initialise FLIC buttons to listen to this activity
            Flic1Controller.Initialise(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PermissionsHandler.PERMISSIONS_REQUEST) {
            // pass this to the handler
            this.permissionsHandler.processPermissionsResult(permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected void setKeepScreenOn(boolean keepScreenOn) {
        if (keepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        else {
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    public void changeSettings(int settingsNavId) {
        switch (settingsNavId) {
            case R.id.nav_app_settings_general:
                fragmentContainer.setCurrentItem(3);
                break;
            case R.id.nav_app_settings_sounds:
                fragmentContainer.setCurrentItem(4);
                break;
            case R.id.nav_app_settings_controls:
                fragmentContainer.setCurrentItem(5);
                break;
            default:
                // just jump to the first one
                fragmentContainer.setCurrentItem(3);
                break;
        }
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
        Fragment fragment = pagerAdapter.getFragment(this, fragmentContainer.getCurrentItem());
        if (fragment instanceof PagingFragmentAdapter.BackPressedHandler) {
            // this fragment is special as it has a container of it's own, do the back history for
            // this instead first
            isBackHandled = ((PagingFragmentAdapter.BackPressedHandler) fragment).handleBackPressed();
        }
        if (!isBackHandled) {
            // try doing this ourselves
            isBackHandled = handleBackPressed();
        }
        if (!isBackHandled) {
            if (fragmentContainer.getCurrentItem() != 0) {
                // the last back should always go back home, without adding to the history
                pagerAdapter.setIsSaveToHistory(false);
                fragmentContainer.setCurrentItem(0);
                pagerAdapter.setIsSaveToHistory(true);
            }
            else {
                // no back handling by our children / bases, let's actually go back then (warning them first)
                new CustomSnackbar(this,
                        R.string.exitConfirmDetail,
                        R.drawable.ic_help_black_24dp,
                        R.string.yes, R.string.no,
                        new CustomSnackbar.SnackbarListener() {
                            @Override
                            public void onButtonOnePressed() {
                                //Yes button clicked
                                cancelMatchAndGoBack();
                            }

                            @Override
                            public void onButtonTwoPressed() {
                                // no, do nothing
                            }

                            @Override
                            public void onDismissed() {
                                // closed, do nothing
                            }
                        });
            }
        }
    }

    private void cancelMatchAndGoBack() {
        // cancel the running service
        this.activeMatchService.cancelMatch(!this.activeMatch.isMatchPlayStarted());
        // call the super version of back to not ask they user if they want to again
        ActivityPlayMatch.super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // let the base have it
        super.onActivityResult(requestCode, resultCode, data);
        // and if flic is wanted deal with that here too
        if (requestCode == FlicManager.GRAB_BUTTON_REQUEST_CODE) {
            // and if flic is wanted deal with that here too
            Flic1Controller.Initialise(this).handleRequestResult(this, requestCode, resultCode, data);
        }
    }

    @Override
    protected void onPause() {
        // we are paused, pause our helper too
        this.activityHelper.closeApplicationState();

        // don't release FLIC - we want to carry on listening to the button presses

        // and pause this activity
        super.onPause();
    }

    @Override
    public PermissionsHandler getPermissionsHandler() {
        return this.permissionsHandler;
    }
}
