package uk.co.darkerwaters.scorepal.application;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.controllers.Controller;
import uk.co.darkerwaters.scorepal.score.MatchWriter;
import uk.co.darkerwaters.scorepal.score.bluetooth.BluetoothMatch;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.MatchSettings;
import uk.co.darkerwaters.scorepal.score.base.Sport;
import uk.co.darkerwaters.scorepal.settings.SettingsControl;

public class GamePlayNotification {

    private static final int K_MATCH_NOTIFICATION_ID = 132;

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
    private final Application application;
    private final NotificationManagerCompat notificationManager;

    private Controller.ControllerAction actionOne, actionTwo;

    public GamePlayNotification(Context context, Application application) {
        this.context = context;
        this.application = application;
        // create the channel if required
        createNotificationChannel();
        // get the manager
        this.notificationManager = NotificationManagerCompat.from(this.context);

        // our action intents are always the same so we can create them here
        this.actionOneIntent = ActionIntent.K_ACTION_ONE.getIntent(this.context);
        this.actionTwoIntent = ActionIntent.K_ACTION_TWO.getIntent(this.context);
        this.undoIntent = ActionIntent.K_ACTION_UNDO.getIntent(this.context);
        this.announceIntent = ActionIntent.K_ACTION_ANNOUNCE.getIntent(this.context);
    }

    public void close() {
        // cancel the message when closed
        this.notificationManager.cancelAll();
        // and update the widget
        WidgetBroadcastReceiver.UpdateAppWidgets(application, context);
    }

    public void createMatchNotification(MatchSettings settings, Match match) {
        if (match instanceof BluetoothMatch) {
            // show the data for the contained match rather than the match itself.
            settings = ((BluetoothMatch)match).getContainedMatchSettings();
            match = ((BluetoothMatch)match).getContainedMatch();
        }
        if (null == settings || null == match) {
            // no data or match not playing
            this.notificationManager.cancelAll();
            return;
        }
        // show the alert for this current sport / match
        Sport sport = settings.getSport();

        // Create an explicit intent for an Activity in your app - jump to the playing activity
        // for the sport - will be the latest anyway as this is what's showing when this pops up
        Intent intent = new Intent(this.context, sport.playActivityClass);
        PendingIntent pendingIntent = PendingIntent.getActivity(this.context, 0, intent, 0);

        int actionOneIcon, actionTwoIcon;
        if (new SettingsControl(this.application).getIsControlTeams()) {
            this.actionOne = Controller.ControllerAction.PointTeamOne;
            this.actionTwo = Controller.ControllerAction.PointTeamTwo;
            actionOneIcon = R.drawable.ic_team_one;
            actionTwoIcon = R.drawable.ic_team_two;
        }
        else {
            this.actionOne = Controller.ControllerAction.PointServer;
            this.actionTwo = Controller.ControllerAction.PointReceiver;
            actionOneIcon = R.drawable.ic_tennis_serve;
            actionTwoIcon = R.drawable.ic_tennis_receive;
        }

        // create the media player style notification (using media buttons to add score / undo)
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.context, Log.K_APPLICATION)
                // Show controls on lock screen even when user hides sensitive content.
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_tennis)
                .setContentTitle(sport.getTitle(this.context))
                .setContentText(match.getDescription(MatchWriter.DescriptionLevel.ONELINEBOTTOM, this.context))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(match.getDescription(MatchWriter.DescriptionLevel.TWOLINE, this.context)))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                // Add 'media' control buttons that invoke intents in your media service
                .addAction(actionOneIcon, this.actionOne.toStringShort(this.context), this.actionOneIntent)
                .addAction(actionTwoIcon, this.actionTwo.toStringShort(this.context), this.actionTwoIntent)
                .addAction(R.drawable.ic_baseline_undo, Controller.ControllerAction.UndoLastPoint.toStringShort(this.context), this.undoIntent)
                .addAction(R.drawable.ic_baseline_volume_up, Controller.ControllerAction.AnnouncePoints.toStringShort(this.context), this.announceIntent)
                // Apply the media style template
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2))
                        //.setMediaSession(mediaSession.getSessionToken()))
                .setLargeIcon(Application.GetBitmapFromAssets(sport.imageFilename, this.context))
                .setOngoing(true);
        /*
        // build notification - standard with buttons
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.context, Log.K_APPLICATION)
                .setSmallIcon(R.drawable.ic_tennis)
                .setContentTitle(sport.getTitle(this.context))
                .setContentText(match.getDescriptionBrief(this.context))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(match.getDescriptionShort(this.context)))
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_tennis_serve, this.actionOne.toStringShort(this.context), this.actionOneIntent)
                .addAction(R.drawable.ic_tennis_receive, this.actionTwo.toStringShort(this.context), this.actionTwoIntent)
                .addAction(R.drawable.ic_baseline_undo, Controller.ControllerAction.UndoLastPoint.toStringShort(this.context), this.undoIntent)
                .addAction(R.drawable.ic_baseline_volume_up, Controller.ControllerAction.AnnouncePoints.toStringShort(this.context), this.announceIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setLargeIcon(Application.GetBitmapFromAssets(sport.imageFilename, this.context))
                .setOngoing(true);
                */
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(K_MATCH_NOTIFICATION_ID, builder.build());

        // and update the widget
        WidgetBroadcastReceiver.UpdateAppWidgets(application, context);
    }

    public static void KillOldNotifications(Context context) {
        // this is called when there is potentially no manager in our memory, but there might
        // be a notification from and old killed app - create the channel to kill any remaining
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancelAll();
    }

    public boolean onGamePlayNotificationReceived(int extraNotificationId) {
        // got the message back that the user pressed a button on the notification, deal with this here
        GamePlayCommunicator communicator = GamePlayCommunicator.GetActiveCommunicator();
        if (null == communicator || null == communicator.getCurrentMatch()) {
            // there is no communicator, or there is no current match, this is no good
            close();
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
            communicator.onControllerInput(selectedAction);
            return true;
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = this.context.getString(R.string.channel_name);
            String description = this.context.getString(R.string.channel_description);
            NotificationChannel channel = new NotificationChannel(Log.K_APPLICATION, name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = this.context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
