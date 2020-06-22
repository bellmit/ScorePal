package uk.co.darkerwaters.scorepal.ui.matchplay;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;

import uk.co.darkerwaters.scorepal.application.ApplicationState;
import uk.co.darkerwaters.scorepal.application.MatchService;
import uk.co.darkerwaters.scorepal.application.ReportingService;
import uk.co.darkerwaters.scorepal.data.Match;
import uk.co.darkerwaters.scorepal.ui.login.ActivityLogin;

public class ActivityHelper {

    public interface ServiceBindListener {
        void onServiceConnected(MatchService service);
        void onServiceDisconnected();
    }

    private ServiceConnection serviceConnection = null;
    private BroadcastReceiver matchBroadcastReceiver = null;
    private boolean isServiceBound = false;

    private final Activity activity;

    public ActivityHelper(Activity activity) {
        this.activity = activity;
        // be sure the state is initialised
        ApplicationState.Initialise(activity);
    }

    public boolean checkApplicationState() {
        // if we are not logged in then show the login screen
        if (!ApplicationState.Instance().isLoggedIn()) {
            // we are logged in, show the main activity
            Intent intent = new Intent(activity, ActivityLogin.class);
            activity.startActivity(intent);
            return false;
        } else {
            // we are logged in, let's see if a service is running my pinging it
            isServiceBound = false;
            // close ny current state to start again
            closeApplicationState();
            matchBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // here you receive the response from the service
                    if (intent.getAction().equals(MatchService.ACTION_PONG)) {
                        // the service is running, let's bind to it right away
                        bindToGamePlayService(null);
                    }
                }
            };
            // quickly ping the match service to see if we can connect to any one still running
            activity.registerReceiver(matchBroadcastReceiver, new IntentFilter(MatchService.ACTION_PONG));
            // the service will respond to this broadcast only if it's running
            activity.sendBroadcast(new Intent(MatchService.ACTION_PING));
            // return that we are logged in though
            return true;
        }
    }

    public void closeApplicationState() {
        // stop listing for messages from the service
        if (null != matchBroadcastReceiver) {
            activity.unregisterReceiver(matchBroadcastReceiver);
            matchBroadcastReceiver = null;
        }
        // and unbind if we are bound to the service
        if (null != this.serviceConnection) {
            // release the binding service
            activity.unbindService(this.serviceConnection);
            this.serviceConnection = null;
        }
    }

    private synchronized void bindToGamePlayService(final ActivityHelper.ServiceBindListener listener) {
        this.serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                MatchService runningService = (MatchService)((ReportingService.LocalBinder) iBinder).getService();
                setGamePlayService(runningService);
                if (null != listener) {
                    listener.onServiceConnected(runningService);
                }
            }
            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                setGamePlayService(null);
                if (null != listener) {
                    listener.onServiceDisconnected();
                }
            }
        };
        // call to bind to this service
        activity.bindService(new Intent(activity, MatchService.class), this.serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private synchronized void setGamePlayService(MatchService newService) {
        // remember we bound in this activity
        isServiceBound = null != newService;
        // and remember the latest service that we know is running now
        MatchService.SetRunningService(newService);
        // the service is running, we are playing a match - show the match play activity then
        if (isServiceBound && false == activity instanceof ActivityPlayMatch) {
            // we have found a running match service, but we are not in the match screen
            // let's change this
            Intent intent = new Intent(activity, ActivityPlayMatch.class);
            activity.startActivity(intent);
        }
    }

    public void startMatchService(Match newMatch) {
        if (!isServiceBound) {
            // there wasn't one bound, create a new one then
            MatchService.CreateService(activity, newMatch);
            // the service will respond to this broadcast only if it's running and we will store it
            activity.sendBroadcast(new Intent(MatchService.ACTION_PING));
        } else {
            // the service is bound - so already exists, set the match on this
            MatchService matchService = MatchService.GetRunningService();
            if (null != matchService) {
                matchService.setActiveMatch(newMatch);
            }
        }
    }

    public static void StartNewMatch(Match match, Activity callingActivity) {
        // this is better - prepare the match
        MatchService.PrepareNewMatch(match.getSetup());
        // and kick off the service to play it
        MatchService.CreateService(callingActivity, match);
        // and show the activity to play this match
        Intent intent = new Intent(callingActivity, ActivityPlayMatch.class);
        callingActivity.startActivity(intent);
    }
}
