import * as admin from 'firebase-admin';

// import { firestore } from 'firebase-admin';

// Start writing Firebase Functions
// https://firebase.google.com/docs/functions/typescript

admin.initializeApp();

module.exports = {
    ...require('./UserFunctions')
};
