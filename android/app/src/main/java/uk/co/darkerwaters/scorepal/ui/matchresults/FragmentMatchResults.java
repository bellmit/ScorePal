package uk.co.darkerwaters.scorepal.ui.matchresults;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.MatchPersistenceManager;
import uk.co.darkerwaters.scorepal.data.Match;
import uk.co.darkerwaters.scorepal.data.MatchId;
import uk.co.darkerwaters.scorepal.points.Sport;
import uk.co.darkerwaters.scorepal.ui.UiHelper;
import uk.co.darkerwaters.scorepal.ui.matchlists.CardMatchRecyclerAdapter;

public abstract class FragmentMatchResults<TMatch extends Match> extends Fragment {

    protected final int fragmentId;
    protected final Sport sport;

    protected TMatch activeMatch;

    private ViewGroup moreLessLayout;
    private Button moreLessButton;
    private Button deleteButton;
    private Button shareButton;
    //private Button hideButton;
    private final boolean isAllowExpandContract;

    private boolean isMoreShown;

    protected CardMatchRecyclerAdapter.MatchFileListener parent;
    private boolean isHideMoreSection = false;
    private boolean isFragmentCreated = false;

    protected FragmentMatchResults(Sport sport, int fragmentId, boolean isAllowExpandContract) {
        this.fragmentId = fragmentId;
        this.sport = sport;
        this.isAllowExpandContract = isAllowExpandContract;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(this.fragmentId, container, false);

        // setup the controls on this new fragment
        setupControls(root);

        // and return the view
        return root;
    }

    protected void setupControls(View root) {
        // get the more / less controls and button
        this.moreLessButton = root.findViewById(R.id.moreLessButton);
        this.moreLessLayout = root.findViewById(R.id.moreLessLayout);
        this.deleteButton = root.findViewById(R.id.deleteButton);
        this.shareButton = root.findViewById(R.id.shareButton);
        //this.hideButton = root.findViewById(R.id.hideButton);

        // currently more is shown
        this.isMoreShown = true;

        isFragmentCreated = true;
    }

    public boolean getIsFragmentCreated() {
        return this.isFragmentCreated;
    }

    @Override
    public void onResume() {
        super.onResume();
        // to be safe (we had some crashes) check our member data before we update
        if (isMemberDataSet()) {
            // and show the current match data
            showMatchData();
        }
    }

    protected boolean isMemberDataSet() {
        return null != parent && null != parent.getContext() && null != activeMatch;
    }

    public void setMatchData(final TMatch match, final CardMatchRecyclerAdapter.MatchFileListener source) {
        // set the data to show on this fragment when we are setup
        activeMatch = match;
        parent = source;
    }

    public void showMatchData() {
        // setup the data on this fragment for the active match now
        if (isMemberDataSet()) {
            MatchPersistenceManager matchPersistenceManager = MatchPersistenceManager.GetInstance();
            if (matchPersistenceManager.isFileDeleted(new MatchId(activeMatch), parent.getContext())) {
                //TODO the match is deleted, adjust the icon accordingly to look more serious
                Drawable drawable = parent.getContext().getDrawable(R.drawable.ic_delete_forever_black_24dp);
                this.moreLessButton.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            }
            // and handle the more / less clicking
            this.moreLessButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showMoreLess();
                }
            });
            this.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    parent.deleteMatchFile(activeMatch);
                }
            });
            this.shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    parent.shareMatchFile(activeMatch);
                }
            });
            /*
            this.hideButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    source.hideMatchFile(match);
                }
            });
             */

            // force the fragment to show more and hide the more button on this activity
            if (isAllowExpandContract) {
                // we are allowing the view to expand and contract the controls, hide them for starters
                showMoreLess();
            } else {
                // we want to leave everything showing all the time
                hideMoreButton();
            }
            // and update the current server
            showCurrentServer(activeMatch);
        }

        if (isHideMoreSection) {
            moreLessLayout.setVisibility(View.GONE);
        }
    }

    protected void setTextViewBold(TextView textView) {
        UiHelper.setTextViewBold(textView);
    }

    protected void setTextViewNoBold(TextView textView) {
        UiHelper.setTextViewNoBold(textView);
    }

    public abstract void showCurrentServer(TMatch match);

    public boolean isMoreShown() { return this.isMoreShown; }

    public void hideMoreButton() {
        this.moreLessButton.setVisibility(View.INVISIBLE);
    }
    /*
    public void hideHideButton() {
        this.hideButton.setVisibility(View.INVISIBLE);
    }*/

    public void showMoreLess() {
        this.isMoreShown = !this.isMoreShown;
        // hide show everything except the button
        for (int i = 0; i < this.moreLessLayout.getChildCount(); ++i) {
            View child = this.moreLessLayout.getChildAt(i);
            if (child != this.moreLessButton) {
                // this is not the button we always want shown, so show / hide it
                child.setVisibility(this.isMoreShown ? View.VISIBLE : View.GONE);
            }
        }
        if (this.isMoreShown) {
            // more is shown
            this.moreLessButton.setText(R.string.btn_less);
            Drawable drawable = parent.getContext().getDrawable(R.drawable.ic_expand_less_black_24dp);
            this.moreLessButton.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        }
        else {
            // less is shown
            this.moreLessButton.setText(R.string.btn_more);
            Drawable drawable = parent.getContext().getDrawable(R.drawable.ic_expand_more_black_24dp);
            this.moreLessButton.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        }
    }

    public void hideMoreSection() {
        this.isHideMoreSection = true;
    }
}
