package uk.co.darkerwaters.scorepal.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.Log;

public class UiHelper {

    private static final int FILE_CHOSEN = 141;

    public static void ImportMatchData(Activity activity) {
        // import our match data by showing the file chooser intent to select a file to import
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //sets the select file to all types of files
        intent.setType("*/*");
        // Only get openable files
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        //starts new activity to select file and return data
        activity.startActivityForResult(Intent.createChooser(intent, "Choose File to Upload.."), FILE_CHOSEN);
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context){
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static void setTextViewBold(TextView textView) {
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
    }

    public static void setTextViewNoBold(TextView textView) {
        textView.setTypeface(textView.getTypeface(), Typeface.NORMAL);
    }

    public static void showUserMessage(Context context, int messageId) {
        if (null != context && context instanceof Activity) {
            View snackbarView = ((Activity) context).findViewById(R.id.container);
            if (null != snackbarView) {
                Snackbar.make(snackbarView, messageId, Snackbar.LENGTH_LONG).show();
            }
            else {
                // just be lazy and toast
                Toast.makeText(context, messageId, Toast.LENGTH_SHORT).show();
                Log.error("showing toast because context is" + context.getClass().toString());
            }
        }
    }

    public static void showUserMessage(Context context, String message) {
        if (null != context) {
            boolean isMessageShown = false;
            if (context instanceof Activity) {
                View snackbarView = ((Activity) context).findViewById(R.id.container);
                if (null != snackbarView) {
                    Snackbar.make(snackbarView, message, Snackbar.LENGTH_LONG).show();
                    isMessageShown = true;
                }
            }
            if (!isMessageShown) {
                // just be lazy and toast
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                Log.error("showing toast because context is" + context.getClass().toString());
            }
        }
    }
}
