package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.NoDrinks
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhoneDisabled
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WorkOutline
import androidx.compose.ui.graphics.vector.ImageVector

data class HabitIconOption(
    val key: String,
    val label: String,
    val icon: ImageVector,
)

object HabitIconCatalog {
    /**
     * Exact 30 curated icons (6x5 symmetric grid).
     */
    val options: List<HabitIconOption> = listOf(
        HabitIconOption("check", "Habit", Icons.Rounded.CheckCircle),
        HabitIconOption("bolt", "Energy", Icons.Rounded.Bolt),
        HabitIconOption("fitness", "Workout", Icons.Rounded.FitnessCenter),
        HabitIconOption("run", "Run", Icons.Rounded.DirectionsRun),
        HabitIconOption("bike", "Cycle", Icons.Rounded.DirectionsBike),
        HabitIconOption("water", "Hydrate", Icons.Rounded.LocalDrink),
        HabitIconOption("meditate", "Meditate", Icons.Rounded.SelfImprovement),
        HabitIconOption("spa", "Wellness", Icons.Rounded.Spa),
        HabitIconOption("sleep", "Sleep", Icons.Rounded.Bedtime),
        HabitIconOption("sun", "Morning", Icons.Rounded.WbSunny),
        HabitIconOption("book", "Read", Icons.Rounded.MenuBook),
        HabitIconOption("journal", "Journal", Icons.Rounded.EditNote),
        HabitIconOption("code", "Code", Icons.Rounded.Code),
        HabitIconOption("learn", "Learn", Icons.Rounded.School),
        HabitIconOption("language", "Language", Icons.Rounded.Translate),
        HabitIconOption("focus", "Focus", Icons.Rounded.Timer),
        HabitIconOption("work", "Work", Icons.Rounded.WorkOutline),
        HabitIconOption("mind", "Mindset", Icons.Rounded.Psychology),
        HabitIconOption("heart", "Health", Icons.Rounded.Favorite),
        HabitIconOption("food", "Nutrition", Icons.Rounded.Restaurant),
        HabitIconOption("coffee", "Coffee", Icons.Rounded.LocalCafe),
        HabitIconOption("nodrinks", "Clean", Icons.Rounded.NoDrinks),
        HabitIconOption("detox", "Screen Free", Icons.Rounded.PhoneDisabled),
        HabitIconOption("money", "Save", Icons.Rounded.Savings),
        HabitIconOption("music", "Music", Icons.Rounded.MusicNote),
        HabitIconOption("art", "Create", Icons.Rounded.Palette),
        HabitIconOption("eco", "Nature", Icons.Rounded.Eco),
        HabitIconOption("flag", "Goal", Icons.Rounded.Flag),
        HabitIconOption("star", "Priority", Icons.Rounded.Star),
        HabitIconOption("study", "Study", Icons.Rounded.AutoStories),
    )

    private val legacyAliases: Map<String, ImageVector> = mapOf(
        "stories" to Icons.Rounded.AutoStories,
        "brush" to Icons.Rounded.Brush,
    )

    private val iconMap: Map<String, ImageVector> = options.associate { it.key to it.icon } + legacyAliases

    fun resolve(key: String): ImageVector = iconMap[key] ?: Icons.Rounded.CheckCircle

    fun labelFor(key: String): String = options.firstOrNull { it.key == key }?.label ?: "Habit"
}
