'use strict';

// Initializes the messages display
function MessagesCardController(parentDiv, isSiteMessages) {
    // initialise this class
    this.document = document;
    this.parentDiv = parentDiv;
    this.isSiteMessages = isSiteMessages;
    // and remember the very useful data controller object
    this.umpyrData = window.umpyr.umpyrData;

    // remember the list of users that we will add to, each time we find a new person
    // that has sent us, or we have sent them, a message
    this.usersList = this.parentDiv.querySelector('#message-users');

    // now that the card is setup, get the data we want do display, listen for changes
    this.umpyrData.addDataListener(this, this.onDataChanged);
    // listening for changes now, but some probably already in the list
    var messages;
    if (this.isSiteMessages) {
        messages = this.umpyrData.messages.getSiteMessages();
    } else {
        messages = this.umpyrData.messages.getMessages();
    }
    for (var i = 0; i < messages.length; i += 1) {
        this.displayMessage(messages[i]);
    }
}

MessagesCardController.prototype.close = function(isLocalClose) {
    // called as this card is removed and closed
    this.umpyrData.removeDataListener(this);
};

MessagesCardController.prototype.onDataChanged = function(dataList, data, reason) {
    // called as the data in the list changes, is it for this card to listen?
    if ((this.isSiteMessages && dataList == this.umpyrData.messages.getSiteMessages()) ||
        (!this.isSiteMessages && dataList == this.umpyrData.messages.getMessages())) {
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
    }
};

