package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Dark Palette (HabitKit Signature Obsidian Slate)
val DarkBackground = Color(0xFF0B0D11)
val DarkSurface = Color(0xFF14171F)
val DarkSurfaceElevated = Color(0xFF1A1E28)
val DarkSurfaceVariant = Color(0xFF212633)
val DarkBorder = Color(0xFF262C3B)
val DarkOnSurface = Color(0xFFF4F6FA)
val DarkOnSurfaceMuted = Color(0xFF8E96A8)
val DarkPrimary = Color(0xFF2EE59D)
val DarkOnPrimary = Color(0xFF052417)
val DarkPrimaryContainer = Color(0xFF11382A)
val DarkOnPrimaryContainer = Color(0xFF9DF7CC)
val DarkSecondary = Color(0xFF38BDF8)
val DarkTertiary = Color(0xFFFF7A59)
val DarkError = Color(0xFFFF5C6C)

// Light Palette (Crisp Warm Alabaster)
val LightBackground = Color(0xFFF6F7FA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceElevated = Color(0xFFF0F2F7)
val LightSurfaceVariant = Color(0xFFE7EAF2)
val LightBorder = Color(0xFFE0E4EE)
val LightOnSurface = Color(0xFF12151C)
val LightOnSurfaceMuted = Color(0xFF5E6678)
val LightPrimary = Color(0xFF0D9F6E)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFD6F7E8)
val LightOnPrimaryContainer = Color(0xFF053B27)
val LightSecondary = Color(0xFF0284C7)
val LightTertiary = Color(0xFFEA580C)
val LightError = Color(0xFFDC2626)

// Curated 18 Habit Accent Swatches (HabitKit Pro Palette)
data class HabitColorSwatch(
    val name: String,
    val argb: Long,
)

val HabitColorPalette: List<HabitColorSwatch> = listOf(
    HabitColorSwatch("Emerald", 0xFF22C55E),
    HabitColorSwatch("Mint", 0xFF10B981),
    HabitColorSwatch("Teal", 0xFF14B8A6),
    HabitColorSwatch("Cyan", 0xFF06B6D4),
    HabitColorSwatch("Sky", 0xFF0EA5E9),
    HabitColorSwatch("Blue", 0xFF3B82F6),
    HabitColorSwatch("Indigo", 0xFF6366F1),
    HabitColorSwatch("Violet", 0xFF8B5CF6),
    HabitColorSwatch("Purple", 0xFFA855F7),
    HabitColorSwatch("Fuchsia", 0xFFD946EF),
    HabitColorSwatch("Pink", 0xFFEC4899),
    HabitColorSwatch("Rose", 0xFFF43F5E),
    HabitColorSwatch("Coral", 0xFFFF6B57),
    HabitColorSwatch("Orange", 0xFFF97316),
    HabitColorSwatch("Amber", 0xFFF59E0B),
    HabitColorSwatch("Gold", 0xFFEAB308),
    HabitColorSwatch("Lime", 0xFF84CC16),
    HabitColorSwatch("Slate", 0xFF64748B),
)
