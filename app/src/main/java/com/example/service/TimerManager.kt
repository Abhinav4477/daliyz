package com.example.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ActiveTimerState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val startTimestamp: Long = 0L,
    val lastResumeTimestamp: Long = 0L,
    val accumulatedDurationMillis: Long = 0L,
    val currentElapsedMillis: Long = 0L
) {
    val formattedDuration: String
        get() = formatDuration(currentElapsedMillis)

    companion object {
        fun formatDuration(millis: Long): String {
            val totalSeconds = (millis / 1000).coerceAtLeast(0)
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        }
    }
}

object TimerManager {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var tickerJob: Job? = null

    private val _timerState = MutableStateFlow(ActiveTimerState())
    val timerState: StateFlow<ActiveTimerState> = _timerState.asStateFlow()

    fun start() {
        val now = System.currentTimeMillis()
        _timerState.value = ActiveTimerState(
            isRunning = true,
            isPaused = false,
            startTimestamp = now,
            lastResumeTimestamp = now,
            accumulatedDurationMillis = 0L,
            currentElapsedMillis = 0L
        )
        startTicker()
    }

    fun pause() {
        val current = _timerState.value
        if (!current.isRunning || current.isPaused) return

        val now = System.currentTimeMillis()
        val additional = (now - current.lastResumeTimestamp).coerceAtLeast(0L)
        val newAccumulated = current.accumulatedDurationMillis + additional

        _timerState.value = current.copy(
            isPaused = true,
            accumulatedDurationMillis = newAccumulated,
            currentElapsedMillis = newAccumulated
        )
        stopTicker()
    }

    fun resume() {
        val current = _timerState.value
        if (!current.isRunning || !current.isPaused) return

        val now = System.currentTimeMillis()
        _timerState.value = current.copy(
            isPaused = false,
            lastResumeTimestamp = now
        )
        startTicker()
    }

    /**
     * Stops the timer and returns Triple(startTime, endTime, durationMillis)
     */
    fun stop(): Triple<Long, Long, Long>? {
        val current = _timerState.value
        if (!current.isRunning && current.currentElapsedMillis == 0L) return null

        stopTicker()
        val now = System.currentTimeMillis()
        val finalElapsed = if (current.isPaused) {
            current.accumulatedDurationMillis
        } else {
            current.accumulatedDurationMillis + (now - current.lastResumeTimestamp).coerceAtLeast(0L)
        }

        val startTime = current.startTimestamp
        val endTime = now

        _timerState.value = ActiveTimerState() // Reset
        return Triple(startTime, endTime, finalElapsed)
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                delay(1000)
                val current = _timerState.value
                if (current.isRunning && !current.isPaused) {
                    val now = System.currentTimeMillis()
                    val sessionElapsed = (now - current.lastResumeTimestamp).coerceAtLeast(0L)
                    val totalElapsed = current.accumulatedDurationMillis + sessionElapsed
                    _timerState.value = current.copy(currentElapsedMillis = totalElapsed)
                }
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }
}
