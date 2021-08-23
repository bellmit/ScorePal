/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

const K_USECHIPS = false;
const K_SHOW_ENTERSCORE = false;

// Initializes Umpyr.
function Umpyr() {
    // Shortcuts to DOM Elements.
    this.mainTitle = document.getElementById('main-title');
    this.userPic = document.getElementById('user-pic');
    this.userName = document.getElementById('user-name');
    this.signInButtons = document.getElementsByClassName('sign-in-button');
    this.signOutButton = document.getElementById('sign-out');
    this.signInOtherButton = document.getElementById('sign-in-other');
    this.signInSnackbar = document.getElementById('must-signin-snackbar');
    this.chipsHeader = document.getElementById('main-header-chips');

    // initialise our data object
    this.umpyrData = new UmpyrData(this);

    // handle signing in and out here
    this.signOutButton.addEventListener('click', this.umpyrData.signOut.bind(this.umpyrData));
    // find any sign-in buttons scattered around to handle their clickings
    for (var i = 0; i < this.signInButtons.length; i++) {
        this.signInButtons[i].addEventListener('click', this.umpyrData.signIn.bind(this.umpyrData));
    }
    // and the sign in as other account button
    this.signInOtherButton.addEventListener('click', this.umpyrData.signInOther.bind(this.umpyrData));

    if (K_SHOW_ENTERSCORE) {
        // event for clicking the 'enter score' button
        document.getElementById('enter-score-button').addEventListener('click', function(e) {
            e.preventDefault();
            window.umpyr.showCard(0);
        });
        document.getElementById('enter-score-button').removeAttribute('hidden');
    } else {
        document.getElementById('enter-score-button').setAttribute('hidden', 'true');
    }
}

// Triggers when the auth state change for instance when the user signs-in or signs-out.
Umpyr.prototype.onUmpyrDataStateChanged = function(user) {
    if (user) { // User is signed in!
        // Get profile pic and user's name from the Firebase user object.
        var profilePicUrl = user.photoUrl; // Only change these two lines!
        var userName = user.nickname; // Only change these two lines!

        // Set the user's profile pic and name.
        this.userPic.src = profilePicUrl;
        this.userName.textContent = userName;

        // Show user's profile and sign-out button.
        this.userName.removeAttribute('hidden');
        this.userPic.removeAttribute('hidden');
        this.signOutButton.removeAttribute('hidden');

        // Hide all the sign-in buttons.
        for (var i = 0; i < this.signInButtons.length; i++) {
            this.signInButtons[i].setAttribute('hidden', 'true');
        }
    } else { // User is signed out!
        // Hide user's profile and sign-out button.
        this.userName.setAttribute('hidden', 'true');
        this.userPic.setAttribute('hidden', 'true');
        this.signOutButton.setAttribute('hidden', 'true');

        // Show all the sign-in buttons
        for (var i = 0; i < this.signInButtons.length; i++) {
            this.signInButtons[i].removeAttribute('hidden');
        }
    }
    // and show the correct cards for the current active page
    this.showActiveCards();
};

function onToggleCard(cardNumber) {
    // called from the page on the main index, redirect to Umpyr
    window.umpyr.onToggleCard(cardNumber);
    return false;
}

function onNavigationClick(pageNumber) {
    // called from the page on the main index, redirect to Umpyr
    window.umpyr.onNavigationClick(pageNumber, true);
}

Umpyr.prototype.showActiveCards = function() {
    if (!this.umpyrData.isUserSignedIn()) {
        // hide all cards, but don't write to the database
        this.hideAllCards();
        // not signed in - show the welcome card
        this.showCard(9);
        // and hide the chips
        this.chipsHeader.setAttribute('hidden', 'true');
    } else {
        // get the page object for the set active page
        var page;
        for (var i = 0; i < UmpyrPages.length; i += 1) {
            if (UmpyrPages[i].id === this.activePage) {
                page = UmpyrPages[i];
                break;
            }
        }
        // hide everything
        this.hideAllCards();
        if (!page) {
            console.error('unable to show the unrecognised page: ' + this.activePage);
        } else {
            // have the page, show the cards for this page
            if (K_USECHIPS && page.id === 0) {
                // this is the home page, we are showing a special, user-defined list of cards here
                var activePages = this.umpyrData.getActivePages().split('.');
                // set the active page to be not home (so we don't save these changes as we show them)
                this.activePage = -1;
                // and show what they want to be shown
                for (var i = 0; i < activePages.length; i += 1) {
                    // show every active page
                    if (activePages[i] && activePages[i].length > 0) {
                        this.onToggleCard(activePages[i], true);
                    }
                }
                // now we are showing the cards, remember the active page is home
                this.activePage = 0;
                // and show the header
                this.chipsHeader.removeAttribute('hidden');
            } else {
                // not the home page or not showing chips, hide the chips
                this.chipsHeader.setAttribute('hidden', true);
            }
            // show the list of active cards hard-coded
            for (var i = 0; i < page.activeCards.length; i += 1) {
                this.onToggleCard(page.activeCards[i], true);
            }
            // set the title of the page we are now showing
            this.mainTitle.innerText = page.title;
        }
    }
};

