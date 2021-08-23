'use strict';

// Initialise the data object that will do all our data operations
function UmpyrData(umpyrParent) {
    // remember the reference to umpyr so we can inform when our data changes
    this.umpyr = umpyrParent;

    if (!window.firebase || !(firebase.app instanceof Function) || !firebase.app().options) {
        window.alert('You have not configured and imported the Firebase SDK. ' +
            'Make sure you go through the codelab setup instructions and make ' +
            'sure you are running the codelab using `firebase serve`');
    }

    // initlise our firebase interface, this is our data
    this.auth = firebase.auth();
    this.database = firebase.database();
    this.storage = firebase.storage();
    // initiates firebase auth and listen to auth state changes
    this.auth.onAuthStateChanged(this.onAuthStateChanged.bind(this));
}

UmpyrData.prototype.addDataListener = function(source, dataCallback) {
    if (!this.dataCallbacks) {
        this.dataCallbacks = [];
    }
    // check not already in the list
    for (var i = 0; i < this.dataCallbacks.length; i += 1) {
        if (this.dataCallbacks[i][0] == source) {
            // return already added
            return false;
        }
    }
    // add to the list
    this.dataCallbacks.push([source, dataCallback]);
    return true;
};

UmpyrData.prototype.removeDataListener = function(source) {
    // remove the callback
    if (!this.dataCallbacks) {
        // return that it was not found
        return false;
    }
    // find in the list
    for (var i = 0; i < this.dataCallbacks.length; i += 1) {
        if (this.dataCallbacks[i][0] == source) {
            // found, remove
            this.dataCallbacks.splice(i, 1);
            // return that it was removed ok
            return true;
        }
    }
    // return that it was not found
    return false;
};

UmpyrData.prototype.informDataListenersOfChange = function(dataListChanged, data, reason) {
    // inform all the registered callbacks that a list of data has changed
    if (this.dataCallbacks) {
        // loop through all the callbacks
        for (var i = 0; i < this.dataCallbacks.length; i += 1) {
            // call the function registered, passing the list of data that has changed
            try {
                // call the function, using the source as 'this'
                this.dataCallbacks[i][1].call(this.dataCallbacks[i][0], dataListChanged, data, reason);
            } catch (error) {
                console.error(error);
            }
        }
    }
};

// Sets the URL of the given img element with the URL of the image stored in Cloud Storage.
UmpyrData.prototype.setImageUrl = function(imageUri, imgElement) {
    // If the image is a Cloud Storage URI we fetch the URL.
    if (imageUri.startsWith('gs://')) {
        imgElement.src = UmpyrData.LOADING_IMAGE_URL; // Display a loading image first.
        this.storage.refFromURL(imageUri).getMetadata().then(function(metadata) {
            imgElement.src = metadata.downloadURLs[0];
        });
    } else {
        imgElement.src = imageUri;
    }
};

// Signs-in to Firebase
UmpyrData.prototype.signIn = function() {
    // Sign in Firebase using popup auth and Google as the identity provider.
    var provider = new firebase.auth.GoogleAuthProvider();
    this.auth.signInWithPopup(provider);
};

UmpyrData.prototype.signInOther = function() {
    var googleAuthProvider = new firebase.auth.GoogleAuthProvider();
    googleAuthProvider.setCustomParameters({
        prompt: 'select_account'
    });
    this.auth.signInWithRedirect(googleAuthProvider);
};

// Signs-out of Umpyr
UmpyrData.prototype.signOut = function() {
    // Sign out of Firebase.
    this.auth.signOut();
};

UmpyrData.prototype.getUserId = function() {
    if (this.currentUser) {
        return this.currentUser.ID;
    } else {
        return undefined;
    }
};

UmpyrData.prototype.getUserMatchesStats = function() {
    if (this.currentUser) {
        return this.currentUser.getMatchesStats();
    } else {
        return {};
    }
};

