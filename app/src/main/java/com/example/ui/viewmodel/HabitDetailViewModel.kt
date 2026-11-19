package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.UserPreferencesRepository
import com.example.domain.DateUtil
import com.example.domain.DayStatus
import com.example.domain.Habit
import com.example.domain.HabitReminder
import com.example.domain.HabitRepository
import com.example.domain.HabitWithLogs
import com.example.domain.StreakGoalType
import com.example.domain.UserPreferences
import com.example.notifications.ReminderScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HabitDetailViewModel(
    private val habitId: String,
    private val repository: HabitRepository,
    preferencesRepository: UserPreferencesRepository,
    private val appContext: Context,
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesRepository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences())

    val habitState: StateFlow<HabitWithLogs?> = preferencesRepository.preferencesFlow
        .flatMapLatest { prefs ->
            val today = DateUtil.todayEpochDay()
            repository.observeHabit(habitId, prefs.weekStartMonday, today)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setDayStatus(epochDay: Long, status: DayStatus, count: Int? = null, note: String? = null) {
        viewModelScope.launch {
            repository.setDayStatus(habitId, epochDay, status, count, note)
        }
    }

    fun setDayCount(epochDay: Long, count: Int, note: String?) {
        viewModelScope.launch {
            repository.setDayCount(habitId, epochDay, count, note)
        }
    }

    fun updateHabit(
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
        val current = habitState.value?.habit ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            val updated = Habit(
                id = current.id,
                name = name.trim(),
                description = description.trim(),
                iconKey = iconKey,
                colorArgb = colorArgb,
                category = category.ifBlank { "General" },
                dailyTarget = dailyTarget.coerceIn(1, 20),
                streakGoalType = streakGoalType,
                streakGoalTarget = streakGoalTarget.coerceAtLeast(1),
                sortOrder = current.sortOrder,
                isArchived = current.isArchived,
                createdAt = current.createdAt,
                updatedAt = System.currentTimeMillis(),
                remoteId = current.remoteId,
            )
            val normalizedReminders = reminders.map { it.copy(habitId = current.id) }
            repository.saveHabit(updated, normalizedReminders)
            ReminderScheduler.cancelReminders(appContext, previousReminders)
            if (!updated.isArchived) {
                ReminderScheduler.syncHabitReminders(appContext, current.id, normalizedReminders)
            }
        }
    }

    fun toggleArchive(archived: Boolean, reminders: List<HabitReminder>) {
        viewModelScope.launch {
            repository.setArchived(habitId, archived)
            if (archived) {
                ReminderScheduler.cancelReminders(appContext, reminders)
            } else {
                ReminderScheduler.syncHabitReminders(appContext, habitId, reminders)
            }
        }
    }

    fun deletePermanently(reminders: List<HabitReminder>) {
        viewModelScope.launch {
            ReminderScheduler.cancelReminders(appContext, reminders)
            repository.deleteHabitPermanently(habitId)
        }
    }
}
