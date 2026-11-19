package com.example.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.domain.DayStatus
import com.example.domain.StreakGoalType
import com.example.domain.TaskPriority

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
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
    val isDeleted: Boolean = false,
    val remoteId: String? = null,
)

@Entity(
    tableName = "completion_logs",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["habitId"]),
        Index(value = ["habitId", "dateEpochDay"], unique = true),
    ],
)
data class CompletionLogEntity(
    @PrimaryKey val id: String,
    val habitId: String,
    val dateEpochDay: Long,
    val count: Int,
    val status: DayStatus = DayStatus.DONE,
    val note: String = "",
    val updatedAt: Long,
    val isDeleted: Boolean = false,
)

@Entity(
    tableName = "habit_reminders",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["habitId"])],
)
data class ReminderEntity(
    @PrimaryKey val id: String,
    val habitId: String,
    val hour: Int,
    val minute: Int,
    val daysOfWeekMask: Int,
    val isEnabled: Boolean,
    val updatedAt: Long,
)

@Entity(
    tableName = "tasks",
    indices = [Index(value = ["dueEpochDay"])],
)
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val notes: String,
    val dueEpochDay: Long,
    val status: DayStatus,
    val priority: TaskPriority,
    val colorArgb: Long,
    val dueTimeMinutes: Int?,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
)

data class HabitWithDetails(
    @Embedded val habit: HabitEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "habitId",
    )
    val logs: List<CompletionLogEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "habitId",
    )
    val reminders: List<ReminderEntity>,
)
