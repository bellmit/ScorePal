'use strict';

/*
#define K_SCOREWIMBLEDON5	1
#define K_SCOREWIMBLEDON3	2
#define K_SCOREBADMINTON3	3
#define K_SCOREBADMINTON5	4
#define K_SCOREPOINTS		5
#define K_SCOREFAST4		6
*/

// Initializes the score entry
function ScoreEntryCardController(parentDiv) {
    // initialise this class
    this.document = document;
    this.parentDiv = parentDiv;
    // and remember the very useful data controller object
    this.umpyrData = window.umpyr.umpyrData;

    this.signInSnackbar = this.parentDiv.querySelector('#must-signin-snackbar');
    this.scoreSelector = this.parentDiv.querySelector('#score-type');
    this.scoreEntryPastOrLive = this.parentDiv.querySelector('#score-entry-live');
    this.scoreEntryTitleImage = this.parentDiv.querySelector('#score-entry-title-image');
    this.scoreEntryCCImage = this.parentDiv.querySelector('#score-entry-image');
    this.scoreEntryPointsTitle = this.parentDiv.querySelector('#score-entry-points-title');
    this.scoreEntryPointsSubtitle = this.parentDiv.querySelector('#score-entry-points-subtitle');

    this.scoreEntrySetsOptions = this.parentDiv.querySelector('#score-entry-sets-options-row');

    this.scoreEntryWimbledon = this.parentDiv.querySelector('#score-entry-wimbledon');
    this.scoreEntryDoubles = this.parentDiv.querySelector('#score-entry-enter-doubles');
    this.scoreEntrySetsTitle = this.parentDiv.querySelector('#score-entry-sets-title');
    this.scoreEntryGamesTitle = this.parentDiv.querySelector('#score-entry-games-title');
    this.scoreEntryTimeSelectionPane = this.parentDiv.querySelector('#score-entry-time-selection');

    this.undoButton = this.parentDiv.querySelector('#score-entry-undo-button');
    this.resetButton = this.parentDiv.querySelector('#score-entry-reset-button');
    this.matchOver = this.parentDiv.querySelector('#score-entry-match-over-span');
    // points entry buttons
    this.playerOnePointsBtn = this.parentDiv.querySelector('#score-entry-player-one-points-button');
    this.playerTwoPointsBtn = this.parentDiv.querySelector('#score-entry-player-two-points-button');

    this.gamesEntryCheck = this.parentDiv.querySelector('#score-entry-enter-games');

    // get all the score elements
    this.elementTitles = [
        this.parentDiv.querySelector('#player-one-title'),
        this.parentDiv.querySelector('#player-two-title'),
        this.parentDiv.querySelector('#player-one-partner-title'),
        this.parentDiv.querySelector('#player-two-partner-title')
    ];
    // and the controls that allow the editing of the sets
    this.elementSets = [
        this.parentDiv.querySelector('#player-one-sets'),
        this.parentDiv.querySelector('#player-two-sets')
    ];
    // and the controls that allow the editing of the points
    this.elementPoints = [
        this.parentDiv.querySelector('#score-entry-player-one-points'),
        this.parentDiv.querySelector('#score-entry-player-two-points')
    ];
    // and the controls that allow the editing of the games for each set
    this.elementGames = [
        [
            this.parentDiv.querySelector('#player-one-game-one'),
            this.parentDiv.querySelector('#player-one-game-two'),
            this.parentDiv.querySelector('#player-one-game-three'),
            this.parentDiv.querySelector('#player-one-game-four'),
            this.parentDiv.querySelector('#player-one-game-five')
        ],
        [
            this.parentDiv.querySelector('#player-two-game-one'),
            this.parentDiv.querySelector('#player-two-game-two'),
            this.parentDiv.querySelector('#player-two-game-three'),
            this.parentDiv.querySelector('#player-two-game-four'),
            this.parentDiv.querySelector('#player-two-game-five')
        ]
    ];
    this.elementGamesHeadings = [
        this.parentDiv.querySelector('#score-entry-game-heading-1'),
        this.parentDiv.querySelector('#score-entry-game-heading-2'),
        this.parentDiv.querySelector('#score-entry-game-heading-3'),
        this.parentDiv.querySelector('#score-entry-game-heading-4'),
        this.parentDiv.querySelector('#score-entry-game-heading-5')
    ];
    this.elementSetOptions = [
        this.parentDiv.querySelector('#score-entry-set-option-1'),
        this.parentDiv.querySelector('#score-entry-set-option-2'),
        this.parentDiv.querySelector('#score-entry-set-option-3')
    ];

    this.populateScoreSelector();

    // ADD THIS FIRST TO CHANGE THE sport controller before anything else is done
    // add the onchange function to listen for the user selecting the type
    this.scoreSelector.addEventListener(
        'change',
        function() { thisScoreEntryCardController.onScoreTypeChanged(); },
        false
    );

    // in order to call to this inside another function, remember what this is
    var thisScoreEntryCardController = this;
    // handle the expand / collapse button
    mdlHandleExpand(this.scoreEntryWimbledon, function() { thisScoreEntryCardController.onClickExpand(); });
    // every child of this that can change should be listened to for change
    var textFields = this.scoreEntryWimbledon.getElementsByClassName('mdl-textfield__input');
    var changeFunction = function() { thisScoreEntryCardController.onTennisScoreChanged(); };
    for (var i = 0; i < textFields.length; i += 1) {
        textFields[i].addEventListener(
            'change',
            changeFunction,
            false
        );
    }
    changeFunction = function() { thisScoreEntryCardController.onSetsOptionChanged(); };
    for (var i = 0; i < this.elementSetOptions.length; i += 1) {
        this.elementSetOptions[i].addEventListener(
            'change',
            changeFunction,
            false
        );
    }
    // add the onchange function to listen for them selected live or in the past
    this.scoreEntryPastOrLive.addEventListener(
        'change',
        function() { thisScoreEntryCardController.onLivePastSelection(); },
        false
    );
    // add the onchange function to listen for the user selecting doubles / singles
    this.scoreEntryDoubles.addEventListener(
        'change',
        function() { thisScoreEntryCardController.onScorePersonsChanged(); },
        false
    );
    // intercept the message to get the user editing the 'played on' time to show the dialog
    var dateButton = this.parentDiv.querySelector('#score-entry-played-on-button');
    if (dateButton) {
        dateButton.addEventListener(
            'click',
            function() { thisScoreEntryCardController.onClickScoreEntryCardControllerPlayedOn(); },
            false
        );
    }
    this.parentDiv.querySelector('#score-entry-played-on').addEventListener(
        'click',
        function() { thisScoreEntryCardController.onClickScoreEntryCardControllerPlayedOn(); },
        false
    );
    // listen for the user clicking the submit button too
    this.parentDiv.querySelector('#score-entry-submit-button').addEventListener(
        'click',
        function() { thisScoreEntryCardController.onClickScoreSubmit(); },
        false
    );
    // and the cancel button
    this.parentDiv.querySelector('#score-entry-cancel-button').addEventListener(
        'click',
        function() { thisScoreEntryCardController.onClickScoreCancel(); },
        false
    );
    // and the points buttons
    this.playerOnePointsBtn.addEventListener(
        'click',
        function() { thisScoreEntryCardController.onClickPlayerScore(0); },
        false
    );
    this.playerTwoPointsBtn.addEventListener(
        'click',
        function() { thisScoreEntryCardController.onClickPlayerScore(1); },
        false
    );
    this.undoButton.addEventListener(
        'click',
        function() { thisScoreEntryCardController.onClickPlayerScore(-1); },
        false
    );
    this.resetButton.addEventListener(
        'click',
        function() { thisScoreEntryCardController.onClickPlayerScore(-2); },
        false
    );
    this.gamesEntryCheck.addEventListener(
        'change',
        function() { thisScoreEntryCardController.onGamesEntryChanged(); },
        false
    );

    var changeFunction = function() {
        // called when the user input changes
        thisScoreEntryCardController.onTennisScoreChanged();
    };
    // make the name edit boxes friend selectors
    umpyrMakeTextFieldFriendsSelector(this.elementTitles[0], changeFunction);
    umpyrMakeTextFieldFriendsSelector(this.elementTitles[1], changeFunction);
    umpyrMakeTextFieldFriendsSelector(this.elementTitles[2], changeFunction);
    umpyrMakeTextFieldFriendsSelector(this.elementTitles[3], changeFunction);

    // and call the changed function straight away to start it up
    this.onScoreTypeChanged();
    this.onGamesEntryChanged();
    this.onScorePersonsChanged();
    // call the update date function to display the current date
    this.updateDatePlayed();
}

