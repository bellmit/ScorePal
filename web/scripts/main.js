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

// Initializes Umpyr.
function Umpyr() {
    this.checkSetup();

    // Shortcuts to DOM Elements.
    this.matchList = document.getElementById('matches');
    this.matchForm = document.getElementById('match-form');
    this.matchInput = document.getElementById('match');
    this.submitButton = document.getElementById('submit');
    this.submitImageButton = document.getElementById('submitImage');
    this.imageForm = document.getElementById('image-form');
    this.mediaCapture = document.getElementById('mediaCapture');
    this.userPic = document.getElementById('user-pic');
    this.userName = document.getElementById('user-name');
    this.welcomeCard = document.getElementById('welcome-card');
    this.signInButtons = document.getElementsByClassName('sign-in-button');
    this.signOutButton = document.getElementById('sign-out');
    this.signInSnackbar = document.getElementById('must-signin-snackbar');

    // Saves match on form submit.
    this.matchForm.addEventListener('submit', this.saveMatch.bind(this));
    this.signOutButton.addEventListener('click', this.signOut.bind(this));
    var signInButton;
    var i;
    for (i = 0; i < this.signInButtons.length; i++) {
        this.signInButtons[i].addEventListener('click', this.signIn.bind(this));
    }


    // Toggle for the button.
    var buttonTogglingHandler = this.toggleButton.bind(this);
    this.matchInput.addEventListener('keyup', buttonTogglingHandler);
    this.matchInput.addEventListener('change', buttonTogglingHandler);

    // Events for image upload.
    this.submitImageButton.addEventListener('click', function(e) {
        e.preventDefault();
        this.mediaCapture.click();
    }.bind(this));
    this.mediaCapture.addEventListener('change', this.saveImagePost.bind(this));

    this.initFirebase();
}

// Sets up shortcuts to Firebase features and initiate firebase auth.
Umpyr.prototype.initFirebase = function() {
    // shortcuts to the Firebase SDK features
    this.auth = firebase.auth();
    this.database = firebase.database();
    this.storage = firebase.storage();
    // initiates firebase auth and listen to auth state changes
    this.auth.onAuthStateChanged(this.onAuthStateChanged.bind(this));
};

// Loads matches history and listens for upcoming ones.
Umpyr.prototype.loadMatches = function() {
    // Load and listens for new matches.
    this.matchesRef = this.database.ref('matches/' + this.userId);
    this.postsRef = this.database.ref('posts/');
    // make sure we remove all previous listeners
    this.matchesRef.off();
    this.postsRef.off();

    // load the last 30 matches and listen for new ones
    var setMatch = function(data) {
        var val = data.val();
        this.displayMatch(val.matchPlayedDate, val.playerOneTitle + " vs " + val.playerTwoTitle, val.scoreSummary);
    }.bind(this);
    this.matchesRef.limitToLast(30).on('child_added', setMatch);
    this.matchesRef.limitToLast(30).on('child_changed', setMatch);

    // load the last 12 posts and listen for new ones
    var setPost = function(data) {
        var val = data.val();
        this.displayPost(data.key, val.name, val.photoUrl, val.imageUrl);
    }.bind(this);
    this.postsRef.limitToLast(12).on('child_added', setPost);
    this.postsRef.limitToLast(12).on('child_changed', setPost);
};

// Saves a new match on the Firebase DB.
Umpyr.prototype.saveMatch = function(e) {
    e.preventDefault();
    // Check that the user entered a match and is signed in.
    if (this.matchInput.value && this.checkSignedInWithMessage()) {
        var currentUser = this.auth.currentUser;
        // Add a new match entry to the Firebase Database.
        var matchId = "20170707141630";
        this.database.ref('matches/' + this.userId + "/" + matchId).set({
            gameMode: 1,
            matchPlayedDate: matchId,
            playerOneId: "",
            playerOneTitle: "Testing 1",
            playerTwoId: "",
            playerTwoTitle: "Testing 2",
            scoreData: "",
            scoreSummary: "Sets-2-1"
        }).then(function() {
            // Clear match text field and SEND button state.
            Umpyr.resetMaterialTextfield(this.matchInput);
            this.toggleButton();
        }.bind(this)).catch(function(error) {
            console.error('Error writing new match to Firebase Database', error);
        });
    }
};

