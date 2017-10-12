'use strict';

// Initializes the friends card and controls the data on the card
function FriendsCardController(parentDiv) {
    // initialise this class
    this.document = document;
    this.parentDiv = parentDiv;
    // and remember the very useful data controller object
    this.umpyrData = window.umpyr.umpyrData;

    this.friendList = this.parentDiv.querySelector('#friends');

    // remember the list of users that we will add to, each time we find a new person
    // that has sent us, or we have sent them, a message
    this.messagesList = this.parentDiv.querySelector('#friends-message-list');

    // now that the card is setup, get the data we want do display, listen for changes
    this.umpyrData.addDataListener(this, this.onDataChanged);
    // listening for changes now, but some probably already in the list
    var friends = this.umpyrData.friends.getFriends();
    var friendWithMessageUnread;
    for (var i = 0; i < friends.length; i += 1) {
        // display the friend
        this.displayFriend(friends[i]);
        // do they have something unread?
        if (!friendWithMessageUnread &&
            this.umpyrData.messages.getNumberMessagesUnread(friends[i].ID) > 0) {
            // this is the first friend with an unread message, show them selected
            friendWithMessageUnread = friends[i];
        }
    }
    if (!friendWithMessageUnread) {
        // there are no friends with unread messages, just select the first
        if (friends.length > 0) {
            this.selectFriend(friends[0], true);
        }
    } else {
        // select the first one found that had a message unread
        this.selectFriend(friendWithMessageUnread, true);
    }

    // show the button that will add a new friend
    this.document.querySelector('#find-new-friend-button').removeAttribute('hidden');
    this.document.querySelector('#find-new-friend-button').addEventListener(
        'click',
        function(e) {
            // show the find card
            window.umpyr.showCard(12);
        },
        false);
}

FriendsCardController.prototype.close = function(isLocalClose) {
    // called as this card is removed and closed
    this.umpyrData.removeDataListener(this);

    // hide the button that will add a new friend
    this.document.querySelector('#find-new-friend-button').setAttribute('hidden', 'true');
};

FriendsCardController.prototype.onDataChanged = function(dataList, data, reason) {
    // called as the data in the list changes, is it for this card to listen?
    if (dataList == this.umpyrData.friends.getFriends()) {
        // this is the friends list changed, this is for us
        if (reason === 'added' || reason === 'changed') {
            // this is a new friend, add it
            this.displayFriend(data);
        } else if (reason === 'removed') {
            // remove the div that represents the message from the list
            var div = this.parentDiv.querySelector('#fb' + data.id);
            if (div && div.parentElement) {
                div.parentElement.removeChild(div);
            }
        }
    } else if (dataList == this.umpyrData.messages.getMessages()) {
        // this is the messages list changed, this is for us
        if (reason === 'added' || reason === 'changed') {
            // this is a new message, add it
            this.displayMessage(data);
        } else if (reason === 'removed') {
            // remove the div that represents the message from the list
            var div = this.parentDiv.querySelector('#fb' + data.key);
            if (div && div.parentElement) {
                div.parentElement.removeChild(div);
            }
        }
        // a changed message can change our display of friends (number of unread messages) update them
        var friends = this.umpyrData.friends.getFriends();
        for (var i = 0; i < friends.length; i += 1) {
            this.displayFriend(friends[i]);
        }
    }
};

FriendsCardController.prototype.sendMessage = function(friend, textInput) {
    // Check if the user is signed-in before we do anything
    if (window.umpyr.checkSignedInWithMessage()) {
        // user is signed in, send the message
        window.umpyr.umpyrData.messages.sendUserMessage(friend.ID, textInput.value);
        mdlChange(textInput, "");
        //TODO! show the user that this message has been sent by showing the messages card
        console.debug('implement the showing of the messages card for this friend then...');
    }
};

FriendsCardController.prototype.selectFriend = function(friend, isSelected) {
    // get the row that is the friend
    var div = this.parentDiv.querySelector('#fb' + friend.ID);
    var selectableElement;
    if (div) {
        // get the selectable area
        selectableElement = div.querySelector('.friend-selectable');
    }
    // remove all the messages from the list
    while (this.messagesList.childNodes.length > 0) {
        this.messagesList.removeChild(this.messagesList.childNodes[0]);
    }
    // set the selection on the specified friend
    if (isSelected) {
        this.selectedFriend = friend;
        // listening for changes in messages already, but some probably already in the list
        var messages = this.umpyrData.messages.getMessages();
        for (var i = 0; i < messages.length; i += 1) {
            this.displayMessage(messages[i]);
        }
        if (selectableElement) {
            selectableElement.setAttribute('selected', 'true');
        }
    } else {
        this.selectedFriend = undefined;
        if (selectableElement) {
            selectableElement.removeAttribute('selected');
        }
    }
};

