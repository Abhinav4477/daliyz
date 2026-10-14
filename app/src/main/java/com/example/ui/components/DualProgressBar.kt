package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.ActiveTimerState
import com.example.ui.theme.ChartBarGoalMet
import com.example.ui.theme.OvertimeAmber
import com.example.ui.tracker.WorkProgressState

@Composable
fun DualProgressHeroCard(
    progressState: WorkProgressState,
    timerState: ActiveTimerState,
    modifier: Modifier = Modifier
) {
    val animatedBaseProgress by animateFloatAsState(
        targetValue = progressState.baseProgress,
        animationSpec = tween(durationMillis = 600),
        label = "baseProgress"
    )

    val animatedOvertimeProgress by animateFloatAsState(
        targetValue = progressState.overtimeProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "overtimeProgress"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_timer_progress_card"),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Header Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(
                        when {
                            progressState.overtimeMillis > 0 -> OvertimeAmber.copy(alpha = 0.18f)
                            progressState.isGoalMet -> ChartBarGoalMet.copy(alpha = 0.15f)
                            timerState.isRunning -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            timerState.isPaused -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (progressState.overtimeMillis > 0) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Overtime",
                        tint = OvertimeAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "OVERTIME BONUS",
                        color = OvertimeAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                } else if (progressState.isGoalMet) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Target Reached",
                        tint = ChartBarGoalMet,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "3H GOAL COMPLETED!",
                        color = ChartBarGoalMet,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (timerState.isRunning) {
                                    if (timerState.isPaused) OvertimeAmber else ChartBarGoalMet
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                    )
                    Text(
                        text = when {
                            timerState.isRunning && !timerState.isPaused -> "WORKING NOW"
                            timerState.isPaused -> "SESSION PAUSED"
                            else -> "READY TO START"
                        },
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Circular Dual-Progress Visualizer
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(230.dp)
            ) {
                Canvas(modifier = Modifier.size(210.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val overtimeStrokeWidth = 16.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    val arcSize = Size(diameter, diameter)

                    // 1. Background Track
                    drawArc(
                        color = Color(0x1F94A3B8),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // 2. Base 3-Hour Target Progress Arc (Emerald / Blue)
                    val baseSweep = 270f * animatedBaseProgress
                    if (baseSweep > 0f) {
                        drawArc(
                            color = if (progressState.isGoalMet) ChartBarGoalMet else Color(0xFF2563EB),
                            startAngle = 135f,
                            sweepAngle = baseSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // 3. Stacked Overtime Progress Arc (Directly over the completed bar!)
                    if (progressState.isGoalMet && animatedOvertimeProgress > 0f) {
                        val overtimeSweep = 270f * animatedOvertimeProgress
                        drawArc(
                            color = OvertimeAmber,
                            startAngle = 135f,
                            sweepAngle = overtimeSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = overtimeStrokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                // Inner Dial Content: Time display & Goal stats
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (timerState.isRunning || timerState.isPaused) {
                            timerState.formattedDuration
                        } else {
                            ActiveTimerState.formatDuration(progressState.totalTodayMillis)
                        },
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (timerState.isRunning) "Current Session" else "Logged Today",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (progressState.overtimeMillis > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "+${ActiveTimerState.formatDuration(progressState.overtimeMillis)} Over",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = OvertimeAmber
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Linear Dual Progress Bar Representation
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Daily Target: 3 hrs",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.1f", progressState.totalTodayMillis / 3600000f)}h / 3.0h",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (progressState.isGoalMet) ChartBarGoalMet else MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Stacked / Overlaid Linear Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    // Base completed bar (fills up to 100% of 3 hours)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = animatedBaseProgress)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (progressState.isGoalMet) ChartBarGoalMet else Color(0xFF2563EB))
                    )

                    // Overtime bar (starts a new progress bar over the completed progress bar!)
                    if (progressState.isGoalMet && animatedOvertimeProgress > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = animatedOvertimeProgress)
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(OvertimeAmber)
                        )
                    }
                }
            }
        }
    }
}
