'use strict';

// Initializes the postes display
function PostsCardController(parentDiv) {
    // initialise this class
    this.document = document;
    this.parentDiv = parentDiv;
    // and load the data we require
    this.umpyrData = window.umpyr.umpyrData;

    this.postList = this.parentDiv.querySelector('#posts');
    this.postForm = this.parentDiv.querySelector('#post-form');
    this.postInput = this.parentDiv.querySelector('#post');

    this.submitButton = this.parentDiv.querySelector('#submit');
    this.submitImageButton = this.parentDiv.querySelector('#submitImage');
    this.mediaCapture = this.parentDiv.querySelector('#postMediaCapture');

    // Saves post on form submit.
    this.postForm.addEventListener('submit', this.savePost.bind(this));

    // Toggle for the button.
    var buttonTogglingHandler = this.toggleButton.bind(this);
    this.postInput.addEventListener('keyup', buttonTogglingHandler);
    this.postInput.addEventListener('change', buttonTogglingHandler);

    // Events for image upload.
    this.submitImageButton.addEventListener('click', function(e) {
        e.preventDefault();
        this.mediaCapture.click();
    }.bind(this));
    this.mediaCapture.addEventListener('change', this.saveImagePost.bind(this));

    // intialise the button
    this.toggleButton();

    // now that the card is setup, get the data we want do display, listen for changes
    this.umpyrData.addDataListener(this, this.onDataChanged);
    // listening for changes now, but some probably already in the list
    var posts = this.umpyrData.posts.getPosts();
    for (var i = 0; i < posts.length; i += 1) {
        this.displayPost(posts[i]);
    }
}

PostsCardController.prototype.close = function(isLocalClose) {
    // called as this card is removed and closed
    this.umpyrData.removeDataListener(this);
};

PostsCardController.prototype.onDataChanged = function(dataList, data, reason) {
    // called as the data in the list changes, is it for this card to listen?
    if (dataList == this.umpyrData.posts.getPosts()) {
        // this is the posts list changed, this is for us
        if (reason === 'added' || reason === 'changed') {
            // this is a new post, add it
            this.displayPost(data);
        } else if (reason === 'removed') {
            // remove the div that represents the post from the list
            var div = this.parentDiv.querySelector('#fb' + data.key);
            if (div && div.parentElement) {
                div.parentElement.removeChild(div);
            }
        }
    }
};

// Saves a new post containing an image URI in Firebase.
// This first saves the image in Firebase storage.
PostsCardController.prototype.savePost = function(event) {
    event.preventDefault();
    // create the data object that will be saved
    var post = new Post();
    post.text = this.postInput.value;
    mdlChange(this.postInput, "");
    // Check if the user is signed-in
    if (window.umpyr.checkSignedInWithMessage()) {
        // user is signed in, save the data
        this.umpyrData.posts.savePost(post);
    }
    // and clear the selection
    this.postForm.reset();
    this.toggleButton();
};

// Saves a new post containing an image URI in Firebase.
// This first saves the image in Firebase storage.
PostsCardController.prototype.saveImagePost = function(event) {
    event.preventDefault();
    var file = event.target.files[0];

    // Check if the file is an image.
    if (!file.type.match('image.*')) {
        var data = {
            message: 'You can only share images',
            timeout: 2000
        };
        window.umpyr.signInSnackbar.MaterialSnackbar.showSnackbar(data);
        return;
    }
    // create the data object that will be saved
    var post = new Post();
    post.text = this.postInput.value;
    mdlChange(this.postInput, "");
    // Check if the user is signed-in
    if (window.umpyr.checkSignedInWithMessage()) {
        // user is signed in, save the data
        this.umpyrData.posts.savePost(post, file);
    }
    // and clear the selection
    this.postForm.reset();
    this.toggleButton();
};

// Displays a post in the UI.
PostsCardController.prototype.displayPost = function(post) {
    var div = this.parentDiv.querySelector('#fb' + post.key);
    // If an element for that post does not exists yet we create it.
    if (!div) {
        var container = document.createElement('div');
        if (!this.postRowContent) {
            // load the raw content from the html file that we want to use
            this.postRowContent = clientSideInclude('/components/postrow.html');
        }
        container.innerHTML = this.postRowContent;
        div = container.firstChild;
        div.setAttribute('id', 'fb' + post.key);
        if (this.postList.childNodes.length === 0) {
            // this is the first, append this
            this.postList.appendChild(div);
        } else {
            // add this to the top of the list
            this.postList.insertBefore(div, this.postList.childNodes[0]);
        }
    }
    if (post.photoUrl) {
        div.querySelector('.pic').style.backgroundImage = 'url(' + post.photoUrl + ')';
    }
    div.querySelector('.name').textContent = post.authorName;
    var postedDate = post.getPostedDateFormatted();
    div.querySelector('.posted-date').textContent = dateFormat(postedDate, "default");
    var textElement = div.querySelector('.posted-text');
    var imageElement = div.querySelector('.posted-image');
    // do the text content of the post
    textElement.innerHTML = post.text;
    // Replace all line breaks by <br>.
    textElement.innerHTML = textElement.innerHTML.replace(/\n/g, '<br>');
    // do the image of the post
    if (post.imageUrl) {
        var image = document.createElement('img');
        image.addEventListener('load', function() {
            this.postList.scrollTop = this.postList.scrollHeight;
        }.bind(this));
        // clear any current children - might have the initial loading image
        imageElement.innerHTML = "";
        // set the new URL and append as a child of the div
        this.umpyrData.setImageUrl(post.imageUrl, image);
        imageElement.appendChild(image);
    }

    if (post.authorId && post.authorId === this.umpyrData.getUserId()) {
        // this is our post - let us delete it
        var deleteButton = div.querySelector('.post-delete-button');
        var thisController = this;
        deleteButton.removeAttribute('hidden');
        deleteButton.addEventListener('click',
            function() {
                // clicked delete, delete it then
                thisController.umpyrData.posts.deletePost(post);
            },
            false
        );
    }

    // Show the card fading-in and scroll to view the new message.
    fadeElement(div, true);
    this.postList.scrollTop = this.postList.scrollHeight;
    this.postList.focus();
};

// Enables or disables the submit button depending on the values of the input
// fields.
PostsCardController.prototype.toggleButton = function() {
    if (this.postInput.value) {
        this.submitButton.removeAttribute('disabled');
        this.submitImageButton.removeAttribute('disabled');
    } else {
        this.submitButton.setAttribute('disabled', 'true');
        this.submitImageButton.setAttribute('disabled', 'true');
    }
};