package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.DateUtil
import com.example.domain.DayStatus
import com.example.domain.TaskItem
import com.example.domain.TaskPriority
import com.example.ui.theme.HabitColorPalette
import com.example.ui.theme.LocalTileTrackColors
import com.example.ui.theme.TileTrackMotion
import java.util.Calendar
import java.util.TimeZone

private const val MILLIS_PER_DAY = 86_400_000L
private val TaskMissColor = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorSheet(
    existing: TaskItem?,
    defaultEpochDay: Long,
    todayEpochDay: Long,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        notes: String,
        dueEpochDay: Long,
        priority: TaskPriority,
        colorArgb: Long,
        dueTimeMinutes: Int?,
        status: DayStatus,
    ) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val extended = LocalTileTrackColors.current

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var dueEpochDay by remember { mutableLongStateOf(existing?.dueEpochDay ?: defaultEpochDay) }
    var priority by remember { mutableStateOf(existing?.priority ?: TaskPriority.MEDIUM) }
    var status by remember { mutableStateOf(existing?.status ?: DayStatus.UNDONE) }
    var colorArgb by remember { mutableLongStateOf(existing?.colorArgb ?: HabitColorPalette[5].argb) }
    var dueTimeMinutes by remember { mutableStateOf(existing?.dueTimeMinutes) }
    var showTitleError by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val accent by animateColorAsState(
        targetValue = Color(colorArgb),
        animationSpec = TileTrackMotion.fastEffects(),
        label = "task_editor_accent",
    )

    val dueLabel = remember(dueEpochDay, todayEpochDay) {
        when (dueEpochDay) {
            todayEpochDay -> "Today • ${DateUtil.formatShortDate(dueEpochDay)}"
            todayEpochDay + 1L -> "Tomorrow • ${DateUtil.formatShortDate(dueEpochDay)}"
            todayEpochDay - 1L -> "Yesterday • ${DateUtil.formatShortDate(dueEpochDay)}"
            else -> DateUtil.formatFullDate(dueEpochDay)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = if (existing == null) "New Task" else "Edit Task",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = "Schedule, prioritize & organize your action item",
                        style = MaterialTheme.typography.bodySmall,
                        color = extended.mutedText,
                    )
                }
                Surface(
                    onClick = onDismiss,
                    shape = CircleShape,
                    color = extended.subtleSurface,
                    border = BorderStroke(1.dp, extended.cardBorder),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(40.dp)
                        .testTag("task_editor_close"),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close task editor",
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            // Live Task Preview Banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, extended.cardBorder),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val barColor = when (priority) {
                        TaskPriority.HIGH -> TaskMissColor
                        TaskPriority.MEDIUM -> accent
                        TaskPriority.LOW -> extended.mutedText
                    }
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(38.dp)
                            .clip(CircleShape)
                            .background(barColor),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = title.ifBlank { "Task preview" },
                            style = MaterialTheme.typography.titleMedium.copy(
                                textDecoration = if (status == DayStatus.DONE) TextDecoration.LineThrough else TextDecoration.None,
                            ),
                            color = if (title.isBlank()) extended.mutedText else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        val meta = buildString {
                            append(dueLabel)
                            dueTimeMinutes?.let {
                                append(" • ")
                                append(DateUtil.formatTime(it / 60, it % 60))
                            }
                            append(" • ")
                            append(priority.label)
                        }
                        Text(
                            text = meta,
                            style = MaterialTheme.typography.bodySmall,
                            color = extended.mutedText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = accent.copy(alpha = 0.16f),
                        modifier = Modifier.size(34.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.TaskAlt,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }

            // Title & Notes Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = extended.subtleSurface,
                border = BorderStroke(1.dp, extended.cardBorder),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            if (it.isNotBlank()) showTitleError = false
                        },
                        label = { Text("Task title") },
                        placeholder = { Text("e.g., Pay electricity bill, Call dentist") },
                        singleLine = true,
                        isError = showTitleError,
                        supportingText = if (showTitleError) {
                            { Text("Please enter a task title") }
                        } else null,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_title_input"),
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes & details (optional)") },
                        placeholder = { Text("Add checklist items, links, or context") },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_notes_input"),
                    )
                }
            }

            // Schedule Date & Time Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = extended.subtleSurface,
                border = BorderStroke(1.dp, extended.cardBorder),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Schedule", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = dueLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = accent,
                            )
                        }
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("task_pick_date_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pick Date")
                        }
                    }

                    // 4 symmetric quick schedule pills
                    val quickOptions = listOf(
                        todayEpochDay to "Today",
                        (todayEpochDay + 1L) to "Tomorrow",
                        (todayEpochDay + 3L) to "+3 Days",
                        (todayEpochDay + 7L) to "Next Week",
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        quickOptions.forEach { (day, label) ->
                            val selected = dueEpochDay == day
                            Surface(
                                onClick = { dueEpochDay = day },
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface,
                                border = if (selected) null else BorderStroke(1.dp, extended.cardBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 40.dp),
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = extended.cardBorder)

                    // Time row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            onClick = { showTimePicker = true },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, extended.cardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 46.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AccessTime,
                                    contentDescription = null,
                                    tint = if (dueTimeMinutes != null) accent else extended.mutedText,
                                    modifier = Modifier.size(18.dp),
                                )
                                val timeLabel = dueTimeMinutes?.let {
                                    DateUtil.formatTime(it / 60, it % 60)
                                } ?: "Add time (optional)"
                                Text(
                                    text = timeLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (dueTimeMinutes != null) MaterialTheme.colorScheme.onSurface else extended.mutedText,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                        if (dueTimeMinutes != null) {
                            Surface(
                                onClick = { dueTimeMinutes = null },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, extended.cardBorder),
                                modifier = Modifier.size(46.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Clear time",
                                        tint = extended.mutedText,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Priority & Status Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = extended.subtleSurface,
                border = BorderStroke(1.dp, extended.cardBorder),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Priority", style = MaterialTheme.typography.titleSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TaskPriority.entries.forEach { item ->
                            val selected = priority == item
                            val pColor = when (item) {
                                TaskPriority.LOW -> MaterialTheme.colorScheme.onSurface
                                TaskPriority.MEDIUM -> accent
                                TaskPriority.HIGH -> TaskMissColor
                            }
                            val pSelectedFg = if (item == TaskPriority.LOW) {
                                MaterialTheme.colorScheme.surface
                            } else {
                                Color.White
                            }
                            val pIcon = when (item) {
                                TaskPriority.LOW -> Icons.Rounded.KeyboardArrowDown
                                TaskPriority.MEDIUM -> Icons.Rounded.Remove
                                TaskPriority.HIGH -> Icons.Rounded.KeyboardArrowUp
                            }
                            Surface(
                                onClick = { priority = item },
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) pColor else MaterialTheme.colorScheme.surface,
                                border = if (selected) null else BorderStroke(1.dp, extended.cardBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Icon(
                                        imageVector = pIcon,
                                        contentDescription = null,
                                        tint = if (selected) pSelectedFg else extended.mutedText,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (selected) pSelectedFg else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }

                    if (existing != null) {
                        HorizontalDivider(color = extended.cardBorder)
                        Text("Status", style = MaterialTheme.typography.titleSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf(
                                Triple(DayStatus.UNDONE, "Pending", Icons.Rounded.RadioButtonUnchecked),
                                Triple(DayStatus.DONE, "Done", Icons.Rounded.Check),
                                Triple(DayStatus.MISS, "Missed", Icons.Rounded.Close),
                            ).forEach { (st, label, icon) ->
                                val selected = status == st
                                val sColor = when (st) {
                                    DayStatus.UNDONE -> MaterialTheme.colorScheme.onSurface
                                    DayStatus.DONE -> accent
                                    DayStatus.MISS -> TaskMissColor
                                }
                                Surface(
                                    onClick = { status = st },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selected) sColor else MaterialTheme.colorScheme.surface,
                                    border = if (selected) null else BorderStroke(1.dp, extended.cardBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 42.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                    ) {
                                        val fgColor = if (selected) {
                                            if (st == DayStatus.UNDONE) MaterialTheme.colorScheme.surface else Color.White
                                        } else {
                                            extended.mutedText
                                        }
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = fgColor,
                                            modifier = Modifier.size(15.dp),
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (selected) fgColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Symmetric 6x2 Tag Color Grid (12 Swatches)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = extended.subtleSurface,
                border = BorderStroke(1.dp, extended.cardBorder),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    val taskPalette = HabitColorPalette.take(12)
                    val selectedColorName = taskPalette.firstOrNull { it.argb == colorArgb }?.name ?: "Accent"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Tag Color", style = MaterialTheme.typography.titleSmall)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = accent.copy(alpha = 0.16f),
                        ) {
                            Text(
                                text = selectedColorName,
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        taskPalette.chunked(6).forEach { rowSwatches ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                rowSwatches.forEach { swatch ->
                                    val selected = swatch.argb == colorArgb
                                    val scale by animateFloatAsState(
                                        targetValue = if (selected) 1.08f else 1f,
                                        animationSpec = TileTrackMotion.fastSpatial(),
                                        label = "task_color_${swatch.name}",
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .scale(scale)
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(Color(swatch.argb))
                                                .border(
                                                    width = if (selected) 2.5.dp else 1.dp,
                                                    color = if (selected) MaterialTheme.colorScheme.onSurface else Color.White.copy(alpha = 0.12f),
                                                    shape = CircleShape,
                                                )
                                                .clickable { colorArgb = swatch.argb },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            if (selected) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Check,
                                                    contentDescription = swatch.name,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp),
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

            Button(
                onClick = {
                    if (title.isBlank()) {
                        showTitleError = true
                    } else {
                        onSave(title, notes, dueEpochDay, priority, colorArgb, dueTimeMinutes, status)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_task_button"),
            ) {
                Text(
                    text = if (existing == null) "Create Task" else "Save Changes",
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueEpochDay * MILLIS_PER_DAY,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { utcMillis ->
                            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = utcMillis
                            }
                            dueEpochDay = DateUtil.yearMonthDayToEpochDay(
                                year = cal.get(Calendar.YEAR),
                                month = cal.get(Calendar.MONTH) + 1,
                                day = cal.get(Calendar.DAY_OF_MONTH),
                            )
                        }
                        showDatePicker = false
                    },
                ) {
                    Text("Select")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val initHour = (dueTimeMinutes ?: (9 * 60)) / 60
        val initMin = (dueTimeMinutes ?: (9 * 60)) % 60
        val pickerState = rememberTimePickerState(
            initialHour = initHour,
            initialMinute = initMin,
            is24Hour = false,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Task Time") },
            text = { TimePicker(state = pickerState) },
            confirmButton = {
                TextButton(
                    onClick = {
                        dueTimeMinutes = pickerState.hour * 60 + pickerState.minute
                        showTimePicker = false
                    },
                ) {
                    Text("Set")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}
