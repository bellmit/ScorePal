package uk.co.darkerwaters.scorepal.application;

import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;

public class ReportingService extends Service {
    /** if you broadcast a ping to the application, this will pong back */
    public static final String ACTION_PING = ReportingService.class.getName() + ".PING";
    /** a pong to send back in response to a ping so people know they can bind to us */
    public static final String ACTION_PONG = ReportingService.class.getName() + ".PONG";

    protected void closeService() {
        // stop the foreground service to end the notification
        stopForeground(true);
        // and stop ourselves
        stopSelf();
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public ReportingService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ReportingService.this;
        }
    }

    public ReportingService() {
        // construct this
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // broadcast out a pong in case people are listening for our alive-ness
        Application application = getApplication();
        if (null != application) {
            application.sendBroadcast(new Intent(ACTION_PONG));
        }
        // if we are killed, as android to start us again - keeping score pretty important
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    /**
     * the receiver for being pinged - so we respond accordingly
     */
    private BroadcastReceiver pingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive (Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_PING)) {
                // when we get pinged, respond wit a pong
                Application application = getApplication();
                if (null != application) {
                    // had a ping - let's pong back so they can bind to us
                    application.sendBroadcast(new Intent(ACTION_PONG));
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        // listen for pings so we can respond with a pong when we are running
        getApplication().registerReceiver(pingReceiver, new IntentFilter(ACTION_PING));
    }

    @Override
    public void onDestroy() {
        // stop responding to ping messages
        Application application = getApplication();
        if (null != application) {
            application.unregisterReceiver(pingReceiver);
        }
        // and destroy
        super.onDestroy();
    }
}
