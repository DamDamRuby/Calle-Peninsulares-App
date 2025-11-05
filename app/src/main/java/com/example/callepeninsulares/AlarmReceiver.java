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
        String room = intent.getStringExtra("room");
        String startTime = intent.getStringExtra("startTime");
        String meetLink = intent.getStringExtra("meetLink");
        int scheduleId = intent.getIntExtra("scheduleId", -1);

        // 🟣 Decide where to redirect when user taps the notification
        Intent notificationIntent;

        if (meetLink != null && !meetLink.trim().isEmpty()) {
            notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(meetLink));
        } else {

            notificationIntent = new Intent(context, activity_schedule_list.class);
        }

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 🔔 Notification setup
        String channelId = "reminder_channel";
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Schedule Alarm Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        // 🪄 Build the notification message depending on type
        String notificationTitle = "Upcoming Class: " + subject;
        String notificationText;

        if (meetLink != null && !meetLink.trim().isEmpty()) {
            notificationText = "Tap to join your online class on Google Meet.";
        } else {
            notificationText = "Room: " + room + " | Starts at: " + startTime;
        }

        // 📱 Build the actual notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(alarmSound)
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());

        if (scheduleId != -1) {
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            boolean deleted = dbHelper.deleteSchedule(scheduleId);
            if (deleted) {
                // Optionally refresh list immediately
                Intent refreshIntent = new Intent("com.example.callepeninsulares.REFRESH_SCHEDULES");
                context.sendBroadcast(refreshIntent);
            }
        }
    }
}
