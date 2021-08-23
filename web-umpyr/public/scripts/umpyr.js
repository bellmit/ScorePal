'use strict';

function umpyrMakeTextFieldFriendsSelector(inputEdit, changeFunction) {
    // load the content for the friends container
    if (!Umpyr.friendsContainer) {
        // load the raw content from the html file that we want to use
        Umpyr.friendsContainer = clientSideInclude('/components/friendselectioncontainer.html');
    }
    inputEdit.oninput = function() {
        // as the contents change, edit the displayed friends in the list
        // let's get all the list items below the input edit that are friends
        var listItems = inputEdit.parentElement.querySelectorAll('.friends-selection-list-item');
        var filter = inputEdit.value.toUpperCase();
        for (var i = 0; i < listItems.length; i += 1) {
            // get the nickname and email of each item to see if it matches our filter
            if ((listItems[i].querySelector('.friends-selection-list-name').textContent.toUpperCase().indexOf(filter) > -1) ||
                (listItems[i].querySelector('.friends-selection-list-email').textContent.toUpperCase().indexOf(filter) > -1)) {
                // the user has started to type something in the name or the email
                listItems[i].style.display = "";
            } else {
                listItems[i].style.display = "none";
            }
        }
        // any change to the contents invalidates the ID in the data
        inputEdit.removeAttribute('data-user-id');
    };
    // listen for the focus message to show a list of friends
    inputEdit.onfocus = function() {
        // add the div that will contain the friends the user can select from
        var container = document.createElement('div');
        container.innerHTML = Umpyr.friendsContainer;
        var div = container.firstChild;
        // have the div that is the container
        var friendsList = div.querySelector('.friends-selection-list');
        // firstly we want to remove the <li> that is there to use as a template
        var listItem = div.querySelector('.friends-selection-list-item');

        // this is nasty but I want to detect if the focus is off the frends selector, or the text editor
        // so when it goes away I can hide the list of friends. This is hard with focus and blur because
        // we would have to handle the blur from so many children. So let's just start a timer and keep
        // checking - ugly but effective
        var blurSelectionId = setInterval(function() {
            var parentElement = inputEdit.parentElement;
            //TODO this is getting the input as the BODY of the document so deselects when click the list
            var container = parentElement.querySelector('.friends-selection-container');
            if (container) {
                // have the element, but are they looking at it?
                var x = document.activeElement;
                var isFocused = false;
                while (x = x.parentElement) {
                    // while we look up at parents, see if we are selected
                    if (x === parentElement) {
                        isFocused = true;
                        break;
                    }
                }
                if (!isFocused) {
                    // they are not looking at the list of friends, remove this then
                    container.parentElement.removeChild(container);
                    // and stop checking
                    clearInterval(blurSelectionId);
                }
            }
        }, 1000);
        // create the function to call when an item is clicked on
        var clickFunction = function() {
            // get the name they clicked on
            var clickedName = this.querySelector('.friends-selection-list-name').textContent;
            // and set it in the edit box
            mdlChange(inputEdit, clickedName);
            // get the ID they clicked on
            var clickedId = this.getAttribute('data-user-id');
            // and put the data in the input edit for us to get later
            inputEdit.setAttribute('data-user-id', clickedId);
            // and call call the function from the creator if it exists
            if (changeFunction) {
                changeFunction();
            }
        };

        // now populate the list, firstly with the current user
        var userId = umpyr.umpyrData.getUserId();
        var userName = umpyr.umpyrData.getUserName(userId);
        var selfItem = listItem.cloneNode(true);
        listItem.parentElement.removeChild(listItem);
        selfItem.querySelector('.friends-selection-list-icon').style.backgroundImage = 'url(' + umpyr.umpyrData.getUserIcon(userId) + ')';
        selfItem.querySelector('.friends-selection-list-icon').className += 'player-avatar pic';
        selfItem.querySelector('.friends-selection-list-name').textContent = userName;
        selfItem.querySelector('.friends-selection-list-email').textContent = "";
        selfItem.setAttribute('data-user-id', userId);
        // listen to the click of this item
        selfItem.addEventListener('click', clickFunction, false);
        // and add to the list
        friendsList.appendChild(selfItem);
        // and now any friends
        var friends = umpyr.umpyrData.friends.getFriends();
        for (var i = 0; i < friends.length; i += 1) {
            var friendItem = listItem.cloneNode(true);
            // set the new data
            friendItem.querySelector('.friends-selection-list-icon').style.backgroundImage = 'url(' + friends[i].photoUrl + ')';
            friendItem.querySelector('.friends-selection-list-icon').className += 'player-avatar pic';
            friendItem.querySelector('.friends-selection-list-name').textContent = friends[i].nickname;
            friendItem.querySelector('.friends-selection-list-email').textContent = friends[i].email;
            friendItem.setAttribute('data-user-id', friends[i].ID);
            // listen to the click of this item
            friendItem.addEventListener('click', clickFunction, false);
            // and add to the list
            friendsList.appendChild(friendItem);
        }
        // now any players we played recently that are not friends
        var recentOpponents = umpyr.umpyrData.matches.getRecentOpponents();
        for (var i = 0; i < recentOpponents.length; i += 1) {
            var opponentItem = listItem.cloneNode(true);
            // set the new data
            opponentItem.querySelector('.friends-selection-list-icon').style.backgroundImage = '';
            opponentItem.querySelector('.friends-selection-list-icon').setAttribute('src', 'images/ic_person_black_24px.svg');
            opponentItem.querySelector('.friends-selection-list-name').textContent = recentOpponents[i];
            opponentItem.querySelector('.friends-selection-list-email').textContent = "not a known friend.";
            opponentItem.setAttribute('data-user-id', '');
            // listen to the click of this item
            opponentItem.addEventListener('click', clickFunction, false);
            // and add to the list
            friendsList.appendChild(opponentItem);
        }

        // and put below the input editor
        inputEdit.parentElement.appendChild(div);
    };
}