ScoreEntryCardController.prototype.close = function(isLocalClose) {
    // called as this card closes...
};

ScoreEntryCardController.prototype.populateScoreSelector = function() {
    // put all the sports into the score selector, but let's have some order to it
    // first get the last match they played
    var sports = [];
    if (this.umpyrData.matches) {
        // matches are loaded, get the ones loaded recently
        var match = this.umpyrData.matches.getLatestMatch();
        if (match && SportTitleIsValid(match.sport)) {
            // the latest match can go at the top
            sports.push(match.sport);
        }
    }
    // now do the sports the user plays, adding them to the list
    var sportsPlayed = this.umpyrData.getUserSports();
    for (var i = 0; i < sportsPlayed.length; i += 1) {
        if (!isValInList(sportsPlayed[i].title, sports)) {
            // not in the list already, add
            sports.push(sportsPlayed[i].title);
        }
    }
    // now add anything else missing
    for (var i = 0; i < UmpyrSports.length; i += 1) {
        if (!isValInList(UmpyrSports[i].title, sports)) {
            // not in the list already, add
            sports.push(UmpyrSports[i].title);
        }
    }
    // for all the sports in the list, added in a nice order, create options for each one
    for (var i = 0; i < sports.length; i += 1) {
        var option = this.document.createElement('option');
        option.setAttribute('value', SportFromTitle(sports[i]).id);
        option.innerHTML = sports[i];
        // and add the child
        this.scoreSelector.appendChild(option);
    }
};

