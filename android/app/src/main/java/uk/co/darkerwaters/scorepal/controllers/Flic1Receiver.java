package uk.co.darkerwaters.scorepal.controllers;

import android.content.Context;

import io.flic.lib.FlicBroadcastReceiver;
import io.flic.lib.FlicButton;
import io.flic.lib.FlicManager;
import uk.co.darkerwaters.scorepal.application.Log;

public class Flic1Receiver extends FlicBroadcastReceiver {

    private final static String K_FLIC_APP_ID = "fa0684f6-7d59-4102-8b4e-4793671f4f87";
    private final static String K_FLIC_APP_SEC = "9391b36a-22a4-4955-9783-4739c8d148bf";


    public Flic1Receiver() {
        // create the class
    }

    @Override
    protected void onRequestAppCredentials() {
        // initialise our controls with Flic
        SetupAppCredentials();
    }

    public static void SetupAppCredentials() {
        FlicManager.setAppCredentials(K_FLIC_APP_ID, K_FLIC_APP_SEC, Log.K_APPLICATION);
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
            Flic1Controller controller = Flic1Controller.Instance();
            if (null != controller) {
                controller.handleButtonClick(isSingleClick, isDoubleClick, isHold);
            }
        }
    }

    @Override
    public void onButtonRemoved() {
        // Button was removed, whatever
    }
}
