package uk.co.darkerwaters.scorepal.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.flic.lib.FlicBroadcastReceiverFlags;
import io.flic.lib.FlicButton;
import io.flic.lib.FlicManager;
import io.flic.lib.FlicManagerInitializedCallback;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.handlers.NavigationDrawerHandler;
import uk.co.darkerwaters.scorepal.activities.handlers.ResourceManager;
import uk.co.darkerwaters.scorepal.application.Application;
import uk.co.darkerwaters.scorepal.application.GamePlayService;
import uk.co.darkerwaters.scorepal.controllers.FlicButtonBroadcastReceiver;
import uk.co.darkerwaters.scorepal.score.MatchPersistenceManager;

import static io.flic.lib.FlicManager.GRAB_BUTTON_REQUEST_CODE;

public abstract class BaseActivity extends AppCompatActivity {

    public static final String K_OPEN_DRAWER = "open_drawer";

    private static final int FILE_CHOSEN = 141;

    protected Application application;

    private NavigationDrawerHandler navigationActor = null;
    private ServiceConnection gamePlayServiceConnection = null;
    private GamePlayService service = null;

    private static ResourceManager resources = null;
    private final static Object ResourceLock = new Object();

    public interface ServiceBindListener {
        void onServiceConnected(GamePlayService service);
        void onServiceDisconnected();
    }

    public static void ClearResources() {
        synchronized (ResourceLock) {
            if (null != resources) {
                resources.close();
                resources = null;
            }
        }
    }

    public static String JSONToString(JSONObject json) throws JSONException {
        return json.toString();
    }

    public static String JSONToString(JSONArray json) throws JSONException {
        return json.toString();
    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive (Context context, Intent intent) {
            // here you receive the response from the service
            if (intent.getAction().equals(GamePlayService.ACTION_PONG)) {
                // the service is running, let's bind to it right away
                bindToGamePlayService(null);
            }
        }
    };

    public synchronized GamePlayService getGamePlayService() {
        return this.service;
    }

    public void createGamePlayService(ServiceBindListener listener) {
        // start the service for the first time
        Intent serviceIntent = new Intent(this, GamePlayService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
        // and bind to the service right away to get it
        bindToGamePlayService(listener);
    }

    private synchronized void bindToGamePlayService(final ServiceBindListener listener) {
        this.gamePlayServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                BaseActivity.this.setGamePlayService(((GamePlayService.LocalBinder) iBinder).getService());
                if (null != listener) {
                    listener.onServiceConnected(BaseActivity.this.service);
                }
            }
            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                BaseActivity.this.setGamePlayService(null);
                if (null != listener) {
                    listener.onServiceDisconnected();
                }
            }
        };
        // call to bind to this service
        bindService(new Intent(BaseActivity.this, GamePlayService.class), this.gamePlayServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private synchronized void setGamePlayService(GamePlayService newService) {
        this.service = newService;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setup our pointers to each other so we an short-cut about the app
        this.application = Application.getApplication(this);
        createResources();

        // as we are created, let's see if the GamePlayService is active
        registerReceiver(mReceiver, new IntentFilter(GamePlayService.ACTION_PONG));
        // the service will respond to this broadcast only if it's running
        sendBroadcast(new Intent(GamePlayService.ACTION_PING));
    }

    private void createResources() {
        synchronized (ResourceLock) {
            if (null == resources) {
                resources = new ResourceManager();
            }
            resources.create(this);
        }
    }

    protected void setupActivity(int titleStringId) {
        // set the title
        setTitle(titleStringId);
        // and setup the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (null != toolbar) {
            setSupportActionBar(toolbar);
        }

        // create the nav listener
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (null != drawer) {
            // there is a menu drawer, handle this on this activity please
            this.navigationActor = new NavigationDrawerHandler(this, drawer, toolbar);
        }
        else {
            // no drawer, enable the back button instead
            ActionBar supportActionBar = getSupportActionBar();
            if (null != supportActionBar) {
                supportActionBar.setDisplayHomeAsUpEnabled(true);
            }
        }

        // if we are sent a message to open the drawer, open it
        boolean isOpenDrawer = this.getIntent().getBooleanExtra(K_OPEN_DRAWER, false);
        if (null != drawer && isOpenDrawer) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawer.openDrawer(GravityCompat.START, true);
                }
            }, 250);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case FILE_CHOSEN:
                if (null != data) {
                    String importedMatchId = MatchPersistenceManager.GetInstance().importMatchData(application, this, data.getData());
                    if (null != importedMatchId && !importedMatchId.isEmpty()) {
                        // tell the user that this worked
                        Toast.makeText(this, String.format(getString(R.string.successful_import), importedMatchId), Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public void importMatchData() {
        // import our match data by showing the file chooser intent to select a file to import
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //sets the select file to all types of files
        intent.setType("*/*");
        // Only get openable files
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        //starts new activity to select file and return data
        startActivityForResult(Intent.createChooser(intent,
                "Choose File to Upload.."), FILE_CHOSEN);
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context){
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static void setupButtonIcon(Button button, int drawableIdStart, int drawableIdEnd) {
        resources.setDrawables(button.getContext(), button, drawableIdStart, drawableIdEnd);
    }

    public static void setupButtonIcon(Button button, int drawableIdStart, int drawableIdEnd, int colorId) {
        resources.setDrawables(button.getContext(), button, drawableIdStart, drawableIdEnd, colorId);
    }

    public static void setupButtonIcon(ImageButton button, int drawableId) {
        resources.setDrawable(button.getContext(), button, drawableId);
    }

    public static void setupButtonIcon(ImageButton button, int drawableId, int colorId) {
        resources.setDrawable(button.getContext(), button, drawableId, colorId);
    }

    public static void setupButtonIcon(ImageView button, int drawableId) {
        resources.setDrawable(button.getContext(), button, drawableId);
    }

    public static void setupButtonIcon(ImageView button, int drawableId, int colorId) {
        resources.setDrawable(button.getContext(), button, drawableId, colorId);
    }

    public static void setupButtonIcon(Context context, MenuItem item, int drawableId) {
        resources.setDrawable(context, item, drawableId);
    }

    @Override
    protected void onResume() {
        // set this on the application
        if (null != this.application) {
            this.application.setActiveActivity(this);
        }
        // create any required resources
        createResources();
        // resume this activity
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // unregister our receiver
        this.unregisterReceiver(mReceiver);
        synchronized (this) {
            if (null != this.gamePlayServiceConnection) {
                // have called bind, call unbind
                unbindService(this.gamePlayServiceConnection);
                this.gamePlayServiceConnection = null;
            }
        }
        // tell the application this
        if (null != this.application) {
            this.application.activityDestroyed(this);
        }
        // and destroy us
        super.onDestroy();
    }

    public static void setTextViewBold(TextView textView) {
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
    }

    public static void setTextViewNoBold(TextView textView) {
        textView.setTypeface(textView.getTypeface(), Typeface.NORMAL);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
