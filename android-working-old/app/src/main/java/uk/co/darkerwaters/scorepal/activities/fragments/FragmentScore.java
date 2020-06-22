package uk.co.darkerwaters.scorepal.activities.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.BaseActivity;
import uk.co.darkerwaters.scorepal.activities.animation.ChangeEndsTextAnimation;
import uk.co.darkerwaters.scorepal.activities.animation.ChangeServerTextAnimation;
import uk.co.darkerwaters.scorepal.activities.animation.DecidingPointTextAnimation;
import uk.co.darkerwaters.scorepal.activities.animation.GameOverTextAnimation;
import uk.co.darkerwaters.scorepal.activities.animation.TextViewAnimation;
import uk.co.darkerwaters.scorepal.score.base.Point;
import uk.co.darkerwaters.scorepal.score.tennis.TennisPoint;

public abstract class FragmentScore extends Fragment {

    private final static int K_NO_TEAMS = 2;
    private final static long K_ANIMATION_DURATION = 1000;
    
    private final int noScoreLevels;

    private ScoreState activeState;

    public interface FragmentScoreInteractionListener {
        void onAttachFragment(FragmentScore fragment);
        void onFragmentScorePointsClick(int teamIndex);
        void onFragmentScoreServerMoved();
    }

    public enum ScoreState {
        COMPLETED,
        CHANGE_ENDS,
        CHANGE_SERVER,
        CORRECTION,
        DECIDING_POINT
    }

    private FragmentScoreInteractionListener listener;

    private TextViewAnimation informationAnimator = null;
    private TextView informationText;
    private View serveArrowView;
    private ImageView serveArrowImage;
    private float serverViewYPosition = 0f;
    private boolean isServeChangeEnabled = true;

    private final TextSwitcher[][] switchers;
    private final ViewSwitcher.ViewFactory[] switcherFactories;

    public FragmentScore(int noScoreLevels) { 
        this.noScoreLevels = noScoreLevels;
        // create our lists of controls
        this.switchers = new TextSwitcher[K_NO_TEAMS][this.noScoreLevels];
        this.switcherFactories = new ViewSwitcher.ViewFactory[K_NO_TEAMS];
    }

