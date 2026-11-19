package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.domain.ThemeMode

@Immutable
data class TileTrackExtendedColors(
    val cardBorder: Color,
    val emptyTile: Color,
    val subtleSurface: Color,
    val mutedText: Color,
    val isDark: Boolean,
)

val LocalTileTrackColors = staticCompositionLocalOf {
    TileTrackExtendedColors(
        cardBorder = DarkBorder,
        emptyTile = DarkSurfaceVariant,
        subtleSurface = DarkSurfaceElevated,
        mutedText = DarkOnSurfaceMuted,
        isDark = true,
    )
}

private val TileTrackDarkScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = Color(0xFF041E2B),
    tertiary = DarkTertiary,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceMuted,
    surfaceContainer = DarkSurface,
    surfaceContainerLow = DarkBackground,
    surfaceContainerHigh = DarkSurfaceElevated,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = DarkError,
)

private val TileTrackLightScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = Color.White,
    tertiary = LightTertiary,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceMuted,
    surfaceContainer = LightSurface,
    surfaceContainerLow = LightBackground,
    surfaceContainerHigh = LightSurfaceElevated,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = LightError,
)

private val TileTrackShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun TileTrackTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit,
) {
    val useDarkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (useDarkTheme) TileTrackDarkScheme else TileTrackLightScheme
    val extendedColors = if (useDarkTheme) {
        TileTrackExtendedColors(
            cardBorder = DarkBorder,
            emptyTile = DarkSurfaceVariant,
            subtleSurface = DarkSurfaceElevated,
            mutedText = DarkOnSurfaceMuted,
            isDark = true,
        )
    } else {
        TileTrackExtendedColors(
            cardBorder = LightBorder,
            emptyTile = LightSurfaceVariant,
            subtleSurface = LightSurfaceElevated,
            mutedText = LightOnSurfaceMuted,
            isDark = false,
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !useDarkTheme
                    isAppearanceLightNavigationBars = !useDarkTheme
                }
            }
        }
    }

    CompositionLocalProvider(LocalTileTrackColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TileTrackTypography,
            shapes = TileTrackShapes,
            content = content,
        )
    }
}
