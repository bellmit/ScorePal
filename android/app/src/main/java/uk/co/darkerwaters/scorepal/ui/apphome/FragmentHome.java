package uk.co.darkerwaters.scorepal.ui.apphome;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.Application;
import uk.co.darkerwaters.scorepal.application.ApplicationState;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.application.MatchPersistenceManager;
import uk.co.darkerwaters.scorepal.application.MatchStatistics;
import uk.co.darkerwaters.scorepal.data.Match;
import uk.co.darkerwaters.scorepal.data.MatchId;
import uk.co.darkerwaters.scorepal.points.Sport;
import uk.co.darkerwaters.scorepal.ui.ActivityMain;
import uk.co.darkerwaters.scorepal.ui.PermissionsHandler;
import uk.co.darkerwaters.scorepal.ui.appsettings.FragmentAppSettingsGeneral;
import uk.co.darkerwaters.scorepal.ui.matchlists.ActivityMatchStatistics;
import uk.co.darkerwaters.scorepal.ui.matchlists.CardMatchHolder;
import uk.co.darkerwaters.scorepal.ui.matchlists.CardMatchRecyclerAdapter;
import uk.co.darkerwaters.scorepal.ui.matchlists.MatchSwipeHandler;
import uk.co.darkerwaters.scorepal.ui.views.CircularProgressBar;
import uk.co.darkerwaters.scorepal.ui.views.CustomSnackbar;

