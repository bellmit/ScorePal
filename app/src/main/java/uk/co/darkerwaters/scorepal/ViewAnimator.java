package uk.co.darkerwaters.scorepal;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * Created by douglasbrain on 14/06/2017.
 */

public class ViewAnimator {

    public interface IViewAnimator {
        public void onAnimationEnd();
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
