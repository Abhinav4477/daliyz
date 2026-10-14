package com.example.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.DailyzApplication
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class WorkTimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var stateCollectJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START

        when (action) {
            ACTION_START -> {
                if (!TimerManager.timerState.value.isRunning) {
                    TimerManager.start()
                }
                startForegroundServiceWithNotification()
                observeTimerState()
            }
            ACTION_PAUSE -> {
                TimerManager.pause()
                updateNotification()
            }
            ACTION_RESUME -> {
                TimerManager.resume()
                updateNotification()
            }
            ACTION_STOP -> {
                handleStopAndSave()
            }
        }

        return START_STICKY
    }

    private fun startForegroundServiceWithNotification() {
        val notification = buildTimerNotification(TimerManager.timerState.value)
        val foregroundType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            foregroundType
        )
    }

    private fun observeTimerState() {
        stateCollectJob?.cancel()
        stateCollectJob = serviceScope.launch {
            TimerManager.timerState.collect { state ->
                if (state.isRunning || state.isPaused) {
                    updateNotification()
                }
            }
        }
    }

    private fun updateNotification() {
        val state = TimerManager.timerState.value
        val notification = buildTimerNotification(state)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildTimerNotification(state: ActiveTimerState): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val statusText = if (state.isPaused) "Paused" else "Tracking Work Time"
        val timeDisplay = state.formattedDuration

        val builder = NotificationCompat.Builder(this, DailyzApplication.CHANNEL_WORK_TIMER)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("dailyz • $timeDisplay")
            .setContentText("$statusText (3h daily target)")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)

        // Action Buttons
        if (state.isPaused) {
            val resumeIntent = PendingIntent.getBroadcast(
                this,
                101,
                Intent(this, NotificationActionReceiver::class.java).apply {
                    action = NotificationActionReceiver.ACTION_RESUME
                },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            builder.addAction(0, "Resume", resumeIntent)
        } else {
            val pauseIntent = PendingIntent.getBroadcast(
                this,
                102,
                Intent(this, NotificationActionReceiver::class.java).apply {
                    action = NotificationActionReceiver.ACTION_PAUSE
                },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            builder.addAction(0, "Pause", pauseIntent)
        }

        val stopIntent = PendingIntent.getBroadcast(
            this,
            103,
            Intent(this, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_STOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder.addAction(0, "Stop & Save", stopIntent)

        return builder.build()
    }

    private fun handleStopAndSave() {
        val sessionData = TimerManager.stop()
        if (sessionData != null) {
            val (startTime, endTime, _) = sessionData
            serviceScope.launch {
                DailyzApplication.instance.workRepository.saveSession(
                    startTime = startTime,
                    endTime = endTime,
                    notes = ""
                )
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        } else {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val NOTIFICATION_ID = 2001

        const val ACTION_START = "com.example.dailyz.action.START"
        const val ACTION_PAUSE = "com.example.dailyz.action.PAUSE"
        const val ACTION_RESUME = "com.example.dailyz.action.RESUME"
        const val ACTION_STOP = "com.example.dailyz.action.STOP"

        fun startService(context: Context) {
            val intent = Intent(context, WorkTimerService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, WorkTimerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun pauseService(context: Context) {
            val intent = Intent(context, WorkTimerService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun resumeService(context: Context) {
            val intent = Intent(context, WorkTimerService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }
    }
}
