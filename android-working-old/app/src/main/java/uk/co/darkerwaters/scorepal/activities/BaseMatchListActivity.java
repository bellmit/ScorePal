package uk.co.darkerwaters.scorepal.activities;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.widget.helper.ItemTouchHelper;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.handlers.MatchRecyclerAdapter;
import uk.co.darkerwaters.scorepal.activities.handlers.PermissionHandler;
import uk.co.darkerwaters.scorepal.activities.handlers.SwipeMatchHandler;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.MatchPersistenceManager;

import static uk.co.darkerwaters.scorepal.activities.handlers.PermissionHandler.MY_PERMISSIONS_REQUEST_READ_FILES;

public abstract class BaseMatchListActivity extends BaseListedActivity implements MatchRecyclerAdapter.MatchFileListener {

    private MatchRecyclerAdapter listAdapter;
    private Match matchToShare;
    private PermissionHandler permissionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupActivity(int titleStringId, SwipeMatchHandler.SwipeMatchInterface swipeMatchInterface, int maxColumns, boolean isCreateExpandedCards) {
        super.setupActivity(titleStringId);
        // setup our data
        this.permissionHandler = null;

        // setup the list adapter
        this.listAdapter = new MatchRecyclerAdapter(this, isCreateExpandedCards);
        setupRecyclerView(R.id.recyclerView, maxColumns, this.listAdapter);

        SwipeMatchHandler swipeHandler = new SwipeMatchHandler(this, swipeMatchInterface);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeHandler);
        itemTouchHelper.attachToRecyclerView(this.recyclerView);
    }

    protected abstract String[] getMatchList();

    @Override
    protected void onResume() {
        super.onResume();
        // setup the list to show each time we are shown in case another one appeared
        updateMatches();
    }

    protected void updateMatches() {
        // refresh the contents of the list adapter
        this.listAdapter.updateMatches(getMatchList());
    }

    @Override
    public void deleteMatchFile(final Match match) {
        final String matchId = match.getMatchId(this);
        MatchPersistenceManager manager = MatchPersistenceManager.GetInstance();
        if (manager.isFileDeleted(matchId, this)) {
            // this is a deleted file, wipe this file forever from here
            manager.wipeDeletedMatchFile(matchId,
                    new Runnable() {
                        @Override
                        public void run() {
                            // also remove from the list
                            listAdapter.remove(matchId);
                        }
                    }, new Runnable() {
                        @Override
                        public void run() {
                            // said 'no' refresh the list to redraw it
                            listAdapter.notifyDataSetChanged();
                        }
                    }, this);
        }
        else {
            // delete the file
            manager.deleteMatchFile(matchId, this);
            // just deleted it, which worked so we can also remove from the list
            listAdapter.remove(matchId);
        }
    }

    private void requestFileAccess(Match match) {
        // need to request permission to access files for the sharing of match data
        this.matchToShare = match;
        // request permission to share the file
        if (null == this.permissionHandler) {
            // there is no handler, make one here
            this.permissionHandler = new PermissionHandler(this,
                    R.string.file_access_explanation,
                    MY_PERMISSIONS_REQUEST_READ_FILES,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    new PermissionHandler.PermissionsHandlerConstructor() {
                        @Override
                        public boolean getIsRequestPermission() {
                            return application.getSettings().getIsRequestFileAccessPermission();
                        }

                        @Override
                        public void onPermissionsDenied(String[] permissions) {
                            application.getSettings().setIsRequestFileAccessPermission(false);
                            shareMatchData(matchToShare, false);
                        }

                        @Override
                        public void onPermissionsGranted(String[] permissions) {
                            shareMatchData(matchToShare, true);
                        }
                    });
        }
        // check / request access to file writing to subsequently share the file
        this.permissionHandler.requestPermission();
    }

    private void shareMatchData(Match matchToShare, boolean isSendFile) {
        // get the persistence manager to do this
        String matchId = matchToShare.getMatchId(this);
        MatchPersistenceManager manager = MatchPersistenceManager.GetInstance();
        manager.shareMatchData(matchToShare, matchId, isSendFile, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // pass this message to our handler
        if (!this.permissionHandler.processPermissionsResult(requestCode, permissions, grantResults)) {
            // the handler didn't do anything, pass it on
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void shareMatchFile(Match match) {
        // Check whether this app has write external storage permission or not.
        this.matchToShare = match;
        // request file access, this will come back to shareMatchData once that is resolved
        requestFileAccess(match);
    }

    @Override
    public void hideMatchFile(Match match) {
        // hiding a file is just renaming it to the hidden extension and removing it
        // from the list
        String matchId = match.getMatchId(this);
        MatchPersistenceManager manager = MatchPersistenceManager.GetInstance();
        if (manager.hideMatchFile(matchId, this)) {
            listAdapter.remove(matchId);
        }
    }

    @Override
    public void restoreMatchFile(Match match) {
        // restore a file is just renaming it to the recent extension and removing it
        // from the list
        String matchId = match.getMatchId(this);
        MatchPersistenceManager manager = MatchPersistenceManager.GetInstance();
        if (manager.restoreMatchFile(matchId, this)) {
            // this file is not valid any more, need to remove it from the list
            listAdapter.remove(matchId);
        }
    }
}
