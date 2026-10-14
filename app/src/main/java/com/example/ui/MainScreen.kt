package com.example.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.notes.NotesScreen
import com.example.ui.notes.NotesViewModel
import com.example.ui.theme.DailyzTheme
import com.example.ui.tracker.WorkTrackerScreen
import com.example.ui.tracker.WorkTrackerViewModel

@Composable
fun MainScreen(
    trackerViewModel: WorkTrackerViewModel = viewModel(),
    notesViewModel: NotesViewModel = viewModel()
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val themeMode by trackerViewModel.themeMode.collectAsStateWithLifecycle()

    val isDark = when (themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }

    DailyzTheme(darkTheme = isDark) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Filled.Timer else Icons.Outlined.Timer,
                                contentDescription = "Work Tracker"
                            )
                        },
                        label = { Text("Tracker") },
                        modifier = Modifier.testTag("nav_tracker_tab")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 1) Icons.Filled.EditNote else Icons.Outlined.Description,
                                contentDescription = "Smart Notes"
                            )
                        },
                        label = { Text("Notes & CSV") },
                        modifier = Modifier.testTag("nav_notes_tab")
                    )
                }
            }
        ) { paddingValues ->
            if (selectedTab == 0) {
                WorkTrackerScreen(
                    viewModel = trackerViewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            } else {
                NotesScreen(
                    viewModel = notesViewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}