public class FragmentHome extends Fragment
        implements CardMatchRecyclerAdapter.MatchFileListener<Match>,
                    PermissionsHandler.PermissionsListener {

    private final static int RECENT_MATCHES_TO_SHOW = 5;

    private MatchPersistenceManager persistenceManager;
    private PermissionsHandler permissionsHandler;
    
    private ImageButton playTennisButton;
    private FloatingActionButton playTennisFab;
    private ImageButton playBadmintonButton;
    private FloatingActionButton playBadmintonFab;
    private ImageButton playPingPongButton;
    private FloatingActionButton playPingPongFab;


    private ImageView statisticsImage;
    private TextView statisticsSummary;

    private CircularProgressBar wins;
    private CircularProgressBar losses;

    private RecyclerView recyclerView;
    private CardMatchRecyclerAdapter listAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private Context context;
    private Match matchToShare = null;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        context = root.getContext();
        this.persistenceManager = MatchPersistenceManager.GetInstance();
        
        playTennisButton = root.findViewById(R.id.playTennisButton);
        playTennisFab = root.findViewById(R.id.playTennisFab);
        playBadmintonButton = root.findViewById(R.id.playBadmintonButton);
        playBadmintonFab = root.findViewById(R.id.playBadmintonFab);
        playPingPongButton = root.findViewById(R.id.playPingPongButton);
        playPingPongFab = root.findViewById(R.id.playPingPongFab);

        this.statisticsImage = root.findViewById(R.id.matchStatisticsImage);
        this.statisticsSummary = root.findViewById(R.id.statisticsSummary);

        this.wins = root.findViewById(R.id.statisticsProgressWins);
        this.losses = root.findViewById(R.id.statisticsProgressLosses);
        
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playNewMatch(Sport.TENNIS);       
            }
        };
        playTennisButton.setOnClickListener(listener);
        playTennisFab.setOnClickListener(listener);
        listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playNewMatch(Sport.BADMINTON);
            }
        };
        playBadmintonButton.setOnClickListener(listener);
        playBadmintonFab.setOnClickListener(listener);
        listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playNewMatch(Sport.PINGPONG);
            }
        };
        playPingPongButton.setOnClickListener(listener);
        playPingPongFab.setOnClickListener(listener);

        wins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showStatisticsActivity();
            }
        });
        losses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showStatisticsActivity();
            }
        });

        // setup the list on this view now then
        recyclerView = root.findViewById(R.id.recycler_view);

        MatchSwipeHandler.SwipeMatchInterface swipeMatchInterface = new MatchSwipeHandler.SwipeMatchInterface() {
            @Override
            public int getSwipeMode() { return ItemTouchHelper.RIGHT; }
            @Override
            public int getLeftIconResId() { return R.drawable.ic_redo_black_24dp; }
            @Override
            public int getRightIconResId() { return R.drawable.ic_history_black_24dp; }
            @Override
            public int getLeftColor() { return context.getColor(R.color.secondaryColor); }
            @Override
            public int getRightColor() { return context.getColor(R.color.secondaryColor); }
            @Override
            public void handleSwipeLeft(RecyclerView.ViewHolder viewHolder) {
                // whatever
            }
            @Override
            public void handleSwipeRight(RecyclerView.ViewHolder viewHolder) {
                CardMatchHolder card = null;
                if (viewHolder instanceof CardMatchHolder) {
                    card = (CardMatchHolder)viewHolder;
                }
                if (null != card) {
                    // setup right to hide it from the main page
                    card.hideMatchFile();
                }
            }
        };
        // setup the list adapter
        this.listAdapter = new CardMatchRecyclerAdapter(getActivity(), getActivity().getSupportFragmentManager(), this, true);

        float displaySize = Application.getDisplaySize(context).getWidth();
        int noColumns = 1 + (int)(displaySize / 550f);
        layoutManager = new GridLayoutManager(context, Math.min(3, noColumns));
        if (null != this.recyclerView) {
            this.recyclerView.setLayoutManager(layoutManager);
        }

        MatchSwipeHandler swipeHandler = new MatchSwipeHandler(context, swipeMatchInterface);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeHandler);
        itemTouchHelper.attachToRecyclerView(this.recyclerView);

        // specify an adapter with the data
        this.recyclerView.setAdapter(this.listAdapter);

        return root;
    }

    private void showStatisticsActivity() {
        Intent intent = new Intent(getActivity(), ActivityMatchStatistics.class);
        startActivity(intent);
    }

    private void initialiseStatistics() {
        // set our image if we have one
        statisticsImage.setImageResource(R.drawable.ic_person_black_24dp);
        Uri userImage = ApplicationState.Instance().getUserImage();
        if (null != userImage && !userImage.toString().isEmpty()) {
            statisticsImage.setImageURI(userImage);
        }
        // and get the recent match stats to show
        MatchStatistics matchStatistics = MatchStatistics.GetInstance(context, true);
        // create the text description
        int played = matchStatistics.getRecentMatchesRecorded();
        int wins = matchStatistics.getRecentWinsTotal();
        int losses = matchStatistics.getRecentLossesTotal();
        String description = String.format(context.getString(R.string.recent_summary),
                MatchStatistics.K_RECENT_DAYS_THRESHOLD, played, wins, losses);
        statisticsSummary.setText(description);
        // and set the progress
        if (played > 0) {
            float progress = wins / (float)played * 100f;
            this.wins.setProgress((int)progress);
            progress = losses / (float)played * 100f;
            this.losses.setProgress((int)progress);
        }
        else {
            this.wins.setProgress(0);
            this.losses.setProgress(0);
        }
        // set the text for the numbers
        this.wins.setTitle(Integer.toString(wins));
        this.losses.setTitle(Integer.toString(losses));
        // and the subtitles
        this.wins.setSubTitle(context.getString(R.string.statistics_wins));
        this.losses.setSubTitle(context.getString(R.string.statistics_losses));
    }

    private void playNewMatch(Sport sport) {
        FragmentActivity activity = getActivity();
        if (activity instanceof ActivityMain) {
            // have the main start the new match
            ((ActivityMain)activity).playNewMatch(sport);
        }
        else {
            Log.error("FragementHome is parented by " + activity);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        FragmentActivity activity = getActivity();
        if (null != activity && activity instanceof PermissionsHandler.Container) {
            permissionsHandler = ((PermissionsHandler.Container)activity).getPermissionsHandler();
            permissionsHandler.addListener(this);
        }
        initialiseStatistics();
        this.listAdapter.updateMatches(getMatchList());
        // update the contents of the recycler view
        this.recyclerView.setAdapter(this.listAdapter);
    }

    private MatchId[] getMatchList() {
        MatchId[] matchIds = MatchPersistenceManager.GetInstance().listRecentMatches(-1, getContext());
        // sort the array so that we have the latest
        Arrays.sort(matchIds, new Comparator<MatchId>() {
            @Override
            public int compare(MatchId obj1, MatchId obj2) {
                // we want the latest first - so reverse the sort order of the objects
                if (null != obj1 && null != obj2) {
                    // have objects
                    Date date1 = obj1.getDate();
                    Date date2 = obj2.getDate();
                    if (null != date1 && null != date2) {
                        // compare the dates
                        return date2.compareTo(date1);
                    }
                    else {
                        // compare the strings - should never get here as should always have dates
                        return obj2.toString().compareTo(obj1.toString());
                    }
                }
                else {
                    // missing some object
                    return 0;
                }
            }
        });
        MatchId[] toReturn = new MatchId[Math.min(RECENT_MATCHES_TO_SHOW, matchIds.length)];
        for (int i = 0; i < toReturn.length; ++i) {
            // move the match ID from the persistence manager to our list to show
            toReturn[i] = matchIds[i];
        }
        return toReturn;
    }

    @Override
    public void onPause() {
        // stop listening to the permissions handler
        if (null != permissionsHandler) {
            permissionsHandler.removeListener(this);
        }
        super.onPause();
    }

    private boolean isPermissionsGranted(String[] permissions) {
        return null != permissionsHandler && permissionsHandler.isPermissionsGranted(permissions);
    }

    private void checkPermissions(int rationaleString, int iconRes, String[] permissions) {
        // check for these permissions
        if (null != permissionsHandler) {
            permissionsHandler.checkPermissions(rationaleString, iconRes, permissions, true);
        }
    }

    @Override
    public void onPermissionsChanged(String[] permissions, int[] grantResults) {
        shareMatchFile(matchToShare);
    }

    @Override
    public void deleteMatchFile(final Match match) {
        final MatchId matchId = new MatchId(match);
        MatchPersistenceManager manager = MatchPersistenceManager.GetInstance();
        if (manager.isFileDeleted(matchId, getContext())) {
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
                    }, getActivity());
        }
        else {
            // delete the file
            manager.deleteMatchFile(matchId, getContext());
            // just deleted it, which worked so we can also remove from the list
            listAdapter.remove(matchId);
        }
    }

    @Override
    public void shareMatchFile(Match match) {
        // Check whether this app has write external storage permission or not.
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

    private void shareMatchData(Match matchToShare, boolean isSendFile) {
        // get the persistence manager to do this
        final MatchId matchId = new MatchId(matchToShare);
        MatchPersistenceManager manager = MatchPersistenceManager.GetInstance();
        manager.shareMatchData(matchToShare, matchId, isSendFile, getContext());
    }

    private final CustomSnackbar.SnackbarListener snackbarResponder = new CustomSnackbar.SnackbarListener() {
        @Override
        public void onButtonOnePressed() {
            // update the adapter
            listAdapter.updateMatches(getMatchList());
            listAdapter.notifyDataSetChanged();
        }
        @Override
        public void onButtonTwoPressed() {
            listAdapter.updateMatches(getMatchList());
            listAdapter.notifyDataSetChanged();
        }

        @Override
        public void onDismissed() {
            listAdapter.updateMatches(getMatchList());
            listAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void hideMatchFile(Match match) {
        // hiding a file is just renaming it to the hidden extension and removing it
        // from the list
        final MatchId matchId = new MatchId(match);
        MatchPersistenceManager manager = MatchPersistenceManager.GetInstance();
        if (manager.hideMatchFile(matchId, getContext(), snackbarResponder)) {
            listAdapter.remove(matchId);
        }
    }

    @Override
    public void restoreMatchFile(Match match) {
        // restore a file is just renaming it to the recent extension and removing it
        // from the list
        final MatchId matchId = new MatchId(match);
        MatchPersistenceManager manager = MatchPersistenceManager.GetInstance();
        if (manager.restoreMatchFile(matchId, getContext())) {
            // this file is not valid any more, need to remove it from the list
            listAdapter.remove(matchId);
        }
    }
}
