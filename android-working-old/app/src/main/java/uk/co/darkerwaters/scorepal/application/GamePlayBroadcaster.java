package uk.co.darkerwaters.scorepal.application;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import uk.co.darkerwaters.scorepal.activities.BaseActivity;
import uk.co.darkerwaters.scorepal.activities.BaseBluetoothActivity;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.base.MatchMessage;
import uk.co.darkerwaters.scorepal.score.MatchPersistenceManager;
import uk.co.darkerwaters.scorepal.score.MatchSettings;
import uk.co.darkerwaters.scorepal.score.base.PointChange;

public class GamePlayBroadcaster implements Match.MatchListener, GamePlayService.GamePlayListener, GamePlayBroadcastSocket.BroadcastSocketListener {

    static final UUID MY_UUID = UUID.fromString("23b8b153-4eba-4b8e-8793-b40d8995f5bd");

    private static final String K_MESSAGING_PREFIX_MATCH = "m";
    private static final String K_MESSAGING_PREFIX_MATCH_CHANGE = "mc";
    private static final String K_MESSAGING_PREFIX_MATCH_MESSAGE = "mm";

    private static final String K_MESSAGING_PREFIX_NAME = "p";
    private static final String K_MESSAGING_VERSION_NAME = "v";
    private static final String K_MESSAGING_DATA_NAME = "d";

    private final Application application;
    private GamePlayService service;
    private Activity activeActivity;

    private Thread serverThread;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothServerSocket btServerSocket;
    private volatile boolean isAcceptingIncomingSockets = true;

    private final List<BluetoothDevice> devicesConnecting;
    private final List<GamePlayBroadcastSocket> incomingClientSockets;
    private final Map<String, GamePlayBroadcastSocket> outgoingClientSockets;
    private BluetoothDevice deviceToConnect = null;


    public interface GamePlayBroadcastListener {
        void onDeviceSocketConnected(BluetoothDevice connectedDevice);
        void onDeviceSocketDisconnected(BluetoothDevice disconnectedDevice);
    }

    public interface ClientConnectionInterface {
        void onSocketConnectionSuccess();
        void onSocketConnectionFailed();
    }

    private final List<GamePlayBroadcastListener> listeners;

    private static class ActiveBroadcaster {
        GamePlayBroadcaster Broadcaster = null;
    }
    private static final ActiveBroadcaster ActiveBroadcaster = new ActiveBroadcaster();

    public static boolean IsBroadcasting() {
        synchronized (ActiveBroadcaster) {
            return null != ActiveBroadcaster.Broadcaster
                    && null != ActiveBroadcaster.Broadcaster.activeActivity;
        }
    }

    public static void MatchChanged(MatchMessage message, MatchMessage.Param... dataParams) {
        synchronized (ActiveBroadcaster) {
            if (ActiveBroadcaster.Broadcaster != null && IsBroadcasting()) {
                // we are broadcasting let the broadcaster broadcast this data
                ActiveBroadcaster.Broadcaster.sendDataToConnected(message, dataParams);
            }
        }
    }

    public static void BroadcastMatchUpdate() {
        synchronized (ActiveBroadcaster) {
            if (ActiveBroadcaster.Broadcaster != null && IsBroadcasting()) {
                // we are broadcasting let the broadcaster broadcast this data
                ActiveBroadcaster.Broadcaster.sendMatchDataToConnectedClients();
            }
        }
    }

    public static GamePlayBroadcaster ActivateBroadcaster(BaseActivity activity) {
        synchronized (ActiveBroadcaster) {
            // create only one Broadcaster for everyone
            if (ActiveBroadcaster.Broadcaster == null || !IsBroadcasting()) {
                // there is no Broadcaster or it is not running, remedy this now
                ActiveBroadcaster.Broadcaster = new GamePlayBroadcaster(activity);
            }
            else if (ActiveBroadcaster.Broadcaster != null) {
                ActiveBroadcaster.Broadcaster.setActiveActivity(activity, activity.getGamePlayService());
            }
            // and return the valid Broadcaster
            return ActiveBroadcaster.Broadcaster;
        }
    }

    private GamePlayBroadcaster(BaseActivity activity) {
        // setup this Broadcaster for the specified activity
        this.application = Application.getApplication(activity);
        this.listeners = new ArrayList<>();
        this.incomingClientSockets = new ArrayList<>();
        this.outgoingClientSockets = new HashMap<>();
        this.devicesConnecting = new ArrayList<>();

        // remember for who we are communicating
        setActiveActivity(activity, activity.getGamePlayService());
    }

