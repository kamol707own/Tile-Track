package com.example.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.TileTrackApplication
import com.example.domain.HabitReminder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getStringExtra(ReminderScheduler.EXTRA_HABIT_ID) ?: return
        val reminderId = intent.getStringExtra(ReminderScheduler.EXTRA_REMINDER_ID) ?: return
        val hour = intent.getIntExtra(ReminderScheduler.EXTRA_HOUR, 9)
        val minute = intent.getIntExtra(ReminderScheduler.EXTRA_MINUTE, 0)
        val daysMask = intent.getIntExtra(
            ReminderScheduler.EXTRA_DAYS_MASK,
            HabitReminder.ALL_DAYS_MASK,
        )

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ReminderScheduler.ensureNotificationChannel(context)
                val app = context.applicationContext as? TileTrackApplication
                val habitName = app?.container?.habitRepository?.getHabitName(habitId) ?: "Habit Check-in"

                val canNotify = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) == PackageManager.PERMISSION_GRANTED
                } else {
                    true
                }

                if (canNotify) {
                    val launchIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra(ReminderScheduler.EXTRA_HABIT_ID, habitId)
                    }
                    val contentPendingIntent = PendingIntent.getActivity(
                        context,
                        habitId.hashCode(),
                        launchIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    )

                    val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle(habitName)
                        .setContentText("Tap to log today's tile and keep your streak alive.")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(contentPendingIntent)
                        .setAutoCancel(true)
                        .build()

                    NotificationManagerCompat.from(context).notify(habitId.hashCode(), notification)
                }

                // Reschedule next occurrence
                ReminderScheduler.scheduleReminder(
                    context,
                    HabitReminder(
                        id = reminderId,
                        habitId = habitId,
                        hour = hour,
                        minute = minute,
                        daysOfWeekMask = daysMask,
                        isEnabled = true,
                    ),
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}