Umpyr.prototype.hideAllCards = function() {
    for (var i = 0; i < UmpyrCards.length; i += 1) {
        // for each card, make sure it is hidden
        this.hideCard(UmpyrCards[i].id, UmpyrCards[i]);
    }
};

function onNavigationBack() {
    // pass to umpyr
    window.umpyr.onNavigationBack();
}

Umpyr.prototype.onNavigationBack = function() {
    // go back a page in the history
    if (this.activePageHistory && this.activePageHistory.length > 1) {
        // pop the current page from history, ignore this as this is where we are...
        this.activePageHistory.pop();
        // pop the page before that and go there
        onNavigationClick(this.activePageHistory.pop(), false);
    }
};

Umpyr.prototype.onNavigationClick = function(pageNumber, isAddToHistory) {
    if (pageNumber === this.activePage) {
        // nothing to do here
        return;
    }
    // change the page now
    this.activePage = pageNumber;
    // handle the page navigation to turn on / off the cards for each page
    if (isAddToHistory) {
        if (!this.activePageHistory) {
            this.activePageHistory = [];
        }
        // add the active page to our history
        this.activePageHistory.push(this.activePage);
        // limit the size of the stack
        while (this.activePageHistory.length > 20) {
            // too many pages, forget the history from here
            this.activePageHistory.splice(0, 1);
        }
    }
    // and show the cards for this page
    this.showActiveCards();
    // toggle the selection state of this new page
    for (var i = 0; i < UmpyrPages.length; i += 1) {
        var pageElement = document.getElementById(UmpyrPages[i].navId);
        if (pageElement) {
            if (UmpyrPages[i].id === this.activePage) {
                // this page is showing, set it to be selected
                pageElement.setAttribute('selected', 'true');
            } else {
                pageElement.removeAttribute('selected');
            }
        } else {
            //console.debug('cannot highlight element for page id ' + UmpyrPages[i].id + ' as cannot find navID of ' + UmpyrPages[i].navId);
        }
    }
};

Umpyr.prototype.onToggleCard = function(cardId, forceOn) {
    // toggle the display of the selected card
    if (!this.checkSignedInWithMessage()) {
        // do no toggling if they are not signed in...
        return;
    }
    // get the card
    var card = this.getCard(cardId);
    if (!card) {
        console.error('unable to toggle the card with unrecognised ID: ' + cardId);
    } else {
        if (!card.controller || forceOn) {
            // there is no controller, or we want to be sure this is on, show this card
            this.showCard(cardId, card);
        } else {
            // we want to hide this card
            this.hideCard(cardId, card);
        }
    }
    if (this.activePage === 0) {
        // this is the home page so set wether this page is shown or not in the user data
        this.umpyrData.setUserPage(cardId, card.controller);
    }
};

Umpyr.prototype.displayMatchDetails = function(match, deleteFunction) {
    // are to display this match details, make sure the card is displayed
    var card = this.getCard(10);
    if (card) {
        if (card.controller) {
            // hide it to make any old ones go away
            this.hideCard(card.id, card, true);
        }
        // now show the card
        this.showCard(card.id, card);
    }
    // and display the match on the card
    card.controller.displayMatch(match, deleteFunction);
};

// Returns true if user is signed-in. Otherwise false and displays a match.
Umpyr.prototype.checkSignedInWithMessage = function() {
    // Return true if the user is signed in Firebase
    if (this.umpyrData.isUserSignedIn()) {
        return true;
    } else {
        // not signed in, display a match to the user using a Toast.
        var data = {
            message: 'You must sign-in first',
            timeout: 2000
        };
        this.signInSnackbar.MaterialSnackbar.showSnackbar(data);
        return false;
    }
};

