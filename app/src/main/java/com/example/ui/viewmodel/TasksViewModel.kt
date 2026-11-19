package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.UserPreferencesRepository
import com.example.domain.DateUtil
import com.example.domain.DayStatus
import com.example.domain.HabitRepository
import com.example.domain.TaskItem
import com.example.domain.TaskPriority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class TaskListFilter(val label: String) {
    ALL("All"),
    OPEN("Open"),
    DONE("Done"),
    MISSED("Missed"),
}

data class TasksUiState(
    val allTasks: List<TaskItem> = emptyList(),
    val overdueTasks: List<TaskItem> = emptyList(),
    val todayTasks: List<TaskItem> = emptyList(),
    val upcomingTasks: List<TaskItem> = emptyList(),
    val earlierResolvedTasks: List<TaskItem> = emptyList(),
    val selectedFilter: TaskListFilter = TaskListFilter.ALL,
    val todayEpochDay: Long = DateUtil.todayEpochDay(),
    val openCount: Int = 0,
    val doneCount: Int = 0,
    val missedCount: Int = 0,
    val isLoading: Boolean = true,
) {
    val hasVisibleTasks: Boolean
        get() = overdueTasks.isNotEmpty() ||
            todayTasks.isNotEmpty() ||
            upcomingTasks.isNotEmpty() ||
            earlierResolvedTasks.isNotEmpty()
}

class TasksViewModel(
    private val repository: HabitRepository,
    preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(TaskListFilter.ALL)

    val uiState: StateFlow<TasksUiState> = combine(
        repository.observeAllActiveTasks(),
        preferencesRepository.preferencesFlow,
        _selectedFilter,
    ) { tasks, _, filter ->
        val today = DateUtil.todayEpochDay()

        val openCount = tasks.count { it.status == DayStatus.UNDONE }
        val doneCount = tasks.count { it.status == DayStatus.DONE }
        val missedCount = tasks.count { it.status == DayStatus.MISS }

        val filtered = when (filter) {
            TaskListFilter.ALL -> tasks
            TaskListFilter.OPEN -> tasks.filter { it.status == DayStatus.UNDONE }
            TaskListFilter.DONE -> tasks.filter { it.status == DayStatus.DONE }
            TaskListFilter.MISSED -> tasks.filter { it.status == DayStatus.MISS }
        }

        val sorted = filtered.sortedWith(
            compareBy<TaskItem>(
                { it.dueEpochDay },
                { it.status != DayStatus.UNDONE },
                { -it.priority.ordinal },
                { it.dueTimeMinutes ?: Int.MAX_VALUE },
            ),
        )

        val overdue = sorted.filter { it.dueEpochDay < today && it.status == DayStatus.UNDONE }
        val todayList = sorted.filter { it.dueEpochDay == today }
        val upcoming = sorted.filter { it.dueEpochDay > today }
        val earlierResolved = sorted.filter { it.dueEpochDay < today && it.status != DayStatus.UNDONE }

        TasksUiState(
            allTasks = tasks,
            overdueTasks = overdue,
            todayTasks = todayList,
            upcomingTasks = upcoming,
            earlierResolvedTasks = earlierResolved,
            selectedFilter = filter,
            todayEpochDay = today,
            openCount = openCount,
            doneCount = doneCount,
            missedCount = missedCount,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TasksUiState())

    fun selectFilter(filter: TaskListFilter) {
        _selectedFilter.value = filter
    }

    fun saveTask(
        existingId: String?,
        title: String,
        notes: String,
        dueEpochDay: Long,
        priority: TaskPriority,
        colorArgb: Long,
        dueTimeMinutes: Int?,
        status: DayStatus,
        existingCreatedAt: Long? = null,
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.saveTask(
                TaskItem(
                    id = existingId ?: UUID.randomUUID().toString(),
                    title = title.trim(),
                    notes = notes.trim(),
                    dueEpochDay = dueEpochDay,
                    status = status,
                    priority = priority,
                    colorArgb = colorArgb,
                    dueTimeMinutes = dueTimeMinutes,
                    createdAt = existingCreatedAt ?: now,
                    updatedAt = now,
                ),
            )
        }
    }

    fun setTaskStatus(taskId: String, status: DayStatus) {
        viewModelScope.launch {
            repository.setTaskStatus(taskId, status)
        }
    }

    fun cycleTaskDoneUndone(task: TaskItem) {
        val next = when (task.status) {
            DayStatus.UNDONE -> DayStatus.DONE
            DayStatus.DONE -> DayStatus.UNDONE
            DayStatus.MISS -> DayStatus.DONE
        }
        setTaskStatus(task.id, next)
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }
}