    public void setDeviceToConnect(BluetoothDevice device) { this.deviceToConnect = device; }

    public BluetoothDevice getDeviceToConnect() { return this.deviceToConnect; }

    private void setActiveActivity(BaseActivity activity, GamePlayService service) {
        this.activeActivity = activity;
        // and get the service from this activity
        this.service = service;
        if (null != this.service) {
            // we want and need to listen to the service and the match for their input too
            this.service.addMatchListener(this);
            this.service.addStateListener(this);
        }
    }

    public boolean addListener(GamePlayBroadcastListener listener) {
        synchronized (this.listeners) {
            if (this.listeners.contains(listener)) {
                // already in the list, fine, just return nicely
                return false;
            }
            else {
                // add to the list
                return this.listeners.add(listener);
            }
        }
    }

    public boolean removeListener(GamePlayBroadcastListener listener) {
        synchronized (this.listeners) {
            return this.listeners.remove(listener);
        }
    }

    public static void SilenceCommunications() {
        synchronized (ActiveBroadcaster) {
            if (null != ActiveBroadcaster.Broadcaster) {
                // silence the Broadcaster
                ActiveBroadcaster.Broadcaster.silenceCommunications();
            }
        }
    }

    public void silenceCommunications() {
        // we are no longer active, just dumb
        this.activeActivity = null;
        // close any connecting socket
        this.isAcceptingIncomingSockets = false;
        if (null != this.btServerSocket) {
            try {
                this.btServerSocket.close();
            } catch (IOException e) {
                Log.error("Failed to close the bt server socket", e);
            }
            this.btServerSocket = null;
        }

        // close all sockets
        synchronized (this.incomingClientSockets) {
            for (GamePlayBroadcastSocket openSocket : this.incomingClientSockets) {
                // close each one - probably already closed because we closed the server socket
                openSocket.close();
            }
            // and clear the list
            this.incomingClientSockets.clear();
        }

        synchronized (this.outgoingClientSockets) {
            for (GamePlayBroadcastSocket clientSocket : this.outgoingClientSockets.values()) {
                // close each client that is connected
                clientSocket.close();
            }
            // and clear them
            this.outgoingClientSockets.clear();
        }

        if (null != this.service) {
            // we want and need to listen to the service and the match for their input too
            this.service.removeMatchListener(this);
            this.service.removeStateListener(this);
        }
    }

    public boolean disconnectClientFromBroadcaster(BluetoothDevice device) {
        // remember that we are trying with this one
        synchronized (this.devicesConnecting) {
            this.devicesConnecting.remove(device);
        }
        return disconnectClientFromBroadcaster(device.getAddress());
    }

    private boolean disconnectClientFromBroadcaster(String deviceAddress) {
        boolean isRemoved = false;
        synchronized (this.outgoingClientSockets) {
            GamePlayBroadcastSocket removed = this.outgoingClientSockets.remove(deviceAddress);
            if (null != removed) {
                // close this removed client socket
                removed.close();
                isRemoved = true;
            }
        }
        return isRemoved;
    }

    private void addConnectedSocket(BluetoothSocket socket, BluetoothDevice device) {
        // and put this in the map
        synchronized (this.outgoingClientSockets) {
            // wrap this up in one we manage data to and from - creating one starts it running
            GamePlayBroadcastSocket broadcastSocket = new GamePlayBroadcastSocket(socket, this);
            this.outgoingClientSockets.put(device.getAddress(), broadcastSocket);
        }
        synchronized (this.listeners) {
            for (GamePlayBroadcastListener listener : this.listeners) {
                listener.onDeviceSocketConnected(device);
            }
        }
    }

