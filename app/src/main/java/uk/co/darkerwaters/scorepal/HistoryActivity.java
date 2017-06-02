package uk.co.darkerwaters.scorepal;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.co.darkerwaters.scorepal.bluetooth.BtListAdapter;
import uk.co.darkerwaters.scorepal.bluetooth.BtManager;
import uk.co.darkerwaters.scorepal.history.HistoryFile;
import uk.co.darkerwaters.scorepal.history.HistoryListAdapter;
import uk.co.darkerwaters.scorepal.history.HistoryManager;

public class HistoryActivity extends AppCompatActivity {

    private ListView historyListView;
    private HistoryListAdapter listAdapter;

    //private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        historyListView = (ListView) findViewById(R.id.history_list_view);

        // create the list view adapters
        listAdapter = new HistoryListAdapter(this);
        historyListView.setAdapter(listAdapter);

        // listen for clicks on the list
        historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // handle the user clicking the item in the view

            }
        });
/*
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();*/
        // and fill the list if we can
        populateList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //mGoogleApiClient.connect();
    }
/*
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case RESOLVE_CONNECTION_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                }
                break;
        }
    }*/

    private void populateList() {
        HistoryManager manager = HistoryManager.getManager();
        // fill the list with the files we have
        List<HistoryFile> files = manager.listHistory(this);
        //but the files are in date order, I would like them in reverse please
        Collections.reverse(files);
        // and put this data in the list
        listAdapter.upDateEntries(files);
        // and be sure the adapter is set
        historyListView.setAdapter(listAdapter);
    }
}
