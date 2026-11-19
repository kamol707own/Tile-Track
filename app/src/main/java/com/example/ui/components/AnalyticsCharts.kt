package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.MonthlyBarPoint
import com.example.domain.WeekdayStat
import com.example.ui.theme.LocalTileTrackColors
import com.example.ui.theme.MetricNumberStyle
import com.example.ui.theme.TileTrackMotion
import com.example.ui.viewmodel.DayMomentumPoint
import kotlin.math.roundToInt

@Composable
fun CompletionRingGauge(
    progress: Float,
    accentColor: Color,
    centerLabel: String,
    caption: String,
    modifier: Modifier = Modifier,
    ringSize: Dp = 108.dp,
) {
    val extended = LocalTileTrackColors.current
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = TileTrackMotion.DurationLong1,
            easing = TileTrackMotion.EasingEmphasizedDecelerate,
        ),
        label = "ring_progress",
    )

    Box(
        modifier = modifier.size(ringSize),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(ringSize)) {
            val strokePx = 11.dp.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(size.width - strokePx, size.height - strokePx)
            drawArc(
                color = extended.emptyTile,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
            if (animatedProgress > 0f) {
                drawArc(
                    color = accentColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = centerLabel,
                style = MetricNumberStyle.copy(fontSize = MaterialTheme.typography.titleLarge.fontSize),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall,
                color = extended.mutedText,
            )
        }
    }
}

@Composable
fun MonthlyBarChart(
    bars: List<MonthlyBarPoint>,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val extended = LocalTileTrackColors.current
    val chartHeight = 128.dp

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            bars.forEach { bar ->
                val ratio = (bar.completedDays.toFloat() / bar.totalDays.toFloat()).coerceIn(0f, 1f)
                val animRatio by animateFloatAsState(
                    targetValue = ratio,
                    animationSpec = tween(
                        durationMillis = TileTrackMotion.DurationMedium3,
                        easing = TileTrackMotion.EasingEmphasizedDecelerate,
                    ),
                    label = "bar_${bar.label}",
                )
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Text(
                        text = "${bar.completedDays}",
                        style = MaterialTheme.typography.labelSmall,
                        color = extended.mutedText,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(92.dp),
                    ) {
                        val barWidth = size.width * 0.62f
                        val x = (size.width - barWidth) / 2f
                        val radius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        drawRoundRect(
                            color = extended.emptyTile,
                            topLeft = Offset(x, 0f),
                            size = Size(barWidth, size.height),
                            cornerRadius = radius,
                        )
                        val fillH = (size.height * animRatio).coerceAtLeast(if (bar.completedDays > 0) 6.dp.toPx() else 0f)
                        if (fillH > 0f) {
                            drawRoundRect(
                                color = accentColor,
                                topLeft = Offset(x, size.height - fillH),
                                size = Size(barWidth, fillH),
                                cornerRadius = radius,
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            bars.forEach { bar ->
                Text(
                    text = bar.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = extended.mutedText,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun WeekdayBreakdownBars(
    stats: List<WeekdayStat>,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val extended = LocalTileTrackColors.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        stats.forEach { day ->
            val animRatio by animateFloatAsState(
                targetValue = day.ratio.coerceIn(0f, 1f),
                animationSpec = tween(
                    durationMillis = TileTrackMotion.DurationMedium2,
                    easing = TileTrackMotion.EasingEmphasizedDecelerate,
                ),
                label = "weekday_${day.label}",
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = day.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = extended.mutedText,
                    modifier = Modifier.width(36.dp),
                )
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(12.dp),
                ) {
                    val radius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    drawRoundRect(
                        color = extended.emptyTile,
                        topLeft = Offset.Zero,
                        size = size,
                        cornerRadius = radius,
                    )
                    if (day.completionCount > 0) {
                        val w = (size.width * animRatio).coerceAtLeast(10.dp.toPx())
                        drawRoundRect(
                            color = accentColor,
                            topLeft = Offset.Zero,
                            size = Size(w, size.height),
                            cornerRadius = radius,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${day.completionCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(28.dp),
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

@Composable
fun SevenDayMomentumChart(
    points: List<DayMomentumPoint>,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val extended = LocalTileTrackColors.current
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            points.forEach { pt ->
                val ratio = (pt.completedCount.toFloat() / pt.totalCount.toFloat()).coerceIn(0f, 1f)
                val animRatio by animateFloatAsState(
                    targetValue = ratio,
                    animationSpec = tween(
                        durationMillis = TileTrackMotion.DurationMedium3,
                        easing = TileTrackMotion.EasingEmphasizedDecelerate,
                    ),
                    label = "momentum_${pt.label}",
                )
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "${(ratio * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = extended.mutedText,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(82.dp),
                    ) {
                        val barW = size.width * 0.56f
                        val x = (size.width - barW) / 2f
                        val radius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        drawRoundRect(
                            color = extended.emptyTile,
                            topLeft = Offset(x, 0f),
                            size = Size(barW, size.height),
                            cornerRadius = radius,
                        )
                        val fillH = (size.height * animRatio).coerceAtLeast(if (pt.completedCount > 0) 6.dp.toPx() else 0f)
                        if (fillH > 0f) {
                            drawRoundRect(
                                color = accentColor,
                                topLeft = Offset(x, size.height - fillH),
                                size = Size(barW, fillH),
                                cornerRadius = radius,
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            points.forEach { pt ->
                Text(
                    text = pt.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = extended.mutedText,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
