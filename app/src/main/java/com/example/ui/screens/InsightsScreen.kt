package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.CompletionRingGauge
import com.example.ui.components.HabitIconCatalog
import com.example.ui.components.SevenDayMomentumChart
import com.example.ui.theme.LocalTileTrackColors
import com.example.ui.theme.MetricNumberStyle
import com.example.ui.viewmodel.InsightsUiState
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    uiState: InsightsUiState,
    onHabitClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val extended = LocalTileTrackColors.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = {
                    Text(
                        text = stringResource(R.string.insights_title),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
            )
        },
    ) { innerPadding ->
        if (uiState.activeHabits.isEmpty() && uiState.tasks.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Insights,
                    contentDescription = null,
                    tint = extended.mutedText,
                    modifier = Modifier.size(56.dp),
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text("No activity data yet", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Create and check off habits or tasks on your dashboard to unlock streaks and consistency trends.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = extended.mutedText,
                    textAlign = TextAlign.Center,
                )
            }
            return@Scaffold
        }

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
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, extended.cardBorder),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    CompletionRingGauge(
                        progress = uiState.todayCompletionRatio,
                        accentColor = MaterialTheme.colorScheme.primary,
                        centerLabel = "${(uiState.todayCompletionRatio * 100).roundToInt()}%",
                        caption = "Habits",
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        InsightStatRow(
                            label = "Done today",
                            value = "${uiState.todayCompletedHabits} / ${uiState.totalActiveHabits}",
                        )
                        InsightStatRow(
                            label = "30d Avg Consistency",
                            value = "${uiState.averageConsistencyPercent}%",
                        )
                        InsightStatRow(
                            label = "Done / Missed Tiles",
                            value = "${uiState.totalLoggedTiles} / ${uiState.totalMissedTiles}",
                        )
                        InsightStatRow(
                            label = "Tasks Done / Open",
                            value = "${uiState.tasksDoneCount} / ${uiState.tasksPendingCount}",
                        )
                    }
                }
            }

            if (uiState.activeHabits.isNotEmpty()) {
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
                        Text("7-Day Momentum", style = MaterialTheme.typography.titleMedium)
                        SevenDayMomentumChart(
                            points = uiState.last7DaysMomentum,
                            accentColor = MaterialTheme.colorScheme.primary,
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
                        Text("Top Active Streaks", style = MaterialTheme.typography.titleMedium)
                        uiState.topStreaks.forEach { item ->
                            val accent = Color(item.habit.colorArgb)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onHabitClick(item.habit.id) }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = accent.copy(alpha = 0.16f),
                                    modifier = Modifier.size(38.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = HabitIconCatalog.resolve(item.habit.iconKey),
                                            contentDescription = item.habit.name,
                                            tint = accent,
                                            modifier = Modifier.size(19.dp),
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.habit.name, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = "${item.stats.consistency30DaysPercent}% 30d • ${item.stats.missedDaysCount} missed",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = extended.mutedText,
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = accent,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Text(
                                        text = "${item.stats.currentStreak}",
                                        style = MetricNumberStyle,
                                        color = accent,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InsightStatRow(
    label: String,
    value: String,
) {
    val extended = LocalTileTrackColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = extended.mutedText)
        Text(value, style = MetricNumberStyle)
    }
}
