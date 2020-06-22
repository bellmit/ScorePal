package uk.co.darkerwaters.scorepal.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.handlers.PermissionHandler;

import static uk.co.darkerwaters.scorepal.activities.handlers.PermissionHandler.MY_PERMISSIONS_REQUEST_BLUETOOTH;

public class BaseBluetoothActivity extends BaseActivity {

    public static final int REQUEST_ENABLE_BT = 123;

    protected BluetoothAdapter bluetoothAdapter = null;

    private PermissionHandler permissionHandler = null;

    protected boolean turnOnBluetooth() {
        // we want to use a media control to change the score, turn on BT for this
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Phone does not support Bluetooth so let the user know and exit.
        if (this.bluetoothAdapter == null) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.bt_not_compatable)
                    .setMessage(R.string.bt_not_compatable_explain)
                    .setNeutralButton(R.string.ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            // bt isn't enabled, nor can it be
            return false;
        }
        else if (!this.bluetoothAdapter.isEnabled()) {
            // Ensures Bluetooth is available on the device and it is enabled. If not,
            // displays a dialog requesting user permission to enable Bluetooth.
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_ENABLE_BT);
            return true;
        }
        else {
            // already enabled
            onBluetoothEnabled();
            return true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // and request permission right away
        requestBluetoothPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // pass this message to our handler
        if (!this.permissionHandler.processPermissionsResult(requestCode, permissions, grantResults)) {
            // the handler didn't do anything, pass it on
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    protected void requestBluetoothPermission() {
        // need to request permission to access bluetooth things
        // request permission to share the file
        if (null == this.permissionHandler) {
            // there is no handler, make one here
            this.permissionHandler = new PermissionHandler(this,
                    R.string.bluetooth_access_explanation,
                    MY_PERMISSIONS_REQUEST_BLUETOOTH,
                    new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                    },
                    new PermissionHandler.PermissionsHandlerConstructor() {
                        @Override
                        public boolean getIsRequestPermission() {
                            return true;
                        }
                        @Override
                        public void onPermissionsDenied(String[] permissions) {
                            finish();
                        }
                        @Override
                        public void onPermissionsGranted(String[] permissions) {
                            turnOnBluetooth();
                        }
                    });
        }
        // check / request access to file writing to subsequently enable bluetooth
        this.permissionHandler.requestPermission();
    }

    protected void onBluetoothEnabled() {
        // interesting...
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            // this is the response for our BT enabled response
            // not particularly doing anything with it at this stage
            onBluetoothEnabled();
        }
        // pass to the base
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected boolean isDiscovering() {
        return isEnabled() && this.bluetoothAdapter.isDiscovering();
    }

    protected boolean isEnabled() {
        return null != this.bluetoothAdapter && this.bluetoothAdapter.isEnabled();
    }
}
