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
public class Post {
    public String ID;
    public String title;
    public String content;
    public String author;
    public int type;

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Post(String ID, String title, String content) {
        this.ID = ID;
        this.title = title;
        this.content = content;
    }

    @Exclude
    public void getPosts(DatabaseReference topLevel, final StorageResult<Post> result) {
        topLevel.child("posts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // found some data, report this to the listener class
                Post post = dataSnapshot.getValue(Post.class);
                // pass this to the caller
                result.onResult(post);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // inform the user of this
                result.onCancelled(databaseError);
            }
        });
    }
}
