package uk.co.darkerwaters.scorepal.storage.uk.co.darkerwaters.scorepal.storage.data;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.co.darkerwaters.scorepal.activities.MainActivity;
import uk.co.darkerwaters.scorepal.storage.ScoreData;
import uk.co.darkerwaters.scorepal.storage.StorageResult;


/**
 * Created by douglasbrain on 13/06/2017.
 */

@IgnoreExtraProperties
public class Match {

    private static final SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    @Exclude
    private String userId;

    String description;
    boolean isDoubles;
    String isUserPlayed;
    String isUserWinner;
    String matchPlayedDate;
    String playerOneId;
    String playerOnePartnerId;
    String playerOnePartnerTitle;
    String playerOneTitle;
    String playerTwoId;
    String playerTwoPartnerId;
    String playerTwoPartnerTitle;
    String playerTwoTitle;
    String scoreState;
    String sport;
    //TODO save the location the match was played at
    @Exclude
    private ScoreData currentScoreData;
    @Exclude
    private User playerOneUser;
    @Exclude
    private User playerTwoUser;
    @Exclude
    private String id;

    Match() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Match(User user, String playerOne, String playerTwo, ScoreData scoreData, Date date) {
        setCurrentUser(user);
        setPlayerOne(null, playerOne);
        setPlayerTwo(null, playerTwo);
        setMatchPlayedDate(date);
        setCurrentScoreData(scoreData);
    }

    @Exclude
    public String getUserId() {
        return this.userId;
    }

    @Exclude
    public String getMatchId() {
        return this.id == null ? this.matchPlayedDate : this.id;
    }

    @Exclude
    public String getPlayerOneTitle() {
        return this.playerOneTitle;
    }

    @Exclude
    public String getPlayerTwoTitle() {
        return this.playerTwoTitle;
    }

    @Exclude
    public void setCurrentUser(User user) {
        if (null != user) {
            this.userId = user.ID;
        }
        else {
            this.userId = "";
        }
    }

    @Exclude
    public ScoreData getScoreData() {
        if (null == this.currentScoreData) {
            // no member, create it from the loaded string data
            this.currentScoreData = new ScoreData(new StringBuilder(this.scoreState));
        }
        // return the data member as the object which is more helpful
        return this.currentScoreData;
    }

    @Exclude
    public void setCurrentScoreData(ScoreData newData) {
        // store the actual object
        this.currentScoreData = newData;
        // and the string which will go into the Firebase DB
        this.scoreState = newData == null ? null : newData.toString();
    }

    public String createScoreDataMessage() {
        return "{" + scoreState.toString() + "}";
    }

    @Exclude
    public ScoreData.ScoreMode getScoreMode() {
        if (null == this.currentScoreData) {
            // there is no score data, build the data from the string here
            getScoreData();
        }
        if (null == this.currentScoreData || null == this.currentScoreData.currentScoreMode) {
            return ScoreData.ScoreMode.K_POINTS;
        }
        else {
            return this.currentScoreData.currentScoreMode;
        }
    }

    @Exclude
    public Date getMatchPlayedDate() {
        Date played = null;
        try {
            played = fileDateFormat.parse(this.matchPlayedDate);
        } catch (ParseException e) {
            Log.e(MainActivity.TAG, e.getMessage());
        }
        return played;
    }

    @Exclude
    public void setMatchPlayedDate(Date date) {
        this.matchPlayedDate = fileDateFormat.format(date);
    }

    @Exclude
    public void setPlayerOne(User user, String userTitle) {
        this.playerOneTitle = userTitle;
        this.playerOneUser = user;
        this.playerOneId = this.playerOneUser == null ? "" : this.playerOneUser.ID;
    }

    @Exclude
    public void setPlayerTwo(User user, String userTitle) {
        this.playerTwoTitle = userTitle;
        this.playerTwoUser = user;
        this.playerTwoId = this.playerTwoUser == null ? "" : this.playerTwoUser.ID;
    }

    @Exclude
    public User getPlayerOneUser () {
        return this.playerOneUser;
    }

    @Exclude
    public User getPlayerTwoUser () {
        return this.playerTwoUser;
    }

    public static boolean isFileDatesSame(Date fileDate1, Date fileDate2) {
        // compare only up to seconds as only up to seconds stored in the filename
        // for simplicities sake we can use the same formatter we use for the filename and compare strings
        if (fileDate1 != null && fileDate2 == null) {
            return false;
        }
        else if (fileDate1 == null && fileDate2 != null) {
            return false;
        }
        else if (fileDate1 == null && fileDate2 == null) {
            return true;
        }
        else {
            // do the actual comparing then
            String stringDate1 = fileDateFormat.format(fileDate1);
            String stringDate2 = fileDateFormat.format(fileDate2);
            return stringDate1.equals(stringDate2);
        }
    }

    @Exclude
    public static void getMatches(DatabaseReference topLevel, final String userId, final StorageResult<Match> result) {
        // get the matches (ordered by key, so newest will be last) and limit to the last 30 to not overload our list
        topLevel.child("matches").child(userId).orderByKey().limitToLast(30).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // this data is a list of the children of the matches node, get all these
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    // found a child, report this to the listener class
                    Match match = childDataSnapshot.getValue(Match.class);
                    // the match doesn't contain the user id - but we know it, so set it
                    match.userId = userId;
                    match.id = childDataSnapshot.getKey();
                    // pass this to the caller
                    result.onResult(match);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // inform the user of this
                result.onCancelled(databaseError);
            }
        });
    }

    @Exclude
    public static void getMatch(DatabaseReference topLevel, final String userId, final String matchId, final StorageResult<Match> result) {
        topLevel.child("matches").child(userId).child(matchId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // found some data, report this to the listener class
                Match match = dataSnapshot.getValue(Match.class);
                if (null != match) {
                    // the user ID isn't in the data (excluded) but we know it, so set it here
                    match.userId = userId;
                    match.id = dataSnapshot.getKey();
                    // pass this to the caller
                    result.onResult(match);
                }
                else {
                    Log.e(MainActivity.TAG, "Failed to find the match: " + matchId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // inform the user of this
                result.onCancelled(databaseError);
            }
        });
    }

    @Exclude
    public void updateInDatabase(DatabaseReference topLevel) {
        Match.setMatchData(topLevel, this);
    }

    @Exclude
    public static void setMatchData(DatabaseReference topLevel, Match match) {
        // create the match node and set the data for it
        topLevel.child("/matches/" + match.userId + "/" + match.getMatchId()).setValue(match);
    }

    public static void delete(DatabaseReference topLevel, Match match) {
        topLevel.child("/matches/" + match.userId + "/" + match.getMatchId()).removeValue();
    }

    public void delete(DatabaseReference topLevel) {
        topLevel.child("/matches/" + userId + "/" + getMatchId()).removeValue();
    }
}
