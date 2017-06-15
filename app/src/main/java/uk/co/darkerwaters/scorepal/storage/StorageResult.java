package uk.co.darkerwaters.scorepal.storage;

import com.google.firebase.database.DatabaseError;

/**
 * Created by douglasbrain on 14/06/2017.
 */

public abstract class StorageResult <T> {

    public abstract void onResult(T data);

    public void onCancelled(DatabaseError databaseError) {
        // nothing to do in the base
    }

}
