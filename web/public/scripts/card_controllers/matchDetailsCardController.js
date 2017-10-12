'use strict';

// Initializes the display of the match details
function MatchDetailsCardController(parentDiv) {
    // initialise this class
    this.document = document;
    this.parentDiv = parentDiv;
    // and load the data we require
    this.umpyrData = window.umpyr.umpyrData;
}

MatchDetailsCardController.prototype.displayMatch = function(match) {
    // remember the match we are showing data for
    this.match = match;
    // and display its details
    this.displayMatchDetails();
    // and scroll to the card
    scrollTo(this.parentDiv.querySelector('#matchDetailsCard', 0, 100));

    // listen for any changes to data, if this match changes we want to update ourselves
    this.umpyrData.addDataListener(this, this.onDataChanged);
};

MatchDetailsCardController.prototype.close = function(isLocalClose) {
    // called as this card is removed and closed
    this.umpyrData.removeDataListener(this);
};

MatchDetailsCardController.prototype.onDataChanged = function(dataList, data, reason) {
    // called as the data in the list changes, is it for this card to listen?
    if (data == this.match) {
        // this is a change to the match we are showing, update it's display
        if (reason === 'added' || reason === 'changed') {
            // this is a change in the match we are showing match, add it
            this.displayMatch(data);
        } else if (reason === 'removed') {
            // the match we are showing is removed - do something about this here?
            window.umpyr.hideCard(10);
        }
    }
};

MatchDetailsCardController.prototype.displayMatchDetails = function() {
    var tableDiv = this.parentDiv.querySelector('#match-details-table');
    // setup the contents of the table now - starting with the title
    var sport = SportFromMode(this.match.getScoreMode());
    this.parentDiv.querySelector('#match-detils-title').innerHTML = sport.title;
    // set the correct titles for the sets and games they were playing
    this.parentDiv.querySelector("#match-details-sets-title").innerText = sport.scoringTitles[0] + 's';
    this.parentDiv.querySelector("#match-details-games-title").innerText = sport.scoringTitles[1] + 's';
    // and handle the delete button
    var thisData = this.umpyrData.matches;
    var thisMatch = this.match;
    tableDiv.querySelector('.match-delete-button ').addEventListener(
        'click',
        function() {
            // show the confirmation diaog
            mdlShowConfirmDialog('Delete Match', 'Are you sure you want to delete this match? You cannot get it back...',
                'Delete', 'Cancel',
                function() {
                    // delete the match from the data
                    thisData.deleteMatch(thisMatch);
                    // and close this card
                    window.umpyr.hideCard(10);
                });
        },
        false
    );
    this.setGameSummary(tableDiv);
    this.setGameStatistics(tableDiv);
    this.setMatchSummary(tableDiv);
    this.setMatchImage(this.parentDiv.querySelector('#matchDetailsCard'));
};

MatchDetailsCardController.prototype.setMatchImage = function(div) {
    var sport = SportFromMode(this.match.getScoreMode());
    // get the things to change
    var titleImage = div.querySelector('.mdl-card__title');
    var ccImage = div.querySelector('#matches-details-cc-image');

    // we want to set the image for this
    titleImage.className = "mdl-card__title " + sport.imageStyle;
    ccImage.setAttribute('href', sport.imageRef);
    ccImage.getElementsByTagName('img')[0].className = sport.attribImgClass;
    ccImage.querySelector('.mdl-tooltip').innerHTML = sport.imageAttrib;
};

