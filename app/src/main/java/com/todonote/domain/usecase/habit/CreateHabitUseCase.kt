package com.todonote.domain.usecase.habit

import com.todonote.domain.model.Habit
import com.todonote.domain.repository.IHabitRepository

class CreateHabitUseCase(private val repository: IHabitRepository) {
    suspend operator fun invoke(habit: Habit): Long = repository.insert(habit)
}
