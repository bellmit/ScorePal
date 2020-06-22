package uk.co.darkerwaters.scorepal.activities.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.BaseActivity;
import uk.co.darkerwaters.scorepal.activities.handlers.MatchRecyclerAdapter;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.MatchPersistenceManager;

public abstract class LayoutScoreSummary<T extends Match> {

    private ViewGroup moreLessLayout;
    private Button moreLessButton;
    private Button deleteButton;
    private Button shareButton;
    private Button hideButton;

    private boolean isMoreShown;

    protected View parent;

    public LayoutScoreSummary() {

    }

    public abstract View createView(LayoutInflater inflater, ViewGroup container);

    public void initialiseViewContents(View mainView) {
        // this main view is our parent, use this
        this.parent = mainView;

        // get the more / less controls and button
        this.moreLessButton = this.parent.findViewById(R.id.moreLessButton);
        this.moreLessLayout = this.parent.findViewById(R.id.moreLessLayout);
        this.deleteButton = this.parent.findViewById(R.id.deleteButton);
        this.shareButton = this.parent.findViewById(R.id.shareButton);
        this.hideButton = this.parent.findViewById(R.id.hideButton);

        BaseActivity.setupButtonIcon(this.deleteButton, R.drawable.ic_baseline_delete, 0);
        BaseActivity.setupButtonIcon(this.hideButton, R.drawable.ic_baseline_history, 0);

        // currently more is shown
        this.isMoreShown = true;
    }

    protected void setTextViewBold(TextView textView) {
        BaseActivity.setTextViewBold(textView);
    }

    protected void setTextViewNoBold(TextView textView) {
        BaseActivity.setTextViewNoBold(textView);
    }

    public void showCurrentServer(T match) {
        Log.error("Show current server unimplemented");
    }

    public void setMatchData(final T match, final MatchRecyclerAdapter.MatchFileListener source) {
        Context context = this.parent.getContext();
        MatchPersistenceManager matchPersistenceManager = MatchPersistenceManager.GetInstance();
        if (matchPersistenceManager.isFileDeleted(match.getMatchId(context), context)) {
            // the match is deleted, adjust the icon accordingly to look more serious
            BaseActivity.setupButtonIcon(this.deleteButton, R.drawable.ic_baseline_delete_forever, 0);
        }

        this.moreLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreLess();
            }
        });
        this.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                source.deleteMatchFile(match);
            }
        });
        this.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                source.shareMatchFile(match);
            }
        });

        this.hideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                source.hideMatchFile(match);
            }
        });

        // hide the more controls
        showMoreLess();
    }

    public boolean isMoreShown() { return this.isMoreShown; }

    public void hideMoreButton() {
        this.moreLessButton.setVisibility(View.INVISIBLE);
    }

    public void hideHideButton() {
        this.hideButton.setVisibility(View.INVISIBLE);
    }

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
            BaseActivity.setupButtonIcon(this.moreLessButton, R.drawable.ic_baseline_keyboard_arrow_left, 0);
        }
        else {
            // less is shown
            this.moreLessButton.setText(R.string.btn_more);
            BaseActivity.setupButtonIcon(this.moreLessButton, R.drawable.ic_baseline_keyboard_arrow_right, 0);
        }
    }

}