// Sets the URL of the given img element with the URL of the image stored in Cloud Storage.
Umpyr.prototype.setImageUrl = function(imageUri, imgElement) {
    // If the image is a Cloud Storage URI we fetch the URL.
    if (imageUri.startsWith('gs://')) {
        imgElement.src = Umpyr.LOADING_IMAGE_URL; // Display a loading image first.
        this.storage.refFromURL(imageUri).getMetadata().then(function(metadata) {
            imgElement.src = metadata.downloadURLs[0];
        });
    } else {
        imgElement.src = imageUri;
    }
};

// Saves a new post containing an image URI in Firebase.
// This first saves the image in Firebase storage.
Umpyr.prototype.saveImagePost = function(event) {
    event.preventDefault();
    var file = event.target.files[0];

    // Clear the selection in the file picker input.
    this.imageForm.reset();

    // Check if the file is an image.
    if (!file.type.match('image.*')) {
        var data = {
            message: 'You can only share images',
            timeout: 2000
        };
        this.signInSnackbar.MaterialSnackbar.showSnackbar(data);
        return;
    }
    // Check if the user is signed-in
    if (this.checkSignedInWithMessage()) {
        // We add a match with a loading icon that will get updated with the shared image.
        var currentUser = this.auth.currentUser;
        this.postsRef.push({
            name: currentUser.displayName,
            imageUrl: Umpyr.LOADING_IMAGE_URL,
            photoUrl: currentUser.photoURL || '/images/profile_placeholder.png'
        }).then(function(data) {
            // Upload the image to Cloud Storage.
            var filePath = currentUser.uid + '/' + data.key + '/' + file.name;
            return this.storage.ref(filePath).put(file).then(function(snapshot) {

                // Get the file's Storage URI and update the posts placeholder.
                var fullPath = snapshot.metadata.fullPath;
                return data.update({ imageUrl: this.storage.ref(fullPath).toString() });
            }.bind(this));
        }.bind(this)).catch(function(error) {
            console.error('There was an error uploading a file to Cloud Storage:', error);
        });
    }
};

// Signs-in to Umpyr
Umpyr.prototype.signIn = function() {
    // Sign in Firebase using popup auth and Google as the identity provider.
    var provider = new firebase.auth.GoogleAuthProvider();
    this.auth.signInWithPopup(provider);
};

// Signs-out of Umpyr
Umpyr.prototype.signOut = function() {
    // Sign out of Firebase.
    this.auth.signOut();
};

// Triggers when the auth state change for instance when the user signs-in or signs-out.
Umpyr.prototype.onAuthStateChanged = function(user) {
    if (user) { // User is signed in!
        // Get profile pic and user's name from the Firebase user object.
        var profilePicUrl = user.photoURL; // Only change these two lines!
        var userName = user.displayName; // Only change these two lines!

        // Set the user's profile pic and name.
        this.userPic.src = profilePicUrl;
        this.userName.textContent = userName;
        this.userId = user.uid;

        // Show user's profile and sign-out button.
        this.userName.removeAttribute('hidden');
        this.userPic.removeAttribute('hidden');
        this.signOutButton.removeAttribute('hidden');

        // Hide sign-in button.
        var signInButton;
        var i;
        for (i = 0; i < this.signInButtons.length; i++) {
            this.signInButtons[i].setAttribute('hidden', 'true');
        }
        // delete the welcome card
        this.welcomeCard.innerHTML = "";
        this.welcomeCard.setAttribute('hidden', 'true');

        // We load currently existing matches.
        this.loadMatches();

        // We save the Firebase Messaging Device token and enable notifications.
        this.saveMessagingDeviceToken();
    } else { // User is signed out!
        // Hide user's profile and sign-out button.
        this.userName.setAttribute('hidden', 'true');
        this.userPic.setAttribute('hidden', 'true');
        this.signOutButton.setAttribute('hidden', 'true');

        // Show sign-in button.
        var signInButton;
        var i;
        for (i = 0; i < this.signInButtons.length; i++) {
            this.signInButtons[i].removeAttribute('hidden');
        }
        // and the welcome card by loading the welcome card contents
        clientSideInclude('welcome-card', '/components/welcomecard.html');
    }
};

