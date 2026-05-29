package com.todonote.domain.usecase.habit

import com.todonote.data.local.dao.HabitCheckInDao
import com.todonote.data.local.entity.HabitCheckInEntity
import com.todonote.domain.model.Habit
import com.todonote.domain.repository.IHabitRepository
import kotlinx.coroutines.flow.first

class CheckInHabitUseCase(
    private val habitRepository: IHabitRepository,
    private val habitCheckInDao: HabitCheckInDao,
    private val calculateStreakUseCase: CalculateStreakUseCase
) {
    suspend operator fun invoke(habitId: Long, date: Long, isComplete: Boolean = true, note: String = "") {
        val existing = habitCheckInDao.getByHabitAndDate(habitId, date)
        if (existing != null) {
            habitCheckInDao.update(existing.copy(isComplete = isComplete, note = note))
        } else {
            habitCheckInDao.insert(
                HabitCheckInEntity(
                    habitId = habitId,
                    date = date,
                    isComplete = isComplete,
                    note = note
                )
            )
        }

        // Recalculate streaks
        val checkIns = habitCheckInDao.getByHabitId(habitId).first()
        val (currentStreak, bestStreak) = calculateStreakUseCase(checkIns)

        val habit = habitRepository.getById(habitId)
        if (habit != null) {
            habitRepository.update(
                habit.copy(
                    currentStreak = currentStreak,
                    bestStreak = bestStreak
                )
            )
        }
    }
}
