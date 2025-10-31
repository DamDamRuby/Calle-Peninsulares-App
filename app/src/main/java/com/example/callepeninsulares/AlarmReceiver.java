package com.example.callepeninsulares;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String subject = intent.getStringExtra("subject");
        String building = intent.getStringExtra("building");


        String message;
        if (subject.contains("Starting in")) {
            message = "Your " + subject.replace("(Starting", "Class").trim() + " will begin soon at " + building + "!";
        } else if (subject.contains("Class time now!")) {
            message = "It's time for your " + subject.replace(" Class time now!", " Class").trim() + " Subject at " + building + "!";
        } else {
            message = "Reminder for " + subject + " at " + building;
        }


        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        String channelId = "reminder_channel";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Class Reminders", NotificationManager.IMPORTANCE_HIGH);channel.setDescription("Reminders for your class schedules");notificationManager.createNotificationChannel(channel);
        }


        Intent openAppIntent = new Intent(context, Home.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Class Reminder")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(alarmSound)
                .setVibrate(new long[]{0, 500, 500, 500, 1000})
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);


        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
