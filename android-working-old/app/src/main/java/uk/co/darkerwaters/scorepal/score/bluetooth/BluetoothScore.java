package uk.co.darkerwaters.scorepal.score.bluetooth;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.Score;

public class BluetoothScore extends Score<BluetoothMatchSettings> {

    BluetoothScore(Team[] startingTeams, BluetoothMatchSettings startingSettings) {
        super(startingTeams, startingSettings, 1);
    }

    @Override
    protected void resetScore(Team[] startingTeams, BluetoothMatchSettings startingSettings) {
        // let the base reset
        super.resetScore(startingTeams, startingSettings);
        // and do ours

    }

    @Override
    public void serialiseToJson(Context context, JSONArray dataArray) throws JSONException {
        // do the base
        super.serialiseToJson(context, dataArray);
        // add our data to this array

    }

    @Override
    public void deserialiseFromJson(Context context, int version, JSONArray dataArray) throws JSONException {
        // do the base
        super.deserialiseFromJson(context, version, dataArray);
        // get our data from this array
        switch (version) {
            case 1:
                // load version 1 data
                break;

        }
    }
}