ScoreEntryCardController.prototype.onClickScoreEntryCardControllerPlayedOn = function() {
    // show the dialog and let the user select the date specifically
    // https://puranjayjain.github.io/md-date-time-picker/#about/examples
    if (!this.datePickerDialog) {
        this.datePickerDialog = new mdDateTimePicker.default({
            type: 'date'
        });
        this.datePickerDialog.trigger = this.parentDiv.querySelector('#score-entry-played-on');
        var pickerDialog = this.datePickerDialog;
        var thisCard = this;
        this.parentDiv.querySelector('#score-entry-played-on').addEventListener('onOk', function() {
            thisCard.sport.controller.match.setMatchPlayedDate(pickerDialog.time.toDate());
            thisCard.updateDatePlayed();
        });
    }
    this.datePickerDialog.show();
};

ScoreEntryCardController.prototype.onLivePastSelection = function() {
    if (!this.scoreEntryPastOrLive.checked) {
        // the match is live, hide the date selection and time played controls
        this.hideElement(this.scoreEntryTimeSelectionPane);
        if (this.gamesEntryCheck) {
            // there is a check box for entering games, hide this too
            this.hideElement(this.gamesEntryCheck.parentElement);
        }
        // we are playing from now then
        if (!this.liveStartTime) {
            this.liveStartTime = new Date();
        }
        // show the points headings
        this.showElement(this.scoreEntryPointsTitle);
        this.showElement(this.scoreEntryPointsSubtitle);
        this.showElement(this.elementPoints[0]);
        this.showElement(this.elementPoints[1]);
        this.showElement(this.undoButton);
        this.showElement(this.resetButton);
        // are entering live match, disable the games controls
        for (var i = 0; i < 5; i += 1) {
            this.disableElement(this.elementGames[0][i]);
            this.disableElement(this.elementGames[1][i]);
        }
        // and the set controls
        this.disableElement(this.elementSets[0]);
        this.disableElement(this.elementSets[1]);
    } else {
        // are entering a history match, show the time selection controls
        this.showElement(this.scoreEntryTimeSelectionPane);
        if (this.gamesEntryCheck) {
            // there is a check box for entering games, show this to let them enter the games
            //this.showElement(this.gamesEntryCheck.parentElement);
            // DECIDED That this was over-complicating things, always hide it
            this.hideElement(this.gamesEntryCheck.parentElement);
        }
        // delete the start time for the live match
        if (this.liveStartTime) {
            delete this.liveStartTime;;
        }
        // hide the points headings
        this.hideElement(this.scoreEntryPointsTitle);
        this.hideElement(this.scoreEntryPointsSubtitle);
        this.hideElement(this.elementPoints[0]);
        this.hideElement(this.elementPoints[1]);
        this.hideElement(this.undoButton);
        this.hideElement(this.resetButton);
        // are entering historic match, enable the games controls
        for (var i = 0; i < 5; i += 1) {
            this.enableElement(this.elementGames[0][i]);
            this.enableElement(this.elementGames[1][i]);
        }
    }
    // if live we are showing sets and games, make sure they are all shown
    this.onGamesEntryChanged();
};

