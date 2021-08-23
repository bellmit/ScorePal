'use strict';

// Initializes the matches display
function MatchesCardController(parentDiv, isShowMatchesSubmitted) {
    // initialise this class
    this.document = document;
    this.parentDiv = parentDiv;
    this.isShowMatchesSubmitted = isShowMatchesSubmitted;
    // and load the data we require
    this.umpyrData = window.umpyr.umpyrData;

    if (this.isShowMatchesSubmitted) {
        this.parentDiv.querySelector('.mdl-card__title-text').innerText = "Matches Entered from another player";
        this.parentDiv.querySelector('#matches-card-submitted-buttons').removeAttribute('hidden');
        // and handle the <accept> button to move this match from submitted to the list of matches played
        var thisController = this;
        this.parentDiv.querySelector('#matches-card-submitted-accept-button').addEventListener(
            'click',
            function(e) {
                // hand the user accepting this here
                var matches = thisController.umpyrData.matches.getMatchesSubmitted();
                // accept all the matches in the list
                for (var i = 0; i < matches.length; i += 1) {
                    thisController.umpyrData.matches.acceptSubmittedMatch(matches[i]);
                }
            },
            false
        );
        // and the <reject all> button
        this.parentDiv.querySelector('#matches-card-submitted-reject-button').addEventListener(
            'click',
            function(e) {
                // hand the user rejecting this here
                var matches = thisController.umpyrData.matches.getMatchesSubmitted();
                // reject all the matches in the list
                for (var i = 0; i < matches.length; i += 1) {
                    thisController.umpyrData.matches.deleteMatchSubmitted(matches[i]);
                }
            },
            false
        );
    } else {
        this.parentDiv.querySelector('.mdl-card__title-text').innerText = "Recent Matches Played";
        this.parentDiv.querySelector('#matches-card-submitted-buttons').setAttribute('hidden', 'true');
    }

    // now that the card is setup, get the data we want do display, listen for changes
    this.umpyrData.addDataListener(this, this.onDataChanged);
    // listening for changes now, but some probably already in the list
    this.displayMatches();
    // and update the display of this card now it is changed
    this.updateCardStatus();
}

MatchesCardController.prototype.close = function(isLocalClose) {
    // called as this card is removed and closed
    this.umpyrData.removeDataListener(this);
};

MatchesCardController.prototype.onDataChanged = function(dataList, data, reason) {
    // called as the data in the list changes, is it for this card to listen?
    if ((!this.isShowMatchesSubmitted && dataList == this.umpyrData.matches.getMatches()) ||
        (this.isShowMatchesSubmitted && dataList == this.umpyrData.matches.getMatchesSubmitted())) {
        // this is the correct matches list changed, this is for us
        if (reason === 'added' || reason === 'changed') {
            // this is a new match, add it
            this.displayMatch(data);
        } else if (reason === 'removed') {
            // remove the div that represents the match from the list
            var div = this.parentDiv.querySelector('#fb' + data.getMatchId());
            if (div && div.parentElement) {
                fadeElement(div, false, function() {
                    // when completed, remove the child completely
                    div.parentElement.removeChild(div);
                });
            }
        }
        // update the display of this card now it is changed
        this.updateCardStatus();
    }
};

MatchesCardController.prototype.updateCardStatus = function() {
    if (this.isShowMatchesSubmitted) {
        // this card will show and hide when empty / populated. Check the data now
        if (this.umpyrData.matches.getMatchesSubmitted().length > 0) {
            // ensure this card is shown
            this.parentDiv.removeAttribute('hidden');
        } else {
            this.parentDiv.setAttribute('hidden', 'true');
        }
    }
};

MatchesCardController.prototype.displayMatches = function() {
    // display all the matches that are in the data at this time
    var matches;
    if (this.isShowMatchesSubmitted) {
        // show the matches submitted by another player
        matches = this.umpyrData.matches.getMatchesSubmitted();
    } else {
        // show our matches
        matches = this.umpyrData.matches.getMatches();
    }
    for (var i = 0; i < matches.length; i += 1) {
        this.displayMatch(matches[i]);
    }
};

