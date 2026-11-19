package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.domain.DateUtil
import com.example.domain.HabitCategoryDefaults
import com.example.domain.HabitReminder
import com.example.domain.HabitWithLogs
import com.example.domain.StreakGoalType
import com.example.ui.theme.HabitColorPalette
import com.example.ui.theme.LocalTileTrackColors
import com.example.ui.theme.MetricNumberStyle
import com.example.ui.theme.TileTrackMotion
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitEditorSheet(
    existing: HabitWithLogs?,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        description: String,
        iconKey: String,
        colorArgb: Long,
        category: String,
        dailyTarget: Int,
        streakGoalType: StreakGoalType,
        streakGoalTarget: Int,
        reminders: List<HabitReminder>,
    ) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val extended = LocalTileTrackColors.current
    val context = LocalContext.current

    var name by remember { mutableStateOf(existing?.habit?.name ?: "") }
    var description by remember { mutableStateOf(existing?.habit?.description ?: "") }
    var iconKey by remember { mutableStateOf(existing?.habit?.iconKey ?: "check") }
    var colorArgb by remember { mutableLongStateOf(existing?.habit?.colorArgb ?: HabitColorPalette.first().argb) }
    var category by remember { mutableStateOf(existing?.habit?.category ?: "General") }
    var dailyTarget by remember { mutableIntStateOf(existing?.habit?.dailyTarget ?: 1) }
    var streakGoalType by remember { mutableStateOf(existing?.habit?.streakGoalType ?: StreakGoalType.DAILY) }
    var streakGoalTarget by remember { mutableIntStateOf(existing?.habit?.streakGoalTarget ?: 1) }
    var reminders by remember { mutableStateOf(existing?.reminders ?: emptyList()) }
    var showNameError by remember { mutableStateOf(false) }

    var editingReminderIndex by remember { mutableStateOf<Int?>(null) }
    var showTimePickerForNew by remember { mutableStateOf(false) }
    var notificationPermissionDeniedNote by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            notificationPermissionDeniedNote = false
            showTimePickerForNew = true
        } else {
            notificationPermissionDeniedNote = true
        }
    }

    val accent by animateColorAsState(
        targetValue = Color(colorArgb),
        animationSpec = TileTrackMotion.fastEffects(),
        label = "habit_editor_accent",
    )
    val selectedColorName = remember(colorArgb) {
        HabitColorPalette.firstOrNull { it.argb == colorArgb }?.name ?: "Custom"
    }
    val selectedIconLabel = remember(iconKey) {
        HabitIconCatalog.labelFor(iconKey)
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
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = if (existing == null) "New Habit" else "Edit Habit",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = "Configure your tile grid, goal & alerts",
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
                        .testTag("editor_close_button"),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close editor",
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            // Live Habit Preview Card
            HabitLivePreviewCard(
                name = name,
                description = description,
                category = category,
                iconKey = iconKey,
                accent = accent,
                dailyTarget = dailyTarget,
            )

            // Identity Inputs
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
                        value = name,
                        onValueChange = {
                            name = it
                            if (it.isNotBlank()) showNameError = false
                        },
                        label = { Text("Habit name") },
                        placeholder = { Text("e.g., Read 20 pages, Run 5km") },
                        singleLine = true,
                        isError = showNameError,
                        supportingText = if (showNameError) {
                            { Text("Please enter a habit name") }
                        } else null,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("habit_name_input"),
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (optional)") },
                        placeholder = { Text("Short note or motivation") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("habit_description_input"),
                    )
                }
            }

            // Appearance Card: Symmetric 6x3 Color Grid + Symmetric 6x5 Icon Grid
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = extended.subtleSurface,
                border = BorderStroke(1.dp, extended.cardBorder),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Tile Color (6 columns x 3 rows = 18 colors, 100% symmetric)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Tile Color", style = MaterialTheme.typography.titleSmall)
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
                            HabitColorPalette.chunked(6).forEach { rowSwatches ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    rowSwatches.forEach { swatch ->
                                        val selected = swatch.argb == colorArgb
                                        val scale by animateFloatAsState(
                                            targetValue = if (selected) 1.08f else 1f,
                                            animationSpec = TileTrackMotion.fastSpatial(),
                                            label = "color_scale_${swatch.name}",
                                        )
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .scale(scale)
                                                    .size(36.dp)
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
                                                        modifier = Modifier.size(17.dp),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = extended.cardBorder)

                    // Icon Picker (6 columns x 5 rows = 30 icons, 100% symmetric)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Icon", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = selectedIconLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = extended.mutedText,
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            HabitIconCatalog.options.chunked(6).forEach { rowIcons ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    rowIcons.forEach { option ->
                                        val selected = option.key == iconKey
                                        Surface(
                                            onClick = { iconKey = option.key },
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (selected) accent else MaterialTheme.colorScheme.surface,
                                            border = if (selected) null else BorderStroke(1.dp, extended.cardBorder),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp),
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = option.icon,
                                                    contentDescription = option.label,
                                                    tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(21.dp),
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

            // Category (Symmetric 3x3 Grid)
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
                    Text("Category", style = MaterialTheme.typography.titleSmall)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HabitCategoryDefaults.presets.chunked(3).forEach { rowCategories ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                rowCategories.forEach { preset ->
                                    val selected = category == preset
                                    Surface(
                                        onClick = { category = preset },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface,
                                        border = if (selected) null else BorderStroke(1.dp, extended.cardBorder),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 40.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = preset,
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
                        }
                    }
                }
            }

            // Daily Target & Streak Goal
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = extended.subtleSurface,
                border = BorderStroke(1.dp, extended.cardBorder),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Completions per day", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "Tap check-in multiple times to fill tile intensity",
                                style = MaterialTheme.typography.bodySmall,
                                color = extended.mutedText,
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, extended.cardBorder),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (dailyTarget > 1) dailyTarget-- },
                                    modifier = Modifier.size(40.dp),
                                ) {
                                    Icon(
                                        Icons.Rounded.Remove,
                                        contentDescription = "Decrease target",
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                                Text(
                                    text = "$dailyTarget",
                                    style = MetricNumberStyle.copy(fontSize = MaterialTheme.typography.titleMedium.fontSize),
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                )
                                IconButton(
                                    onClick = { if (dailyTarget < 10) dailyTarget++ },
                                    modifier = Modifier.size(40.dp),
                                ) {
                                    Icon(
                                        Icons.Rounded.Add,
                                        contentDescription = "Increase target",
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = extended.cardBorder)

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Streak Goal Interval", style = MaterialTheme.typography.titleSmall)
                        // 4 equal-width symmetric options
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            StreakGoalType.entries.forEach { type ->
                                val selected = streakGoalType == type
                                Surface(
                                    onClick = {
                                        streakGoalType = type
                                        if (type == StreakGoalType.WEEKLY && streakGoalTarget < 2) {
                                            streakGoalTarget = 3
                                        } else if (type == StreakGoalType.MONTHLY && streakGoalTarget < 5) {
                                            streakGoalTarget = 12
                                        } else if (type == StreakGoalType.DAILY) {
                                            streakGoalTarget = 1
                                        }
                                    },
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
                                            text = type.label,
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

                        AnimatedVisibility(
                            visible = streakGoalType == StreakGoalType.WEEKLY || streakGoalType == StreakGoalType.MONTHLY,
                            enter = fadeIn() + expandVertically(TileTrackMotion.fastSpatial()),
                            exit = fadeOut() + shrinkVertically(TileTrackMotion.fastSpatial()),
                        ) {
                            val maxGoal = if (streakGoalType == StreakGoalType.WEEKLY) 7 else 30
                            val unitLabel = if (streakGoalType == StreakGoalType.WEEKLY) "days / week" else "days / month"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Target ($unitLabel)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = extended.mutedText,
                                )
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, extended.cardBorder),
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { if (streakGoalTarget > 1) streakGoalTarget-- },
                                            modifier = Modifier.size(38.dp),
                                        ) {
                                            Icon(Icons.Rounded.Remove, contentDescription = "Decrease goal", modifier = Modifier.size(16.dp))
                                        }
                                        Text(
                                            text = "$streakGoalTarget",
                                            style = MetricNumberStyle,
                                            modifier = Modifier.padding(horizontal = 8.dp),
                                        )
                                        IconButton(
                                            onClick = { if (streakGoalTarget < maxGoal) streakGoalTarget++ },
                                            modifier = Modifier.size(38.dp),
                                        ) {
                                            Icon(Icons.Rounded.Add, contentDescription = "Increase goal", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Multiple Daily Reminders (Pro Feature Unlocked)
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Reminders (${reminders.size}/5)", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "Schedule multiple alerts per habit",
                                style = MaterialTheme.typography.bodySmall,
                                color = extended.mutedText,
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                if (reminders.size >= 5) return@OutlinedButton
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS,
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (granted) {
                                        showTimePickerForNew = true
                                    } else {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                } else {
                                    showTimePickerForNew = true
                                }
                            },
                            enabled = reminders.size < 5,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("add_reminder_button"),
                        ) {
                            Icon(Icons.Rounded.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add")
                        }
                    }

                    if (notificationPermissionDeniedNote) {
                        Text(
                            text = "Notification permission was denied. Enable notifications in Android Settings to receive alerts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    reminders.forEachIndexed { index, reminder ->
                        ReminderRowCard(
                            reminder = reminder,
                            accent = accent,
                            onToggleEnabled = { enabled ->
                                reminders = reminders.toMutableList().apply {
                                    this[index] = reminder.copy(isEnabled = enabled)
                                }
                            },
                            onEditTime = { editingReminderIndex = index },
                            onToggleDay = { isoDay ->
                                val bit = 1 shl (isoDay - 1)
                                val newMask = reminder.daysOfWeekMask xor bit
                                if (newMask != 0) {
                                    reminders = reminders.toMutableList().apply {
                                        this[index] = reminder.copy(daysOfWeekMask = newMask)
                                    }
                                }
                            },
                            onDelete = {
                                reminders = reminders.toMutableList().apply { removeAt(index) }
                            },
                        )
                    }
                }
            }

            // Save Button
            Button(
                onClick = {
                    if (name.isBlank()) {
                        showNameError = true
                    } else {
                        onSave(
                            name,
                            description,
                            iconKey,
                            colorArgb,
                            category,
                            dailyTarget,
                            streakGoalType,
                            streakGoalTarget,
                            reminders,
                        )
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
                    .testTag("save_habit_button"),
            ) {
                Text(
                    text = if (existing == null) "Create Habit" else "Save Changes",
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showTimePickerForNew) {
        HabitTimePickerDialog(
            initialHour = 9,
            initialMinute = 0,
            onDismiss = { showTimePickerForNew = false },
            onConfirm = { h, m ->
                reminders = reminders + HabitReminder(
                    id = UUID.randomUUID().toString(),
                    habitId = existing?.habit?.id ?: "",
                    hour = h,
                    minute = m,
                    daysOfWeekMask = HabitReminder.ALL_DAYS_MASK,
                    isEnabled = true,
                )
                showTimePickerForNew = false
            },
        )
    }

    val editIdx = editingReminderIndex
    if (editIdx != null && editIdx in reminders.indices) {
        val currentRem = reminders[editIdx]
        HabitTimePickerDialog(
            initialHour = currentRem.hour,
            initialMinute = currentRem.minute,
            onDismiss = { editingReminderIndex = null },
            onConfirm = { h, m ->
                reminders = reminders.toMutableList().apply {
                    this[editIdx] = currentRem.copy(hour = h, minute = m)
                }
                editingReminderIndex = null
            },
        )
    }
}

@Composable
private fun HabitLivePreviewCard(
    name: String,
    description: String,
    category: String,
    iconKey: String,
    accent: Color,
    dailyTarget: Int,
) {
    val extended = LocalTileTrackColors.current
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, extended.cardBorder),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
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
                            imageVector = HabitIconCatalog.resolve(iconKey),
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name.ifBlank { "Habit preview" },
                        style = MaterialTheme.typography.titleMedium,
                        color = if (name.isBlank()) extended.mutedText else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val subtitle = buildString {
                        append(description.ifBlank { category })
                        if (dailyTarget > 1) append(" • ${dailyTarget}x/day")
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = extended.mutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accent,
                    modifier = Modifier.size(34.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            // Symmetric 12-tile mini heatmap preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                val pattern = listOf(0.25f, 1f, 1f, 0f, 0.7f, 1f, 1f, 0.45f, 1f, 0f, 1f, 1f)
                pattern.forEach { ratio ->
                    val tileColor = if (ratio <= 0f) extended.emptyTile else accent.copy(alpha = ratio)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(tileColor),
                    )
                }
            }
        }
    }
}

@Composable
private fun ReminderRowCard(
    reminder: HabitReminder,
    accent: Color,
    onToggleEnabled: (Boolean) -> Unit,
    onEditTime: () -> Unit,
    onToggleDay: (Int) -> Unit,
    onDelete: () -> Unit,
) {
    val extended = LocalTileTrackColors.current
    val dayShort = listOf("M", "T", "W", "T", "F", "S", "S")

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, extended.cardBorder),
    ) {
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
                Text(
                    text = DateUtil.formatTime(reminder.hour, reminder.minute),
                    style = MetricNumberStyle.copy(fontSize = MaterialTheme.typography.titleLarge.fontSize),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onEditTime() }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = reminder.isEnabled,
                        onCheckedChange = onToggleEnabled,
                    )
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Remove reminder",
                            tint = extended.mutedText,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                (1..7).forEach { isoDay ->
                    val active = reminder.isDayEnabled(isoDay)
                    Surface(
                        onClick = { onToggleDay(isoDay) },
                        shape = CircleShape,
                        color = if (active) accent else extended.subtleSurface,
                        border = BorderStroke(1.dp, if (active) accent else extended.cardBorder),
                        modifier = Modifier.size(34.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = dayShort[isoDay - 1],
                                style = MaterialTheme.typography.labelMedium,
                                color = if (active) Color.White else extended.mutedText,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Reminder Time") },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text("Set Time")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
