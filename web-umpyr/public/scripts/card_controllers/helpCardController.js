function HelpCardController(parentDiv) {
    // initialise this class
    this.document = document;
    this.parentDiv = parentDiv;
    // and remember the very useful data controller object
    this.umpyrData = window.umpyr.umpyrData;
    // handle legal
    this.parentDiv.querySelector("#help-legal-button").addEventListener(
        'click',
        function(e) {
            var element = parentDiv.querySelector('#help-card-legal');
            if (element.getAttribute('hidden')) {
                // show the data
                element.removeAttribute('hidden');
                // select the chip
                parentDiv.querySelector('#help-legal-button').parentElement.setAttribute('selected', 'true');
            } else {
                // hide the data
                element.setAttribute('hidden', 'true');
                // deselect the chip
                parentDiv.querySelector('#help-legal-button').parentElement.removeAttribute('selected');
            }
        },
        false);
    // handle about
    this.parentDiv.querySelector("#help-about-button").addEventListener(
        'click',
        function(e) {
            var element = parentDiv.querySelector('#help-card-about');
            if (element.getAttribute('hidden')) {
                // show the data
                element.removeAttribute('hidden');
                // select the chip
                parentDiv.querySelector('#help-about-button').parentElement.setAttribute('selected', 'true');
            } else {
                // hide the data
                element.setAttribute('hidden', 'true');
                // deselect the chip
                parentDiv.querySelector('#help-about-button').parentElement.removeAttribute('selected');
            }
        },
        false);
    // handle contact
    this.parentDiv.querySelector("#help-contact-button").addEventListener(
        'click',
        function(e) {
            var element = parentDiv.querySelector('#help-card-contact');
            if (element.getAttribute('hidden')) {
                // show the data
                element.removeAttribute('hidden');
                // select the chip
                parentDiv.querySelector('#help-contact-button').parentElement.setAttribute('selected', 'true');
            } else {
                // hide the data
                element.setAttribute('hidden', 'true');
                // deselect the chip
                parentDiv.querySelector('#help-contact-button').parentElement.removeAttribute('selected');
            }
        },
        false);

    // and submit
    var thisController = this;
    this.parentDiv.querySelector('#help-contact-send').addEventListener(
        'click',
        function(e) {
            e.preventDefault();
            // accept the information as a message to the administrator
            thisController.acceptMessage();
        },
        false
    );

    // populate the data we can on the form (who and the email source)
    mdlChange(this.parentDiv.querySelector('#help-contact-name'), this.umpyrData.getUserName());
    mdlChange(this.parentDiv.querySelector('#help-contact-email'), this.umpyrData.currentUser.email);

    // listen for them typing a message
    this.parentDiv.querySelector('#help-contact-message').addEventListener("input", function() {
        thisController.messageChanged();
    });
}

HelpCardController.prototype.close = function(isLocalClose) {
    // called as this card closes...

};

HelpCardController.prototype.messageChanged = function() {
    var contents = this.parentDiv.querySelector('#help-contact-message').value;
    if (contents && contents.length > 0) {
        this.parentDiv.querySelector('#help-contact-send').removeAttribute('disabled');
    } else {
        this.parentDiv.querySelector('#help-contact-send').setAttribute('disabled', 'true');
    }
}

HelpCardController.prototype.acceptMessage = function() {
    // send this message to the site administrator
    this.umpyrData.messages.sendSiteMessage(this.umpyrData.currentUser.email + "    " + this.parentDiv.querySelector('#help-contact-message').value);
    mdlChange(this.parentDiv.querySelector('#help-contact-message'), '');
    var data = {
        message: 'Thank you for your message, we will try to get back to you as soon as possible via email.',
        timeout: 5000
    };
    window.umpyr.signInSnackbar.MaterialSnackbar.showSnackbar(data);
};