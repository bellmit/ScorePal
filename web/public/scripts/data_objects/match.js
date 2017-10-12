'use strict';

// Initializes the match code object
function Match(data) {
    // setup the limit of the undo stack
    this.K_UNDOLIMIT = 20;
    // and load the data specified
    if (!data) {
        // there is no data, set all the data to be from the defaults
        this.id = "";
        this.matchPlayedDate = this.createDateString();
        this.isDoubles = false;
        this.sport = "";
        this.description = "";
        this.isUserPlayed = false;
        this.isUserWinner = false;
        this.isUserLoser = false;
        this.playerOneId = "";
        this.playerOneTitle = "";
        this.playerOnePartnerId = "";
        this.playerOnePartnerTitle = "";
        this.playerTwoId = "";
        this.playerTwoTitle = "";
        this.playerTwoPartnerId = "";
        this.playerTwoPartnerTitle = "";
        // and create the state
        this.state = new ScoreState();
    } else {
        // get all the data from that specified (from the firebase entry)
        var val;
        if (!data.val) {
            // there is no val in the data (not from firebase?) try to the data as the val...
            val = data;
            this.id = val.id;
        } else {
            // val is an option, use it
            val = data.val();
            this.id = data.key;
        }
        this.matchPlayedDate = val.matchPlayedDate;
        this.isDoubles = val.isDoubles || false;
        this.sport = val.sport || "";
        this.description = val.description || "";
        this.isUserPlayed = val.isUserPlayed || false;
        this.isUserWinner = val.isUserWinner || false;
        this.isUserLoser = val.isUserLoser || false;
        this.playerOneId = val.playerOneId;
        this.playerOneTitle = val.playerOneTitle;
        this.playerOnePartnerId = val.playerOnePartnerId;
        this.playerOnePartnerTitle = val.playerOnePartnerTitle;
        this.playerTwoId = val.playerTwoId;
        this.playerTwoTitle = val.playerTwoTitle;
        this.playerTwoPartnerId = val.playerTwoPartnerId;
        this.playerTwoPartnerTitle = val.playerTwoPartnerTitle;
        // and create the state
        this.state = new ScoreState(val.scoreState);
    }
}

Match.prototype.getMatchData = function(userId) {
    // now return the data to represent this object in the firebase store - will get back on construction
    var newObject = {
        matchPlayedDate: this.getMatchPlayedDate(),
        isDoubles: this.isDoubles || false,
        sport: this.sport || "",
        description: this.description || "",
        playerOneId: this.getPlayerId(0) || "",
        playerOneTitle: this.getPlayerTitle(0),
        playerOnePartnerId: this.getPlayerId(2) || "",
        playerOnePartnerTitle: this.getPlayerTitle(2),
        playerTwoId: this.getPlayerId(1) || "",
        playerTwoTitle: this.getPlayerTitle(1),
        playerTwoPartnerId: this.getPlayerId(3) || "",
        playerTwoPartnerTitle: this.getPlayerTitle(3),
        scoreState: this.state.getScoreData()
    };
    if (this.id && this.id.length > 0) {
        // there is an ID - have loaded this so passing it around we want the ID to be passed around as well
        newObject.id = this.id;
    }
    // only add the data for played / won if the user did in fact play, to initiate the function on the server each time
    if (this.getIsUserPlayed(userId)) {
        // the user played - add the sport that they played as the answer to the variable
        newObject.isUserPlayed = this.sport;
        if (this.getMatchWinner() < 9) {
            // someone won (0 || 1) so add if this user won / lost
            if (this.getIsUserWinner(userId)) {
                // they won - tell us what they won at
                newObject.isUserWinner = this.sport;
            } else {
                // they lost - tell us what they lost at
                newObject.isUserLoser = this.sport;
            }
        }
    }
    // and retun the data
    return newObject;
};

Match.prototype.calculateSetsFromGames = function() {
    // okay then, we want to be sure that the sets is built from the games
    // they have entered - for consistency and reliability
    this.state.sets = [0, 0];
    for (var i = 0; i < this.state.setResults.length; i += 1) {
        if (this.state.setResults[i][0] > this.state.setResults[i][1]) {
            // the first player / pair of players won this set
            this.state.sets[0] += 1;
        } else if (this.state.setResults[i][0] < this.state.setResults[i][1]) {
            // the second player / pair of players won that set
            this.state.sets[1] += 1;
        }
        // else it was a draw. I know that they might not have got enough
        // games to actually win but here we assume the set is over - or
        // they want the score as best as we can manage anyway...
    }
};

Match.prototype.getSets = function(player) {
    return this.state.sets[player];
};

