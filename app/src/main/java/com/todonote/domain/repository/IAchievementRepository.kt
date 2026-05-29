package com.todonote.domain.repository

import com.todonote.domain.model.Achievement
import kotlinx.coroutines.flow.Flow

interface IAchievementRepository {
    fun getAll(): Flow<List<Achievement>>
    fun getUnlocked(): Flow<List<Achievement>>
    suspend fun insert(achievement: Achievement): Long
    suspend fun update(achievement: Achievement)
    suspend fun unlock(id: Long)
}
