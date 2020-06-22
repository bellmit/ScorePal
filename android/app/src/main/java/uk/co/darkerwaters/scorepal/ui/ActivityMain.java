package uk.co.darkerwaters.scorepal.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import io.flic.lib.FlicManager;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.ApplicationPreferences;
import uk.co.darkerwaters.scorepal.application.ApplicationState;
import uk.co.darkerwaters.scorepal.controllers.Flic1Controller;
import uk.co.darkerwaters.scorepal.points.Sport;
import uk.co.darkerwaters.scorepal.ui.apphome.FragmentHome;
import uk.co.darkerwaters.scorepal.ui.apphome.FragmentMainHistory;
import uk.co.darkerwaters.scorepal.ui.appsettings.FragmentAppSettingsControls;
import uk.co.darkerwaters.scorepal.ui.appsettings.FragmentAppSettingsGeneral;
import uk.co.darkerwaters.scorepal.ui.appsettings.FragmentAppSettingsSounds;
import uk.co.darkerwaters.scorepal.ui.matchplay.ActivityHelper;
import uk.co.darkerwaters.scorepal.ui.matchsetup.FragmentSetupBadminton;
import uk.co.darkerwaters.scorepal.ui.matchsetup.FragmentSetupMatch;
import uk.co.darkerwaters.scorepal.ui.matchsetup.FragmentSetupPingPong;
import uk.co.darkerwaters.scorepal.ui.matchsetup.FragmentSetupTennis;
import uk.co.darkerwaters.scorepal.ui.views.CustomSnackbar;

public class ActivityMain extends AppCompatActivity
        implements PermissionsHandler.Container,
                    PagingFragmentAdapter.BackPressedHandler<Fragment> {

    public static final String INITIAL_FRAGEMENT = "INITIAL_NAVID";

    private ActivityHelper activityHelper = null;
    private PermissionsHandler permissionsHandler = null;
    private BottomNavigationView navBar;
    private ViewPager2 fragmentContainer;
    private PagingFragmentAdapter<Fragment> pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.activityHelper = new ActivityHelper(this);
        this.permissionsHandler = new PermissionsHandler(this);

        navBar = findViewById(R.id.nav_view);
        fragmentContainer = findViewById(R.id.nav_host_fragment);

        pagerAdapter = new PagingFragmentAdapter<>(getSupportFragmentManager(),
                getLifecycle(), fragmentContainer,
            new Class [] {
                FragmentHome.class,
                FragmentSetupTennis.class,
                FragmentSetupBadminton.class,
                FragmentSetupPingPong.class,
                FragmentMainHistory.class,
                FragmentAppSettingsGeneral.class,
                FragmentAppSettingsSounds.class,
                FragmentAppSettingsControls.class,
            });
        pagerAdapter.addListener(this);
        // and set this as the adapter on the container
        fragmentContainer.setAdapter(pagerAdapter);

        // setup the mapping from nav bar to the view pager here
        pagerAdapter.setupNavBarToViewPagerMapping(
                navBar,
                fragmentContainer,
                new int[] {
                    R.id.navigation_home,
                    R.id.navigation_record,
                    R.id.navigation_record,
                    R.id.navigation_record,
                    R.id.navigation_main_history,
                    R.id.navigation_settings,
                    R.id.navigation_settings,
                    R.id.navigation_settings,
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // if we are not logged in then show the login screen
        this.activityHelper.checkApplicationState();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            int navId = bundle.getInt(INITIAL_FRAGEMENT, 0);
            if (navId == R.id.navigation_record) {
                // we are to jump to a sport - let's use the default
                playNewMatch(ApplicationState.Instance().getPreferences().getLastSport());
            }
            else {
                // jump to what we were told to
                navBar = findViewById(R.id.nav_view);
                if (navId != 0 && navBar != null) {
                    // user chose to show a particular fragment
                    navBar.setSelectedItemId(navId);
                }
            }
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
            isBackHandled = ((PagingFragmentAdapter.BackPressedHandler)fragment).handleBackPressed();
        }
        if (!isBackHandled) {
            // try doing this ourselves
            isBackHandled = handleBackPressed();
        }
        if (!isBackHandled) {
            // no back handling by our children / bases
            if (fragmentContainer.getCurrentItem() != 0) {
                // the last back should always go back home, without adding to the history
                pagerAdapter.setIsSaveToHistory(false);
                fragmentContainer.setCurrentItem(0);
                pagerAdapter.setIsSaveToHistory(true);
            }
            else {
                // let's actually go back then
                super.onBackPressed();
                /*
                //TODO or stop the exit of the application by accident?
                new CustomSnackbar(this,
                        R.string.exitMainConfirmDetail,
                        R.drawable.ic_help_black_24dp,
                        R.string.yes, R.string.no,
                        new CustomSnackbar.SnackbarListener() {
                            @Override
                            public void onButtonOnePressed() {
                                //Yes button clicked
                                ActivityMain.super.onBackPressed();
                            }
                            @Override
                            public void onButtonTwoPressed() {
                                // no, do nothing
                            }
                            @Override
                            public void onDismissed() {
                                // closed, do nothing
                            }
                        });*/
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // we are paused, pause our helper too
        this.activityHelper.closeApplicationState();
    }

    public void playNewMatch(Sport sport) {
        // let's try to find the fragment for this sport
        switch (sport) {
            case TENNIS:
                fragmentContainer.setCurrentItem(1);
                break;
            case BADMINTON:
                fragmentContainer.setCurrentItem(2);
                break;
            case PINGPONG:
                fragmentContainer.setCurrentItem(3);
                break;
            default:
                // show the fragment to start setting up something (unknown sport)
                navBar.setSelectedItemId(R.id.navigation_record);
                break;
        }
    }

    public void changeSettings(int settingsNavId) {
        switch (settingsNavId) {
            case R.id.nav_app_settings_general:
                fragmentContainer.setCurrentItem(5);
                break;
            case R.id.nav_app_settings_sounds:
                fragmentContainer.setCurrentItem(6);
                break;
            case R.id.nav_app_settings_controls:
                fragmentContainer.setCurrentItem(7);
                break;
            default:
                // just jump to the first one
                fragmentContainer.setCurrentItem(5);
                break;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // let the base have it
        super.onActivityResult(requestCode, resultCode, data);
        // and if flic is wanted deal with that here too
        if (requestCode == FlicManager.GRAB_BUTTON_REQUEST_CODE) {
            // and if flic1 is wanted deal with that here too
            Flic1Controller flic1Controller = Flic1Controller.Instance();
            if (null != flic1Controller) {
                flic1Controller.handleRequestResult(this, requestCode, resultCode, data);
            }
        }
    }

    @Override
    public PermissionsHandler getPermissionsHandler() {
        return this.permissionsHandler;
    }
}
