package uk.co.darkerwaters.scorepal.storage.uk.co.darkerwaters.scorepal.storage.data;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import uk.co.darkerwaters.scorepal.storage.ScoreData;
import uk.co.darkerwaters.scorepal.storage.StorageResult;


/**
 * Created by douglasbrain on 13/06/2017.
 */

@IgnoreExtraProperties
public class Score {

    // members to store / load / save from Firebase
    String matchID;
    String scoreData;

    Score() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Score(String matchID, ScoreData scoreData) {
        this.matchID = matchID;
        this.scoreData = scoreData.toString();
    }

    @Exclude
    public void getScores(DatabaseReference topLevel, final StorageResult<Score> result) {
        topLevel.child("scores").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // found some data, report this to the listener class
                Score score = dataSnapshot.getValue(Score.class);
                // pass this to the caller
                result.onResult(score);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // inform the user of this
                result.onCancelled(databaseError);
            }
        });
    }

    @Exclude
    public void getScore(DatabaseReference topLevel, String scoreID, final StorageResult<Score> result) {
        topLevel.child("scores").child(scoreID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // found some data, report this to the listener class
                Score score = dataSnapshot.getValue(Score.class);
                // pass this to the caller
                result.onResult(score);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // inform the user of this
                result.onCancelled(databaseError);
            }
        });
    }
}
