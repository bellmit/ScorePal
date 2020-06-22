package uk.co.darkerwaters.scorepal.score.bluetooth;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Date;

import uk.co.darkerwaters.scorepal.score.MatchSettings;
import uk.co.darkerwaters.scorepal.score.base.Sport;

public class BluetoothMatchSettings extends MatchSettings<BluetoothMatch> {
    
    private MatchSettings containedMatchSettings;

    public BluetoothMatchSettings(Context context) {
        super(Sport.BLUETOOTH);
        this.containedMatchSettings = null;
    }

    public BluetoothMatch createMatch() {
        return new BluetoothMatch(this);
    }

    public void setContainedMatchSettings(MatchSettings containedMatchSettings) {
        this.containedMatchSettings = containedMatchSettings;
    }
    
    public MatchSettings getContainedMatchSettings() {
        return this.containedMatchSettings;
    }

    @Override
    public Sport getSport() {
        // overridden to save the wrapped match instead of this one
        if (null != this.containedMatchSettings){
            return this.containedMatchSettings.getSport();
        }
        else {
            return super.getSport();
        }
    }

    @Override
    public String getMatchId(Context context) {
        // overridden to save the wrapped match instead of this one
        if (null != this.containedMatchSettings){
            return this.containedMatchSettings.getMatchId(context);
        }
        else {
            return super.getMatchId(context);
        }
    }

    @Override
    public Date getMatchPlayedDate() {
        // overridden to save the wrapped match instead of this one
        if (null != this.containedMatchSettings){
            return this.containedMatchSettings.getMatchPlayedDate();
        }
        else {
            return super.getMatchPlayedDate();
        }
    }

    @Override
    public void serialiseToJson(Context context, JSONArray dataArray) throws JSONException {
        // overridden to save the wrapped match instead of this one
        if (null != this.containedMatchSettings){
            this.containedMatchSettings.serialiseToJson(context, dataArray);
        }
        else {
            super.serialiseToJson(context, dataArray);
        }
    }

    @Override
    public MatchSettings deserialiseFromJson(Context context, int version, JSONArray dataArray) throws JSONException {
        // overridden to save the wrapped match instead of this one
        if (null != this.containedMatchSettings){
            return this.containedMatchSettings.deserialiseFromJson(context, version, dataArray);
        }
        else {
            return super.deserialiseFromJson(context, version, dataArray);
        }
    }
}