Match.prototype.getSetResult = function(setIndex, player) {
    if (setIndex < this.state.setResults.length) {
        return this.state.setResults[setIndex][player];
    } else {
        return 0;
    }
};

Match.prototype.setSets = function(player, value) {
    // set the value for sets
    this.state.sets[player] = value;
};

Match.prototype.getGames = function(player) {
    return this.state.games[player];
};

Match.prototype.setGames = function(player, value) {
    // set the value for sets
    this.state.games[player] = value;
};

Match.prototype.getPoints = function(player) {
    return this.state.points[player];
};

Match.prototype.getTotalPoints = function(player) {
    return this.state.pointsTotal[player];
};

Match.prototype.setPoints = function(player, value) {
    // set the value for sets
    this.state.points[player] = value;
};

Match.prototype.setTotalPoints = function(player, value) {
    this.state.pointsTotal[player] = value;
};

Match.prototype.getDescription = function() {
    return this.description;
};

Match.prototype.setDescription = function(desc) {
    this.description = desc;
};

Match.prototype.getIsUserPlayed = function(userId) {
    // the user has played if they are in the list of IDs
    if (userId === undefined) {
        // no id, not a player
        return false;
    }
    return userId === this.getPlayerId(0) ||
        userId === this.getPlayerId(1) ||
        userId === this.getPlayerId(2) ||
        userId === this.getPlayerId(3);
};

Match.prototype.getIsUserWinner = function(userId) {
    if (userId === undefined) {
        // no id, not a winner
        return false;
    }
    // the user is the winner if their ID is in the team that is the winner
    switch (Number(this.getMatchWinner())) {
        case 0:
            // if the user id is player one, or player one partner then yes
            return userId === this.getPlayerId(0) || userId === this.getPlayerId(2);
        case 1:
            // if the user id is player two, or player two partner then yes
            return userId === this.getPlayerId(1) || userId === this.getPlayerId(3);
        default:
        case 9:
            // no winner
            return false;
    }
};

Match.prototype.getMatchWinner = function() {
    // the winner is the player / pair of players with the most sets
    if (Number(this.state.sets[0]) > Number(this.state.sets[1])) {
        // return that index zero (player one) is the winner
        return 0;
    } else if (Number(this.state.sets[0]) < Number(this.state.sets[1])) {
        // return that index one (player two) is the winner
        return 1;
    } else {
        // the sets give no clear winner
        if (Number(this.state.games[0]) > Number(this.state.games[1])) {
            // index zero (player one) had more games in the end, they won
            return 0;
        } else if (Number(this.state.games[1]) > Number(this.state.games[0])) {
            // index one (player two) had more games in the end, they won
            return 1;
        } else {
            // games give no clear winner
            if (Number(this.state.points[0]) > Number(this.state.points[1])) {
                // index zero (player one) had more points in the end, they won
                return 0;
            } else if (Number(this.state.points[1]) > Number(this.state.points[0])) {
                // index one (player two) had more points in the end, they won
                return 1;
            } else {
                // points give no clear winner
                if (Number(this.state.pointsTotal[0]) > Number(this.state.pointsTotal[1])) {
                    // index zero (player one) had more pointsTotal in the end, they won
                    return 0;
                } else if (Number(this.state.pointsTotal[1]) > Number(this.state.pointsTotal[0])) {
                    // index one (player two) had more pointsTotal in the end, they won
                    return 1;
                } else {
                    // pointsTotal give no clear winner, is this even a game!?
                    return 9;
                }
            }
        }
    }
};

Match.prototype.recordMatchWinner = function(matchWinner) {
    this.state.recordMatchWinner(matchWinner);
};

Match.prototype.addGame = function(player) {
    // add a game for the specified player
    this.state.games[player] += 1;
    // and reset the points
    this.resetPoints();
};

Match.prototype.addPoint = function(player) {
    // add the point now for this player
    this.state.pointsTotal[player] += 1;
    // and we need to push this result to the history
    this.state.historicPoints.push(player);
};

Match.prototype.removeLastPoint = function(controller, noToRemove) {
    // we used to keep a stack of the states, but replaying the entire match from the history is actually
    // super quick so now we just clear the state and replay everything in a very fast blast of calculation
    if (!noToRemove) {
        // they didn't specify a number to remove, remove just the last one
        noToRemove = 1;
    }
    var previousHistory = this.state.historicPoints;
    // clear the current state of everything
    this.state.reset();
    // and replay the history, less the number to remove
    for (var i = 0; i < previousHistory.length - noToRemove; i += 1) {
        controller.addPoint(previousHistory[i]);
    }
};

Match.prototype.swapServer = function() {
    this.state.currentServer = this.getOtherPlayer(this.state.currentServer);
};

Match.prototype.swapEnds = function() {
    this.state.currentNorth = this.getOtherPlayer(this.state.currentNorth);
};