ScoreEntryCardController.prototype.updateDatePlayed = function() {
    // set the date played on the entry card
    var dateElement = this.parentDiv.querySelector('#score-entry-played-on');
    var playedDate = this.sport.controller.match.getMatchPlayedDateFormatted();
    var dateString = dateFormat(playedDate, "fullDate");
    // set this date string into the entry
    mdlChange(dateElement, dateString);
    // also set the time the game was played
    var hoursElement = this.parentDiv.querySelector('#score-entry-time-hours-played');
    hoursElement.selectedIndex = playedDate.getHours();
    // we have a limited number of minutes, pick the one that doesn't go over
    var minsElement = this.parentDiv.querySelector('#score-entry-time-minutes-played');
    var minutes = Number(playedDate.getMinutes());
    minsElement.selectedIndex = 0;
    for (var i = 0; i < minsElement.options.length; i += 1) {
        if (Number(minsElement.options[i].value) < minutes) {
            // this is good
            minsElement.selectedIndex = i;
        } else {
            // this is too many, forget it
            break;
        }
    }
};

ScoreEntryCardController.prototype.onScorePersonsChanged = function() {
    // the number of people playing has changed
    this.sport.controller.match.isDoubles = this.scoreEntryDoubles.checked;
    if (this.sport.controller.match.isDoubles) {
        // are playing doubles, show the entry elements for their names
        this.showElement(this.elementTitles[2].parentElement);
        this.showElement(this.elementTitles[3].parentElement);
    } else {
        // hide the player name entries
        this.hideElement(this.elementTitles[2].parentElement);
        this.hideElement(this.elementTitles[3].parentElement);
    }
};

ScoreEntryCardController.prototype.onSetsOptionChanged = function(iClicked) {
    for (var i = 0; i < this.elementSetOptions.length; i += 1) {
        if (this.elementSetOptions[i].checked) {
            if (this.currentOptionSelected !== i + 1) {
                // this is a change
                this.currentOptionSelected = i + 1;
                // and update this page to this new data
                this.onScoreTypeChanged();
            }
            break;
        }
    }
};

ScoreEntryCardController.prototype.onScoreTypeChanged = function() {
    // get the current mode
    var oldScoreState;
    if (this.sport) {
        // there is a score mode, clear the data that was
        if (this.sport.controller) {
            oldScoreState = this.sport.controller.match.state;
            delete this.sport.controller;
            this.sport.controller = null;
        }
    }
    var scoreMode = this.scoreSelector.options[this.scoreSelector.selectedIndex].value;
    // and create the controller for this mode of operation, get the sport for the selected value
    this.sport = SportFromMode(scoreMode);
    if (this.sport) {
        if (!this.currentOptionSelected) {
            // there is no set option selected, select the middle one
            if (this.sport.setsOptions.length > 0) {
                this.currentOptionSelected = Math.floor(this.sport.setsOptions.length / 2.0) + 1;
            }
        }
        var setsOption = 0;
        if (this.currentOptionSelected) {
            // set the radio button to reflect this
            this.elementSetOptions[this.currentOptionSelected - 1].parentNode.MaterialRadio.check();
            // and get the value this represents
            setsOption = this.sport.setsOptions[this.currentOptionSelected - 1];
        }
        // create the controller, set it on the sport so it knows it has one created
        this.sport.controller = this.sport.createController(setsOption);
        // and set the score mode on the match it contains
        this.sport.controller.match.setScoreMode(this.sport);
        this.sport.controller.match.setSetsOption(setsOption);
        // set the current options on the match correctly
        this.sport.controller.match.isDoubles = this.scoreEntryDoubles.checked;
        this.onTennisScoreChanged();
    }
    if (oldScoreState && oldScoreState.historicPoints) {
        // there is an old one, replay the sore into the new one
        for (var i = 0; i < oldScoreState.historicPoints.length; i += 1) {
            // for each of the historic points in the old state, add a point to the new controller
            this.sport.controller.addPoint(oldScoreState.historicPoints[i]);
        }
    }
    // we want to set the image for this
    this.scoreEntryCCImage.setAttribute('href', this.sport.imageRef);
    this.scoreEntryCCImage.getElementsByTagName('img')[0].className = this.sport.attribImgClass;
    this.scoreEntryCCImage.querySelector('.mdl-tooltip').innerHTML = this.sport.imageAttrib;
    this.scoreEntryTitleImage.className = "mdl-card__title " + this.sport.imageStyle;
    // set up the titles for the score card for the selected sport
    if (this.sport.scoringTitles[0]) {
        this.scoreEntrySetsTitle.innerText = this.sport.scoringTitles[0] + 's';
        this.scoreEntrySetsTitle.removeAttribute('hidden');
    } else {
        this.scoreEntrySetsTitle.setAttribute('hidden', true);
    }
    if (this.sport.scoringTitles[1]) {
        this.scoreEntryGamesTitle.innerText = this.sport.scoringTitles[1] + 's';
        this.scoreEntryGamesTitle.removeAttribute('hidden');
    } else {
        this.scoreEntryGamesTitle.setAttribute('hidden', true);
    }
    if (this.sport.scoringTitles[3]) {
        this.scoreEntryPointsTitle.innerText = this.sport.scoringTitles[3] + 's';
    } else {
        this.scoreEntryPointsTitle.innerText = '';
    }

    // also we want to show any options available to us
    if (this.sport.setsOptions && this.sport.setsOptions.length > 0) {
        // show the options...
        this.showElement(this.scoreEntrySetsOptions);
        // and set the options that are available to us
        for (var i = 0; i < this.sport.setsOptions.length; i += 1) {
            // set the title for this option
            this.scoreEntrySetsOptions.querySelector('#score-entry-set-option-label-' + (i + 1)).innerText =
                '' + this.sport.setsOptions[i] + ' ' + this.sport.scoringTitles[0] + 's';
            // and set the value on the element
            this.elementSetOptions[i].value = this.sport.setsOptions[i];
        }
    } else {
        // there are no options to show
        this.hideElement(this.scoreEntrySetsOptions);
    }

    // update the data on the controls to be that from the controller
    this.setScoresOnControls();
    this.onGamesEntryChanged();
};

