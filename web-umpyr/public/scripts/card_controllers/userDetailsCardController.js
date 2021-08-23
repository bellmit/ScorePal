function UserDetailsCardController(parentDiv) {
    // initialise this class
    this.document = document;
    this.parentDiv = parentDiv;
    // and remember the very useful data controller object
    this.umpyrData = window.umpyr.umpyrData;

    // setup the card buttons and global things
    this.parentDiv.querySelector('#user-details-sign-out-button').addEventListener('click', this.umpyrData.signOut.bind(this.umpyrData));
    this.parentDiv.querySelector('#user-details-user-pic').src = this.umpyrData.getUserIcon();
    this.parentDiv.querySelector('#user-details-user-name').textContent = this.umpyrData.getUserName();

    // setup the display of the number of messages we might have, listen for changes to data
    this.umpyrData.addDataListener(this, this.onDataChanged);
    // and show the current situation
    this.updateMessagesSummaryText();

    // setup our global counters
    this.sportsPlayed = 0;
    this.matchesEntered = 0;
    this.matchesPlayed = 0;
    this.matchesWon = 0;
    this.matchesLost = 0;
    this.matchesDrew = 0;
    // and setup the charts
    this.setupCharts();
    // now that the card is setup, get the data we want do display, listen for changes
    this.umpyrData.addDataListener(this, this.onDataChanged);
}

UserDetailsCardController.prototype.close = function(isLocalClose) {
    // called as this card closes...
    this.umpyrData.removeDataListener(this);
};

UserDetailsCardController.prototype.onDataChanged = function(dataList, data, reason) {
    // called as the data in the list changes, is it for this card to listen?
    if (data && data == this.umpyrData.currentUser) {
        // this is the user that has changed - update our display of data
        this.updateMessagesSummaryText();
        this.setupCharts();
    }
};

UserDetailsCardController.prototype.setupCharts = function() {
    // get the charts as members we can use
    this.sportCharts = [];
    // first is the bigger summary chart
    this.sportCharts.push(this.parentDiv.querySelector('#user-details-sports-piechart'));
    // then for each sport they have played, add a chart to the rows below...
    var matchesStats = this.umpyrData.getUserMatchesStats();
    var noSports = UmpyrSports.length;
    var chartsAvailable = this.parentDiv.getElementsByClassName('user-details-sport-piechart');
    var chartIndex = 0;
    for (var i = 0; i < noSports; i += 1) {
        // for each sport, see if the user has played them
        if (matchesStats[UmpyrSports[i].title]) {
            // they played this, add the chart to the list to create charts for
            if (chartIndex >= chartsAvailable.length) {
                // we need some more charts, copy the table row of the first chart
                var chartRow = chartsAvailable[0].parentElement.parentElement;
                var newRow = chartRow.cloneNode(true);
                // append this new row
                chartRow.parentElement.appendChild(newRow);
                // and re-fill the list of available charts to this
                chartsAvailable = newRow.getElementsByClassName('user-details-sport-piechart');
                chartIndex = 0;
            }
            // there is one available now, use this chart
            var element = chartsAvailable[chartIndex];
            element.setAttribute('sport', UmpyrSports[i].title);
            this.sportCharts.push(element);
            chartIndex += 1;
        }
    }

    // Load Charts and the corechart package.
    google.charts.load('current', { 'packages': ['corechart'] });
    // each chart we have created we want to load them then call the function to draw them, do this now...
    for (var i = 0; i < this.sportCharts.length; i += 1) {
        var thisController = this;
        // set the callback - need to use a function generator to get the correct value of i each time
        google.charts.setOnLoadCallback((function(i) {
            return function() {
                thisController.setupSportChart(thisController.sportCharts[i], matchesStats);
            }
        })(i));
    }
};

