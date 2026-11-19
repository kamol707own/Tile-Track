package com.example.domain

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * API 24+ safe local calendar date utilities using epoch days (days since 1970-01-01).
 * Supports optional explicit UTC offset override when device/emulator timezone differs.
 */
object DateUtil {
    private val monthNamesShort = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
    )

    private val monthNamesFull = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December",
    )

    private val weekdayNamesShort = arrayOf(
        "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun",
    )

    fun todayEpochDay(utcOffsetMinutes: Int? = null): Long {
        val cal = if (utcOffsetMinutes == null) {
            Calendar.getInstance()
        } else {
            val sign = if (utcOffsetMinutes >= 0) "+" else "-"
            val absMin = kotlin.math.abs(utcOffsetMinutes)
            val tzId = String.format(Locale.US, "GMT%s%02d:%02d", sign, absMin / 60, absMin % 60)
            Calendar.getInstance(TimeZone.getTimeZone(tzId))
        }
        return yearMonthDayToEpochDay(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
        )
    }

    /**
     * Howard Hinnant civil-from-days / days-from-civil algorithm.
     * Month is 1..12, Day is 1..31.
     */
    fun yearMonthDayToEpochDay(year: Int, month: Int, day: Int): Long {
        val y = if (month <= 2) year - 1 else year
        val era = if (y >= 0) y / 400 else (y - 399) / 400
        val yoe = y - era * 400
        val m = month
        val doy = (153 * (m + (if (m > 2) -3 else 9)) + 2) / 5 + day - 1
        val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        return (era * 146097L + doe - 719468L)
    }

    data class CivilDate(val year: Int, val month: Int, val day: Int)

    fun epochDayToCivilDate(epochDay: Long): CivilDate {
        val z = epochDay + 719468L
        val era = if (z >= 0) z / 146097L else (z - 146096L) / 146097L
        val doe = (z - era * 146097L).toInt()
        val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
        val y = yoe + era.toInt() * 400
        val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
        val mp = (5 * doy + 2) / 153
        val d = doy - (153 * mp + 2) / 5 + 1
        val m = mp + if (mp < 10) 3 else -9
        val finalYear = y + if (m <= 2) 1 else 0
        return CivilDate(finalYear, m, d)
    }

    /**
     * ISO-8601 day of week: 1 = Monday .. 7 = Sunday.
     * 1970-01-01 (epochDay 0) was Thursday (4).
     */
    fun dayOfWeekIso(epochDay: Long): Int {
        val mod = ((epochDay + 3L) % 7L + 7L) % 7L
        return mod.toInt() + 1
    }

    fun weekdayShortLabel(isoDay: Int): String = weekdayNamesShort[(isoDay - 1).coerceIn(0, 6)]

    fun monthShortLabel(month1Based: Int): String = monthNamesShort[(month1Based - 1).coerceIn(0, 11)]

    fun monthFullLabel(month1Based: Int): String = monthNamesFull[(month1Based - 1).coerceIn(0, 11)]

    fun formatMonthYear(epochDay: Long): String {
        val d = epochDayToCivilDate(epochDay)
        return "${monthFullLabel(d.month)} ${d.year}"
    }

    fun formatShortDate(epochDay: Long): String {
        val d = epochDayToCivilDate(epochDay)
        return "${monthShortLabel(d.month)} ${d.day}"
    }

    fun formatFullDate(epochDay: Long): String {
        val d = epochDayToCivilDate(epochDay)
        val wd = weekdayShortLabel(dayOfWeekIso(epochDay))
        return "$wd, ${monthShortLabel(d.month)} ${d.day}, ${d.year}"
    }

    fun formatTime(hour: Int, minute: Int): String {
        val amPm = if (hour < 12) "AM" else "PM"
        val h12 = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return String.format(Locale.US, "%d:%02d %s", h12, minute, amPm)
    }

    fun startOfWeekEpochDay(epochDay: Long, weekStartMonday: Boolean): Long {
        val iso = dayOfWeekIso(epochDay) // 1=Mon .. 7=Sun
        val offset = if (weekStartMonday) {
            iso - 1
        } else {
            if (iso == 7) 0 else iso
        }
        return epochDay - offset
    }

    fun daysInMonth(year: Int, month: Int): Int {
        return when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) 29 else 28
            else -> 30
        }
    }

    fun monthKey(epochDay: Long): Int {
        val d = epochDayToCivilDate(epochDay)
        return d.year * 100 + d.month
    }

    fun formatUtcOffsetLabel(offsetMinutes: Int?): String {
        if (offsetMinutes == null) return "Device Auto"
        val sign = if (offsetMinutes >= 0) "+" else "-"
        val absMin = kotlin.math.abs(offsetMinutes)
        val hrs = absMin / 60
        val mins = absMin % 60
        return if (mins == 0) "UTC$sign$hrs" else String.format(Locale.US, "UTC%s%d:%02d", sign, hrs, mins)
    }

    fun formatTimestamp(epochMillis: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(epochMillis)
    }
}
