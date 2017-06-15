package uk.co.darkerwaters.scorepal.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.bluetooth.BtConnectActivity;
import uk.co.darkerwaters.scorepal.bluetooth.BtManager;
import uk.co.darkerwaters.scorepal.storage.StorageManager;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "ScorePal";

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialise the Bluetooth manager
        BtManager.initialise(this);

        // setup the navigation bar things
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();

        // setup the navigation drawer
        setupDrawer();

        // configure all the google stuff in the storage manager
        StorageManager storageManager = StorageManager.getManager();
        // and initialise the manager
        storageManager.initialise(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // unregister our listeners
        BtManager.getManager().unregisterGlobalListeners();
        super.onDestroy();
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(R.string.title_activity_navigation);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
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

    public void onDeviceScore(View view) {
        Intent intent = new Intent(getApplicationContext(), ScoreActivity.class);
        startActivity(intent);
    }

    public void onManualScore(View view) {
        Toast.makeText(this, "Sorry not done this yet...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // process the option clicked in the menu bar
        if (item.getItemId() == android.R.id.home) {
            // pressed the home button, ... or < to show/hide the drawer
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                // close the drawer
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else {
                // open the drawer
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        else {
            // try to process the item
            if (processNavigationItem(item)) {
                // this is handled
                return true;
            }
            else {
                // failed to process this, let the base try
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private boolean processNavigationItem(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent;

        switch (item.getItemId()) {
            case R.id.nav_device:
                intent = new Intent(getApplicationContext(), BtConnectActivity.class);
                startActivity(intent);
                return true;
            case R.id.nav_history:
                intent = new Intent(getApplicationContext(), HistoryActivity.class);
                startActivity(intent);
                return true;
            case R.id.nav_account:
                intent = new Intent(getApplicationContext(), AccountActivity.class);
                startActivity(intent);
                return true;
            case R.id.nav_share:
                Toast.makeText(this, "Sorry not done this yet...", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.nav_send:
                Toast.makeText(this, "Sorry not done this yet...", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        processNavigationItem(item);
        // and close the drawer
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
