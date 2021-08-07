package uk.co.darkerwaters.flic_button;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import io.flic.flic2libandroid.Flic2Button;
import io.flic.flic2libandroid.Flic2Manager;
import io.flic.flic2libandroid.Flic2ScanCallback;
import io.flutter.plugin.common.MethodChannel;

public class FlicButton2Plugin {
    // keep the buttons so we can call functions on them later from flutter (via UUID)
    final Map<String, Flic2Button> buttons = new HashMap<>();

    public interface ButtonCallback {
        void onButtonFound(String button);
        void onError(String error);
    }
    public FlicButton2Plugin(Context context) {
        // initialise the manager, don't need to remember it as we can just get it later
        Flic2Manager.initAndGetInstance(context, new Handler());
    }

    public static String ButtonToJson(Flic2Button button) {
        return "{" +
                "\"UUID\":\""+ button.getUuid() + "\"" +
                "}";
    }

    public void getButton(final ButtonCallback callback) {
        Flic2Manager.getInstance().startScan(new Flic2ScanCallback() {
            @Override
            public void onDiscoveredAlreadyPairedButton(Flic2Button button) {
                // Found an already paired button
                storeButtonData(button);
                // and inform the caller of this state
                callback.onButtonFound(ButtonToJson(button));
            }
            @Override
            public void onDiscovered(String bdAddr) {
                // Found Flic2, now connecting...
                Log.d("flic_button", "found flic2 at $bdAddr");
            }
            @Override
            public void onConnected() {
                Log.d("flic_button", "Connected. Now pairing...");
            }
            @Override
            public void onComplete(int result, int subCode, Flic2Button button) {
                if (result == Flic2ScanCallback.RESULT_SUCCESS) {
                    // The button object can now be used, store this
                    storeButtonData(button);
                    // and inform the caller of this state
                    callback.onButtonFound(ButtonToJson(button));
                } else {
                    callback.onError("$result, $subCode");
                }
            }
        });
    }

    private void storeButtonData(Flic2Button button) {
        // store this data for later
        synchronized (buttons) {
            buttons.put(button.getUuid(), button);
        }
    }
}
