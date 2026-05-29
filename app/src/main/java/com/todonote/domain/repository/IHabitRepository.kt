package com.todonote.domain.repository

import com.todonote.domain.model.Habit
import kotlinx.coroutines.flow.Flow

interface IHabitRepository {
    fun getActive(): Flow<List<Habit>>
    suspend fun getById(id: Long): Habit?
    suspend fun insert(habit: Habit): Long
    suspend fun update(habit: Habit)
    suspend fun delete(habit: Habit)
}
