package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.domain.DateUtil
import com.example.domain.DayStatus
import com.example.domain.HabitWithLogs
import com.example.domain.UserPreferences
import com.example.ui.components.HabitEditorSheet
import com.example.ui.components.HabitIconCatalog
import com.example.ui.components.InteractiveYearHeatmap
import com.example.ui.components.MonthlyBarChart
import com.example.ui.components.WeekdayBreakdownBars
import com.example.ui.theme.LocalTileTrackColors
import com.example.ui.theme.MetricNumberStyle
import com.example.ui.viewmodel.HabitDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habitWithLogs: HabitWithLogs?,
    preferences: UserPreferences,
    viewModel: HabitDetailViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extended = LocalTileTrackColors.current
    val haptic = LocalHapticFeedback.current
    val todayEpochDay = DateUtil.todayEpochDay()
    var showEditSheet by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var selectedEpochDay by remember(todayEpochDay) { mutableLongStateOf(todayEpochDay) }

    val item = habitWithLogs
    val accent = item?.let { Color(it.habit.colorArgb) } ?: MaterialTheme.colorScheme.primary

    val selectedLog = item?.logsByEpochDay?.get(selectedEpochDay)
    var noteDraft by remember(selectedEpochDay, selectedLog?.note) {
        mutableStateOf(selectedLog?.note ?: "")
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("detail_back_button"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                title = {
                    Text(
                        text = item?.habit?.name ?: "Habit Details",
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                    )
                },
                actions = {
                    if (item != null) {
                        IconButton(
                            onClick = { showEditSheet = true },
                            modifier = Modifier.testTag("detail_edit_button"),
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit habit")
                        }
                        IconButton(
                            onClick = {
                                viewModel.toggleArchive(!item.habit.isArchived, item.reminders)
                                onNavigateBack()
                            },
                            modifier = Modifier.testTag("detail_archive_button"),
                        ) {
                            Icon(
                                imageVector = if (item.habit.isArchived) Icons.Rounded.Unarchive else Icons.Rounded.Archive,
                                contentDescription = if (item.habit.isArchived) "Restore habit" else "Archive habit",
                            )
                        }
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.testTag("detail_delete_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Delete habit",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        if (item == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text("Habit not found", color = extended.mutedText)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, extended.cardBorder),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = accent.copy(alpha = 0.16f),
                        modifier = Modifier.size(52.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = HabitIconCatalog.resolve(item.habit.iconKey),
                                contentDescription = item.habit.name,
                                tint = accent,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(item.habit.name, style = MaterialTheme.typography.headlineSmall)
                        if (item.habit.description.isNotBlank()) {
                            Text(
                                text = item.habit.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = extended.mutedText,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = extended.subtleSurface,
                            ) {
                                Text(
                                    text = item.habit.category,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = extended.mutedText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = accent.copy(alpha = 0.14f),
                            ) {
                                Text(
                                    text = "${item.habit.dailyTarget}x / day • ${item.habit.streakGoalType.label}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = accent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                KpiMetricCard(
                    title = "Current Streak",
                    value = "${item.stats.currentStreak}",
                    accent = accent,
                    modifier = Modifier.weight(1f),
                )
                KpiMetricCard(
                    title = "Best Streak",
                    value = "${item.stats.bestStreak}",
                    accent = accent,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                KpiMetricCard(
                    title = "Done / Missed",
                    value = "${item.stats.completedDaysCount} / ${item.stats.missedDaysCount}",
                    accent = accent,
                    modifier = Modifier.weight(1f),
                )
                KpiMetricCard(
                    title = "30d Consistency",
                    value = "${item.stats.consistency30DaysPercent}%",
                    accent = accent,
                    modifier = Modifier.weight(1f),
                )
            }

            // Full-Year Interactive Heatmap + Selected Day Editor (Done / Undone / Miss)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, extended.cardBorder),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("52-Week History", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Tap any tile to edit",
                            style = MaterialTheme.typography.bodySmall,
                            color = extended.mutedText,
                        )
                    }

                    InteractiveYearHeatmap(
                        logsByEpochDay = item.logsByEpochDay,
                        accentColor = accent,
                        dailyTarget = item.habit.dailyTarget,
                        weekStartMonday = preferences.weekStartMonday,
                        todayEpochDay = todayEpochDay,
                        selectedEpochDay = selectedEpochDay,
                        onSelectDay = { selectedEpochDay = it },
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = extended.subtleSurface,
                        border = BorderStroke(1.dp, extended.cardBorder),
                    ) {
                        val currentCount = selectedLog?.count ?: 0
                        val currentStatus = when {
                            selectedLog?.status == DayStatus.MISS -> DayStatus.MISS
                            selectedLog?.status == DayStatus.DONE || currentCount >= item.habit.dailyTarget -> DayStatus.DONE
                            else -> DayStatus.UNDONE
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(
                                        text = DateUtil.formatFullDate(selectedEpochDay),
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                    Text(
                                        text = when (currentStatus) {
                                            DayStatus.DONE -> "Done"
                                            DayStatus.MISS -> "Missed"
                                            DayStatus.UNDONE -> if (currentCount > 0) "In progress ($currentCount/${item.habit.dailyTarget})" else "Pending"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = when (currentStatus) {
                                            DayStatus.DONE -> accent
                                            DayStatus.MISS -> Color(0xFFEF4444)
                                            DayStatus.UNDONE -> extended.mutedText
                                        },
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            if (currentCount > 0) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                viewModel.setDayCount(selectedEpochDay, currentCount - 1, noteDraft)
                                            }
                                        },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("day_decrement_button"),
                                    ) {
                                        Icon(Icons.Rounded.Remove, contentDescription = "Decrease day count")
                                    }
                                    Text(
                                        text = "$currentCount / ${item.habit.dailyTarget}",
                                        style = MetricNumberStyle,
                                        modifier = Modifier.padding(horizontal = 6.dp),
                                    )
                                    IconButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.setDayCount(selectedEpochDay, currentCount + 1, noteDraft)
                                        },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("day_increment_button"),
                                    ) {
                                        Icon(Icons.Rounded.Add, contentDescription = "Increase day count")
                                    }
                                }
                            }

                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("detail_day_status_selector"),
                            ) {
                                val statuses = listOf(DayStatus.DONE, DayStatus.UNDONE, DayStatus.MISS)
                                statuses.forEachIndexed { index, status ->
                                    SegmentedButton(
                                        selected = currentStatus == status,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            viewModel.setDayStatus(selectedEpochDay, status, note = noteDraft)
                                        },
                                        shape = SegmentedButtonDefaults.itemShape(
                                            index = index,
                                            count = statuses.size,
                                        ),
                                        label = { Text(status.label) },
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = noteDraft,
                                onValueChange = { noteDraft = it },
                                label = { Text("Day note (optional)") },
                                singleLine = true,
                                trailingIcon = {
                                    if (noteDraft != (selectedLog?.note ?: "")) {
                                        TextButton(
                                            onClick = {
                                                viewModel.setDayCount(selectedEpochDay, currentCount, noteDraft)
                                            },
                                        ) {
                                            Text("Save")
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, extended.cardBorder),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("6-Month Consistency", style = MaterialTheme.typography.titleMedium)
                    MonthlyBarChart(
                        bars = item.stats.monthlyBars,
                        accentColor = accent,
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, extended.cardBorder),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Performance by Weekday", style = MaterialTheme.typography.titleMedium)
                    WeekdayBreakdownBars(
                        stats = item.stats.weekdayStats,
                        accentColor = accent,
                    )
                }
            }

            if (item.reminders.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, extended.cardBorder),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("Scheduled Reminders", style = MaterialTheme.typography.titleMedium)
                        item.reminders.forEach { reminder ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.NotificationsActive,
                                        contentDescription = null,
                                        tint = if (reminder.isEnabled) accent else extended.mutedText,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Text(
                                        text = DateUtil.formatTime(reminder.hour, reminder.minute),
                                        style = MetricNumberStyle,
                                    )
                                }
                                Text(
                                    text = if (reminder.isEnabled) reminder.formatDaysSummary() else "Paused",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = extended.mutedText,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showEditSheet && item != null) {
        HabitEditorSheet(
            existing = item,
            onDismiss = { showEditSheet = false },
            onSave = { name, desc, iconKey, colorArgb, category, dailyTarget, streakType, streakTarget, reminders ->
                viewModel.updateHabit(
                    name = name,
                    description = desc,
                    iconKey = iconKey,
                    colorArgb = colorArgb,
                    category = category,
                    dailyTarget = dailyTarget,
                    streakGoalType = streakType,
                    streakGoalTarget = streakTarget,
                    reminders = reminders,
                    previousReminders = item.reminders,
                )
                showEditSheet = false
            },
        )
    }

    if (showDeleteConfirm && item != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Habit?") },
            text = {
                Text("This will permanently remove \"${item.habit.name}\" and all of its tile history. You can archive it instead to keep your logs.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deletePermanently(item.reminders)
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun KpiMetricCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val extended = LocalTileTrackColors.current
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, extended.cardBorder),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = extended.mutedText,
            )
            Text(
                text = value,
                style = MetricNumberStyle.copy(fontSize = MaterialTheme.typography.headlineMedium.fontSize),
                color = accent,
            )
        }
    }
}
