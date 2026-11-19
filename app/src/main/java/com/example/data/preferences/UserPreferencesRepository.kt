package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.DashboardViewMode
import com.example.domain.ThemeMode
import com.example.domain.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tiletrack_prefs")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DASHBOARD_VIEW_MODE = stringPreferencesKey("dashboard_view_mode")
        val WEEK_START_MONDAY = booleanPreferencesKey("week_start_monday")
        val SHOW_STREAK_BADGES = booleanPreferencesKey("show_streak_badges")
        val SHOW_CATEGORY_FILTER = booleanPreferencesKey("show_category_filter")
        val TIMEZONE_OFFSET_MINUTES = intPreferencesKey("timezone_offset_minutes")
        val HAS_CUSTOM_TIMEZONE = booleanPreferencesKey("has_custom_timezone")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val hasCustomTz = prefs[Keys.HAS_CUSTOM_TIMEZONE] ?: false
        val tzOffset = if (hasCustomTz) prefs[Keys.TIMEZONE_OFFSET_MINUTES] else null
        UserPreferences(
            themeMode = prefs[Keys.THEME_MODE]?.let {
                runCatching { ThemeMode.valueOf(it) }.getOrNull()
            } ?: ThemeMode.DARK,
            dashboardViewMode = prefs[Keys.DASHBOARD_VIEW_MODE]?.let {
                runCatching { DashboardViewMode.valueOf(it) }.getOrNull()
            } ?: DashboardViewMode.GRID,
            weekStartMonday = prefs[Keys.WEEK_START_MONDAY] ?: true,
            showStreakBadges = prefs[Keys.SHOW_STREAK_BADGES] ?: true,
            showCategoryFilter = prefs[Keys.SHOW_CATEGORY_FILTER] ?: true,
            timezoneOffsetMinutes = tzOffset,
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setDashboardViewMode(mode: DashboardViewMode) {
        context.dataStore.edit { it[Keys.DASHBOARD_VIEW_MODE] = mode.name }
    }

    suspend fun setWeekStartMonday(monday: Boolean) {
        context.dataStore.edit { it[Keys.WEEK_START_MONDAY] = monday }
    }

    suspend fun setShowStreakBadges(show: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_STREAK_BADGES] = show }
    }

    suspend fun setShowCategoryFilter(show: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_CATEGORY_FILTER] = show }
    }

    suspend fun setTimezoneOffsetMinutes(offsetMinutes: Int?) {
        context.dataStore.edit {
            if (offsetMinutes == null) {
                it[Keys.HAS_CUSTOM_TIMEZONE] = false
                it.remove(Keys.TIMEZONE_OFFSET_MINUTES)
            } else {
                it[Keys.HAS_CUSTOM_TIMEZONE] = true
                it[Keys.TIMEZONE_OFFSET_MINUTES] = offsetMinutes
            }
        }
    }
}
