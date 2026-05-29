package com.todonote.data.local.dao

import androidx.room.*
import com.todonote.data.local.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY unlockedAt DESC")
    fun getAll(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements ORDER BY unlockedAt DESC")
    suspend fun getAllSync(): List<AchievementEntity>

    @Query("SELECT * FROM achievements WHERE unlockedAt IS NOT NULL ORDER BY unlockedAt DESC")
    fun getUnlocked(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(achievement: AchievementEntity): Long

    @Update
    suspend fun update(achievement: AchievementEntity)

    @Query("UPDATE achievements SET unlockedAt = :unlockedAt WHERE id = :id")
    suspend fun unlock(id: Long, unlockedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM achievements")
    suspend fun deleteAll()
}
