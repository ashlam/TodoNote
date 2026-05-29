package com.todonote.domain.usecase.habit

import com.todonote.domain.model.Habit
import com.todonote.domain.repository.IHabitRepository

class DeleteHabitUseCase(private val repository: IHabitRepository) {
    suspend operator fun invoke(habit: Habit) = repository.delete(habit)
}