MatchDetailsCardController.prototype.setGameSummary = function(div) {
    // add all the information
    div.querySelector('.match-information-player-one-title').textContent = this.match.getTeamTitle(0);
    div.querySelector('.match-information-player-two-title').textContent = this.match.getTeamTitle(1);
    // now the date
    var playedDate = this.match.getMatchPlayedDateFormatted();
    div.querySelector('.match-information-date').textContent = dateFormat(playedDate, "fullDate");
    // and the time
    div.querySelector('.match-information-time').textContent = playedDate.getHours() + ':' + ('0' + playedDate.getMinutes()).slice(-2);
    // now we can add the time to this string, hours and seconds
    var totalMinutes = ((this.match.state.secondsGameDuration / 60.0) | 0);
    var hours = ((totalMinutes / 60.0) | 0);
    var minutes = (totalMinutes - (hours * 60));
    div.querySelector('.match-information-for-time').textContent = hours + " hr " + minutes + " mn";
    // now the sets - if there are any...
    if (this.match.getSets(0) + this.match.getSets(1) > 0) {
        var playerOneSet = div.querySelector('.match-information-player-one-set');
        var playerTwoSet = div.querySelector('.match-information-player-two-set');
        // set the contents of the set results
        playerOneSet.textContent = this.match.getSets(0);
        playerTwoSet.textContent = this.match.getSets(1);
        // and BOLD the winner
        if (this.match.getSets(0) > this.match.getSets(1)) {
            // player one won
            playerOneSet.setAttribute('game-winner', 'true');
            playerTwoSet.removeAttribute('game-winner');
        } else if (this.match.getSets(0) < this.match.getSets(1)) {
            // player two won
            playerTwoSet.setAttribute('game-winner', 'true');
            playerOneSet.removeAttribute('game-winner');
        } else {
            // draw
            playerOneSet.removeAttribute('game-winner');
            playerTwoSet.removeAttribute('game-winner');
        }
    }
    // now the games
    for (var i = 0; i < 5; i += 1) {
        var teamOneGame = div.querySelector(".match-information-player-1-game" + (i + 1));
        var teamTwoGame = div.querySelector(".match-information-player-2-game" + (i + 1));
        if (i < this.match.state.setResults.length && this.match.state.setResults[i][0] + this.match.state.setResults[i][1] > 0) {
            // there are games for this
            teamOneGame.textContent = this.match.state.setResults[i][0];
            teamTwoGame.textContent = this.match.state.setResults[i][1];
            if (this.match.state.setResults[i][0] > this.match.state.setResults[i][1]) {
                // team one won that one
                teamOneGame.setAttribute('game-winner', 'true');
                teamTwoGame.removeAttribute('game-winner');
            } else if (this.match.state.setResults[i][0] < this.match.state.setResults[i][1]) {
                // team one lost that one
                teamTwoGame.setAttribute('game-winner', 'true');
                teamOneGame.removeAttribute('game-winner');
            } else {
                // draw
                teamOneGame.removeAttribute('game-winner');
                teamTwoGame.removeAttribute('game-winner');
            }
        } else {
            // clear it all, no data to show
            teamOneGame.textContent = "";
            teamTwoGame.textContent = "";
            teamOneGame.removeAttribute('game-winner');
            teamTwoGame.removeAttribute('game-winner');
        }
    }
};

