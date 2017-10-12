// Create and Deploy Your Cloud Functions
// https://firebase.google.com/docs/functions/write-firebase-functions
// https://firebase.google.com/docs/database/extend-with-functions
// enter:
// firebase deploy --only functions
//

// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
const functions = require('firebase-functions');

// The Firebase Admin SDK to access the Firebase Realtime Database. 
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

// Max number of matches that other people can submit to us before we start deleting them
const MAX_MATCHES_SUB_COUNT = 20;
const MAX_MESSAGES_SUB_COUNT = 100;
const MAX_MESSAGE_SUBMITTED_SUB_COUNT = 10;

// Removes siblings of the node that element that triggered the function if there are more than MAX_MATCHES_SUB_COUNT.
exports.limitMatchesSubmitted = functions.database.ref('/matches_submitted/{userid}/{matchid}').onWrite(event => {
    // get all the matches submitted under the target user we just added one under
    var parentRef = admin.database().ref('/matches_submitted/' + event.params.userid);
    // the example provided just gets the parent ref of the event and queries that for the number of children.
    // THIS IS NO GOOD for us because the user posting the match_submitted has no rights to read the data
    // SO INSTEAD we can use the admin.database() call to get the correct permissions to delete when there are
    // too many matches posted in the user's inbox that they haven't accepted yet.
    parentRef.once('value').then(snapshot => {
        if (snapshot.numChildren() >= MAX_MATCHES_SUB_COUNT) {
            let childCount = 0;
            const updates = {};
            snapshot.forEach(function(child) {
                if (++childCount <= snapshot.numChildren() - MAX_MATCHES_SUB_COUNT) {
                    updates[child.key] = null;
                }
            });
            // Update the parent. This effectively removes the extra children.
            return parentRef.update(updates);
        }
    });
});

// Removes siblings of the node that element that triggered the function if there are more than MAX_MESSAGES_SUB_COUNT.
exports.limitMessagesStored = functions.database.ref('/messages/{userid}/{messageid}').onWrite(event => {
    // get all the messages submitted under the target user we just added one under
    var parentRef = admin.database().ref('/messages/' + event.params.userid);
    // the example provided just gets the parent ref of the event and queries that for the number of children.
    // THIS IS NO GOOD for us because the user posting the message might not have rights to read the data
    // SO INSTEAD we can use the admin.database() call to get the correct permissions
    parentRef.once('value').then(snapshot => {
        if (snapshot.numChildren() >= MAX_MESSAGES_SUB_COUNT) {
            let childCount = 0;
            const updates = {};
            snapshot.forEach(function(child) {
                if (++childCount <= snapshot.numChildren() - MAX_MESSAGES_SUB_COUNT) {
                    updates[child.key] = null;
                }
            });
            // Update the parent. This effectively removes the extra children.
            return parentRef.update(updates);
        }
    });
});

// Removes siblings of the node that element that triggered the function if there are more than MAX_MESSAGE_SUBMITTED_SUB_COUNT.
exports.limitMessagesSubmitted = functions.database.ref('/messages_submitted/{adminid}/{userid}/{messageid}').onWrite(event => {
    // get all the messages submitted under the target user we just added one under
    var parentRef = admin.database().ref('messages_submitted/' + event.params.adminid + '/' + event.params.userid);
    // the example provided just gets the parent ref of the event and queries that for the number of children.
    // THIS IS NO GOOD for us because the user posting the message might not have rights to read the data
    // SO INSTEAD we can use the admin.database() call to get the correct permissions
    parentRef.once('value').then(snapshot => {
        if (snapshot.numChildren() >= MAX_MESSAGE_SUBMITTED_SUB_COUNT) {
            let childCount = 0;
            const updates = {};
            snapshot.forEach(function(child) {
                if (++childCount <= snapshot.numChildren() - MAX_MESSAGE_SUBMITTED_SUB_COUNT) {
                    updates[child.key] = null;
                }
            });
            // Update the parent. This effectively removes the extra children.
            return parentRef.update(updates);
        }
    });
});

