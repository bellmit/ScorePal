package uk.co.darkerwaters.scorepal.activities.handlers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.HashMap;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.BaseActivity;

public class ResourceManager {

    private final HashMap<Integer, Drawable> drawables;

    public ResourceManager() {
        // do all in create and close
        this.drawables = new HashMap<>();
    }

    public void create(BaseActivity context) {
        // create all the drawables etc
        storeDrawable(context, R.drawable.ic_baseline_add, R.color.play);
        storeDrawable(context, R.drawable.ic_baseline_arrow_forward, R.color.expand);
        storeDrawable(context, R.drawable.ic_baseline_arrow_back, R.color.expand);
        storeDrawable(context, R.drawable.ic_baseline_bluetooth, R.color.primaryTextColor);
        storeDrawable(context, R.drawable.ic_baseline_bluetooth_connected, R.color.play);
        storeDrawable(context, R.drawable.ic_baseline_bluetooth_sm, R.color.primaryTextColor);
        storeDrawable(context, R.drawable.ic_baseline_bluetooth_searching, R.color.primaryTextColor);
        storeDrawable(context, R.drawable.ic_baseline_bluetooth_disabled, R.color.primaryTextColor);
        storeDrawable(context, R.drawable.ic_baseline_cancel, R.color.resetColor);
        storeDrawable(context, R.drawable.ic_baseline_compare_arrows, R.color.swapEndColor);
        storeDrawable(context, R.drawable.ic_baseline_contacts, R.color.primaryTextColor);
        storeDrawable(context, R.drawable.ic_baseline_delete, R.color.resetColor);
        storeDrawable(context, R.drawable.ic_baseline_delete_forever, R.color.resetColor);
        storeDrawable(context, R.drawable.ic_baseline_flip_to_back, R.color.primaryTextColor);
        storeDrawable(context, R.drawable.ic_baseline_games, R.color.primaryTextColor);
        storeDrawable(context, R.drawable.ic_baseline_games_sm, R.color.primaryTextColor);
        storeDrawable(context, R.drawable.ic_baseline_gps_fixed, R.color.primaryTextColor);
        storeDrawable(context, R.drawable.ic_baseline_history, R.color.primaryTextColor);
        storeDrawable(context, R.drawable.ic_baseline_import_export, R.color.swapEndColor);
        storeDrawable(context, R.drawable.ic_baseline_input, R.color.swapEndColor);
        storeDrawable(context, R.drawable.ic_baseline_keyboard_arrow_left, R.color.expand);
        storeDrawable(context, R.drawable.ic_baseline_keyboard_arrow_right, R.color.expand);
        storeDrawable(context, R.drawable.ic_baseline_link, R.color.primaryTextColor);
        storeDrawable(context, R.drawable.ic_baseline_lock, R.color.resetColor);
        storeDrawable(context, R.drawable.ic_baseline_lock_open, R.color.primaryTextColor);
        storeDrawable(context, R.drawable.ic_baseline_person_outline, R.color.primaryTextColor);
        storeDrawable(context, R.drawable.ic_baseline_pie_chart, R.color.primaryTextColor);
        storeDrawable(context, R.drawable.ic_baseline_play_circle_outline, R.color.play);
        storeDrawable(context, R.drawable.ic_baseline_replay, R.color.undo);
        storeDrawable(context, R.drawable.ic_baseline_settings_remote, R.color.expand);
        storeDrawable(context, R.drawable.ic_baseline_settings_input_antenna, R.color.expand);
        storeDrawable(context, R.drawable.ic_baseline_settings, R.color.expand);
        storeDrawable(context, R.drawable.ic_baseline_settings_sm, R.color.expand);
        storeDrawable(context, R.drawable.ic_baseline_share, R.color.primaryTextColor);
        storeDrawable(context, R.drawable.ic_baseline_stop, R.color.stop);
        storeDrawable(context, R.drawable.ic_baseline_undo, R.color.undo);
        storeDrawable(context, R.drawable.ic_baseline_vibration, R.color.primaryTextColor);
        storeDrawable(context, R.drawable.ic_baseline_volume_down, R.color.primaryTextColor);
        storeDrawable(context, R.drawable.ic_baseline_volume_down_sys, R.color.primaryTextColor);
        storeDrawable(context, R.drawable.ic_baseline_volume_mute, R.color.primaryTextColor);
        storeDrawable(context, R.drawable.ic_baseline_volume_mute_sm, R.color.primaryTextColor);
        storeDrawable(context, R.drawable.ic_baseline_volume_off, R.color.primaryTextColor);
        storeDrawable(context, R.drawable.ic_baseline_volume_off_sm, R.color.primaryTextColor);
        storeDrawable(context, R.drawable.ic_baseline_volume_up, R.color.primaryTextColor);
        storeDrawable(context, R.drawable.ic_baseline_volume_up_sm, R.color.primaryTextColor);
        storeDrawable(context, R.drawable.ic_baseline_volume_up_sys, R.color.primaryTextColor);
        storeDrawable(context, R.drawable.ic_tennis_receive, R.color.primaryTextColor);
        storeDrawable(context, R.drawable.ic_tennis_serve, R.color.serverColor);
        storeDrawable(context, R.drawable.ic_team_one_color, R.color.teamOneColor);
        storeDrawable(context, R.drawable.ic_team_two_color, R.color.teamTwoColor);
    }