// Displays a friend in the UI.
FriendsCardController.prototype.displayFriend = function(friend) {
    var div = this.parentDiv.querySelector('#fb' + friend.ID);
    // If an element for that friend does not exists yet we create it.
    if (!div) {
        var container = document.createElement('div');
        if (!this.friendRowContent) {
            // load the raw content from the html file that we want to use
            this.friendRowContent = clientSideInclude('/components/friendrow.html');
        }
        container.innerHTML = this.friendRowContent;
        div = container.firstChild;
        // and put in the document
        this.friendList.appendChild(div);
        div.setAttribute('id', 'fb' + friend.ID);
        // there is a text input and label with an ID, we need to set these for them to work
        var textInput = div.querySelector('.mdl-textfield__input');
        div.querySelector('.mdl-textfield__input').setAttribute('id', 'fb' + friend.ID + "_input");
        div.querySelector('.mdl-textfield__label').setAttribute('for', 'fb' + friend.ID + "_input");
        // and listen for changes
        var messageChangeFunction = function() {
            if (textInput.value) {
                sendMessageButton.removeAttribute('disabled');
            } else {
                sendMessageButton.setAttribute('disabled', 'true');
            }
        };
        // and handle them clicking the button
        var thisController = this;
        var sendMessageButton = div.querySelector('.message-friend-button');
        sendMessageButton.addEventListener('click', function(e) {
            e.preventDefault();
            // send the message
            thisController.sendMessage(friend, textInput);
            // this changes the message, deletes the contents
            messageChangeFunction();
        });
        textInput.addEventListener('keyup', messageChangeFunction);
        textInput.addEventListener('change', messageChangeFunction);
        // setup the click on the selectable area to select this friend
        var selectableFriend = div.querySelector('.friend-selectable');
        var thisController = this;
        selectableFriend.addEventListener(
            'click',
            function(e) {
                if (selectableFriend.getAttribute('selected')) {
                    // de-select this friend
                    thisController.selectFriend(friend, false);
                } else {
                    // select this friend
                    thisController.selectFriend(friend, true);
                }
            },
            false);
    }
    if (friend.photoUrl) {
        div.querySelector('.pic').style.backgroundImage = 'url(' + friend.photoUrl + ')';
    }
    var noUnread = this.umpyrData.messages.getNumberMessagesUnread(friend.ID);
    div.querySelector('.friend-name').textContent = friend.nickname + ' (' + noUnread + ')';
    div.querySelector('.friend-description').textContent = friend.email + " is your friend";
    if (!window.umpyr.umpyrData.friends.isFriendRequested(friend.ID)) {
        var button = div.querySelector('.reciprocate-friend-button');
        var thisController = this;
        button.removeAttribute('hidden');
        button.addEventListener('click', function(e) {
            e.preventDefault();
            // set the member data to that from the friend
            thisController.enteredFriendEmail = friend.email;
            thisController.enteredFriendId = friend.ID;
            thisController.friendOtherUser();
            button.setAttribute('hidden', true);
        });
    }
    // Show the card fading-in and scroll to view the new friend
    fadeElement(div, true, function() {
        // update the google MDL stuff
        mdlCleanup(div);
    });
    this.friendList.scrollTop = this.friendList.scrollHeight;
    this.friendList.focus();
};

