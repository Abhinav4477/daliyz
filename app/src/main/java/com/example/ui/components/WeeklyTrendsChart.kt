package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.DayProductivity
import com.example.data.repository.WeeklySummary
import com.example.ui.theme.ChartBarGoalMet
import com.example.ui.theme.OvertimeAmber
import java.util.Locale

@Composable
fun WeeklyTrendsChartCard(
    weeklySummary: WeeklySummary,
    modifier: Modifier = Modifier
) {
    var selectedDay by remember { mutableStateOf<DayProductivity?>(null) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_trends_chart_card"),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Weekly Trends",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Weekly Productivity",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = weeklySummary.weekRangeLabel,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatPill(
                    label = "Total Worked",
                    value = String.format(Locale.US, "%.1fh", weeklySummary.totalHours),
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    label = "Daily Avg",
                    value = String.format(Locale.US, "%.1fh/d", weeklySummary.averageDailyHours),
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    label = "3h Goal Met",
                    value = "${weeklySummary.daysGoalMetCount}/7 days",
                    modifier = Modifier.weight(1f),
                    highlightColor = if (weeklySummary.daysGoalMetCount >= 5) ChartBarGoalMet else null
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Interactive Bar Chart
            val maxHours = maxOf(4.5f, (weeklySummary.days.maxOfOrNull { it.totalHours } ?: 0f) * 1.15f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    val width = size.width
                    val height = size.height

                    // Draw 3-hour Target Dashed Guideline
                    val targetY = height * (1f - (3.0f / maxHours))
                    drawLine(
                        color = Color(0x6694A3B8),
                        start = Offset(0f, targetY),
                        end = Offset(width, targetY),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                    )

                    // Draw baseline
                    drawLine(
                        color = Color(0x3394A3B8),
                        start = Offset(0f, height),
                        end = Offset(width, height),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // 7 Bar Columns
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weeklySummary.days.forEach { day ->
                        val barHeightFraction = (day.totalHours / maxHours).coerceIn(0f, 1f)
                        val animatedFraction by animateFloatAsState(
                            targetValue = barHeightFraction,
                            animationSpec = tween(durationMillis = 600),
                            label = "barHeight"
                        )

                        val isOvertime = day.totalHours > 3.0f
                        val isGoalMet = day.totalHours >= 3.0f
                        val isSelected = selectedDay?.dateKey == day.dateKey

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedDay = if (selectedDay?.dateKey == day.dateKey) null else day
                                }
                                .padding(horizontal = 2.dp)
                        ) {
                            // Value on top of bar
                            Text(
                                text = if (day.totalHours > 0) String.format(Locale.US, "%.1f", day.totalHours) else "-",
                                fontSize = 11.sp,
                                fontWeight = if (day.isToday || isGoalMet) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    isOvertime -> OvertimeAmber
                                    isGoalMet -> ChartBarGoalMet
                                    day.isToday -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Vertical Bar
                            Box(
                                modifier = Modifier
                                    .width(if (isSelected) 24.dp else 18.dp)
                                    .height((120.dp * animatedFraction).coerceAtLeast(6.dp))
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                                    .background(
                                        when {
                                            isOvertime -> OvertimeAmber
                                            isGoalMet -> ChartBarGoalMet
                                            day.totalHours > 0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Day Label (Mon, Tue, etc.)
                            Text(
                                text = day.dayLabel,
                                fontSize = 12.sp,
                                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Medium,
                                color = if (day.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Today Indicator dot
                            if (day.isToday) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            } else {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }

            // Legend / Selected Day Details Card
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(2.dp)
                            .background(Color(0x9994A3B8))
                    )
                    Text(
                        text = "3h Target Line",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (selectedDay != null) {
                    val sDay = selectedDay!!
                    Text(
                        text = "${sDay.dayLabel}: ${String.format(Locale.US, "%.2f", sDay.totalHours)}h ${if (sDay.totalHours >= 3f) "(Goal met)" else ""}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (sDay.totalHours >= 3f) ChartBarGoalMet else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlightColor: Color? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = highlightColor ?: MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
