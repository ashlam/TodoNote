package com.todonote.data.repository

import com.todonote.data.local.dao.AchievementDao
import com.todonote.data.local.entity.AchievementEntity
import com.todonote.domain.model.Achievement
import com.todonote.domain.repository.IAchievementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AchievementRepository(private val achievementDao: AchievementDao) : IAchievementRepository {

    override fun getAll(): Flow<List<Achievement>> = achievementDao.getAll().map { entities ->
        entities.map { it.toDomain() }
    }

    override fun getUnlocked(): Flow<List<Achievement>> = achievementDao.getUnlocked().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun insert(achievement: Achievement): Long = achievementDao.insert(achievement.toEntity())

    override suspend fun update(achievement: Achievement) = achievementDao.update(achievement.toEntity())

    override suspend fun unlock(id: Long) = achievementDao.unlock(id)

    private fun AchievementEntity.toDomain() = Achievement(
        id = id,
        name = name,
        description = description,
        iconRes = iconRes,
        conditionType = conditionType,
        conditionValue = conditionValue,
        unlockedAt = unlockedAt,
        isUnlocked = unlockedAt != null
    )

    private fun Achievement.toEntity() = AchievementEntity(
        id = id,
        name = name,
        description = description,
        iconRes = iconRes,
        conditionType = conditionType,
        conditionValue = conditionValue,
        unlockedAt = if (isUnlocked) System.currentTimeMillis() else null
    )
}
