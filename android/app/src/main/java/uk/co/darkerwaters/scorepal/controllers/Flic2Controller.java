package uk.co.darkerwaters.scorepal.controllers;

import android.content.Context;
import android.os.Handler;

import io.flic.flic2libandroid.Flic2Button;
import io.flic.flic2libandroid.Flic2ButtonListener;
import io.flic.flic2libandroid.Flic2Manager;
import io.flic.flic2libandroid.Flic2ScanCallback;
import uk.co.darkerwaters.scorepal.application.Log;

public class Flic2Controller extends Controller<Flic2Controller.Flic2Listener> {

    public interface Flic2Listener extends Controller.ControllerListener {
        void onFlic2ButtonConnected(Flic2Button button);
    }

    private static Flic2Controller INSTANCE = null;
    private final static Object Lock = new Object();

    private boolean isFlic2Initialised = false;

    public static Flic2Controller Initialise(Context context) {
        synchronized (Lock) {
            if (null == INSTANCE) {
                INSTANCE = new Flic2Controller(context);
            }
            return INSTANCE;
        }
    }

    public static Flic2Controller Instance() {
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

    private Flic2Controller(Context context) {
        // this controller is created, find and connect to any buttons there are
        Flic2Manager manager = Flic2Manager.initAndGetInstance(context, new Handler());
        if (null != manager) {
            // Every time the app process starts, connect to all paired buttons and assign a click listener
            for (Flic2Button button : manager.getButtons()) {
                // connect to this button
                button.connect();
                listenToButton(button);
            }
            isFlic2Initialised = true;
        }
    }

    private void informListeners(Flic2Button button) {
        synchronized (listeners) {
            for (Flic2Listener listener: listeners) {
                listener.onFlic2ButtonConnected(button);
            }
        }
    }

    public void initiateScan() {
        cancelScan();
        Flic2Manager.getInstance().startScan(new Flic2ScanCallback() {
            @Override
            public void onDiscoveredAlreadyPairedButton(Flic2Button button) {
                // Found an already paired button, connect to this
                listenToButton(button);
            }
            @Override
            public void onDiscovered(String bdAddr) {
                // Found Flic2, now connecting...
                Log.debug("Discovered flic button " + bdAddr);
            }
            @Override
            public void onConnected() {
                // Connected. Now pairing...
                Log.debug("Connected to flic button ");
            }
            @Override
            public void onComplete(int result, int subCode, Flic2Button button) {
                if (result == Flic2ScanCallback.RESULT_SUCCESS) {
                    // The button object can now be used
                    listenToButton(button);
                } else {
                    // Failed
                    informListeners("Failed to connect to that button");
                }
            }
        });
    }

    public void cancelScan() {
        try {
            Flic2Manager manager = Flic2Manager.getInstance();
            if (null != manager) {
                manager.stopScan();
            }
        }
        catch (Exception e) {
            Log.error("Failed to stop scan while releasing flic", e);
        }
    }

    private void listenToButton(Flic2Button button) {
        // we might already be listening to this, try to remove it first
        button.removeListener(buttonListener);
        // and add it back in to listen to each button only once.
        button.addListener(buttonListener);
        // and inform listeners of this
        informListeners(button);
    }

    private final Flic2ButtonListener buttonListener = new Flic2ButtonListener() {
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
                handleButtonClick(isSingleClick, isDoubleClick, isHold);
            }
        }
    };

    private void releaseFlic() {
        // cancel any scanning
        cancelScan();
        // release all the flic 2 listeners on the managers
        try {
            Flic2Manager manager = Flic2Manager.getInstance();
            if (null != manager) {
                // we are probably listening to buttons, stop this
                for (Flic2Button button : manager.getButtons()) {
                    try {
                        button.removeListener(buttonListener);
                    } catch (Exception e) {
                        Log.error("Failed to remove listener on releasing flic", e);
                    }
                }
            }
        }
        catch (Exception e) {
            Log.error("Failed to destroy the flic two instance as it was not initialised", e);
        }
        isFlic2Initialised = false;
    }

    private void handleButtonClick(boolean isSingleClick, boolean isDoubleClick, boolean isHold) {
        // inform any local listeners of this click
        if (isSingleClick) {
            informListeners(ControllerPattern.SingleClick);
        }
        if (isDoubleClick) {
            informListeners(ControllerPattern.DoubleClick);
        }
        if (isHold) {
            informListeners(ControllerPattern.LongClick);
        }
    }
}
