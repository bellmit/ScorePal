package uk.co.darkerwaters.scorepal.storage;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.darkerwaters.scorepal.ScoreData;

@IgnoreExtraProperties
public class User {

    public enum ShareLevel {
        K_NONE(0),
        K_FRIENDSONLY(1),
        K_FRIENDSANDGROUPS(2),
        K_EVERYONE(9);

        public final int value;
        private ShareLevel(int value) {
            this.value = value;
        }

        // Mapping enum to id
        private static final Map<Integer, ShareLevel> valueMap = new HashMap<Integer, ShareLevel>();
        // initliase the map in a static global function
        static {
            for (ShareLevel mode : ShareLevel.values())
                valueMap.put(mode.value, mode);
        }

        public static ShareLevel from(int value) {
            ShareLevel mode = valueMap.get(value);
            if (null == mode) {
                return ShareLevel.K_NONE;
            }
            else {
                return mode;
            }
        }
    }
    // add all the members to store
    String ID;
    String nickname;
    int shareDetailsLevel;
    String email;
    String photoUrl;
    List<String> groupsOwned;
    List<String> friends;
    List<String> groups;

    public User() {
        // empty constructor needed for firebase construction
    }

    public User(String ID, String nickname) {
        this.ID = ID;
        this.nickname = nickname;
        this.shareDetailsLevel = ShareLevel.K_NONE.value;
        this.groupsOwned = new ArrayList<String>();
        this.friends = new ArrayList<String>();
        this.groups = new ArrayList<String>();
    }

    @Exclude
    public static void getUsers(DatabaseReference topLevel, final StorageResult<User> result) {
        topLevel.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // found some data, report this to the listener class
                User user = dataSnapshot.getValue(User.class);
                // pass this to the caller
                result.onResult(user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // inform the user of this
                result.onCancelled(databaseError);
            }
        });
    }

    @Exclude
    public static void getUser(DatabaseReference topLevel, String userID, final StorageResult<User> result) {
        topLevel.child("users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // found some data, report this to the listener class
                User user = dataSnapshot.getValue(User.class);
                // pass this to the caller
                result.onResult(user);
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
        User.setUserData(topLevel, this);
    }

    @Exclude
    public static void setUserData(DatabaseReference topLevel, User user) {
        // create the user node and set the data for it
        topLevel.child("/users/" + user.ID).setValue(user);
    }
}
