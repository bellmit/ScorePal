package uk.co.darkerwaters.scorepal.storage;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by douglasbrain on 13/06/2017.
 */

@IgnoreExtraProperties
public class Match {

    private static final SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    public String ID;
    public String playerOne;
    public String playerTwo;
    public String scoreSummary;
    public int gameMode;
    public String matchPlayedDate;

    public Match() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Match(String ID, String playerOne, String playerTwo, String scoreSummary, int gameMode, Date matchPlayedDate) {
        this.ID = ID;
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.scoreSummary = scoreSummary;
        this.gameMode = gameMode;
        this.matchPlayedDate = fileDateFormat.format(matchPlayedDate);
    }

    @Exclude
    public void getMatches(DatabaseReference topLevel, String userId, final StorageResult<Match> result) {
        topLevel.child("matches").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // found some data, report this to the listener class
                Match match = dataSnapshot.getValue(Match.class);
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
    public void getMatch(DatabaseReference topLevel, String userId, String matchId, final StorageResult<Match> result) {
        topLevel.child("matches").child(userId).child(matchId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // found some data, report this to the listener class
                Match match = dataSnapshot.getValue(Match.class);
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
}
