package com.todonote.data.local.dao

import androidx.room.*
import com.todonote.data.local.entity.PomodoroSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroSessionDao {
    @Query("SELECT * FROM pomodoro_sessions ORDER BY startedAt DESC")
    fun getAll(): Flow<List<PomodoroSessionEntity>>

    @Query("SELECT * FROM pomodoro_sessions ORDER BY startedAt DESC")
    suspend fun getAllSync(): List<PomodoroSessionEntity>

    @Query("SELECT * FROM pomodoro_sessions WHERE taskId = :taskId ORDER BY startedAt DESC")
    fun getByTaskId(taskId: Long): Flow<List<PomodoroSessionEntity>>

    @Query("SELECT * FROM pomodoro_sessions WHERE startedAt >= :dayStart ORDER BY startedAt DESC")
    fun getFromDate(dayStart: Long): Flow<List<PomodoroSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: PomodoroSessionEntity): Long

    @Update
    suspend fun update(session: PomodoroSessionEntity)

    @Delete
    suspend fun delete(session: PomodoroSessionEntity)

    @Query("DELETE FROM pomodoro_sessions")
    suspend fun deleteAll()
}
