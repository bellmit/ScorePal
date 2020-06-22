package uk.co.darkerwaters.scorepal.ui.appsettings;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.flic.flic2libandroid.Flic2Button;
import io.flic.flic2libandroid.Flic2Manager;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.ApplicationPreferences;
import uk.co.darkerwaters.scorepal.application.ApplicationState;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.controllers.Controller;
import uk.co.darkerwaters.scorepal.controllers.Flic2Controller;
import uk.co.darkerwaters.scorepal.ui.views.CustomSnackbar;

public class ActivityFlic2Setup extends ActivityBluetoothSetup implements Flic2Controller.Flic2Listener {

    private Button scanNewButton;
    private TextView scanWizardStatus;

    private FlicRecyclerViewAdapter flicRecyclerViewAdapter = new FlicRecyclerViewAdapter();
    private boolean isScanning;

    private ApplicationPreferences preferences;

    public ActivityFlic2Setup() {
        super(R.layout.activity_flic2setup);
    }

    @Override
    protected void setupViewControls() {
        // setup all our controls here now that we are created
        preferences = ApplicationState.Instance().getPreferences();

        this.scanNewButton = findViewById(R.id.scanNewButton);
        this.scanWizardStatus = findViewById(R.id.scanWizardStatus);

        // initialise Flic 2 always
        Flic2Controller.Initialise(this);

        // by default you cannot scan
        this.scanNewButton.setEnabled(false);
        this.scanNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanForButtons();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // listen for flic2 messages
        Flic2Controller.Initialise(this).addListener(this);

        // setup the display of the flic setup layouts
        setupFlicSetup();
    }

    @Override
    protected void onBluetoothEnabled() {
        // enable the scan button
        this.scanNewButton.setEnabled(true);
    }

    @Override
    protected void onPause() {
        // stop listening for flic2 messages
        Flic2Controller instance = Flic2Controller.Instance();
        if (null != instance) {
            instance.removeListener(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // This will make sure button listeners are correctly removed
        flicRecyclerViewAdapter.onDestroy();

        // Stop a scan, if it's running
        Flic2Controller instance = Flic2Controller.Instance();
        if (null != instance) {
            instance.cancelScan();
        }
    }

    private void scanForButtons() {
        // start scanning for flic version 2 buttons
        Flic2Controller flic2Controller = Flic2Controller.Initialise(this);
        if (isScanning) {
            // cancel the running scan
            flic2Controller.cancelScan();
            isScanning = false;
            // and reset the controls
            scanNewButton.setText(R.string.flic_scan);
            scanWizardStatus.setText("");
        } else {
            // setup the views to cancel the scan next time around
            scanNewButton.setText(R.string.flic_cancelScan);
            scanWizardStatus.setText(R.string.flic_scanInstruction);
            // and start the scan
            isScanning = true;
            flic2Controller.initiateScan();
        }
    }

    private void setupFlicSetup() {
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

    @Override
    public void onFlic2ButtonConnected(Flic2Button button) {
        isScanning = false;

        scanNewButton.setText(R.string.flic_scan);
        scanWizardStatus.setText("");
        flicRecyclerViewAdapter.addButton(button);
    }

    @Override
    public void onControllerError(String message) {
        // let them know about this
        new CustomSnackbar(this,
                getString(R.string.errorFlic) + "\n" +message,
                R.drawable.ic_flic_two_black_24dp,
                R.string.ok,
                null);
    }

    @Override
    public void onControllerInteraction(Controller.ControllerAction action) {
        // message from the flic controller - we can ignore, the card will show something is happening
    }
}
