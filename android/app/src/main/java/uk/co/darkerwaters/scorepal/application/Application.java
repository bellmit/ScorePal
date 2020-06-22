package uk.co.darkerwaters.scorepal.application;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.SizeF;

import java.io.IOException;
import java.io.InputStream;

public class Application extends android.app.Application {

    private Log log = null;

    @Override
    public void onCreate() {
        super.onCreate();

        // create the log and the settings so can access our state
        this.log = Log.CreateLog(this);

        Log.debug("Application initialised...");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // clear all the data we can to help out here
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
        this.log = null;

        // and terminate the app
        super.onTerminate();
    }

    public Log getLog() {
        return this.log;
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