package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.DateUtil
import com.example.ui.theme.LocalTileTrackColors
import com.example.ui.theme.MetricNumberStyle
import com.example.ui.theme.TileTrackMotion

/**
 * Cardless, edge-to-edge horizontal calendar strip featuring two-tier rounded day capsules
 * with Material Motion spring/effects feedback.
 */
@Composable
fun WeekCalendarStrip(
    weekStartEpochDay: Long,
    selectedEpochDay: Long,
    todayEpochDay: Long,
    onSelectDay: (Long) -> Unit,
    onShiftWeek: (Int) -> Unit,
    onJumpToToday: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extended = LocalTileTrackColors.current
    val haptic = LocalHapticFeedback.current

    val startDay = remember(todayEpochDay) { todayEpochDay - 45L }
    val endDay = remember(todayEpochDay) { todayEpochDay + 21L }
    val days = remember(startDay, endDay) { (startDay..endDay).toList() }

    val initialIdx = remember(selectedEpochDay, days) {
        val idx = days.indexOf(selectedEpochDay)
        if (idx >= 0) (idx - 3).coerceAtLeast(0) else 0
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIdx)
    var lastWeekStart by remember { mutableLongStateOf(weekStartEpochDay) }

    LaunchedEffect(selectedEpochDay) {
        val idx = days.indexOf(selectedEpochDay)
        if (idx >= 0) {
            listState.animateScrollToItem((idx - 3).coerceAtLeast(0))
        }
    }

    LaunchedEffect(weekStartEpochDay) {
        if (weekStartEpochDay != lastWeekStart) {
            lastWeekStart = weekStartEpochDay
            val idx = days.indexOf(weekStartEpochDay)
            if (idx >= 0) {
                listState.animateScrollToItem(idx)
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = DateUtil.formatMonthYear(selectedEpochDay),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                if (selectedEpochDay != todayEpochDay) {
                    Surface(
                        onClick = onJumpToToday,
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.testTag("calendar_jump_today"),
                    ) {
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onShiftWeek(-1) },
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("calendar_prev_week"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = "Previous week",
                        tint = extended.mutedText,
                    )
                }
                IconButton(
                    onClick = { onShiftWeek(1) },
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("calendar_next_week"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "Next week",
                        tint = extended.mutedText,
                    )
                }
            }
        }

        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(days, key = { it }) { day ->
                val civil = DateUtil.epochDayToCivilDate(day)
                val wdLabel = DateUtil.weekdayShortLabel(DateUtil.dayOfWeekIso(day))
                val isSelected = day == selectedEpochDay
                val isToday = day == todayEpochDay

                val topCapColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.80f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    },
                    animationSpec = TileTrackMotion.fastEffects(),
                    label = "day_cap_$day",
                )
                val bottomBodyColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    animationSpec = TileTrackMotion.fastEffects(),
                    label = "day_body_$day",
                )
                val weekdayColor by animateColorAsState(
                    targetValue = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        isToday -> MaterialTheme.colorScheme.primary
                        else -> extended.mutedText
                    },
                    animationSpec = TileTrackMotion.fastEffects(),
                    label = "day_wd_$day",
                )
                val numberColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    animationSpec = TileTrackMotion.fastEffects(),
                    label = "day_num_$day",
                )
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.03f else 1f,
                    animationSpec = TileTrackMotion.fastSpatial(),
                    label = "day_scale_$day",
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .scale(scale)
                        .width(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(topCapColor)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelectDay(day)
                        }
                        .testTag("calendar_day_$day"),
                ) {
                    Text(
                        text = wdLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isSelected || isToday) FontWeight.SemiBold else FontWeight.Medium,
                        ),
                        color = weekdayColor,
                        modifier = Modifier.padding(top = 6.dp, bottom = 5.dp),
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(bottomBodyColor)
                            .padding(top = 8.dp, bottom = 6.dp),
                    ) {
                        Text(
                            text = "${civil.day}",
                            style = MetricNumberStyle.copy(
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            ),
                            color = numberColor,
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(3.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isToday && isSelected -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                        isToday -> MaterialTheme.colorScheme.primary
                                        else -> Color.Transparent
                                    },
                                ),
                        )
                    }
                }
            }
        }
    }
}
