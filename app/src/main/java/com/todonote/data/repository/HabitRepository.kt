package com.todonote.data.repository

import com.todonote.data.local.dao.HabitDao
import com.todonote.data.local.entity.HabitEntity
import com.todonote.domain.model.Frequency
import com.todonote.domain.model.Habit
import com.todonote.domain.repository.IHabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HabitRepository(private val habitDao: HabitDao) : IHabitRepository {

    override fun getActive(): Flow<List<Habit>> = habitDao.getActive().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun getById(id: Long): Habit? = habitDao.getById(id)?.toDomain()

    override suspend fun insert(habit: Habit): Long = habitDao.insert(habit.toEntity())

    override suspend fun update(habit: Habit) = habitDao.update(habit.toEntity())

    override suspend fun delete(habit: Habit) = habitDao.delete(habit.toEntity())

    private fun HabitEntity.toDomain() = Habit(
        id = id,
        name = name,
        description = description,
        frequency = Frequency.fromValue(frequency),
        targetCount = targetCount,
        color = color,
        icon = icon,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isArchived = isArchived,
        currentStreak = currentStreak,
        bestStreak = bestStreak
    )

    private fun Habit.toEntity() = HabitEntity(
        id = id,
        name = name,
        description = description,
        frequency = frequency.value,
        targetCount = targetCount,
        color = color,
        icon = icon,
        createdAt = createdAt,
        updatedAt = System.currentTimeMillis(),
        isArchived = isArchived,
        currentStreak = currentStreak,
        bestStreak = bestStreak
    )
}
