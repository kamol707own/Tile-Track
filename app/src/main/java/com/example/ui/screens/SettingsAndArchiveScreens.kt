package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Unarchive
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.DashboardViewMode
import com.example.domain.HabitWithLogs
import com.example.domain.ThemeMode
import com.example.ui.components.ContributionGridPreview
import com.example.ui.components.HabitIconCatalog
import com.example.ui.theme.LocalTileTrackColors
import com.example.ui.viewmodel.SettingsUiState
import com.example.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
    onOpenArchive: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extended = LocalTileTrackColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage) {
        val msg = uiState.statusMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val json = viewModel.generateBackupJson()
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(json.toByteArray(Charsets.UTF_8))
                    }
                }
                viewModel.notifyBackupExported()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val raw = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                }
                if (raw != null) {
                    viewModel.importBackupJson(raw)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 24.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Appearance Section (Theme toggle lives exclusively here!)
            SettingsSectionCard(title = "Appearance") {
                Text("Theme", style = MaterialTheme.typography.titleSmall)
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_theme_selector"),
                ) {
                    val options = listOf(
                        ThemeMode.DARK to "Dark",
                        ThemeMode.LIGHT to "Light",
                        ThemeMode.SYSTEM to "System",
                    )
                    options.forEachIndexed { index, (mode, label) ->
                        SegmentedButton(
                            selected = uiState.preferences.themeMode == mode,
                            onClick = { viewModel.setThemeMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                            label = { Text(label) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text("Default Dashboard View", style = MaterialTheme.typography.titleSmall)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val viewModes = listOf(
                        DashboardViewMode.GRID to "Grid",
                        DashboardViewMode.COMPACT to "Compact",
                        DashboardViewMode.WEEK_STRIP to "5-Day",
                    )
                    viewModes.forEachIndexed { index, (mode, label) ->
                        SegmentedButton(
                            selected = uiState.preferences.dashboardViewMode == mode,
                            onClick = { viewModel.setDashboardViewMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = viewModes.size),
                            label = { Text(label) },
                        )
                    }
                }

                SettingsSwitchRow(
                    title = "Week starts on Monday",
                    subtitle = if (uiState.preferences.weekStartMonday) "Monday – Sunday" else "Sunday – Saturday",
                    checked = uiState.preferences.weekStartMonday,
                    onCheckedChange = viewModel::setWeekStartMonday,
                )
                SettingsSwitchRow(
                    title = "Show streak badges",
                    subtitle = "Display active streak pills on habit cards",
                    checked = uiState.preferences.showStreakBadges,
                    onCheckedChange = viewModel::setShowStreakBadges,
                )
                SettingsSwitchRow(
                    title = "Show category filter bar",
                    subtitle = "Quickly filter habits by category tag",
                    checked = uiState.preferences.showCategoryFilter,
                    onCheckedChange = viewModel::setShowCategoryFilter,
                )
            }

            // Archive Manager
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, extended.cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onOpenArchive() }
                    .testTag("settings_archive_row"),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Archive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Archived Habits", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${uiState.archivedHabits.size} archived • Tap to restore",
                            style = MaterialTheme.typography.bodySmall,
                            color = extended.mutedText,
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "Open archive",
                        tint = extended.mutedText,
                    )
                }
            }

            // Data Portability & Local-First Storage
            SettingsSectionCard(title = "Backup & Storage") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            exportLauncher.launch("tiletrack_backup_${System.currentTimeMillis() / 1000}.json")
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_json_button"),
                    ) {
                        Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export JSON")
                    }
                    OutlinedButton(
                        onClick = {
                            importLauncher.launch(arrayOf("application/json", "text/*"))
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("import_json_button"),
                    ) {
                        Icon(Icons.Rounded.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Import JSON")
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = extended.subtleSurface,
                    border = BorderStroke(1.dp, extended.cardBorder),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CloudOff,
                            contentDescription = null,
                            tint = extended.mutedText,
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Local-First Storage Active", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "${uiState.activeHabitCount} active habit(s) stored privately in Room SQLite with UUID sync metadata ready for Supabase.",
                                style = MaterialTheme.typography.bodySmall,
                                color = extended.mutedText,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    val extended = LocalTileTrackColors.current
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
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val extended = LocalTileTrackColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = extended.mutedText)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    archivedHabits: List<HabitWithLogs>,
    weekStartMonday: Boolean,
    todayEpochDay: Long,
    onNavigateBack: () -> Unit,
    onRestore: (HabitWithLogs) -> Unit,
    onDeletePermanently: (HabitWithLogs) -> Unit,
    onHabitClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val extended = LocalTileTrackColors.current
    var pendingDelete by remember { mutableStateOf<HabitWithLogs?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.archive_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
            )
        },
    ) { innerPadding ->
        if (archivedHabits.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Archive,
                    contentDescription = null,
                    tint = extended.mutedText,
                    modifier = Modifier.size(52.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.empty_archive_title), style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.empty_archive_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = extended.mutedText,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(archivedHabits, key = { it.habit.id }) { item ->
                    val accent = Color(item.habit.colorArgb)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, extended.cardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onHabitClick(item.habit.id) },
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
                                    modifier = Modifier.size(40.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = HabitIconCatalog.resolve(item.habit.iconKey),
                                            contentDescription = item.habit.name,
                                            tint = accent,
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.habit.name, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = "${item.stats.completedDaysCount} done • ${item.stats.missedDaysCount} missed",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = extended.mutedText,
                                    )
                                }
                                IconButton(
                                    onClick = { onRestore(item) },
                                    modifier = Modifier.testTag("restore_habit_${item.habit.id}"),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Unarchive,
                                        contentDescription = "Restore habit",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                IconButton(onClick = { pendingDelete = item }) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "Delete permanently",
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                            ContributionGridPreview(
                                logsByEpochDay = item.logsByEpochDay,
                                accentColor = accent,
                                dailyTarget = item.habit.dailyTarget,
                                weekStartMonday = weekStartMonday,
                                todayEpochDay = todayEpochDay,
                            )
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete Permanently?") },
            text = { Text("Remove \"${target.habit.name}\" and all recorded history forever?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePermanently(target)
                        pendingDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}
