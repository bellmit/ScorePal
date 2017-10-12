'use strict';

// Initializes the friends card and controls the data on the card
function FriendsFindCardController(parentDiv) {
    // initialise this class
    this.document = document;
    this.parentDiv = parentDiv;
    // and remember the very useful data controller object
    this.umpyrData = window.umpyr.umpyrData;

    this.helpButton = this.parentDiv.querySelector('#friends-help-button');
    this.helpList = this.parentDiv.querySelector('#friends-help-contents');

    this.friendForm = this.parentDiv.querySelector('#friend-form');
    this.friendInput = this.parentDiv.querySelector('#friend');

    this.friendResult = this.parentDiv.querySelector('#friend-action-result');
    this.findButton = this.parentDiv.querySelector('#find-friend-button');
    this.findSpinner = this.parentDiv.querySelector('#find-friend-spinner');
    this.makeButton = this.parentDiv.querySelector('#make-friend-button');

    // Saves friend on form submit.
    var thisController = this;
    this.findButton.addEventListener('click', function(e) {
        e.preventDefault();
        thisController.findFriend();
    });
    this.makeButton.addEventListener('click', function(e) {
        e.preventDefault();
        thisController.friendOtherUser();
    });

    // handle the more / less for the display of the help
    var thisController = this;
    // setup the expand button here
    mdlHandleExpand(this.helpButton, function() { thisController.onClickExpandHelp(); });

    // Toggle for the button.
    var buttonTogglingHandler = this.toggleButton.bind(this);
    this.friendInput.addEventListener('keyup', buttonTogglingHandler);
    this.friendInput.addEventListener('change', buttonTogglingHandler);

    // intialise the button
    this.toggleButton();

    // hide the button that will add a new friend
    this.document.querySelector('#find-new-friend-button').setAttribute('hidden', 'true');
}

FriendsFindCardController.prototype.close = function(isLocalClose) {
    // called as this card is removed and closed
    if (isLocalClose) {
        // the friends page is active as they used the 'x' to close us
        this.document.querySelector('#find-new-friend-button').removeAttribute('hidden');
    }
};

FriendsFindCardController.prototype.onClickExpandHelp = function() {
    var helpList = this.helpList;
    if (this.helpList.getAttribute('hidden') !== null) {
        // is hidden, show it by expanding
        helpList.removeAttribute('hidden');
        // show the contents fading in
        fadeElement(div, true);
    } else {
        // fade out
        fadeElement(div, false, function() {
            // when completed, actually hide it
            helpList.setAttribute('hidden', 'true');
        });
    }
};

// Sends a new friend request to the specified target
FriendsFindCardController.prototype.findFriend = function() {
    // find the friend if there is one
    var thisController = this;
    if (this.friendInput.value && this.isValidEmail(this.friendInput.value)) {
        // there is a value in the input field and it looks like an email, is it a user though?
        this.findSpinner.removeAttribute('hidden');
        window.umpyr.umpyrData.getUserIdByEmail(this.friendInput.value, function(validAddress, userId) {
            // this is a valid email that results in a user ID that is an active user
            if (userId) {
                thisController.makeButton.removeAttribute('disabled');
                thisController.enteredFriendEmail = validAddress;
                thisController.enteredFriendId = userId;
                thisController.friendResult.innerText = validAddress + " is a registered user, you can make them your friend...";
            } else {
                thisController.friendResult.innerText = validAddress + " is not a registered user, please do ask them to join...";
            }
            thisController.findButton.setAttribute('disabled', 'true');
            thisController.findSpinner.setAttribute('hidden', 'true');
        });
    } else {
        this.friendResult.innerText = "This is not a valid email, please enter the email of your friend to search...";
    }
};

// Sends a new friend request to the specified target
FriendsFindCardController.prototype.friendOtherUser = function() {
    // create the data object that will be saved
    // Check if the user is signed-in before we do anything
    if (window.umpyr.checkSignedInWithMessage()) {
        // user is signed in, save the data
        window.umpyr.umpyrData.friends.friendOtherUser(this.enteredFriendId);
        this.friendResult.innerText = this.enteredFriendEmail + " has been sent a friend request...";
    }
};

FriendsFindCardController.prototype.isValidEmail = function(email) {
    var re = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;
    return re.test(email);
}

// Enables or disables the submit button depending on the values of the input
// fields.
FriendsFindCardController.prototype.toggleButton = function() {
    // by default disable the friend button
    this.makeButton.setAttribute('disabled', 'true');
    if (this.friendInput.value && this.isValidEmail(this.friendInput.value)) {
        this.findButton.removeAttribute('disabled');
    } else {
        this.findButton.setAttribute('disabled', 'true');
    }
    this.friendResult.innerText = "";
};