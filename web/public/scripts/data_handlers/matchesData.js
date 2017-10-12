'use strict';

const MAX_MATCHES_RETRIEVAL = 10;

// Initialise the data object that will do all our data operations
function UmpyrDataMatches(umpyrData, database) {
    // remember the reference to umpyrData so we can pass back information to the master class
    this.umpyrData = umpyrData;
    this.database = database;

    // set our member data
    this.matches = [];
    this.matchesSubmitted = [];
    this.recentOpponents = [];

    // load the data
    this.loadMatches();
    this.loadMatchesSubmitted();
}

UmpyrDataMatches.prototype.getMatches = function() {
    if (this.matches) {
        // return the array of matches currently loaded
        return this.matches;
    } else {
        // return an empty array all the time so don't have to check
        return [];
    }
};

UmpyrDataMatches.prototype.getMatchesSubmitted = function() {
    if (this.matchesSubmitted) {
        // return the array of matches submitted currently loaded
        return this.matchesSubmitted;
    } else {
        // return an empty array all the time so don't have to check
        return [];
    }
};

UmpyrDataMatches.prototype.getLatestMatch = function() {
    // find and return the latest match
    if (this.matches) {
        // return the array of matches currently loaded
        var latestMatch;
        for (var i = 0; i < this.matches.length; i += 1) {
            if (!latestMatch || this.matches[i].matchPlayedDate > latestMatch.matchPlayedDate) {
                // this is after the current latest
                latestMatch = this.matches[i];
            }
        }
        return latestMatch;
    } else {
        // no matches
        return undefined;
    }
};

UmpyrDataMatches.prototype.getRecentOpponents = function() {
    // return the list of recent opponenets that are not friends already
    if (this.recentOpponents) {
        // return the array of names of recent opponents
        return this.recentOpponents;
    } else {
        // return an empty array to be safe
        return [];
    }
};

// Loads all our data
UmpyrDataMatches.prototype.loadMatches = function() {
    // Load and listens for new matches
    this.matchesRef = this.database.ref('matches/' + this.umpyrData.currentUser.ID);
    // make sure we remove all previous listeners
    this.matchesRef.off();
    this.matches = [];
    var thisData = this;
    // load all our matches
    var setMatch = function(data) {
        // load the data into a nice object
        var match = new Match(data);
        // check this is not already in the list
        var isMatchExist = false;
        for (var i = 0; i < thisData.matches.length; i += 1) {
            if (match.id === thisData.matches[i].id) {
                // this is the same, just update this
                thisData.matches[i] = match;
                isMatchExist = true;
                break;
            }
        }
        if (!isMatchExist) {
            // just push to the end of the list
            thisData.matches.push(match);
        }
        // check to see if there is an opponent we should know about...
        for (var i = 0; i < 4; i += 1) {
            // for each player, see if we have an ID, if we do then it is a genuine friend
            if (!match.getPlayerId(i)) {
                // there is no ID, this is a player without an ID, remember this so we can add to the list of friends
                var playerName = match.getPlayerTitle(i);
                if (playerName && !playerName.toLowerCase().startsWith('player')) {
                    // have a name that isn't 'player one' etc, add as a recent opponent
                    var isPlayerFound = false;
                    for (var j = 0; j < thisData.recentOpponents.length; j += 1) {
                        if (thisData.recentOpponents[j] === playerName) {
                            // this is a match, found this already
                            isPlayerFound = true;
                            break;
                        }
                    }
                    if (!isPlayerFound) {
                        // not in the list, add to the list
                        thisData.recentOpponents.push(playerName);
                    }
                }
            }
        }
        // inform any listeners of this change to the list
        thisData.umpyrData.informDataListenersOfChange(thisData.matches, match, 'added');
    }.bind(this);
    // limit the number of matches retrieved for the main list
    this.matchesRef.limitToLast(MAX_MATCHES_RETRIEVAL).on('child_added', setMatch);
    //this.matchesRef.limitToLast(MAX_MATCHES_RETRIEVAL).on('child_changed', changeMatch);
};

// Loads all our data
UmpyrDataMatches.prototype.loadMatchesSubmitted = function() {
    // Load and listens for new matches that have been submitted to us
    this.matchesSubRef = this.database.ref('matches_submitted/' + this.umpyrData.currentUser.ID);
    // make sure we remove all previous listeners
    this.matchesSubRef.off();
    this.matchesSubmitted = [];
    var thisData = this;
    // load all our matches
    var setMatch = function(data) {
        // load the data into a nice object
        var match = new Match(data);
        // check this is not already in the list
        var isMatchExist = false;
        for (var i = 0; i < thisData.matchesSubmitted.length; i += 1) {
            if (match.id === thisData.matchesSubmitted[i].id) {
                // this is the same, just update this
                thisData.matchesSubmitted[i] = match;
                isMatchExist = true;
                break;
            }
        }
        if (!isMatchExist) {
            // just push to the end of the list
            thisData.matchesSubmitted.push(match);
        }
        // inform any listeners of this change to the list
        thisData.umpyrData.informDataListenersOfChange(thisData.matchesSubmitted, match, 'added');
    }.bind(this);
    this.matchesSubRef.on('child_added', setMatch);
    //this.matchesSubRef.on('child_changed', changeMatch);
};

