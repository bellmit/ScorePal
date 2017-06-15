package uk.co.darkerwaters.scorepal.storage;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;


/**
 * Created by douglasbrain on 13/06/2017.
 */

@IgnoreExtraProperties
public class Score {

    public String matchID;
    public String scoreData;

    public Score() {
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
