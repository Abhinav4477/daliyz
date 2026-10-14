package com.example.ui.tracker

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.DailyzApplication
import com.example.data.local.WorkSession
import com.example.data.repository.DayProductivity
import com.example.data.repository.WeeklySummary
import com.example.data.repository.WorkRepository
import com.example.service.ActiveTimerState
import com.example.service.ReminderScheduler
import com.example.service.TimerManager
import com.example.service.WorkTimerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WorkProgressState(
    val todayCompletedSavedMillis: Long = 0L,
    val liveActiveElapsedMillis: Long = 0L,
    val totalTodayMillis: Long = 0L,
    val targetMillis: Long = 3 * 3600 * 1000L, // 3 Hours Target
    val baseProgress: Float = 0f, // 0.0 to 1.0 (fills the 3h goal)
    val isGoalMet: Boolean = false,
    val overtimeMillis: Long = 0L,
    val overtimeProgress: Float = 0f // Progress bar stacked over the completed 3h bar!
)

class WorkTrackerViewModel : ViewModel() {

    private val workRepository = DailyzApplication.instance.workRepository
    private val preferenceRepository = DailyzApplication.instance.preferenceRepository

    val activeTimerState: StateFlow<ActiveTimerState> = TimerManager.timerState

    val allSessions: StateFlow<List<WorkSession>> = workRepository.allSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val weeklySummary: StateFlow<WeeklySummary> = workRepository.getWeeklySummaryFlow(targetHours = 3.0f)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeeklySummary(emptyList(), 0f, 0f, 0, "")
        )

    val reminderEnabled: StateFlow<Boolean> = preferenceRepository.reminderEnabled
    val reminderHour: StateFlow<Int> = preferenceRepository.reminderHour
    val reminderMinute: StateFlow<Int> = preferenceRepository.reminderMinute
    val themeMode: StateFlow<String> = preferenceRepository.themeMode

    private val _lastPruneNotice = MutableStateFlow<String?>(null)
    val lastPruneNotice: StateFlow<String?> = _lastPruneNotice.asStateFlow()

    // Real-time calculation of today's total work time, base 3h progress, and overtime progress bar!
    val workProgress: StateFlow<WorkProgressState> = combine(
        workRepository.getTodayTotalDuration(),
        TimerManager.timerState
    ) { savedTodayDuration, liveState ->
        val savedMillis = savedTodayDuration ?: 0L
        val activeMillis = liveState.currentElapsedMillis
        val totalToday = savedMillis + activeMillis
        val target = 3 * 3600 * 1000L // 3 Hours = 10,800,000 ms

        val baseProgress = (totalToday.toFloat() / target).coerceIn(0f, 1f)
        val isGoalMet = totalToday >= target
        val overtimeMillis = if (totalToday > target) totalToday - target else 0L

        // Overtime progress: starts at 0% when target is reached and grows as user overworks!
        // We compute overtime relative to the target (or cycle up to another 3h loop)
        val overtimeProgress = if (isGoalMet) {
            // E.g. If overtime is 1.5 hrs out of 3h target, progress is 0.5f (50% stacked bar)
            (overtimeMillis.toFloat() / target).coerceAtLeast(0.02f)
        } else {
            0f
        }

        WorkProgressState(
            todayCompletedSavedMillis = savedMillis,
            liveActiveElapsedMillis = activeMillis,
            totalTodayMillis = totalToday,
            targetMillis = target,
            baseProgress = baseProgress,
            isGoalMet = isGoalMet,
            overtimeMillis = overtimeMillis,
            overtimeProgress = overtimeProgress
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WorkProgressState()
    )

    fun startWork(context: Context) {
        WorkTimerService.startService(context)
    }

    fun pauseWork(context: Context) {
        WorkTimerService.pauseService(context)
    }

    fun resumeWork(context: Context) {
        WorkTimerService.resumeService(context)
    }

    fun stopWork(context: Context) {
        WorkTimerService.stopService(context)
    }

    fun updateReminder(context: Context, enabled: Boolean, hour: Int, minute: Int) {
        preferenceRepository.setReminderEnabled(enabled)
        preferenceRepository.setReminderTime(hour, minute)

        if (enabled) {
            ReminderScheduler.scheduleReminder(context, hour, minute)
        } else {
            ReminderScheduler.cancelReminder(context)
        }
    }

    fun setThemeMode(mode: String) {
        preferenceRepository.setThemeMode(mode)
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch {
            workRepository.deleteSession(id)
        }
    }

    fun manuallyPruneOldHistory() {
        viewModelScope.launch {
            val pruned = workRepository.pruneSessionsOlderThan90Days()
            _lastPruneNotice.value = if (pruned > 0) {
                "Cleaned up $pruned records older than 3 months."
            } else {
                "History is completely up to date (no records older than 3 months)."
            }
        }
    }

    fun clearPruneNotice() {
        _lastPruneNotice.value = null
    }
}
