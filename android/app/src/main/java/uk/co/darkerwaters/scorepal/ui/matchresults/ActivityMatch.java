package uk.co.darkerwaters.scorepal.ui.matchresults;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.MatchPersistenceManager;
import uk.co.darkerwaters.scorepal.application.MatchService;
import uk.co.darkerwaters.scorepal.data.Match;
import uk.co.darkerwaters.scorepal.data.MatchId;

public class ActivityMatch extends AppCompatActivity {

    public static final String MATCHID = "MATCHID";
    public static final String FROMMATCH = "FROMMATCH";

    protected boolean isFromMatch;

    protected MatchId lastMatch;
    protected Match activeMatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_results);

        Bundle bundle = getIntent().getExtras();
        // get any messages sent
        lastMatch = new MatchId(bundle.getString(MATCHID, ""));
        isFromMatch = bundle.getBoolean(FROMMATCH, false);

        // be sure to get the active match now
        getActiveMatch();
    }

    @Override
    protected void onResume() {
        // be sure the active match is the correct one (if from the service)
        getActiveMatch();
        super.onResume();
    }

    private void getActiveMatch() {
        MatchService service = MatchService.GetRunningService();
        if (null != service && isFromMatch) {
            // this is the results of the active match, use this
            activeMatch = service.getActiveMatch();
        }
        else if (lastMatch.isValid()) {
            // load this data
            activeMatch = MatchPersistenceManager.GetInstance().loadMatch(lastMatch, this);
        }
    }
}
