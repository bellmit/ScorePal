package uk.co.darkerwaters.scorepal.activities;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

import io.flic.flic2libandroid.Flic2Button;
import io.flic.flic2libandroid.Flic2Manager;
import io.flic.flic2libandroid.Flic2ScanCallback;
import io.flic.lib.FlicAppNotInstalledException;
import io.flic.lib.FlicManager;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.handlers.FlicRecyclerViewAdapter;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.controllers.FlicButtonBroadcastReceiver;
import uk.co.darkerwaters.scorepal.settings.SettingsControl;

public class FlicHelpActivity extends BaseBluetoothActivity implements FlicButtonBroadcastReceiver.FlicActivityInterface {

    private Button nextButton;
    private RadioButton flicVersionOneRadio;
    private RadioButton flicVersionTwoRadio;

    private View flicVersionOneSetup;
    private View flicVersionTwoSetup;

    private FlicRecyclerViewAdapter flicRecyclerViewAdapter = new FlicRecyclerViewAdapter();
    private boolean isScanning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flichelp);

        final SettingsControl appSettings = new SettingsControl(this.application);

        this.flicVersionOneSetup = findViewById(R.id.flicOneSetupLayout);
        this.flicVersionTwoSetup = findViewById(R.id.flicTwoSetupLayout);

        this.nextButton = findViewById(R.id.login);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show the flic application
                setupFlic(appSettings);
            }
        });

        this.flicVersionOneRadio = findViewById(R.id.radioButtonFlicOne);
        this.flicVersionTwoRadio = findViewById(R.id.radioButtonFlicTwo);
        if (appSettings.getIsControlUseFlic1()) {
            this.flicVersionOneRadio.setChecked(true);
        }
        else if (appSettings.getIsControlUseFlic2()) {
            this.flicVersionTwoRadio.setChecked(true);
        }

        this.flicVersionOneRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change the display properly
                setupFlicSetup();
            }
        });

        this.flicVersionTwoRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change the display properly
                setupFlicSetup();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // turn on bluetooth
        requestBluetoothPermission();

        // setup the display of the flic setup layouts
        setupFlicSetup();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // This will make sure button listeners are correctly removed
        flicRecyclerViewAdapter.onDestroy();

        // Stop a scan, if it's running
        if (isScanning) {
            try {
                Flic2Manager.getInstance().stopScan();
            }
            catch (Exception e) {
                Log.error("Failed to stop the flic 2 scan", e);
            }
        }
    }

    @Override
    protected void onBluetoothEnabled() {
        super.onBluetoothEnabled();

        // setup the display of the flic setup layouts
        setupFlicSetup();
    }

    @Override
    protected boolean turnOnBluetooth() {
        // let the base handle this
        boolean result = super.turnOnBluetooth();
        // but show our status
        setupFlicSetup();
        // and return the result
        return result;
    }

    private void setupFlicSetup() {
        final SettingsControl appSettings = new SettingsControl(this.application);

        // setup the app settings from the radio they have selected
        appSettings.setIsControlUseFlic1(this.flicVersionOneRadio.isChecked());
        appSettings.setIsControlUseFlic2(this.flicVersionTwoRadio.isChecked());

        // and show the right setup layout accordingly
        if (appSettings.getIsControlUseFlic1()) {
            this.flicVersionOneSetup.setVisibility(View.VISIBLE);
            this.flicVersionTwoSetup.setVisibility(View.GONE);
        }
        if (appSettings.getIsControlUseFlic2()) {
            this.flicVersionOneSetup.setVisibility(View.GONE);
            this.flicVersionTwoSetup.setVisibility(View.VISIBLE);

            FlicButtonBroadcastReceiver.InitialiseFlic(this.application, this, this);

            // setup the flic 2 view of buttons etc
            RecyclerView recyclerView = findViewById(R.id.flicsView);
            recyclerView.setHasFixedSize(true);

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);

            flicRecyclerViewAdapter.clearAllButtons();
            recyclerView.setAdapter(flicRecyclerViewAdapter);

            try {
                for (Flic2Button button : Flic2Manager.getInstance().getButtons()) {
                    flicRecyclerViewAdapter.addButton(button);
                }
            }
            catch (Exception e) {
                Log.error("Failed to find flic 2 buttons", e);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // let the base have it
        super.onActivityResult(requestCode, resultCode, data);
        // and if flic is wanted deal with that here too
        if (requestCode == FlicManager.GRAB_BUTTON_REQUEST_CODE) {
            // and if flic is wanted deal with that here too
            FlicButtonBroadcastReceiver.HandleRequestResult(this, requestCode, resultCode, data);
        }
    }

    private void setupFlic(final SettingsControl appSettings) {
        // and try to setup Flic if we can
        // setup flic if we want
        FlicButtonBroadcastReceiver.InitialiseFlic(true, this.application, this, new FlicButtonBroadcastReceiver.FlicActivityInterface() {
            @Override
            public void onFlicInitialised() {
                // whatever
                finish();
            }
            @Override
            public void onFlicUninitialised() {
                // whatever
                finish();
            }
            @Override
            public void onFlicNotInstalled(FlicAppNotInstalledException err) {
                // show that they need to buy one
                appSettings.setIsControlUseFlic1(false);
                Toast.makeText(FlicHelpActivity.this, R.string.install_flic, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanNewButton(findViewById(R.id.scanNewButton));
            } else {
                Toast.makeText(getApplicationContext(), "Scanning needs Location permission, which you have rejected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void scanNewButton(View v) {
        final Button scanNewButton = findViewById(R.id.scanNewButton);
        final TextView scanWizardStatus = findViewById(R.id.scanWizardStatus);
        if (isScanning) {
            try {
                Flic2Manager.getInstance().stopScan();
            }
            catch (Exception e) {
                Log.error("Failed to stop flic 2 scan", e);
            }

            isScanning = false;

            scanNewButton.setText(R.string.flic_scan);
            scanWizardStatus.setText("");
        } else {
            if (!isEnabled()) {
                requestBluetoothPermission();
                return;
            }

            scanNewButton.setText(R.string.flic_cancelScan);
            scanWizardStatus.setText(R.string.flic_scanInstruction);

            isScanning = true;
            try {
                Flic2Manager.getInstance().startScan(new Flic2ScanCallback() {
                    @Override
                    public void onDiscoveredAlreadyPairedButton(Flic2Button button) {
                        scanWizardStatus.setText(R.string.flic_scanAlreadyFound);
                    }

                    @Override
                    public void onDiscovered(String bdAddr) {
                        scanWizardStatus.setText(R.string.flic_scanFound);
                    }

                    @Override
                    public void onConnected() {
                        scanWizardStatus.setText(R.string.flic_connected);
                    }

                    @Override
                    public void onComplete(int result, int subCode, Flic2Button button) {
                        isScanning = false;

                        scanNewButton.setText(R.string.flic_scan);
                        if (result == Flic2ScanCallback.RESULT_SUCCESS) {
                            scanWizardStatus.setText("");
                            flicRecyclerViewAdapter.addButton(button);
                        } else {
                            scanWizardStatus.setText(getString(R.string.flic_scanError, Flic2Manager.errorCodeToString(result)));
                        }
                    }
                });
            }
            catch (Exception e) {
                Log.error("Failed to scan for flic 2 buttons", e);
                Toast.makeText(this, getString(R.string.flic_scanError, e.getMessage()), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onFlicInitialised() {

    }

    @Override
    public void onFlicUninitialised() {

    }

    @Override
    public void onFlicNotInstalled(final FlicAppNotInstalledException err) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView scanWizardStatus = findViewById(R.id.scanWizardStatus);
                if (null != scanWizardStatus) {
                    scanWizardStatus.setText(getString(R.string.flic_scanError, err.getMessage()));
                }
            }
        });
    }
}
