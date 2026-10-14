package com.example

import com.example.service.ActiveTimerState
import com.example.util.CsvCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testCsvCalculator_commaSeparated() {
    val input = "10.5, 20.5, 30"
    val summary = CsvCalculator.calculate(input)

    assertEquals(3, summary.count)
    assertEquals(61.0, summary.sum, 0.001)
    assertEquals(20.333, summary.average, 0.01)
    assertEquals("61", summary.formattedSum)
  }

  @Test
  fun testCsvCalculator_newlineAndCurrencySymbols() {
    val input = "$100\n$250.50\n$50"
    val summary = CsvCalculator.calculate(input)

    assertEquals(3, summary.count)
    assertEquals(400.50, summary.sum, 0.001)
    assertEquals(133.50, summary.average, 0.01)
    assertEquals("400.50", summary.formattedSum)
  }

  @Test
  fun testCsvCalculator_emptyOrMalformed() {
    val input = "abc, xyz, \n, "
    val summary = CsvCalculator.calculate(input)

    assertEquals(0, summary.count)
    assertEquals(0.0, summary.sum, 0.001)
  }

  @Test
  fun testOvertimeCalculation_withinGoal() {
    val targetMillis = 3 * 3600 * 1000L // 3 Hours
    val loggedMillis = 10800000L / 2 // 1.5 Hours

    val baseProgress = (loggedMillis.toFloat() / targetMillis).coerceIn(0f, 1f)
    val isGoalMet = loggedMillis >= targetMillis
    val overtimeMillis = if (loggedMillis > targetMillis) loggedMillis - targetMillis else 0L

    assertEquals(0.5f, baseProgress, 0.001f)
    assertTrue(!isGoalMet)
    assertEquals(0L, overtimeMillis)
  }

  @Test
  fun testOvertimeCalculation_overtimeStartsStackedProgress() {
    val targetMillis = 3 * 3600 * 1000L // 3 Hours (10,800,000 ms)
    val loggedMillis = targetMillis + (45 * 60 * 1000L) // 3 Hours 45 Min

    val baseProgress = (loggedMillis.toFloat() / targetMillis).coerceIn(0f, 1f)
    val isGoalMet = loggedMillis >= targetMillis
    val overtimeMillis = if (loggedMillis > targetMillis) loggedMillis - targetMillis else 0L
    val overtimeProgress = (overtimeMillis.toFloat() / targetMillis).coerceIn(0f, 1f)

    assertEquals(1.0f, baseProgress, 0.001f) // Base progress is 100% full
    assertTrue(isGoalMet)
    assertEquals(45 * 60 * 1000L, overtimeMillis)
    assertTrue(overtimeProgress > 0f)
  }

  @Test
  fun testDurationFormat() {
    val oneHourThirtyMins = (90 * 60 * 1000L)
    assertEquals("01:30:00", ActiveTimerState.formatDuration(oneHourThirtyMins))
  }
}
