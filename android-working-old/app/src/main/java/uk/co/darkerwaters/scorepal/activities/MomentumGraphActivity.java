package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.GamePlayCommunicator;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.base.PointChange;
import uk.co.darkerwaters.scorepal.views.MatchMomentumGraph;

public class MomentumGraphActivity extends BaseActivity implements Match.MatchListener {

    private static final float K_MIN_ZOOM = 1f;
    private static final float K_MAX_ZOOM = 64f;
    protected MatchMomentumGraph matchMomentumGraph;
    private Match activeMatch = null;

    private Button matchMomentumFocusButton;

    private ImageButton leftButton;
    private ImageButton rightButton;
    private ImageButton zoomInButton;
    private ImageButton zoomOutButton;

    private int momentumFocus = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_momentum_graph);

        // set the title of this
        setupActivity(R.string.match_momentum);

        this.matchMomentumGraph = findViewById(R.id.matchMomentumGraph);
        this.matchMomentumGraph.setIsShowEntireMatchData(true);

        this.matchMomentumFocusButton = findViewById(R.id.matchMomentumFocusButton);
        this.matchMomentumFocusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swapMomentumFocus();
            }
        });

        this.leftButton = findViewById(R.id.imageButtonMomentumLeft);
        this.rightButton = findViewById(R.id.imageButtonMomentumRight);
        this.zoomInButton = findViewById(R.id.imageButtonMomentumZoomIn);
        this.zoomOutButton = findViewById(R.id.imageButtonMomentumZoomOut);

        this.leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                offsetViewZoom(-0.2f);
            }
        });
        this.rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                offsetViewZoom(+0.2f);
            }
        });

        this.zoomInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeViewZoom(2f);
            }
        });
        this.zoomOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeViewZoom(0.5f);
            }
        });
    }

    private void offsetViewZoom(float factor) {
        this.matchMomentumGraph.offsetZoomPosition(factor);
    }

    private void changeViewZoom(float factor) {
        float newZoom = this.matchMomentumGraph.getViewZoom() * factor;
        if (newZoom >= K_MIN_ZOOM && newZoom <= K_MAX_ZOOM) {
            this.matchMomentumGraph.setViewZoom(newZoom);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        GamePlayCommunicator communicator = GamePlayCommunicator.ActivateCommunicator(this);
        if (null != communicator) {
            this.activeMatch = communicator.getCurrentMatch();
        }
        else {
            this.activeMatch = null;
        }
        if (null != this.activeMatch) {
            this.activeMatch.addListener(this);
        }

        // show the up-to-date data from the current match
        setMatchHistory();
    }

    @Override
    protected void onPause() {
        if (null != this.activeMatch) {
            this.activeMatch.removeListener(this);
            this.activeMatch = null;
        }
        super.onPause();
    }

    @Override
    public void onMatchChanged(Match.MatchChange type) {
        // whatever
    }

    @Override
    public void onMatchPointsChanged(PointChange[] levelsChanged) {
        // show the up-to-date data from the current match
        this.matchMomentumGraph.setMatchData(this.activeMatch);
        // and update the display
        this.matchMomentumGraph.invalidate();
    }

    private void swapMomentumFocus() {
        // swap the numbers
        this.momentumFocus = this.momentumFocus == 0 ? 1 : 0;
        // and update the display of this focus
        setMatchHistory();
    }

    public void setMatchHistory() {
        if (null != this.matchMomentumGraph && null != this.activeMatch) {
            // set the focus correctly
            this.matchMomentumGraph.setGraphFocus(this.momentumFocus);
            // and show this on the button
            this.matchMomentumFocusButton.setText(
                    this.momentumFocus == 0 ?
                            this.activeMatch.getTeamOne().getTeamName() : this.activeMatch.getTeamTwo().getTeamName());
            // set the color of the text to the correct team color
            this.matchMomentumFocusButton.setTextColor(getColor(this.momentumFocus == 0 ?
                    R.color.teamOneColor : R.color.teamTwoColor));
            // and set the data accordingly
            this.matchMomentumGraph.setMatchData(this.activeMatch);
        }
    }
}
