package io.flic.lib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Broadcast receiver for events from the Flic App.
 * Extend this class and implement the methods you want to get events for.
 * To activate events, call {@link FlicButton#registerListenForBroadcast(int)}.
 * See also {@link FlicBroadcastReceiverFlags}.
 */
public abstract class FlicBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = "FlicBroadcastReceiver";

	public final void onReceive(final Context context, final Intent intent) {
		if (!FlicManager.hasSetAppCredentials()) {
			onRequestAppCredentials();
		}
		FlicManager.getInstance(context, new FlicManagerInitializedCallback() {
			@Override
			public void onInitialized(FlicManager manager) {
				if (!manager.validateIntent(intent)) {
					Log.d(TAG, "Invalid intent received");
					return;
				}

				String deviceId = intent.getStringExtra(FlicIntentExtras.BUTTON_ID);
				FlicButton button = manager.getButtonByDeviceId(deviceId);
				if (button == null) { // For removed
					button = new FlicButton(manager, deviceId);
				}

				String value = intent.getStringExtra(FlicIntentExtras.VALUE);

				switch (intent.getStringExtra(FlicIntentExtras.TYPE)) {
					case FlicIntentTypes.UP_OR_DOWN:
						onButtonUpOrDown();
						break;
					case FlicIntentTypes.CLICK_OR_HOLD:
						onButtonClickOrHold();
						break;
					case FlicIntentTypes.SINGLE_OR_DOUBLE_CLICK:
						onButtonSingleOrDoubleClick();
						break;
					case FlicIntentTypes.SINGLE_OR_DOUBLE_CLICK_OR_HOLD:
						onButtonSingleOrDoubleClickOrHold(context, button, intent.getBooleanExtra(FlicIntentExtras.WAS_QUEUED, false), intent.getIntExtra(FlicIntentExtras.TIME_DIFF, 0), value.equals(FlicIntentValues.SINGLE_CLICK), value.equals(FlicIntentValues.DOUBLE_CLICK), value.equals(FlicIntentValues.HOLD));
						break;
					case FlicIntentTypes.REMOVED:
						onButtonRemoved();
						button.forgotten = true;
						break;
				}
			}
		});
	}

	/**
	 * Sets app credentials.
	 *
	 * In this method, call {@link FlicManager#setAppCredentials(String, String, String)} and set the appropriate app credentials.
	 *
	 */
	protected abstract void onRequestAppCredentials();

	/**
	 * Called when the button was pressed or released.
	 *
	 */
	public void onButtonUpOrDown() {}

	/**
	 * Used for the scenario where you want to listen on button click and hold.
	 *
	 */
	public void onButtonClickOrHold() {}

	/**
	 * Used for the scenario where you want to listen on single click and double click.
	 * Single clicks might be delayed for up to 0.5 seconds because we can't be sure if it was rather a double click or not until then.
	 *
	 */
	public void onButtonSingleOrDoubleClick() {}

	/**
	 * Used for the scenario where you want to listen on single click, double click and hold.
	 * Single clicks might be delayed for up to 0.5 seconds because we can't be sure if it was rather a double click or not until then.
	 *
	 * @param context The Context in which the receiver is running
	 * @param button The button
	 * @param wasQueued If the event was locally queued in the button because it was disconnected. After the connection is completed, the event will be sent with this parameter set to true.
	 * @param timeDiff If the event was queued, the timeDiff will be the number of seconds since the event happened.
	 * @param isSingleClick True if single click, else false
	 * @param isDoubleClick True if double click, else false
	 * @param isHold True if hold, else false
	 */
	public void onButtonSingleOrDoubleClickOrHold(Context context, FlicButton button, boolean wasQueued, int timeDiff, boolean isSingleClick, boolean isDoubleClick, boolean isHold) {}

	/**
	 * Called when the button was removed in the Flic App, or when the user disconnected this app from the button in the Flic App (if so the button can be grabbed again as usual).
	 * This object cannot be used any more once this method has returned.
	 *
	 */
	public void onButtonRemoved() {}
}
