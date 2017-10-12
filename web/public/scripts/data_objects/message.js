'use strict';


// Initializes the message object
function Message(data) {
    if (!data) {
        // initialise defaults
        this.key = "";
        this.receivedDate = this.createDateString();
        this.fromId = "";
        this.toId = "";
        this.fromNickname = "";
        this.contents = "";
        this.isRead = false;
    } else {
        var val = data.val();
        this.key = data.key;
        this.receivedDate = val.receivedDate;
        this.toId = val.toId;
        this.fromId = val.fromId;
        this.fromNickname = val.fromNickname;
        this.contents = val.contents;
        this.isRead = val.isRead || false;
    }
}

Message.prototype.getMessageData = function() {
    // return this as a data object to set in firebase - will get back in constructor
    return {
        receivedDate: this.receivedDate || this.createDateString(),
        toId: this.toId,
        fromId: this.fromId,
        fromNickname: this.fromNickname,
        contents: this.contents,
        isRead: this.isRead || false,
    };
};

Message.prototype.createDateString = function(date) {
    if (!date) {
        date = new Date();
    }
    return date.getFullYear() +
        ("0" + (date.getMonth() + 1)).slice(-2) +
        ("0" + date.getDate()).slice(-2) +
        ("0" + date.getHours()).slice(-2) +
        ("0" + date.getMinutes()).slice(-2) +
        ("0" + date.getSeconds()).slice(-2);
};

Message.prototype.getReceivedDateFormatted = function(date) {
    if (!date) {
        date = this.receivedDate;
    }
    if (!date) {
        date = this.createDateString();
    }
    var yr = parseInt(date.substring(0, 4));
    var mon = parseInt(date.substring(4, 6));
    var dt = parseInt(date.substring(6, 8));
    var hr = parseInt(date.substring(8, 10));
    var mn = parseInt(date.substring(10, 12));
    var sc = parseInt(date.substring(12, 14));
    return new Date(yr, mon - 1, dt, hr, mn, sc);
};