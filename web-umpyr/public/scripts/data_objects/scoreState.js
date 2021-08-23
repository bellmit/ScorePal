'use strict';

// Initializes the scoring state object
function ScoreState(data) {
    // reset all the game / set members
    this.reset();

    if (data) {
        // there is data specified, load the state from what was specified in the firebase object
        this.dataString = data;
        this.fromDataString();
    }
}

ScoreState.prototype.getScoreData = function() {
    // return this state as a data object to set in firebase - will get back in constructor
    return this.toDataString();
};

ScoreState.prototype.reset = function() {
    // reset all the game / set members
    this.resetPoints();
    this.resetGames();
    this.resetSets();

    // need to know if we are in a tie-break
    this.isTieBreak = false;
    // and the current server (zero-index counter)
    this.currentServer = 0;
    // and the current player / pair of players that are at the north-end
    this.currentNorth = 0;
    // and the server that served in the tie
    this.tieBreakServer = this.currentServer;

    // and initialise all the scoring data as-defaults
    this.secondsStartTime = 0;
    this.secondsGameDuration = 0;
    this.currentScoreMode = 0;
    this.currentSetsOption = 0;
    this.matchWinner = 9;
    // and empty the history
    this.historicPoints = [];
    this.pointsTotal = [0, 0];
};

ScoreState.prototype.setScoreMode = function(scoreMode) {
    this.currentScoreMode = scoreMode;
};

ScoreState.prototype.setSetsOption = function(setsOption) {
    this.currentSetsOption = setsOption;
};

ScoreState.prototype.resetPoints = function() {
    this.points = [0, 0];
};

ScoreState.prototype.resetGames = function() {
    this.games = [0, 0];
};

ScoreState.prototype.resetSets = function() {
    this.sets = [0, 0];
    this.setResults = [];
};

ScoreState.prototype.recordMatchWinner = function(matchWinner) {
    // record the match winner, will be undefined if the match is not over
    if (typeof matchWinner === 'undefined') {
        // has to be 0 or 1 as only two teams, so nine is clearly a single char error code for this...
        this.matchWinner = 9;
    } else {
        this.matchWinner = matchWinner;
    }
};

ScoreState.prototype.fromDataString = function() {
    // construct the data from the compacted string
    var dataCommand = this.extractChars(1);
    if (dataCommand === 'u') {
        // first get the version - one char only but representing the number of it
        var dataVersion = this.extractChars(1);
        // now process the data for this version
        switch (dataVersion) {
            case 'a':
                this.parseVersionOneScoreData();
                break;
            default:
                //TODO error reporting of unsupported version
                break;
        }
    }
};

ScoreState.prototype.parseVersionOneScoreData = function() {
    // get the code that we need to respond with
    var dataCode = this.extractValueToColon();
    // get the start and duration timers
    this.secondsStartTime = this.extractValueToColon();
    this.secondsGameDuration = this.extractValueToColon();
    // get the active mode
    this.currentScoreMode = this.extractValueToColon();
    // and the sets option
    this.currentSetsOption = this.extractValueToColon();
    // now the sets winner
    this.matchWinner = this.extractChars(1);
    this.isInTieBreak = Number(this.extractChars(1)) == 1;
    // get the current server
    this.currentServer = Number(this.extractChars(1));
    this.currentNorth = Number(this.extractChars(1));
    this.sets = [this.extractValueToColon(), this.extractValueToColon()];
    // do the games for each set played
    var totalSets = this.extractValueToColon();
    this.setResults = [];
    for (var i = 0; i < totalSets; i += 1) {
        this.setResults.push([this.extractValueToColon(), this.extractValueToColon()]);
    }
    this.points = [this.extractValueToColon(), this.extractValueToColon()];
    this.games = [this.extractValueToColon(), this.extractValueToColon()];
    this.pointsTotal = [this.extractValueToColon(), this.extractValueToColon()];
    // now do all the historic points
    var noHistoricPoints = this.extractValueToColon();
    this.historicPoints = [];
    // load the historic points
    var dataCounter = 0;
    while (dataCounter < noHistoricPoints) {
        // while there are points to get, get them
        var dataReceived = this.extractHistoryValue();
        // this char contains somewhere between one and eight values all bit-shifted, extract them now
        var bitCounter = 0;
        while (bitCounter < 10 && dataCounter < noHistoricPoints) {
            var bitValue = 1 & (dataReceived >> bitCounter++);
            // add this to the list of value received
            this.historicPoints.push(bitValue);
            ++dataCounter;
        }
    }
};

