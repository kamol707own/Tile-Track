package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.DateUtil
import com.example.domain.DayStatus
import com.example.domain.TaskItem
import com.example.domain.TaskPriority
import com.example.ui.components.TaskEditorSheet
import com.example.ui.theme.LocalTileTrackColors
import com.example.ui.theme.MetricNumberStyle
import com.example.ui.theme.TileTrackMotion
import com.example.ui.viewmodel.TaskListFilter
import com.example.ui.viewmodel.TasksUiState
import com.example.ui.viewmodel.TasksViewModel

private val TasksMissColor = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    uiState: TasksUiState,
    viewModel: TasksViewModel,
    widthSizeClass: WindowWidthSizeClass,
    modifier: Modifier = Modifier,
) {
    val extended = LocalTileTrackColors.current
    var showCreateTaskSheet by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TaskItem?>(null) }

    val columns = when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> 1
        else -> 2
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.tasks_title),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateTaskSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("New Task") },
                modifier = Modifier.testTag("tasks_fab_add"),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Clean flat inline status summary (no glowy bordered boxes)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TaskInlineCount(label = "open", count = uiState.openCount, tint = MaterialTheme.colorScheme.onSurface)
                TaskInlineCount(label = "done", count = uiState.doneCount, tint = extended.mutedText)
                TaskInlineCount(label = "missed", count = uiState.missedCount, tint = TasksMissColor)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Clean flat filter pills (no neon outline borders)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TaskListFilter.entries.forEach { filter ->
                    val selected = uiState.selectedFilter == filter
                    Surface(
                        onClick = { viewModel.selectFilter(filter) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .testTag("tasks_filter_${filter.name.lowercase()}"),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = filter.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) MaterialTheme.colorScheme.surface else extended.mutedText,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!uiState.isLoading && uiState.allTasks.isEmpty()) {
                TasksEmptyState(
                    onCreateTask = { showCreateTaskSheet = true },
                    modifier = Modifier.fillMaxSize(),
                )
            } else if (!uiState.isLoading && !uiState.hasVisibleTasks) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.TaskAlt,
                            contentDescription = null,
                            tint = extended.mutedText,
                            modifier = Modifier.size(40.dp),
                        )
                        Text(
                            text = "No ${uiState.selectedFilter.label.lowercase()} tasks",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "Try switching filters or add a new scheduled task.",
                            style = MaterialTheme.typography.bodySmall,
                            color = extended.mutedText,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 6.dp,
                        bottom = 104.dp,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val sections = listOf(
                        Triple("Overdue", uiState.overdueTasks, true),
                        Triple("Today", uiState.todayTasks, false),
                        Triple("Upcoming", uiState.upcomingTasks, false),
                        Triple("Earlier", uiState.earlierResolvedTasks, false),
                    ).filter { it.second.isNotEmpty() }

                    sections.forEach { (sectionTitle, tasks, isAlert) ->
                        item(span = { GridItemSpan(columns) }) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp, bottom = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = sectionTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (isAlert) TasksMissColor else extended.mutedText,
                                )
                                Text(
                                    text = "${tasks.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isAlert) TasksMissColor else extended.mutedText,
                                )
                            }
                        }

                        itemsIndexed(
                            items = tasks,
                            key = { _, task -> task.id },
                        ) { _, task ->
                            TaskSectionCard(
                                task = task,
                                todayEpochDay = uiState.todayEpochDay,
                                onToggleDone = { viewModel.cycleTaskDoneUndone(task) },
                                onSetStatus = { status -> viewModel.setTaskStatus(task.id, status) },
                                onEdit = { editingTask = task },
                                onDelete = { viewModel.deleteTask(task.id) },
                                modifier = Modifier.animateItem(
                                    placementSpec = TileTrackMotion.listPlacementSpec,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateTaskSheet) {
        TaskEditorSheet(
            existing = null,
            defaultEpochDay = uiState.todayEpochDay,
            todayEpochDay = uiState.todayEpochDay,
            onDismiss = { showCreateTaskSheet = false },
            onSave = { title, notes, dueDay, priority, colorArgb, dueTimeMinutes, status ->
                viewModel.saveTask(
                    existingId = null,
                    title = title,
                    notes = notes,
                    dueEpochDay = dueDay,
                    priority = priority,
                    colorArgb = colorArgb,
                    dueTimeMinutes = dueTimeMinutes,
                    status = status,
                )
                showCreateTaskSheet = false
            },
        )
    }

    editingTask?.let { current ->
        TaskEditorSheet(
            existing = current,
            defaultEpochDay = current.dueEpochDay,
            todayEpochDay = uiState.todayEpochDay,
            onDismiss = { editingTask = null },
            onSave = { title, notes, dueDay, priority, colorArgb, dueTimeMinutes, status ->
                viewModel.saveTask(
                    existingId = current.id,
                    title = title,
                    notes = notes,
                    dueEpochDay = dueDay,
                    priority = priority,
                    colorArgb = colorArgb,
                    dueTimeMinutes = dueTimeMinutes,
                    status = status,
                    existingCreatedAt = current.createdAt,
                )
                editingTask = null
            },
        )
    }
}

@Composable
private fun TaskInlineCount(
    label: String,
    count: Int,
    tint: Color,
) {
    val extended = LocalTileTrackColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(
            text = "$count",
            style = MetricNumberStyle.copy(fontSize = MaterialTheme.typography.titleMedium.fontSize),
            color = tint,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = extended.mutedText,
        )
    }
}