ScoreEntryCardController.prototype.onClickPlayerPointButton = function(player) {
    // add the point for the specified player to the member sport controller
    this.sport.controller.addPoint(player);
};

ScoreEntryCardController.prototype.resolvePlayerTitleToId = function(playerTitle) {
    var playerId = undefined;
    if (playerTitle) {
        var userId = this.umpyrData.getUserId();
        var filter = playerTitle.toUpperCase();
        if (this.umpyrData.getUserName(userId).toUpperCase() === filter) {
            // this is themselves
            playerId = this.umpyrData.getUserId(userId);
        } else {
            // try the list of friends then
            var friends = this.umpyrData.friends.getFriends();
            for (var i = 0; i < friends.length; i += 1) {
                if ((friends[i].nickname.toUpperCase() === filter) ||
                    (friends[i].email.toUpperCase() === filter)) {
                    // this is it
                    playerId = friends[i].ID;
                    break;
                }
            }
        }
    }
    return playerId;
};

ScoreEntryCardController.prototype.onClickPlayerScore = function(player) {
    if (player == -1) {
        // this is the undo operation, remove the last point
        this.sport.controller.match.removeLastPoint(this.sport.controller, 1);
    } else if (player == -2) {
        // this is the reset operation, reset everything if they are sure...
        var thisScoreEntryCardController = this;
        mdlShowConfirmDialog('Reset Match', 'Are you sure you want to reset the score of this match? You cannot get it back...',
            'Reset', 'Cancel',
            function() {
                // reset the match from the data
                thisScoreEntryCardController.sport.controller.match.reset();
                // update the data on the controls to be that from the sport.controller
                thisScoreEntryCardController.setScoresOnControls();
            });
    } else {
        // add the new point on the sport.controller
        this.sport.controller.addPoint(player);
    }
    // also while we are recording scores we want to record the match duration
    if (this.liveStartTime) {
        // get the time between the start time and now
        // Discard the time and time-zone information.
        var timeDiff = Math.abs(new Date().getTime() - this.liveStartTime.getTime());
        // set this value, converting from ms to seconds while we do
        this.sport.controller.match.setSecondsPlayedFor(Math.ceil(timeDiff / 1000));
    }
    // update the data on the controls to be that from the sport controller
    this.setScoresOnControls();
};

