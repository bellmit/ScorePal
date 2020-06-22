package uk.co.darkerwaters.scorepal.ui.matchplay;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.fragment.app.Fragment;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.data.Match;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.points.TennisPoint;
import uk.co.darkerwaters.scorepal.ui.views.ResizeTextView;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

public abstract class FragmentScore<TMatch extends Match> extends Fragment {

    private View root = null;
    protected MatchSetup.Team team = null;

    protected TMatch activeMatch;
    private final int fragmentId;

    public FragmentScore(int fragmentId) {
        this.fragmentId = fragmentId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(fragmentId, container, false);

        return root;
    }

    public void setupControls(MatchSetup.Team team) {
        this.team = team;
    }

    public <T extends View> T findViewById(int viewId) {
        return root.findViewById(viewId);
    }

    public void setDataToControls(TMatch match) {
        this.activeMatch = match;
        displayCurrentScore();
    }

    public abstract void displayCurrentScore();

    protected void setTeamColor(int color, TextView[] textViews) {
        for (TextView view: textViews) {
            view.setTextColor(color);
        }
    }

    protected void setTeamColor(final int color, TextSwitcher[] textViews) {
        final String scoreRef = TennisPoint.ADVANTAGE.displayString(getContext());
        final Context context = getContext();

        // we can share a factory
        final ViewSwitcher.ViewFactory viewFactory = new ViewSwitcher.ViewFactory() {
            public View makeView() {
                // create a TextView
                TextView t = new ResizeTextView(getActivity(), scoreRef);
                t.setGravity(Gravity.CENTER);
                t.setTextColor(color);
                return t;
            }
        };
        // set this factory for all the switchers
        for (TextSwitcher view: textViews) {
            // but we need seperate animators as they might be operating all at once
            final Animation in, out;
            if (team == MatchSetup.Team.T_ONE && getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT) {
                // in portrait team one on top so come in from the top
                in = AnimationUtils.loadAnimation(context, R.anim.slide_in_top);
                out= AnimationUtils.loadAnimation(context, R.anim.slide_out_bottom);
            } else {
                // they are below their labels so both come in from the bottom
                in = AnimationUtils.loadAnimation(context, R.anim.slide_in_bottom);
                out= AnimationUtils.loadAnimation(context, R.anim.slide_out_top);
            }
            // the colours for the text switcher work with a factory for each
            view.setFactory(viewFactory);
            // and the animations
            view.setInAnimation(in);
            view.setOutAnimation(out);
        }
    }

    protected void setSwitcherText(TextSwitcher switcher, String content) {
        // if we are not attached to a context, ignore this attempt to show things
        if (false == this.isDetached() && null != switcher && switcher.getCurrentView() instanceof TextView) {
            TextView currentTextView = (TextView) switcher.getCurrentView();
            CharSequence text = currentTextView.getText();
            if (null == text || false == text.toString().equals(content)) {
                // this is different, set it to that passed
                switcher.setText(content);
            }
        } else {
            Log.error("something is wrong with the child");
        }
    }
}
