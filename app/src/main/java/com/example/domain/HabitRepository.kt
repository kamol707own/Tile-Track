package com.example.domain

import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun observeActiveHabits(weekStartMonday: Boolean, todayEpochDay: Long): Flow<List<HabitWithLogs>>
    fun observeArchivedHabits(weekStartMonday: Boolean, todayEpochDay: Long): Flow<List<HabitWithLogs>>
    fun observeHabit(habitId: String, weekStartMonday: Boolean, todayEpochDay: Long): Flow<HabitWithLogs?>
    suspend fun getAllReminders(): List<HabitReminder>
    suspend fun getHabitName(habitId: String): String?
    suspend fun saveHabit(habit: Habit, reminders: List<HabitReminder>)
    suspend fun toggleOrIncrementDay(habitId: String, epochDay: Long)
    suspend fun setDayStatus(habitId: String, epochDay: Long, status: DayStatus, count: Int? = null, note: String? = null)
    suspend fun setDayCount(habitId: String, epochDay: Long, count: Int, note: String? = null)
    suspend fun setArchived(habitId: String, archived: Boolean)
    suspend fun deleteHabitPermanently(habitId: String)
    suspend fun moveHabitOrder(habitIdsInOrder: List<String>)

    // Tasks
    fun observeTasksForDay(epochDay: Long): Flow<List<TaskItem>>
    fun observeAllActiveTasks(): Flow<List<TaskItem>>
    suspend fun saveTask(task: TaskItem)
    suspend fun setTaskStatus(taskId: String, status: DayStatus)
    suspend fun deleteTask(taskId: String)

    // Backup
    suspend fun exportBackupJson(): String
    suspend fun importBackupJson(jsonString: String): Result<Int>
}
