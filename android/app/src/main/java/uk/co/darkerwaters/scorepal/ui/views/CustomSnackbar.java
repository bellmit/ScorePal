package uk.co.darkerwaters.scorepal.ui.views;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import uk.co.darkerwaters.scorepal.R;

public class CustomSnackbar {

    private final Snackbar snackbar;

    private final TextView messageTextView;
    private final TextView textViewOne;
    private final TextView textViewTwo;
    private final ImageView iconView;
    private final ImageButton closeButton;

    public interface SnackbarListener {
        void onButtonOnePressed();
        void onButtonTwoPressed();
        void onDismissed();
    }

    public CustomSnackbar(Activity context, int message, int iconRes, SnackbarListener listener) {
        this(context, message, iconRes, -1, -1, listener);
    }
    public CustomSnackbar(Activity context, String message, int iconRes, SnackbarListener listener) {
        this(context, message, iconRes, -1, -1, listener);
    }

    public CustomSnackbar(Activity context, int message, int iconRes, int buttonString, SnackbarListener listener) {
        this(context, message, iconRes, buttonString, -1, listener);
    }

    public CustomSnackbar(Activity context, String message, int iconRes, int buttonString, SnackbarListener listener) {
        this(context, message, iconRes, buttonString, -1, listener);
    }

    public CustomSnackbar(Activity context, int message, int iconRes, int buttonOneString, int buttonTwoString, final SnackbarListener listener) {
        this(context, context.getString(message), iconRes, buttonOneString, buttonTwoString, listener);
    }

    public CustomSnackbar(Activity context, String message, int iconRes, int buttonOneString, int buttonTwoString, final SnackbarListener listener) {
        // Create the Snackbar
        LinearLayout.LayoutParams objLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        snackbar = Snackbar.make(context.findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE);

        // Get the Snackbar layout view
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();

        // Set snackbar layout params
        int navbarHeight = getNavBarHeight(context);
        FrameLayout.LayoutParams parentParams = (FrameLayout.LayoutParams) layout.getLayoutParams();
        parentParams.setMargins(0, 0, 0, 0 - navbarHeight + 50);
        layout.setLayoutParams(parentParams);
        layout.setPadding(0, 0, 0, 0);
        layout.setLayoutParams(parentParams);

        // Inflate our custom view
        View snackView = context.getLayoutInflater().inflate(R.layout.layout_snackbar, null);

        // Configure our custom view
        messageTextView = snackView.findViewById(R.id.message_text_view);
        messageTextView.setText(message);

        iconView = snackView.findViewById(R.id.imageIconSnackbar);
        iconView.setImageResource(iconRes);

        textViewOne = snackView.findViewById(R.id.first_text_view);
        if (buttonOneString < 0) {
            // hide this
            textViewOne.setVisibility(View.GONE);
        }
        else {
            // setup this data
            textViewOne.setVisibility(View.VISIBLE);
            textViewOne.setText(buttonOneString);

            textViewOne.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != listener) { listener.onButtonOnePressed(); }
                    snackbar.dismiss();
                }
            });
        }

        textViewTwo = snackView.findViewById(R.id.second_text_view);
        if (buttonTwoString < 0) {
            // hide this
            textViewTwo.setVisibility(View.GONE);
        }
        else {
            // setup this data
            textViewTwo.setVisibility(View.VISIBLE);
            textViewTwo.setText(buttonTwoString);
            textViewTwo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != listener) { listener.onButtonTwoPressed(); }
                    snackbar.dismiss();
                }
            });
        }

        closeButton = snackView.findViewById(R.id.imageButtonClose);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != listener) { listener.onDismissed(); }
                snackbar.dismiss();
            }
        });
        // Add our custom view to the Snackbar's layout
        layout.addView(snackView, objLayoutParams);
        // Show the Snackbar
        snackbar.show();
    }

    public static int getNavBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
