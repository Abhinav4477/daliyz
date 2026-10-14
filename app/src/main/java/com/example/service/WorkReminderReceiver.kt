package com.example.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.DailyzApplication
import com.example.MainActivity
import com.example.R

class WorkReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        // If the user is already working, do not disturb them with a reminder to start
        if (TimerManager.timerState.value.isRunning) {
            // Re-schedule for next day
            ReminderScheduler.rescheduleNextDay(context)
            return
        }

        // Show push notification reminder with direct "Start Work" action button
        showReminderNotification(context)

        // Schedule next day's reminder
        ReminderScheduler.rescheduleNextDay(context)
    }

    private fun showReminderNotification(context: Context) {
        val openAppIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Action button to start work directly from notification tray!
        val startWorkIntent = PendingIntent.getBroadcast(
            context,
            201,
            Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_START
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, DailyzApplication.CHANNEL_WORK_REMINDER)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("dailyz • Time to Start Work!")
            .setContentText("Your scheduled work reminder has arrived. Ready to hit your 3-hour goal?")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Your scheduled work reminder has arrived. Tap 'Start Work' below to begin your workday timer and start tracking progress toward your 3-hour goal.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent)
            .addAction(0, "Start Work", startWorkIntent)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID_REMINDER, notification)
    }

    companion object {
        const val NOTIFICATION_ID_REMINDER = 3001
    }
}
