'use strict';


// Initializes the friend object
function Friend(data) {
    if (!data) {
        // initialise defaults
        this.ID = "";
        this.nickname = "";
        this.email = "";
        this.photoUrl = "";
    } else {
        var val = data.val();
        this.ID = val.ID;
        this.nickname = val.nickname;
        this.email = val.email;
        this.photoUrl = val.photoUrl;
    }
}

Friend.prototype.getFriendData = function() {
    // return this as a data object to set in firebase - will get back in constructor
    return {
        ID: this.ID,
        nickname: this.nickname,
        email: this.email,
        photoUrl: this.photoUrl
    };
};