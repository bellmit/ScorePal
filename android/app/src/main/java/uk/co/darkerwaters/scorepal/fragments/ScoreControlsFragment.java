package uk.co.darkerwaters.scorepal.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.DeviceScoreActivity;
import uk.co.darkerwaters.scorepal.bluetooth.BtManager;
import uk.co.darkerwaters.scorepal.storage.uk.co.darkerwaters.scorepal.storage.data.Match;
import uk.co.darkerwaters.scorepal.storage.ScoreData;
import uk.co.darkerwaters.scorepal.storage.StorageManager;

public class ScoreControlsFragment extends Fragment {

    private ImageButton saveButton;
    private ImageButton undoButton;
    private ImageButton resetButton;
    private DeviceScoreActivity parentContext;

    public ScoreControlsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            // we want it to be an activity to call RunOnUI
            this.parentContext = (DeviceScoreActivity)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement DeviceScoreActivity");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_score_controls, container, false);

        // setup the buttons
        saveButton = (ImageButton) view.findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveClicked();
            }
        });

        undoButton = (ImageButton) view.findViewById(R.id.undo_point_button);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUndoClicked();
            }
        });

        resetButton = (ImageButton) view.findViewById(R.id.reset_match_button);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onResetClicked();
            }
        });

        // return the view that is the fragment expanded from the xml
        return view;
    }

    private void onSaveClicked() {
        StorageManager manager = StorageManager.getManager();
        ScoreData data = manager.getCurrentScoreData();
        if (null == manager || null == manager.getCurrentUser()) {
            Toast.makeText(parentContext, R.string.error_user_not_signedin, Toast.LENGTH_SHORT).show();
        }
        else if (null != data) {
            // there is data to store, store it, first work out what we would want to call this
            String playerOne = manager.getCurrentPlayerOneTitle();
            String playerTwo = manager.getCurrentPlayerTwoTitle();

            // get the match data for this
            Match match = new Match(manager.getCurrentUser(),
                    playerOne, playerTwo,
                    data,
                    manager.getMatchStartedDate());
            // and store this match / will overwrite any old on this key of userId / match date
            match.updateInDatabase(StorageManager.getManager().getTopLevel());
            // and tell the user this worked
            Toast.makeText(parentContext, R.string.successful_save, Toast.LENGTH_SHORT).show();
            //TODO could show a fun little animation of a file wizzing off here to show it worked
        }
    }

    private void onUndoClicked() {
        BtManager.getManager().sendMessage("{a3}");
    }

    private void onResetClicked() {
        AlertDialog.Builder alert = new AlertDialog.Builder(parentContext);
        alert.setTitle(getResources().getString(R.string.warning));
        alert.setMessage(getResources().getString(R.string.sure_to_reset));
        alert.setPositiveButton(getResources().getString(R.string.yes), new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // command the score board to reset
                if (BtManager.getManager().sendMessage("{a9}")) {
                    // and reset our current match date
                    StorageManager.getManager().resetMatchStartedDate(0);
                }
                else {
                    parentContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // show the error
                            Toast.makeText(parentContext, R.string.unsuccessful_reset, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        alert.setNegativeButton(getResources().getString(R.string.no), new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // whatever
            }
        });
        alert.show();
    }
}
