'use strict';


// Initializes the user object
function User(data) {
    if (!data) {
        // initialise defaults
        this.ID = "";
        this.email = "";
        this.nickname = "";
        this.pages = ".9.";
        this.friendsRequested = [];
        this.matches_stats = {};
        this.photoUrl = Post.PHOTO_PLACEHOLDER_URL;
    } else {
        this.ID = data.ID;
        this.email = data.email;
        this.nickname = data.nickname;
        this.pages = data.pages;
        this.matches_stats = data.matches_stats || {};
        this.friendsRequested = data.friendsRequested || [];
        this.photoUrl = data.photoUrl || Post.PHOTO_PLACEHOLDER_URL;
    }
}

User.prototype.getUserData = function() {
    // return this as a data object to set in firebase - will get back in constructor
    return {
        ID: this.ID,
        email: this.email,
        nickname: this.nickname,
        pages: this.pages || ".9.",
        matches_stats: this.matches_stats,
        friendsRequested: this.friendsRequested || [],
        photoUrl: this.photoUrl || Post.PHOTO_PLACEHOLDER_URL,
    };
};

User.prototype.setPageOn = function(page) {
    // add the page number to the array of pages in this structure
    if (!this.isPageOn(page)) {
        // this page isn't on already, add it
        if (!this.pages) {
            // setup the new array
            this.pages = '.' + page + '.';
        } else {
            // add to the array (always starts and ends with a dot)
            this.pages += page + '.';
        }
    }
};

User.prototype.setPageOff = function(page) {
    // remove the page number from the array of pages in this structure
    if (this.pages) {
        this.pages = this.pages.replace('.' + page + '.', '.');
    }
};

User.prototype.isPageOn = function(page) {
    // return if the array of page numbers contains the one requested
    return this.pages && this.pages.indexOf('.' + page + '.') !== -1;
}

User.prototype.getActivePages = function() {
    return this.pages || '.9.';
};

User.prototype.isFriendRequested = function(friendID) {
    if (this.friendsRequested) {
        for (var i = 0; i < this.friendsRequested.length; i += 1) {
            if (this.friendsRequested[i] === friendID) {
                // this ID is in the list, return true
                return true;
            }
        }
    }
    // if here, then no
    return false;
};

User.prototype.setFriendRequested = function(friendID) {
    if (!this.isFriendRequested(friendID)) {
        // not already
        if (!this.friendsRequested) {
            // create the list
            this.friendsRequested = [];
        }
        this.friendsRequested.push(friendID);
    }
};

User.prototype.getFriendsRequested = function() {
    return this.friendsRequested || [];
};

User.prototype.getMatchesStats = function() {
    return this.matches_stats || {};
};