MatchDetailsCardController.prototype.setGameStatistics = function(div) {
    // do the stats breakdown of the game here
    div.querySelector('#match-details-team-one-title').textContent = this.match.getTeamTitle(0);
    div.querySelector('#match-details-team-two-title').textContent = this.match.getTeamTitle(1);
    // do the player icons correctly also
    this.setPlayerIcon(div.querySelector('.player-one-pic'), this.match.getPlayerId(0));
    this.setPlayerIcon(div.querySelector('.player-two-pic'), this.match.getPlayerId(1));
    if (this.match.isDoubles) {
        // show the icons for their partners
        this.setPlayerIcon(div.querySelector('.player-one-partner-pic'), this.match.getPlayerId(2));
        this.setPlayerIcon(div.querySelector('.player-two-partner-pic'), this.match.getPlayerId(3));
    } else {
        this.setPlayerIcon(div.querySelector('.player-one-partner-pic'), null);
        this.setPlayerIcon(div.querySelector('.player-two-partner-pic'), null);
    }
    // now the date
    var playedDate = this.match.getMatchPlayedDateFormatted();
    div.querySelector('.match-information-date').textContent = dateFormat(playedDate, "fullDate");
    // and the time
    div.querySelector('.match-information-time').textContent = playedDate.getHours() + ':' + ('0' + playedDate.getMinutes()).slice(-2);
    // now we can add the time to this string, hours and seconds
    var totalMinutes = ((this.match.state.secondsGameDuration / 60.0) | 0);
    var hours = ((totalMinutes / 60.0) | 0);
    var minutes = (totalMinutes - (hours * 60));
    div.querySelector('.match-information-for-time').textContent = hours + " hr " + minutes + " mn";
    // get the sport they were playing
    var sport = SportFromMode(this.match.getScoreMode());
    //div.querySelector('.summary').textContent = sport.title;
    // set the correct titles for the sets and games they were playing
    div.querySelector("#match-details-sets-title").innerText = sport.scoringTitles[0] + 's';
    if (sport.scoringTitles[1] && sport.scoringTitles[1].length > 0) {
        div.querySelector("#match-details-games-title").innerText = sport.scoringTitles[1] + 's';
    } else {
        div.querySelector("#match-details-games-title").innerText = '';
    }
    // show the sets
    div.querySelector('#match-details-team-one-sets').textContent = this.match.getSets(0);
    div.querySelector('#match-details-team-two-sets').textContent = this.match.getSets(1);

    // now the set results
    var isSetResultRowPopulated = false;
    for (var i = 0; i < 5; i += 1) {
        if (i < this.match.state.setResults.length && this.match.state.setResults[i][0] + this.match.state.setResults[i][1] > 0) {
            // there are games for this
            var element = div.querySelector("#match-details-team-one-set-results-" + (i + 1));
            element.textContent = this.match.state.setResults[i][0];
            element.removeAttribute('hidden');
            // and for the other team
            element = div.querySelector("#match-details-team-two-set-results-" + (i + 1));
            element.textContent = this.match.state.setResults[i][1];
            element.removeAttribute('hidden');
            // remember we showed something
            isSetResultRowPopulated = true;
        } else {
            div.querySelector("#match-details-team-one-set-results-" + (i + 1)).setAttribute('hidden', true);
            div.querySelector("#match-details-team-two-set-results-" + (i + 1)).setAttribute('hidden', true);
        }
    }
    if (!isSetResultRowPopulated) {
        // there are no set results, hide the whole row
        div.querySelector('#match-details-set-results-row').setAttribute('hidden', true);
    } else {
        div.querySelector('#match-details-set-results-row').removeAttribute('hidden');
    }
    // and total points
    div.querySelector('#match-details-team-one-total-points').textContent = this.match.getTotalPoints(0);
    div.querySelector('#match-details-team-two-total-points').textContent = this.match.getTotalPoints(1);

    // at the bottom we can show the description
    var descriptionRow = div.querySelector('#match-details-description');
    var description = this.match.getDescription();
    if (description && description.length > 0) {
        descriptionRow.parentElement.removeAttribute('hidden');
        descriptionRow.innerHTML = description;
    } else {
        descriptionRow.parentElement.setAttribute('hidden', 'true');
    }

}

MatchDetailsCardController.prototype.setMatchSummary = function(div) {
    var matchSummary;
    var matchWinner = this.match.getMatchWinner();
    var matchLoser = this.match.getOtherPlayer(matchWinner);
    // get the names of those who played
    var teamOneTitle = this.match.getTeamTitle(0);
    var teamTwoTitle = this.match.getTeamTitle(1);
    switch (matchWinner) {
        case 0:
            matchSummary = teamOneTitle + " beat " + teamTwoTitle;
            break;
        case 1:
            matchSummary = teamTwoTitle + " beat " + teamOneTitle;
            break;
        default:
            // there is no winner, to match winner will be '9' - make the winner / loser correct in the right order
            matchWinner = 0;
            matchLoser = 1;
            matchSummary = teamOneTitle + " vs " + teamTwoTitle;
            break;
    }
    // now we want to summarise the match based on what was played
    matchSummary += this.match.getMatchSummary(matchWinner, matchLoser);
    // set the summary of the game
    div.querySelector('.summary').innerHTML = matchSummary;
};

MatchDetailsCardController.prototype.setPlayerIcon = function(element, playerId) {
    // set the icon to the icon of the player that played
    if (playerId !== null) {
        // want to show the icon, so make sure it isn't hidden
        element.removeAttribute('hidden');
        // there is an ID, we can set this icon
        var icon = window.umpyr.umpyrData.getUserIcon(playerId);
        if (icon) {
            element.src = icon;
        } else {
            element.src = 'images/ic_person_black_24px.svg';
        }
    } else {
        // hide the icon
        element.setAttribute('hidden', true);
    }
};