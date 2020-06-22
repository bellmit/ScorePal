package uk.co.darkerwaters.scorepal.application;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.SizeF;

import java.io.IOException;
import java.io.InputStream;

import uk.co.darkerwaters.scorepal.activities.BaseActivity;
import uk.co.darkerwaters.scorepal.controllers.FlicButtonBroadcastReceiver;
import uk.co.darkerwaters.scorepal.score.MatchPersistenceManager;
import uk.co.darkerwaters.scorepal.settings.Settings;

public class Application extends android.app.Application {

    private Log log = null;

    private Settings settings = null;

    private Activity mainActivity = null;
    private Activity activeActivity = null;

    @Override
    public void onCreate() {
        super.onCreate();

        // create the log and the settings so can access our state
        this.log = Log.CreateLog(this);
        this.settings = new Settings(this);

        Log.debug("Application initialised...");

        // be sure any active notification is dead
        GamePlayNotification.KillOldNotifications(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // clear all the data we can to help out here
        MatchPersistenceManager.GetInstance().clearCache();
        BaseActivity.ClearResources();
    }

    public static Application getApplication(Activity context) {
        android.app.Application baseApp = context.getApplication();
        if (baseApp instanceof Application) {
            return (Application) baseApp;
        }
        else {
            return null;
        }
    }

    public static Application getApplication(Service context) {
        android.app.Application baseApp = context.getApplication();
        if (baseApp instanceof Application) {
            return (Application) context.getApplication();
        }
        else {
            return null;
        }
    }

    public static SizeF getDisplaySize(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return new SizeF(dpWidth, dpHeight);
    }

    @Override
    public void onTerminate() {
        // close things down
        Log.debug("Application terminated...");
        // set everything to null, no longer around
        this.settings = null;
        this.log = null;
        this.activeActivity = null;
        this.mainActivity = null;

        // kill the communicator
        GamePlayCommunicator.ActivateCommunicator(null);

        // and any notification
        GamePlayNotification.KillOldNotifications(this);

        // release all our flic stuff here
        FlicButtonBroadcastReceiver.ReleaseFlic();

        // and terminate the app
        super.onTerminate();
    }

    public Log getLog() {
        return this.log;
    }

    public Settings getSettings() {
        // return the settings (exist as long as the application does)
        return this.settings;
    }

    public void setMainActivity(Activity activity) {
        // set the activity to use to set things up
        this.mainActivity = activity;
        new Thread(new Runnable() {
            @Override
            public void run() {
                // on the first activity (this one) check for excessive files
                MatchPersistenceManager.GetInstance().checkForExcessiveFiles(Application.this);
            }
        }).start();
    }

    public Activity getMainActivity() {
        return this.mainActivity;
    }

    public Activity getActiveActivity() {
        return this.activeActivity;
    }

    public void setActiveActivity(BaseActivity activity) {
        if (null == this.mainActivity) {
            setMainActivity(activity);
        }
        // set the new active activity
        this.activeActivity = activity;
        // as we change activities, we should just check our play service
        // should still be running. We don't use onDestroy etc as we want
        // the service to hang around to listen for keypresses for points
        GamePlayCommunicator.ActivateCommunicator(activity);
    }

    public void activityDestroyed(Activity activity) {
        // clear the pointers as they go away
        if (this.mainActivity == activity) {
            this.mainActivity = null;
        }
        if (this.activeActivity == activity) {
            this.activeActivity = null;
        }
    }

    public static Bitmap GetBitmapFromAssets(String fileName, Context context) {
        // Custom method to get assets folder image as bitmap
        AssetManager am = context.getAssets();
        InputStream is = null;
        try{
            is = am.open(fileName);
        }catch(IOException e){
            e.printStackTrace();
        }
        return BitmapFactory.decodeStream(is);
    }

    public static Bitmap GetBitmapFromUrl(String url) {
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(url).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.error("Failed to decode image", e);
            e.printStackTrace();
        }
        return mIcon11;
    }
}