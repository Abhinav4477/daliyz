package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferenceRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("dailyz_prefs", Context.MODE_PRIVATE)

    private val _reminderEnabled = MutableStateFlow(prefs.getBoolean(KEY_REMINDER_ENABLED, true))
    val reminderEnabled: StateFlow<Boolean> = _reminderEnabled.asStateFlow()

    private val _reminderHour = MutableStateFlow(prefs.getInt(KEY_REMINDER_HOUR, 9))
    val reminderHour: StateFlow<Int> = _reminderHour.asStateFlow()

    private val _reminderMinute = MutableStateFlow(prefs.getInt(KEY_REMINDER_MINUTE, 0))
    val reminderMinute: StateFlow<Int> = _reminderMinute.asStateFlow()

    private val _dailyTargetHours = MutableStateFlow(prefs.getFloat(KEY_DAILY_TARGET_HOURS, 3.0f))
    val dailyTargetHours: StateFlow<Float> = _dailyTargetHours.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    fun setReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply()
        _reminderEnabled.value = enabled
    }

    fun setReminderTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt(KEY_REMINDER_HOUR, hour)
            .putInt(KEY_REMINDER_MINUTE, minute)
            .apply()
        _reminderHour.value = hour
        _reminderMinute.value = minute
    }

    fun setDailyTargetHours(hours: Float) {
        prefs.edit().putFloat(KEY_DAILY_TARGET_HOURS, hours).apply()
        _dailyTargetHours.value = hours
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeMode.value = mode
    }

    fun getLastPruneTimestamp(): Long = prefs.getLong(KEY_LAST_PRUNE, 0L)

    fun updateLastPruneTimestamp(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_PRUNE, timestamp).apply()
    }

    companion object {
        private const val KEY_REMINDER_ENABLED = "key_reminder_enabled"
        private const val KEY_REMINDER_HOUR = "key_reminder_hour"
        private const val KEY_REMINDER_MINUTE = "key_reminder_minute"
        private const val KEY_DAILY_TARGET_HOURS = "key_daily_target_hours"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_LAST_PRUNE = "key_last_prune"
    }
}
