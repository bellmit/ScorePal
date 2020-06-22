package uk.co.darkerwaters.scorepal.ui.matchlists;

import android.content.Context;
import android.os.Bundle;

import androidx.recyclerview.widget.ItemTouchHelper;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.MatchPersistenceManager;
import uk.co.darkerwaters.scorepal.data.Match;
import uk.co.darkerwaters.scorepal.data.MatchId;
import uk.co.darkerwaters.scorepal.ui.PermissionsHandler;
import uk.co.darkerwaters.scorepal.ui.appsettings.FragmentAppSettingsGeneral;
import uk.co.darkerwaters.scorepal.ui.views.CustomSnackbar;

public abstract class BaseMatchListActivity extends BaseListedActivity
        implements CardMatchRecyclerAdapter.MatchFileListener,
                    PermissionsHandler.Container,
                    PermissionsHandler.PermissionsListener {

    private CardMatchRecyclerAdapter listAdapter;
    private Match matchToShare;

    private PermissionsHandler permissionsHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupActivity(int titleStringId, MatchSwipeHandler.SwipeMatchInterface swipeMatchInterface, int maxColumns, boolean isCreateExpandedCards) {
        super.setTitle(titleStringId);
        // setup our data
        permissionsHandler = new PermissionsHandler(this);

        // setup the list adapter
        this.listAdapter = new CardMatchRecyclerAdapter(this, getSupportFragmentManager(), this, isCreateExpandedCards);
        setupRecyclerView(R.id.recycler_view, maxColumns, this.listAdapter);

        if (null != swipeMatchInterface) {
            MatchSwipeHandler swipeHandler = new MatchSwipeHandler(this, swipeMatchInterface);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeHandler);
            itemTouchHelper.attachToRecyclerView(this.recyclerView);
        }
    }

    protected abstract MatchId[] getMatchList();

    @Override
    protected void onPause() {
        if (null != permissionsHandler) {
            permissionsHandler.removeListener(this);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // listen to the permissions handler
        permissionsHandler.addListener(this);
        // setup the list to show each time we are shown in case another one appeared
        updateMatches();
    }

    @Override
    public PermissionsHandler getPermissionsHandler() {
        return permissionsHandler;
    }

    protected void updateMatches() {
        // refresh the contents of the list adapter
        this.listAdapter.updateMatches(getMatchList());
    }

    @Override
    public void deleteMatchFile(final Match match) {
        final MatchId matchId = new MatchId(match);
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
        if (permissionsHandler.isPermissionsGranted(FragmentAppSettingsGeneral.PERMISSIONS_FILES)) {
            // we have the permissions granted now share the data
            shareMatchData(matchToShare, true);
        }
        else {
            // not allowed, ask for it
            permissionsHandler.checkPermissions(R.string.filesRationale, R.drawable.ic_attach_email_black_24dp, FragmentAppSettingsGeneral.PERMISSIONS_FILES, true);
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onPermissionsChanged(String[] permissions, int[] grantResults) {
        // try to send the match data
        shareMatchData(matchToShare, permissionsHandler.isPermissionsGranted(FragmentAppSettingsGeneral.PERMISSIONS_FILES));
    }

    private void shareMatchData(Match matchToShare, boolean isSendFile) {
        // get the persistence manager to do this
        final MatchId matchId = new MatchId(matchToShare);
        MatchPersistenceManager manager = MatchPersistenceManager.GetInstance();
        manager.shareMatchData(matchToShare, matchId, isSendFile, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PermissionsHandler.PERMISSIONS_REQUEST) {
            // pass this to the handler
            this.permissionsHandler.processPermissionsResult(permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void shareMatchFile(Match match) {
        // Check whether this app has write external storage permission or not.
        this.matchToShare = match;
        // request file access, this will come back to shareMatchData once that is resolved
        requestFileAccess(match);
    }

    private final CustomSnackbar.SnackbarListener snackbarResponder = new CustomSnackbar.SnackbarListener() {
        @Override
        public void onButtonOnePressed() {
            // update the adapter
            updateMatches();
        }
        @Override
        public void onButtonTwoPressed() {
            // update the adapter
            updateMatches();
        }

        @Override
        public void onDismissed() {
            // update the adapter
            updateMatches();
        }
    };

    @Override
    public void hideMatchFile(Match match) {
        // hiding a file is just renaming it to the hidden extension and removing it
        // from the list
        final MatchId matchId = new MatchId(match);
        MatchPersistenceManager manager = MatchPersistenceManager.GetInstance();
        if (manager.hideMatchFile(matchId, this, snackbarResponder)) {
            listAdapter.remove(matchId);
        }
    }

    @Override
    public void restoreMatchFile(Match match) {
        // restore a file is just renaming it to the recent extension and removing it
        // from the list
        final MatchId matchId = new MatchId(match);
        MatchPersistenceManager manager = MatchPersistenceManager.GetInstance();
        if (manager.restoreMatchFile(matchId, this)) {
            // this file is not valid any more, need to remove it from the list
            listAdapter.remove(matchId);
        }
    }
}
