package com.example.markenteder.locktalk;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessaging extends FirebaseMessagingService{

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String nTitle = remoteMessage.getNotification().getTitle();
        String nMessage = remoteMessage.getNotification().getBody();

        String clickAction = remoteMessage.getNotification().getClickAction();

        String fromUserId = remoteMessage.getData().get("fromUserId");

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(nTitle)
                    .setContentText(nMessage);

        Intent resultIntent = new Intent(clickAction);
        resultIntent.putExtra("userId", fromUserId);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);


        int notificationId = (int) System.currentTimeMillis();

        NotificationManager notifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notifyManager.notify(notificationId, builder.build());

    }
}
