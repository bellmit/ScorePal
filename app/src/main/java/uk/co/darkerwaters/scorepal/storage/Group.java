package uk.co.darkerwaters.scorepal.storage;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Group {
    public String ID;
    public String name;
    public String owner;
    public String location;
    List<String> members;
    List<String> matches;


    public Group() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Group(String ID, String name, User owner, String location) {
        this.ID = ID;
        this.name = name;
        this.owner = owner.ID;
        this.location = location;

        this.members = new ArrayList<String>();
        this.matches = new ArrayList<String>();
    }

    @Exclude
    public void getGroups(DatabaseReference topLevel, final StorageResult<Group> result) {
        topLevel.child("groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // found some data, report this to the listener class
                Group group = dataSnapshot.getValue(Group.class);
                // pass this to the caller
                result.onResult(group);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // inform the user of this
                result.onCancelled(databaseError);
            }
        });
    }

    @Exclude
    public void getGroup(DatabaseReference topLevel, String groupID, final StorageResult<Group> result) {
        topLevel.child("groups").child(groupID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // found some data, report this to the listener class
                Group group = dataSnapshot.getValue(Group.class);
                // pass this to the caller
                result.onResult(group);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // inform the user of this
                result.onCancelled(databaseError);
            }
        });
    }
}