// Displays a match in the UI.
MatchesCardController.prototype.displayMatch = function(match) {
    // get the data that we want to be diplayed
    // check that the matches display card is shown
    var matchesList = this.parentDiv.querySelector('#matches-card-list');
    // now see if the match we have to display is a child of this card
    var div = this.parentDiv.querySelector('#fb' + match.getMatchId());
    // If an element for that match does not exists yet we create it.
    if (!div) {
        // the match div doesn't exist, create it
        var container = this.document.createElement('div');
        if (!this.matchRowContent) {
            // load the raw content from the html file that we want to use
            this.matchRowContent = clientSideInclude('/components/matchrow.html');
        }
        // set the content of this row
        container.innerHTML = this.matchRowContent;
        div = container.firstChild;
        div.setAttribute('id', 'fb' + match.getMatchId());
        div.setAttribute('playedDate', match.getMatchPlayedDate());
        // find the correct position for this data, based on it's matchPlayedDate which can be sorted alphabetically
        var isInserted = false;
        for (var i = 0; i < matchesList.childNodes.length; i += 1) {
            var child = matchesList.childNodes[i];
            if (child && child.getAttribute) {
                var childDate = matchesList.childNodes[i].getAttribute('playedDate');
                if (match.matchPlayedDate >= childDate) {
                    // received date goes before here
                    matchesList.insertBefore(div, matchesList.childNodes[i]);
                    isInserted = true;
                    break;
                }
            }
        }
        if (!isInserted) {
            // goes nowhere in the list (or list is empty), put it at the bottom
            matchesList.appendChild(div);
        }
    }
    // show the summary of the game, create the scorer to get it's title
    var sport = SportFromMode(match.getScoreMode());
    div.querySelector('.summary').textContent = sport.title;
    // and the icon for this sport
    div.querySelector('#match-sport-icon').src = './images/' + sport.title + '.png';
    // set the correct titles for the sets and games they were playing
    div.querySelector("#match-details-sets-title").innerText = sport.scoringTitles[0] + 's';
    if (sport.scoringTitles[1] && sport.scoringTitles[1].length > 0) {
        div.querySelector("#match-details-games-title").innerText = sport.scoringTitles[1] + 's';
    } else {
        div.querySelector("#match-details-games-title").innerText = '';
    }
    // show the player's names
    div.querySelector('.match-summary').innerHTML = this.getMatchSummary(match, div);
    // setup the expansion click
    var thisMatchesCardController = this;
    mdlHandleExpand(div, function() { thisMatchesCardController.onClickExpandMatch(div); });
    // and handle the delete button
    var deleteButton = div.querySelector('.match-delete-button ');
    if (!this.isShowMatchesSubmitted) {
        deleteButton.addEventListener(
            'click',
            function() {
                // show the confirmation diaog
                mdlShowConfirmDialog('Delete Match', 'Are you sure you want to delete this match? You cannot get it back...',
                    'Delete', 'Cancel',
                    function() {
                        // delete the match from the data
                        window.umpyr.umpyrData.matches.deleteMatch(match);
                    });
            },
            false
        );
        deleteButton.removeAttribute('hidden');
    } else if (this.isShowMatchesSubmitted) {
        // hide the delete button - reject instead does the job
        deleteButton.setAttribute('hidden', 'true');
        // handle the <accept> button
        var acceptButton = div.querySelector('.match-submitted-accept-button');
        acceptButton.removeAttribute('hidden');
        acceptButton.addEventListener(
            'click',
            function() {
                thisMatchesCardController.umpyrData.matches.acceptSubmittedMatch(match);
            },
            false
        );
        // handle the <reject> button
        var rejectButton = div.querySelector('.match-submitted-reject-button');
        rejectButton.removeAttribute('hidden');
        rejectButton.addEventListener(
            'click',
            function() {
                thisMatchesCardController.umpyrData.matches.deleteMatchSubmitted(match);
            },
            false
        );
    }
    // handle the <more details> button
    div.querySelector('.match-information-details-button').addEventListener(
        'click',
        function() {
            window.umpyr.displayMatchDetails(match);
        },
        false
    );
    // add all the expanded information, first the teams's titles
    div.querySelector('.match-information-player-one-title').innerHTML = match.getTeamTitle(0);
    div.querySelector('.match-information-player-two-title').innerHTML = match.getTeamTitle(1);
    // now the date
    var playedDate = match.getMatchPlayedDateFormatted();
    div.querySelector('.match-information-date').textContent = dateFormat(playedDate, "fullDate");
    // and the time
    div.querySelector('.match-information-time').textContent = playedDate.getHours() + ':' + ('0' + playedDate.getMinutes()).slice(-2);
    // now we can add the time to this string, hours and seconds
    var totalMinutes = ((match.state.secondsGameDuration / 60.0) | 0);
    var hours = ((totalMinutes / 60.0) | 0);
    var minutes = (totalMinutes - (hours * 60));
    div.querySelector('.match-information-for-time').textContent = hours + " hr " + minutes + " mn";
    // now the sets - if there are any...
    if (match.getSets(0) + match.getSets(1) > 0) {
        var playerOneSet = div.querySelector('.match-information-player-one-set');
        var playerTwoSet = div.querySelector('.match-information-player-two-set');
        // set the contents of the set results
        playerOneSet.textContent = match.getSets(0);
        playerTwoSet.textContent = match.getSets(1);
        // and BOLD the winner
        if (match.getSets(0) > match.getSets(1)) {
            // player one won
            playerOneSet.setAttribute('game-winner', 'true');
            playerTwoSet.removeAttribute('game-winner');
        } else if (match.getSets(0) < match.getSets(1)) {
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
        if (i < match.state.setResults.length && match.state.setResults[i][0] + match.state.setResults[i][1] > 0) {
            // there are games for this
            teamOneGame.textContent = match.state.setResults[i][0];
            teamTwoGame.textContent = match.state.setResults[i][1];
            if (match.state.setResults[i][0] > match.state.setResults[i][1]) {
                // team one won that one
                teamOneGame.setAttribute('game-winner', 'true');
                teamTwoGame.removeAttribute('game-winner');
            } else if (match.state.setResults[i][0] < match.state.setResults[i][1]) {
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
    // show the contents fading in
    fadeElement(div, true);
    // scroll to the top to show this card
    //this.matchesDisplayCard.scrollTop = this.matchesDisplayCard.scrollHeight;
};

MatchesCardController.prototype.getMatchSummary = function(match, div) {
    var matchSummary;
    var matchWinner = match.getMatchWinner();
    var matchLoser = match.getOtherPlayer(matchWinner);
    // get the names of those who played
    var teamOneTitle = match.getTeamTitle(0);
    var teamTwoTitle = match.getTeamTitle(1);
    switch (matchWinner) {
        case 0:
            matchSummary = teamOneTitle + " beat " + teamTwoTitle;
            // the first player is the winner, show the image for the player one first and the second second
            this.setPlayerIcon(div.querySelector('.player-one-pic'), match.getPlayerId(0));
            this.setPlayerIcon(div.querySelector('.player-two-pic'), match.getPlayerId(1));
            if (match.isDoubles) {
                // show the icons for their partners
                this.setPlayerIcon(div.querySelector('.player-one-partner-pic'), match.getPlayerId(2));
                this.setPlayerIcon(div.querySelector('.player-two-partner-pic'), match.getPlayerId(3));
            }
            break;
        case 1:
            matchSummary = teamTwoTitle + " beat " + teamOneTitle;
            // the second player is the winner, show the image for the player two first and the first second
            this.setPlayerIcon(div.querySelector('.player-one-pic'), match.getPlayerId(1));
            this.setPlayerIcon(div.querySelector('.player-two-pic'), match.getPlayerId(0));
            if (match.isDoubles) {
                // show the icons for their partners
                this.setPlayerIcon(div.querySelector('.player-one-partner-pic'), match.getPlayerId(3));
                this.setPlayerIcon(div.querySelector('.player-two-partner-pic'), match.getPlayerId(2));
            }
            break;
        default:
            // there is no winner, to match winner will be '9' - make the winner / loser correct in the right order
            matchWinner = 0;
            matchLoser = 1;
            matchSummary = teamOneTitle + " vs " + teamTwoTitle;
            // we are showing player one vs player two, set the icons accordingly
            this.setPlayerIcon(div.querySelector('.player-one-pic'), match.getPlayerId(0));
            this.setPlayerIcon(div.querySelector('.player-two-pic'), match.getPlayerId(1));
            if (match.isDoubles) {
                // show the icons for their partners
                this.setPlayerIcon(div.querySelector('.player-one-partner-pic'), match.getPlayerId(2));
                this.setPlayerIcon(div.querySelector('.player-two-partner-pic'), match.getPlayerId(3));
            }
            break;
    }
    // add the date to the summary text
    matchSummary += " on " + dateFormat(match.getMatchPlayedDateFormatted(), 'mediumDate');
    // now we want to summarise the match based on what was played
    matchSummary += match.getMatchSummary(matchWinner, matchLoser);
    // return the summary of the game
    return matchSummary;
};

MatchesCardController.prototype.setPlayerIcon = function(element, playerId) {
    // want to show the icon, so make sure it isn't hidden
    element.removeAttribute('hidden');
    // set the icon to the icon of the player that played
    if (playerId) {
        // there is an ID, we can set this icon
        var icon = window.umpyr.umpyrData.getUserIcon(playerId);
        if (icon) {
            element.src = icon;
        }
    }
};

MatchesCardController.prototype.onClickExpandMatch = function(div) {
    var expansionArea = div.querySelector('.expansion-area');
    if (!expansionArea.classList.contains('visible')) {
        // isn't hidden, show it by expanding
        expansionArea.removeAttribute('hidden');
        // show the contents fading in
        fadeElement(expansionArea, true);
    } else {
        // show the contents fading out
        fadeElement(expansionArea, false, function() {
            // when completed, make it acually hidden
            expansionArea.setAttribute('hidden', 'true');
        });
    }
};