ScoreEntryCardController.prototype.setScoresOnControls = function() {
    // set the titles
    mdlChange(this.elementTitles[0], this.sport.controller.match.playerOneTitle);
    mdlChange(this.elementTitles[1], this.sport.controller.match.playerTwoTitle);
    // are we playing doubles?
    this.scoreEntryDoubles.checked = this.sport.controller.match.isDoubles;
    if (this.sport.controller.match.isDoubles) {
        // are playing doubles
        mdlChange(this.elementTitles[2], this.sport.controller.match.playerOnePartnerTitle);
        mdlChange(this.elementTitles[3], this.sport.controller.match.playerTwoPartnerTitle);
    }
    // get the points won
    this.playerOnePointsBtn.innerText = '' + this.sport.controller.getPoints(0);
    this.playerTwoPointsBtn.innerText = '' + this.sport.controller.getPoints(1);

    if (this.sport.controller.numberSets === 0 && this.sport.controller.numberGames === 0) {
        // there are no games and no sets - just points, show the total points in the 'sets' boxes
        mdlChange(this.elementSets[0], this.sport.controller.match.getTotalPoints(0));
        mdlChange(this.elementSets[1], this.sport.controller.match.getTotalPoints(1));
    } else {
        // get the sets won
        if (this.sport.controller.match.getSets(0) + this.sport.controller.match.getSets(1) > 0) {
            // they have played a set, show this data (if to prevent showing 0-0)
            mdlChange(this.elementSets[0], '' + this.sport.controller.match.getSets(0));
            mdlChange(this.elementSets[1], '' + this.sport.controller.match.getSets(1));
        } else {
            // clear the sets displayed
            mdlChange(this.elementSets[0], '');
            mdlChange(this.elementSets[1], '');
        }
    }

    // for each set played, get the historic values of games for those sets
    var set = 0;
    var setsPlayed = this.sport.controller.match.getNumberSetsPlayed();
    for (set = 0; set < setsPlayed && set < 5; set += 1) {
        mdlChange(this.elementGames[0][set], '' + this.sport.controller.match.getSetResult(set, 0));
        mdlChange(this.elementGames[1][set], '' + this.sport.controller.match.getSetResult(set, 1));
    }
    // now put in the games we are currently playing
    if (set < 5) {
        if (this.sport.controller.isShowGamesInSetResult > 0 && this.sport.controller.match.getGames(0) + this.sport.controller.match.getGames(1) > 0) {
            // they have played a game, not 0-0, show this
            mdlChange(this.elementGames[0][set], '' + this.sport.controller.match.getGames(0));
            mdlChange(this.elementGames[1][set], '' + this.sport.controller.match.getGames(1));
            // increment the set counter to not clear this in the subsequent loop
            set += 1;
        }
        // and clear any games that are not set at this point
        for (; set < 5; set += 1) {
            mdlChange(this.elementGames[0][set], '');
            mdlChange(this.elementGames[1][set], '');
        }
    }

    if (this.sport.controller.isMatchOver()) {
        // show the match over element
        this.showElement(this.matchOver);
        var matchSummary;
        var matchWinner = this.sport.controller.match.getMatchWinner();
        var matchLoser = this.sport.controller.match.getOtherPlayer(matchWinner);
        // get the names of those who played
        var playerOneTitle = this.sport.controller.match.getPlayerTitle(0);
        var playerTwoTitle = this.sport.controller.match.getPlayerTitle(1);
        if (this.sport.controller.match.isDoubles) {
            playerOneTitle += ' and ' + this.sport.controller.match.getPlayerTitle(2);
            playerTwoTitle += ' and ' + this.sport.controller.match.getPlayerTitle(3);
        }
        switch (matchWinner) {
            case 0:
                matchSummary = playerOneTitle + " beat " + playerTwoTitle;
                break;
            case 1:
                matchSummary = playerTwoTitle + " beat " + playerOneTitle;
                break;
            default:
                matchSummary = playerOneTitle + " vs " + playerOneTitle;
                break;
        }
        // show this to the user
        this.matchOver.innerText = matchSummary;
    } else {
        // hide the match over element
        this.hideElement(this.matchOver);
    }
};