UmpyrData.prototype.getUserSports = function() {
    // return the sports the user plays, in order of their frequency played
    var stats = this.getUserMatchesStats();
    // get the frequency entered for each sport in the list and if it is played at all, then add to the list
    // accordingly
    var sportsInOrder = [];
    for (var i = 0; i < UmpyrSports.length; i += 1) {
        // for each sport, see if they entered a score for it
        if (stats[UmpyrSports[i].title]) {
            // there is stats for this
            var entered = stats[UmpyrSports[i].title].entered_count || 0;
            // put this in the right place
            var isInserted = false;
            for (var j = 0; j < sportsInOrder; j += 1) {
                if (sportsInOrder[j][1] > entered) {
                    // goes before this one as has a higher value than it
                    sportsInOrder.splice(j, 0, [UmpyrSports[i], entered]);
                    isInserted = true;
                    break;
                }
            }
            if (!isInserted) {
                // add to the end
                sportsInOrder.push([UmpyrSports[i], entered]);
            }
        }
    }
    // now create the list to acutally return, the sports themselves
    var sportsToReturn = [];
    for (var i = 0; i < sportsInOrder.length; i += 1) {
        sportsToReturn.push(sportsInOrder[i][0]);
    }
    return sportsToReturn;
};

UmpyrData.prototype.getActivePages = function() {
    // the active pages are those from the user
    if (this.currentUser) {
        return this.currentUser.getActivePages();
    } else {
        // no active pages
        return '.';
    }
};

// Triggers when the auth state change for instance when the user signs-in or signs-out.
UmpyrData.prototype.onAuthStateChanged = function(user) {
    if (user) {
        // remember the user ID to key all our data on
        if (!this.currentUser || this.currentUser.ID !== user.uid) {
            // this is our first log in, or a change in the login
            // let's update our user data in the database
            // first let's create the user data we want to store
            this.currentUser = new User();
            this.currentUser.ID = user.uid;
            this.currentUser.email = user.email;
            this.currentUser.nickname = user.displayName;
            this.currentUser.photoUrl = user.photoURL;
            // and remember this for inside annonymous functions
            var thisData = this;
            // now we can do a transation, this let's us see if it already exists and update it
            // if it does with the data they won't want to change
            this.userRef = this.database.ref('/users/' + user.uid);
            this.userRef.off();
            this.userRef.transaction(function(currentUserData) {
                if (currentUserData === null) {
                    // there is no user data, create the new user by returning it here for
                    // firebase to set into the store
                    return thisData.currentUser;
                } else {
                    // use the user data from the database instead, there are specific entries here
                    thisData.currentUser = new User(currentUserData);
                    // but overwrite with the stuff we take from firebase
                    thisData.currentUser.ID = user.uid;
                    thisData.currentUser.email = user.email;
                    thisData.currentUser.nickname = user.displayName;
                    thisData.photoUrl = user.photoURL;
                    // and return the newly amalgamated user data which will write it to the database
                    return thisData.currentUser.getUserData();
                }
            }, function(error, committed) {
                if (error) {
                    console.log('failed to write the user data to the database:' + error);
                }
                // now we are logged in we can load all of our data, create all the data objects here
                thisData.createDataStores();
                // and inform the main page with the best user data we have, that from firebase
                window.umpyr.onUmpyrDataStateChanged(thisData.currentUser);
                // now we are done accessing the user data, listen for changes too
                thisData.listenForUserChanges();
            });
            // also push the email to key conversion to the store to be sure it is up to date
            this.database.ref('emails_to_ids/' + this.emailToKey(user.email)).set(user.uid);
            if (this.currentUser.ID === 'unOMG3gEibPh76lSVdQfdKrNr163') {
                // we are the admin - show our page
                document.querySelector('#admin-contents').removeAttribute('hidden');
            }
        }
        // We save the Firebase Messaging Device token and enable notifications to this user
        this.saveMessagingDeviceToken();
    } else {
        // User is signed out, clear the user data
        this.currentUser = null;
        // and inform the parent that we are logged out
        window.umpyr.onUmpyrDataStateChanged(this.currentUser);
    }
};

UmpyrData.prototype.listenForUserChanges = function() {
    // and remember this for inside annonymous functions
    var thisData = this;
    // and listen for changes to the user data that may occur
    var userDataChanged = function(data) {
        if (data.key === 'matches_stats') {
            // the stats on the user changed, update this
            thisData.currentUser.matches_stats = data.val();
            // inform any listeners of this change to the list
            thisData.informDataListenersOfChange([], thisData.currentUser, 'changed');
        }
        // else we can ignore all the other data - shouldn't change anyway so let's not bother.
    }.bind(this);
    // listen for changes to this single child
    this.userRef.on('child_changed', userDataChanged);
};

