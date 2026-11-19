package com.example.domain

import kotlin.math.max
import kotlin.math.roundToInt

object StreakCalculator {

    fun calculateStats(
        habit: Habit,
        logsByEpochDay: Map<Long, CompletionLog>,
        todayEpochDay: Long = DateUtil.todayEpochDay(),
        weekStartMonday: Boolean = true,
    ): HabitStats {
        val target = habit.dailyTarget.coerceAtLeast(1)
        val completedDays = logsByEpochDay
            .filterValues { it.status == DayStatus.DONE || (it.status != DayStatus.MISS && it.count >= target) }
            .keys
            .sorted()

        val missedDaysCount = logsByEpochDay.values.count { it.status == DayStatus.MISS }

        val completedSet = completedDays.toHashSet()
        val todayLog = logsByEpochDay[todayEpochDay]
        val todayCount = todayLog?.count ?: 0
        val todayStatus = when {
            todayLog?.status == DayStatus.MISS -> DayStatus.MISS
            todayLog?.status == DayStatus.DONE || todayCount >= target -> DayStatus.DONE
            else -> DayStatus.UNDONE
        }
        val isCompletedToday = todayStatus == DayStatus.DONE

        val (currentStreak, bestStreak) = when (habit.streakGoalType) {
            StreakGoalType.WEEKLY -> computePeriodStreaks(
                completedDays = completedDays,
                goalTarget = habit.streakGoalTarget.coerceAtLeast(1),
                todayEpochDay = todayEpochDay,
                periodKeyFn = { day -> DateUtil.startOfWeekEpochDay(day, weekStartMonday) },
                previousPeriodFn = { key -> key - 7L },
            )
            StreakGoalType.MONTHLY -> computeMonthStreaks(
                completedDays = completedDays,
                goalTarget = habit.streakGoalTarget.coerceAtLeast(1),
                todayEpochDay = todayEpochDay,
            )
            StreakGoalType.DAILY, StreakGoalType.NONE -> computeDailyStreaks(
                completedDays = completedDays,
                completedSet = completedSet,
                todayEpochDay = todayEpochDay,
            )
        }

        val totalCompletions = logsByEpochDay.values
            .filter { it.status != DayStatus.MISS }
            .sumOf { it.count }

        var daysMetInLast30 = 0
        for (offset in 0 until 30) {
            if (completedSet.contains(todayEpochDay - offset)) {
                daysMetInLast30++
            }
        }
        val consistency30DaysPercent = ((daysMetInLast30 / 30f) * 100f).roundToInt().coerceIn(0, 100)

        val todayCivil = DateUtil.epochDayToCivilDate(todayEpochDay)
        val monthlyBars = (5 downTo 0).map { monthsBack ->
            var y = todayCivil.year
            var m = todayCivil.month - monthsBack
            while (m <= 0) {
                m += 12
                y -= 1
            }
            val daysInM = DateUtil.daysInMonth(y, m)
            val startDay = DateUtil.yearMonthDayToEpochDay(y, m, 1)
            val endDay = minOf(DateUtil.yearMonthDayToEpochDay(y, m, daysInM), todayEpochDay)
            var count = 0
            if (endDay >= startDay) {
                for (d in startDay..endDay) {
                    if (completedSet.contains(d)) count++
                }
            }
            val totalElapsedDays = if (endDay >= startDay) (endDay - startDay + 1).toInt() else daysInM
            MonthlyBarPoint(
                label = DateUtil.monthShortLabel(m),
                completedDays = count,
                totalDays = totalElapsedDays.coerceAtLeast(1),
            )
        }

        val countsByIsoDay = IntArray(8)
        for (day in completedDays) {
            val iso = DateUtil.dayOfWeekIso(day)
            countsByIsoDay[iso]++
        }
        val maxWeekdayCount = (1..7).maxOf { countsByIsoDay[it] }.coerceAtLeast(1)
        val weekdayStats = (1..7).map { iso ->
            val c = countsByIsoDay[iso]
            WeekdayStat(
                isoDay = iso,
                label = DateUtil.weekdayShortLabel(iso),
                completionCount = c,
                ratio = c.toFloat() / maxWeekdayCount.toFloat(),
            )
        }

        return HabitStats(
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            totalCompletions = totalCompletions,
            completedDaysCount = completedDays.size,
            missedDaysCount = missedDaysCount,
            consistency30DaysPercent = consistency30DaysPercent,
            todayCount = todayCount,
            todayStatus = todayStatus,
            isCompletedToday = isCompletedToday,
            monthlyBars = monthlyBars,
            weekdayStats = weekdayStats,
        )
    }

