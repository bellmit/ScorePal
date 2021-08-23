'use strict';


// Initializes the post object
function Post(data) {
    if (!data) {
        // initialise defaults
        this.key = "";
        this.authorName = "";
        this.authorId = "";
        this.text = "";
        this.postedDate = this.createDateString();
        this.photoUrl = Post.PHOTO_PLACEHOLDER_URL;
        this.imageUrl = "";
    } else {
        var val = data.val();
        this.key = data.key;
        this.authorName = val.authorName;
        this.authorId = val.authorId;
        this.text = val.text;
        this.postedDate = val.postedDate;
        this.photoUrl = val.photoUrl || '/images/profile_placeholder.png';
        this.imageUrl = val.imageUrl;
    }
}
// A loading image URL.
Post.LOADING_IMAGE_URL = 'https://www.google.com/images/spin-32.gif';
Post.PHOTO_PLACEHOLDER_URL = '/images/profile_placeholder.png';

Post.prototype.getPostData = function() {
    // return this as a data object to set in firebase - will get back in constructor
    return {
        authorName: this.authorName,
        authorId: this.authorId,
        text: this.text,
        postedDate: this.postedDate || this.createDateString(),
        photoUrl: this.photoUrl,
        imageUrl: this.imageUrl,
    };
};

Post.prototype.createDateString = function(date) {
    if (!date) {
        date = new Date();
    }
    return date.getFullYear() +
        ("0" + (date.getMonth() + 1)).slice(-2) +
        ("0" + date.getDate()).slice(-2) +
        ("0" + date.getHours()).slice(-2) +
        ("0" + date.getMinutes()).slice(-2) +
        ("0" + date.getSeconds()).slice(-2);
};

Post.prototype.getPostedDateFormatted = function() {
    if (!this.receivedDate) {
        this.receivedDate = this.createDateString();
    }
    var yr = parseInt(this.receivedDate.substring(0, 4));
    var mon = parseInt(this.receivedDate.substring(4, 6));
    var dt = parseInt(this.receivedDate.substring(6, 8));
    var hr = parseInt(this.receivedDate.substring(8, 10));
    var mn = parseInt(this.receivedDate.substring(10, 12));
    var sc = parseInt(this.receivedDate.substring(12, 14));
    return new Date(yr, mon - 1, dt, hr, mn, sc);
};