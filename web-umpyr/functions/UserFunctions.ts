import * as admin from 'firebase-admin';
import * as functions from 'firebase-functions';
/**
 * intercepts the call to delete a user to delete their user data along with it
 */
exports.deleteUserData = functions.auth.user().onDelete((user) => {
    // the user is deleted, delete the document
    if (!user) {
        console.error('User deleted without a user document passed');
        return false;
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