ScoreEntryCardController.prototype.onTennisScoreChanged = function() {
    // called every time some data changes on this view
    this.sport.controller.match.playerOneTitle = this.elementTitles[0].value;
    this.sport.controller.match.playerTwoTitle = this.elementTitles[1].value;

    // do we have an ID that matches this?
    this.sport.controller.match.playerOneId = this.elementTitles[0].getAttribute('data-user-id');
    if (!this.sport.controller.match.playerOneId) {
        // didn't select from the list of friends, but it might match anyway
        this.sport.controller.match.playerOneId = this.resolvePlayerTitleToId(this.sport.controller.match.playerOneTitle);
    }
    // and player two
    this.sport.controller.match.playerTwoId = this.elementTitles[1].getAttribute('data-user-id');
    if (!this.sport.controller.match.playerTwoId) {
        // didn't select from the list of friends, but it might match anyway
        this.sport.controller.match.playerTwoId = this.resolvePlayerTitleToId(this.sport.controller.match.playerTwoTitle);
    }
    // now let's do the partners if we need to
    this.sport.controller.match.isDoubles = this.scoreEntryDoubles.checked;
    if (this.sport.controller.match.isDoubles) {
        // are playing doubles
        this.sport.controller.match.playerOnePartnerTitle = this.elementTitles[2].value;
        this.sport.controller.match.playerTwoPartnerTitle = this.elementTitles[3].value;
        // match these titles to IDs if we can
        this.sport.controller.match.playerOnePartnerId = this.elementTitles[2].getAttribute('data-user-id');
        if (!this.sport.controller.match.playerOnePartnerId) {
            // didn't select from the list of friends, but it might match anyway
            this.sport.controller.match.playerOnePartnerId = this.resolvePlayerTitleToId(this.sport.controller.match.playerOnePartnerTitle);
        }
        // and player two's partner
        this.sport.controller.match.playerTwoPartnerId = this.elementTitles[3].getAttribute('data-user-id');
        if (!this.sport.controller.match.playerTwoPartnerId) {
            // didn't select from the list of friends, but it might match anyway
            this.sport.controller.match.playerTwoPartnerId = this.resolvePlayerTitleToId(this.sport.controller.match.playerTwoPartnerTitle);
        }
    } else {
        // clear the partner data
        this.sport.controller.match.playerOnePartnerTitle = undefined;
        this.sport.controller.match.playerOnePartnerId = undefined;
        this.sport.controller.match.playerTwoPartnerTitle = undefined;
        this.sport.controller.match.playerTwoPartnerId = undefined;
    }

    // get the score as the user has entered it (will overwrite what is there)
    this.sport.controller.match.setSets(0, this.elementSets[0].value);
    this.sport.controller.match.setSets(1, this.elementSets[1].value);

    if (this.gamesEntryCheck.checked) {
        if (this.sport.controller.numberSets) {
            // now set the games for each set they may or may not have entered
            var i = 0;
            for (i = 0; i < this.sport.controller.numberSets; i += 1) {
                this.sport.controller.match.recordSetResult(
                    Number(this.elementGames[0][i].value),
                    Number(this.elementGames[1][i].value),
                    i);
            }
            // and record zero for the ones we are not showing / having
            for (i = i; i < 5; i += 1) {
                this.sport.controller.match.recordSetResult(0, 0, i);
            }
        }
        if (this.scoreEntryPastOrLive) {
            // we are not taking live points so now the games have been entered we can calculate the sets
            this.sport.controller.updateScoreCalculations();
        }
        // and populate the sets boxes
        mdlChange(this.elementSets[0], this.sport.controller.match.getSets(0));
        mdlChange(this.elementSets[1], this.sport.controller.match.getSets(1));
    } else {
        // are recording sets only, not games - reset these
        for (var i = 0; i < 5; i += 1) {
            this.sport.controller.match.recordSetResult(0, 0, i);
        }
    }
};

ScoreEntryCardController.prototype.onGamesEntryChanged = function() {
    // if we are entering games, then show all the games' controls
    if (this.sport.controller.numberGames > 0 &&
        (this.gamesEntryCheck.checked || !this.scoreEntryPastOrLive.checked)) {
        // showing games, disable the sets entry directly
        this.disableElement(this.elementSets[0]);
        this.disableElement(this.elementSets[1]);
        // show the title for entering games
        this.showElement(this.scoreEntryGamesTitle);
        if (this.sport.controller.numberSets) {
            var i = 0;
            // and set the span of the title
            this.scoreEntryGamesTitle.setAttribute('colspan', this.sport.controller.numberSets);
            // show the correct number of sets
            for (i = 0; i < this.sport.controller.numberSets; i += 1) {
                this.showElement(this.elementGamesHeadings[i]);
                this.elementGamesHeadings[i].innerText = this.sport.scoringTitles[0] + ' ' + (i + 1);
                this.showElement(this.elementGames[0][i].parentNode.parentNode);
                this.showElement(this.elementGames[1][i].parentNode.parentNode);
            }
            // and hide what remains
            for (i = i; i < 5; i += 1) {
                this.hideElement(this.elementGamesHeadings[i]);
                this.hideElement(this.elementGames[0][i].parentNode.parentNode);
                this.hideElement(this.elementGames[1][i].parentNode.parentNode);
            }
        }
    } else {
        // not showing games, enable the sets entry directly
        this.enableElement(this.elementSets[0]);
        this.enableElement(this.elementSets[1]);
        // hide the title
        this.hideElement(this.scoreEntryGamesTitle);
        // hide all the game entries
        for (var i = 0; i < 5; i += 1) {
            this.hideElement(this.elementGames[0][i].parentNode.parentNode);
            this.hideElement(this.elementGames[1][i].parentNode.parentNode);
            this.hideElement(this.elementGamesHeadings[i]);
        }
    }
    // clan up any data in the fields
    mdlCleanup(this.parentDiv);
};