UmpyrDataMatches.prototype.getMatchesBetweenDates = function(start, end, callback) {
    this.matchesRef = this.database.ref('matches/' + this.umpyrData.currentUser.ID);
    // make sure we remove all previous listeners
    this.matchesRef.off();

    // create the date strings that matches are coded as for the search from - to
    var match = new Match();
    var startDateString = match.createDateString(start);
    var endDateString = match.createDateString(end);

    // load the matches from start to end 
    var setHistoryMatch = function(data) {
        callback(new Match(data));
    }.bind(this);
    this.matchesRef
        .orderByChild("matchPlayedDate")
        .startAt(startDateString)
        .endAt(endDateString)
        .on('child_added', setHistoryMatch);
};

UmpyrDataMatches.prototype.acceptSubmittedMatch = function(match, successFunction, errorFunction) {
    // do accept a match we just save it to the list of our played matches, getting the data
    // of the match to save will recalculate the isUserWinner etc based on the user being us now
    var thisData = this;
    // remember the match ID of the match as it is, saving changes it to the new one
    var submittedMatchId = match.id;
    // save the match to our list of matches, on success delete the match submitted
    this.saveMatch(match, function() {
        // success, remove the submitted match, set the ID to the one we want to remove
        match.id = submittedMatchId;
        // and delete it now, passing the original success and error functions
        thisData.deleteMatchSubmitted(match, successFunction, errorFunction);
    }, errorFunction, false);
}

UmpyrDataMatches.prototype.saveMatch = function(match, successFunction, errorFunction, isSubmitToOpponents) {
    // need the user ID of ourselves to get the correct match data
    var userId = this.umpyrData.getUserId();
    // Add a new match entry to the Firebase Database.
    this.database.ref('matches/' + userId).push(match.getMatchData(userId))
        .then((snap) => {
            match.id = snap.key;
            successFunction();
        })
        .catch(errorFunction);
    if (isSubmitToOpponents) {
        // we have saved this match, fine, but the match might also involve other players...
        // let's see if it does
        var playersSentScore = [];
        for (var i = 0; i < 4; i += 1) {
            var playerId = match.getPlayerId(i);
            if (playerId && playerId.length > 0 && playerId !== userId) {
                // there is a player ID and it is not ours, send this match data to them
                if (!isValInList(playerId, playersSentScore)) {
                    // didn't already do this player, do them now
                    this.saveMatchToFriend(match, playerId);
                    playersSentScore.push(playerId);
                }
            }
        }
    }
};

UmpyrDataMatches.prototype.saveMatchToFriend = function(match, friendId) {
    // save this match data in the list of our friend
    var userId = this.umpyrData.getUserId();
    this.database.ref('matches_submitted/' + friendId)
        .push(match.getMatchData(userId))
        .then((snap) => {
            // when it works - remember the ID of the match set in the database
            match.id = snap.key;
            // success
        })
        .catch(er => {
            // error, log it
            console.error('...', er);
        });
};

UmpyrDataMatches.prototype.removeFromList = function(match, matchesList) {
    if (matchesList) {
        for (var i = 0; i < matchesList.length; i += 1) {
            if (match.id === matchesList[i].id) {
                // this is the one, remove this
                matchesList.splice(i, 1);
                break;
            }
        }
    }
    // inform any listeners of this change
    this.umpyrData.informDataListenersOfChange(matchesList, match, 'removed');
};

UmpyrDataMatches.prototype.deleteMatch = function(match) {
    // remove this match entry to the Firebase Database.
    var thisData = this;
    this.database.ref('matches/' + this.umpyrData.currentUser.ID + "/" + match.id).remove(function(error) {
        if (!error) {
            // removed, remove from our list here
            thisData.removeFromList(match, thisData.matches);
        }
    });
    return true;
};

UmpyrDataMatches.prototype.deleteMatchSubmitted = function(match) {
    // remove this match entry to the Firebase Database.
    var thisData = this;
    this.database.ref('matches_submitted/' + this.umpyrData.currentUser.ID + "/" + match.id).remove(function(error) {
        if (!error) {
            // removed, remove from our list here
            thisData.removeFromList(match, thisData.matchesSubmitted);
        }
    });
    return true;
};