// Displays a message in the UI.
MessagesCardController.prototype.displayMessage = function(message) {
    // first find the user that sent / received
    var userDiv = undefined;
    var userId = undefined;
    var userPic = undefined;
    var sentFromUs = false;
    if (this.umpyrData.getUserId() === message.toId) {
        // this is a message to us from someone else
        userId = message.fromId;
        if (userId === this.umpyrData.getUserId()) {
            // this is a message from us, to us. forget this...
            console.error("Found a message from ourselves to ourselves, ignoring...");
            return;
        }
        sentFromUs = true;
    } else {
        // this is a message from us to someone else
        userId = message.toId;
    }
    // find the element for this user id
    userDiv = this.parentDiv.querySelector('#fb' + userId + 'messages');
    var messageList = undefined;
    var userAdded = false;
    if (!userDiv) {
        // this doesn't exist, create the object for this user
        var container = document.createElement('div');
        if (!this.messageUserRowContent) {
            // load the raw content from the html file that we want to use
            this.messageUserRowContent = clientSideInclude('/components/messageuserrow.html');
        }
        // and set the content of the container
        container.innerHTML = this.messageUserRowContent;
        // add the content to the div we created
        userDiv = container.firstChild;
        // and set the ID so we find it next time
        userDiv.setAttribute('id', 'fb' + userId + 'messages');
        // we are creating the user here, set the ID of the list of messages this user will show
        messageList = userDiv.querySelector('.card-contents');
        var thisController = this;
        // setup the expand button here
        mdlHandleExpand(userDiv, function() { thisController.onClickExpandUser(userDiv, messageList); });
        // there is a text input and label with an ID, we need to set these for them to work
        var textInput = userDiv.querySelector('.mdl-textfield__input');
        userDiv.querySelector('.mdl-textfield__input').setAttribute('id', 'fb' + userId + "_message_input");
        userDiv.querySelector('.mdl-textfield__label').setAttribute('for', 'fb' + userId + "_message_input");
        // and listen for changes
        var messageChangeFunction = function() {
            if (textInput.value) {
                sendMessageButton.removeAttribute('disabled');
            } else {
                sendMessageButton.setAttribute('disabled', 'true');
            }
        };
        // and handle them clicking the button
        var sendMessageButton = userDiv.querySelector('.message-send-message-button');
        sendMessageButton.addEventListener('click', function(e) {
            e.preventDefault();
            // send the message
            thisController.sendMessage(userId, textInput);
            // this changes the message, deletes the contents
            messageChangeFunction();
        });
        textInput.addEventListener('keyup', messageChangeFunction);
        textInput.addEventListener('change', messageChangeFunction);

        // and add to the list of users
        this.usersList.appendChild(userDiv);
        // remember we added this
        userAdded = true;
    } else {
        // this exists, so must the message list then
        messageList = userDiv.querySelector('.card-contents');
    }
    // we have the user div area, show the contents of the user
    var userIcon = this.umpyrData.getUserIcon(userId);
    if (userIcon) {
        userDiv.querySelector('.pic').style.backgroundImage = 'url(' + userIcon + ')';
    }
    var userName = this.umpyrData.getUserName(userId);
    if (!userName) {
        userName = 'Unknown';
    }
    userDiv.querySelector('.name').textContent = userName;
    var deleteButton = userDiv.querySelector('.message-user-delete-button');
    //TODO - do we want to be able to delete all messages from a user in one go? If so then show this button...
    // now we have the user div on the cars, find the message on this...
    var div = this.parentDiv.querySelector('#fb' + message.key);
    // If an element for that message does not exist yet we create it.
    var messageAdded = false;
    if (!div) {
        var container = document.createElement('div');
        if (!sentFromUs) {
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
        // and the date so we can put it in the right place in the list...
        div.setAttribute('receivedDate', message.receivedDate);
        // find the correct position for this message, based on it's receivedDate which can be sorted alphabetically
        var isInserted = false;
        for (var i = 0; i < messageList.childNodes.length; i += 1) {
            var childReceivedDate = messageList.childNodes[i].getAttribute('receivedDate');
            if (message.receivedDate >= childReceivedDate) {
                // received date goes before here
                messageList.insertBefore(div, messageList.childNodes[i]);
                isInserted = true;
                break;
            }
        }
        if (!isInserted) {
            // goes nowhere, put it at the bottom
            messageList.appendChild(div);
        }
        // remember to show this
        messageAdded = true;
    }
    //get the image of the ID of the sender of this message
    userIcon = this.umpyrData.getUserIcon(message.fromId);
    if (userIcon) {
        div.querySelector('.pic').style.backgroundImage = 'url(' + userIcon + ')';
    }
    div.querySelector('.name').textContent = message.fromNickname;
    div.querySelector('.message-date').textContent = dateFormat(message.getReceivedDateFormatted(), "default");
    var messageElement = div.querySelector('.card-contents');
    // do the text content of the message
    messageElement.innerHTML = message.contents;
    // Replace all line breaks by <br>.
    messageElement.innerHTML = messageElement.innerHTML.replace(/\n/g, '<br>');
    if (userAdded) {
        // Show the card fading-in and scroll to view the new message.
        fadeElement(userDiv, true, function() {
            // update the google MDL stuff
            mdlCleanup(div);
        });
        // and scroll the messages
        this.usersList.scrollTop = this.usersList.scrollHeight;
        this.usersList.focus();
    }
    if (messageAdded) {
        // Show the card fading-in and scroll to view the new message.
        // show the contents fading in
        fadeElement(div, true, function() {
            // update the google MDL stuff
            mdlCleanup(div);
        });
        // and scroll the messages
        messageList.scrollTop = messageList.scrollHeight;
        messageList.focus();
    }

    // we added the message ok, also handle it's deletion
    var deleteButton = div.querySelector('.message-delete-button');
    var thisController = this;
    deleteButton.removeAttribute('hidden');
    deleteButton.addEventListener('click',
        function() {
            // clicked delete, delete it then
            if (thisController.isSiteMessages) {
                thisController.umpyrData.messages.deleteSiteMessage(message);
            } else {
                thisController.umpyrData.messages.deleteMessage(message);
            }
        },
        false
    );
};

MessagesCardController.prototype.sendMessage = function(targetId, textInput) {
    // Check if the user is signed-in before we do anything
    if (window.umpyr.checkSignedInWithMessage()) {
        // user is signed in, send the message
        this.umpyrData.messages.sendUserMessage(targetId, textInput.value);
        mdlChange(textInput, "");
    }
};

MessagesCardController.prototype.onClickExpandUser = function(userDiv, messagesList) {
    if (messagesList.getAttribute('hidden') !== null) {
        // is hidden, show it by expanding
        messagesList.removeAttribute('hidden');
        // show the contents fading in
        fadeElement(messagesList, true);
    } else {
        // hide it by removing the visible from the class list
        fadeElement(messagesList, false, function() {
            // and actually hide it
            messagesList.setAttribute('hidden', 'true');
        });
    }
};