ScoreEntryCardController.prototype.hideElement = function(element) {
    // this is ok, try to find the parent element that is the table entry to hide though
    element.setAttribute('hidden', true);
};

ScoreEntryCardController.prototype.showElement = function(element) {
    element.removeAttribute('hidden');
};

ScoreEntryCardController.prototype.enableElement = function(element) {
    element.removeAttribute('readonly');
};

ScoreEntryCardController.prototype.disableElement = function(element) {
    element.setAttribute('readonly', 'true');
};

ScoreEntryCardController.prototype.onClickScoreCancel = function() {
    // close the score entry window without submitting the data
    window.umpyr.hideCard(0);
    // and go back to the previous page
    window.umpyr.onNavigationBack();
};

ScoreEntryCardController.prototype.onClickScoreSubmit = function() {
    // get the current mode and call the correct validation method on the score object

    // first let's set all the data the user has entered on the score object
    // let's do the amount of time they played for
    if (this.scoreEntryPastOrLive.checked) {
        // we are entering the data for a game that was played another time - take the time played from the controls
        var hours = this.parentDiv.querySelector('#score-entry-hours-played').value;
        var minutes = this.parentDiv.querySelector('#score-entry-minutes-played').value;
        this.sport.controller.match.setTimePlayedFor(hours, minutes, 0);
        // now we can do the time they say they played at - if live will be today
        var playedDate = this.sport.controller.match.getMatchPlayedDateFormatted();
        // also set the time the game was played
        hours = this.parentDiv.querySelector('#score-entry-time-hours-played').value;
        minutes = this.parentDiv.querySelector('#score-entry-time-minutes-played').value;
        // set this on the date
        playedDate.setHours(hours);
        playedDate.setMinutes(minutes);
        // aren't doing seconds
        playedDate.setSeconds(0);
        // put the played date back into the score, if live it will be 'now'
        this.sport.controller.match.setMatchPlayedDate(playedDate);
        // ensure the score is up-to-date from what they entered in the boxes
        this.sport.controller.updateScoreCalculations();
    }
    // set the description entered on the match
    this.sport.controller.match.setDescription(this.parentDiv.querySelector('#score-entry-description-text').value);
    // get the controller to check the score is valid now
    var isDataValid = this.sport.controller.checkCurrentScore();
    if (isDataValid && window.umpyr.checkSignedInWithMessage()) {
        // the data is valid and the user is logged in, we can save this data now then
        var successFunction = function() {
            // if the save works then hide this card, created on Umpyr - let Umpyr delete it...
            window.umpyr.hideCard(0);
        };
        var errorFunction = function(error) {
            console.error('Error writing new match to Firebase Database', error);
        };
        // call the data manager to save this score in the current mode and let us know when is worked / failed
        this.umpyrData.matches.saveMatch(this.sport.controller.match, successFunction, errorFunction, true);
        // this worked, show the match on the screen now they entered one
        window.umpyr.showLatestEnteredMatch();
    } else {
        // check with the user, if they click again submit the score anyway!...

    }
};

ScoreEntryCardController.prototype.onClickExpand = function() {
    var expansionArea = this.scoreEntryWimbledon.querySelector('.expansion-area');
    if (!expansionArea.classList.contains('visible')) {
        // isn't hidden, show it by expanding
        expansionArea.removeAttribute('hidden');
        // show the contents fading in
        fadeElement(expansionArea, true);
    } else {
        // hide it by removing the visible from the class list
        fadeElement(expansionArea, false, function() {
            // and actually hide the area
            expansionArea.setAttribute('hidden', 'true');
        });
    }
};