@Composable
private fun TaskSectionCard(
    task: TaskItem,
    todayEpochDay: Long,
    onToggleDone: () -> Unit,
    onSetStatus: (DayStatus) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extended = LocalTileTrackColors.current
    val accent = Color(task.colorArgb)
    val haptic = LocalHapticFeedback.current
    var menuExpanded by remember { mutableStateOf(false) }

    val priorityColor = when (task.priority) {
        TaskPriority.HIGH -> TasksMissColor
        TaskPriority.MEDIUM -> accent
        TaskPriority.LOW -> extended.mutedText
    }
    val dateLabel = remember(task.dueEpochDay, todayEpochDay) {
        when (task.dueEpochDay) {
            todayEpochDay -> "Today"
            todayEpochDay + 1L -> "Tomorrow"
            todayEpochDay - 1L -> "Yesterday"
            else -> DateUtil.formatShortDate(task.dueEpochDay)
        }
    }

    val isDone = task.status == DayStatus.DONE
    val isMiss = task.status == DayStatus.MISS

    val doneScale by animateFloatAsState(
        targetValue = if (isDone) 1.04f else 1f,
        animationSpec = TileTrackMotion.fastSpatial(),
        label = "tasks_done_scale",
    )
    val doneBg by animateColorAsState(
        targetValue = if (isDone) accent else extended.subtleSurface,
        animationSpec = TileTrackMotion.fastEffects(),
        label = "tasks_done_bg",
    )
    val missBg by animateColorAsState(
        targetValue = if (isMiss) TasksMissColor.copy(alpha = 0.22f) else extended.subtleSurface,
        animationSpec = TileTrackMotion.fastEffects(),
        label = "tasks_miss_bg",
    )

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, extended.cardBorder),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onEdit() }
            .testTag("tasks_screen_card_${task.id}"),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(42.dp)
                    .clip(CircleShape)
                    .background(priorityColor),
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                    ),
                    color = when (task.status) {
                        DayStatus.DONE -> extended.mutedText
                        DayStatus.MISS -> TasksMissColor
                        DayStatus.UNDONE -> MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (task.notes.isNotBlank()) {
                    Text(
                        text = task.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = extended.mutedText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarToday,
                            contentDescription = null,
                            tint = if (task.dueEpochDay < todayEpochDay && task.status == DayStatus.UNDONE) TasksMissColor else extended.mutedText,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (task.dueEpochDay < todayEpochDay && task.status == DayStatus.UNDONE) TasksMissColor else extended.mutedText,
                        )
                    }
                    if (task.dueTimeMinutes != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AccessTime,
                                contentDescription = null,
                                tint = extended.mutedText,
                                modifier = Modifier.size(12.dp),
                            )
                            Text(
                                text = DateUtil.formatTime(task.dueTimeMinutes / 60, task.dueTimeMinutes % 60),
                                style = MaterialTheme.typography.labelSmall,
                                color = extended.mutedText,
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = priorityColor.copy(alpha = 0.14f),
                    ) {
                        Text(
                            text = task.priority.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = priorityColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSetStatus(if (isMiss) DayStatus.UNDONE else DayStatus.MISS)
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = missBg,
                    border = if (isMiss) null else BorderStroke(1.dp, extended.cardBorder),
                    modifier = Modifier.size(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = if (isMiss) "Unmark missed" else "Mark missed",
                            tint = if (isMiss) TasksMissColor else extended.mutedText,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }

                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleDone()
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = doneBg,
                    border = if (isDone) null else BorderStroke(1.dp, extended.cardBorder),
                    modifier = Modifier
                        .scale(doneScale)
                        .minimumInteractiveComponentSize()
                        .size(44.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = if (isDone) "Mark undone" else "Mark done",
                            tint = if (isDone) Color.White else extended.mutedText,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                }
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Task options",
                        tint = extended.mutedText,
                        modifier = Modifier.size(18.dp),
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    if (task.status != DayStatus.DONE) {
                        DropdownMenuItem(
                            text = { Text("Mark as Done") },
                            leadingIcon = { Icon(Icons.Rounded.Check, contentDescription = null, tint = accent) },
                            onClick = {
                                menuExpanded = false
                                onSetStatus(DayStatus.DONE)
                            },
                        )
                    }
                    if (task.status != DayStatus.UNDONE) {
                        DropdownMenuItem(
                            text = { Text("Reset to Pending") },
                            leadingIcon = { Icon(Icons.Rounded.RadioButtonUnchecked, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onSetStatus(DayStatus.UNDONE)
                            },
                        )
                    }
                    if (task.status != DayStatus.MISS) {
                        DropdownMenuItem(
                            text = { Text("Mark Missed") },
                            leadingIcon = { Icon(Icons.Rounded.Close, contentDescription = null, tint = TasksMissColor) },
                            onClick = {
                                menuExpanded = false
                                onSetStatus(DayStatus.MISS)
                            },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        leadingIcon = { Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = TasksMissColor) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun TasksEmptyState(
    onCreateTask: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extended = LocalTileTrackColors.current
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.size(68.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.TaskAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "Plan your action items",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Schedule one-off action items with custom dates, times, priorities, and clear status tracking.",
            style = MaterialTheme.typography.bodyMedium,
            color = extended.mutedText,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(22.dp))
        Button(
            onClick = onCreateTask,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .height(48.dp)
                .testTag("empty_tasks_create_button"),
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Create Your First Task")
        }
    }
}
