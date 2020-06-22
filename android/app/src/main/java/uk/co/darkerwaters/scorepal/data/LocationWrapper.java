package uk.co.darkerwaters.scorepal.data;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;

public class LocationWrapper {
    public Location content;
    public LocationWrapper() {
        this.content = null;
    }
    public LocationWrapper(Location content) {
        this.content = content;
    }

    public String serialiseToString() throws JSONException {
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
            return dataArray.toString();
        }
    }

    public LocationWrapper deserialiseFromString(String string) throws JSONException {
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