Umpyr.prototype.getCard = function(cardId) {
    // get the card data for the specified ID
    var card = null;
    for (var i = 0; i < UmpyrCards.length; i += 1) {
        if (UmpyrCards[i].id === Number(cardId)) {
            // this is the correct card
            card = UmpyrCards[i];
            break;
        }
    }
    return card;
};

Umpyr.prototype.showCard = function(cardId, card) {
    if (!card) {
        // get the card data for the specified ID
        card = this.getCard(cardId);
    }
    if (!card) {
        console.error('unable to show because I cannot find a card for the ID: ' + cardId);
    } else {
        // have the card, show the card then...
        clientSideInclude(card.htmlSource, card.placeholderId);
        // get the added card
        var addedCard = document.getElementById(card.placeholderId);
        if (!addedCard) {
            console.error('unable to show because I cannot the placeholder to add the card at: ' + card.placeholderIdardId);
        } else {
            // have mdl update the elements we just added
            mdlCleanup(addedCard);
        }
        // and create the card controller to do all the work here
        if (card.controller) {
            // there is already a controller, delete it
            card.controller.close();
            delete card.controller;
        }
        if (card.chipId) {
            // show the card is shown by enabling the chip
            var chip = document.getElementById(card.chipId).parentElement;
            chip.setAttribute('selected', 'true');
        }
        if (addedCard) {
            // scroll to the top to show this newly shown card of data
            scrollTo(addedCard, 0, 100);
            // remove any hidden atrribute from the card to show it
            addedCard.removeAttribute('hidden');
            // and fade it in
            fadeElement(addedCard, true);
        }
        // create the new controller to populate the created card
        card.controller = card.createController(addedCard);
        if (card.closeButtonId) {
            // handle the built-in close button also, as there is one
            document.getElementById(card.closeButtonId).addEventListener(
                'click',
                function() {
                    window.umpyr.hideCard(cardId, card, true);
                },
                false
            );
        }
    }
    // return the card this was performed upon
    return card;
};

Umpyr.prototype.hideCard = function(cardId, card, isLocalClose) {
    // get the card data for the specified ID
    if (!card) {
        // get the card data for the specified ID
        card = this.getCard(cardId);
    }
    if (!card) {
        console.error('unable to hide because I cannot find a card for the ID: ' + cardId);
    } else {
        // have the card, get the card that is there then
        var cardElement = document.getElementById(card.placeholderId);
        if (cardElement) {
            // hide this card by removing the content of it
            cardElement.innerHTML = "";
            // and hide the empty element
            cardElement.setAttribute('hidden', 'true');
        }
        // delete the controller if there is one
        if (card.controller) {
            card.controller.close(isLocalClose);
            delete card.controller;
            card.controller = null;
        }
        if (card.chipId) {
            // show the card is hidden by disabling the chip
            var chip = document.getElementById(card.chipId).parentElement;
            chip.removeAttribute('selected');
        }
    }
};

Umpyr.prototype.showLatestEnteredMatch = function() {
    // this is called as a user enters a new match, show that match on the page please now...
    if (this.activePage === 0) {
        // this is the home page, make sure the matches card is shown, this will show the match on top. Done
        this.showCard(7);
    } else if (this.activePage !== 1 && this.activePage !== 4) {
        // this is not the matches or history pages, set the page to 'matches' to show the match they just entered
        this.onNavigationClick(1, true);
    }
    // scroll to the top of the matches card that will be showing the match
    var card = this.getCard(7);
    var cardElement = document.getElementById(card.placeholderId);
    if (cardElement) {
        scrollTo(cardElement, 0, 100);
    }
};

window.onload = function() {
    /* The Google charts loader blows up if gadgets is defined, as it is
     * when the Firebase SDK is loaded.  Unfortunately, neither component
     * creates gadgets.config, which the rpc.v.js file loaded by the charts
     * loader relies on.
     *
     * This nasty kludge prevents error messages from appearing in the
     * console.
     */
    window.gadgets = window.gadgets || {
        config: {
            register: function() {}
        }
    };

    // when the window is loaded create the Umpyr object that will do all the work of the app!
    window.umpyr = new Umpyr();
    // show the home page as the first thing we do
    window.umpyr.onNavigationClick(0, true);
};