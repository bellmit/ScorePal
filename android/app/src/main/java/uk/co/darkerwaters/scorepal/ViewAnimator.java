package uk.co.darkerwaters.scorepal;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

/**
 * Created by douglasbrain on 14/06/2017.
 */

public class ViewAnimator {

    public final Animation animationIn;
    public final Animation animationOut;

    public final ViewSwitcher.ViewFactory pointViewFactory;
    public final ViewSwitcher.ViewFactory scoreViewFactory;

    public interface IViewAnimator {
        public void onAnimationEnd();
    }

    public ViewAnimator(final Context context) {
        // create the text view animators
        this.animationIn = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
        this.animationOut = AnimationUtils.loadAnimation(context, android.R.anim.slide_out_right);
        // and the text view factories
        this.pointViewFactory = new ViewSwitcher.ViewFactory() {
            public View makeView() {
                // create a TextView for the switcher
                TextView myText = new TextView(context);
                myText.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                myText.setTextAppearance(context, R.style.ScorePoints);
                return myText;
            }
        };
        // and the text view factory
        this.scoreViewFactory = new ViewSwitcher.ViewFactory() {
            public View makeView() {
                // create a TextView for the switcher
                TextView myText = new TextView(context);
                myText.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                myText.setTextAppearance(context, R.style.ScoreValues);
                return myText;
            }
        };
    }

    public void setTextSwitcherContent(String content, TextSwitcher view) {
        // get the current view and then the current text
        TextView currentTextView = (TextView) view.getCurrentView();
        String currentText = currentTextView.getText().toString();
        if (false == currentText.equals(content)) {
            // change this data
            view.setText(content);
        }
    }

    public void setTextSwitcherFactories(TextSwitcher view, ViewSwitcher.ViewFactory pointViewFactory) {
        if (null != view) {
            view.setFactory(pointViewFactory);
            view.setInAnimation(animationIn);
            view.setOutAnimation(animationOut);
        }
    }

    static public void slideControlsUpAndAway(Context context, final IViewAnimator listener, final View... views) {
        // visible - hide it
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.out_top);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // nothing here
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                // hide it here
                for (View view : views) {
                    // change the visibility of each view
                    if (null != view) {
                        view.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
                // nothing here
                if (null != listener) {
                    listener.onAnimationEnd();
                }
            }
        });
        for (View view : views) {
            // start the animation of each view
            if (null != view) {
                view.startAnimation(animation);
            }
        }
    }

    public static void slideControlsDownAndIn(Context context, final IViewAnimator listener, final View... views) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.in_top);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // show the views here
                for (View view : views) {
                    // change the visibility of each view
                    if (null != view) {
                        view.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                // nothing here
                if (null != listener) {
                    listener.onAnimationEnd();
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
                // nothing here
            }
        });
        for (View view : views) {
            // start the animation of each view
            if (null != view) {
                view.startAnimation(animation);
            }
        }
    }
}
