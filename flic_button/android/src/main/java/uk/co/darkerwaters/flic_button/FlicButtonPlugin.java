package uk.co.darkerwaters.flic_button;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.flic.flic2libandroid.Flic2Button;
import io.flic.flic2libandroid.Flic2Manager;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** FlicButtonPlugin */
public class FlicButtonPlugin implements FlutterPlugin, MethodCallHandler {
  public static final String channelName = "flic_button";

  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;

  // we can and want to control a flic2 then
  private FlicButton2Plugin flic2Plugin = null;

  private long mCallbackDispatcherHandle;
  private Context mContext;

  private Map<Integer, Runnable> callbackById = new HashMap<>();

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    this.channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), channelName);
    this.channel.setMethodCallHandler(this);

    this.mContext = flutterPluginBinding.getApplicationContext();
    // and we can initialise our flic2 support here
    this.flic2Plugin = new FlicButton2Plugin(mContext);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull final Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if (call.method.equals("getFlic2Button")) {
      // scan for the flic2
      flic2Plugin.getButton(new FlicButton2Plugin.ButtonCallback() {
        @Override
        public void onButtonFound(String button) {
          result.success(button);
        }
        @Override
        public void onError(String error) {
          result.error(error, "Failed to scan for the Flic2", null);
        }
      });
    } else if(call.method.equals("initializeService")){
        // Get callback id
        ArrayList args = call.arguments();
        final int currentListenerId = (int) args.get(0);
        // Prepare a timer like self calling task
        final Handler handler = new Handler();
        callbackById.put(currentListenerId, new Runnable() {
          @Override
          public void run() {
            if (callbackById.containsKey(currentListenerId)) {
              Map<String, Object> args = new HashMap();
              args.put("id", currentListenerId);
              args.put("args", "Hello listener! " + (System.currentTimeMillis() / 1000));
              // Send some value to callback
              channel.invokeMethod("callListener", args);
            }
            handler.postDelayed(this, 1000);
          }
        });
        // Run task
        handler.postDelayed(callbackById.get(currentListenerId), 1000);
        // Return immediately
        result.success(null);
      }
    else if(call.method.equals("cancelListening")){
      // Get callback id
      ArrayList args = call.arguments();
      int currentListenerId = (int) args.get(0);
      // Remove callback
      callbackById.remove(currentListenerId);
      // Do additional stuff if required to cancel the listener
      result.success(null);
    }
    else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }
}
