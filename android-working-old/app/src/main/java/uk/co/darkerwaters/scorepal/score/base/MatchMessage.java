package uk.co.darkerwaters.scorepal.score.base;

import android.content.Context;
import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;

import uk.co.darkerwaters.scorepal.activities.BaseActivity;

public enum MatchMessage {
    RESET,
    CREATE_MATCH, SETUP_NEW_MATCH, SETUP_EXISTING_MATCH,
    STORE_STATE,
    START_PLAY, STOP_PLAY,
    INCREMENT_POINT, UNDO_POINT, ANNOUNCE_POINTS,
    CHANGE_STARTING_ENDS, CHANGE_STARTING_SERVER, CHANGE_STARTER, REQUEST_MATCH_UPDATE;

    public static class StringParam implements Param {
        public String content;
        public StringParam() {
            this.content = "";
        }
        public StringParam(String content) {
            this.content = content;
        }
        @Override
        public String serialiseToString(Context context) {
            return this.content;
        }
        @Override
        public Param deserialiseFromString(Context context, int version, String string) {
            this.content = string;
            return this;
        }
    }

    public static class LocationParam implements Param {
        public Location content;
        public LocationParam() {
            this.content = null;
        }
        public LocationParam(Location content) {
            this.content = content;
        }
        @Override
        public String serialiseToString(Context context) throws JSONException {
            if (null == this.content) {
                return "";
            }
            else {
                // do as JSON for simplicity
                JSONArray dataArray = new JSONArray();
                dataArray.put(this.content.getProvider());
                dataArray.put(this.content.getLatitude());
                dataArray.put(this.content.getLongitude());
                dataArray.put(this.content.getAltitude());
                dataArray.put(this.content.getBearing());
                dataArray.put(this.content.getAccuracy());
                dataArray.put(this.content.getSpeed());
                return BaseActivity.JSONToString(dataArray);
            }
        }
        @Override
        public Param deserialiseFromString(Context context, int version, String string) throws JSONException {
            if (null == string || string.isEmpty()) {
                // no location
                this.content = null;
            }
            else {
                // use the JSON
                JSONArray dataArray = new JSONArray(string);
                int dataIndex = 0;
                this.content = new Location(dataArray.getString(dataIndex++));
                this.content.setLatitude(dataArray.getDouble(dataIndex++));
                this.content.setLongitude(dataArray.getDouble(dataIndex++));
                this.content.setAltitude(dataArray.getDouble(dataIndex++));
                this.content.setBearing((float)dataArray.getDouble(dataIndex++));
                this.content.setAccuracy((float)dataArray.getDouble(dataIndex++));
                this.content.setSpeed((float)dataArray.getDouble(dataIndex++));
            }
            return this;
        }
    }

    public static class FileParam implements Param {
        public File content;
        public FileParam() {
            this.content = null;
        }
        public FileParam(File content) {
            this.content = content;
        }
        @Override
        public String serialiseToString(Context context) {
            return this.content == null ? "" : this.content.getPath();
        }
        @Override
        public Param deserialiseFromString(Context context, int version, String string) {
            this.content = string == null || string.isEmpty() ? null : new File(string);
            return this;
        }
    }

    public interface Param {
        String serialiseToString(Context context) throws Exception;
        Param deserialiseFromString(Context context, int version, String string) throws Exception;
    }
}
