import * as admin from 'firebase-admin';
import * as functions from 'firebase-functions';

/**
 * Listen for changes to matches to count the wins and losses the account holder records
 */
 exports.processMatchWinnings = functions.firestore
 .document('users/{userId}/matches/{matchId}')
 .onWrite((change, context) => {
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

 /**
 * Listen for changes to matches to send the player's opponents the score also
 */
  exports.processMatchOpponents = functions.firestore
  .document('users/{userId}/matches/{matchId}')
  .onWrite(async (change, context) => {
      // get the change in data we experienced, we are listening for a write to
      // be sure to always have the latest in the inbox of the player's the user played
      const data = change.after.data();
      // check all the data is there, needs to be complete and not received_from an inbox
      if (data && data.setup &&
        data.analysis && data.analysis.complete &&
        !data.received_from) {
          // this match is complete we can try to find all our opponents and send
          // them the score also
          var addresses = [
            ...data.setup['player1_addresses'],
            ...data.setup['player2_addresses'],
            ...data.setup['player3_addresses'],
            ...data.setup['player4_addresses'],
          ];
          // so we need to find any users with each of these addresses and send them
          // the results of the match to see if they are interested
          for (var i = 0; i < addresses.length; ++i) {
              const lcAddress = addresses[i].toLowerCase();
              const userDocs = await admin.firestore().collection('users').where('email_lc', '==', lcAddress).get();
              if (null != userDocs && !userDocs.empty) {
                  // have users that match this email, add the match to their inbox
                  const inboxData = {
                      ...data,
                      'received_from': context.params.userId,
                  } as any;
                  // being sure the state is not deleted or whatever from the source player
                  inboxData['state'] = 'communicated';
                  // and put this in all the inboxes there are (will have a 'received_from' so when put into matches
                  // the subsequent change will not trigger this going back to another one and round and round we would go)
                  userDocs.forEach(async (userDoc) => {
                      // first check that we are not sending a match to ourselves
                      if (userDoc.id !== context.params.userId) {
                        // for each userDoc in the list, add the match data we just detected has changed to their inbox
                        await admin.firestore()
                            .collection('users/' + userDoc.id + '/inbox')
                            .doc(context.params.matchId)
                            .set(inboxData);
                      }
                  });
              }
          }
          // we waited til everything was complete in this function so we can just return here
          return true;
      }
      // nothing to do
      return false;
  });
