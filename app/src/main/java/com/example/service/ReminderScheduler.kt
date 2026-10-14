package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.DailyzApplication
import java.util.Calendar

object ReminderScheduler {

    private const val REQUEST_CODE_REMINDER = 9001

    fun scheduleReminder(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If scheduled time has already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val pendingIntent = getPendingIntent(context)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            // Fallback to inexact repeating if exact alarm permission is restricted
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = getPendingIntent(context)
        alarmManager.cancel(pendingIntent)
    }

    fun rescheduleNextDay(context: Context) {
        val prefs = DailyzApplication.instance.preferenceRepository
        if (prefs.reminderEnabled.value) {
            scheduleReminder(context, prefs.reminderHour.value, prefs.reminderMinute.value)
        }
    }

    private fun getPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, WorkReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_REMINDER,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}
