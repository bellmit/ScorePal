'use strict';

// Initializes the history display
function HistoryCardController(parentDiv) {
    // initialise this class
    this.document = document;
    this.parentDiv = parentDiv;
    // and remember the very useful data controller object
    this.umpyrData = window.umpyr.umpyrData;
    // remember this so we can access it in the cal functions
    var thisController = this;
    // create the list of events that we can create for each match
    this.events = [];
    // get the cal object
    this.calendar = $('#calendar');
    // listen for changes to matches while we are showing them
    this.umpyrData.addDataListener(this, this.onDataChanged);
    // try to get the calendar created
    this.calendar.fullCalendar({
        // put your options and callbacks here
        events: function(start, end, timezone, callback) {
            // this provides the cal with the source of events (the array list)
            callback(thisController.events);
        },
        viewRender: function(view, element) {
            // this is called each time the view changes, populate the list of events
            thisController.loadMatches(view.start, view.end);
        },
        eventClick: function(event, element) {
            // can show the event they have clicked on, should contain the match object
            //event.title = "CLICKED!";
            //$('#calendar').fullCalendar('updateEvent', event);
            // so here we want to get the match to show the details for
            var match = new Match(event.match);
            // remember the event clicked
            thisController.summarisedEvent = event;
            // and show the summary of this event
            thisController.showMatchSummary(match, element);
            // and rerender the events to show the selected one
            thisController.calendar.fullCalendar('refetchEvents');
        },
        eventAfterRender: function(event, elements) {
            if (thisController.summarisedEvent && event.id === thisController.summarisedEvent.id) {
                for (var i = 0; i < elements.length; i += 1) {
                    elements[i].setAttribute('selected', 'true');
                }
            } else {
                for (var i = 0; i < elements.length; i += 1) {
                    elements[i].removeAttribute('selected');
                }
            }
        },
        eventRender: function(event, eventElement) {
            thisController.renderEvent(event, eventElement);
        },
    });
}

HistoryCardController.prototype.close = function(isLocalClose) {
    // called as this card is removed and closed
    this.umpyrData.removeDataListener(this);
    // we have messed up the list of matches by getting some between dates etc, reset them again
    this.umpyrData.matches.loadMatches();
};

HistoryCardController.prototype.onDataChanged = function(dataList, data, reason) {
    // called as the data in the list changes, is it for this card to listen?
    if (dataList == this.umpyrData.matches.getMatches()) {
        // this is the matches list changed, this is for us
        if (reason === 'added' || reason === 'changed') {
            //TODO this is a new match, add it to the calendar

        } else if (reason === 'removed') {
            // remove the event that represents the match from the list
            for (var i = 0; i < this.events.length; i += 1) {
                if (this.events[i].id === 'fb' + data.getMatchId()) {
                    // this is the event for this match that will be deleted
                    // remove the selected event from the list of events
                    this.events.splice(i, 1);
                    // and refresh the calander
                    $('#calendar').fullCalendar('refetchEvents');
                    break;
                }
            }
        }
    }
};

HistoryCardController.prototype.loadMatches = function(start, end, timezone, callback) {
    // clear our list, the view has changed
    this.events = [];
    // remember the controller to get the list to add to
    var thisController = this;
    var thisUserId = this.umpyrData.getUserId();
    // load all the matches between the dates and call the function to add them as events
    this.umpyrData.matches.getMatchesBetweenDates(start.toDate(), end.toDate(), function(match) {
        var matchMoment = moment(match.getMatchPlayedDate(), "YYYYMMDDHHmmss");
        var sport = SportFromMode(match.getScoreMode());
        // splitting to just get the first word (before a space)
        var sportFirstWord = sport.title.split(' ')[0];
        thisController.events.push({
            id: 'fb' + match.id,
            allDay: false,
            editable: false,
            title: sportFirstWord,
            subtitle: match.getMatchSummary(0, 1, thisController.umpyrData.getUserId(), 'score-pair-cal', 'score-pairs-cal-container'),
            start: matchMoment,
            match: match.getMatchData(thisUserId),
            imageurl: "images/" + sportFirstWord + ".png"
        });
        // are populting the list as a source, now it is changed ask the cal to update it's source of events
        // as it now includes a new one
        thisController.calendar.fullCalendar('refetchEvents');
    });
};

HistoryCardController.prototype.showMatchSummary = function(match, element) {
    // show this card
    window.umpyr.displayMatchDetails(match);
    // this will add the card but it would be nicer to show the card right next to the thing we just click on, wouldn't it?
    var detailsCard = this.document.querySelector('#matchDetailsCard');
    if (detailsCard) {
        // set the postion
        detailsCard.style.position = "absolute";
        // and move it to the correct place
        element = element.toElement;
        var top = 0,
            left = 0;
        do {
            top += element.offsetTop || 0;
            left += element.offsetLeft || 0;
            element = element.offsetParent;
        } while (element);
        if (left > document.body.clientWidth / 2) {
            // position from the right (width - left)
            detailsCard.style.right = document.body.clientWidth - left;
        } else {
            detailsCard.style.left = left; //rect.left + window.scrollX - element.offsetX;
        }
        detailsCard.style.top = top; //rect.top + window.scrollY - element.offsetY;
    }
};

HistoryCardController.prototype.renderEvent = function(event, element) {
    if (event.imageurl) {
        element.find("div.fc-content").prepend("<img src='" + event.imageurl + "' width='12' height='12'>");
    }
    if (event.subtitle) {
        element.find("div.fc-content").append("</br><span>" + event.subtitle + "</span>");
    }
};