package com.example.data.repository

import com.example.data.local.WorkSession
import com.example.data.local.WorkSessionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DayProductivity(
    val dayLabel: String, // Mon, Tue, etc.
    val dateKey: String, // YYYY-MM-DD
    val totalHours: Float,
    val totalMillis: Long,
    val isToday: Boolean,
    val targetMet: Boolean
)

data class WeeklySummary(
    val days: List<DayProductivity>,
    val totalHours: Float,
    val averageDailyHours: Float,
    val daysGoalMetCount: Int,
    val weekRangeLabel: String
)

class WorkRepository(
    private val workSessionDao: WorkSessionDao,
    private val preferenceRepository: PreferenceRepository
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    val allSessions: Flow<List<WorkSession>> = workSessionDao.getAllSessions()
        .flowOn(Dispatchers.IO)

    fun getTodayDateKey(): String {
        return dateFormat.format(Date())
    }

    fun getTodaySessions(): Flow<List<WorkSession>> {
        return workSessionDao.getSessionsForDate(getTodayDateKey())
            .flowOn(Dispatchers.IO)
    }

    fun getTodayTotalDuration(): Flow<Long?> {
        return workSessionDao.getTodayTotalDuration(getTodayDateKey())
            .flowOn(Dispatchers.IO)
    }

    /**
     * Reactively computes the weekly productivity summary from all sessions.
     */
    fun getWeeklySummaryFlow(targetHours: Float = 3.0f): Flow<WeeklySummary> {
        return allSessions.map { sessions ->
            calculateWeeklySummary(sessions, targetHours)
        }.flowOn(Dispatchers.Default)
    }

    suspend fun saveSession(startTime: Long, endTime: Long, notes: String = ""): Long {
        return withContext(Dispatchers.IO) {
            val duration = (endTime - startTime).coerceAtLeast(0L)
            val dateKey = dateFormat.format(Date(startTime))
            val session = WorkSession(
                startTime = startTime,
                endTime = endTime,
                durationMillis = duration,
                dateKey = dateKey,
                notes = notes
            )
            val id = workSessionDao.insertSession(session)
            // Automatically remove any data older than 3 months permanently
            pruneSessionsOlderThan90Days()
            id
        }
    }

    suspend fun deleteSession(id: Long) {
        withContext(Dispatchers.IO) {
            workSessionDao.deleteSessionById(id)
        }
    }

    /**
     * Permanently deletes any sessions older than 90 days (3 months).
     */
    suspend fun pruneSessionsOlderThan90Days(): Int {
        return withContext(Dispatchers.IO) {
            val ninetyDaysMillis = 90L * 24L * 60L * 60L * 1000L
            val cutoffTimestamp = System.currentTimeMillis() - ninetyDaysMillis
            val deletedCount = workSessionDao.deleteSessionsOlderThan(cutoffTimestamp)
            preferenceRepository.updateLastPruneTimestamp(System.currentTimeMillis())
            deletedCount
        }
    }

    fun calculateWeeklySummary(
        allSessions: List<WorkSession>,
        targetHours: Float = 3.0f
    ): WeeklySummary {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val weekStartLabel = SimpleDateFormat("MMM d", Locale.US).format(calendar.time)
        val todayKey = getTodayDateKey()
        val dayNameFormat = SimpleDateFormat("EEE", Locale.US)

        val sessionDurationByDateKey = allSessions.groupBy { it.dateKey }
            .mapValues { entry -> entry.value.sumOf { it.durationMillis } }

        val days = mutableListOf<DayProductivity>()
        var weekTotalMillis = 0L
        var goalsMet = 0

        for (i in 0 until 7) {
            val dayKey = dateFormat.format(calendar.time)
            val dayName = dayNameFormat.format(calendar.time)

            val dayMillis = sessionDurationByDateKey[dayKey] ?: 0L
            val dayHours = dayMillis / 3600000f
            val isMet = dayHours >= targetHours && dayHours > 0f

            if (isMet) goalsMet++
            weekTotalMillis += dayMillis

            days.add(
                DayProductivity(
                    dayLabel = dayName,
                    dateKey = dayKey,
                    totalHours = dayHours,
                    totalMillis = dayMillis,
                    isToday = (dayKey == todayKey),
                    targetMet = isMet
                )
            )

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val weekEndLabel = SimpleDateFormat("MMM d", Locale.US).format(calendar.time)

        val totalHours = weekTotalMillis / 3600000f
        return WeeklySummary(
            days = days,
            totalHours = totalHours,
            averageDailyHours = totalHours / 7f,
            daysGoalMetCount = goalsMet,
            weekRangeLabel = "$weekStartLabel - $weekEndLabel"
        )
    }
}
