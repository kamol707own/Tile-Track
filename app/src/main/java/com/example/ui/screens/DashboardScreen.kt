package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material.icons.rounded.ViewWeek
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.example.domain.DashboardContentFilter
import com.example.domain.DashboardViewMode
import com.example.domain.DateUtil
import com.example.domain.DayStatus
import com.example.domain.HabitWithLogs
import com.example.domain.StreakGoalType
import com.example.domain.TaskItem
import com.example.domain.TaskPriority
import com.example.ui.components.ContributionGridPreview
import com.example.ui.components.HabitEditorSheet
import com.example.ui.components.HabitIconCatalog
import com.example.ui.components.TaskEditorSheet
import com.example.ui.components.WeekCalendarStrip
import com.example.ui.theme.LocalTileTrackColors
import com.example.ui.theme.TileTrackMotion
import com.example.ui.viewmodel.DashboardUiState
import com.example.ui.viewmodel.DashboardViewModel

private val MissAccentColor = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    viewModel: DashboardViewModel,
    widthSizeClass: WindowWidthSizeClass,
    onHabitClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val extended = LocalTileTrackColors.current
    var fabExpanded by remember { mutableStateOf(false) }
    var showCreateHabitSheet by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<HabitWithLogs?>(null) }
    var showCreateTaskSheet by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TaskItem?>(null) }

    val columns = when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> 1
        else -> 2
    }

    val selectedDay = uiState.selectedEpochDay
    val todayDay = uiState.todayEpochDay

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AnimatedVisibility(
                    visible = fabExpanded,
                    enter = fadeIn(
                        androidx.compose.animation.core.tween(
                            TileTrackMotion.DurationShort3,
                            easing = TileTrackMotion.EasingEmphasizedDecelerate,
                        ),
                    ) + expandVertically(animationSpec = TileTrackMotion.fastSpatial()),
                    exit = fadeOut(
                        androidx.compose.animation.core.tween(
                            TileTrackMotion.DurationShort1,
                            easing = TileTrackMotion.EasingEmphasizedAccelerate,
                        ),
                    ) + shrinkVertically(animationSpec = TileTrackMotion.fastSpatial()),
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                fabExpanded = false
                                showCreateTaskSheet = true
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            icon = { Icon(Icons.Rounded.TaskAlt, contentDescription = null) },
                            text = { Text("New Task") },
                            modifier = Modifier.testTag("fab_new_task"),
                        )
                        ExtendedFloatingActionButton(
                            onClick = {
                                fabExpanded = false
                                showCreateHabitSheet = true
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            icon = { Icon(Icons.Rounded.GridView, contentDescription = null) },
                            text = { Text("New Habit") },
                            modifier = Modifier.testTag("fab_new_habit"),
                        )
                    }
                }

                val rotation by animateFloatAsState(
                    targetValue = if (fabExpanded) 45f else 0f,
                    animationSpec = TileTrackMotion.fastSpatial(),
                    label = "fab_rotation",
                )
                FloatingActionButton(
                    onClick = { fabExpanded = !fabExpanded },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("fab_add_menu"),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.action_new_habit),
                        modifier = Modifier.rotate(rotation),
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Edge-to-edge two-tier horizontal calendar strip
            WeekCalendarStrip(
                weekStartEpochDay = uiState.displayedWeekStartEpochDay,
                selectedEpochDay = selectedDay,
                todayEpochDay = todayDay,
                onSelectDay = viewModel::selectDay,
                onShiftWeek = viewModel::shiftWeek,
                onJumpToToday = viewModel::jumpToToday,
                modifier = Modifier.padding(top = 6.dp, bottom = 4.dp),
            )

            val hasAnyContent = uiState.habits.isNotEmpty() || uiState.tasksForSelectedDay.isNotEmpty()

            if (hasAnyContent) {
                DashboardControlsBar(
                    viewMode = uiState.preferences.dashboardViewMode,
                    onViewModeChange = viewModel::setViewMode,
                    contentFilter = uiState.contentFilter,
                    onContentFilterChange = viewModel::selectContentFilter,
                    categories = if (uiState.preferences.showCategoryFilter) uiState.availableCategories else emptyList(),
                    selectedCategory = uiState.selectedCategory,
                    onSelectCategory = viewModel::selectCategory,
                )
            }

            if (!uiState.isLoading && !hasAnyContent) {
                EmptyDashboardState(
                    onCreateHabit = { showCreateHabitSheet = true },
                    onCreateTask = { showCreateTaskSheet = true },
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                AnimatedContent(
                    targetState = uiState.preferences.dashboardViewMode,
                    transitionSpec = { TileTrackMotion.sharedAxisYContentTransform() },
                    label = "dashboard_view_mode",
                ) { mode ->
                    val showHabits = uiState.contentFilter != DashboardContentFilter.TASKS
                    val showTasks = uiState.contentFilter != DashboardContentFilter.HABITS

                    if (mode == DashboardViewMode.WEEK_STRIP && showHabits && !showTasks) {
                        WeekStripDashboardList(
                            habits = uiState.filteredHabits,
                            todayEpochDay = todayDay,
                            showStreakBadges = uiState.preferences.showStreakBadges,
                            onToggleDay = viewModel::toggleDay,
                            onSetStatus = viewModel::setHabitDayStatus,
                            onHabitClick = onHabitClick,
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 8.dp,
                                bottom = 104.dp,
                            ),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            if (showTasks && uiState.tasksForSelectedDay.isNotEmpty()) {
                                item(span = { GridItemSpan(columns) }) {
                                    SectionTitleRow(
                                        title = if (selectedDay == todayDay) "Today's Tasks" else "Tasks • ${DateUtil.formatShortDate(selectedDay)}",
                                        count = uiState.tasksForSelectedDay.size,
                                    )
                                }
                                itemsIndexed(
                                    items = uiState.tasksForSelectedDay,
                                    key = { _, t -> "task_${t.id}" },
                                ) { _, task ->
                                    TaskDashboardCard(
                                        task = task,
                                        onToggleDone = { viewModel.cycleTaskDoneUndone(task) },
                                        onSetStatus = { status -> viewModel.setTaskStatus(task.id, status) },
                                        onEdit = { editingTask = task },
                                        onDelete = { viewModel.deleteTask(task.id) },
                                        modifier = Modifier.animateItem(
                                            placementSpec = TileTrackMotion.listPlacementSpec,
                                        ),
                                    )
                                }
                            } else if (showTasks && uiState.contentFilter == DashboardContentFilter.TASKS && uiState.tasksForSelectedDay.isEmpty()) {
                                item(span = { GridItemSpan(columns) }) {
                                    EmptyTasksInlineCard(
                                        dateLabel = if (selectedDay == todayDay) "today" else DateUtil.formatShortDate(selectedDay),
                                        onAddTask = { showCreateTaskSheet = true },
                                    )
                                }
                            }

                            if (showHabits && uiState.filteredHabits.isNotEmpty()) {
                                if (showTasks && uiState.tasksForSelectedDay.isNotEmpty()) {
                                    item(span = { GridItemSpan(columns) }) {
                                        SectionTitleRow(
                                            title = "Habits",
                                            count = uiState.filteredHabits.size,
                                        )
                                    }
                                }

                                if (mode == DashboardViewMode.WEEK_STRIP) {
                                    item(span = { GridItemSpan(columns) }) {
                                        WeekStripEmbeddedColumn(
                                            habits = uiState.filteredHabits,
                                            todayEpochDay = todayDay,
                                            showStreakBadges = uiState.preferences.showStreakBadges,
                                            onToggleDay = viewModel::toggleDay,
                                            onSetStatus = viewModel::setHabitDayStatus,
                                            onHabitClick = onHabitClick,
                                        )
                                    }
                                } else {
                                    itemsIndexed(
                                        items = uiState.filteredHabits,
                                        key = { _, item -> item.habit.id },
                                    ) { index, item ->
                                        if (mode == DashboardViewMode.GRID) {
                                            HabitGridCard(
                                                item = item,
                                                selectedEpochDay = selectedDay,
                                                todayEpochDay = todayDay,
                                                weekStartMonday = uiState.preferences.weekStartMonday,
                                                showStreakBadge = uiState.preferences.showStreakBadges,
                                                canMoveUp = index > 0 && uiState.selectedCategory == "All",
                                                canMoveDown = index < uiState.filteredHabits.lastIndex && uiState.selectedCategory == "All",
                                                onToggleSelectedDay = { viewModel.toggleDay(item.habit.id, selectedDay) },
                                                onSetSelectedDayStatus = { status ->
                                                    viewModel.setHabitDayStatus(item.habit.id, selectedDay, status)
                                                },
                                                onClick = { onHabitClick(item.habit.id) },
                                                onEdit = { editingHabit = item },
                                                onArchive = { viewModel.archiveHabit(item.habit.id, item.reminders) },
                                                onMoveUp = { viewModel.moveHabit(item.habit.id, moveUp = true) },
                                                onMoveDown = { viewModel.moveHabit(item.habit.id, moveUp = false) },
                                                modifier = Modifier.animateItem(
                                                    placementSpec = TileTrackMotion.listPlacementSpec,
                                                ),
                                            )
                                        } else {
                                            HabitCompactCard(
                                                item = item,
                                                selectedEpochDay = selectedDay,
                                                todayEpochDay = todayDay,
                                                showStreakBadge = uiState.preferences.showStreakBadges,
                                                onToggleSelectedDay = { viewModel.toggleDay(item.habit.id, selectedDay) },
                                                onSetSelectedDayStatus = { status ->
                                                    viewModel.setHabitDayStatus(item.habit.id, selectedDay, status)
                                                },
                                                onClick = { onHabitClick(item.habit.id) },
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
                }
            }
        }
    }

    if (showCreateHabitSheet) {
        HabitEditorSheet(
            existing = null,
            onDismiss = { showCreateHabitSheet = false },
            onSave = { name, desc, iconKey, colorArgb, category, dailyTarget, streakType, streakTarget, reminders ->
                viewModel.saveHabit(
                    existingId = null,
                    name = name,
                    description = desc,
                    iconKey = iconKey,
                    colorArgb = colorArgb,
                    category = category,
                    dailyTarget = dailyTarget,
                    streakGoalType = streakType,
                    streakGoalTarget = streakTarget,
                    reminders = reminders,
                    previousReminders = emptyList(),
                )
                showCreateHabitSheet = false
            },
        )
    }

    editingHabit?.let { current ->
        HabitEditorSheet(
            existing = current,
            onDismiss = { editingHabit = null },
            onSave = { name, desc, iconKey, colorArgb, category, dailyTarget, streakType, streakTarget, reminders ->
                viewModel.saveHabit(
                    existingId = current.habit.id,
                    name = name,
                    description = desc,
                    iconKey = iconKey,
                    colorArgb = colorArgb,
                    category = category,
                    dailyTarget = dailyTarget,
                    streakGoalType = streakType,
                    streakGoalTarget = streakTarget,
                    reminders = reminders,
                    previousReminders = current.reminders,
                )
                editingHabit = null
            },
        )
    }

    if (showCreateTaskSheet) {
        TaskEditorSheet(
            existing = null,
            defaultEpochDay = selectedDay,
            todayEpochDay = todayDay,
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
                    existingStatus = status,
                )
                showCreateTaskSheet = false
            },
        )
    }

    editingTask?.let { current ->
        TaskEditorSheet(
            existing = current,
            defaultEpochDay = current.dueEpochDay,
            todayEpochDay = todayDay,
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
                    existingStatus = status,
                    existingCreatedAt = current.createdAt,
                )
                editingTask = null
            },
        )
    }
}

