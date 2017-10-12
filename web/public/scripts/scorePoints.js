'use strict';

// Initializes the tennis scoring code object
function ScorePoints(numberGames, pointsToWin, pointsAhead, pointsToWinOutright) {
    // this is to extend the Score object, so get all the functionality from that now please
    this.match = new Match();
    // setup the members that track the current state, we are using the sets to record the results
    // of the games so set these to be the number of games
    this.numberSets = numberGames;
    // and now the members to do the scoring correctly
    this.numberGames = numberGames;
    this.pointsToWin = pointsToWin;
    this.pointsAhead = pointsAhead;
    this.pointsToWinOutright = pointsToWinOutright;
}

ScorePoints.prototype.updateScoreCalculations = function() {
    // here we can calculate any data from the score the user may have entered
    if (this.numberGames > 0) {
        // we are tracking games; calculate these from the points
        this.match.calculateSetsFromGames();
        // the results for each game are stored in the set results, add these to to the total points
        var totalPoints = [0, 0];
        for (var i = 0; i < this.match.state.setResults.length; i += 1) {
            // add the points on each game (in setResults)
            totalPoints[0] += this.match.state.setResults[i][0];
            totalPoints[1] += this.match.state.setResults[i][1];
        }
        // and set this result in the match
        this.match.setTotalPoints(0, totalPoints[0]);
        this.match.setTotalPoints(1, totalPoints[1]);
    } else { //if (this.match.state.sets[0] + this.match.state.sets[1] > 0) {
        // the points are the sets themselves
        this.match.setTotalPoints(0, this.match.getSets(0));
        this.match.setTotalPoints(1, this.match.getSets(1));
    }
};

ScorePoints.prototype.isMatchOver = function() {
    var isMatchOver = false;
    if (this.numberGames > 0) {
        // have they won the right number of games?
        var gamesToWin = this.numberGames / 2.0;
        // return if someone has exceeded the number of games required to win
        isMatchOver = (this.match.getGames(0) * 1.0 > gamesToWin) || (this.match.getGames(1) * 1.0 > gamesToWin);
    } else if (this.pointsToWin !== null) {
        // there are a number of points required to win, has someone reached that?
        if (this.match.getPoints(0) >= this.pointsToWin ||
            this.match.getPoints(1) >= this.pointsToWin) {
            // someone has won
            if (this.pointsAhead !== null) {
                // but they have to be a certain amount ahead
                if (Math.abs(this.match.getPoints(0) - this.match.getPoints(1)) >= this.pointsToWin) {
                    // far enough ahead
                    isMatchOver = true;
                }
            } else {
                // got the points - done.
                isMatchOver = true;
            }
        }
    }
    //else just points, never over

    // if we are done, record the winner now
    if (!isMatchOver) {
        // the match is not over, store this on the state
        this.match.recordMatchWinner();
    } else {
        // the match is over, record the winner
        this.match.recordMatchWinner(this.match.getMatchWinner());
    }
    return isMatchOver;
};

ScorePoints.prototype.getPoints = function(player) {
    // return the points to show for the player
    return this.match.getPoints(player);
};

ScorePoints.prototype.addPoint = function(player) {
    // add a point for the player correctly for this match
    this.match.addPoint(player);
    // for this we simply add a point to the state
    this.match.state.points[player] += 1;
    // now do the correct operation for our current mode - games or no games...
    if (!this.numberGames) {
        // this is just scoring points - limit the points they show to double figures
        // and balance a trouncing a little bit
        this.addPointPointsOnly(player);
    } else {
        // there are no sets, just doing games, check to see if we have won a game here then.
        var playerPoints = this.match.getPoints(player);
        var opponent = this.match.getOtherPlayer(player);
        var opponentPoints = this.match.getPoints(opponent);
        var isGameWon = false;
        if (this.pointsToWinOutright && playerPoints >= this.pointsToWinOutright) {
            // have specified a number of points to win a game outright, and we have that now, won the game
            isGameWon = true;
        }
        if (this.pointsToWin && playerPoints >= this.pointsToWin) {
            // have specified a number of points to win a game and we have exceeded that now
            if (!this.pointsAhead || playerPoints - opponentPoints >= this.pointsAhead) {
                // either havn't specified number of points they have to be ahead, or have excceded them, won the game
                isGameWon = true;
            }
        }
        // now check to see if they have won
        if (isGameWon) {
            // the player with the latest point just won the game
            this.match.state.games[player] += 1;
            // and record the result of this game as a set - so we keep the results of each game really...
            this.match.recordSetResult(this.match.state.points[0], this.match.state.points[1], undefined, true);
            // and reset the points on the match - start the next game
            this.match.resetPoints();
        }
    }
};

ScorePoints.prototype.addPointPointsOnly = function(player) {
    // limit the points won to something sensible here
    var playerOnePoints = this.match.getPoints(0);
    var playerTwoPoints = this.match.getPoints(1);
    var pointSum = playerOnePoints + playerTwoPoints;
    if (pointSum > 0 && pointSum % 5 === 0) {
        // change the server
        this.match.swapServer();
    }

    if (this.match.getPoints(0) > 99 || playerTwoPoints > 99) {
        // gone over the edge of Points, reset a little
        var toRemove;
        if (playerOnePoints < playerTwoPoints) {
            toRemove = playerOnePoints;
        } else {
            toRemove = playerTwoPoints;
        }
        playerOnePoints -= toRemove;
        playerTwoPoints -= toRemove;
        // this might not be enough if they have a huge advantage - so disadvantage them a little
        if (playerOnePoints > 90) {
            playerOnePoints = 50;
        }
        if (playerTwoPoints > 90) {
            playerTwoPoints = 50;
        }
        // and set these results back on the match
        this.match.setPoints(0, playerOnePoints);
        this.match.setPoints(1, playerTwoPoints);
    }

    // we change ends after five Points, then every ten Points
    if (pointSum > 0 && (pointSum + 5) % 10 === 0) {
        // change ends then
        this.match.swapEnds();
    }
};

ScorePoints.prototype.checkCurrentScore = function() {
    //TODO check the score makes sense
    return true;
};