exports.countMatchesEntered = functions.database.ref("/matches/{userid}/{matchid}/sport").onWrite((event) => {
    // find the counter that we want to update each time they delete / add a match of some kind
    var sport;
    if (event.data.exists()) {
        // the new data is the stuff to get, get the sport they entered
        sport = event.data.val();
    } else {
        // the new data is not there, get the old instead
        sport = event.data.previous.val();
    }
    var countRef = admin.database().ref('/users/' + event.params.userid + '/matches_stats/' + sport + '/entered_count');
    // increment / decrement the entered count for the user, returns a Promise (sets data in the database)
    return countRef.transaction(function(current) {
        if (event.data.exists() && !event.data.previous.exists()) {
            return (current || 0) + 1;
        } else if (!event.data.exists() && event.data.previous.exists()) {
            return (current || 0) - 1;
        } else {
            return current;
        }
    }).then(() => null).catch(er => {
        console.error('...', er);
    });
});

exports.countMatchesPlayed = functions.database.ref("/matches/{userid}/{matchid}/isUserPlayed").onWrite((event) => {
    // count the number of times the user has played a match
    var sport;
    if (event.data.exists()) {
        // the new data is the stuff to get, get the sport they entered
        sport = event.data.val();
    } else {
        // the new data is not there, get the old instead
        sport = event.data.previous.val();
    }
    // get the reference to the counter for this data
    var countRef = admin.database().ref('/users/' + event.params.userid + '/matches_stats/' + sport + '/played_count');
    // increment / decrement the entered count for the user, returns a Promise (sets data in the database)
    return countRef.transaction(function(current) {
        if (event.data.exists() && !event.data.previous.exists()) {
            return (current || 0) + 1;
        } else if (!event.data.exists() && event.data.previous.exists()) {
            return (current || 0) - 1;
        } else {
            return current;
        }
    }).then(() => null).catch(er => {
        console.error('...', er);
    });
});

exports.countMatchesWon = functions.database.ref("/matches/{userid}/{matchid}/isUserWinner").onWrite((event) => {
    // count the number of times a user has won a match
    var sport;
    if (event.data.exists()) {
        // the new data is the stuff to get, get the sport they entered
        sport = event.data.val();
    } else {
        // the new data is not there, get the old instead
        sport = event.data.previous.val();
    }
    // get the reference to the counter for this data
    var countRef = admin.database().ref('/users/' + event.params.userid + '/matches_stats/' + sport + '/won_count');
    // increment / decrement the entered count for the user, returns a Promise (sets data in the database)
    return countRef.transaction(function(current) {
        if (event.data.exists() && !event.data.previous.exists()) {
            return (current || 0) + 1;
        } else if (!event.data.exists() && event.data.previous.exists()) {
            return (current || 0) - 1;
        } else {
            return current;
        }
    }).then(() => null).catch(er => {
        console.error('...', er);
    });
});

exports.countMatchesLost = functions.database.ref("/matches/{userid}/{matchid}/isUserLoser").onWrite((event) => {
    // count the number of times a user has won a match
    var sport;
    if (event.data.exists()) {
        // the new data is the stuff to get, get the sport they entered
        sport = event.data.val();
    } else {
        // the new data is not there, get the old instead
        sport = event.data.previous.val();
    }
    // get the reference to the counter for this data
    var countRef = admin.database().ref('/users/' + event.params.userid + '/matches_stats/' + sport + '/lose_count');
    // increment / decrement the entered count for the user, returns a Promise (sets data in the database)
    return countRef.transaction(function(current) {
        if (event.data.exists() && !event.data.previous.exists()) {
            return (current || 0) + 1;
        } else if (!event.data.exists() && event.data.previous.exists()) {
            return (current || 0) - 1;
        } else {
            return current;
        }
    }).then(() => null).catch(er => {
        console.error('...', er);
    });
});