@Composable
private fun SectionTitleRow(
    title: String,
    count: Int,
) {
    val extended = LocalTileTrackColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = extended.mutedText,
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall,
            color = extended.mutedText,
        )
    }
}

@Composable
private fun DashboardControlsBar(
    viewMode: DashboardViewMode,
    onViewModeChange: (DashboardViewMode) -> Unit,
    contentFilter: DashboardContentFilter,
    onContentFilterChange: (DashboardContentFilter) -> Unit,
    categories: List<String>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
) {
    val extended = LocalTileTrackColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DashboardContentFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = contentFilter == filter,
                        onClick = { onContentFilterChange(filter) },
                        label = { Text(filter.label) },
                        modifier = Modifier.testTag("content_filter_${filter.name.lowercase()}"),
                    )
                }
            }

            if (contentFilter != DashboardContentFilter.TASKS) {
                val nextMode = when (viewMode) {
                    DashboardViewMode.GRID -> DashboardViewMode.COMPACT
                    DashboardViewMode.COMPACT -> DashboardViewMode.WEEK_STRIP
                    DashboardViewMode.WEEK_STRIP -> DashboardViewMode.GRID
                }

                Surface(
                    onClick = { onViewModeChange(nextMode) },
                    shape = RoundedCornerShape(10.dp),
                    color = extended.subtleSurface,
                    border = BorderStroke(1.dp, extended.cardBorder),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("view_mode_cycle_button"),
                ) {
                    AnimatedContent(
                        targetState = viewMode,
                        transitionSpec = { TileTrackMotion.sharedAxisYContentTransform() },
                        label = "compact_view_mode_pill",
                    ) { targetMode ->
                        val (modeIcon, modeLabel) = when (targetMode) {
                            DashboardViewMode.GRID -> Icons.Rounded.GridView to "Grid"
                            DashboardViewMode.COMPACT -> Icons.Rounded.ViewAgenda to "Compact"
                            DashboardViewMode.WEEK_STRIP -> Icons.Rounded.ViewWeek to "5-Day"
                        }
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Icon(
                                imageVector = modeIcon,
                                contentDescription = "Switch view mode: $modeLabel",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = modeLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }

        if (categories.size > 2 && contentFilter != DashboardContentFilter.TASKS) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                categories.filter { it != "All" }.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            onSelectCategory(if (selectedCategory == category) "All" else category)
                        },
                        label = { Text(category) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitGridCard(
    item: HabitWithLogs,
    selectedEpochDay: Long,
    todayEpochDay: Long,
    weekStartMonday: Boolean,
    showStreakBadge: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onToggleSelectedDay: () -> Unit,
    onSetSelectedDayStatus: (DayStatus) -> Unit,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extended = LocalTileTrackColors.current
    val accent = Color(item.habit.colorArgb)
    var menuExpanded by remember { mutableStateOf(false) }

    val selectedLog = item.logsByEpochDay[selectedEpochDay]
    val selectedCount = selectedLog?.count ?: 0
    val selectedStatus = when {
        selectedLog?.status == DayStatus.MISS -> DayStatus.MISS
        selectedLog?.status == DayStatus.DONE || selectedCount >= item.habit.dailyTarget -> DayStatus.DONE
        else -> DayStatus.UNDONE
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, extended.cardBorder),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("habit_card_${item.habit.id}"),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = accent.copy(alpha = 0.16f),
                    modifier = Modifier.size(42.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = HabitIconCatalog.resolve(item.habit.iconKey),
                            contentDescription = item.habit.name,
                            tint = accent,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = item.habit.description.ifBlank { item.habit.category },
                        style = MaterialTheme.typography.bodySmall,
                        color = extended.mutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (showStreakBadge && item.stats.currentStreak > 0) {
                    StreakBadgePill(
                        streak = item.stats.currentStreak,
                        goalType = item.habit.streakGoalType,
                        accent = accent,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }

                // Quick Done/Undone + Miss controls
                DayStatusActionPair(
                    status = selectedStatus,
                    count = selectedCount,
                    dailyTarget = item.habit.dailyTarget,
                    accent = accent,
                    onToggleDone = onToggleSelectedDay,
                    onToggleMiss = {
                        val next = if (selectedStatus == DayStatus.MISS) DayStatus.UNDONE else DayStatus.MISS
                        onSetSelectedDayStatus(next)
                    },
                    checkInTestTag = "checkin_button_${item.habit.id}",
                    missTestTag = "miss_button_${item.habit.id}",
                )

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "Habit options",
                            tint = extended.mutedText,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        if (selectedStatus != DayStatus.DONE) {
                            DropdownMenuItem(
                                text = { Text("Mark as Done") },
                                leadingIcon = { Icon(Icons.Rounded.Check, contentDescription = null, tint = accent) },
                                onClick = {
                                    menuExpanded = false
                                    onSetSelectedDayStatus(DayStatus.DONE)
                                },
                            )
                        }
                        if (selectedStatus != DayStatus.UNDONE) {
                            DropdownMenuItem(
                                text = { Text("Reset to Pending") },
                                leadingIcon = { Icon(Icons.Rounded.RadioButtonUnchecked, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onSetSelectedDayStatus(DayStatus.UNDONE)
                                },
                            )
                        }
                        if (selectedStatus != DayStatus.MISS) {
                            DropdownMenuItem(
                                text = { Text("Mark Missed") },
                                leadingIcon = { Icon(Icons.Rounded.Close, contentDescription = null, tint = MissAccentColor) },
                                onClick = {
                                    menuExpanded = false
                                    onSetSelectedDayStatus(DayStatus.MISS)
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
                        if (canMoveUp) {
                            DropdownMenuItem(
                                text = { Text("Move Up") },
                                leadingIcon = { Icon(Icons.Rounded.ArrowUpward, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onMoveUp()
                                },
                            )
                        }
                        if (canMoveDown) {
                            DropdownMenuItem(
                                text = { Text("Move Down") },
                                leadingIcon = { Icon(Icons.Rounded.ArrowDownward, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onMoveDown()
                                },
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Archive") },
                            leadingIcon = { Icon(Icons.Rounded.Archive, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onArchive()
                            },
                        )
                    }
                }
            }

            ContributionGridPreview(
                logsByEpochDay = item.logsByEpochDay,
                accentColor = accent,
                dailyTarget = item.habit.dailyTarget,
                weekStartMonday = weekStartMonday,
                todayEpochDay = todayEpochDay,
                selectedEpochDay = selectedEpochDay,
            )
        }
    }
}

@Composable
private fun HabitCompactCard(
    item: HabitWithLogs,
    selectedEpochDay: Long,
    todayEpochDay: Long,
    showStreakBadge: Boolean,
    onToggleSelectedDay: () -> Unit,
    onSetSelectedDayStatus: (DayStatus) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extended = LocalTileTrackColors.current
    val accent = Color(item.habit.colorArgb)
    val selectedLog = item.logsByEpochDay[selectedEpochDay]
    val selectedCount = selectedLog?.count ?: 0
    val selectedStatus = when {
        selectedLog?.status == DayStatus.MISS -> DayStatus.MISS
        selectedLog?.status == DayStatus.DONE || selectedCount >= item.habit.dailyTarget -> DayStatus.DONE
        else -> DayStatus.UNDONE
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, extended.cardBorder),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accent.copy(alpha = 0.16f),
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = HabitIconCatalog.resolve(item.habit.iconKey),
                        contentDescription = item.habit.name,
                        tint = accent,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = item.habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (showStreakBadge && item.stats.currentStreak > 0) {
                        StreakBadgePill(
                            streak = item.stats.currentStreak,
                            goalType = item.habit.streakGoalType,
                            accent = accent,
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (offset in 6 downTo 0) {
                        val day = todayEpochDay - offset
                        val log = item.logsByEpochDay[day]
                        val status = log?.status ?: DayStatus.UNDONE
                        val count = log?.count ?: 0
                        val color = when {
                            status == DayStatus.MISS -> MissAccentColor.copy(alpha = 0.5f)
                            status == DayStatus.DONE || count >= item.habit.dailyTarget -> accent
                            count > 0 -> accent.copy(alpha = 0.45f)
                            else -> extended.emptyTile
                        }
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(color),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (status == DayStatus.MISS) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Missed",
                                    tint = Color.White,
                                    modifier = Modifier.size(8.dp),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            DayStatusActionPair(
                status = selectedStatus,
                count = selectedCount,
                dailyTarget = item.habit.dailyTarget,
                accent = accent,
                onToggleDone = onToggleSelectedDay,
                onToggleMiss = {
                    val next = if (selectedStatus == DayStatus.MISS) DayStatus.UNDONE else DayStatus.MISS
                    onSetSelectedDayStatus(next)
                },
            )
        }
    }
}

@Composable
private fun TaskDashboardCard(
    task: TaskItem,
    onToggleDone: () -> Unit,
    onSetStatus: (DayStatus) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extended = LocalTileTrackColors.current
    val accent = Color(task.colorArgb)
    var menuExpanded by remember { mutableStateOf(false) }

    val priorityColor = when (task.priority) {
        TaskPriority.HIGH -> MissAccentColor
        TaskPriority.MEDIUM -> accent
        TaskPriority.LOW -> extended.mutedText
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, extended.cardBorder),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onEdit() }
            .testTag("task_card_${task.id}"),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(34.dp)
                    .clip(CircleShape)
                    .background(priorityColor),
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        textDecoration = if (task.status == DayStatus.DONE) TextDecoration.LineThrough else TextDecoration.None,
                    ),
                    color = when (task.status) {
                        DayStatus.DONE -> extended.mutedText
                        DayStatus.MISS -> MissAccentColor
                        DayStatus.UNDONE -> MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
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
                    if (task.notes.isNotBlank()) {
                        Text(
                            text = task.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = extended.mutedText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    } else if (task.dueTimeMinutes == null) {
                        Text(
                            text = "${task.priority.label} • ${task.status.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (task.status == DayStatus.MISS) MissAccentColor else extended.mutedText,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            DayStatusActionPair(
                status = task.status,
                count = if (task.status == DayStatus.DONE) 1 else 0,
                dailyTarget = 1,
                accent = accent,
                onToggleDone = onToggleDone,
                onToggleMiss = {
                    val next = if (task.status == DayStatus.MISS) DayStatus.UNDONE else DayStatus.MISS
                    onSetStatus(next)
                },
                checkInTestTag = "task_done_${task.id}",
                missTestTag = "task_miss_${task.id}",
            )

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
                            leadingIcon = { Icon(Icons.Rounded.Close, contentDescription = null, tint = MissAccentColor) },
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
                        leadingIcon = { Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = MissAccentColor) },
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
private fun WeekStripDashboardList(
    habits: List<HabitWithLogs>,
    todayEpochDay: Long,
    showStreakBadges: Boolean,
    onToggleDay: (String, Long) -> Unit,
    onSetStatus: (String, Long, DayStatus) -> Unit,
    onHabitClick: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            WeekStripEmbeddedColumn(
                habits = habits,
                todayEpochDay = todayEpochDay,
                showStreakBadges = showStreakBadges,
                onToggleDay = onToggleDay,
                onSetStatus = onSetStatus,
                onHabitClick = onHabitClick,
            )
        }
    }
}

@Composable
private fun WeekStripEmbeddedColumn(
    habits: List<HabitWithLogs>,
    todayEpochDay: Long,
    showStreakBadges: Boolean,
    onToggleDay: (String, Long) -> Unit,
    onSetStatus: (String, Long, DayStatus) -> Unit,
    onHabitClick: (String) -> Unit,
) {
    val extended = LocalTileTrackColors.current
    val days = (4 downTo 0).map { todayEpochDay - it }
    val haptic = LocalHapticFeedback.current

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Habit",
                style = MaterialTheme.typography.labelMedium,
                color = extended.mutedText,
                modifier = Modifier.weight(1f),
            )
            days.forEach { day ->
                val civil = DateUtil.epochDayToCivilDate(day)
                val wd = DateUtil.weekdayShortLabel(DateUtil.dayOfWeekIso(day)).take(2)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(42.dp),
                ) {
                    Text(
                        text = wd,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (day == todayEpochDay) MaterialTheme.colorScheme.primary else extended.mutedText,
                    )
                    Text(
                        text = "${civil.day}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (day == todayEpochDay) MaterialTheme.colorScheme.primary else extended.mutedText,
                    )
                }
            }
        }

        habits.forEach { item ->
            val accent = Color(item.habit.colorArgb)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, extended.cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onHabitClick(item.habit.id) },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = accent.copy(alpha = 0.16f),
                        modifier = Modifier.size(34.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = HabitIconCatalog.resolve(item.habit.iconKey),
                                contentDescription = item.habit.name,
                                tint = accent,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.habit.name,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (showStreakBadges && item.stats.currentStreak > 0) {
                            Text(
                                text = "${item.stats.currentStreak} streak",
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                            )
                        }
                    }

                    days.forEach { day ->
                        val log = item.logsByEpochDay[day]
                        val count = log?.count ?: 0
                        val status = log?.status ?: DayStatus.UNDONE
                        val target = item.habit.dailyTarget
                        val isMet = status == DayStatus.DONE || (status != DayStatus.MISS && count >= target)
                        val isMiss = status == DayStatus.MISS
                        val tileColor = when {
                            isMiss -> MissAccentColor.copy(alpha = 0.25f)
                            isMet -> accent
                            count > 0 -> accent.copy(alpha = 0.48f)
                            else -> extended.emptyTile
                        }
                        Box(
                            modifier = Modifier
                                .width(42.dp)
                                .height(48.dp)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    if (isMiss) {
                                        onSetStatus(item.habit.id, day, DayStatus.UNDONE)
                                    } else {
                                        onToggleDay(item.habit.id, day)
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(tileColor),
                                contentAlignment = Alignment.Center,
                            ) {
                                when {
                                    isMiss -> Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Missed",
                                        tint = MissAccentColor,
                                        modifier = Modifier.size(15.dp),
                                    )
                                    isMet -> Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Completed",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    count > 0 -> Text(
                                        text = "$count",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Paired quick Done/Undone + Miss controls using Material Motion FastSpatial & FastEffects springs.
 */
@Composable
private fun DayStatusActionPair(
    status: DayStatus,
    count: Int,
    dailyTarget: Int,
    accent: Color,
    onToggleDone: () -> Unit,
    onToggleMiss: () -> Unit,
    checkInTestTag: String? = null,
    missTestTag: String? = null,
) {
    val extended = LocalTileTrackColors.current
    val haptic = LocalHapticFeedback.current
    val isDone = status == DayStatus.DONE
    val isMiss = status == DayStatus.MISS

    val doneScale by animateFloatAsState(
        targetValue = if (isDone) 1.04f else 1f,
        animationSpec = TileTrackMotion.fastSpatial(),
        label = "done_scale",
    )
    val doneBg by animateColorAsState(
        targetValue = when {
            isDone -> accent
            count > 0 -> accent.copy(alpha = 0.28f)
            else -> extended.subtleSurface
        },
        animationSpec = TileTrackMotion.fastEffects(),
        label = "done_bg",
    )

    val missScale by animateFloatAsState(
        targetValue = if (isMiss) 1.04f else 1f,
        animationSpec = TileTrackMotion.fastSpatial(),
        label = "miss_scale",
    )
    val missBg by animateColorAsState(
        targetValue = if (isMiss) MissAccentColor.copy(alpha = 0.22f) else extended.subtleSurface,
        animationSpec = TileTrackMotion.fastEffects(),
        label = "miss_bg",
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onToggleMiss()
            },
            shape = RoundedCornerShape(12.dp),
            color = missBg,
            border = if (isMiss) null else BorderStroke(1.dp, extended.cardBorder),
            modifier = Modifier
                .scale(missScale)
                .size(36.dp)
                .then(if (missTestTag != null) Modifier.testTag(missTestTag) else Modifier),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = if (isMiss) "Unmark missed" else "Mark missed",
                    tint = if (isMiss) MissAccentColor else extended.mutedText,
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
            border = if (isDone || count > 0) null else BorderStroke(1.dp, extended.cardBorder),
            modifier = Modifier
                .scale(doneScale)
                .minimumInteractiveComponentSize()
                .size(44.dp)
                .then(if (checkInTestTag != null) Modifier.testTag(checkInTestTag) else Modifier),
        ) {
            Box(contentAlignment = Alignment.Center) {
                when {
                    isDone && dailyTarget == 1 -> Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Done",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                    dailyTarget > 1 -> Text(
                        text = "$count/$dailyTarget",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDone) Color.White else MaterialTheme.colorScheme.onSurface,
                    )
                    else -> Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Mark done",
                        tint = extended.mutedText,
                        modifier = Modifier.size(19.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StreakBadgePill(
    streak: Int,
    goalType: StreakGoalType,
    accent: Color,
) {
    val unit = when (goalType) {
        StreakGoalType.WEEKLY -> "w"
        StreakGoalType.MONTHLY -> "m"
        else -> "d"
    }
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = accent.copy(alpha = 0.14f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.LocalFireDepartment,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(13.dp),
            )
            Text(
                text = "$streak$unit",
                style = MaterialTheme.typography.labelSmall,
                color = accent,
            )
        }
    }
}

@Composable
private fun EmptyTasksInlineCard(
    dateLabel: String,
    onAddTask: () -> Unit,
) {
    val extended = LocalTileTrackColors.current
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, extended.cardBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.TaskAlt,
                contentDescription = null,
                tint = extended.mutedText,
                modifier = Modifier.size(36.dp),
            )
            Text("No tasks for $dateLabel", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Schedule one-off tasks alongside your daily habit grids.",
                style = MaterialTheme.typography.bodySmall,
                color = extended.mutedText,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(onClick = onAddTask) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Task")
            }
        }
    }
}

@Composable
private fun EmptyDashboardState(
    onCreateHabit: () -> Unit,
    onCreateTask: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extended = LocalTileTrackColors.current
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        TileTrackBrandGlyph(modifier = Modifier.size(68.dp))
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = stringResource(R.string.empty_habits_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.empty_habits_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = extended.mutedText,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(22.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = onCreateHabit,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("empty_create_habit_button"),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Habit")
            }
            OutlinedButton(
                onClick = onCreateTask,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("empty_create_task_button"),
            ) {
                Icon(Icons.Rounded.TaskAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Task")
            }
        }
    }
}

@Composable
fun TileTrackBrandGlyph(modifier: Modifier = Modifier) {
    val empty = LocalTileTrackColors.current.emptyTile
    Canvas(modifier = modifier) {
        val gap = size.width * 0.08f
        val cell = (size.width - gap * 2f) / 3f
        val radius = CornerRadius(cell * 0.26f, cell * 0.26f)
        val palette = listOf(
            Color(0xFF06B6D4), Color(0xFF06B6D4), Color(0xFF22C55E),
            empty, Color(0xFF22C55E), empty,
            Color(0xFFFF6B57), Color(0xFFFF6B57), Color(0xFF06B6D4),
        )
        for (r in 0..2) {
            for (c in 0..2) {
                drawRoundRect(
                    color = palette[r * 3 + c],
                    topLeft = Offset(c * (cell + gap), r * (cell + gap)),
                    size = Size(cell, cell),
                    cornerRadius = radius,
                )
            }
        }
    }
}
