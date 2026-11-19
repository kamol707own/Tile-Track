package com.example.data.local

import com.example.domain.CompletionLog
import com.example.domain.DayStatus
import com.example.domain.Habit
import com.example.domain.HabitBackupDto
import com.example.domain.HabitReminder
import com.example.domain.HabitRepository
import com.example.domain.HabitWithLogs
import com.example.domain.LogBackupDto
import com.example.domain.ReminderBackupDto
import com.example.domain.StreakCalculator
import com.example.domain.StreakGoalType
import com.example.domain.TaskBackupDto
import com.example.domain.TaskItem
import com.example.domain.TaskPriority
import com.example.domain.TileTrackBackupDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.UUID

class LocalRoomHabitRepository(
    private val dao: HabitDao,
) : HabitRepository {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override fun observeActiveHabits(
        weekStartMonday: Boolean,
        todayEpochDay: Long,
    ): Flow<List<HabitWithLogs>> {
        return dao.observeActiveHabits().map { list ->
            list.map { it.toDomain(weekStartMonday, todayEpochDay) }
        }
    }

    override fun observeArchivedHabits(
        weekStartMonday: Boolean,
        todayEpochDay: Long,
    ): Flow<List<HabitWithLogs>> {
        return dao.observeArchivedHabits().map { list ->
            list.map { it.toDomain(weekStartMonday, todayEpochDay) }
        }
    }

    override fun observeHabit(
        habitId: String,
        weekStartMonday: Boolean,
        todayEpochDay: Long,
    ): Flow<HabitWithLogs?> {
        return dao.observeHabitById(habitId).map { it?.toDomain(weekStartMonday, todayEpochDay) }
    }

    override suspend fun getAllReminders(): List<HabitReminder> = withContext(Dispatchers.IO) {
        dao.getEnabledRemindersSnapshot().map { it.toDomain() }
    }

    override suspend fun getHabitName(habitId: String): String? = withContext(Dispatchers.IO) {
        dao.getHabitById(habitId)?.name
    }

    override suspend fun saveHabit(habit: Habit, reminders: List<HabitReminder>) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val existing = dao.getHabitById(habit.id)
        val sortOrder = existing?.sortOrder ?: dao.getNextSortOrder()
        val entity = HabitEntity(
            id = habit.id,
            name = habit.name.trim(),
            description = habit.description.trim(),
            iconKey = habit.iconKey,
            colorArgb = habit.colorArgb,
            category = habit.category.ifBlank { "General" },
            dailyTarget = habit.dailyTarget.coerceIn(1, 20),
            streakGoalType = habit.streakGoalType,
            streakGoalTarget = habit.streakGoalTarget.coerceAtLeast(1),
            sortOrder = sortOrder,
            isArchived = existing?.isArchived ?: habit.isArchived,
            createdAt = existing?.createdAt ?: habit.createdAt,
            updatedAt = now,
            isDeleted = false,
            remoteId = existing?.remoteId ?: habit.remoteId,
        )
        val reminderEntities = reminders.map { rem ->
            ReminderEntity(
                id = rem.id,
                habitId = entity.id,
                hour = rem.hour.coerceIn(0, 23),
                minute = rem.minute.coerceIn(0, 59),
                daysOfWeekMask = rem.daysOfWeekMask,
                isEnabled = rem.isEnabled,
                updatedAt = now,
            )
        }
        dao.saveHabitWithReminders(entity, reminderEntities)
    }

    override suspend fun toggleOrIncrementDay(habitId: String, epochDay: Long) = withContext(Dispatchers.IO) {
        val habit = dao.getHabitById(habitId) ?: return@withContext
        val existing = dao.getLogForDay(habitId, epochDay)
        val target = habit.dailyTarget.coerceAtLeast(1)
        val currentStatus = existing?.status ?: DayStatus.UNDONE
        val currentCount = existing?.count ?: 0

        when {
            currentStatus == DayStatus.MISS -> {
                setDayInternal(habitId, epochDay, DayStatus.UNDONE, 0, existing?.note ?: "")
            }
            currentStatus == DayStatus.DONE || currentCount >= target -> {
                setDayInternal(habitId, epochDay, DayStatus.UNDONE, 0, existing?.note ?: "")
            }
            else -> {
                val nextCount = currentCount + 1
                val nextStatus = if (nextCount >= target) DayStatus.DONE else DayStatus.UNDONE
                setDayInternal(habitId, epochDay, nextStatus, nextCount, existing?.note ?: "")
            }
        }
    }

    override suspend fun setDayStatus(
        habitId: String,
        epochDay: Long,
        status: DayStatus,
        count: Int?,
        note: String?,
    ) = withContext(Dispatchers.IO) {
        val habit = dao.getHabitById(habitId) ?: return@withContext
        val existing = dao.getLogForDay(habitId, epochDay)
        val target = habit.dailyTarget.coerceAtLeast(1)
        val resolvedCount = when (status) {
            DayStatus.DONE -> (count ?: target).coerceAtLeast(target)
            DayStatus.MISS -> 0
            DayStatus.UNDONE -> (count ?: 0).coerceIn(0, (target - 1).coerceAtLeast(0))
        }
        setDayInternal(habitId, epochDay, status, resolvedCount, note ?: existing?.note.orEmpty())
    }

    override suspend fun setDayCount(habitId: String, epochDay: Long, count: Int, note: String?) =
        withContext(Dispatchers.IO) {
            val habit = dao.getHabitById(habitId) ?: return@withContext
            val existing = dao.getLogForDay(habitId, epochDay)
            val safeCount = count.coerceAtLeast(0)
            val target = habit.dailyTarget.coerceAtLeast(1)
            val status = when {
                safeCount >= target -> DayStatus.DONE
                existing?.status == DayStatus.MISS && safeCount == 0 -> DayStatus.MISS
                else -> DayStatus.UNDONE
            }
            setDayInternal(habitId, epochDay, status, safeCount, note ?: existing?.note.orEmpty())
        }

    private suspend fun setDayInternal(
        habitId: String,
        epochDay: Long,
        status: DayStatus,
        count: Int,
        note: String,
    ) {
        val now = System.currentTimeMillis()
        if (status == DayStatus.UNDONE && count <= 0 && note.isBlank()) {
            dao.deleteLogForDay(habitId, epochDay)
        } else {
            val existing = dao.getLogForDay(habitId, epochDay)
            dao.upsertLog(
                CompletionLogEntity(
                    id = existing?.id ?: UUID.randomUUID().toString(),
                    habitId = habitId,
                    dateEpochDay = epochDay,
                    count = count.coerceAtLeast(0),
                    status = status,
                    note = note.trim(),
                    updatedAt = now,
                    isDeleted = false,
                ),
            )
        }
    }

    override suspend fun setArchived(habitId: String, archived: Boolean) = withContext(Dispatchers.IO) {
        dao.updateArchived(habitId, archived, System.currentTimeMillis())
    }

    override suspend fun deleteHabitPermanently(habitId: String) = withContext(Dispatchers.IO) {
        dao.deleteHabitById(habitId)
    }

    override suspend fun moveHabitOrder(habitIdsInOrder: List<String>) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        habitIdsInOrder.forEachIndexed { index, id ->
            dao.updateSortOrder(id, index, now)
        }
    }

    // Tasks
    override fun observeTasksForDay(epochDay: Long): Flow<List<TaskItem>> {
        return dao.observeTasksForDay(epochDay).map { list -> list.map { it.toDomain() } }
    }

    override fun observeAllActiveTasks(): Flow<List<TaskItem>> {
        return dao.observeAllActiveTasks().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun saveTask(task: TaskItem) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        dao.upsertTask(
            TaskEntity(
                id = task.id,
                title = task.title.trim(),
                notes = task.notes.trim(),
                dueEpochDay = task.dueEpochDay,
                status = task.status,
                priority = task.priority,
                colorArgb = task.colorArgb,
                dueTimeMinutes = task.dueTimeMinutes,
                createdAt = task.createdAt,
                updatedAt = now,
                isDeleted = false,
            ),
        )
    }

    override suspend fun setTaskStatus(taskId: String, status: DayStatus) = withContext(Dispatchers.IO) {
        dao.updateTaskStatus(taskId, status, System.currentTimeMillis())
    }

    override suspend fun deleteTask(taskId: String) = withContext(Dispatchers.IO) {
        dao.deleteTaskById(taskId)
    }

    override suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val habits = dao.getAllHabitsSnapshot().map {
            HabitBackupDto(
                id = it.id,
                name = it.name,
                description = it.description,
                iconKey = it.iconKey,
                colorArgb = it.colorArgb,
                category = it.category,
                dailyTarget = it.dailyTarget,
                streakGoalType = it.streakGoalType.name,
                streakGoalTarget = it.streakGoalTarget,
                sortOrder = it.sortOrder,
                isArchived = it.isArchived,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
            )
        }
        val logs = dao.getAllLogsSnapshot().map {
            LogBackupDto(
                id = it.id,
                habitId = it.habitId,
                dateEpochDay = it.dateEpochDay,
                count = it.count,
                status = it.status.name,
                note = it.note,
                updatedAt = it.updatedAt,
            )
        }
        val reminders = dao.getAllRemindersSnapshot().map {
            ReminderBackupDto(
                id = it.id,
                habitId = it.habitId,
                hour = it.hour,
                minute = it.minute,
                daysOfWeekMask = it.daysOfWeekMask,
                isEnabled = it.isEnabled,
            )
        }
        val tasks = dao.getAllTasksSnapshot().map {
            TaskBackupDto(
                id = it.id,
                title = it.title,
                notes = it.notes,
                dueEpochDay = it.dueEpochDay,
                status = it.status.name,
                priority = it.priority.name,
                colorArgb = it.colorArgb,
                dueTimeMinutes = it.dueTimeMinutes,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
            )
        }
        val dto = TileTrackBackupDto(
            version = 2,
            exportedAt = System.currentTimeMillis(),
            habits = habits,
            logs = logs,
            reminders = reminders,
            tasks = tasks,
        )
        json.encodeToString(dto)
    }

    override suspend fun importBackupJson(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val dto = json.decodeFromString<TileTrackBackupDto>(jsonString)
            val now = System.currentTimeMillis()
            val habitEntities = dto.habits.map { h ->
                HabitEntity(
                    id = h.id,
                    name = h.name,
                    description = h.description,
                    iconKey = h.iconKey,
                    colorArgb = h.colorArgb,
                    category = h.category,
                    dailyTarget = h.dailyTarget.coerceIn(1, 20),
                    streakGoalType = runCatching { StreakGoalType.valueOf(h.streakGoalType) }
                        .getOrDefault(StreakGoalType.DAILY),
                    streakGoalTarget = h.streakGoalTarget.coerceAtLeast(1),
                    sortOrder = h.sortOrder,
                    isArchived = h.isArchived,
                    createdAt = h.createdAt,
                    updatedAt = h.updatedAt,
                    isDeleted = false,
                )
            }
            val validHabitIds = habitEntities.map { it.id }.toHashSet()
            val logEntities = dto.logs.filter { it.habitId in validHabitIds }.map { l ->
                CompletionLogEntity(
                    id = l.id,
                    habitId = l.habitId,
                    dateEpochDay = l.dateEpochDay,
                    count = l.count,
                    status = runCatching { DayStatus.valueOf(l.status) }.getOrDefault(
                        if (l.count > 0) DayStatus.DONE else DayStatus.UNDONE,
                    ),
                    note = l.note,
                    updatedAt = l.updatedAt,
                    isDeleted = false,
                )
            }
            val reminderEntities = dto.reminders.filter { it.habitId in validHabitIds }.map { r ->
                ReminderEntity(
                    id = r.id,
                    habitId = r.habitId,
                    hour = r.hour,
                    minute = r.minute,
                    daysOfWeekMask = r.daysOfWeekMask,
                    isEnabled = r.isEnabled,
                    updatedAt = now,
                )
            }
            val taskEntities = dto.tasks.map { t ->
                TaskEntity(
                    id = t.id,
                    title = t.title,
                    notes = t.notes,
                    dueEpochDay = t.dueEpochDay,
                    status = runCatching { DayStatus.valueOf(t.status) }.getOrDefault(DayStatus.UNDONE),
                    priority = runCatching { TaskPriority.valueOf(t.priority) }.getOrDefault(TaskPriority.MEDIUM),
                    colorArgb = t.colorArgb,
                    dueTimeMinutes = t.dueTimeMinutes,
                    createdAt = t.createdAt,
                    updatedAt = t.updatedAt,
                    isDeleted = false,
                )
            }
            dao.importBackupTransaction(habitEntities, logEntities, reminderEntities, taskEntities)
            habitEntities.size + taskEntities.size
        }
    }

    private fun HabitWithDetails.toDomain(weekStartMonday: Boolean, todayEpochDay: Long): HabitWithLogs {
        val domainHabit = Habit(
            id = habit.id,
            name = habit.name,
            description = habit.description,
            iconKey = habit.iconKey,
            colorArgb = habit.colorArgb,
            category = habit.category,
            dailyTarget = habit.dailyTarget,
            streakGoalType = habit.streakGoalType,
            streakGoalTarget = habit.streakGoalTarget,
            sortOrder = habit.sortOrder,
            isArchived = habit.isArchived,
            createdAt = habit.createdAt,
            updatedAt = habit.updatedAt,
            remoteId = habit.remoteId,
        )
        val logsMap = logs
            .filter { !it.isDeleted }
            .associate { entity ->
                entity.dateEpochDay to CompletionLog(
                    id = entity.id,
                    habitId = entity.habitId,
                    dateEpochDay = entity.dateEpochDay,
                    count = entity.count,
                    status = entity.status,
                    note = entity.note,
                    updatedAt = entity.updatedAt,
                )
            }
        val domainReminders = reminders.map { it.toDomain() }
        val stats = StreakCalculator.calculateStats(
            habit = domainHabit,
            logsByEpochDay = logsMap,
            todayEpochDay = todayEpochDay,
            weekStartMonday = weekStartMonday,
        )
        return HabitWithLogs(
            habit = domainHabit,
            logsByEpochDay = logsMap,
            reminders = domainReminders,
            stats = stats,
        )
    }

    private fun ReminderEntity.toDomain(): HabitReminder {
        return HabitReminder(
            id = id,
            habitId = habitId,
            hour = hour,
            minute = minute,
            daysOfWeekMask = daysOfWeekMask,
            isEnabled = isEnabled,
        )
    }

    private fun TaskEntity.toDomain(): TaskItem {
        return TaskItem(
            id = id,
            title = title,
            notes = notes,
            dueEpochDay = dueEpochDay,
            status = status,
            priority = priority,
            colorArgb = colorArgb,
            dueTimeMinutes = dueTimeMinutes,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
