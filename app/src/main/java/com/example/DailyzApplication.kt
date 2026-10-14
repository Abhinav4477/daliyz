package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.data.local.DailyzDatabase
import com.example.data.repository.NotesRepository
import com.example.data.repository.PreferenceRepository
import com.example.data.repository.WorkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DailyzApplication : Application() {

    lateinit var database: DailyzDatabase
        private set

    lateinit var workRepository: WorkRepository
        private set

    lateinit var notesRepository: NotesRepository
        private set

    lateinit var preferenceRepository: PreferenceRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = DailyzDatabase.getDatabase(this)
        preferenceRepository = PreferenceRepository(this)
        workRepository = WorkRepository(database.workSessionDao(), preferenceRepository)
        notesRepository = NotesRepository(database.noteDao())

        createNotificationChannels()

        // Permanent automated maintenance: purge any sessions older than 90 days / 3 months
        CoroutineScope(Dispatchers.IO).launch {
            workRepository.pruneSessionsOlderThan90Days()
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Ongoing timer channel (silent update)
            val timerChannel = NotificationChannel(
                CHANNEL_WORK_TIMER,
                "Work Tracker Ongoing Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live ongoing persistent notification while work timer is running"
                setShowBadge(false)
            }

            // High importance reminder channel (alerts user to start work)
            val reminderChannel = NotificationChannel(
                CHANNEL_WORK_REMINDER,
                "Workday Start Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminds you to start working at your scheduled workday time"
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(timerChannel)
            notificationManager.createNotificationChannel(reminderChannel)
        }
    }

    companion object {
        const val CHANNEL_WORK_TIMER = "dailyz_channel_timer"
        const val CHANNEL_WORK_REMINDER = "dailyz_channel_reminder"

        lateinit var instance: DailyzApplication
            private set
    }
}
