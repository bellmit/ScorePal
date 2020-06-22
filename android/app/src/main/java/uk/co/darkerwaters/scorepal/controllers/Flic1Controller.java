package uk.co.darkerwaters.scorepal.controllers;

import android.app.Activity;
import android.content.Intent;

import java.util.List;

import io.flic.lib.FlicAppNotInstalledException;
import io.flic.lib.FlicBroadcastReceiverFlags;
import io.flic.lib.FlicButton;
import io.flic.lib.FlicManager;
import io.flic.lib.FlicManagerInitializedCallback;
import io.flic.lib.FlicManagerUninitializedCallback;
import uk.co.darkerwaters.scorepal.application.Log;

public class Flic1Controller extends Controller<Flic1Controller.Flic1Listener> {

    private static Flic1Controller INSTANCE = null;
    private final static Object Lock = new Object();

    private boolean isFlic1Initialised = false;

    public interface Flic1Listener extends Controller.ControllerListener {
        void onFlic1ButtonConnected(FlicButton button);
    }

    public static Flic1Controller Initialise(Activity context) {
        return Initialise(context, false);
    }

    public static Flic1Controller Initialise(Activity context, boolean isForceInitialisation) {
        synchronized (Lock) {
            if (null == INSTANCE) {
                INSTANCE = new Flic1Controller(context);
            }
            else if (isForceInitialisation) {
                // initialise again
                INSTANCE.initialiseFlic(context);
            }
            return INSTANCE;
        }
    }

    public static Flic1Controller Instance() {
        synchronized (Lock) {
            return INSTANCE;
        }
    }

    public static void Release() {
        synchronized (Lock) {
            if (null != INSTANCE) {
                INSTANCE.releaseFlic();
            }
            INSTANCE = null;
        }
    }

    private Flic1Controller(Activity context) {
        // and initialise this controller now
        initialiseFlic(context);
    }

    private void initialiseFlic(final Activity context) {
        // initialise it now
        try {
            // be sure that our credentials are setup correctly
            Flic1Receiver.SetupAppCredentials();
            // and get the instance to grab the button
            FlicManager.getInstance(context, new FlicManagerInitializedCallback() {
                @Override
                public void onInitialized(FlicManager manager) {
                    List<FlicButton> knownButtons = manager.getKnownButtons();
                    if (null == knownButtons || knownButtons.size() == 0) {
                        // we have no buttons, we want one though, grab one here
                        manager.initiateGrabButton(context);
                    }
                    isFlic1Initialised = true;
                }
            }, new FlicManagerUninitializedCallback() {
                @Override
                public void onUninitialized(FlicManager manager) {
                    super.onUninitialized(manager);
                    informListeners("Flic not initialised");
                    isFlic1Initialised = false;
                }
            });
        } catch (FlicAppNotInstalledException err) {
            isFlic1Initialised = false;
            informListeners(err.getMessage());
        }
    }

    protected void informListeners(FlicButton button) {
        synchronized (listeners) {
            for (Flic1Listener listener: listeners) {
                listener.onFlic1ButtonConnected(button);
            }
        }
    }

    private void releaseFlic() {
        // release all the flic one things
        try {
            FlicManager.destroyInstance();
            isFlic1Initialised = false;
        }
        catch (Exception e) {
            Log.error("Failed to destroy the flic one instance", e);
        }
    }

    public void handleRequestResult(Activity activity, final int requestCode, final int resultCode, final Intent data) {
        if (null != activity) {
            FlicManager.getInstance(activity, new FlicManagerInitializedCallback() {
                @Override
                public void onInitialized(FlicManager manager) {
                    if (manager != null) {
                        FlicButton button = manager.completeGrabButton(requestCode, resultCode, data);
                        if (button != null) {
                            button.registerListenForBroadcast(FlicBroadcastReceiverFlags.CLICK_OR_DOUBLE_CLICK_OR_HOLD | FlicBroadcastReceiverFlags.REMOVED);
                            // inform listeners of this button being listened to now
                            informListeners(button);
                        } else {
                            informListeners("Failed to grab that button");
                        }
                    }
                }
            });
        }
    }

    void handleButtonClick(boolean isSingleClick, boolean isDoubleClick, boolean isHold) {
        if (isSingleClick) {
            informListeners(Controller.ControllerPattern.SingleClick);
        }
        if (isDoubleClick) {
            informListeners(Controller.ControllerPattern.DoubleClick);
        }
        if (isHold) {
            informListeners(Controller.ControllerPattern.LongClick);
        }
    }
}
