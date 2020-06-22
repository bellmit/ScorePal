package io.flic.lib;

/**
 * FlicButtonCallback
 *
 * Callbacks for button updates. You should extend this class and implement the callbacks you want.
 */
public class FlicButtonCallback {
	/**
	 * Called when the Bluetooth connection has just been started.
	 * It's not ready for use yet however - {@link FlicButtonCallback#onConnectionCompleted()} will be called when ready.
	 *
     */
	public void onConnectionStarted() {}

	/**
	 * Called if there was a problem establishing a Bluetooth connection to the button. Happens very rarely.
     *
     */
	public void onConnectionFailed() {}

	/**
	 * Called when the Bluetooth connection was disconnected, for example if the button becomes out of range or the user manually disconnecting this button in the Flic Application.
	 *
     */
	public void onDisconnect() {}

	/**
	 * Called when the connection to the button has been established and is ready to use.
	 *
     */
	public void onConnectionCompleted() {}

	/**
	 * Called as a result of {@link FlicButton#readRemoteRSSI()}.
     *
     */
	public void onReadRemoteRSSI() {}

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
     */
	public void onButtonSingleOrDoubleClickOrHold() {}

	/**
	 * Called when the button was removed in the Flic App, or when the user disconnected this app from the button in the Flic App (if so the button can be grabbed again as usual).
	 * This object cannot be used any more once this method has returned.
	 *
     */
	public void onButtonRemoved() {}
}