UmpyrData.prototype.createDataStores = function() {
    // create all the data stores we use and need
    this.friends = new UmpyrDataFriends(this, this.database);
    this.messages = new UmpyrDataMessages(this, this.database);
    this.posts = new UmpyrDataPosts(this, this.database);
    this.matches = new UmpyrDataMatches(this, this.database);
}

UmpyrData.prototype.isUserSignedIn = function() {
    return this.auth.currentUser && this.currentUser;
};

UmpyrData.prototype.getUserIcon = function(id) {
    if (this.isUserSignedIn() && (id === this.currentUser.ID || id === undefined)) {
        // the player ID is for the signed in user, use their icon
        return this.currentUser.photoUrl;
    } else if (this.friends) {
        // try to find the icon of the friend
        var friend = this.friends.getFriendFromId(id);
        if (friend) {
            return friend.photoUrl;
        } else {
            //TODO get the icon for the player ID from firebase
            return undefined;
        }
    } else {
        return undefined;
    }
};

UmpyrData.prototype.getUserName = function(id) {
    if (this.isUserSignedIn() && (id === this.currentUser.ID || id === undefined)) {
        // the player ID is for the signed in user, use their name
        return this.currentUser.nickname;
    } else if (this.friends) {
        // try to find the name of the friend
        var friend = this.friends.getFriendFromId(id);
        if (friend) {
            return friend.nickname;
        } else {
            //TODO get the name for the player ID from firebase
            return undefined;
        }
    } else {
        return undefined;
    }
};

// Saves the messaging device token to the datastore.
UmpyrData.prototype.saveMessagingDeviceToken = function() {
    /*var firebaseMessaging = firebase.messaging();
    firebaseMessaging.getToken().then(function(currentToken) {
        if (currentToken) {
            console.log('Got FCM device token:', currentToken);
            // Saving the Device Token to the datastore.
            firebase.database().ref('/fcmTokens').child(currentToken)
                .set(firebase.auth().currentUser.uid);
        } else {
            // Need to request permissions to show notifications.
            this.requestNotificationsPermissions();
        }
    }.bind(this)).catch(function(error) {
        console.error('Unable to get messaging token.', error);
    });*/
};

// Requests permissions to show notifications.
UmpyrData.prototype.requestNotificationsPermissions = function() {
    /*
    console.log('Requesting notifications permission...');
    firebase.messaging().requestPermission().then(function() {
        // Notification permission granted.
        this.saveMessagingDeviceToken();
    }.bind(this)).catch(function(error) {
        console.error('Unable to get permission to notify.', error);
    });*/
};

UmpyrData.prototype.isUserEmailSignedUp = function(emailAddress) {
    this.auth.fetchProvidersForEmail(emailAddress).then(providers => {
        if (providers.length === 0) {
            // this email hasn't signed up yet
            return false;
        } else {
            // has signed up
            return true;
        }
    });
};

/**
 * Looks up a user id by email address and invokes callback with the id or null if not found
 * @return {Object|null} the object contains the key/value hash for one user
 */
UmpyrData.prototype.getUserIdByEmail = function(emailAddress, callback) {
    this.database.ref('emails_to_ids/' + this.emailToKey(emailAddress)).once('value', function(snap) {
        callback(emailAddress, snap.val());
    });
};

/**
 * Firebase keys cannot have a period (.) in them, so this converts the emails to valid keys
 */
UmpyrData.prototype.emailToKey = function(emailAddress) {
    // hash the email address as a 64bit string
    return btoa(emailAddress);
};

UmpyrData.prototype.setUserPage = function(page, isOn) {
    // set the data on the user to include this new page status
    if (this.currentUser) {
        // there is a user, set the page status on this
        if (isOn) {
            this.currentUser.setPageOn(page);
        } else {
            this.currentUser.setPageOff(page);
        }
        var pages = this.currentUser.getActivePages();
        // and write to the database the new value
        this.database.ref('users/' + this.currentUser.ID + "/pages").set(pages);
    }
};