package uk.co.darkerwaters.scorepal.application;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.controllers.Controller;
import uk.co.darkerwaters.scorepal.data.Match;
import uk.co.darkerwaters.scorepal.dataui.MatchWriter;
import uk.co.darkerwaters.scorepal.points.Sport;
import uk.co.darkerwaters.scorepal.ui.matchplay.ActivityPlayMatch;

import static androidx.core.app.NotificationCompat.GROUP_ALERT_SUMMARY;

public class MatchNotification {

    public static final int K_MATCH_NOTIFICATION_ID = 132;

    enum ActionIntent {
        K_ACTION_ONE("actionOne", 0),
        K_ACTION_TWO("actionTwo", 1),
        K_ACTION_UNDO("actionUndo", 2),
        K_ACTION_ANNOUNCE("actionAnnounce", 3);

        final int val;
        final String string;

        ActionIntent(String string, int val) {
            this.string = string;
            this.val = val;
        }

        public PendingIntent getIntent(Context context) {
            Intent intent = new Intent(context, NotificationBroadcastReceiver.class);
            intent.setAction(this.string);
            intent.putExtra(Notification.EXTRA_NOTIFICATION_ID, this.val);
            return PendingIntent.getBroadcast(context, 0, intent, 0);
        }
    }

    private final Context context;

    private final PendingIntent actionOneIntent;
    private final PendingIntent actionTwoIntent;
    private final PendingIntent undoIntent;
    private final PendingIntent announceIntent;
    private final NotificationManagerCompat notificationManager;

    private Controller.ControllerAction actionOne, actionTwo;

    public MatchNotification(Context context) {
        // create this class
        this.context = context;

        // create the channel if required
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(this.context.getClass().getName(), Log.K_APPLICATION);
        }
        // get the manager
        this.notificationManager = NotificationManagerCompat.from(this.context);

        // our action intents are always the same so we can create them here
        this.actionOneIntent = ActionIntent.K_ACTION_ONE.getIntent(this.context);
        this.actionTwoIntent = ActionIntent.K_ACTION_TWO.getIntent(this.context);
        this.undoIntent = ActionIntent.K_ACTION_UNDO.getIntent(this.context);
        this.announceIntent = ActionIntent.K_ACTION_ANNOUNCE.getIntent(this.context);
    }

    public void close() {
        // and update the widget
        WidgetBroadcastReceiver.UpdateAppWidgets(context);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName) {
        NotificationChannel chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }

    public NotificationCompat.Builder createNoficationBuilder(Match activeMatch) {
        int actionOneIcon, actionTwoIcon;
        ApplicationPreferences preferences = ApplicationState.Instance().getPreferences();
        if (preferences.getIsControlTeams()) {
            this.actionOne = Controller.ControllerAction.PointTeamOne;
            this.actionTwo = Controller.ControllerAction.PointTeamTwo;
            actionOneIcon = R.drawable.ic_team_one_black_24dp;
            actionTwoIcon = R.drawable.ic_team_two_black_24dp;
        }
        else {
            this.actionOne = Controller.ControllerAction.PointServer;
            this.actionTwo = Controller.ControllerAction.PointReceiver;
            actionOneIcon = R.drawable.ic_player_serving_black_24dp;
            actionTwoIcon = R.drawable.ic_player_receiving_backhand_black_24dp;
        }
        Sport sport = null != activeMatch ? activeMatch.getSport() : Sport.TENNIS;
        // Create an explicit intent for an Activity in your app - jump to the playing activity
        // for the sport - will be the latest anyway as this is what's showing when this pops up
        Intent intent = new Intent(this.context, ActivityPlayMatch.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this.context, 0, intent, 0);

        // create the media player style notification (using media buttons to add score / undo)
        return new NotificationCompat.Builder(context, this.context.getClass().getName())
                // Show controls on lock screen even when user hides sensitive content.
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(sport.iconRes)
                .setContentTitle(context.getString(sport.strRes))
                .setContentText(activeMatch.getDescription(MatchWriter.DescriptionLevel.ONELINEBOTTOM, this.context))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(activeMatch.getDescription(MatchWriter.DescriptionLevel.TWOLINE, this.context)))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setGroupAlertBehavior(GROUP_ALERT_SUMMARY)
                .setGroup("Scorepal")
                .setGroupSummary(false)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                // Add 'media' control buttons that invoke intents in your media service
                .addAction(actionOneIcon, this.actionOne.toStringShort(this.context), this.actionOneIntent)
                .addAction(actionTwoIcon, this.actionTwo.toStringShort(this.context), this.actionTwoIntent)
                .addAction(R.drawable.ic_undo_black_24dp, Controller.ControllerAction.UndoLastPoint.toStringShort(this.context), this.undoIntent)
                .addAction(R.drawable.ic_volume_up_black_24dp, Controller.ControllerAction.AnnouncePoints.toStringShort(this.context), this.announceIntent)
                // Apply the media style template
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2))
                //.setMediaSession(mediaSession.getSessionToken()))
                .setLargeIcon(Application.GetBitmapFromAssets(sport.imageFilename, this.context))
                .setOngoing(true);
    }

    public void updateNotification(Match activeMatch) {
        if (null != activeMatch) {
            notificationManager.notify(K_MATCH_NOTIFICATION_ID, createNoficationBuilder(activeMatch).build());
        } else {
            // there is no active match - kill the notification
            KillOldNotifications(context);
        }

        // and update the widget
        WidgetBroadcastReceiver.UpdateAppWidgets(context);
    }

    public static void KillOldNotifications(Context context) {
        // this is called when there is potentially no manager in our memory, but there might
        // be a notification from and old killed app - create the channel to kill any remaining
        if (null != context) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancelAll();
        }
    }

    public boolean onGamePlayNotificationReceived(int extraNotificationId) {
        // got the message back that the user pressed a button on the notification, deal with this here
        MatchService service = MatchService.GetRunningService();
        if (null == service || null == service.getActiveMatch()) {
            // there is no service, or there is no current match, this is no good
            KillOldNotifications(service);
            return false;
        }
        else {
            Controller.ControllerAction selectedAction;
            switch (extraNotificationId) {
                case 0:
                    selectedAction = this.actionOne;
                    break;
                case 1:
                    selectedAction = this.actionTwo;
                    break;
                case 2:
                    selectedAction = Controller.ControllerAction.UndoLastPoint;
                    break;
                default:
                    Log.error("unexpected notification button press with id " + extraNotificationId);
                case 3:
                    selectedAction = Controller.ControllerAction.AnnouncePoints;
                    break;
            }
            // send this to the communicator like it came from anywhere
            service.onControllerInteraction(selectedAction);
            return true;
        }
    }
}
