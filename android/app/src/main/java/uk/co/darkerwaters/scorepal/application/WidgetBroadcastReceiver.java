package uk.co.darkerwaters.scorepal.application;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.Match;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.points.Point;
import uk.co.darkerwaters.scorepal.points.Sport;
import uk.co.darkerwaters.scorepal.ui.ActivityMain;
import uk.co.darkerwaters.scorepal.ui.matchplay.ActivityPlayMatch;

public class WidgetBroadcastReceiver extends AppWidgetProvider {

    public static void UpdateAppWidgets(Context context) {
        int[] ids = AppWidgetManager.getInstance(context.getApplicationContext()).getAppWidgetIds(new ComponentName(context, WidgetBroadcastReceiver.class));
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

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_widget_new);
        /*if (rows >= 2) {
            // First find out rows and columns based on width provided.
            int rows = getCellsForSize(minHeight);
            int columns = getCellsForSize(minWidth);
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_widget_2row);
        }
        else {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_widget);
        }*/

        // first, if there is a sport, it can jump to the running match
        MatchService service = MatchService.GetRunningService();
        Match match = null;
        MatchSetup preparedSetup = null;
        // by defaut we can show the app main screen
        Intent intent = new Intent(context, ActivityMain.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int actionOneIcon = R.drawable.ic_player_serving_black_24dp;
        int actionTwoIcon = R.drawable.ic_player_receiving_backhand_black_24dp;

        // but are we playing a match?
        if (null != service) {
            // there is a communicator, get the active match
            match = service.getActiveMatch();
            preparedSetup = service.getPreparedMatchSetup();
            if (ApplicationState.Instance().getPreferences().getIsControlTeams()) {
                actionOneIcon = R.drawable.ic_team_one_black_24dp;
                actionTwoIcon = R.drawable.ic_team_two_black_24dp;
            }
        }
        String player = context.getString(R.string.app_name);
        String opponent = "";
        String playerScore = "";
        String playedLevel = "";
        String opponentScore = "";

        Sport sport = null;
        int buttonVisibility = View.INVISIBLE;
        // Create an explicit intent for an Activity in your app - jump to the playing activity
        // for the sport - will be the latest anyway as this is what's showing when this pops up
        if (match != null) {
            MatchSetup setup = match.getSetup();
            sport = setup.getSport();
            // there is a match, clicking the main image should jump to the play activity of this, set all the data
            // find the level to which we played and the scores at that level
            Point pointOne = null, pointTwo = null;
            int level = 0;
            for (int i = 0; i < match.getScoreLevels(); ++i) {
                pointOne = match.getDisplayPoint(i, MatchSetup.Team.T_ONE);
                pointTwo = match.getDisplayPoint(i, MatchSetup.Team.T_TWO);
                if (null != pointOne && null != pointTwo && (
                        pointOne.val() > 0 || pointTwo.val() > 0)) {
                    // we have two display points and one of them isn't zero, don't go lower
                    level = i;
                    break;
                }
            }
            // get the data to show
            player = setup.getTeamName(context, MatchSetup.Team.T_ONE);
            opponent = setup.getTeamName(context, MatchSetup.Team.T_TWO);
            playedLevel = match.getLevelTitle(level, context);
            playerScore = null != pointOne ? pointOne.displayString(context) : context.getString(R.string.display_zero);
            opponentScore = null != pointTwo ? pointTwo.displayString(context) : context.getString(R.string.display_zero);
            buttonVisibility = View.VISIBLE;
        }
        else if (preparedSetup != null) {
            // there is no match, but there are settings, jump here instead
            sport = preparedSetup.getSport();
            intent.putExtra(ActivityMain.INITIAL_FRAGEMENT, R.id.navigation_settings);
            playedLevel = context.getString(R.string.match_not_started);
            player = preparedSetup.getTeamName(context, MatchSetup.Team.T_ONE);
            opponent = preparedSetup.getTeamName(context, MatchSetup.Team.T_TWO);
        }

        // and setup this display now
        remoteViews.setTextViewText(R.id.matchWinnerTitle, player);
        remoteViews.setTextViewText(R.id.matchLoserTitle, opponent);
        remoteViews.setTextViewText(R.id.matchPlayedLevel, playedLevel);
        remoteViews.setTextViewText(R.id.matchWinnerScore, playerScore);
        remoteViews.setTextViewText(R.id.matchLoserScore, opponentScore);

        // set the color for the widget
        //remoteViews.setInt(R.id.main_layout, "setBackgroundColor", R.color.colorPrimaryDark);

        // Set the image for the current sport
        if (null != sport) {
            //remoteViews.setImageViewBitmap(R.id.widgetTitleImageView, Application.GetBitmapFromAssets(sport.imageFilename, context));
            remoteViews.setImageViewResource(R.id.widgetTitleImageView, sport.iconRes);
        }
        else {
            remoteViews.setImageViewResource(R.id.widgetTitleImageView, R.mipmap.ic_launcher_round);
        }

        // set the button icons correctly
        remoteViews.setImageViewResource(R.id.widgetActionOneButton, actionOneIcon);
        remoteViews.setImageViewResource(R.id.widgetActionTwoButton, actionTwoIcon);
        remoteViews.setImageViewResource(R.id.widgetUndoButon, R.drawable.ic_undo_black_24dp);
        remoteViews.setImageViewResource(R.id.widgetAnnounceButton, R.drawable.ic_volume_up_black_24dp);

        // set the visibility of the buttons
        remoteViews.setViewVisibility(R.id.widgetActionOneButton, buttonVisibility);
        remoteViews.setViewVisibility(R.id.widgetActionTwoButton, buttonVisibility);
        remoteViews.setViewVisibility(R.id.widgetUndoButon, buttonVisibility);
        remoteViews.setViewVisibility(R.id.widgetAnnounceButton, buttonVisibility);
        /*
        remoteViews.setInt(R.id.widgetActionOneButton, "setBackgroundResource", R.drawable.background_details);
        remoteViews.setInt(R.id.widgetActionTwoButton, "setBackgroundResource", R.drawable.background_details);
        remoteViews.setInt(R.id.widgetUndoButon, "setBackgroundResource", R.drawable.background_details);
        remoteViews.setInt(R.id.widgetAnnounceButton, "setBackgroundResource", R.drawable.background_details);
        */
        // Register an onClickListener to take the use to the main activity on clicking
        PendingIntent mainIntent = PendingIntent.getActivity(context, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widgetTitleImageView, mainIntent);
        // setup the clicking actions correctly
        remoteViews.setOnClickPendingIntent(R.id.widgetActionOneButton, MatchNotification.ActionIntent.K_ACTION_ONE.getIntent(context));
        remoteViews.setOnClickPendingIntent(R.id.widgetActionTwoButton, MatchNotification.ActionIntent.K_ACTION_TWO.getIntent(context));
        remoteViews.setOnClickPendingIntent(R.id.widgetUndoButon, MatchNotification.ActionIntent.K_ACTION_UNDO.getIntent(context));
        remoteViews.setOnClickPendingIntent(R.id.widgetAnnounceButton, MatchNotification.ActionIntent.K_ACTION_ANNOUNCE.getIntent(context));

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
