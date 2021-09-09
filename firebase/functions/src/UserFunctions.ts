import * as admin from 'firebase-admin';
import * as functions from 'firebase-functions';

/**
 * Listen for changes to users data to be sure to keep it up-to-date
 */
 exports.processUserDataChange = functions.firestore
 .document('users/{userId}')
 .onWrite((change, context) => {
     // when we have user data, be sure to also have the email in lower-case so we can search for it
     const data = change.after.data();
     const beforeData = change.before ? change.before.data() : null;
     if (data && (!beforeData || beforeData.email !== data.email)) {
        const email = data['email'];
        return change.after.ref.update({'email_lc': email == null ? '' : email.toLowerCase()});
     } else {
        // no change to make is fine
            return Promise.resolve();
     }
 });

/**
 * intercepts the call to delete a user to delete their user data along with it
 */
exports.deleteUserData = functions.auth.user().onDelete((user) => {
    // the user is deleted, delete the document
    if (!user) {
        console.error('User deleted without a user document passed');
        return Promise.reject(Error('User deleted without a user document passed'));
    } else {
        // delete the user's document data
        return admin.firestore()
            .collection('users')
            .doc(user.uid)
            .delete()
            .then()
            .catch((error) => {
                console.error('Failed to delete the document data for the user: ' + user.uid, error);
            });
    }
});
