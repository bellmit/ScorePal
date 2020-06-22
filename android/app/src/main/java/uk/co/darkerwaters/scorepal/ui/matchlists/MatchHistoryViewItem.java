package uk.co.darkerwaters.scorepal.ui.matchlists;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.Match;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.data.ScoreHistory;

public class MatchHistoryViewItem extends RecyclerView.ViewHolder {

    public final View root;

    private final ImageView teamImage;
    private final TextView title;
    private final TextView description;

    private final TextView teamOneText;
    private final TextView scoreOneText;
    private final TextView teamTwoText;
    private final TextView scoreTwoText;

    public MatchHistoryViewItem(View root) {
        super(root);
        this.root = root;

        // get all the controls on this item
        this.teamImage = root.findViewById(R.id.teamImage);
        this.title = root.findViewById(R.id.matchHistoryTitle);
        this.description = root.findViewById(R.id.matchHistoryDescription);
        
        this.teamOneText = root.findViewById(R.id.teamOneText);
        this.scoreOneText = root.findViewById(R.id.scoreOneText);
        this.teamTwoText = root.findViewById(R.id.teamTwoText);
        this.scoreTwoText = root.findViewById(R.id.scoreTwoText);
    }

    public void setData(ScoreHistory.HistoryValue value, Match match) {
        // this is our data to show, so show it
        int colorId;
        Context context = root.getContext();
        if (value.team == MatchSetup.Team.T_ONE) {
            teamImage.setImageResource(R.drawable.ic_team_one_notitle_black_24dp);
            colorId = R.color.teamOneColor;
        }
        else {
            teamImage.setImageResource(R.drawable.ic_team_two_notitle_black_24dp);
            colorId = R.color.teamTwoColor;
        }
        if (value.scoreString == null || value.scoreString.isEmpty()) {
            this.title.setText(R.string.errorNoScoreString);
        }
        else {
            // we can split this score string into the component data
            String[] scoreStrings = value.scoreString.split(",");
            if (scoreStrings.length != 5) {
                this.title.setText(R.string.errorNoScoreString);
            }
            else {
                // this is from the match writer
                this.title.setText(context.getString(R.string.scoreIncrementDescription,
                        scoreStrings[value.team == MatchSetup.Team.T_ONE ? 0 : 1]));
                /*
                Team One
                Team Two
                Change Level
                Score One
                Score Two
                 */
                // so put this in all our controls
                this.teamOneText.setText(scoreStrings[0]);
                this.teamTwoText.setText(scoreStrings[1]);
                this.scoreOneText.setText(scoreStrings[2] + " - " + scoreStrings[3]);
                this.scoreTwoText.setText(scoreStrings[4]);
            }
        }
        this.description.setText(match.getStateDescription(context, value.state));

        // and set the colours
        teamImage.setImageTintList(ContextCompat.getColorStateList(context, colorId));
        title.setTextColor(context.getColor(colorId));
    }

}
