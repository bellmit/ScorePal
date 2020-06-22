package uk.co.darkerwaters.scorepal.ui.matchplay;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.MatchService;
import uk.co.darkerwaters.scorepal.data.Match;
import uk.co.darkerwaters.scorepal.data.MatchId;
import uk.co.darkerwaters.scorepal.data.Score;
import uk.co.darkerwaters.scorepal.data.ScoreState;
import uk.co.darkerwaters.scorepal.points.Sport;
import uk.co.darkerwaters.scorepal.ui.matchresults.ActivityMatch;
import uk.co.darkerwaters.scorepal.ui.matchresults.ActivityResultsMatch;

public abstract class FragmentMatchPlay <TMatch extends Match, TScore extends Score>
        extends Fragment
        implements Match.MatchListener<TScore> {

    protected final int fragmentId;
    protected final Sport sport;

    protected TMatch activeMatch;

    protected ViewGroup messageLayout;
    protected ImageView messageIcon;
    protected TextView messageText;

    protected FloatingActionButton undoButton;
    protected FloatingActionButton stopButton;

    protected FragmentMatchPlay(Sport sport, int fragmentId) {
        this.fragmentId = fragmentId;
        this.sport = sport;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(this.fragmentId, container, false);
        // setup the controls on this new fragment
        setupControls(root);
        // and return the view
        return root;
    }

    protected void setupControls(View root) {
        messageLayout = root.findViewById(R.id.messageOverlayLayout);
        messageIcon = root.findViewById(R.id.messageOverlayImage);
        messageText = root.findViewById(R.id.messageOverlayText);

        undoButton = root.findViewById(R.id.undoLastButton);
        stopButton = root.findViewById(R.id.stopMatchButton);

        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                undoLastPoint();
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopMatch();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // get the active match
        MatchService matchService = MatchService.GetRunningService();
        if (null != matchService) {
            // was there a previously active match?
            if (null != activeMatch) {
                // there was a previous match that was different
                if (null != activeMatch) {
                    activeMatch.removeListener(this);
                }
            }
            // and get the new active match
            activeMatch = (TMatch) matchService.getActiveMatch();
            // listen for changes to the score so we can show that
            activeMatch.addListener(this);
            // set all the data to be up-to-date now
            setDataToControls(activeMatch, null);
        }
        else {
            // there is no service to play this match
            getActivity().finish();
        }
    }

    protected abstract void setDataToControls(TMatch activeMatch, TScore score);

    @Override
    public void onMatchStateChanged(final TScore score, final ScoreState state) {
        // the score just changed, update our data
        if (!isDetached() && !state.isChanged(ScoreState.ScoreChange.INCREMENT_REDO)) {
            // this activity is still good, this might come from outside the UI thread though
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // set our data again now the score has changed
                    setDataToControls(activeMatch, score);
                    // is this an important state change?
                    String stateDescription = activeMatch.getStateDescription(getContext(), state.getState());
                    if (null != stateDescription && !stateDescription.isEmpty()) {
                        Display display = getActivity().getWindowManager().getDefaultDisplay();
                        Point windowSize = new Point();
                        display.getSize(windowSize);
                        // we want to translate this text out
                        final float movement = (0.5f * windowSize.x) + messageLayout.getWidth();
                        messageText.setText(stateDescription);
                        messageLayout.setTranslationX(-movement);
                        messageLayout.setAlpha(0f);
                        messageLayout
                                .animate()
                                .alpha(1f)
                                .withStartAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        messageLayout.setVisibility(View.VISIBLE);
                                    }
                                })
                                .translationXBy(movement)
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        messageLayout
                                                .animate()
                                                .setStartDelay(500)
                                                .alpha(0f)
                                                .translationX(movement)
                                                .withEndAction(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        messageLayout.setTranslationX(0);
                                                        messageLayout.setVisibility(View.GONE);
                                                    }
                                                })
                                                .setDuration(750)
                                                .start();
                                    }
                                })
                                .setDuration(750)
                                .start();
                    }
                }
            });
        }
    }

    @Override
    public void onPause() {
        if (null != activeMatch) {
            // pausing this activity - store the match data
            MatchService service = MatchService.GetRunningService();
            if (null != service) {
                service.storeMatchState(false);
            }
            activeMatch.removeListener(this);
            activeMatch = null;
        }
        super.onPause();
    }

    private void stopMatch() {
        // this doesn't as much stop the match as show the results screen. Then they can accept
        // the results, go back, or throw away the memories
        Intent intent = new Intent(getActivity(), ActivityResultsMatch.class);
        intent.putExtra(ActivityMatch.MATCHID, new MatchId(activeMatch).toString());
        intent.putExtra(ActivityMatch.FROMMATCH, true);
        // and show these results to let them end the match
        getActivity().startActivity(intent);
    }

    private void undoLastPoint() {
        if (null != activeMatch) {
            // undo the last point on the score, will cause a message to be sent to update
            activeMatch.undoLastPoint();
        }
    }
}
