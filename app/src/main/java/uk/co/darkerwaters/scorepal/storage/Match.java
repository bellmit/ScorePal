package uk.co.darkerwaters.scorepal.storage;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by douglasbrain on 13/06/2017.
 */

@IgnoreExtraProperties
public class Match {

    private static final SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    @Exclude
    public String userId;

    public String playerOne;
    public String playerTwo;
    public String scoreSummary;
    public int gameMode;
    public String matchPlayedDate;
    public String scoreData;

    public Match() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Match(User user, String playerOne, String playerTwo, String scoreSummary, ScoreData scoreData, Date matchPlayedDate) {
        this.userId = user.ID;
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.scoreSummary = scoreSummary;
        this.gameMode = scoreData.currentScoreMode.value;
        this.matchPlayedDate = fileDateFormat.format(matchPlayedDate);
        this.scoreData = scoreData.toString();
    }

    @Exclude
    public String getMatchId() {
        return matchPlayedDate;
    }

    @Exclude
    public String getPlayerOne() {
        return this.playerOne;
    }

    @Exclude
    public String getPlayerTwo() {
        return this.playerTwo;
    }

    @Exclude
    public String getScoreSummary() {
        return this.scoreSummary;
    }

    @Exclude
    public ScoreData getScoreData() {
        if (this.scoreData == null) {
            return new ScoreData();
        }
        else {
            return new ScoreData(new StringBuilder(this.scoreData));
        }
    }

    @Exclude
    public ScoreData.ScoreMode getScoreMode() {
        return ScoreData.ScoreMode.from(this.gameMode);
    }

    @Exclude
    public Date getMatchPlayedDate() {
        Date played = null;
        try {
            played = fileDateFormat.parse(this.matchPlayedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return played;
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
        topLevel.child("matches").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // this data is a list of the children of the matches node, get all these
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    // found a child, report this to the listener class
                    Match match = childDataSnapshot.getValue(Match.class);
                    // the match doesn't contain the user id - but we know it, so set it
                    match.userId = userId;
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
    public static void getMatch(DatabaseReference topLevel, final String userId, String matchId, final StorageResult<Match> result) {
        topLevel.child("matches").child(userId).child(matchId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // found some data, report this to the listener class
                Match match = dataSnapshot.getValue(Match.class);
                // the user ID isn't in the data (excluded) but we know it, so set it here
                match.userId = userId;
                // pass this to the caller
                result.onResult(match);
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
