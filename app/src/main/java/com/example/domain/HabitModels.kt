package com.example.domain

enum class ThemeMode {
    DARK,
    LIGHT,
    SYSTEM,
}

enum class DashboardViewMode {
    GRID,
    COMPACT,
    WEEK_STRIP,
}

enum class DashboardContentFilter(val label: String) {
    ALL("All"),
    HABITS("Habits"),
    TASKS("Tasks"),
}

enum class StreakGoalType(val label: String) {
    NONE("No Goal"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
}

enum class DayStatus(val label: String) {
    UNDONE("Pending"),
    DONE("Done"),
    MISS("Missed"),
}

enum class TaskPriority(val label: String) {
    LOW("Low"),
    MEDIUM("Normal"),
    HIGH("High"),
}

data class Habit(
    val id: String,
    val name: String,
    val description: String,
    val iconKey: String,
    val colorArgb: Long,
    val category: String,
    val dailyTarget: Int,
    val streakGoalType: StreakGoalType,
    val streakGoalTarget: Int,
    val sortOrder: Int,
    val isArchived: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val remoteId: String? = null,
)

data class CompletionLog(
    val id: String,
    val habitId: String,
    val dateEpochDay: Long,
    val count: Int,
    val status: DayStatus = DayStatus.UNDONE,
    val note: String = "",
    val updatedAt: Long,
)

data class TaskItem(
    val id: String,
    val title: String,
    val notes: String,
    val dueEpochDay: Long,
    val status: DayStatus = DayStatus.UNDONE,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val colorArgb: Long,
    val dueTimeMinutes: Int? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

data class HabitReminder(
    val id: String,
    val habitId: String,
    val hour: Int,
    val minute: Int,
    val daysOfWeekMask: Int = ALL_DAYS_MASK,
    val isEnabled: Boolean = true,
) {
    companion object {
        const val ALL_DAYS_MASK = 0b1111111 // bits 0..6 for Mon..Sun
    }

    fun isDayEnabled(isoDay: Int): Boolean {
        val bit = 1 shl (isoDay - 1)
        return (daysOfWeekMask and bit) != 0
    }

    fun formatDaysSummary(): String {
        if (daysOfWeekMask == ALL_DAYS_MASK) return "Every day"
        if (daysOfWeekMask == 0b0011111) return "Weekdays"
        if (daysOfWeekMask == 0b1100000) return "Weekends"
        val names = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val selected = names.filterIndexed { index, _ -> (daysOfWeekMask and (1 shl index)) != 0 }
        return if (selected.isEmpty()) "No days" else selected.joinToString(", ")
    }
}

data class MonthlyBarPoint(
    val label: String,
    val completedDays: Int,
    val totalDays: Int,
)

data class WeekdayStat(
    val isoDay: Int,
    val label: String,
    val completionCount: Int,
    val ratio: Float,
)

data class HabitStats(
    val currentStreak: Int,
    val bestStreak: Int,
    val totalCompletions: Int,
    val completedDaysCount: Int,
    val missedDaysCount: Int,
    val consistency30DaysPercent: Int,
    val todayCount: Int,
    val todayStatus: DayStatus,
    val isCompletedToday: Boolean,
    val monthlyBars: List<MonthlyBarPoint>,
    val weekdayStats: List<WeekdayStat>,
)

data class HabitWithLogs(
    val habit: Habit,
    val logsByEpochDay: Map<Long, CompletionLog>,
    val reminders: List<HabitReminder>,
    val stats: HabitStats,
)

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val dashboardViewMode: DashboardViewMode = DashboardViewMode.GRID,
    val weekStartMonday: Boolean = true,
    val showStreakBadges: Boolean = true,
    val showCategoryFilter: Boolean = true,
    val timezoneOffsetMinutes: Int? = null,
)

object HabitCategoryDefaults {
    val presets = listOf(
        "General",
        "Health",
        "Fitness",
        "Mindfulness",
        "Learning",
        "Productivity",
        "Morning",
        "Evening",
        "Finance",
    )
}
