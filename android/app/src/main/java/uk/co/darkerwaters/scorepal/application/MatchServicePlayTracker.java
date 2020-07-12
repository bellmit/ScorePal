package uk.co.darkerwaters.scorepal.application;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Calendar;
import java.util.Date;

import uk.co.darkerwaters.scorepal.data.Match;

public class MatchServicePlayTracker  {

    private final MatchService service;

    private Date playEnded;
    private Date playStarted;

    private final FusedLocationProviderClient fusedLocationClient;
    private Location location = null;

    public MatchServicePlayTracker(MatchService parentService) {
        // construct this
        this.service = parentService;
        // setup our initial members
        this.playEnded = null;
        this.location = null;
        // we will also need a location to store (maybe)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(parentService);
        // have started then
        playStarted = Calendar.getInstance().getTime();
        Match activeMatch = service.getActiveMatch();
        // set this on the match if there isn't a match date played yet
        if (null != activeMatch && null == activeMatch.getDateMatchStarted()) {
            // there are settings, but there is no match played date, set one
            activeMatch.setDateMatchStarted(playStarted);
        }
        // and get the location for later
        if (ApplicationState.Instance().getPreferences().getIsStoreLocations()) {
            // to store a location we need a location
            if (ActivityCompat.checkSelfPermission(service, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(service, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // we have permission to ask for location of some kind, get the location for this match being started
                this.fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                MatchServicePlayTracker.this.setCurrentLocation(location);
                            }
                        });
            }
        }
    }

    public void destroy(boolean isStoreResults) {
        if (isStoreResults) {
            // store these results for sure
            storeMatchResults(true, false);
        }
    }

    public void stopPlay() {
        // set the time at which play ended
        playEnded = Calendar.getInstance().getTime();
    }

    private void clearEndedPlay() {
        if (null != this.playEnded) {
            this.playEnded = null;
        }
    }

    private synchronized void setCurrentLocation(Location location) {
        this.location = location;
    }

    private synchronized Location getCurrentLocation() {
        return ApplicationState.Instance().getPreferences().getIsStoreLocations() ? this.location : null;
    }

    public void handlePlayEnding() {
        Match activeMatch = service.getActiveMatch();
        // is the match over
        if (null != activeMatch && activeMatch.isMatchOver()) {
            // the match is over
            if (null == this.playEnded) {
                // the match is over but play hasn't ended - yes it has!
                stopPlay();
            }
        } else {
            // the match is not over, this might be from an undo, get rid of the time either way
            clearEndedPlay();
        }
    }

    public int getMatchTimePlayed() {
        int timePlayed;
        Match activeMatch = service.getActiveMatch();
        timePlayed = activeMatch == null ? 0 : activeMatch.getMatchTimePlayed();
        int activityTime = getTimePlayed();
        if (activityTime >= 0) {
            timePlayed += activityTime;
        }
        return timePlayed;
    }

    public void storeCurrentState(boolean areResultsAccepted) {
        if (null != this.playStarted) {
            // and add the time played in this session to the active match
            int activityTime = getTimePlayed();
            Match activeMatch = service.getActiveMatch();
            if (activityTime > 0 && null != activeMatch) {
                activeMatch.addMatchTimePlayed(activityTime);
            }
            // now we added these time, we need to not add them again, reset the
            // play started time to be now
            this.playStarted = Calendar.getInstance().getTime();
        }
        // store the match results
        storeMatchResults(true, areResultsAccepted);
    }

    private int getTimePlayed() {
        if (null == this.playStarted) {
            return 0;
        }
        else {
            long playEndedMs;
            if (null == this.playEnded) {
                // play isn't over yet, use now
                playEndedMs = Calendar.getInstance().getTimeInMillis();
            } else {
                // use the play ended time
                playEndedMs = this.playEnded.getTime();
            }
            // Calculate difference in milliseconds
            long diff = playEndedMs - this.playStarted.getTime();
            // and add the time played in seconds to the active match
            return (int) (diff / 1000L);
        }
    }

    private void storeMatchResults(boolean storeIfPersisted, boolean areResultsAccepted) {
        Match activeMatch = service.getActiveMatch();
        if (null != activeMatch) {
            // set the location of this active match
            Location currentLocation = getCurrentLocation();
            if (null != currentLocation) {
                activeMatch.setPlayedLocation(currentLocation);
            }
            // store the results of the match we started
            MatchPersistenceManager persistenceManager = MatchPersistenceManager.GetInstance();
            if (storeIfPersisted || false == persistenceManager.isMatchDataPersisted(activeMatch)) {
                // we are forcing a save, or the data is different, so save
                if (areResultsAccepted) {
                    // accept the results
                    persistenceManager.saveAcceptedMatchToFile(activeMatch, service);
                }
                else {
                    // just save the results as they are
                    persistenceManager.saveMatchToFile(activeMatch, service);
                }
            }
        }
    }
}