// Displays a message in the UI.
FriendsCardController.prototype.displayMessage = function(message) {
    // first find the user that sent / received
    var userId = undefined;
    var sentToUs = false;
    if (this.umpyrData.getUserId() === message.toId) {
        // this is a message to us from someone else
        userId = message.fromId;
        if (userId === this.umpyrData.getUserId()) {
            // this is a message from us, to us. forget this...
            console.error("Found a message from ourselves to ourselves, ignoring...");
            return;
        }
        sentToUs = true;
    } else {
        // this is a message from us to someone else
        userId = message.toId;
    }
    //TODO show the message only if for the selected friend...
    if (!this.selectedFriend || userId !== this.selectedFriend.ID) {
        // this is not the friend we have selected, forget this
        return;
    }

    //TODO - do we want to be able to delete all messages from a user in one go? If so then show this button...
    // now we have the user div on the cars, find the message on this...
    var div = this.parentDiv.querySelector('#fb' + message.key);
    // If an element for that message does not exist yet we create it.
    var messageAdded = false;
    if (!div) {
        var container = document.createElement('div');
        if (!sentToUs) {
            if (!this.messageRowContent) {
                // load the raw content from the html file that we want to use
                this.messageRowContent = clientSideInclude('/components/messagerowleft.html');
            }
            // set the content of the container
            container.innerHTML = this.messageRowContent;
        } else {
            if (!this.messageRowContentRight) {
                // load the raw content from the html file that we want to use
                this.messageRowContentRight = clientSideInclude('/components/messagerowright.html');
            }
            // set the content of the container
            container.innerHTML = this.messageRowContentRight;
        }
        // and add the container to the div we made for the purpose
        div = container.firstChild;
        // set the ID so we find it the next time
        div.setAttribute('id', 'fb' + message.key);
        if (!sentToUs) {
            // we sent this so we must have read it when we sent it
            div.removeAttribute('unread');
        } else if (!message.isRead) {
            // this was received - and hasn't been read yet, mart it as so
            div.setAttribute('unread', 'true');
            // handle the reading of this message
            var timer;
            var thisController = this;
            div.addEventListener('mouseenter',
                function() {
                    timer = setTimeout(function() {
                        if (!message.isRead) {
                            // tell the data that this message is now read
                            thisController.umpyrData.messages.setMessageAsRead(message, true);
                            thisController.displayFriend(thisController.selectedFriend);
                            // and set the local data
                            message.isRead = true;
                            div.removeAttribute('unread');
                        }
                    }, 500);
                });
            div.addEventListener('mouseleave',
                function() {
                    clearInterval(timer);
                });
        }

        // and the date so we can put it in the right place in the list...
        div.setAttribute('receivedDate', message.receivedDate);
        // find the correct position for this message, based on it's receivedDate which can be sorted alphabetically
        var isInserted = false;
        for (var i = 0; i < this.messagesList.childNodes.length; i += 1) {
            if (this.messagesList.childNodes[i].getAttribute) {
                // this is an actual child that has attributes, check this
                var childReceivedDate = this.messagesList.childNodes[i].getAttribute('receivedDate');
                if (message.receivedDate >= childReceivedDate) {
                    // received date goes before here
                    this.messagesList.insertBefore(div, this.messagesList.childNodes[i]);
                    isInserted = true;
                    break;
                }
            }
        }
        if (!isInserted) {
            // goes nowhere, put it at the bottom
            this.messagesList.appendChild(div);
        }

        // remember to show this
        messageAdded = true;
    }
    //get the image of the ID of the sender of this message
    var userIcon = this.umpyrData.getUserIcon(message.fromId);
    if (userIcon) {
        div.querySelector('.pic').style.backgroundImage = 'url(' + userIcon + ')';
    }
    div.querySelector('.name').textContent = message.fromNickname;
    div.querySelector('.message-date').textContent = dateFormat(message.getReceivedDateFormatted(), "default");

    if (sentToUs && !message.isRead) {
        // this was received - and hasn't been read yet, mart it as so
        div.setAttribute('unread', 'true');
    }

    var messageElement = div.querySelector('.card-contents');
    // do the text content of the message
    messageElement.innerHTML = message.contents;
    // Replace all line breaks by <br>.
    messageElement.innerHTML = messageElement.innerHTML.replace(/\n/g, '<br>');
    if (messageAdded) {
        // show the contents fading in
        fadeElement(div, true, function() {
            // update the google MDL stuff
            mdlCleanup(div);
        });
    }

    // we added the message ok, also handle it's deletion
    var deleteButton = div.querySelector('.message-delete-button');
    var thisController = this;
    deleteButton.removeAttribute('hidden');
    deleteButton.addEventListener('click',
        function() {
            // clicked delete, delete it then
            thisController.umpyrData.messages.deleteMessage(message);
        },
        false
    );
};