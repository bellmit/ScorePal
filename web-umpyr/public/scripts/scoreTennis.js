'use strict';

// Initializes the tennis scoring code object
function ScoreTennis(numberSets) {
    // this is to extend the Score object, so get all the functionality from that now please
    this.match = new Match();
    // setup the members that track the current state
    this.numberSets = numberSets;
    // there are 6 games per set which is set - just important not to be zero really
    this.numberGames = 6;
    // set and the live score entry will show the games result for the current set in the entry box
    this.isShowGamesInSetResult = true;
    // create the static const to use to represent 40 points to make the logic easier
    this.K_POINTS40 = 3;
}

ScoreTennis.prototype.updateScoreCalculations = function() {
    // here we can calculate the set results from the entered games in the score entry system
    this.match.calculateSetsFromGames();
    // also we can have a guess at the total points that the players made to get each game
    var totalPoints = [0, 0];
    for (var i = 0; i < this.match.state.setResults.length; i += 1) {
        // it takes at least 4 points to win a game, so 4 points per game please
        totalPoints[0] += this.match.state.setResults[i][0] * 4;
        totalPoints[1] += this.match.state.setResults[i][1] * 4;
    }
    // and set this result in the match
    this.match.setTotalPoints(0, totalPoints[0]);
    this.match.setTotalPoints(1, totalPoints[1]);
};

ScoreTennis.prototype.isMatchOver = function() {
    var setsToWin = this.numberSets / 2.0;
    // return if someone has exceeded the number of sets required to win
    var isMatchOver = (this.match.getSets(0) * 1.0 > setsToWin) || (this.match.getSets(1) * 1.0 > setsToWin);
    if (!isMatchOver) {
        // the match is not over, store this on the state
        this.match.recordMatchWinner();
    } else {
        // the match is over, record the winner
        this.match.recordMatchWinner(this.match.getMatchWinner());
    }
    return isMatchOver;
};

ScoreTennis.prototype.getPoints = function(player) {
    // return the points to show for the player
    if (this.match.state.isTieBreak) {
        // in a tie - return the number
        return this.match.getPoints(player);
    } else {
        // return 15-40 etc
        switch (this.match.getPoints(player)) {
            case 0:
                return '00';
            case 1:
                return '15';
            case 2:
                return '30';
            case 3:
                return '40';
            case 4:
                return 'ad';
            default:
                // return the points as a number
                return this.match.getPoints(player);
        }
    }
}

ScoreTennis.prototype.addPoint = function(player) {
    // add a point for the player correctly for this tennis match
    this.match.addPoint(player);
    // do the complicated tennis stuff here then now the base has added the point
    var otherPlayer = this.match.getOtherPlayer(player);

    // check to see if we are in a tie-breaker
    if (false === this.match.state.isTieBreak) {
        // now we do our thing, normall stuff here
        if (this.match.state.points[player] < this.K_POINTS40) {
            // have we 0, 15, or 30, just add
            this.match.state.points[player] += 1;
        } else if (this.match.state.points[player] === this.K_POINTS40) {
            // we are on 40
            if (this.match.state.points[otherPlayer] === this.K_POINTS40) {
                // they are on 40 too, advantage to the player with the Point
                this.match.state.points[player] += 1;
            } else if (this.match.state.points[otherPlayer] > this.K_POINTS40) {
                // they are on ad, remove theirs
                this.match.state.points[otherPlayer] -= 1;
            } else {
                // we won the game
                this.addGame(player);
            }
        } else {
            // just won advantage, player wins this game
            this.addGame(player);
        }
    } else {
        // do the tie-breaker scoring, add the Point first
        this.match.state.points[player] += 1;
        // now check to see if they have won
        if (this.match.state.points[player] > 6 && this.match.state.points[player] - this.match.state.points[otherPlayer] > 1) {
            // they won the tie-break as they have 7 Points at least and are 2 ahead of the other, so they won the tie
            this.addGame(player);
        } else {
            // we need to swap servers every two Points, except the first
            var pointSum = this.match.state.points[player] + this.match.state.points[otherPlayer];
            if ((pointSum + 1) % 2 === 0) {
                // swap servers
                this.match.swapServer();
            }
            // also we want to swap ends every six points in a tie-break
            if (pointSum % 6 === 0) {
                // swap ends
                this.match.swapEnds();
            }
        }
    }
};

ScoreTennis.prototype.addGame = function(player) {
    // add the game to our match
    var otherPlayer = this.match.getOtherPlayer(player);
    // just add to the game counter
    this.match.state.games[player] += 1;
    var isSetWon = false;
    if (this.match.state.isTieBreak) {
        // might be 6-6 or whatever, but either way when you win a tie you win the set
        this.match.recordSetResult(this.match.state.games[0], this.match.state.games[1], undefined, true);
        isSetWon = true;
        // not in a tie break now!
        this.match.state.isTieBreak = false;
        // make the current server the server that started the tie, that way we will swap now to the other
        this.match.state.currentServer = this.match.state.tieBreakServer;
    } else if (this.match.state.games[player] > 5) {
        // player has six games, check to see if they have won the set
        if (this.match.state.games[player] - this.match.state.games[otherPlayer] > 1) {
            // the player is now two games ahead having won at least 6, they just won the set
            this.match.recordSetResult(this.match.state.games[0], this.match.state.games[1], undefined, true);
            isSetWon = true;
        } else {
            if (this.match.state.games[otherPlayer] === 6) {
                // the other player has six too, this is a tie-breaker as can't get too far ahead
                if (this.match.getNumberSetsPlayed() < this.numberSets - 1) {
                    // this is a tie-breaker as we are not in the final set
                    this.match.state.isTieBreak = true;
                    // and remember the server, as the other as about to swap
                    this.match.state.tieBreakServer = this.match.getOtherPlayer(this.match.state.currentServer);
                }
            }
        }
    }
    if (false === isSetWon) {
        // set not over - if we are on the first, third fifth, seventh game then we need to swap ends here
        // change ends every two games, except the first
        var gameSum = this.match.state.games[player] + this.match.state.games[otherPlayer];
        // change ends on the first game, then every two (when Points are zero, ie the start)
        if ((gameSum + 1) % 2 === 0) {
            // we are on an odd numbered game in the set, swap ends
            this.match.swapEnds();
        }
    } else {
        // the set is won, reset games
        this.match.resetGames();
    }
    // game over - reset our Points for the next game
    this.match.resetPoints();
    // swap servers every time we play a new game
    this.match.swapServer();
};

ScoreTennis.prototype.checkCurrentScore = function() {
    //TODO check the score makes sense
    return true;
};