Match.prototype.getOtherPlayer = function(player) {
    if (player === 0) {
        return 1;
    } else {
        return 0;
    }
};

Match.prototype.recordSetResult = function(playerOneScore, playerTwoScore, setIndex, isAddSetWon) {
    // record that the current set has been won by the currently winning player
    if (typeof setIndex === 'undefined') {
        // we want to just set the latest so search for the data to set and set it
        var isDataSet = false;
        for (setIndex = 0; setIndex < this.state.setResults.length; setIndex += 1) {
            if (this.state.setResults[setIndex][0] === 0 &&
                this.state.setResults[setIndex][1] === 0) {
                // there is no data in this result, set this result at this index by breaking
                this.state.setResults[setIndex][0] = playerOneScore;
                this.state.setResults[setIndex][1] = playerTwoScore;
                isDataSet = true;
                break;
            }
        }
        if (!isDataSet) {
            // the array is not large enough, add a new entry
            this.state.setResults.push([playerOneScore, playerTwoScore]);
        }
    } else {
        // the have specified a number, create new data until there is enough
        while (this.state.setResults.length <= setIndex) {
            // push empty data ahead of this
            this.state.setResults.push([0, 0]);
        }
        // there is data for sure at the index now so set it direct
        this.state.setResults[setIndex][0] = playerOneScore;
        this.state.setResults[setIndex][1] = playerTwoScore;
    }
    if (isAddSetWon) {
        // want to increment the number of sets the winner now has
        if (playerOneScore > playerTwoScore) {
            this.state.sets[0] += 1;
        } else if (playerTwoScore > playerOneScore) {
            this.state.sets[1] += 1;
        }
    }
};

Match.prototype.getNumberSetsPlayed = function() {
    // want to return the number of sets that have been played in this match
    var numberSetsPlayed = 0;
    for (var i = 0; i < this.state.setResults.length; i += 1) {
        if (this.state.setResults[i][0] > 0 || this.state.setResults[i][1] > 0) {
            // there is some result in this entry of data, this set was played
            numberSetsPlayed += 1;
        }
    }
    return numberSetsPlayed;
};

Match.prototype.resetPoints = function() {
    this.state.resetPoints();
};

Match.prototype.resetGames = function() {
    this.state.resetGames();
};

Match.prototype.resetSets = function() {
    this.state.resetSets();
};

Match.prototype.reset = function() {
    // reset / set all the member data
    this.previousStates = [];
    // reset the state also
    this.state.reset();
};

Match.prototype.getMatchId = function() {
    return this.id;
};

Match.prototype.setMatchPlayedDate = function(date) {
    // set the string that represents the date on this match as the date object string
    this.matchPlayedDate = this.createDateString(date);
};

Match.prototype.getMatchPlayedDate = function() {
    if (!this.matchPlayedDate) {
        // create the played date here
        this.matchPlayedDate = this.createDateString();
    }
    return this.matchPlayedDate;
};

Match.prototype.getTimePlayedFor = function() {
    return this.state.secondsGameDuration;
};

Match.prototype.setScoreMode = function(sport) {
    // set the sport as a nice string on this match (for stats)
    this.sport = sport.title;
    // and the ID of the sport on the current state
    this.state.currentScoreMode = sport.id;
};

Match.prototype.setSetsOption = function(setsOption) {
    this.state.currentSetsOption = setsOption;
};

Match.prototype.getScoreMode = function() {
    return this.state.currentScoreMode;
};

Match.prototype.setTimePlayedFor = function(hours, minutes, seconds) {
    this.setSecondsPlayedFor((hours * 3600) + (minutes * 60) + seconds);
};

Match.prototype.setSecondsPlayedFor = function(seconds) {
    this.state.secondsGameDuration = seconds;
};

Match.prototype.createDateString = function(date) {
    if (!date) {
        date = new Date();
    }
    return date.getFullYear() +
        ("0" + (date.getMonth() + 1)).slice(-2) +
        ("0" + date.getDate()).slice(-2) +
        ("0" + date.getHours()).slice(-2) +
        ("0" + date.getMinutes()).slice(-2) +
        ("0" + date.getSeconds()).slice(-2);
};

Match.prototype.getMatchPlayedDateFormatted = function() {
    var yr = parseInt(this.matchPlayedDate.substring(0, 4));
    var mon = parseInt(this.matchPlayedDate.substring(4, 6));
    var dt = parseInt(this.matchPlayedDate.substring(6, 8));
    var hr = parseInt(this.matchPlayedDate.substring(8, 10));
    var mn = parseInt(this.matchPlayedDate.substring(10, 12));
    var sc = parseInt(this.matchPlayedDate.substring(12, 14));
    return new Date(yr, mon - 1, dt, hr, mn, sc);
};

