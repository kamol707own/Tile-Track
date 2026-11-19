package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.notifications.ReminderScheduler
import com.example.ui.TileTrackApp

class MainActivity : ComponentActivity() {

    private var pendingHabitId by mutableStateOf<String?>(null)

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingHabitId = intent?.getStringExtra(ReminderScheduler.EXTRA_HABIT_ID)

        val app = application as TileTrackApplication
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            TileTrackApp(
                container = app.container,
                appContext = applicationContext,
                widthSizeClass = windowSizeClass.widthSizeClass,
                initialHabitId = pendingHabitId,
                onConsumedInitialHabitId = { pendingHabitId = null },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val habitId = intent.getStringExtra(ReminderScheduler.EXTRA_HABIT_ID)
        if (!habitId.isNullOrBlank()) {
            pendingHabitId = habitId
        }
    }
}
