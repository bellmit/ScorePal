function WelcomeCardController(parentDiv) {
    // initialise this class
    this.document = document;
    this.parentDiv = parentDiv;
    // and remember the very useful data controller object
    this.umpyrData = window.umpyr.umpyrData;
    // handle the clicking of the sign-in button
    this.parentDiv.querySelector('#welcome-sign-in-button').addEventListener('click', this.umpyrData.signIn.bind(this.umpyrData));
}

WelcomeCardController.prototype.close = function(isLocalClose) {
    // called as this card closes...
};