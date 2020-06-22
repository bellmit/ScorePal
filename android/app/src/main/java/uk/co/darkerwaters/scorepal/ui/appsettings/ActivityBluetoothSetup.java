package uk.co.darkerwaters.scorepal.ui.appsettings;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.ui.PermissionsHandler;
import uk.co.darkerwaters.scorepal.ui.views.CustomSnackbar;

public abstract class ActivityBluetoothSetup extends AppCompatActivity implements PermissionsHandler.PermissionsListener {

    public static final int REQUEST_ENABLE_BT = 128;

    private PermissionsHandler permissionsHandler;
    private final int layoutId;

    protected ActivityBluetoothSetup(int layoutId) {
        this.layoutId = layoutId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutId);

        this.permissionsHandler = new PermissionsHandler(this);

        setupViewControls();
    }

    protected abstract void setupViewControls();

    @Override
    protected void onResume() {
        super.onResume();
        permissionsHandler.addListener(this);

        // check we have the bluetooth permissions required
        enableBluetooth();
    }

    protected void enableBluetooth() {
        // we want to use a media control to change the score, turn on BT for this
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Phone does not support Bluetooth so let the user know
        if (bluetoothAdapter == null || permissionsHandler == null) {
            new CustomSnackbar(this,
                    R.string.bt_not_compatable_explain,
                    R.drawable.ic_bluetooth_black_24dp,
                    R.string.ok,
                    new CustomSnackbar.SnackbarListener() {
                        @Override
                        public void onButtonOnePressed() {
                            //ok button clicked
                            finish();
                        }
                        @Override
                        public void onButtonTwoPressed() {
                            // whatever
                        }
                        @Override
                        public void onDismissed() {
                            // whatever
                        }
                    });
        }
        else {
            // we can support BT - do we have permission?
            if (permissionsHandler.isPermissionsGranted(FragmentAppSettingsGeneral.PERMISSIONS_BT)) {
                // we have the bluetooth permissions granted now, is it on?
                if (!bluetoothAdapter.isEnabled()) {
                    // Ensures Bluetooth is available on the device and it is enabled. If not,
                    // displays a dialog requesting user permission to enable Bluetooth.
                    new CustomSnackbar(this,
                            R.string.bluetoothNotEnabled,
                            R.drawable.ic_bluetooth_black_24dp,
                            R.string.enable,
                            new CustomSnackbar.SnackbarListener() {
                                @Override
                                public void onButtonOnePressed() {
                                    //Yes button clicked
                                    Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                    startActivityForResult(enableBT, REQUEST_ENABLE_BT);
                                }
                                @Override
                                public void onButtonTwoPressed() {
                                    // whatever
                                }
                                @Override
                                public void onDismissed() {
                                    // whatever
                                }
                            });
                }
                else {
                    // bluetooth is on already
                    onBluetoothEnabled();
                }
            }
            else {
                // not allowed, ask for it
                permissionsHandler.checkPermissions(R.string.bluetoothRationale, R.drawable.ic_bluetooth_black_24dp, FragmentAppSettingsGeneral.PERMISSIONS_BT, true);
            }
        }
    }

    protected abstract void onBluetoothEnabled();

    @Override
    protected void onPause() {
        // stop listening for changes in our permissions
        permissionsHandler.removeListener(this);
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PermissionsHandler.PERMISSIONS_REQUEST) {
            // pass this to the handler
            this.permissionsHandler.processPermissionsResult(permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionsChanged(String[] permissions, int[] grantResults) {
        // try to enable bluetooth again then
        enableBluetooth();
    }
}