// Returns true if user is signed-in. Otherwise false and displays a match.
Umpyr.prototype.checkSignedInWithMessage = function() {
    // Return true if the user is signed in Firebase
    if (this.auth.currentUser) {
        return true;
    }

    // Display a match to the user using a Toast.
    var data = {
        message: 'You must sign-in first',
        timeout: 2000
    };
    this.signInSnackbar.MaterialSnackbar.showSnackbar(data);
    return false;
};

// Saves the messaging device token to the datastore.
Umpyr.prototype.saveMessagingDeviceToken = function() {
    var firebaseMessaging = firebase.messaging();
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
    });
};

// Requests permissions to show notifications.
Umpyr.prototype.requestNotificationsPermissions = function() {
    console.log('Requesting notifications permission...');
    firebase.messaging().requestPermission().then(function() {
        // Notification permission granted.
        this.saveMessagingDeviceToken();
    }.bind(this)).catch(function(error) {
        console.error('Unable to get permission to notify.', error);
    });
};

// Resets the given MaterialTextField.
Umpyr.resetMaterialTextfield = function(element) {
    element.value = '';
    element.parentNode.MaterialTextfield.boundUpdateClassesHandler();
};

// Template for matches.
Umpyr.MATCH_TEMPLATE =
    '<div class="match-container">' +
    '<div class="spacing"><div class="pic"></div></div>' +
    '<div class="match"></div>' +
    '<div class="name"></div>' +
    '</div>';

// A loading image URL.
Umpyr.LOADING_IMAGE_URL = 'https://www.google.com/images/spin-32.gif';

// Displays a match in the UI.
Umpyr.prototype.displayMatch = function(key, name, text) {
    var div = document.getElementById(key);
    // If an element for that match does not exists yet we create it.
    if (!div) {
        var container = document.createElement('div');
        container.innerHTML = Umpyr.MATCH_TEMPLATE;
        div = container.firstChild;
        div.setAttribute('id', key);
        this.matchList.appendChild(div);
    }
    div.querySelector('.name').textContent = name;
    var matchElement = div.querySelector('.match');
    if (text) { // If the match is text.
        matchElement.textContent = text;
        // Replace all line breaks by <br>.
        matchElement.innerHTML = matchElement.innerHTML.replace(/\n/g, '<br>');
    }
    // Show the card fading-in.
    setTimeout(function() { div.classList.add('visible') }, 1);
    this.matchList.scrollTop = this.matchList.scrollHeight;
    this.matchInput.focus();
};

// Displays a post in the UI.
Umpyr.prototype.displayPost = function(key, name, picUrl, imageUri) {
    var div = document.getElementById(key);
    // If an element for that post does not exists yet we create it.
    if (!div) {
        var container = document.createElement('div');
        container.innerHTML = Umpyr.MATCH_TEMPLATE;
        div = container.firstChild;
        div.setAttribute('id', key);
        this.matchList.appendChild(div);
    }
    if (picUrl) {
        div.querySelector('.pic').style.backgroundImage = 'url(' + picUrl + ')';
    }
    div.querySelector('.name').textContent = name;
    var messageElement = div.querySelector('.match');
    if (imageUri) { //  the message is an image.
        var image = document.createElement('img');
        image.addEventListener('load', function() {
            this.matchList.scrollTop = this.matchList.scrollHeight;
        }.bind(this));
        this.setImageUrl(imageUri, image);
        messageElement.innerHTML = '';
        messageElement.appendChild(image);
    }
    // Show the card fading-in and scroll to view the new message.
    setTimeout(function() { div.classList.add('visible') }, 1);
    this.matchList.scrollTop = this.matchList.scrollHeight;
    this.matchList.focus();
};

// Enables or disables the submit button depending on the values of the input
// fields.
Umpyr.prototype.toggleButton = function() {
    if (this.matchInput.value) {
        this.submitButton.removeAttribute('disabled');
    } else {
        this.submitButton.setAttribute('disabled', 'true');
    }
};

// Checks that the Firebase SDK has been correctly setup and configured.
Umpyr.prototype.checkSetup = function() {
    if (!window.firebase || !(firebase.app instanceof Function) || !firebase.app().options) {
        window.alert('You have not configured and imported the Firebase SDK. ' +
            'Make sure you go through the codelab setup instructions and make ' +
            'sure you are running the codelab using `firebase serve`');
    }
};

window.onload = function() {
    window.umpyr = new Umpyr();
};