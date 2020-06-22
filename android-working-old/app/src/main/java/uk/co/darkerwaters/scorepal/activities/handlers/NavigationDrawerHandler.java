package uk.co.darkerwaters.scorepal.activities.handlers;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import uk.co.darkerwaters.scorepal.activities.BaseActivity;
import uk.co.darkerwaters.scorepal.activities.BroadcastMatchActivity;
import uk.co.darkerwaters.scorepal.activities.AttributionsActivity;
import uk.co.darkerwaters.scorepal.activities.MatchTrashActivity;
import uk.co.darkerwaters.scorepal.activities.RemoteSetupActivity;
import uk.co.darkerwaters.scorepal.activities.StatisticsActivity;
import uk.co.darkerwaters.scorepal.application.Application;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.MainActivity;
import uk.co.darkerwaters.scorepal.activities.MatchHistoryActivity;
import uk.co.darkerwaters.scorepal.activities.MainSettingsActivity;
import uk.co.darkerwaters.scorepal.settings.Settings;

public class NavigationDrawerHandler extends ActionBarDrawerToggle
    /*GOOGLE SIGN-IN
    implements SignInHandler.SignInListener */ {

    private final NavigationView navigationView;
    private final Application application;
    private final BaseActivity parent;

    //private final SignInButton signInButton;
    private final ImageView imageView;
    private final TextView userNameTextView;

    public NavigationDrawerHandler(final BaseActivity parent, DrawerLayout drawer, Toolbar toolbar) {
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

        View header = this.navigationView.getHeaderView(0);
        this.imageView = header.findViewById(R.id.imageView);
        this.userNameTextView = header.findViewById(R.id.userNameTextView);

        /*GOOGLE SIGN-IN
        // Set the dimensions of the sign-in button.
        this.signInButton = header.findViewById(R.id.sign_in_button);
        this.signInButton.setSize(SignInButton.SIZE_STANDARD);
        this.signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // sign in to a new google account
                parent.signInToGoogle();
            }
        });
        */

        if (null != application) {
            Settings settings = this.application.getSettings();
            if (null != settings) {
                // set the name of the user the app remembers
                this.userNameTextView.setText(settings.getSelfName());
                this.imageView.setImageURI(Uri.parse(settings.getSelfImage()));
            }
        }

        // sync the state of the buttons
        syncState();
    }

    private void updateNavSelection(MenuItem item) {
        Intent myIntent;
        switch (item.getItemId()) {
            case R.id.nav_home:
                // go home
                myIntent = new Intent(this.parent, MainActivity.class);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                this.parent.startActivity(myIntent);
                break;
            case R.id.nav_opponent_stats:
                myIntent = new Intent(this.parent, StatisticsActivity.class);
                this.parent.startActivity(myIntent);
                break;
            case R.id.nav_history:
                myIntent = new Intent(this.parent, MatchHistoryActivity.class);
                this.parent.startActivity(myIntent);
                break;
            case R.id.nav_trash:
                myIntent = new Intent(this.parent, MatchTrashActivity.class);
                this.parent.startActivity(myIntent);
                break;
            case R.id.nav_import:
                this.parent.importMatchData();
                break;
            case R.id.nav_mediaController:
                myIntent = new Intent(this.parent, RemoteSetupActivity.class);
                this.parent.startActivity(myIntent);
                break;
            case R.id.nav_broadcastMatch:
                myIntent = new Intent(this.parent, BroadcastMatchActivity.class);
                this.parent.startActivity(myIntent);
                break;
            case R.id.nav_export:
                //myIntent = new Intent(this.container, AttributionsActivity.class);
                //this.container.startActivity(myIntent);
                break;
            case R.id.nav_attributions:
                myIntent = new Intent(this.parent, AttributionsActivity.class);
                this.parent.startActivity(myIntent);
                break;
            case R.id.nav_settings:
                myIntent = new Intent(this.parent, MainSettingsActivity.class);
                this.parent.startActivity(myIntent);
                break;
            default:
                // Handle navigation view item clicks here.
                break;
        }
        // close the drawer now the item is selected
        closeDrawer();
    }

    private void closeDrawer() {
        // close the drawer
        DrawerLayout drawer = this.parent.findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    /*GOOGLE SIGN-IN
    @Override
    public void showSignedInUI(GoogleSignInAccount account) {
        Application application = Application.getApplication(this.parent);
        Settings settings = application.getMatchSettings();
        if (account != null) {
            // get the details from the account
            String personName = account.getDisplayName();
            if (null != personName && false == personName.isEmpty()) {
                // set this up as the name we will use, unless they choose to override it
                settings.setSelfName(personName, false, this.parent);
            }
            String personGivenName = account.getGivenName();
            String personFamilyName = account.getFamilyName();
            String personEmail = account.getEmail();
            String personId = account.getId();
            final Uri personPhoto = account.getPhotoUrl();
            if (null != personPhoto) {
                // as accessing the internet we need to run the bitmap loading in a thread
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final Bitmap bitmap = Application.GetBitmapFromUrl(personPhoto.toString());
                        if (null != bitmap && null != parent) {
                            // get back to the main UI to set it
                            new Handler(parent.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if (null != imageView) {
                                        imageView.setImageBitmap(bitmap);
                                    }
                                }
                            });
                        }
                    }
                }).start();
            }
        }
        // and update the UI from the settings
        this.userNameTextView.setText(settings.getSelfName());
        this.signInButton.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showSignedOutUI() {
        Application application = Application.getApplication(this.parent);
        Settings settings = application.getMatchSettings();
        // and update the UI from the settings
        this.userNameTextView.setText(settings.getSelfName());
        this.imageView.setImageResource(R.drawable.ic_baseline_person_outline);
        this.signInButton.setVisibility(View.VISIBLE);
    }
    */
}
