package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.UserPreferencesRepository
import com.example.domain.DashboardContentFilter
import com.example.domain.DashboardViewMode
import com.example.domain.DateUtil
import com.example.domain.DayStatus
import com.example.domain.Habit
import com.example.domain.HabitReminder
import com.example.domain.HabitRepository
import com.example.domain.HabitWithLogs
import com.example.domain.StreakGoalType
import com.example.domain.TaskItem
import com.example.domain.TaskPriority
import com.example.domain.UserPreferences
import com.example.notifications.ReminderScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class DashboardUiState(
    val habits: List<HabitWithLogs> = emptyList(),
    val filteredHabits: List<HabitWithLogs> = emptyList(),
    val tasksForSelectedDay: List<TaskItem> = emptyList(),
    val availableCategories: List<String> = emptyList(),
    val selectedCategory: String = "All",
    val contentFilter: DashboardContentFilter = DashboardContentFilter.ALL,
    val todayEpochDay: Long = DateUtil.todayEpochDay(),
    val selectedEpochDay: Long = DateUtil.todayEpochDay(),
    val displayedWeekStartEpochDay: Long = DateUtil.startOfWeekEpochDay(DateUtil.todayEpochDay(), true),
    val preferences: UserPreferences = UserPreferences(),
    val isLoading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val repository: HabitRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val appContext: Context,
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("All")
    private val _contentFilter = MutableStateFlow(DashboardContentFilter.ALL)
    private val _selectedDayOffset = MutableStateFlow<Long?>(null)
    private val _weekOffset = MutableStateFlow(0)

    val preferences: StateFlow<UserPreferences> = preferencesRepository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences())

    private val activeHabitsFlow = preferencesRepository.preferencesFlow
        .flatMapLatest { prefs ->
            val today = DateUtil.todayEpochDay()
            repository.observeActiveHabits(prefs.weekStartMonday, today)
        }

    private val tasksForSelectedDayFlow = combine(
        preferencesRepository.preferencesFlow,
        _selectedDayOffset,
    ) { _, explicitDay ->
        explicitDay ?: DateUtil.todayEpochDay()
    }.flatMapLatest { day ->
        repository.observeTasksForDay(day)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        activeHabitsFlow,
        tasksForSelectedDayFlow,
        _selectedCategory,
        _contentFilter,
        combine(preferencesRepository.preferencesFlow, _selectedDayOffset, _weekOffset) { prefs, selDay, wkOff ->
            Triple(prefs, selDay, wkOff)
        },
    ) { habits, tasks, selectedCat, contentFilter, (prefs, explicitSelDay, weekOffset) ->
        val today = DateUtil.todayEpochDay()
        val selectedDay = explicitSelDay ?: today
        val currentWeekStart = DateUtil.startOfWeekEpochDay(today, prefs.weekStartMonday)
        val displayedWeekStart = currentWeekStart + (weekOffset * 7L)

        val categories = buildList {
            add("All")
            addAll(habits.map { it.habit.category }.distinct().sorted())
        }
        val effectiveCategory = if (selectedCat in categories) selectedCat else "All"
        val filtered = if (effectiveCategory == "All") {
            habits
        } else {
            habits.filter { it.habit.category == effectiveCategory }
        }
        DashboardUiState(
            habits = habits,
            filteredHabits = filtered,
            tasksForSelectedDay = tasks,
            availableCategories = categories,
            selectedCategory = effectiveCategory,
            contentFilter = contentFilter,
            todayEpochDay = today,
            selectedEpochDay = selectedDay,
            displayedWeekStartEpochDay = displayedWeekStart,
            preferences = prefs,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun selectContentFilter(filter: DashboardContentFilter) {
        _contentFilter.value = filter
    }

    fun selectDay(epochDay: Long) {
        _selectedDayOffset.value = epochDay
    }

    fun shiftWeek(deltaWeeks: Int) {
        _weekOffset.value += deltaWeeks
    }

    fun jumpToToday() {
        val today = DateUtil.todayEpochDay()
        _selectedDayOffset.value = today
        _weekOffset.value = 0
    }

    fun setViewMode(mode: DashboardViewMode) {
        viewModelScope.launch {
            preferencesRepository.setDashboardViewMode(mode)
        }
    }

    fun toggleDay(habitId: String, epochDay: Long) {
        viewModelScope.launch {
            repository.toggleOrIncrementDay(habitId, epochDay)
        }
    }

    fun setHabitDayStatus(habitId: String, epochDay: Long, status: DayStatus) {
        viewModelScope.launch {
            repository.setDayStatus(habitId, epochDay, status)
        }
    }

    fun saveHabit(
        existingId: String?,
        name: String,
        description: String,
        iconKey: String,
        colorArgb: Long,
        category: String,
        dailyTarget: Int,
        streakGoalType: StreakGoalType,
        streakGoalTarget: Int,
        reminders: List<HabitReminder>,
        previousReminders: List<HabitReminder>,
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val habitId = existingId ?: UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            val habit = Habit(
                id = habitId,
                name = name.trim(),
                description = description.trim(),
                iconKey = iconKey,
                colorArgb = colorArgb,
                category = category.ifBlank { "General" },
                dailyTarget = dailyTarget.coerceIn(1, 20),
                streakGoalType = streakGoalType,
                streakGoalTarget = streakGoalTarget.coerceAtLeast(1),
                sortOrder = 0,
                isArchived = false,
                createdAt = now,
                updatedAt = now,
            )
            val normalizedReminders = reminders.map { it.copy(habitId = habitId) }
            repository.saveHabit(habit, normalizedReminders)

            ReminderScheduler.cancelReminders(appContext, previousReminders)
            ReminderScheduler.syncHabitReminders(appContext, habitId, normalizedReminders)
        }
    }

    fun archiveHabit(habitId: String, reminders: List<HabitReminder>) {
        viewModelScope.launch {
            repository.setArchived(habitId, true)
            ReminderScheduler.cancelReminders(appContext, reminders)
        }
    }

    fun moveHabit(habitId: String, moveUp: Boolean) {
        viewModelScope.launch {
            val currentList = uiState.value.habits.map { it.habit.id }.toMutableList()
            val idx = currentList.indexOf(habitId)
            if (idx == -1) return@launch
            val targetIdx = if (moveUp) idx - 1 else idx + 1
            if (targetIdx !in currentList.indices) return@launch
            val item = currentList.removeAt(idx)
            currentList.add(targetIdx, item)
            repository.moveHabitOrder(currentList)
        }
    }

    // Tasks actions
    fun saveTask(
        existingId: String?,
        title: String,
        notes: String,
        dueEpochDay: Long,
        priority: TaskPriority,
        colorArgb: Long,
        dueTimeMinutes: Int?,
        existingStatus: DayStatus = DayStatus.UNDONE,
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
                    status = existingStatus,
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

class TileTrackViewModelFactory(
    private val repository: HabitRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val appContext: Context,
    private val habitId: String? = null,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
                DashboardViewModel(repository, preferencesRepository, appContext) as T
            modelClass.isAssignableFrom(HabitDetailViewModel::class.java) ->
                HabitDetailViewModel(
                    habitId = requireNotNull(habitId),
                    repository = repository,
                    preferencesRepository = preferencesRepository,
                    appContext = appContext,
                ) as T
            modelClass.isAssignableFrom(InsightsViewModel::class.java) ->
                InsightsViewModel(repository, preferencesRepository) as T
            modelClass.isAssignableFrom(TasksViewModel::class.java) ->
                TasksViewModel(repository, preferencesRepository) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(repository, preferencesRepository, appContext) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
