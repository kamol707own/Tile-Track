package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.domain.DayStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Transaction
    @Query("SELECT * FROM habits WHERE isArchived = 0 AND isDeleted = 0 ORDER BY sortOrder ASC, createdAt ASC")
    fun observeActiveHabits(): Flow<List<HabitWithDetails>>

    @Transaction
    @Query("SELECT * FROM habits WHERE isArchived = 1 AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun observeArchivedHabits(): Flow<List<HabitWithDetails>>

    @Transaction
    @Query("SELECT * FROM habits WHERE id = :habitId AND isDeleted = 0")
    fun observeHabitById(habitId: String): Flow<HabitWithDetails?>

    @Query("SELECT * FROM habits WHERE id = :habitId LIMIT 1")
    suspend fun getHabitById(habitId: String): HabitEntity?

    @Query("SELECT * FROM habits WHERE isDeleted = 0")
    suspend fun getAllHabitsSnapshot(): List<HabitEntity>

    @Query("SELECT * FROM completion_logs WHERE isDeleted = 0")
    suspend fun getAllLogsSnapshot(): List<CompletionLogEntity>

    @Query("SELECT * FROM habit_reminders")
    suspend fun getAllRemindersSnapshot(): List<ReminderEntity>

    @Query("SELECT * FROM habit_reminders WHERE isEnabled = 1")
    suspend fun getEnabledRemindersSnapshot(): List<ReminderEntity>

    @Query("SELECT COALESCE(MAX(sortOrder), -1) + 1 FROM habits WHERE isDeleted = 0")
    suspend fun getNextSortOrder(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHabit(habit: HabitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHabits(habits: List<HabitEntity>)

    @Query("UPDATE habits SET isArchived = :archived, updatedAt = :updatedAt WHERE id = :habitId")
    suspend fun updateArchived(habitId: String, archived: Boolean, updatedAt: Long)

    @Query("UPDATE habits SET sortOrder = :sortOrder, updatedAt = :updatedAt WHERE id = :habitId")
    suspend fun updateSortOrder(habitId: String, sortOrder: Int, updatedAt: Long)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabitById(habitId: String)

    @Query("SELECT * FROM completion_logs WHERE habitId = :habitId AND dateEpochDay = :epochDay LIMIT 1")
    suspend fun getLogForDay(habitId: String, epochDay: Long): CompletionLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: CompletionLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLogs(logs: List<CompletionLogEntity>)

    @Query("DELETE FROM completion_logs WHERE habitId = :habitId AND dateEpochDay = :epochDay")
    suspend fun deleteLogForDay(habitId: String, epochDay: Long)

    @Query("DELETE FROM habit_reminders WHERE habitId = :habitId")
    suspend fun deleteRemindersForHabit(habitId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<ReminderEntity>)

    // Tasks
    @Query("SELECT * FROM tasks WHERE dueEpochDay = :epochDay AND isDeleted = 0 ORDER BY CASE status WHEN 'UNDONE' THEN 0 WHEN 'MISS' THEN 1 ELSE 2 END, priority DESC, createdAt ASC")
    fun observeTasksForDay(epochDay: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isDeleted = 0 ORDER BY dueEpochDay ASC, createdAt ASC")
    fun observeAllActiveTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isDeleted = 0")
    suspend fun getAllTasksSnapshot(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTasks(tasks: List<TaskEntity>)

    @Query("UPDATE tasks SET status = :status, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: String, status: DayStatus, updatedAt: Long)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    @Transaction
    suspend fun saveHabitWithReminders(habit: HabitEntity, reminders: List<ReminderEntity>) {
        upsertHabit(habit)
        deleteRemindersForHabit(habit.id)
        if (reminders.isNotEmpty()) {
            insertReminders(reminders)
        }
    }

    @Transaction
    suspend fun importBackupTransaction(
        habits: List<HabitEntity>,
        logs: List<CompletionLogEntity>,
        reminders: List<ReminderEntity>,
        tasks: List<TaskEntity>,
    ) {
        upsertHabits(habits)
        upsertLogs(logs)
        if (reminders.isNotEmpty()) {
            insertReminders(reminders)
        }
        if (tasks.isNotEmpty()) {
            upsertTasks(tasks)
        }
    }
}
