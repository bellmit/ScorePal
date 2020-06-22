package uk.co.darkerwaters.scorepal.activities.handlers;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ActionProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import uk.co.darkerwaters.scorepal.activities.BroadcastMatchActivity;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.MainSettingsActivity;
import uk.co.darkerwaters.scorepal.activities.PlayActivity;
import uk.co.darkerwaters.scorepal.activities.RemoteSetupActivity;
import uk.co.darkerwaters.scorepal.application.Application;
import uk.co.darkerwaters.scorepal.application.GamePlayCommunicator;
import uk.co.darkerwaters.scorepal.settings.Settings;
import uk.co.darkerwaters.scorepal.settings.SettingsSounds;
import uk.co.darkerwaters.scorepal.score.base.Sport;

public class MatchPlayNavigationHandler extends ActionBarDrawerToggle {

    private final NavigationView navigationView;
    private final Application application;
    private final PlayActivity parent;

    public MatchPlayNavigationHandler(PlayActivity parent, DrawerLayout drawer, Toolbar toolbar) {
        // setup this handler to manage the app drawer
        super(parent, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        // remember the app
        this.parent = parent;
        this.application = Application.getApplication(this.parent);

        // add a listener to the draw so we can respond to button presses
        drawer.addDrawerListener(this);

        // find the view that we act upon and listen for selection changes
        this.navigationView = parent.findViewById(R.id.nav_view);
        this.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // update the selection
                updateNavSelection(menuItem);
                return true;
            }
        });

        // sync the state of the buttons
        syncState();
    }

    public void setHeaderDisplay(Sport sport) {
        View header = this.navigationView.getHeaderView(0);
        if (null != header) {
            ImageView image = header.findViewById(R.id.headerSportImage);
            image.setImageBitmap(Application.GetBitmapFromAssets(sport.imageFilename, this.parent));

            TextView textView = header.findViewById(R.id.headerSportTitle);
            textView.setText(sport.titleResId);
        }
    }

    public void updateMenuItem(int menuId, int resString, int resImage, int resColor) {
        MenuItem item = this.navigationView.getMenu().findItem(menuId);
        if (null != item) {
            item.setIcon(resImage);
            item.setTitle(resString);
            //BaseActivity.SetIconTint(item.getIcon(), this.container.getColor(resColor));

            Drawable drawable = item.getIcon();
            drawable.mutate();
            drawable.setColorFilter(parent.getColor(resColor), PorterDuff.Mode.SRC_ATOP);

            drawable = DrawableCompat.wrap(drawable);
            //DrawableCompat.setTint(drawable, container.getColor(resColor));
            item.setIcon(drawable);
        }
    }

    public void setMenuItemVisible(int menuId, boolean isVisible) {
        MenuItem item = this.navigationView.getMenu().findItem(menuId);
        if (null != item) {
            item.setVisible(isVisible);
        }
    }


    @Override
    public void onDrawerOpened(View drawerView) {
        super.onDrawerOpened(drawerView);

        Settings settings = this.application.getSettings();
        Menu menu = this.navigationView.getMenu();

        final SettingsSounds soundsSettings = new SettingsSounds(this.application);
        if (!soundsSettings.getIsMakingBeepingSounds()) {
            menu.findItem(R.id.nav_soundBeeps).setIcon(R.drawable.ic_baseline_volume_off);
        }
        if (!soundsSettings.getIsMakingSoundingAction()) {
            menu.findItem(R.id.nav_soundActions).setIcon(R.drawable.ic_baseline_volume_off);
        }
        if (!soundsSettings.getIsSpeakingPoints()) {
            menu.findItem(R.id.nav_soundPoints).setIcon(R.drawable.ic_baseline_volume_off);
        }
        if (!soundsSettings.getIsSpeakingMessages()) {
            menu.findItem(R.id.nav_soundMessages).setIcon(R.drawable.ic_baseline_volume_off);
        }

        MenuItem mediaItem = menu.findItem(R.id.nav_mediavolume);
        ActionProvider actionProvider = MenuItemCompat.getActionProvider(mediaItem);
        if (actionProvider instanceof ControllerSliderHandler) {
            // set the position of the slider
            ControllerSliderHandler sliderHandler = (ControllerSliderHandler)actionProvider;
            View actionView = mediaItem.getActionView();
            sliderHandler.setSliderPosition(actionView, soundsSettings.getMaxMediaVolume(drawerView.getContext()), soundsSettings.getMediaVolume());
        }

        GamePlayCommunicator communicator = GamePlayCommunicator.GetActiveCommunicator();
        if (null == communicator || !communicator.getIsBroadcastingMatch()) {
            menu.findItem(R.id.nav_broadcastMatch).setIcon(R.drawable.ic_baseline_bluetooth_disabled);
            menu.findItem(R.id.nav_broadcastMatch).setChecked(true);
        }
        // setup the action menu item selections
        setupActionProvider(menu.findItem(R.id.nav_broadcastMatch), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(parent, BroadcastMatchActivity.class);
                parent.startActivity(myIntent);
            }
        });
        // setup the action menu item selections
        setupActionProvider(mediaItem, new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // set the media volume on the settings
                soundsSettings.setMediaVolume(i);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void setupActionProvider(MenuItem item, View.OnClickListener clickHandler) {
        if (null != item) {
            ActionProvider actionProvider = MenuItemCompat.getActionProvider(item);
            if (actionProvider instanceof ControllerSettingsHandler) {
                // handle the clicking of this settings button
                View actionView = item.getActionView();
                ControllerSettingsHandler inputOptionSettingsHandler = (ControllerSettingsHandler) actionProvider;
                inputOptionSettingsHandler.setOnClickListener(actionView, clickHandler);
            }
        }
    }

    private void setupActionProvider(MenuItem item, SeekBar.OnSeekBarChangeListener clickHandler) {
        if (null != item) {
            ActionProvider actionProvider = MenuItemCompat.getActionProvider(item);
            if (actionProvider instanceof ControllerSliderHandler) {
                // handle the clicking of this settings button
                View actionView = item.getActionView();
                ControllerSliderHandler inputOptionSliderHandler = (ControllerSliderHandler) actionProvider;
                inputOptionSliderHandler.setOnClickListener(actionView, clickHandler);
            }
        }
    }

    private void updateNavSelection(MenuItem item) {
        boolean newValue;
        SettingsSounds soundsSettings = new SettingsSounds(this.application);
        switch (item.getItemId()) {
            case R.id.nav_startStopMatch:
                this.parent.stopPlayMatch(true);
                closeDrawer();
                break;
            case R.id.nav_changeEnds:
                this.parent.changeEnds();
                break;
            case R.id.nav_changeServer:
                this.parent.changeServer();
                break;
            case R.id.nav_changeStarter:
                this.parent.changeStarter();
                break;
            case R.id.nav_undoLastPoint:
                this.parent.undoLastPoint();
                break;
            case R.id.nav_matchSettings:
                this.parent.showMatchSettings();
                break;
            case R.id.nav_lock:
                this.parent.lockUnlockActivity(item);
                break;
            case R.id.nav_mediaController:
                Intent myIntent = new Intent(parent, RemoteSetupActivity.class);
                parent.startActivity(myIntent);
                break;
            case R.id.nav_broadcastMatch:
                GamePlayCommunicator communicator = GamePlayCommunicator.GetActiveCommunicator();
                if (null != communicator) {
                    newValue = !communicator.getIsBroadcastingMatch();
                    communicator.setIsBroadcastingMatch(newValue);
                    item.setIcon(newValue ? R.drawable.ic_baseline_bluetooth_searching : R.drawable.ic_baseline_bluetooth_disabled);
                    item.setChecked(!newValue);
                }
                break;
            case R.id.nav_soundBeeps:
                newValue = !soundsSettings.getIsMakingBeepingSounds();
                soundsSettings.setIsMakingBeepingSounds(newValue);
                item.setIcon(newValue ? R.drawable.ic_baseline_volume_up : R.drawable.ic_baseline_volume_off);
                break;
            case R.id.nav_soundActions:
                newValue = !soundsSettings.getIsMakingSoundingAction();
                soundsSettings.setIsMakingSoundingAction(newValue);
                soundsSettings.setIsMakingSoundSpeakingAction(newValue);
                item.setIcon(newValue ? R.drawable.ic_baseline_volume_up : R.drawable.ic_baseline_volume_off);
                break;
            case R.id.nav_soundPoints:
                newValue = !soundsSettings.getIsSpeakingPoints();
                soundsSettings.setIsSpeakingPoints(newValue);
                item.setIcon(newValue ? R.drawable.ic_baseline_volume_up : R.drawable.ic_baseline_volume_off);
                break;
            case R.id.nav_soundMessages:
                newValue = !soundsSettings.getIsSpeakingMessages();
                soundsSettings.setIsSpeakingMessages(newValue);
                item.setIcon(newValue ? R.drawable.ic_baseline_volume_up : R.drawable.ic_baseline_volume_off);
                break;
            case R.id.nav_settings:
                myIntent = new Intent(this.parent, MainSettingsActivity.class);
                this.parent.startActivity(myIntent);
                break;
        }
    }

    private void closeDrawer() {
        // close the drawer
        DrawerLayout drawer = this.parent.findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }
}
