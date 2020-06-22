package uk.co.darkerwaters.scorepal.application;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int extraNotificationId = intent.getIntExtra(Notification.EXTRA_NOTIFICATION_ID, -1);
        // pass this back to the service to process the message from the notification they posted
        MatchService service = MatchService.GetRunningService();
        if (null != service) {
            // get the notification on this
            MatchNotification notification = service.getNotification();
            if (null == notification || !notification.onGamePlayNotificationReceived(extraNotificationId)) {
                Log.error("received a notification for the match, but the communicator has gone");
                // we need to kill any notifications hanging around from before
                MatchNotification.KillOldNotifications(context);
            }
        }
    }
}
