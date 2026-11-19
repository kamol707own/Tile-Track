package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.UserPreferencesRepository
import com.example.domain.DashboardViewMode
import com.example.domain.DateUtil
import com.example.domain.DayStatus
import com.example.domain.HabitReminder
import com.example.domain.HabitRepository
import com.example.domain.HabitWithLogs
import com.example.domain.TaskItem
import com.example.domain.ThemeMode
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

data class DayMomentumPoint(
    val label: String,
    val completedCount: Int,
    val totalCount: Int,
)

data class InsightsUiState(
    val activeHabits: List<HabitWithLogs> = emptyList(),
    val tasks: List<TaskItem> = emptyList(),
    val todayCompletedHabits: Int = 0,
    val totalActiveHabits: Int = 0,
    val todayCompletionRatio: Float = 0f,
    val totalLoggedTiles: Int = 0,
    val totalMissedTiles: Int = 0,
    val tasksDoneCount: Int = 0,
    val tasksPendingCount: Int = 0,
    val bestActiveStreak: Int = 0,
    val averageConsistencyPercent: Int = 0,
    val last7DaysMomentum: List<DayMomentumPoint> = emptyList(),
    val topStreaks: List<HabitWithLogs> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class InsightsViewModel(
    repository: HabitRepository,
    preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val activeHabitsFlow = preferencesRepository.preferencesFlow
        .flatMapLatest { prefs ->
            val today = DateUtil.todayEpochDay()
            repository.observeActiveHabits(prefs.weekStartMonday, today)
        }

    val uiState: StateFlow<InsightsUiState> = combine(
        activeHabitsFlow,
        repository.observeAllActiveTasks(),
        preferencesRepository.preferencesFlow,
    ) { habits, tasks, _ ->
        val today = DateUtil.todayEpochDay()
        val total = habits.size
        val completedToday = habits.count { it.stats.isCompletedToday }
        val ratio = if (total == 0) 0f else completedToday.toFloat() / total.toFloat()
        val totalLoggedTiles = habits.sumOf { it.stats.completedDaysCount }
        val totalMissedTiles = habits.sumOf { it.stats.missedDaysCount }
        val bestActiveStreak = habits.maxOfOrNull { it.stats.currentStreak } ?: 0
        val avgConsistency = if (habits.isEmpty()) 0 else {
            habits.sumOf { it.stats.consistency30DaysPercent } / habits.size
        }

        val momentum = (6 downTo 0).map { offset ->
            val day = today - offset
            val iso = DateUtil.dayOfWeekIso(day)
            val met = habits.count { h ->
                val log = h.logsByEpochDay[day]
                log != null && (log.status == DayStatus.DONE || (log.status != DayStatus.MISS && log.count >= h.habit.dailyTarget))
            }
            DayMomentumPoint(
                label = DateUtil.weekdayShortLabel(iso).take(2),
                completedCount = met,
                totalCount = total.coerceAtLeast(1),
            )
        }

        InsightsUiState(
            activeHabits = habits,
            tasks = tasks,
            todayCompletedHabits = completedToday,
            totalActiveHabits = total,
            todayCompletionRatio = ratio,
            totalLoggedTiles = totalLoggedTiles,
            totalMissedTiles = totalMissedTiles,
            tasksDoneCount = tasks.count { it.status == DayStatus.DONE },
            tasksPendingCount = tasks.count { it.status == DayStatus.UNDONE },
            bestActiveStreak = bestActiveStreak,
            averageConsistencyPercent = avgConsistency,
            last7DaysMomentum = momentum,
            topStreaks = habits.sortedByDescending { it.stats.currentStreak }.take(5),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightsUiState())
}

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val archivedHabits: List<HabitWithLogs> = emptyList(),
    val activeHabitCount: Int = 0,
    val statusMessage: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModel(
    private val repository: HabitRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val appContext: Context,
) : ViewModel() {

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val activeHabitsFlow = preferencesRepository.preferencesFlow
        .flatMapLatest { prefs ->
            val today = DateUtil.todayEpochDay()
            repository.observeActiveHabits(prefs.weekStartMonday, today)
        }

    private val archivedHabitsFlow = preferencesRepository.preferencesFlow
        .flatMapLatest { prefs ->
            val today = DateUtil.todayEpochDay()
            repository.observeArchivedHabits(prefs.weekStartMonday, today)
        }

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.preferencesFlow,
        activeHabitsFlow,
        archivedHabitsFlow,
        _statusMessage,
    ) { prefs, active, archived, msg ->
        SettingsUiState(
            preferences = prefs,
            archivedHabits = archived,
            activeHabitCount = active.size,
            statusMessage = msg,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun clearMessage() {
        _statusMessage.value = null
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
    }

    fun setDashboardViewMode(mode: DashboardViewMode) {
        viewModelScope.launch { preferencesRepository.setDashboardViewMode(mode) }
    }

    fun setWeekStartMonday(monday: Boolean) {
        viewModelScope.launch { preferencesRepository.setWeekStartMonday(monday) }
    }

    fun setShowStreakBadges(show: Boolean) {
        viewModelScope.launch { preferencesRepository.setShowStreakBadges(show) }
    }

    fun setShowCategoryFilter(show: Boolean) {
        viewModelScope.launch { preferencesRepository.setShowCategoryFilter(show) }
    }

    fun restoreHabit(habitId: String, reminders: List<HabitReminder>) {
        viewModelScope.launch {
            repository.setArchived(habitId, false)
            ReminderScheduler.syncHabitReminders(appContext, habitId, reminders)
            _statusMessage.value = "Habit restored to dashboard"
        }
    }

    fun deleteArchivedHabit(habitId: String, reminders: List<HabitReminder>) {
        viewModelScope.launch {
            ReminderScheduler.cancelReminders(appContext, reminders)
            repository.deleteHabitPermanently(habitId)
            _statusMessage.value = "Habit deleted permanently"
        }
    }

    suspend fun generateBackupJson(): String {
        return repository.exportBackupJson()
    }

    fun importBackupJson(rawJson: String) {
        viewModelScope.launch {
            repository.importBackupJson(rawJson)
                .onSuccess { count ->
                    val reminders = repository.getAllReminders()
                    reminders.forEach { ReminderScheduler.scheduleReminder(appContext, it) }
                    _statusMessage.value = "Imported $count item(s) successfully"
                }
                .onFailure {
                    _statusMessage.value = "Invalid backup file format"
                }
        }
    }

    fun notifyBackupExported() {
        _statusMessage.value = "JSON backup exported successfully"
    }
}