ScoreState.prototype.toDataString = function() {
    this.dataString = "";
    // now write all the data, first comes the command character
    this.dataString += 'u';
    // then the version
    this.dataString += 'a';
    // then the data code, this has a colon
    this.dataString += '1:';
    // now the start and duration timers which also have colons
    this.dataString += this.secondsStartTime + ':';
    this.dataString += this.secondsGameDuration + ':';
    // now the active mode and option
    this.dataString += this.currentScoreMode + ':';
    this.dataString += this.currentSetsOption + ':';
    // the match winner
    this.dataString += this.matchWinner;
    // are we in a tie
    this.dataString += this.isInTieBreak ? 1 : 0;
    // the server
    this.dataString += this.currentServer;
    this.dataString += this.currentNorth;
    // now the sets
    this.dataString += this.sets[0] + ':';
    this.dataString += this.sets[1] + ':';
    // now the games for each set player, push the length of the list
    this.dataString += this.setResults.length + ':';
    // now all the values
    for (var i = 0; i < this.setResults.length; i += 1) {
        this.dataString += this.setResults[i][0] + ':';
        this.dataString += this.setResults[i][1] + ':';
    }
    // points
    this.dataString += this.points[0] + ':';
    this.dataString += this.points[1] + ':';
    // games
    this.dataString += this.games[0] + ':';
    this.dataString += this.games[1] + ':';
    // total points
    this.dataString += this.pointsTotal[0] + ':';
    this.dataString += this.pointsTotal[1] + ':';
    // no historic points
    if (!this.historicPoints) {
        // there are no historic points
        this.dataString += '0:';
    } else {
        // write out the historic points we gathered
        this.dataString += this.historicPoints.length + ':';
        // and then all the historic points we have
        var bitCounter = 0;
        var dataPacket = 0;
        for (var i = 0; i < this.historicPoints.length; i += 1) {
            // add this value to the data packet
            dataPacket |= this.historicPoints[i] << bitCounter;
            // and increment the counter, sending as radix32 number means we can store 10 bits of data (up to 1023 base 10)
            bitCounter += 1;
            if (bitCounter >= 10) {
                // exceeded the size for next time, send this packet
                if (dataPacket < 32) {
                    // this will be print as '0' up to 'F' but we need it to be '0F' as expecting a fixed length...
                    // this is true for hex - who knows how a radix32 number is printed - but whatever nice that we get 10 values
                    this.dataString += '0';
                }
                this.dataString += dataPacket.toString(32);
                // and reset the counter and data
                bitCounter = 0;
                dataPacket = 0;
            }
        }
        if (bitCounter > 0) {
            // there was data we failed to send, only partially filled - send this anyway
            if (dataPacket < 32) {
                // this will be print as '0' up to 'F' but we need it to be '0F' as expecting a fixed length...
                // this is true for hex - who knows how a radix64 number is printed - but whatever nice that we get 10 values
                this.dataString += '0';
            }
            this.dataString += dataPacket.toString(32);
        }
    }
    // and this rounds out the data string
    return this.dataString;
};

ScoreState.prototype.extractValueToColon = function() {
    var value = 0;
    var colonIndex = this.dataString.indexOf(':');
    if (colonIndex > -1) {
        // extract this data as a string
        var extracted = this.extractChars(colonIndex);
        // and remove the colon
        this.dataString = this.dataString.slice(1, this.dataString.length);
        // return the data as an integer
        return parseInt(extracted);
    } else {
        return 0;
    }
};

ScoreState.prototype.extractHistoryValue = function() {
    // get the string as a double char value
    var hexString = this.extractChars(2);
    return parseInt(hexString, 32);
};

ScoreState.prototype.extractChars = function(charsLength) {
    var extracted = "";
    if (this.dataString.length >= charsLength) {
        extracted = this.dataString.substring(0, charsLength);
    }
    this.dataString = this.dataString.slice(charsLength, this.dataString.length);
    return extracted;
};