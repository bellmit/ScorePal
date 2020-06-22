package uk.co.darkerwaters.scorepal.controllers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import java.util.List;

import io.flic.flic2libandroid.Flic2Button;
import io.flic.flic2libandroid.Flic2ButtonListener;
import io.flic.flic2libandroid.Flic2Manager;
import io.flic.lib.FlicAppNotInstalledException;
import io.flic.lib.FlicBroadcastReceiver;
import io.flic.lib.FlicBroadcastReceiverFlags;
import io.flic.lib.FlicButton;
import io.flic.lib.FlicManager;
import io.flic.lib.FlicManagerInitializedCallback;
import io.flic.lib.FlicManagerUninitializedCallback;
import uk.co.darkerwaters.scorepal.activities.FlicPlayActivity;
import uk.co.darkerwaters.scorepal.application.Application;
import uk.co.darkerwaters.scorepal.application.GamePlayCommunicator;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.settings.SettingsControl;

public class FlicButtonBroadcastReceiver extends FlicBroadcastReceiver {

    private final static String K_FLIC_APP_ID = "fa0684f6-7d59-4102-8b4e-4793671f4f87";
    private final static String K_FLIC_APP_SEC = "9391b36a-22a4-4955-9783-4739c8d148bf";

    private static boolean IsFlic1Initialised = false;

    public interface FlicActivityInterface {
        void onFlicInitialised();
        void onFlicUninitialised();
        void onFlicNotInstalled(FlicAppNotInstalledException err);
    }

    @Override
    protected void onRequestAppCredentials() {
        // Set app credentials by calling FlicManager.setAppCredentials here
        SetupFlicCredentials();
    }

    private static void SetupFlicCredentials() {
        // initialise our controls with Flic
        FlicManager.setAppCredentials(K_FLIC_APP_ID, K_FLIC_APP_SEC, Log.K_APPLICATION);
    }

    public static void InitialiseFlic(Application application, FlicPlayActivity activity) {
        // just call the other one
        InitialiseFlic(application, activity, activity);
    }

    public static void InitialiseFlic(Application application, final Activity activity, final FlicActivityInterface listener ) {
        InitialiseFlic(false, application, activity, listener);
    }

    public static void InitialiseFlic(boolean isForceInitialisation, Application application, Activity activity, FlicActivityInterface listener ) {
        SettingsControl appSettings = new SettingsControl(application);
        if (appSettings.getIsControlUseFlic1()) {
            // we are to use Flic 1 - so initialise it here
            if (isForceInitialisation || !IsFlic1Initialised) {
                // we want flic but it isn't initialised, initialise it now
                InitialiseFlic1(activity, listener);
            }
        }
        else if (appSettings.getIsControlUseFlic2()) {
            // we are to use Flic 2 - this is initialised differently, do that here
            InitialiseFlic2(activity, listener);
        }
    }

    private static final Flic2ButtonListener ButtonListener = new Flic2ButtonListener() {
        @Override
        public void onButtonSingleOrDoubleClickOrHold(Flic2Button button, boolean wasQueued, boolean lastQueued, long timestamp, boolean isSingleClick, boolean isDoubleClick, boolean isHold) {
            super.onButtonSingleOrDoubleClickOrHold(button, wasQueued, lastQueued, timestamp, isSingleClick, isDoubleClick, isHold);
            // pass this button press from Flic2 on to our application
            if (wasQueued && button.getReadyTimestamp() - timestamp > 15000) {
                // Drop the event if it's more than 15 seconds old
                Log.info("Ignoring an old queued button press from Flic2");
            }
            else {
                // pass this on to the communicator
                HandleButtonClick(isSingleClick, isDoubleClick, isHold);
            }
        }
    };

    private static void InitialiseFlic2(Activity activity, FlicActivityInterface listener) {
        // Initialize the Flic2 manager to run on the same thread as the current thread (the main thread)
        Flic2Manager manager = Flic2Manager.initAndGetInstance(activity.getApplication().getApplicationContext(), new Handler());
        if (null != manager) {
            // Every time the app process starts, connect to all paired buttons and assign a click listener
            for (Flic2Button button : manager.getButtons()) {
                button.connect();
                // we might already be listening to this, try to remove it first
                button.removeListener(ButtonListener);
                // and add it back in to listen to each button only once.
                button.addListener(ButtonListener);
            }
        }
    }

