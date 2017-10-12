'use strict';

// Initialise the data object that will do all our data operations
function UmpyrDataFriends(umpyrData, database) {
    // remember the reference to umpyrData so we can pass back information to the master class
    this.umpyrData = umpyrData;
    this.database = database;

    // set our member data
    this.friends = [];

    // load the data
    this.loadFriends();
}

UmpyrDataFriends.prototype.friendOtherUser = function(targetUserId) {
    // create the friend object
    var friend = new Friend();
    // setup the members to represet us as a friend
    friend.ID = this.umpyrData.currentUser.ID;
    friend.nickname = this.umpyrData.currentUser.nickname;
    friend.email = this.umpyrData.currentUser.email;
    friend.photoUrl = this.umpyrData.currentUser.photoUrl;
    friend.isAccepted = true;
    // send this data to the specified target user, showing them that we are okay being their friend
    this.database.ref('/friends/' + targetUserId + "/" + this.umpyrData.currentUser.ID).set(friend.getFriendData());
    // if this is our friend then we want to remember that we sent this
    this.setFriendRequested(targetUserId);
};

UmpyrDataFriends.prototype.unfriendOtherUser = function(targetUserId) {
    // create the friend object
    var friend = new Friend();
    // setup the members to remove us as a friend
    friend.ID = this.umpyrData.currentUser.ID;
    friend.nickname = this.umpyrData.currentUser.nickname;
    friend.email = this.umpyrData.currentUser.email;
    friend.photoUrl = this.umpyrData.currentUser.photoUrl;
    friend.isAccepted = false;
    // send this data to the specified target user to make them stop using us as a friend
    this.database.ref('/friends/' + targetUserId + "/" + this.umpyrData.currentUser.ID).set(friend.getFriendData());
};

UmpyrDataFriends.prototype.getFriends = function() {
    if (this.friends) {
        // return the array of friends currently loaded
        return this.friends;
    } else {
        // return an empty array all the time so don't have to check
        return [];
    }
};

// Loads all our friends
UmpyrDataFriends.prototype.loadFriends = function() {
    // Load and listens for new friends
    this.friendsRef = this.database.ref('friends/' + this.umpyrData.currentUser.ID);
    // make sure we remove all previous listeners
    this.friendsRef.off();
    this.friends = [];
    var thisData = this;
    // load all our friends
    var setFriend = function(data) {
        // load the data into a nice object
        var friend = new Friend(data);
        // check this is not already in the list
        var isFriendExist = false;
        for (var i = 0; i < thisData.friends.length; i += 1) {
            if (friend.ID === thisData.friends[i].ID) {
                // this is the same, just update this
                thisData.friends[i] = friend;
                isFriendExist = true;
                break;
            }
        }
        if (!isFriendExist) {
            // just push to the end of the list
            thisData.friends.push(friend);
        }
        // inform any listeners of this change to the list
        thisData.umpyrData.informDataListenersOfChange(thisData.friends, friend, 'added');
    }.bind(this);
    this.friendsRef.on('child_added', setFriend);
    //this.friendsRef.on('child_changed', changeFriend);
};

UmpyrDataFriends.prototype.isFriendRequested = function(friendID) {
    // is the specified friend been sent a friend object, requested to be our friend...
    if (this.umpyrData.currentUser) {
        return this.umpyrData.currentUser.isFriendRequested(friendID);
    } else {
        // don't know, so no
        return false;
    }
};

UmpyrDataFriends.prototype.setFriendRequested = function(friendID) {
    if (this.umpyrData.currentUser) {
        // there is a user, set the friend requested status on this
        this.umpyrData.currentUser.setFriendRequested(friendID);
        var friendsRequested = this.umpyrData.currentUser.getFriendsRequested();
        // and write to the database the new value
        this.database.ref('users/' + this.umpyrData.currentUser.ID + "/friendsRequested").set(friendsRequested);
    }
};

UmpyrDataFriends.prototype.getFriendFromId = function(friendID) {
    if (this.friends && friendID) {
        for (var i = 0; i < this.friends.length; i += 1) {
            if (this.friends[i].ID === friendID) {
                // this is a match
                return this.friends[i];
            }
        }
    }
    // if here, no match
    return undefined;
};