    protected void setupActivity(View mainView) {
        
        this.activeState = null;
        this.informationText = mainView.findViewById(R.id.information_textView);
        this.serveArrowView = mainView.findViewById(R.id.serveArrowView);
        this.serveArrowImage = mainView.findViewById(R.id.serveArrowImage);

        this.serveArrowView.setOnTouchListener(new View.OnTouchListener() {
           @Override
           public boolean onTouch(View view, MotionEvent motionEvent) {
               if (isServeChangeEnabled) {
                   int y_cord = (int) motionEvent.getY();
                   float translationY = serveArrowView.getTranslationY();
                   float yDifference = y_cord - serverViewYPosition + translationY;
                   if (translationY == 0) {
                       // this is not moved, set the max movement down, also don't allow upwards movement
                       yDifference = Math.min(yDifference < 0f ? 0f : yDifference, serveArrowView.getHeight() * 0.3f);
                   } else {
                       // this is at the bottom, down allow it to go down
                       yDifference = -Math.min(yDifference > 0f ? 0f : -yDifference, serveArrowView.getHeight() * 0.3f);
                   }
                   switch (motionEvent.getAction()) {
                       case MotionEvent.ACTION_DOWN:
                           serverViewYPosition = serveArrowView.getY();
                           break;
                       case MotionEvent.ACTION_MOVE:
                           // move the arrow as we move
                           serveArrowImage.setTranslationY(yDifference);
                           break;
                       case MotionEvent.ACTION_UP:
                           serveArrowImage.setTranslationY(0f);
                           if (Math.abs(yDifference) > serveArrowView.getHeight() * 0.15f) {
                               listener.onFragmentScoreServerMoved();
                           }
                           break;
                       default:
                           break;
                   }
               }
               return true;
           }
        });

        // setup the view switchers
        createViewSwitchers(this.switchers, mainView);

        // listen for clicks on the points to change the points
        this.switchers[0][this.noScoreLevels - 1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                informListenerOfPointsClick(0);
            }
        });
        this.switchers[1][this.noScoreLevels - 1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                informListenerOfPointsClick(1);
            }
        });

        // make the factory to handle the switching of text here
        final Context context = this.getContext();
        final String scoreRef = TennisPoint.ADVANTAGE.displayString(getContext());
        // Set the ViewFactory of the TextSwitcher that will create TextView object when asked
        this.switcherFactories[0] = new ViewSwitcher.ViewFactory() {
            public View makeView() {
                // create a TextView
                TextView t = new ResizeTextView(context, scoreRef);
                t.setGravity(Gravity.CENTER);
                t.setTextColor(context.getColor(R.color.teamOneColor));
                return t;
            }
        };
        this.switcherFactories[1] = new ViewSwitcher.ViewFactory() {
            public View makeView() {
                // create a TextView
                TextView t = new ResizeTextView(context, scoreRef);
                t.setGravity(Gravity.CENTER);
                t.setTextColor(context.getColor(R.color.teamTwoColor));
                return t;
            }
        };

        // load an animation by using AnimationUtils class
        // set this factory for all the switchers
        Animation in = AnimationUtils.loadAnimation(context, R.anim.slide_in_top);
        Animation out = AnimationUtils.loadAnimation(context, R.anim.slide_out_bottom);
        for (int j = 0; j < this.noScoreLevels; ++j) {
            // set the factory
            if (null != this.switchers[0][j]) {
                this.switchers[0][j].setFactory(this.switcherFactories[0]);
                // and the animations
                this.switchers[0][j].setInAnimation(in);
                this.switchers[0][j].setOutAnimation(out);
            }
        }
        // do the bottom row the other way in / out
        in = AnimationUtils.loadAnimation(context, R.anim.slide_in_bottom);
        out = AnimationUtils.loadAnimation(context, R.anim.slide_out_top);
        for (int j = 0; j < this.noScoreLevels; ++j) {
            // set the factory
            if (null != this.switchers[1][j]) {
                this.switchers[1][j].setFactory(this.switcherFactories[1]);
                // and the animations
                this.switchers[1][j].setInAnimation(in);
                this.switchers[1][j].setOutAnimation(out);
            }
        }

        // set the initial server icon to set the correct color
        setTeamServer(0);
    }

    protected abstract void createViewSwitchers(TextSwitcher[][] switchers, View mainView);

    public ScoreState getMatchState() {
        // return the active match state we are animating
        return this.activeState;
    }

    public void cancelMatchState() {
        // stop showing any messages
        if (null != this.informationAnimator) {
            this.informationAnimator.cancel();
            this.informationAnimator = null;
        }
        this.activeState = null;
    }

    public void showMatchState(ScoreState state) {
        // show the state as a nice animation of text that scales up and slides away
        // first cancel any active one
        cancelMatchState();
        // if we are not attached to a context, ignore this attempt to show things
        FragmentActivity activity = getActivity();
        if (!this.isDetached() && null != activity) {
            switch (state) {
                case COMPLETED:
                    this.informationAnimator = new GameOverTextAnimation(activity, this.informationText);
                    break;
                case DECIDING_POINT:
                    this.informationAnimator = new DecidingPointTextAnimation(activity, this.informationText);
                    break;
                case CORRECTION:
                    // nothing to show here?
                    break;
                case CHANGE_ENDS:
                    this.informationAnimator = new ChangeEndsTextAnimation(activity, this.informationText);
                    break;
                case CHANGE_SERVER:
                    this.informationAnimator = new ChangeServerTextAnimation(activity, this.informationText);
                    break;
            }
            this.activeState = state;
        }
    }

    public void setIsServeChangeEnabled(boolean isEnabled) {
        this.isServeChangeEnabled = isEnabled;
    }

    private void informListenerOfPointsClick(int teamIndex) {
        this.listener.onFragmentScorePointsClick(teamIndex);
    }

    public void setSetValue(int teamIndex, String value) {
        setSwitcherText(this.switchers[teamIndex][this.noScoreLevels - 3], value);
    }

    public void setGamesValue(int teamIndex, String value) {
        setSwitcherText(this.switchers[teamIndex][this.noScoreLevels - 2], value);
    }

    public void setPointsValue(int teamIndex, Point value) {
        setSwitcherText(this.switchers[teamIndex][this.noScoreLevels - 1], value.displayString(getContext()));
    }

    public void setTeamServer(int teamIndex) {
        if (null != this.serveArrowView) {
            if (teamIndex == 0) {
                // animate back to where we started
                this.serveArrowView.animate()
                        .translationY(0f)
                        .withStartAction(new Runnable() {
                            @Override
                            public void run() {
                                serveArrowView.setVisibility(View.VISIBLE);
                                BaseActivity.setupButtonIcon(serveArrowImage, R.drawable.ic_baseline_arrow_forward, R.color.teamOneColor);
                            }
                        })
                        .setDuration(K_ANIMATION_DURATION)
                        .start();
            } else {
                // work out how far down from the top set text changer to the bottom we need to go
                float distance = this.switchers[1][0].getY() - this.switchers[0][0].getY();
                // and animate to here
                this.serveArrowView.animate()
                        .translationY(distance)
                        .withStartAction(new Runnable() {
                            @Override
                            public void run() {
                                serveArrowView.setVisibility(View.VISIBLE);
                                BaseActivity.setupButtonIcon(serveArrowImage, R.drawable.ic_baseline_arrow_forward, R.color.teamTwoColor);
                            }
                        })
                        .setDuration(K_ANIMATION_DURATION)
                        .start();
            }
        }
    }

    private void setSwitcherText(TextSwitcher switcher, String content) {
        // if we are not attached to a context, ignore this attempt to show things
        if (false == this.isDetached() && null != switcher && switcher.getCurrentView() instanceof TextView) {
            TextView currentTextView = (TextView) switcher.getCurrentView();
            CharSequence text = currentTextView.getText();
            if (null == text || false == text.toString().equals(content)) {
                // this is different, set it to that passed
                switcher.setText(content);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentScoreInteractionListener) {
            listener = (FragmentScoreInteractionListener) context;
            // and inform this listener of our attachment
            listener.onAttachFragment(this);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentScoreInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