Match.prototype.getTeamTitle = function(team) {
    var teamTitle = this.getPlayerTitle(team);
    if (this.isDoubles) {
        teamTitle += ' and ' + this.getPlayerTitle(team + 2);
    }
    return teamTitle;
};

Match.prototype.getPlayerTitle = function(player) {
    switch (player) {
        case 0:
            //player 1
            if (!this.playerOneTitle || this.playerOneTitle === "") {
                return "Player One";
            } else {
                return this.playerOneTitle;
            }
            break;
        case 1:
            //player 2
            if (!this.playerTwoTitle || this.playerTwoTitle === "") {
                return "Player Two";
            } else {
                return this.playerTwoTitle;
            }
            break;
        case 2:
            //player 1's partner
            if (!this.playerOnePartnerTitle || this.playerOnePartnerTitle === "") {
                return "Player One Partner";
            } else {
                return this.playerOnePartnerTitle;
            }
            break;
        case 3:
            //player 2's partner
            if (!this.playerTwoPartnerTitle || this.playerTwoPartnerTitle === "") {
                return "Player Two Partner";
            } else {
                return this.playerTwoPartnerTitle;
            }
            break;
        default:
            return "unknown";
    }
};

Match.prototype.getPlayerId = function(player) {
    switch (player) {
        case 0:
            //player 1
            return this.playerOneId;
        case 1:
            //player 2
            return this.playerTwoId;
        case 2:
            //player 1's partner
            return this.playerOnePartnerId;
        case 3:
            //player 2's partner
            return this.playerTwoPartnerId;
        default:
            return undefined;
    };
};

Match.prototype.getMatchSummary = function(firstPlayer, secondPlayer, userId, spanClass, spanContainerClass) {
    // this is to be a nice summary of the score, if they pass the user ID then we can try
    // to put them first each time so they know their results over the other person's
    if (firstPlayer === 'undefined' || firstPlayer === null) {
        firstPlayer = 0;
    }
    if (secondPlayer === 'undefined' || firstPlayer === null) {
        secondPlayer = 1;
    }
    if (userId) {
        // they specified a user ID, is this player / team two?
        if (userId === this.playerTwoId || userId === this.playerTwoPartnerId) {
            // the user was in team two, swap the first and seconds over
            firstPlayer = 1;
            secondPlayer = 0;
        }
    }
    if (!spanClass) {
        // no style specified, use the default
        spanClass = 'score-pair';
    }
    if (!spanContainerClass) {
        spanContainerClass = 'score-pairs-container';
    }
    var totalSets = this.state.currentSetsOption;
    var totalGames = this.getGames(0) + this.getGames(1);
    var totalPoints = this.getPoints(0) + this.getPoints(1);
    var totalPointsTotal = this.getTotalPoints(0) + this.getTotalPoints(1);
    var i, firstSets, secondSets;
    var matchSummary = "<div class='" + spanContainerClass + "'>";
    if (totalSets > 0 || totalGames > 0) {
        // sets or games were played, summarise these
        // if games were played, the points for each game should be in the set results just the same as sets
        for (i = 0; i < totalSets; i += 1) {
            firstSets = this.getSetResult(i, firstPlayer);
            secondSets = this.getSetResult(i, secondPlayer);
            if (Number(firstSets) > 0 || Number(secondSets) > 0) {
                matchSummary += "<span class='" + spanClass + "'>" + firstSets + "-" + secondSets + "<\/span>";
            }
        }
    } else if (totalPoints > 0) {
        // there were just points, summarise this
        matchSummary += "<span class='" + spanClass + "'>" + this.getPoints(firstPlayer) + "-" + this.getPoints(secondPlayer) + "<\/span>";
    } else if (totalPointsTotal > 0) {
        // there were just points totals, summarise this
        matchSummary += "<span class='" + spanClass + "'>" + this.getTotalPoints(firstPlayer) + "-" + this.getTotalPoints(secondPlayer) + "<\/span>";
    } else {
        // there might be sets even though there were none recorded, points does this, show those intead
        firstSets = this.getSets(firstPlayer);
        if (!firstSets || Number.isNaN(firstSets)) {
            // cope with the NaN we get when nothing at all is entered
            firstSets = 0;
        }
        secondSets = this.getSets(secondPlayer);
        if (!secondSets || Number.isNaN(secondSets)) {
            // cope with the NaN we get when nothing at all is entered
            secondSets = 0;
        }
        matchSummary += "<span class='" + spanClass + "'>" + firstSets + "-" + secondSets + "<\/span>";
    }
    matchSummary += "</div>";
    return matchSummary;
};