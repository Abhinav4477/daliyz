package com.example.ui.tracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.WorkSession
import com.example.service.ActiveTimerState
import com.example.ui.components.DualProgressHeroCard
import com.example.ui.components.ReminderSettingDialog
import com.example.ui.components.WeeklyTrendsChartCard
import com.example.ui.theme.ChartBarGoalMet
import com.example.ui.theme.OvertimeAmber
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WorkTrackerScreen(
    viewModel: WorkTrackerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val timerState by viewModel.activeTimerState.collectAsStateWithLifecycle()
    val progressState by viewModel.workProgress.collectAsStateWithLifecycle()
    val weeklySummary by viewModel.weeklySummary.collectAsStateWithLifecycle()
    val sessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val reminderEnabled by viewModel.reminderEnabled.collectAsStateWithLifecycle()
    val reminderHour by viewModel.reminderHour.collectAsStateWithLifecycle()
    val reminderMinute by viewModel.reminderMinute.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val pruneNotice by viewModel.lastPruneNotice.collectAsStateWithLifecycle()

    var showReminderDialog by remember { mutableStateOf(false) }

    // Check & request notification permission for Android 13+
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    LaunchedEffect(pruneNotice) {
        pruneNotice?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearPruneNotice()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // App Header Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "dailyz",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = SimpleDateFormat("EEEE, MMMM d", Locale.US).format(Date()),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Theme Toggle Button
                        IconButton(
                            onClick = {
                                val nextMode = when (themeMode) {
                                    "SYSTEM" -> "LIGHT"
                                    "LIGHT" -> "DARK"
                                    else -> "SYSTEM"
                                }
                                viewModel.setThemeMode(nextMode)
                            },
                            modifier = Modifier.testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = when (themeMode) {
                                    "LIGHT" -> Icons.Default.Brightness7
                                    "DARK" -> Icons.Default.Brightness4
                                    else -> Icons.Default.BrightnessAuto
                                },
                                contentDescription = "Toggle theme mode",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Manual Prune History Button
                        IconButton(
                            onClick = { viewModel.manuallyPruneOldHistory() },
                            modifier = Modifier.testTag("manual_prune_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoDelete,
                                contentDescription = "Clean data older than 3 months",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Notification Permission Banner (if needed)
            if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Enable notifications for the persistent workday timer & reminder alerts.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Button(
                                onClick = {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Allow", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Hero Timer & Stacked 3h Overtime Progress Card
            item {
                DualProgressHeroCard(
                    progressState = progressState,
                    timerState = timerState
                )
            }

            // Timer Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!timerState.isRunning && !timerState.isPaused) {
                        // Start Work Button
                        Button(
                            onClick = {
                                viewModel.startWork(context)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("start_work_button"),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Start Work",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Start Workday",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // Pause / Resume Button
                        if (timerState.isPaused) {
                            Button(
                                onClick = { viewModel.resumeWork(context) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .testTag("resume_work_button"),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ChartBarGoalMet
                                )
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Resume", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            FilledTonalButton(
                                onClick = { viewModel.pauseWork(context) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .testTag("pause_work_button"),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = OvertimeAmber.copy(alpha = 0.2f),
                                    contentColor = OvertimeAmber
                                )
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = "Pause")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pause", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Stop & Save Button
                        Button(
                            onClick = { viewModel.stopWork(context) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("stop_work_button"),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop & Save")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Stop & Save", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Workday Reminder Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showReminderDialog = true }
                        .testTag("reminder_banner_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (reminderEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = "Reminder",
                                    tint = if (reminderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column {
                                Text(
                                    text = if (reminderEnabled) {
                                        "Work Reminder: ${String.format(Locale.US, "%02d:%02d", reminderHour, reminderMinute)}"
                                    } else {
                                        "Work Reminder Disabled"
                                    },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (reminderEnabled) "Alerts if work not started • Tap to edit" else "Tap to set daily start alert",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = if (reminderEnabled) "ON" else "OFF",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (reminderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Weekly Productivity Chart
            item {
                WeeklyTrendsChartCard(weeklySummary = weeklySummary)
            }

            // Session History Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Work History (Last 3 Months)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "${sessions.size} sessions",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 3-Month Retention Guarantee Banner
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoDelete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "History older than 3 months is automatically removed permanently to keep your local device clean.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Sessions List
            if (sessions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recorded work sessions yet.\nHit 'Start Workday' above to begin!",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(
                    items = sessions,
                    key = { it.id }
                ) { session ->
                    WorkSessionItem(
                        session = session,
                        onDelete = { viewModel.deleteSession(session.id) }
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }

    if (showReminderDialog) {
        ReminderSettingDialog(
            initialEnabled = reminderEnabled,
            initialHour = reminderHour,
            initialMinute = reminderMinute,
            onDismiss = { showReminderDialog = false },
            onConfirm = { enabled, hour, minute ->
                viewModel.updateReminder(context, enabled, hour, minute)
                showReminderDialog = false
            }
        )
    }
}

@Composable
private fun WorkSessionItem(
    session: WorkSession,
    onDelete: () -> Unit
) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
    val startStr = timeFormat.format(Date(session.startTime))
    val endStr = timeFormat.format(Date(session.endTime))

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("session_item_${session.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = session.dateKey,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$startStr - $endStr",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = ActiveTimerState.formatDuration(session.durationMillis),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Session",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