    private static void InitialiseFlic1(final Activity activity, final FlicActivityInterface listener) {
        try {
            // be sure that our credentials are setup correctly
            SetupFlicCredentials();
            // and get the instance to grab the button
            FlicManager.getInstance(activity, new FlicManagerInitializedCallback() {
                @Override
                public void onInitialized(FlicManager manager) {
                    List<FlicButton> knownButtons = manager.getKnownButtons();
                    if (null == knownButtons || knownButtons.size() == 0) {
                        // we have no buttons, we want one though, grab one here
                        manager.initiateGrabButton(activity);
                    }
                    IsFlic1Initialised = true;
                    if (null != listener) {
                        listener.onFlicInitialised();
                    }
                }
            }, new FlicManagerUninitializedCallback() {
                @Override
                public void onUninitialized(FlicManager manager) {
                    super.onUninitialized(manager);
                    IsFlic1Initialised = false;
                    if (null != listener) {
                        listener.onFlicUninitialised();
                    }
                }
            });
        } catch (FlicAppNotInstalledException err) {
            IsFlic1Initialised = false;
            if (null != listener) {
                listener.onFlicNotInstalled(err);
            }
        }
    }

    public static void ReleaseFlic() {
        // release all the flic one things
        try {
            FlicManager.destroyInstance();
        }
        catch (Exception e) {
            Log.error("Failed to destroy the flic one instance", e);
        }
        IsFlic1Initialised = false;

        // and the flic 2
        Flic2Manager manager = null;
        try {
            manager = Flic2Manager.getInstance();
        }
        catch (Exception e) {
            Log.error("Failed to destroy the flic two instance as it was not initialised", e);
        }
        if (null != manager) {
            // we are probably listening to buttons, stop this
            for (Flic2Button button : manager.getButtons()) {
                try {
                    button.removeListener(ButtonListener);
                }
                catch (Exception e) {
                    Log.error("Failed to remove listener on releasing flic", e);
                }
            }
            try {
                manager.stopScan();
            }
            catch (Exception e) {
                Log.error("Failed to stop scan while releasing flic", e);
            }
        }
    }

    public static void HandleRequestResult(Activity activity, final int requestCode, final int resultCode, final Intent data) {
        if (null != activity) {
            FlicManager.getInstance(activity, new FlicManagerInitializedCallback() {
                @Override
                public void onInitialized(FlicManager manager) {
                    if (manager != null) {
                        FlicButton button = manager.completeGrabButton(requestCode, resultCode, data);
                        if (button != null) {
                            button.registerListenForBroadcast(FlicBroadcastReceiverFlags.CLICK_OR_DOUBLE_CLICK_OR_HOLD | FlicBroadcastReceiverFlags.REMOVED);
                            Log.info("Grabbed a flic button");
                        } else {
                            Log.info("failed to grab a flic button");
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onButtonSingleOrDoubleClickOrHold(Context context, FlicButton button, boolean wasQueued, int timeDiff, boolean isSingleClick, boolean isDoubleClick, boolean isHold) {
        super.onButtonSingleOrDoubleClickOrHold(context, button, wasQueued, timeDiff, isSingleClick, isDoubleClick, isHold);
        // pass this button press from Flic2 on to our application
        if (wasQueued && timeDiff > 15000) {
            // Drop the event if it's more than 15 seconds old
            Log.info("Ignoring an old queued button press from Flic1");
        }
        else {
            // pass this on to the communicator
            HandleButtonClick(isSingleClick, isDoubleClick, isHold);
        }
    }

    private static void HandleButtonClick(boolean isSingleClick, boolean isDoubleClick, boolean isHold) {
        GamePlayCommunicator activeCommunicator = GamePlayCommunicator.GetActiveCommunicator();
        if (null != activeCommunicator) {
            // inform the communicator that there was an action performed by Flic
            if (isSingleClick) {
                activeCommunicator.onControllerInputFlic(Controller.ControllerPattern.SingleClick);
            }
            if (isDoubleClick) {
                activeCommunicator.onControllerInputFlic(Controller.ControllerPattern.DoubleClick);
            }
            if (isHold) {
                activeCommunicator.onControllerInputFlic(Controller.ControllerPattern.LongClick);
            }
        }
    }

    @Override
    public void onButtonRemoved() {
        // Button was removed, whatever
    }
}
