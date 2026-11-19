package com.example

import com.example.domain.CompletionLog
import com.example.domain.DateUtil
import com.example.domain.DayStatus
import com.example.domain.Habit
import com.example.domain.StreakCalculator
import com.example.domain.StreakGoalType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StreakCalculatorTest {

    private fun sampleHabit(
        dailyTarget: Int = 1,
        goalType: StreakGoalType = StreakGoalType.DAILY,
        goalTarget: Int = 1,
    ) = Habit(
        id = "h1",
        name = "Read",
        description = "",
        iconKey = "book",
        colorArgb = 0xFF22C55E,
        category = "Learning",
        dailyTarget = dailyTarget,
        streakGoalType = goalType,
        streakGoalTarget = goalTarget,
        sortOrder = 0,
        isArchived = false,
        createdAt = 0L,
        updatedAt = 0L,
    )

    private fun log(day: Long, count: Int, status: DayStatus = DayStatus.DONE) = CompletionLog(
        id = "l_$day",
        habitId = "h1",
        dateEpochDay = day,
        count = count,
        status = status,
        note = "",
        updatedAt = 0L,
    )

    @Test
    fun `daily streak counts consecutive days including active yesterday grace`() {
        val today = 20_000L
        val logs = mapOf(
            (today - 1) to log(today - 1, 1),
            (today - 2) to log(today - 2, 1),
            (today - 3) to log(today - 3, 1),
            (today - 10) to log(today - 10, 1),
        )
        val stats = StreakCalculator.calculateStats(
            habit = sampleHabit(),
            logsByEpochDay = logs,
            todayEpochDay = today,
        )
        assertEquals(3, stats.currentStreak)
        assertEquals(3, stats.bestStreak)
        assertFalse(stats.isCompletedToday)
        assertEquals(4, stats.completedDaysCount)
        assertEquals(0, stats.missedDaysCount)
    }

    @Test
    fun `miss status breaks streak and increments missedDaysCount`() {
        val today = 20_000L
        val logs = mapOf(
            today to log(today, 1, DayStatus.DONE),
            (today - 1) to log(today - 1, 0, DayStatus.MISS),
            (today - 2) to log(today - 2, 1, DayStatus.DONE),
            (today - 3) to log(today - 3, 1, DayStatus.DONE),
        )
        val stats = StreakCalculator.calculateStats(
            habit = sampleHabit(),
            logsByEpochDay = logs,
            todayEpochDay = today,
        )
        assertEquals(1, stats.currentStreak)
        assertEquals(2, stats.bestStreak)
        assertEquals(1, stats.missedDaysCount)
        assertEquals(DayStatus.DONE, stats.todayStatus)
    }

    @Test
    fun `multi-completion daily target only counts days meeting full target`() {
        val today = 20_000L
        val logs = mapOf(
            today to log(today, 3, DayStatus.DONE),
            (today - 1) to log(today - 1, 2, DayStatus.UNDONE),
            (today - 2) to log(today - 2, 3, DayStatus.DONE),
        )
        val stats = StreakCalculator.calculateStats(
            habit = sampleHabit(dailyTarget = 3),
            logsByEpochDay = logs,
            todayEpochDay = today,
        )
        assertEquals(1, stats.currentStreak)
        assertEquals(1, stats.bestStreak)
        assertTrue(stats.isCompletedToday)
        assertEquals(8, stats.totalCompletions)
        assertEquals(2, stats.completedDaysCount)
    }

    @Test
    fun `epochDay round-trip and weekday calculation are exact`() {
        val wednesday = DateUtil.yearMonthDayToEpochDay(2026, 11, 18)
        val thursday = DateUtil.yearMonthDayToEpochDay(2026, 11, 19)
        assertEquals(3, DateUtil.dayOfWeekIso(wednesday)) // Wed
        assertEquals(4, DateUtil.dayOfWeekIso(thursday)) // Thu
        val civil = DateUtil.epochDayToCivilDate(thursday)
        assertEquals(2026, civil.year)
        assertEquals(11, civil.month)
        assertEquals(19, civil.day)
    }
}
