package com.example.ui.theme

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset

/**
 * Material Motion system translated directly from material-components-android Motion.md:
 * - Fast / Default / Slow Spatial & Effects Springs
 * - Standard & Emphasized CubicBezier Easings and Duration tokens
 * - Fade Through, Shared Axis (X/Y/Z), and Fade transition patterns
 */
object TileTrackMotion {

    // Durations (ms)
    const val DurationShort1 = 50
    const val DurationShort2 = 100
    const val DurationShort3 = 150
    const val DurationShort4 = 200
    const val DurationMedium1 = 250
    const val DurationMedium2 = 300
    const val DurationMedium3 = 350
    const val DurationMedium4 = 400
    const val DurationLong1 = 450

    // Easing curves
    val EasingStandard = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val EasingStandardDecelerate = CubicBezierEasing(0f, 0f, 0f, 1f)
    val EasingStandardAccelerate = CubicBezierEasing(0.3f, 0f, 1f, 1f)
    val EasingEmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val EasingEmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
    val EasingLinear = LinearEasing

    // Spatial Springs (damping 0.9 — crisp minimal settle without bouncy clutter)
    fun <T> fastSpatial(): SpringSpec<T> = spring(
        dampingRatio = 0.9f,
        stiffness = 1400f,
    )

    fun <T> defaultSpatial(): SpringSpec<T> = spring(
        dampingRatio = 0.9f,
        stiffness = 700f,
    )

    fun <T> slowSpatial(): SpringSpec<T> = spring(
        dampingRatio = 0.9f,
        stiffness = 300f,
    )

    // Effects Springs (damping 1.0 — critically damped, zero overshoot for color/alpha)
    fun fastEffects(): SpringSpec<Color> = spring(
        dampingRatio = 1.0f,
        stiffness = 3800f,
    )

    fun fastEffectsFloat(): SpringSpec<Float> = spring(
        dampingRatio = 1.0f,
        stiffness = 3800f,
    )

    fun defaultEffects(): SpringSpec<Color> = spring(
        dampingRatio = 1.0f,
        stiffness = 1600f,
    )

    // Fade Through (Top-level destinations: Habits / Insights / Settings)
    val fadeThroughEnter: EnterTransition = fadeIn(
        animationSpec = tween(
            durationMillis = DurationMedium1,
            delayMillis = DurationShort1,
            easing = EasingStandardDecelerate,
        ),
    ) + scaleIn(
        initialScale = 0.96f,
        animationSpec = tween(
            durationMillis = DurationMedium1,
            delayMillis = DurationShort1,
            easing = EasingEmphasizedDecelerate,
        ),
    )

    val fadeThroughExit: ExitTransition = fadeOut(
        animationSpec = tween(
            durationMillis = DurationShort2,
            easing = EasingStandardAccelerate,
        ),
    ) + scaleOut(
        targetScale = 0.98f,
        animationSpec = tween(
            durationMillis = DurationShort2,
            easing = EasingStandardAccelerate,
        ),
    )

    // Shared Axis X (Forward / Backward screen hierarchy)
    val sharedAxisXForwardEnter: EnterTransition = slideInHorizontally(
        animationSpec = tween(DurationMedium2, easing = EasingEmphasizedDecelerate),
        initialOffsetX = { (it * 0.18f).toInt() },
    ) + fadeIn(
        animationSpec = tween(DurationMedium1, easing = EasingStandardDecelerate),
    )

    val sharedAxisXForwardExit: ExitTransition = slideOutHorizontally(
        animationSpec = tween(DurationMedium1, easing = EasingStandardAccelerate),
        targetOffsetX = { -(it * 0.10f).toInt() },
    ) + fadeOut(
        animationSpec = tween(DurationShort3, easing = EasingStandardAccelerate),
    )

    val sharedAxisXBackwardEnter: EnterTransition = slideInHorizontally(
        animationSpec = tween(DurationMedium2, easing = EasingEmphasizedDecelerate),
        initialOffsetX = { -(it * 0.14f).toInt() },
    ) + fadeIn(
        animationSpec = tween(DurationMedium1, easing = EasingStandardDecelerate),
    )

    val sharedAxisXBackwardExit: ExitTransition = slideOutHorizontally(
        animationSpec = tween(DurationMedium1, easing = EasingEmphasizedAccelerate),
        targetOffsetX = { (it * 0.18f).toInt() },
    ) + fadeOut(
        animationSpec = tween(DurationShort3, easing = EasingStandardAccelerate),
    )

    // Shared Axis Y (View mode switching & inline panels)
    fun sharedAxisYContentTransform(): ContentTransform = (
        fadeIn(tween(DurationMedium1, delayMillis = DurationShort1, easing = EasingStandardDecelerate)) +
            slideInVertically(tween(DurationMedium1, easing = EasingEmphasizedDecelerate)) { it / 24 }
        ).togetherWith(
        fadeOut(tween(DurationShort2, easing = EasingStandardAccelerate)) +
            slideOutVertically(tween(DurationShort2, easing = EasingStandardAccelerate)) { -it / 24 },
    )

    // List item placement animation spec
    val listPlacementSpec: SpringSpec<IntOffset> = defaultSpatial()
}
