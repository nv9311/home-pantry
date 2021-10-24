package com.example.homepantry.utilities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.homepantry.MainActivity;
import com.example.homepantry.R;

public class NotificationUtils {

    private static final String CHANNEL_ID = "NOTIFICATION_CHANNEL_REMINDERS";
    private static final int NOTIFICATION_ID = 55;

    public static void createNotification(Context context, String contentTitle, String contentText){

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_android_24)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(createNotificationChannel(context));
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());

    }
    private static NotificationChannel createNotificationChannel(Context context) {
        NotificationChannel channel = null;
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.notification_channel_name);
            String description = context.getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
        }
        return channel;
    }
}
