package com.todonote.domain.usecase.habit

import com.todonote.data.local.entity.HabitCheckInEntity
import java.util.Calendar
import java.util.concurrent.TimeUnit

class CalculateStreakUseCase {
    operator fun invoke(checkIns: List<HabitCheckInEntity>): Pair<Int, Int> {
        if (checkIns.isEmpty()) return 0 to 0

        val completedDates = checkIns
            .filter { it.isComplete }
            .map { normalizeDate(it.date) }
            .distinct()
            .sortedDescending()

        if (completedDates.isEmpty()) return 0 to 0

        val today = normalizeDate(System.currentTimeMillis())

        // Calculate current streak (consecutive days from today backwards)
        var currentStreak = 0
        var checkDate = today

        // If no check-in today, current streak starts from yesterday
        if (!completedDates.contains(today)) {
            checkDate = today - TimeUnit.DAYS.toMillis(1)
        }

        while (completedDates.contains(checkDate)) {
            currentStreak++
            checkDate -= TimeUnit.DAYS.toMillis(1)
        }

        // Calculate best streak
        var bestStreak = 0
        var tempStreak = 1

        for (i in 1 until completedDates.size) {
            val prev = completedDates[i - 1]
            val curr = completedDates[i]
            if (prev - curr == TimeUnit.DAYS.toMillis(1)) {
                tempStreak++
            } else {
                if (tempStreak > bestStreak) bestStreak = tempStreak
                tempStreak = 1
            }
        }
        if (tempStreak > bestStreak) bestStreak = tempStreak

        return currentStreak to bestStreak
    }

    private fun normalizeDate(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}
