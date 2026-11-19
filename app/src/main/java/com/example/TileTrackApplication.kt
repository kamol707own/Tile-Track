package com.example

import android.app.Application
import android.content.Context
import com.example.data.local.LocalRoomHabitRepository
import com.example.data.local.TileTrackDatabase
import com.example.data.preferences.UserPreferencesRepository
import com.example.domain.HabitRepository
import com.example.notifications.ReminderScheduler

class AppContainer(context: Context) {
    private val database: TileTrackDatabase = TileTrackDatabase.getInstance(context)
    val habitRepository: HabitRepository = LocalRoomHabitRepository(database.habitDao())
    val preferencesRepository: UserPreferencesRepository = UserPreferencesRepository(context)
}

class TileTrackApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        ReminderScheduler.ensureNotificationChannel(this)
    }
}
