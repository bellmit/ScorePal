'use strict';

// Initialise the data object that will do all our data operations
function UmpyrDataMessages(umpyrData, database) {
    // remember the reference to umpyrData so we can pass back information to the master class
    this.umpyrData = umpyrData;
    this.database = database;

    // set our member data
    this.messages = [];
    this.messagesSubmitted = [];

    // load the data
    this.loadMessages();
}

UmpyrDataMessages.prototype.getMessages = function() {
    if (this.messages) {
        // return the array of messages currently loaded
        return this.messages;
    } else {
        // return an empty array all the time so don't have to check
        return [];
    }
};

UmpyrDataMessages.prototype.getSiteMessages = function() {
    if (this.messagesSubmitted) {
        // return the array of messages submitted to the site that are currently loaded
        return this.messagesSubmitted;
    } else {
        // return an empty array all the time so don't have to check
        return [];
    }
};

UmpyrDataMessages.prototype.getNumberMessagesUnread = function(fromId) {
    var messagesUnread = 0;
    if (this.messages) {
        // return the number of messages we haven't read yet
        var userId = this.umpyrData.getUserId();
        for (var i = 0; i < this.messages.length; i += 1) {
            if (!this.messages[i].isRead) {
                // this is unread
                if (this.messages[i].fromId !== userId &&
                    (fromId === undefined || fromId === this.messages[i].fromId)) {
                    // this is an unread message (not from us) that is either from the specified friend, or total
                    messagesUnread += 1;
                }
            }
        }
    }
    return messagesUnread;
};

// Loads all our data
UmpyrDataMessages.prototype.loadMessages = function() {
    // Load and listens for new messages
    this.messagesRef = this.database.ref('messages/' + this.umpyrData.currentUser.ID);
    // make sure we remove all previous listeners
    this.messagesRef.off();
    this.messages = [];
    var thisData = this;
    // load all our messages
    var setMessage = function(data) {
        // load the data into a nice object
        var message = new Message(data);
        // check this is not already in the list
        var isMessageExist = false;
        for (var i = 0; i < thisData.messages.length; i += 1) {
            if (message.key === thisData.messages[i].key) {
                // this is the same, just update this
                thisData.messages[i] = message;
                isMessageExist = true;
                break;
            }
        }
        if (!isMessageExist) {
            // just push to the end of the list
            thisData.messages.push(message);
        }
        // inform any listeners of this change to the list
        thisData.umpyrData.informDataListenersOfChange(thisData.messages, message, 'added');
    }.bind(this);
    this.messagesRef.on('child_added', setMessage);
    //this.messagesRef.on('child_changed', changeMessage);

    // load the admin messages
    this.adminRef = this.database.ref('messages_submitted/' + this.umpyrData.currentUser.ID);
    this.adminRef.off();
    this.adminRef.on('child_added', function(data) {
        // each child is all the messages from a single user
        data.forEach(function(child) {
            //var key = child.key;
            //var value = child.val();
            // load the data into a nice object
            var message = new Message(child);
            // check this is not already in the list
            var isMessageExist = false;
            for (var i = 0; i < thisData.messagesSubmitted.length; i += 1) {
                if (message.key === thisData.messagesSubmitted[i].key) {
                    // this is the same, just update this
                    thisData.messagesSubmitted[i] = message;
                    isMessageExist = true;
                    break;
                }
            }
            if (!isMessageExist) {
                // just push to the end of the list
                thisData.messagesSubmitted.push(message);
            }
            // inform any listeners of this change to the list
            thisData.umpyrData.informDataListenersOfChange(thisData.messagesSubmitted, message, 'added');
        });
    }, function(errorObject) {
        console.log("The read failed: " + errorObject.code);
    });
};

UmpyrDataMessages.prototype.removeFromList = function(message, messageList) {
    if (messageList) {
        for (var i = 0; i < messageList.length; i += 1) {
            if (message == messageList[i]) {
                // this is the one, remove this
                messageList.splice(i, 1);
                break;
            }
        }
    }
    // inform any listeners of this change
    this.umpyrData.informDataListenersOfChange(messageList, message, 'removed');
};

UmpyrDataMessages.prototype.deleteMessage = function(message) {
    // remove this message from the Firebase Database.
    var thisData = this;
    this.database.ref('messages/' + this.umpyrData.currentUser.ID + "/" + message.key).remove(function(error) {
        if (!error) {
            // removed, remove from our list here
            thisData.removeFromList(message, thisData.messages);
        }
    });
    return true;
};

UmpyrDataMessages.prototype.setMessageAsRead = function(message, isRead) {
    // create the update, just for the flag that signals it has been read
    this.database.ref('messages/' + this.umpyrData.currentUser.ID + "/" + message.key)
        .update({ isRead: isRead });
};

UmpyrDataMessages.prototype.sendUserMessage = function(targetUserId, messageContent) {
    var message = new Message();
    message.toId = targetUserId;
    message.fromId = this.umpyrData.currentUser.ID;
    message.fromNickname = this.umpyrData.currentUser.nickname;
    message.contents = messageContent;
    // get this as data to send
    var data = message.getMessageData();
    // send this message to the specified target user
    this.database.ref('/messages/' + targetUserId).push(data);
    // but we can't read this, let's save the sent message in our list then so we remember we sent it
    // we have sent this so we must have read it, adjust the data accordingly
    data.isRead = true;
    // and place in our list of messages sent
    this.database.ref('/messages/' + this.umpyrData.currentUser.ID).push(data);
};

UmpyrDataMessages.prototype.sendSiteMessage = function(messageContent) {
    var message = new Message();
    message.fromId = this.umpyrData.currentUser.ID;
    message.fromNickname = this.umpyrData.currentUser.nickname;
    message.contents = messageContent;
    // send this message to website admin who can read them
    this.database.ref('/messages_submitted/unOMG3gEibPh76lSVdQfdKrNr163/' + message.fromId).push(message.getMessageData());
};

UmpyrDataMessages.prototype.deleteSiteMessage = function(message) {
    // remove this site message from the Firebase Database.
    var thisData = this;
    this.database.ref('messages_submitted/' + this.umpyrData.currentUser.ID + "/" + message.fromId + "/" + message.key).remove(function(error) {
        if (!error) {
            // removed, remove from our list here
            thisData.removeFromList(message, thisData.messagesSubmitted);
        }
    });
    return true;
};