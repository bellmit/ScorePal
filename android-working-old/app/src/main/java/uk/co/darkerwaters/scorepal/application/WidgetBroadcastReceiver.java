package uk.co.darkerwaters.scorepal.application;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RemoteViews;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.MainActivity;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.MatchSettings;
import uk.co.darkerwaters.scorepal.score.MatchWriter;
import uk.co.darkerwaters.scorepal.score.Score;
import uk.co.darkerwaters.scorepal.score.base.Sport;
import uk.co.darkerwaters.scorepal.settings.SettingsControl;

public class WidgetBroadcastReceiver extends AppWidgetProvider {

    public static void UpdateAppWidgets(Application application, Context context) {
        int[] ids = AppWidgetManager.getInstance(application).getAppWidgetIds(new ComponentName(application, WidgetBroadcastReceiver.class));
        WidgetBroadcastReceiver myWidget = new WidgetBroadcastReceiver();
        myWidget.onUpdate(context, AppWidgetManager.getInstance(context), ids);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        // Get all ids
        ComponentName thisWidget = new ComponentName(context, WidgetBroadcastReceiver.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {
            // See the dimensions and
            Bundle options = appWidgetManager.getAppWidgetOptions(widgetId);
            // Get min width and height.
            int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
            // get the views to show the data on
            RemoteViews remoteViews = getRemoteViews(context, minWidth, minHeight);
            // and update the widget with this data
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context,
                                          AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {

        // See the dimensions and
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);

        // Get min width and height.
        int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

        // Obtain appropriate widget and update it.
        appWidgetManager.updateAppWidget(appWidgetId, getRemoteViews(context, minWidth, minHeight));

        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    private RemoteViews getRemoteViews(Context context, int minWidth, int minHeight) {
        // First find out rows and columns based on width provided.
        int rows = getCellsForSize(minHeight);
        int columns = getCellsForSize(minWidth);

        RemoteViews remoteViews;
        if (rows >= 2) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_widget_2row);
        }
        else {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_widget);
        }

        // first, if there is a sport, it can jump to the running match
        GamePlayCommunicator communicator = GamePlayCommunicator.GetActiveCommunicator();
        Match currentMatch = null;
        MatchSettings currentSettings = null;
        // by defaut we can show the app main screen
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent mainIntent = PendingIntent.getActivity(context, 0, intent, 0);

        int actionOneIcon = R.drawable.ic_tennis_serve;
        int actionTwoIcon = R.drawable.ic_tennis_receive;

        // but are we playing a match?
        if (null != communicator) {
            // there is a communicator, get the active match
            if (communicator.isActive()) {
                currentMatch = communicator.getCurrentMatch();
                currentSettings = communicator.getCurrentSettings();
            }
            if (new SettingsControl(communicator.getCurrentApplication()).getIsControlTeams()) {
                actionOneIcon = R.drawable.ic_team_one;
                actionTwoIcon = R.drawable.ic_team_two;
            }
        }
        String titleString = context.getString(R.string.app_name);
        String subtitleString = "";
        Sport sport = null;
        int buttonVisibility = View.INVISIBLE;
        // Create an explicit intent for an Activity in your app - jump to the playing activity
        // for the sport - will be the latest anyway as this is what's showing when this pops up
        if (currentMatch != null) {
            // there is a match, clicking the main image should jump to the play activity of this, set all the data
            Team teamOne = currentMatch.getTeamOne();
            Team teamTwo = currentMatch.getTeamTwo();
            remoteViews.setTextViewText(R.id.playerOneTextView, teamOne.getTeamName());
            remoteViews.setTextViewText(R.id.playerTwoTextView, teamTwo.getTeamName());
            Score score = currentMatch.getScore();
            int levels = score.getLevels();
            String scoreString;
            if (levels > 2) {
                // show the top level score
                scoreString = String.format("%s - %s", score.getDisplayPoint(2, teamOne).displayString(context), score.getDisplayPoint(2, teamTwo).displayString(context));
                remoteViews.setTextViewText(R.id.scoreOneTextView, scoreString);
            }
            else {
                remoteViews.setTextViewText(R.id.scoreOneTextView, "");
            }
            if (levels > 1) {
                // show the games level score
                scoreString = String.format("%s - %s", score.getDisplayPoint(1, teamOne).displayString(context), score.getDisplayPoint(1, teamTwo).displayString(context));
                remoteViews.setTextViewText(R.id.scoreTwoTextView, scoreString);
            }
            else {
                remoteViews.setTextViewText(R.id.scoreTwoTextView, "");
            }
            // show the games level score
            scoreString = String.format("%s - %s", score.getDisplayPoint(0, teamOne).displayString(context), score.getDisplayPoint(0, teamTwo).displayString(context));
            remoteViews.setTextViewText(R.id.scoreThreeTextView, scoreString);

            if (null != currentSettings) {
                sport = currentSettings.getSport();
                intent = new Intent(context, sport.playActivityClass);
                mainIntent = PendingIntent.getActivity(context, 0, intent, 0);
            }
            buttonVisibility = View.VISIBLE;
        }
        else if (currentSettings != null) {
            // there is no match, but there are settings, jump here instead
            sport = currentSettings.getSport();
            intent = new Intent(context, sport.setupActivityClass);
            mainIntent = PendingIntent.getActivity(context, 0, intent, 0);

            remoteViews.setTextViewText(R.id.playerOneTextView, currentSettings.getTeamOne().getTeamName());
            remoteViews.setTextViewText(R.id.playerTwoTextView, currentSettings.getTeamTwo().getTeamName());

            remoteViews.setTextViewText(R.id.scoreOneTextView, "");
            remoteViews.setTextViewText(R.id.scoreTwoTextView, "");
            remoteViews.setTextViewText(R.id.scoreThreeTextView, "");
        }
        else {
            // there is no match, show the title instead
            remoteViews.setTextViewText(R.id.playerOneTextView, "");
            remoteViews.setTextViewText(R.id.playerTwoTextView, "");
            remoteViews.setTextViewText(R.id.scoreOneTextView, "");
            remoteViews.setTextViewText(R.id.scoreTwoTextView, context.getString(R.string.app_name));
            remoteViews.setTextViewText(R.id.scoreThreeTextView, "");
        }
        // set the color for the widget
        //remoteViews.setInt(R.id.main_layout, "setBackgroundColor", R.color.colorPrimaryDark);

        // Set the image for the current sport
        if (null != sport) {
            remoteViews.setImageViewBitmap(R.id.widgetTitleImageView, Application.GetBitmapFromAssets(sport.imageFilename, context));
        }
        else {
            remoteViews.setImageViewResource(R.id.widgetTitleImageView, R.mipmap.ic_launcher);
        }

        // set the button icons correctly
        remoteViews.setImageViewResource(R.id.widgetActionOneButton, actionOneIcon);
        remoteViews.setImageViewResource(R.id.widgetActionTwoButton, actionTwoIcon);
        remoteViews.setImageViewResource(R.id.widgetUndoButon, R.drawable.ic_baseline_undo);
        remoteViews.setImageViewResource(R.id.widgetAnnounceButton, R.drawable.ic_baseline_volume_up);

        // set the visibility of the buttons
        remoteViews.setViewVisibility(R.id.widgetActionOneButton, buttonVisibility);
        remoteViews.setViewVisibility(R.id.widgetActionTwoButton, buttonVisibility);
        remoteViews.setViewVisibility(R.id.widgetUndoButon, buttonVisibility);
        remoteViews.setViewVisibility(R.id.widgetAnnounceButton, buttonVisibility);

        remoteViews.setInt(R.id.widgetActionOneButton, "setBackgroundResource", R.drawable.card_fade);
        remoteViews.setInt(R.id.widgetActionTwoButton, "setBackgroundResource", R.drawable.card_fade);
        remoteViews.setInt(R.id.widgetUndoButon, "setBackgroundResource", R.drawable.card_fade);
        remoteViews.setInt(R.id.widgetAnnounceButton, "setBackgroundResource", R.drawable.card_fade);

        // Register an onClickListener to take the use to the main activity on clicking
        remoteViews.setOnClickPendingIntent(R.id.widgetTitleImageView, mainIntent);
        // setup the clicking actions correctly
        remoteViews.setOnClickPendingIntent(R.id.widgetActionOneButton, GamePlayNotification.ActionIntent.K_ACTION_ONE.getIntent(context));
        remoteViews.setOnClickPendingIntent(R.id.widgetActionTwoButton, GamePlayNotification.ActionIntent.K_ACTION_TWO.getIntent(context));
        remoteViews.setOnClickPendingIntent(R.id.widgetUndoButon, GamePlayNotification.ActionIntent.K_ACTION_UNDO.getIntent(context));
        remoteViews.setOnClickPendingIntent(R.id.widgetAnnounceButton, GamePlayNotification.ActionIntent.K_ACTION_ANNOUNCE.getIntent(context));

        return remoteViews;
    }

    /**
     * Returns number of cells needed for given size of the widget.
     *
     * @param size Widget size in dp.
     * @return Size in number of cells.
     */
    private static int getCellsForSize(int size) {
        int n = 2;
        while (70 * n - 30 < size) {
            ++n;
        }
        return n - 1;
    }
}
