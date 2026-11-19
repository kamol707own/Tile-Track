package com.example.domain

import kotlinx.serialization.Serializable

@Serializable
data class TileTrackBackupDto(
    val version: Int = 2,
    val exportedAt: Long,
    val habits: List<HabitBackupDto>,
    val logs: List<LogBackupDto>,
    val reminders: List<ReminderBackupDto>,
    val tasks: List<TaskBackupDto> = emptyList(),
)

@Serializable
data class HabitBackupDto(
    val id: String,
    val name: String,
    val description: String = "",
    val iconKey: String,
    val colorArgb: Long,
    val category: String = "General",
    val dailyTarget: Int = 1,
    val streakGoalType: String = StreakGoalType.DAILY.name,
    val streakGoalTarget: Int = 1,
    val sortOrder: Int = 0,
    val isArchived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
data class LogBackupDto(
    val id: String,
    val habitId: String,
    val dateEpochDay: Long,
    val count: Int,
    val status: String = DayStatus.DONE.name,
    val note: String = "",
    val updatedAt: Long,
)

@Serializable
data class ReminderBackupDto(
    val id: String,
    val habitId: String,
    val hour: Int,
    val minute: Int,
    val daysOfWeekMask: Int,
    val isEnabled: Boolean,
)

@Serializable
data class TaskBackupDto(
    val id: String,
    val title: String,
    val notes: String = "",
    val dueEpochDay: Long,
    val status: String = DayStatus.UNDONE.name,
    val priority: String = TaskPriority.MEDIUM.name,
    val colorArgb: Long,
    val dueTimeMinutes: Int? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
