package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return

        when (action) {
            ACTION_START -> {
                WorkTimerService.startService(context)
            }
            ACTION_PAUSE -> {
                WorkTimerService.pauseService(context)
            }
            ACTION_RESUME -> {
                WorkTimerService.resumeService(context)
            }
            ACTION_STOP -> {
                WorkTimerService.stopService(context)
            }
        }
    }

    companion object {
        const val ACTION_START = "com.example.dailyz.action.NOTIFICATION_START"
        const val ACTION_PAUSE = "com.example.dailyz.action.NOTIFICATION_PAUSE"
        const val ACTION_RESUME = "com.example.dailyz.action.NOTIFICATION_RESUME"
        const val ACTION_STOP = "com.example.dailyz.action.NOTIFICATION_STOP"
    }
}