    private fun computeDailyStreaks(
        completedDays: List<Long>,
        completedSet: Set<Long>,
        todayEpochDay: Long,
    ): Pair<Int, Int> {
        if (completedDays.isEmpty()) return 0 to 0

        var current = 0
        var cursor = if (completedSet.contains(todayEpochDay)) {
            todayEpochDay
        } else if (completedSet.contains(todayEpochDay - 1)) {
            todayEpochDay - 1
        } else {
            Long.MIN_VALUE
        }

        if (cursor != Long.MIN_VALUE) {
            while (completedSet.contains(cursor)) {
                current++
                cursor--
            }
        }

        var best = 0
        var run = 0
        var prev = Long.MIN_VALUE
        for (day in completedDays) {
            if (prev != Long.MIN_VALUE && day == prev + 1L) {
                run++
            } else {
                run = 1
            }
            best = max(best, run)
            prev = day
        }

        return current to max(best, current)
    }

    private fun computePeriodStreaks(
        completedDays: List<Long>,
        goalTarget: Int,
        todayEpochDay: Long,
        periodKeyFn: (Long) -> Long,
        previousPeriodFn: (Long) -> Long,
    ): Pair<Int, Int> {
        if (completedDays.isEmpty()) return 0 to 0
        val countsByPeriod = mutableMapOf<Long, Int>()
        for (day in completedDays) {
            val key = periodKeyFn(day)
            countsByPeriod[key] = (countsByPeriod[key] ?: 0) + 1
        }
        val metPeriods = countsByPeriod.filterValues { it >= goalTarget }.keys.sorted()
        val metSet = metPeriods.toHashSet()

        val currentPeriod = periodKeyFn(todayEpochDay)
        val prevPeriod = previousPeriodFn(currentPeriod)
        var current = 0
        var cursor = when {
            metSet.contains(currentPeriod) -> currentPeriod
            metSet.contains(prevPeriod) -> prevPeriod
            else -> Long.MIN_VALUE
        }
        if (cursor != Long.MIN_VALUE) {
            while (metSet.contains(cursor)) {
                current++
                cursor = previousPeriodFn(cursor)
            }
        }

        var best = 0
        var run = 0
        var prevKey = Long.MIN_VALUE
        for (key in metPeriods) {
            if (prevKey != Long.MIN_VALUE && previousPeriodFn(key) == prevKey) {
                run++
            } else {
                run = 1
            }
            best = max(best, run)
            prevKey = key
        }
        return current to max(best, current)
    }

    private fun computeMonthStreaks(
        completedDays: List<Long>,
        goalTarget: Int,
        todayEpochDay: Long,
    ): Pair<Int, Int> {
        if (completedDays.isEmpty()) return 0 to 0
        val countsByMonth = mutableMapOf<Int, Int>()
        for (day in completedDays) {
            val key = DateUtil.monthKey(day)
            countsByMonth[key] = (countsByMonth[key] ?: 0) + 1
        }
        val metMonths = countsByMonth.filterValues { it >= goalTarget }.keys.sorted()
        val metSet = metMonths.toHashSet()

        fun prevMonth(ym: Int): Int {
            val y = ym / 100
            val m = ym % 100
            return if (m == 1) (y - 1) * 100 + 12 else y * 100 + (m - 1)
        }

        val currentYm = DateUtil.monthKey(todayEpochDay)
        val prevYm = prevMonth(currentYm)
        var current = 0
        var cursor = when {
            metSet.contains(currentYm) -> currentYm
            metSet.contains(prevYm) -> prevYm
            else -> -1
        }
        if (cursor != -1) {
            while (metSet.contains(cursor)) {
                current++
                cursor = prevMonth(cursor)
            }
        }

        var best = 0
        var run = 0
        var prev = -1
        for (ym in metMonths) {
            if (prev != -1 && prevMonth(ym) == prev) {
                run++
            } else {
                run = 1
            }
            best = max(best, run)
            prev = ym
        }
        return current to max(best, current)
    }
}