    private Drawable storeDrawable(Context context, int drawableId, int colorId) {
        // get the drawable
        Drawable drawable = context.getDrawable(drawableId);
        if (colorId > 0) {
            // set the tint
            SetIconTint(drawable, context.getColor(colorId));
        }
        // and map it
        this.drawables.put(Integer.valueOf(drawableId), drawable);
        return drawable;
    }

    public Drawable getDrawable(Context context, int drawableId) {
        Drawable drawable = this.drawables.get(drawableId);
        if (null == drawable) {
            // get this instead
            return storeDrawable(context, drawableId, 0);
        }
        else {
            // return the cached one
            return drawable;
        }
    }

    public void setDrawableStart(Context context, Button button, int drawableId) {
        Drawable drawable = getDrawable(context, drawableId);
        button.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
    }

    public void setDrawableEnd(Context context, Button button, int drawableId) {
        Drawable drawable = getDrawable(context, drawableId);
        button.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
    }

    public void setDrawables(Context context, Button button, int drawableIdStart, int drawableIdEnd) {
        Drawable drawableStart = drawableIdStart != 0 ? getDrawable(context, drawableIdStart) : null;
        Drawable drawableEnd = drawableIdEnd != 0 ? getDrawable(context, drawableIdEnd) : null;
        button.setCompoundDrawablesWithIntrinsicBounds(drawableStart, null, drawableEnd, null);
    }

    public void setDrawables(Context context, Button button, int drawableIdStart, int drawableIdEnd, int colorId) {
        Drawable drawableStart = drawableIdStart != 0 ? getDrawable(context, drawableIdStart) : null;
        Drawable drawableEnd = drawableIdEnd != 0 ? getDrawable(context, drawableIdEnd) : null;
        if (null != drawableStart) {
            // mutate this and set the color
            drawableStart = drawableStart.mutate();
            SetIconTint(drawableStart, context.getColor(colorId));
        }
        if (null != drawableEnd) {
            // mutate this and set the color
            drawableEnd = drawableEnd.mutate();
            SetIconTint(drawableEnd, context.getColor(colorId));
        }
        button.setCompoundDrawablesWithIntrinsicBounds(drawableStart, null, drawableEnd, null);
    }

    public void setDrawable(Context context, ImageButton button, int drawableId) {
        Drawable drawable = getDrawable(context, drawableId);
        button.setImageDrawable(drawable);
    }

    public void setDrawable(Context context, ImageButton button, int drawableId, int colorId) {
        Drawable drawable = getDrawable(context, drawableId);
        if (null != drawable) {
            // mutate this and set the color
            drawable = drawable.mutate();
            SetIconTint(drawable, context.getColor(colorId));
        }
        button.setImageDrawable(drawable);
    }

    public void setDrawable(Context context, ImageView button, int drawableId) {
        Drawable drawable = getDrawable(context, drawableId);
        button.setImageDrawable(drawable);
    }

    public void setDrawable(Context context, ImageView button, int drawableId, int colorId) {
        Drawable drawable = getDrawable(context, drawableId);
        if (null != drawable) {
            // mutate this and set the color
            drawable = drawable.mutate();
            SetIconTint(drawable, context.getColor(colorId));
        }
        button.setImageDrawable(drawable);
    }

    public void setDrawable(Context context, MenuItem item, int drawableId) {
        Drawable drawable = getDrawable(context, drawableId);
        item.setIcon(drawable);
    }

    public void close() {
        this.drawables.clear();
    }

    private void SetIconTint(ImageButton button, int tint) {
        DrawableCompat.setTint(button.getDrawable(), tint);
    }

    private void SetIconTint(Drawable drawable, int tint) {
        DrawableCompat.setTint(drawable, tint);
    }

    private void SetIconTint(ImageView image, int tint) {
        DrawableCompat.setTint(image.getDrawable(), tint);
    }

    private void SetIconTint(View button, int tint) {
        if (button instanceof Button) {
            SetIconTint((Button) button, tint);
        } else if (button instanceof ImageButton) {
            SetIconTint((ImageButton) button, tint);
        } else if (button instanceof ImageView) {
            SetIconTint((ImageView) button, tint);
        }
    }

    private void SetIconTint(Button button, int tint) {
        for (Drawable icon : button.getCompoundDrawables()) {
            if (null != icon) {
                SetIconTint(icon, tint);
            }
        }
    }


}