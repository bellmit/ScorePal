import * as admin from 'firebase-admin';
import * as functions from 'firebase-functions';

/**
 * Listen for changes to matches to count the wins and losses the account holder records
 */
 exports.processMatchChange = functions.firestore
 .document('users/{userId}/matches/{matchId}')
 .onUpdate((change, context) => {
     // get the change in data we experienced
     const data = change.after.data();
     const previousData = change.before.data();

     if (data && data.analysis && data.analysis.complete) {
         // this match is complete and there is data we can record
         var winChange = 0;
         var lossChange = 0;
         var playedChange = 0;
         if (!previousData) {
             // there is no previous data, can just increment the proper counters
             if (data.analysis.user_won) {
                 // they won this match
                 ++winChange;
             }
             if (data.analysis.user_lost) {
                // they lost this match
                ++lossChange;
            }
            if (winChange > 0 || lossChange > 0) {
                // played this then
                ++playedChange;
            }
         } else {
             // there is previous data
             if (data.analysis.user_won !== previousData.analysis.user_won) {
                 // there is a change
                 if (previousData.analysis.user_won) {
                     // they had won, they no longer did
                     --winChange;
                 } else {
                     // the hadn't won, now they have
                     ++winChange;
                 }
             }
             if (data.analysis.user_lost !== previousData.analysis.user_lost) {
                // there is a change
                if (previousData.analysis.user_lost) {
                    // they had lost, they no longer have
                    --lossChange;
                } else {
                    // the hadn't lost, now they have
                    ++lossChange;
                }
            }
            if (data.analysis.user_won || data.analysis.user_lost) {
                // played this then, this is new if they hadn't either previously
                if (!previousData.analysis.user_won && !previousData.analysis.user_lost) {
                    ++playedChange;
                }
            } else {
                // didn't play this match, this is new if they played previously
                if (previousData.analysis.user_won || previousData.analysis.user_lost) {
                    --playedChange;
                }
            }
         }
         if (winChange !== 0 || lossChange !== 0 || playedChange !== 0) {
            // there is some change in data, so change it then
            return admin.firestore().collection('users')
                .doc(context.params.userId)
                .update({
                    wins: admin.firestore.FieldValue.increment(winChange),
                    losses: admin.firestore.FieldValue.increment(lossChange),
                    played: admin.firestore.FieldValue.increment(playedChange),
                    })
                .catch((error: any) => {
                    console.error('Failed to increment the change in wins and losses for a user', error);
                });
        }
     }
     // nothing to do
     return false;
 });
