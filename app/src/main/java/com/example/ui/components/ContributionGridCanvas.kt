package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.CompletionLog
import com.example.domain.DateUtil
import com.example.domain.DayStatus
import com.example.ui.theme.LocalTileTrackColors
import kotlin.math.floor

private val MissTileFill = Color(0xFFEF4444)

private fun DrawScope.drawContributionTile(
    x: Float,
    y: Float,
    tilePx: Float,
    radius: CornerRadius,
    log: CompletionLog?,
    dailyTarget: Int,
    accentColor: Color,
    emptyColor: Color,
) {
    val status = log?.status ?: DayStatus.UNDONE
    val count = log?.count ?: 0

    when {
        status == DayStatus.MISS -> {
            drawRoundRect(
                color = MissTileFill.copy(alpha = 0.24f),
                topLeft = Offset(x, y),
                size = Size(tilePx, tilePx),
                cornerRadius = radius,
            )
            val pad = tilePx * 0.28f
            val stroke = (tilePx * 0.12f).coerceAtLeast(1.4f)
            drawLine(
                color = MissTileFill.copy(alpha = 0.9f),
                start = Offset(x + pad, y + pad),
                end = Offset(x + tilePx - pad, y + tilePx - pad),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = MissTileFill.copy(alpha = 0.9f),
                start = Offset(x + tilePx - pad, y + pad),
                end = Offset(x + pad, y + tilePx - pad),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
        }
        status == DayStatus.DONE || count > 0 -> {
            val ratio = if (status == DayStatus.DONE) {
                1f
            } else {
                (count.toFloat() / dailyTarget.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
            }
            val fillColor = when {
                ratio <= 0f -> emptyColor
                ratio < 0.4f -> accentColor.copy(alpha = 0.36f)
                ratio < 0.8f -> accentColor.copy(alpha = 0.68f)
                else -> accentColor
            }
            drawRoundRect(
                color = fillColor,
                topLeft = Offset(x, y),
                size = Size(tilePx, tilePx),
                cornerRadius = radius,
            )
        }
        else -> {
            drawRoundRect(
                color = emptyColor,
                topLeft = Offset(x, y),
                size = Size(tilePx, tilePx),
                cornerRadius = radius,
            )
        }
    }
}

@Composable
fun ContributionGridPreview(
    logsByEpochDay: Map<Long, CompletionLog>,
    accentColor: Color,
    dailyTarget: Int,
    weekStartMonday: Boolean,
    todayEpochDay: Long,
    selectedEpochDay: Long = todayEpochDay,
    modifier: Modifier = Modifier,
    tileSize: Dp = 11.dp,
    tileGap: Dp = 3.dp,
) {
    val extended = LocalTileTrackColors.current
    val anchorDay = maxOf(todayEpochDay, selectedEpochDay)
    val currentWeekStart = DateUtil.startOfWeekEpochDay(anchorDay, weekStartMonday)

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val density = LocalDensity.current
        val cellPitchPx = with(density) { (tileSize + tileGap).toPx() }
        val tilePx = with(density) { tileSize.toPx() }
        val maxW = with(density) { maxWidth.toPx() }
        val columns = floor((maxW + with(density) { tileGap.toPx() }) / cellPitchPx)
            .toInt()
            .coerceIn(10, 28)

        val gridHeight = tileSize * 7 + tileGap * 6
        val firstWeekStart = currentWeekStart - ((columns - 1) * 7L)
        val todayBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
        val selectedBorderColor = accentColor

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(gridHeight),
        ) {
            val radius = CornerRadius(tilePx * 0.28f, tilePx * 0.28f)
            val totalGridWidth = columns * tilePx + (columns - 1) * with(density) { tileGap.toPx() }
            val startX = ((size.width - totalGridWidth) / 2f).coerceAtLeast(0f)

            for (col in 0 until columns) {
                val weekStart = firstWeekStart + col * 7L
                for (row in 0 until 7) {
                    val day = weekStart + row
                    if (day > todayEpochDay) continue

                    val x = startX + col * cellPitchPx
                    val y = row * cellPitchPx
                    drawContributionTile(
                        x = x,
                        y = y,
                        tilePx = tilePx,
                        radius = radius,
                        log = logsByEpochDay[day],
                        dailyTarget = dailyTarget,
                        accentColor = accentColor,
                        emptyColor = extended.emptyTile,
                    )

                    if (day == selectedEpochDay) {
                        drawRoundRect(
                            color = if (selectedEpochDay == todayEpochDay) todayBorderColor else selectedBorderColor,
                            topLeft = Offset(x + 0.75f, y + 0.75f),
                            size = Size(tilePx - 1.5f, tilePx - 1.5f),
                            cornerRadius = radius,
                            style = Stroke(width = 1.6f),
                        )
                    } else if (day == todayEpochDay) {
                        drawRoundRect(
                            color = todayBorderColor,
                            topLeft = Offset(x + 0.75f, y + 0.75f),
                            size = Size(tilePx - 1.5f, tilePx - 1.5f),
                            cornerRadius = radius,
                            style = Stroke(width = 1.4f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveYearHeatmap(
    logsByEpochDay: Map<Long, CompletionLog>,
    accentColor: Color,
    dailyTarget: Int,
    weekStartMonday: Boolean,
    todayEpochDay: Long,
    selectedEpochDay: Long,
    onSelectDay: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val extended = LocalTileTrackColors.current
    val currentWeekStart = DateUtil.startOfWeekEpochDay(todayEpochDay, weekStartMonday)
    val totalWeeks = 52
    val firstWeekStart = currentWeekStart - ((totalWeeks - 1) * 7L)

    val tileSize = 14.dp
    val tileGap = 4.dp
    val density = LocalDensity.current
    val tilePx = with(density) { tileSize.toPx() }
    val gapPx = with(density) { tileGap.toPx() }
    val pitchPx = tilePx + gapPx
    val totalWidth = (tileSize * totalWeeks) + (tileGap * (totalWeeks - 1))
    val gridHeight = (tileSize * 7) + (tileGap * 6)

    val scrollState = rememberScrollState()
    LaunchedEffect(Unit) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    val monthMarkers = remember(firstWeekStart) {
        var lastMonth = -1
        (0 until totalWeeks).mapNotNull { col ->
            val day = firstWeekStart + col * 7L
            val civil = DateUtil.epochDayToCivilDate(day)
            if (civil.month != lastMonth) {
                lastMonth = civil.month
                col to DateUtil.monthShortLabel(civil.month)
            } else {
                null
            }
        }
    }

    val weekdayLabels = remember(weekStartMonday) {
        if (weekStartMonday) listOf("M", "", "W", "", "F", "", "S")
        else listOf("S", "", "T", "", "T", "", "S")
    }

    val selectedOutlineColor = MaterialTheme.colorScheme.onSurface
    val todayOutlineColor = accentColor.copy(alpha = 0.85f)

    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Column(
            modifier = Modifier
                .padding(end = 6.dp, bottom = 2.dp)
                .height(gridHeight),
        ) {
            weekdayLabels.forEachIndexed { idx, label ->
                Box(
                    modifier = Modifier.height(if (idx < 6) tileSize + tileGap else tileSize),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = extended.mutedText,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollState),
        ) {
            Box(
                modifier = Modifier
                    .width(totalWidth)
                    .height(18.dp),
            ) {
                monthMarkers.forEach { (col, label) ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = extended.mutedText,
                        modifier = Modifier.padding(start = (tileSize + tileGap) * col),
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Canvas(
                modifier = Modifier
                    .width(totalWidth)
                    .height(gridHeight)
                    .pointerInput(firstWeekStart, todayEpochDay) {
                        detectTapGestures { offset ->
                            val col = (offset.x / pitchPx).toInt()
                            val row = (offset.y / pitchPx).toInt()
                            if (col in 0 until totalWeeks && row in 0 until 7) {
                                val tappedDay = firstWeekStart + col * 7L + row
                                if (tappedDay <= todayEpochDay) {
                                    onSelectDay(tappedDay)
                                }
                            }
                        }
                    },
            ) {
                val radius = CornerRadius(tilePx * 0.28f, tilePx * 0.28f)
                for (col in 0 until totalWeeks) {
                    val weekStart = firstWeekStart + col * 7L
                    for (row in 0 until 7) {
                        val day = weekStart + row
                        if (day > todayEpochDay) continue

                        val x = col * pitchPx
                        val y = row * pitchPx
                        drawContributionTile(
                            x = x,
                            y = y,
                            tilePx = tilePx,
                            radius = radius,
                            log = logsByEpochDay[day],
                            dailyTarget = dailyTarget,
                            accentColor = accentColor,
                            emptyColor = extended.emptyTile,
                        )

                        if (day == selectedEpochDay) {
                            drawRoundRect(
                                color = selectedOutlineColor,
                                topLeft = Offset(x - 1f, y - 1f),
                                size = Size(tilePx + 2f, tilePx + 2f),
                                cornerRadius = radius,
                                style = Stroke(width = 3f),
                            )
                        } else if (day == todayEpochDay) {
                            drawRoundRect(
                                color = todayOutlineColor,
                                topLeft = Offset(x + 1f, y + 1f),
                                size = Size(tilePx - 2f, tilePx - 2f),
                                cornerRadius = radius,
                                style = Stroke(width = 2f),
                            )
                        }
                    }
                }
            }
        }
    }
}