UserDetailsCardController.prototype.updateMessagesSummaryText = function() {
    var unreadNumber = this.umpyrData.messages.getNumberMessagesUnread();
    var summaryElement = this.parentDiv.querySelector('#user-details-messages-summary');
    if (!unreadNumber) {
        // there are none, hide the entire block 
        summaryElement.parentElement.setAttribute('hidden', 'true');
    } else {
        // show the block
        summaryElement.parentElement.removeAttribute('hidden');
        if (unreadNumber == 1) {
            summaryElement.innerHTML = "You have 1 unread message from ";
        } else {
            summaryElement.innerHTML = "You have " + unreadNumber + " unread messages from ";
        }
    }

    var unhandledNumber = this.umpyrData.matches.getMatchesSubmitted().length;
    summaryElement = this.parentDiv.querySelector('#user-details-submitted-summary');
    if (!unhandledNumber) {
        // there are none, hide the entire block 
        summaryElement.parentElement.setAttribute('hidden', 'true');
    } else {
        // show the block
        summaryElement.parentElement.removeAttribute('hidden');
        summaryElement.innerHTML = 'You have ' + unhandledNumber;
    }

    if (!unhandledNumber && !unreadNumber) {
        // both are hidden, hide the parent of the parent
        summaryElement.parentElement.parentElement.setAttribute('hidden', 'true');
    } else {
        // show the block of data, are telling them something
        summaryElement.parentElement.parentElement.removeAttribute('hidden');
    }
};

UserDetailsCardController.prototype.setupSportChart = function(chartElement, matchesStats) {
    // create the chart on the element
    var chart = new google.visualization.PieChart(chartElement);
    // FROM https://developers.google.com/chart/interactive/docs/gallery/piechart

    // now populate with the stats for the specified sport
    var sport = chartElement.getAttribute('sport');
    if (!sport) {
        // this is the chart for the number of sports, show the sport breakdown here, create an array of the sports we played
        var data = [
            ['Sport', 'Matches Played']
        ];
        for (var i = 0; i < UmpyrSports.length; i += 1) {
            // for each sport, see if the user has played them
            if (matchesStats[UmpyrSports[i].title]) {
                // get the stats for this sport (just played_count)
                data.push([UmpyrSports[i].title, matchesStats[UmpyrSports[i].title].played_count || 0]);
                this.sportsPlayed += 1;
            }
        }
        var options = {
            title: 'Sports Played',
            is3D: true,
        };
        // now we can draw the chart with this data
        chart.draw(google.visualization.arrayToDataTable(data), options);

    } else {
        var sportStats = matchesStats[sport];
        // this is a specific sport, show this win, lose, draw, unplayed breakdown
        var notPlayed = Math.max(0, (sportStats.entered_count || 0) - (sportStats.played_count || 0));
        var lost = sportStats.lose_count || 0;
        var drew = Math.max(0, (sportStats.played_count || 0) - (sportStats.won_count || 0) - (sportStats.lose_count || 0));
        var won = sportStats.won_count || 0;
        // add this data to the global counters
        this.matchesEntered += sportStats.entered_count || 0;
        this.matchesPlayed += sportStats.played_count || 0;
        this.matchesWon += won;
        this.matchesLost += lost;
        this.matchesDrew += drew;
        // create the data table for this data
        var data = google.visualization.arrayToDataTable([
            ['Result', 'Frequency'],
            ['Not Played', notPlayed],
            ['Lose', lost],
            ['Draw', drew],
            ['Win', won],
        ]);
        var options = {
            title: sport,
            is3D: true,
            //pieHole: 0.4,
        };
        // and draw the chart
        chart.draw(data, options);
    }
    // update their summary text
    this.updateUserSummaryText();
};

UserDetailsCardController.prototype.updateUserSummaryText = function() {
    // create the summary of all of their results
    var summaryText = 'You have entered ' + this.matchesEntered + ' match results across ' + this.sportsPlayed + ' sports.</br>';
    if (this.matchesPlayed > 0 && this.matchesEntered > 0) {
        summaryText += 'You played in ' + this.matchesPlayed + ' of these matches (' + Math.round(this.matchesPlayed / this.matchesEntered * 100.0) + '%).</br>';
    }
    summaryText += 'You won ' + this.matchesWon + ' matches, lost ' + this.matchesLost + ' matches, and drew in ' + this.matchesDrew + '.';
    // and set this text on the card
    this.parentDiv.querySelector('#user-details-welcome').innerHTML = summaryText;
};