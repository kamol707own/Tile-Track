package com.example.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.R
import com.example.domain.HabitReminder
import java.util.Calendar

object ReminderScheduler {
    const val CHANNEL_ID = "tiletrack_reminders"
    const val EXTRA_HABIT_ID = "extra_habit_id"
    const val EXTRA_REMINDER_ID = "extra_reminder_id"
    const val EXTRA_HOUR = "extra_hour"
    const val EXTRA_MINUTE = "extra_minute"
    const val EXTRA_DAYS_MASK = "extra_days_mask"

    fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val existing = manager.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.reminder_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = context.getString(R.string.reminder_channel_desc)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    fun syncHabitReminders(context: Context, habitId: String, reminders: List<HabitReminder>) {
        reminders.forEach { reminder ->
            if (reminder.isEnabled && reminder.daysOfWeekMask != 0) {
                scheduleReminder(context, reminder)
            } else {
                cancelReminder(context, reminder.id)
            }
        }
    }

    fun cancelReminders(context: Context, reminders: List<HabitReminder>) {
        reminders.forEach { cancelReminder(context, it.id) }
    }

    fun scheduleReminder(context: Context, reminder: HabitReminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAtMillis = computeNextTriggerMillis(
            hour = reminder.hour,
            minute = reminder.minute,
            daysOfWeekMask = reminder.daysOfWeekMask,
        ) ?: return

        val pendingIntent = buildPendingIntent(context, reminder)
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent,
        )
    }

    fun cancelReminder(context: Context, reminderId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun buildPendingIntent(context: Context, reminder: HabitReminder): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_HABIT_ID, reminder.habitId)
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_HOUR, reminder.hour)
            putExtra(EXTRA_MINUTE, reminder.minute)
            putExtra(EXTRA_DAYS_MASK, reminder.daysOfWeekMask)
        }
        return PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    fun computeNextTriggerMillis(hour: Int, minute: Int, daysOfWeekMask: Int): Long? {
        if (daysOfWeekMask == 0) return null
        val now = Calendar.getInstance()
        for (dayOffset in 0..7) {
            val candidate = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, dayOffset)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (candidate.timeInMillis <= now.timeInMillis + 1_000L) continue
            val isoDay = calendarDayToIso(candidate.get(Calendar.DAY_OF_WEEK))
            if ((daysOfWeekMask and (1 shl (isoDay - 1))) != 0) {
                return candidate.timeInMillis
            }
        }
        return null
    }

    private fun calendarDayToIso(calendarDay: Int): Int {
        return when (calendarDay) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }
}
