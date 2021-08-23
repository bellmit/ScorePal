'use strict';

// Initialise the data object that will do all our data operations
function UmpyrDataPosts(umpyrData, database) {
    // remember the reference to umpyrData so we can pass back information to the master class
    this.umpyrData = umpyrData;
    this.database = database;

    // set our member data
    this.posts = [];

    // load the data
    this.loadPosts();
}

UmpyrDataPosts.prototype.getPosts = function() {
    if (this.posts) {
        // return the array of posts currently loaded
        return this.posts;
    } else {
        // return an empty array all the time so don't have to check
        return [];
    }
};

// Loads all our data
UmpyrDataPosts.prototype.loadPosts = function() {
    // Load and listens for new posts
    this.postsRef = this.database.ref('posts/');
    // make sure we remove all previous listeners
    this.postsRef.off();
    this.posts = [];
    var thisData = this;
    // load all our posts
    var setPost = function(data) {
        // load the data into a nice object
        var post = new Post(data);
        // check this is not already in the list
        var isPostExist = false;
        for (var i = 0; i < thisData.posts.length; i += 1) {
            if (post.key === thisData.posts[i].key) {
                // this is the same, just update this
                thisData.posts[i] = post;
                isPostExist = true;
                break;
            }
        }
        if (!isPostExist) {
            // just push to the end of the list
            thisData.posts.push(post);
        }
        thisData.umpyrData.informDataListenersOfChange(thisData.posts, post, 'added');
    }.bind(this);
    this.postsRef.on('child_added', setPost);
    //this.postsRef.on('child_changed', changePost);
};

UmpyrDataPosts.prototype.savePost = function(post, file) {
    // set the data that we can for the firebase user
    post.authorName = this.umpyrData.currentUser.nickname;
    post.authorId = this.umpyrData.currentUser.ID;
    post.photoUrl = this.umpyrData.currentUser.photoUrl;
    // we want to save an image then, first find the image and push it to firebase
    // via the storage API
    if (file) {
        // add a post with a loading icon initially that will get updated with the shared image.
        post.imageUrl = Post.LOADING_IMAGE_URL;
    } else {
        post.imageUrl = "";
    }
    var postingUser = this.umpyrData.currentUser;
    // push this data to the database to get the key etc we need to set the data
    this.postsRef.push(post).then(function(data) {
        // the placeholder was pushed to the 'posts' part of the database successfully
        // so now we can upload the image to Cloud Storage for the post to reference properly
        if (file) {
            var filePath = postingUser.ID + '/' + data.key + '/' + file.name;
            // there is a file, store this in the cloud store and update the data accordingly
            return this.storage.ref(filePath).put(file).then(function(snapshot) {
                // The photo is online on the cloud store, now we can get the file's Storage URI 
                // and update the posts placeholder to point at the actual file nicely
                var fullPath = snapshot.metadata.fullPath;
                // update the post to point at the image in the cloud store
                return data.update({ imageUrl: this.storage.ref(fullPath).toString() });
            }.bind(this));
        }
    }.bind(this)).catch(function(error) {
        console.error('There was an error uploading a file to Cloud Storage:', error);
    });
};

UmpyrDataPosts.prototype.removeFromList = function(post) {
    if (this.posts) {
        for (var i = 0; i < this.posts.length; i += 1) {
            if (post == this.posts[i]) {
                // this is the one, remove this
                this.posts.splice(i, 1);
                break;
            }
        }
    }
    // inform any listeners of this change
    this.umpyrData.informDataListenersOfChange(this.posts, post, 'removed');
};

UmpyrDataPosts.prototype.deletePost = function(post) {
    // remove this post from the Firebase Database.
    var thisData = this;
    this.database.ref('posts/' + post.key).remove(function(error) {
        if (!error) {
            // removed, remove from our list here
            thisData.removeFromList(post);
        }
    });
    return true;
};