    public void connectClientToBroadcaster(final BluetoothDevice device, final ClientConnectionInterface listener) {
        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            final BluetoothSocket socket = device.createRfcommSocketToServiceRecord(GamePlayBroadcaster.MY_UUID);
            if (null != socket) {
                // we have a socket, remove any exiting client socket
                disconnectClientFromBroadcaster(device);
                // remember that we are trying with this one
                synchronized (this.devicesConnecting) {
                    this.devicesConnecting.add(device);
                }
                // this client needs to connect in a blocking thread, create this here
                Thread clientConnectionThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Connect to the remote device through the socket. This call blocks
                            // until it succeeds or throws an exception.
                            socket.connect();
                            // this socket is connected now, so we can manage it
                            addConnectedSocket(socket, device);
                            listener.onSocketConnectionSuccess();
                        } catch (IOException connectException) {
                            Log.error("Failed to connect to device", connectException);
                            // need to remove this from our list as we failed to connect, or closed
                            // now
                            disconnectClientFromBroadcaster(device);
                            listener.onSocketConnectionFailed();
                        }
                        // we are done with this now
                        synchronized (devicesConnecting) {
                            devicesConnecting.remove(device);
                        }
                    }
                });
                // and run this thread
                clientConnectionThread.start();
            }
        } catch (IOException e) {
            Log.error("Socket's create() method failed", e);
        }
    }

    private void sendDataToConnected(MatchMessage message, MatchMessage.Param[] dataParams) {
        // we need to wrap this data up in a nice JSON structure and send out
        try {
            // create the data as a simple array
            JSONArray data = new JSONArray();
            data.put(message.toString());
            data.put(dataParams.length);
            // put all the params
            for (MatchMessage.Param param : dataParams) {
                data.put(param.serialiseToString(this.activeActivity.getBaseContext()));
            }
            // and send this data
            String dataToSend = packageDataInMessage(K_MESSAGING_PREFIX_MATCH_MESSAGE, data);
            // and send the data to all connected clients if we are a broadcaster
            sendDataToConnectedClients(dataToSend);
            // or if we are a client, send this data to the broadcaster we are connected to
            sendDataToBroadcaster(dataToSend);
        }
        catch (JSONException e) {
            Log.error("JSON Error sending match message", e);
        }
        catch (Exception e) {
            Log.error("Error sending match message", e);
        }
    }

    private void parseReceivedMatchMessage(int version, JSONArray data) throws Exception {
        int dataIndex = 0;
        String dataString;
        switch (version) {
            case 1:
                // version 'a' parsing code, the first thing is the message and the rest
                // are the data params
                dataString = data.getString(dataIndex++);
                MatchMessage message = MatchMessage.valueOf(dataString);
                int noParams = data.getInt(dataIndex++);
                String[] dataParams = new String[noParams];
                for (int i = 0; i < noParams; ++i) {
                    dataString = data.getString(dataIndex++);
                    // we cannot create dataParams from the string because we don't know what to create
                    // the communicator knows this (based on the MatchMessage)
                    dataParams[i] = dataString;
                }
                // now we loaded this message, we can pass it to the active communicator
                GamePlayCommunicator.GetActiveCommunicator().sendRequest(message, dataParams);
                break;
        }
    }

    private String packageDataInMessage(String prefix, JSONArray data) throws JSONException {
        // create the message and put the version on
        JSONObject messageObject = new JSONObject();
        // put the message type (the prefix) and version in
        messageObject.put(K_MESSAGING_PREFIX_NAME, prefix);
        messageObject.put(K_MESSAGING_VERSION_NAME, MatchPersistenceManager.K_VERSION);
        // add the data to the object
        messageObject.put(K_MESSAGING_DATA_NAME, data);
        // this data needs to be a string
        return BaseActivity.JSONToString(messageObject);
    }

    private void sendMatchDataToConnectedClients() {
        // we want to send the match data, but this doesn't really inform anyone, so
        // we want to also send a points change message to get through to those
        // who are interested
        GamePlayCommunicator communicator = GamePlayCommunicator.GetActiveCommunicator();
        Match currentMatch = communicator.getCurrentMatch();
        if (null != currentMatch) {
            // have a match, so we can send a point change for the team for this match
            sendMatchDataToConnectedClients(new PointChange[]{
                    new PointChange(currentMatch.getTeamOne(), -1, 0)});
        }
    }

    private void sendMatchDataToConnectedClients(PointChange[] levelsChanged) {
        GamePlayCommunicator communicator = GamePlayCommunicator.GetActiveCommunicator();
        Match currentMatch = communicator.getCurrentMatch();
        MatchSettings currentSettings = communicator.getCurrentSettings();
        Context context = this.activeActivity.getBaseContext();
        if (null != currentMatch && null != currentSettings) {
            // we need to wrap this data up in a nice JSON structure and send out
            try {
                // create the data as a simple array
                JSONArray data = new JSONArray();
                // and put the settings and the match into the object
                data.put(currentSettings.serialiseToString(context));
                data.put(currentMatch.serialiseToString(context));
                // and the levels that changed on this match data
                data.put(levelsChanged.length);
                Team teamOne = currentMatch.getTeamOne();
                for (PointChange change : levelsChanged) {
                    data.put(change.team == teamOne ? 1 : 2);
                    data.put(change.level);
                    data.put(change.point);
                }
                // and send this data
                String dataToSend = packageDataInMessage(K_MESSAGING_PREFIX_MATCH, data);
                // and send the data to all connected clients if we are a broadcaster
                sendDataToConnectedClients(dataToSend);
            } catch (JSONException e) {
                Log.error("JSON Error sending match message", e);
            } catch (Exception e) {
                Log.error("Error sending match message", e);
            }
        }
    }

    private void sendMatchDataToConnectedClients(Match.MatchChange type) {
        GamePlayCommunicator communicator = GamePlayCommunicator.GetActiveCommunicator();
        Match currentMatch = communicator.getCurrentMatch();
        MatchSettings currentSettings = communicator.getCurrentSettings();
        if (null != currentMatch && null != currentSettings) {
            // we need to wrap this data up in a nice JSON structure and send out
            try {
                // create the data as a simple array
                JSONArray data = new JSONArray();
                // put the type of change into the message
                data.put(type.toString());
                // and send this data
                String dataToSend = packageDataInMessage(K_MESSAGING_PREFIX_MATCH_CHANGE, data);
                // and send the data to all connected clients if we are a broadcaster
                sendDataToConnectedClients(dataToSend);
            } catch (JSONException e) {
                Log.error("JSON Error sending match message", e);
            } catch (Exception e) {
                Log.error("Error sending match message", e);
            }
        }
    }

    private void parseReceivedMatch(int version, JSONArray data) throws Exception {
        int dataIndex = 0;
        Context context = this.activeActivity.getBaseContext();
        switch (version) {
            case 1:
                // version 'a' parsing code, the first thing is the message and the rest
                // are the data params
                String settingsString = data.getString(dataIndex++);
                String matchString = data.getString(dataIndex++);
                // we can create the right settings from the serialised string
                MatchSettings matchSettings = MatchSettings.createFromSerialisedString(context, MatchPersistenceManager.K_VERSION, settingsString);
                if (null != matchSettings) {
                    // create the match from this
                    Match match = matchSettings.createMatch();
                    // and load in the data we stored for it
                    match = match.deserialiseFromString(context, MatchPersistenceManager.K_VERSION, matchString);
                    // we can also load the changes of points that caused this
                    int noChanges = data.getInt(dataIndex++);
                    PointChange[] levelsChanged = new PointChange[noChanges];
                    Team teamOne = match.getTeamOne();
                    Team teamTwo = match.getTeamTwo();
                    for (int i = 0; i < noChanges; ++i) {
                        int teamNumber = data.getInt(dataIndex++);
                        int level = data.getInt(dataIndex++);
                        int point = data.getInt(dataIndex++);
                        levelsChanged[i] = new PointChange(
                                teamNumber == 1 ? teamOne : teamTwo,
                                level,
                                point);
                    }
                    // now we loaded this message, we can pass it to the active communicator
                    GamePlayCommunicator communicator = GamePlayCommunicator.GetActiveCommunicator();
                    communicator.onMatchReceived(matchSettings, match, levelsChanged);
                }
                break;
        }
    }

    private void parseReceivedMatchChange(int version, JSONArray data) throws Exception {
        int dataIndex = 0;
        switch (version) {
            case 1:
                // version 'a' parsing code, the first thing is the message and the rest
                // are the data params
                String changeString = data.getString(dataIndex++);
                Match.MatchChange change = Match.MatchChange.valueOf(changeString);
                // now we loaded this message, we can pass it to the active communicator
                GamePlayCommunicator.GetActiveCommunicator().onMatchChangeReceived(change);
                break;
        }
    }

    @Override
    public void onDataRead(String dataString) {
        // we just read some data from a connected socket
        String prefix = "";
        int version = 0;
        try {
            JSONObject messageObject = new JSONObject(dataString);
            prefix = messageObject.getString(K_MESSAGING_PREFIX_NAME);
            version = messageObject.getInt(K_MESSAGING_VERSION_NAME);
            JSONArray data = messageObject.getJSONArray(K_MESSAGING_DATA_NAME);
            switch (prefix) {
                case K_MESSAGING_PREFIX_MATCH_MESSAGE :
                    // this is a match message, process this
                    parseReceivedMatchMessage(version, data);
                    break;
                case K_MESSAGING_PREFIX_MATCH :
                    // this is the whole match, process this
                    parseReceivedMatch(version, data);
                    break;
                case K_MESSAGING_PREFIX_MATCH_CHANGE :
                    // this is the whole match, process this
                    parseReceivedMatchChange(version, data);
                    break;
                default:
                    Log.error("unrecognised data prefix of " + prefix + ":" + version);
                    break;
            }
        }
        catch (JSONException e) {
            Log.error("JSON Error reading match data " + prefix + ":" + version, e);
        }
        catch (Exception e) {
            Log.error("Error reading match data " + prefix + ":" + version, e);
        }
    }

    @Override
    public void onDataWriteError(String message) {
        // failed to send some data
        Log.error("Socket failed in sending: " + message);
    }

    private void sendDataToBroadcaster(String dataToSend) {
        synchronized (this.outgoingClientSockets) {
            // send data via all the connected clients
            for (GamePlayBroadcastSocket socket : this.outgoingClientSockets.values()) {
                socket.write(dataToSend);
            }
        }
    }

    private void sendDataToConnectedClients(String dataToSend) {
        synchronized (this.incomingClientSockets) {
            // send data via all the connected clients
            for (GamePlayBroadcastSocket socket : this.incomingClientSockets) {
                socket.write(dataToSend);
            }
        }
    }

    @Override
    public void onPlayStateChanged(Date playStarted, Date playEnded) {
        // if we are connected to anyone, send the match data to them
        sendMatchDataToConnectedClients();
    }

    @Override
    public void onMatchChanged(Match.MatchChange type) {
        // if we are connected to anyone, send the match data to them
        sendMatchDataToConnectedClients(type);
        switch (type) {
            case DECREMENT:
                // these are special cases as don't cause a points change message
                // to be communicated, they need to send the new match data now
                sendMatchDataToConnectedClients();
                break;
        }
    }

    @Override
    public void onMatchPointsChanged(PointChange[] levelsChanged) {
        // if we are connected to anyone, send the match data to them
        sendMatchDataToConnectedClients(levelsChanged);
    }

    public boolean startBroadcasting() {
        if (null != this.serverThread && this.isAcceptingIncomingSockets) {
            // we are already broadcasting
            return true;
        }
        // setup the adapter etc here
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Phone does not support Bluetooth so let the user know and exit.
        if (this.bluetoothAdapter != null) {
            if (!this.bluetoothAdapter.isEnabled()) {
                // Ensures Bluetooth is available on the device and it is enabled. If not,
                // displays a dialog requesting user permission to enable Bluetooth.
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                this.activeActivity.startActivityForResult(enableBT, BaseBluetoothActivity.REQUEST_ENABLE_BT);
                Log.error("BT not enabled when starting broadcasting - try again");
                return false;
            }
        } else {
            Log.error("BT not available when starting broadcasting");
            return false;
        }

        // setup the socket here
        this.btServerSocket = null;
        this.isAcceptingIncomingSockets = true;
        try {
            // MY_UUID is the app's UUID string, also used by the client code.
            this.btServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(Log.K_APPLICATION, MY_UUID);
        } catch (IOException e) {
            Log.error("Socket's listen() method failed", e);
            return false;
        }

        // create the accept thread and listen for clients connecting to us
        this.serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                BluetoothSocket socket;
                // Keep listening until exception occurs or a socket is returned.
                while (isAcceptingIncomingSockets) {
                    try {
                        socket = btServerSocket.accept();
                    } catch (IOException e) {
                        Log.error("Socket's accept() method failed", e);
                        break;
                    }

                    if (socket != null) {
                        // A connection was accepted. Perform work associated with
                        // the connection in a separate thread.
                        onSocketConnected(socket);
                        // leave the server socket alone to multiples can connect
                        //btServerSocket.close();
                        //break;
                    }
                }
            }
        });
        this.serverThread.start();
        // this is success
        return true;
    }

    public boolean disconnectConnectedSocketDeviceFromBroadcaster(BluetoothDevice device) {
        boolean isDisconnected = false;
        synchronized (this.incomingClientSockets) {
            for (GamePlayBroadcastSocket socket : this.incomingClientSockets) {
                BluetoothDevice connectedDevice = socket.getConnectedDevice();
                if (null != connectedDevice && connectedDevice.equals(device)) {
                    // this is this one
                    // close this removed client socket
                    socket.close();
                    isDisconnected = true;
                    break;
                }
            }
        }
        // remember that we are no longer trying with this one
        synchronized (this.devicesConnecting) {
            this.devicesConnecting.remove(device);
        }
        return isDisconnected;
    }

    public BluetoothDevice[] getConnectedSocketDevices() {
        List<BluetoothDevice> connectedDevices = new ArrayList<>();
        synchronized (this.incomingClientSockets) {
            for (GamePlayBroadcastSocket socket : this.incomingClientSockets) {
                BluetoothDevice connectedDevice = socket.getConnectedDevice();
                if (null != connectedDevice) {
                    connectedDevices.add(connectedDevice);
                }
            }
        }
        return connectedDevices.toArray(new BluetoothDevice[0]);
    }

    public BluetoothDevice[] getConnectedClientDevices() {
        List<BluetoothDevice> connectedDevices = new ArrayList<>();
        synchronized (this.outgoingClientSockets) {
            for (GamePlayBroadcastSocket socket : this.outgoingClientSockets.values()) {
                BluetoothDevice connectedDevice = socket.getConnectedDevice();
                if (null != connectedDevice) {
                    connectedDevices.add(connectedDevice);
                }
            }
        }
        return connectedDevices.toArray(new BluetoothDevice[0]);
    }

    public boolean isSocketDeviceConnecting(BluetoothDevice device) {
        synchronized (this.devicesConnecting) {
            return this.devicesConnecting.contains(device);
        }
    }

    public boolean isSocketDeviceConnected(BluetoothDevice device) {
        synchronized (this.incomingClientSockets) {
            for (GamePlayBroadcastSocket socket : this.incomingClientSockets) {
                BluetoothDevice connectedDevice = socket.getConnectedDevice();
                if (null != connectedDevice && connectedDevice.equals(device)) {
                    // this is connected
                    return true;
                }
            }
        }
        synchronized (this.outgoingClientSockets) {
            for (GamePlayBroadcastSocket socket : this.outgoingClientSockets.values()) {
                BluetoothDevice connectedDevice = socket.getConnectedDevice();
                if (null != connectedDevice && connectedDevice.equals(device)) {
                    // this is connected
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onSocketDisconnected(GamePlayBroadcastSocket disconnectedSocket, BluetoothSocket disconnectedBtSocket) {
        // one of our sockets disconnected, inform the listeners of this
        // find what it was, incoming client or outgoing client
        BluetoothDevice disconnectedDevice = disconnectedBtSocket.getRemoteDevice();
        synchronized (this.incomingClientSockets) {
            int index = this.incomingClientSockets.indexOf(disconnectedSocket);
            if (index != -1) {
                // remove this from our list
                this.incomingClientSockets.remove(index);
            }
        }
        synchronized (this.outgoingClientSockets) {
            if (null != disconnectedDevice) {
                // there is a disconnected device, is this an outgoing socket?
                this.outgoingClientSockets.remove(disconnectedDevice.getAddress());
            }
        }
        // remember that we are no longer trying with this one
        synchronized (this.devicesConnecting) {
            this.devicesConnecting.remove(disconnectedDevice);
        }
        // inform listeners that an incoming socket was disconnected
        synchronized (this.listeners) {
            for (GamePlayBroadcastListener listener : this.listeners) {
                listener.onDeviceSocketDisconnected(disconnectedDevice);
            }
        }
    }

    private void onSocketConnected(BluetoothSocket socket) {
        // someone connected to us, yey
        synchronized (this.incomingClientSockets) {
            this.incomingClientSockets.add(new GamePlayBroadcastSocket(socket, this));
        }
        BluetoothDevice connectedDevice = socket.getRemoteDevice();
        // remember that we are no longer trying with this one
        synchronized (this.devicesConnecting) {
            this.devicesConnecting.remove(connectedDevice);
        }
        synchronized (this.listeners) {
            for (GamePlayBroadcastListener listener : this.listeners) {
                listener.onDeviceSocketConnected(connectedDevice);
            }
        }
        // send this data to the newly connected socket
        sendMatchDataToConnectedClients(new PointChange[0]);
    }
}
