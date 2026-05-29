package com.todonote.domain.usecase.habit

import com.todonote.domain.model.Habit
import com.todonote.domain.repository.IHabitRepository
import kotlinx.coroutines.flow.Flow

class GetHabitsUseCase(private val repository: IHabitRepository) {
    operator fun invoke(): Flow<List<Habit>> = repository.getActive()
}
